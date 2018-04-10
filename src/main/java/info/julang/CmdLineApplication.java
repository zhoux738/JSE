/*
MIT License

Copyright (c) 2017 Ming Zhou

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

package info.julang;

import info.julang.clapp.CLEnvironment;
import info.julang.clapp.CLExecutor;
import info.julang.clapp.ExecutionArguments;
import info.julang.clapp.IExecutionHelper;
import info.julang.clapp.NamedBinding;
import info.julang.clapp.repl.JSEConsole;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEException;

/**
 * The program entrance when used as a command line application.
 * 
 * @author Ming Zhou
 */
public class CmdLineApplication {

	private static class Helper implements IExecutionHelper {

		private boolean supportsANSIEscaping;
		
		@Override
		public Object getEngineInstance(CLEnvironment env) {
			supportsANSIEscaping = env.supportsANSIEscaping();
			return new JulianScriptEngine(true, env.isInteractiveMode());
		}

		@Override
		public void addModulePaths(Object engine, ExecutionArguments args, String[] paths) {
			JulianScriptEngine jse = (JulianScriptEngine)engine;
			for(String path : paths){
				jse.addModulePath(path);
			}
		}

		@Override
		public Object invokeEngine(Object engine, ExecutionArguments args) throws JSEException {
			JulianScriptEngine jse = (JulianScriptEngine)engine;
			String[] sargs = args.getArguments();
			Object result = null;
			
			if (args.runWithoutScripts()){
				JSEConsole console = new JSEConsole(jse, supportsANSIEscaping);
				console.run();
			} else {
				String snippet = args.getScriptSnippet();
				if (snippet != null){
					result = jse.runScript(snippet, sargs);
				} else {
					result = jse.runFile(args.getScriptPath(), sargs);
				}
			}
			
			return result;
		}

		@Override
		public void addBindings(Object engine, NamedBinding[] namedBindings) {
			JulianScriptEngine jse = (JulianScriptEngine)engine;
			for(NamedBinding nb : namedBindings){
				jse.addBinding(nb.getName(), nb.getBinding());
			}
		}
		
	}
	
	public static void main(String[] args) {
		Helper helper = new Helper();
		CLExecutor exec = new CLExecutor();
		int res = exec.execute(helper, args);
		System.exit(res);
	}
}
