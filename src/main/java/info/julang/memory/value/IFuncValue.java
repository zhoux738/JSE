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

import info.julang.execution.symboltable.LocalBindingTable;
import info.julang.external.interfaces.JValueKind;

/**
 * The overall interface for a function value, mainly used by Executable.
 * 
 * @author Ming Zhou
 */
public interface IFuncValue {

	/**
	 * Get the kind of this function value, which can be one of the following:
	 * <p>
	 * {@link JValueKind#FUNCTION FUNCTION}: a global function, <br>
	 * {@link JValueKind#METHOD METHOD}: a class method, <br> 
	 * {@link JValueKind#METHOD_GROUP METHOD_GROUP}: a group of overloaded class methods, <br>
	 * {@link JValueKind#CONSTRUCTOR CONSTRUCTOR}: a constructor of class.
	 * <p>
	 * <b>CAUTION:</b> If the result is {@link JValueKind#FUNCTION}, 
	 * do not blindly cast this object to a concrete {@link FuncValue}. It's possible the value is simply a {@link FuncValue#DUMMY dummy one}.
	 * @return The {@link JValueKind function kind}.
	 */
	JValueKind getFuncValueKind();
	
	/**
	 * Get the local bindings to this function value. Can be null (if not allowed or never set). These bindings
	 * take the highest precedence during name resolution when executing the function.
	 * 
	 * @return A {@link LocalBindingTable table} containing all of the locally bound variables.
	 */
	LocalBindingTable getLocalBindings();
	
}
