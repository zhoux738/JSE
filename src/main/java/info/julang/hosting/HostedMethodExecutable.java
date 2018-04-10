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

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.attributes.HostedAttributeType;
import info.julang.hosting.execution.INativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.modulesystem.naming.FQName;

/**
 * The executable referenced by a method member which actually executes the native logic.
 * 
 * @author Ming Zhou
 */
public class HostedMethodExecutable extends HostedExecutable {

	private INativeExecutor exe;
	
	private HostedAttributeType hattr;
	
	public HostedMethodExecutable(FQName className, String methodName, HostedAttributeType hattr, boolean isStatic) {
		super(className, methodName, isStatic);
		this.hattr = hattr;
	}
	
	@Override
	protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) throws Exception {
		// Get "this" value, which could be null (static method)
		HostedValue thisVal = ArgumentUtil.<HostedValue>getThisValue(args);
		
		if(thisVal != null){
			Argument[] args2 = new Argument[args.length - 1];
			for(int i=1;i<args.length;i++){
				args2[i-1] = args[i];
			}
			args = args2;
		}
		
		if(exe == null){
			throw new HostingPlatformException("No native method is registered in the script engine.", className, methodName);
		}
		JValue res = exe.execute(runtime, thisVal, args);
		if(res == null){
			throw new HostingPlatformException("The returned value is null.", className, methodName);
		}
		return new Result(res);
	}
	
	public void setNativeExecutor(INativeExecutor exe){
		this.exe = exe;
	}

	public HostedAttributeType getHostedAttribute() {
		return hattr;
	}
}
