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

package info.julang.external.interfaces;

public interface IExtVariableTable {

	public static final String KnownVariableName_Arguments = "arguments";
	
	/**
	 * Enter into a new scope.
	 */
	public void enterScope();
	
	/**
	 * Exit from the current scope. The enclosing scope becomes the current scope.
	 */
	public void exitScope();
	
	/**
	 * Clear everything from this variable table.
	 */
	public void clear();
	
	/**
	 * Get the count of scope level. Each call to {@link #enterScope()} increments one level, 
	 * each call to {@link #exitScope()} decrements one level.
	 * 
	 * @return The nest level.
	 */
	public int getNestLevel();
	
	/**
	 * Get the dereferenced variable for the given name.
	 * 
	 * @param name The name of variable.
	 * @return the actual value that is dereferenced from the variable. So it cannot be a 
	 * {@link info.julang.memory.value.RefValue RefValue} or 
	 * {@link info.julang.memory.value.UntypedValue UntypedValue}. null if not found.
	 */
	public IExtValue getValue(String name); 
	
}
