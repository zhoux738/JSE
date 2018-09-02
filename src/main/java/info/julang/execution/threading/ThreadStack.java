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

import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.interpretation.IStackFrameInfo;
import info.julang.interpretation.UnknownStackFrameInfo;
import info.julang.memory.FrameMemoryArea;
import info.julang.memory.StackArea;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thread stack contains two essential pieces of data for a thread: the memory area 
 * which backs the runtime storage, and the variable tables. Note each frame has
 * one (frame) memory area and one variable table.
 *
 * @author Ming Zhou
 */
public class ThreadStack {

	private StackArea stack;

	private Deque<IVariableTable> varTableStack;
	
	private IVariableTable globalVarTable;
	
	private NamespacePool namespaces;
	
	public NamespacePool getNamespacePool() {
		return namespaces;
	}

	public void setNamespacePool(NamespacePool namespacePool) {
		this.namespaces = namespacePool;
	}
	
	public ThreadStack(StackAreaFactory stFactory, IVariableTable globalVarTable) {
		this.stack = stFactory.createStackArea();
		varTableStack = new ArrayDeque<IVariableTable>();
		this.globalVarTable = globalVarTable;
	}

	/**
	 * Get the stack memory area backing this stack.
	 * @return
	 */
	public StackArea getStackArea() {
		return stack;
	}
	
	/**
	 * Push in a new frame (entering a function call) without stack frame info. 
	 * A new variable table is generated for this frame.
	 */
	public void pushFrame(){
		pushFrame(UnknownStackFrameInfo.INSTANCE);
	}
	
	/**
	 * Push in a new frame (entering a function call) with stack frame info. 
	 * A new variable table is generated for this frame.
	 */
	public void pushFrame(IStackFrameInfo frameInfo){
		pushFrame0(new VariableTable(globalVarTable), frameInfo, true);
	}
	
	/**
	 * Push in a new frame (entering a function call). Using the given variable table for this frame.
	 * <p>
	 * This can be used for the first frame where the variable table, also known as global variable table, is pre-defined.
	 * 
	 * @param varTable
	 * @param enterFirstScope if true, enter into the first scope automatically.
	 */
	public void pushFrame(IVariableTable varTable, IStackFrameInfo frameInfo, boolean enterFirstScope){
		pushFrame0(varTable, frameInfo, enterFirstScope);
	}
	
	private void pushFrame0(IVariableTable varTable, IStackFrameInfo frameInfo, boolean enterFirstScope){
		stack.pushFrame(frameInfo);
		if (enterFirstScope) {
			varTable.enterScope();
		}
		varTableStack.push(varTable);
	}
	
	/**
	 * Pop current frame (exiting a function call).
	 */
	public void popFrame(){
		stack.popFrame();
		varTableStack.pop();
	}
	
	/**
	 * Get current frame.
	 * @return null if there is no frame in the stack.
	 */
	public ThreadFrame currentFrame() {
		FrameMemoryArea area = stack.currentFrame();
		if(area != null){
			return new ThreadFrame(area, varTableStack.peek());
		}
		return null;
	}
	
	/**
	 * Get the info for a frame at the given index. 0 is the current top. 
	 * 
	 * @param index the index of the target frame. 0 is the top, 1 the one frame below the top, and so on. 
	 * Apparently the maximum index value = <code>{@link #getDepth()} - 1</code>.
	 * @return null if the given index is beyond the range.
	 */
	public IStackFrameInfo getFrameInfoFromTop(int index) {
		FrameMemoryArea area = null;
		if (index == 0) {
			area = stack.currentFrame();
		} else if (index > 0 && index < getDepth()) {
			area = stack.getFrameFromTop(index);
		}
		
		if(area != null){
			IStackFrameInfo info = area.getFrameInfo();
			return info == null ? UnknownStackFrameInfo.INSTANCE : info;
		}
		
		return null;
	}
	
	/**
	 * Get the current depth of stack.
	 * 
	 * @return
	 */
	public int getDepth(){
		return varTableStack.size();
	}
	
}
