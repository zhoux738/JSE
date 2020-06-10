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

package info.julang.ide.launcher.console;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.IUpdate;

import info.julang.ide.PluginImages;
import info.julang.ide.launcher.ITerminationListener;
import info.julang.ide.launcher.JulianRunner;

/**
 * The action to be performed when the Terminate button is clicked.
 * 
 * @author Ming Zhou
 */
public class TerminateAction extends Action implements IUpdate, ITerminationListener {

	// IMPLEMENTATION NOTES
	// Inspire by JDT's debug console:
	// https://github.com/eclipse/eclipse.platform.debug/blob/master/org.eclipse.debug.ui/ui/org/eclipse/debug/internal/ui/views/console/ConsoleTerminateAction.java
	
	private JulianRunner runner;

	/**
	 * Creates a terminate action for the Julian console
	 * 
	 * @param window the window
	 * @param console the Julian console
	 */
	public TerminateAction(IWorkbenchWindow window, JulianConsole console) {
		super("Terminate");
		runner = console.getRunner();
		runner.setTerminationListener(this);
		setToolTipText("Terminate the running Julian script");
		setImageDescriptor(PluginImages.IMG_TERMINATE);
		setDisabledImageDescriptor(PluginImages.IMG_TERMINATE_DISABLED);
		update();
	}

	@Override
	public void update() {
		setEnabled(runner.isRunning());
	}

	@Override
	public void run() {
		runner.stop();
	}
	
	public void onTermination() {
		setEnabled(false);
	}
}