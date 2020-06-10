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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

import info.julang.ide.properties.JulianScriptProperties;
import info.julang.ide.reporting.SafeRunner;
import info.julang.ide.util.FSUtil;
import info.julang.ide.util.ResourceUtil;
import info.julang.ide.util.WindowUtil;

/**
 * Launching logic to be triggered from "Run Configurations...".
 * 
 * @author Ming Zhou
 */
public class JulianLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
	
	// UI
	// Tab: Main
	// 		- File selection
	// Tab: Arguments
	//	    - Arguments textbox
	// Tab: Modules
	//      - Module paths
	
	@Override
	public void launch(
		ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) {
		SafeRunner.run(() -> {
			// 1. Get attributes from the UI
			File script = JulianLaunchConfigHelper.getScriptFile(configuration);
			File[] modDirs = JulianLaunchConfigHelper.getModulePaths(configuration);
			String args = JulianLaunchConfigHelper.getArguments(configuration);
			boolean shouldAppendOrReplace = JulianLaunchConfigHelper.isAppendProjectLevelModulePaths(configuration);
			
			// 2. Prepare a runner
			JulianRunner runner = new JulianRunner(mode);
			
			// - Show console view
			try {
				IWorkbenchPage page = WindowUtil.getActivePage();
				if (page != null) {
					IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
					runner.setConsoleView(view);
				}
			} catch (PartInitException e) {
				// The console view cannot be initialized - likely a problem with the IDE or environment.
			}
			
			// - Set module paths
			// -- Project-level
			if (shouldAppendOrReplace) {
				List<IProject> projects = ResourceUtil.getJulianProjects(true);
				IProject project = findProject(projects, script);
				if (project != null) {
					File[] dirs = FSUtil.fromFSPathArray(
						project.getPersistentProperty(
							JulianScriptProperties.MODULE_PATHS_PATHARRAY_PROPERTY));
					runner.AddModuleDirs(dirs);
				}
			}
			
			// -- Run-level
			runner.AddModuleDirs(modDirs);
			
			// - Set arguments
			//   (Must first process double quotes, including the escaping inside the quotes)
			String[] arguments = extractArguments(args);
			
			runner.setArguments(arguments);
			
			// 3. Run
			runner.run(script.getAbsolutePath());
		});
	}
	
	private static String[] extractArguments(String rawArgs) {
		List<String> results = new ArrayList<String>();
		int offset = 0;
		int qindex = -1;
		while(true) {
			// Find the next "
			qindex = rawArgs.indexOf('"', offset);
			
			if (qindex >= offset) {
				// Found. Locate the next unescaped "
				int offset2 = qindex + 1;
				int qindex2 = -1;
				boolean found = false;
				while(true) {
					qindex2 = rawArgs.indexOf('"', offset2);
					if (qindex2 >= offset2) {
						// Found. Check if this is escaped
						if (rawArgs.charAt(qindex2 - 1) != '\\') {
							found = true;
							break;
						} else {
							offset2 = qindex2 + 1;
						}
					} else {
						// Not found the end.
						break;
					}
				}
				
				if (found) {
					// Store the preceding string as multiple arguments separated by blanks.
					String preceding = getSubstring(rawArgs, offset, qindex - 1);
					if (preceding != null) {
						addStringAsMultipleArgs(results, preceding);
					}

					// Store the string inside double-quotes as a single argument
					String current = getSubstring(rawArgs, qindex + 1, qindex2);
					// Also eliminate the escape char
					current = current.replace("\\\"", "\"");
					results.add(current);
					
					offset = qindex2 + 1;
					
					continue;
				}
			}

			break;
		}
		
		// Add the rest
		String rest = rawArgs.substring(offset);
		addStringAsMultipleArgs(results, rest);
		
		String[] ret = new String[results.size()];
		return results.toArray(ret);
	}
	
	private static String getSubstring(String raw, int start, int stop) {
		if (stop < start) {
			return null;
		}
		
		if (start < 0) {
			start = 0;
		}
		
		if (stop > raw.length()) {
			stop = raw.length();
		}
		
		return raw.substring(start, stop);
	}

	private static void addStringAsMultipleArgs(List<String> results, String preceding) {
		String[] sections = preceding.split("[\\n\\t\\r ]+");
		for (String sec : sections) {
			if (!sec.isBlank()) {
				results.add(sec);
			}
		}
	}
	
	// Running from Run Configuration is not inherently associated with a project. 
	// So we need to come up with something hackish. Maybe a better design is to 
	// add an explicit project selection in the config tab.
	private IProject findProject(List<IProject> projects, File script) {
		for (IProject proj : projects) {
			File dir = new File(ResourceUtil.getAbsoluteFSPath(proj));
			if (FSUtil.isPotentialChildOf(script, dir, false)) {
				return proj;
			}
		}
		
		return null;
	}
	
	//----------- A local test for extractArguments (move to test project when we have one) -----------//
	
	/* *
	private static IOPair[] inputs = new IOPair[] {
		new IOPair("a b c", 				"a", "b", "c"),
		new IOPair("a \"b\" c",				"a", "b", "c"),
		new IOPair("a \"b c\" d \"e\"",		"a", "b c", "d", "e"),
		new IOPair("a \"b c\" \"d e\" ",	"a", "b c", "d e"),
		new IOPair("a \"b \\\" c\" d",		"a", "b \" c", "d"),
		new IOPair("\"b \"\" c\"",			"b ", " c"),
		new IOPair("\"b \\\"\\\" c\"",		"b \"\" c"),
	};
	
	public static void main(String[] arguments) throws Exception {
		
		boolean debugMode = false; 
		// false - assert on failure
		// true  - print input and output
		
		if (!debugMode) {
			// Must enable assert programmatically
		    ClassLoader loader = ClassLoader.getSystemClassLoader();
		    loader.setDefaultAssertionStatus(true);
		    Class<?> c = loader.loadClass("info.julang.ide.launcher.JulianLaunchConfigurationDelegate$LocalTest");
		    LocalTest testInst = (LocalTest) c.getConstructor(Boolean.class).newInstance(debugMode);
		    testInst.run();
		} else {
			LocalTest testInst = new LocalTest(debugMode);
			testInst.run();
		}
	}
	
	public static class LocalTest {

		private boolean debugMode;
		
		public LocalTest(Boolean debugMode){
			this.debugMode = debugMode;
		}
		
		public void run() {			
			for (IOPair pair : inputs) {
				String[] args = extractArguments(pair.input);
				if (debugMode) {
					System.out.println(pair.input);
					System.out.println("=>");
					for (String arg : args) {
						System.out.print("  ");
						System.out.println(arg);
					}
					System.out.println();
				} else {
					int expLen = pair.output.length;
					try {
						assert args.length == expLen;
					} catch (Error e) {
						System.out.println(
							"Input = " + pair.input + " | Expected " + expLen + " arguments but saw " + args.length);
						throw e;
					}
					
					for (int i = 0; i < expLen; i++) {
						try {
							assert args[i].equals(pair.output[i]);
						} catch (Error e) {
							System.out.println(
								"Input = " + pair.input + " | Expected " + pair.output[i] + " but saw " + args[i]);
							throw e;
						}
					}
				}
			}

			System.out.println("Tests passed");
		}
	}
	
	private static class IOPair {
		String input;
		String[] output;
		public IOPair(String input, String... output) {
			this.input = input;
			this.output = output;
		}
		
	}
	// */
}
