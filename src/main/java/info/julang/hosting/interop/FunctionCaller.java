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

package info.julang.hosting.interop;

import info.julang.execution.threading.SystemInitiatedThreadRuntime;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.IMethodValue;
import info.julang.memory.value.JValue;

/**
 * A wrapper over a Function value object.
 * <p>
 * When creating an instance of this, it's important to know in what thread it will be actually invoked.
 * If it's not the original JSE-managed thread, must specify <code>useIndependentThreadRuntime = true
 * </code>. Otherwise the thread runtime (<code>rt</code> in the constructors) object will be accessed 
 * from a different thread. This object, however, is completely thread-unsafe and wide open to various
 * race conditions. 
 *  
 * @author Ming Zhou
 */
public class FunctionCaller {

	private ThreadRuntime rt;
	private FuncValue fv;
	private String funcName;
	private JValue instance;
	
	public FunctionCaller(ThreadRuntime rt, FuncValue fv, boolean useIndependentThreadRuntime){
		this(rt, fv, null, null, useIndependentThreadRuntime);
	}
	
	public FunctionCaller(ThreadRuntime rt, FuncValue fv, String funcName, JValue instance, boolean useIndependentThreadRuntime){
		if (useIndependentThreadRuntime) {
			Context cntx = Context.createSystemLoadingContext(rt);
			rt = new SystemInitiatedThreadRuntime(cntx);
		}
		
		this.rt = rt;
		this.fv = fv;
		
		if (funcName == null){
			funcName = fv.getType().getName();
		}
		
		if (instance == null) {
			if (fv instanceof IMethodValue){
				IMethodValue imv = (IMethodValue) fv;
				instance = imv.getThisValue();
			}
		}
		
		this.funcName = funcName;
		this.instance = instance;
	}
	
	public JValue call(JValue[] args, boolean dynamic){
		FuncCallExecutor exec = new FuncCallExecutor(rt);
		exec.setLooseTyping(dynamic);
		JValue ret = exec.invokeFuncValueInternal(fv, funcName, args, instance);
		return ret;
	}
	
}
