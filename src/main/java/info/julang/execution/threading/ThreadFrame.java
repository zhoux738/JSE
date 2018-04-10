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

package info.julang.execution.threading;

import info.julang.execution.symboltable.IVariableTable;
import info.julang.memory.FrameMemoryArea;

/**
 * Thread frame contains two essential pieces of data for a frame in thread: 
 * the memory area which backs the runtime storage, and the variable table.
 *
 * @author Ming Zhou
 */
public class ThreadFrame {

	private FrameMemoryArea momory;
	
	private IVariableTable variableTable;
	
	public FrameMemoryArea getMemory() {
		return momory;
	}

	public IVariableTable getVariableTable() {
		return variableTable;
	}

	public ThreadFrame(FrameMemoryArea momory, IVariableTable variableTable) {
		this.momory = momory;
		this.variableTable = variableTable;
	}

//	/**
//	 * Add a new variable into this frame. The variable will be allocated memory in the
//	 * frame, and get registered in variable table.
//	 * <p/>
//	 * The variable must be not stored anywhere when this method is called.
//	 * 
//	 * @param name
//	 * @param value
//	 */
//	public void addVariable(String name, JValue value) {
//		//Ignore memory exception. (It would be a bug which we let pop up)
//		momory.allocate(value);
//		//Ignore binding exception. (It would be a bug which we let pop up)
//		variableTable.addVariable(name, value);
//	}
	
}
