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

package info.julang.execution;

import info.julang.langspec.Keywords;
import info.julang.memory.value.JValue;

/**
 * The argument accepted by {@link Executable}.
 *
 * @author Ming Zhou
 */
public class Argument {
	
	private String name;

	private JValue value;

	/**
	 * Get the value of this argument. The value is stored in caller's frame.
	 * @return
	 */
	public JValue getValue() {
		return value;
	}

	/**
	 * Set a new value for this argument.
	 * @param val
	 */
	public void setValue(JValue val) {
		this.value = val;
	}

	/**
	 * Get the name of formal parameter.
	 * @return
	 */
	public String getName() {
		return name;
	}

	public Argument(String name, JValue value) {
		this.name = name;
		this.value = value;
	}
	
	/**
	 * Create a "this" argument.
	 * @param value
	 * @return
	 */
	public static Argument CreateThisArgument(JValue value){
		return new Argument(Keywords.THIS, value);
	}
	
	/**
	 * Create argument array that contains only a "this".
	 * @param value
	 * @return
	 */
	public static Argument[] CreateThisOnlyArguments(JValue value){
		return new Argument[]{CreateThisArgument(value)};
	}
	
	@Override
	public String toString() {
		return name + " (" + value + ")";
	}
}
