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

package info.julang.typesystem.jclass.jufc.System.IO;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.interop.FunctionCaller;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;

/**
 * Dynamically invokes user's callback with (int read, ProcessHandle handle).
 * 
 * @author Ming Zhou
 */
public class StreamReadAsyncCallback extends FunctionCaller {

	private ObjectValue handleObj;
	
	public StreamReadAsyncCallback(FuncValue fv, ObjectValue handleObj) {
		super(fv, null, null); // no function name, no instance
		this.handleObj = handleObj;
	}
	
	public void invokeWithReadCountAndHandle(ThreadRuntime rt, int read){
		this.call(rt, new JValue[] {
			TempValueFactory.createTempIntValue(read),
			TempValueFactory.createTempRefValue(handleObj)
		}, true);
	}
	
}
