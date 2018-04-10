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

package info.julang.memory.value;

import info.julang.execution.symboltable.Display;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.jclass.builtin.JLambdaType;

public class LambdaValue extends FuncValue {
	
	private Display display;
	
	/**
	 * Make a new LambdaValue.
	 * 
	 * @param memory
	 * @param lambdaType
	 * @param vt the variable table at the moment this lambda is defined. Variables defined within
	 * will be used to populate a {@link Display} instance which gets carried along in
	 * this object as the referential environment for lambda executable.
	 * @return
	 */
	public static LambdaValue createLambdaValue(MemoryArea memory, JLambdaType lambdaType, Display display){
		return new LambdaValue(memory, lambdaType, display, true);
	}
	
	private LambdaValue(MemoryArea memory, JLambdaType funcType, Display display, boolean initFuncMembers) {
		super(memory, funcType, JValueKind.FUNCTION, initFuncMembers);
		this.display = display;
	}
	
	public Display getDisplay(){
		return display;
	}

}
