/*
MIT License

Copyright (c) 2020 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.ide.launcher;

import java.io.File;

import org.eclipse.ui.console.IConsoleView;

import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEException;
import info.julang.ide.launcher.console.ConsoleManager;
import info.julang.ide.launcher.console.JulianConsole;
import info.julang.ide.launcher.io.DefaultIOSet;
import info.julang.ide.launcher.io.SystemIO;
import info.julang.ide.reporting.LimitationWarner;

/**
 * Run a Julian script file. Effectively a singleton.
 * 
 * @author Ming Zhou
 */
public class JulianRunner {
	
	private static boolean s_live = false;
	private static String s_path = "";
	
	private JulianScriptEngine jse;
	private String[] arguments;
	private IConsoleView view;
	private ITerminationListener termListener;
	private String mode;
	
	public JulianRunner(String mode) {
		jse = new JulianScriptEngine(true, false);
		this.mode = mode;
	}
	
	public void setConsoleView(IConsoleView view) {
		this.view = view;
	}
	
	public void AddModuleDirs(File[] modDirs) {
		if (modDirs != null) {
			for (File f : modDirs) {
				String path = f.getAbsolutePath();
				jse.addModulePath(path);
			}
		}	
	}
	
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}
	
	public void run(String path) {
		if ("debug".equalsIgnoreCase(mode)) {
			LimitationWarner.showLimitation(view != null ? view.getSite() : null, "Debug mode is not supported.");
			return;
		}
		
		boolean canRun = !s_live;
		if (canRun) {
			synchronized (JulianRunner.class) {
				canRun = !s_live;
				if (canRun) {
					s_path = path;
					s_live = true;
					run0(path);
				}
			}
		}
		
		if (!canRun) {
			LimitationWarner.showLimitation(
				view != null ? view.getSite() : null,
				"Cannot run another Julian script (" + path + ") while one (" + s_path + ") is already running.");
		}
	}
	
	public void stop() {
		jse.stopRunning();
	}
	
	public boolean isRunning() {
		return s_live;
	}
	
	public void setTerminationListener(ITerminationListener listener) {
		termListener = listener;
	}
	
	public void run0(String path) {
		if (arguments == null) {
			arguments = new String[0];
		}
		
		SystemIO sysIo = SystemIO.getInstance();
		
		// Create a new set of streams targeting the JSE console.
		JulianConsole console = ConsoleManager.create(this);
		
		// Replace the System streams with the new streams, redirecting all System.out/err/in operation to the IDE console.
		sysIo.saveFrom(console);

		if (view != null) {
			view.display(console);
		}
			
		Thread t = new Thread(() -> {
			try {
				jse.runFile(path, arguments);
			} catch (JSEException e) {
				// Normally this shouldn't happen. All script errors should be handled inside the engine.
			} finally {
				// Invoke the listener
				ITerminationListener tl = this.termListener;
				if (tl != null) {
					try {
						tl.onTermination();
					} catch (Throwable listenerEx) {
						// Don't care
					}
				}
				
				// Restore. This must happen no matter what.
				sysIo.saveFrom(DefaultIOSet.getInstance());
				
				synchronized (JulianRunner.class) {
					s_path = "";
					s_live = false;
				}
			}
		});
		
		t.start();
	}
}
