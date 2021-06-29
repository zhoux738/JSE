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

package info.julang.hosting;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.security.UnderprivilegeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.StackArea;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.naming.FQName;

/**
 * A hosting executable runs a subroutine that is directly implemented by the hosting language (in contrast to
 * interpreting a Julian script)
 * 
 * @author Ming Zhou
 *
 */
public abstract class HostedExecutable implements Executable {

	protected FQName className;
	
	protected String methodName;

	private boolean isStatic;
	
	/**
	 * Create an instance (non-static) executable.
	 * 
	 * @param className
	 * @param methodName
	 */
	public HostedExecutable(FQName className, String methodName){
		this(className, methodName, false);
	}
	
	protected HostedExecutable(FQName className, String methodName, boolean isStatic){
		this.className = className;
		this.methodName = methodName;
		this.isStatic = isStatic;
	}
	
	// Func value is not used.
	public Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args){
		try {
			// Replicate arguments in the current stack frame.
			// (We do not have a dedicated memory area for platform frames)
			replicateArgs(runtime.getStackMemory(), args);
			return executeOnPlatform(runtime, args);
		} catch (JSERuntimeException jsrex) {
			// Throw any JSERuntimeException as is.
			throw jsrex;
		} catch (JulianScriptException jsrex) {
			if (jsrex.shouldPreserveAcrossPlatformBoundary()) {
				// Throw JulianScriptException if required so.
				throw jsrex;
			} else {
				// Otherwise wrap it in HostingPlatformException. 
				throw new HostingPlatformException(jsrex, className, methodName);
			}
		} catch (SecurityException ex) {
			// For security exception, wrap it in System.UnderprivilegeException.
			throw new UnderprivilegeException(new HostingPlatformException(ex, className, methodName));
		} catch (Exception ex) {
			// For other kinds of exceptions wrap it in HostingPlatformException.
			throw new HostingPlatformException(ex, className, methodName);
		}
	}

	private void replicateArgs(StackArea memory, Argument[] args){
		for(int i = 0; i < args.length; i++){
			Argument arg = args[i];
			if(!"this".equals(arg.getName())){
				JValue val = ValueUtilities.replicateValue(arg.getValue(), null, memory);
				arg.setValue(val);
			}
		}
	}
	
	/**
	 * Execute on the platform.
	 * <p>
	 * If any exception is thrown from this method, it will be wrapped in {@link HostingPlatformException} 
	 * before popping up the stack.
	 * 
	 * @param runtime
	 * @param args argument list in order of method signature; the first is <i>this</i>; can be null or empty.
	 * @return
	 * @throws Exception
	 */
	protected abstract Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) throws Exception;

	public boolean isStatic() {
		return isStatic;
	}
}
