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

package info.julang.clapp;

import java.io.File;

import javax.script.ScriptException;

import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEException;

/**
 * A script engine facade used exclusively for command-line based applications.
 * <p>
 * Under no circumstances will this class shut down JVM. It always return an int 
 * value, which is possibly returned by the script, and defer any further 
 * judgment to the caller.
 * 
 * @author Ming Zhou
 */
public class CLExecutor {

	private final static int SUCCESS = 0;
	private final static int SCRIPT_ERROR = 1;
	private final static int USER_ERROR = 2;
	
	private IExecutionHelper helper;
	
	public int execute(IExecutionHelper helper, String[] args) {
		int res = SUCCESS;
		this.helper = helper;
		try {
			CLParser parser = new CLParser(args);
			CLEnvironment env = parser.parse();
			if(env.shouldTerminate()){
				res = env.getExitCode();
			} else {
				res = executeInternal(env);
			}
		} catch (CLParsingException e) {
			res = abortByUserError(e);
		}
		
		return res;
	}

	private int executeInternal(CLEnvironment env) {
		ExecutionArguments args = new ExecutionArguments();
		Object jse = helper.getEngineInstance(env);
		
		if (jse == null){
			System.err.println("Engine not found.");
			return USER_ERROR;
		}
		
		String[] paths = env.getModulePaths();
		if(paths != null){
			helper.addModulePaths(jse, args, paths);
		}
		
		NamedBinding[] bnds = env.getBindings();
		if(bnds != null){
			helper.addBindings(jse, bnds);
		}

		String scFile = env.getScriptFile();
		String snippet = env.getScript();
		
		// The following checks are necessary since we didn't enforce it through the parser.
		try {
			if (!env.isInteractiveMode()){
				if (snippet != null) {
					if ("".equals(snippet.trim())){
						throw new CLParsingException("Script snippet is empty.", false);
					}
					
					args.setScriptSnippet(snippet);
				} else {
					if(scFile == null || "".equals(scFile.trim())){
						throw new CLParsingException("Script file not specified.", false);
					} else {
						// Some further sanity checks
						if(!scFile.endsWith(".jul")){
							throw new CLParsingException("The script file must be named *.jul.", false);
						}
						File file = new File(scFile);
						scFile = file.getAbsolutePath();
						if(!file.exists()){
							scFile = file.getAbsolutePath();
							throw new CLParsingException("Script file not found: " + scFile, false);
						}
					}			
				}
			}
		} catch (CLParsingException e) {
			return abortByUserError(e);
		}
		
		try {
			args.setScriptPath(scFile);
			args.setArguments(env.getArguments());
			Object result = helper.invokeEngine(jse, args);
			if(result != null){
				if (!env.isQuietMode()){
					System.out.println(result);
				}
				
				// If the script returns an integer/byte, propagate it back to 
				// the cmdline. All other kind of results are treated as 0. 
				if (result instanceof Integer){
					return ((Integer)result).intValue();
				} else if (result instanceof Byte){
					return ((Byte)result).byteValue();
				}
				
				// Design decision - what to do with Boolean and Float?
			}
			
			return SUCCESS;
		} catch (JSEException ex) {
			return abortByEngineError(ex);
		}
	}
	
	private int abortByUserError(CLParsingException clpe){
		System.out.println(clpe.getMessage());
		return USER_ERROR;
	}
	
	private int abortByEngineError(Exception ex){
		Throwable ie = ex.getCause();
		if (ie != null) {
			if (ie instanceof ScriptException) { // We already dumped, so don't print errors again.
				ex = null;
			} else if (ie instanceof EngineInvocationError){
				ex = (Exception)ie;
			}
		}
		
		if (ex != null){
			ex.printStackTrace();
		}

		return SCRIPT_ERROR;
	}
}
