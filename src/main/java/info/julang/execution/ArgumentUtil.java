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

import info.julang.external.exceptions.JSEError;
import info.julang.langspec.Keywords;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.typesystem.JArgumentException;

public final class ArgumentUtil {

	/**
	 * Get the <i>this</i> argument from the given array.
	 * @param inScriptClassName
	 * @param args
	 * @return null if the first argument is not "this"
	 */
	public static Argument getThis(Argument[] args){
		if(args == null || args.length == 0){
			return null;
		}
		
		return Keywords.THIS.equals(args[0].getName()) ? args[0] : null;
	}

	/**
	 * Get the <i>this</i> argument's value from the given array.
	 * @param inScriptClassName
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JValue> T getThisValue(Argument[] args){
		Argument a = getThis(args);
		return a != null ? (T) a.getValue() : null;
	}
	
	/**
	 * Get the argument from the given array that matches the specified name.
	 * @param name
	 * @param args
	 * @return
	 */
	public static Argument getArgument(String name, Argument[] args){
		if(name == null || name.equals("")){
			return null;
		}
		
		for(Argument arg : args){
			if(name.equals(arg.getName())){
				return arg;
			}
		}
		
		return null;
	}
	
	/**
	 * Get value of the argument from the given array that matches the specified name.
	 * @param name
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JValue> T getArgumentValue(String name, Argument[] args){
		return (T) getArgument(name, args).getValue();
	}

	/**
	 * Get value of the argument from the given array that matches the specified index.
	 * @param index
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JValue> T getArgumentValue(int index, Argument[] args){
		if(index >= 0 && index < args.length){
			return (T) args[index].getValue().deref();
		} else {
			return null;
		}
	}
	
	/**
	 * Get value of the argument from the given array that matches the specified index.
	 * 
	 * @param args
	 * @param index
	 * @param throwOnNull If true, throw <code><font color="green">System.ArgumentException</font></code> 
	 * if the argument is Julian's null.
	 * @return null if the argument is Julian's null while throwOnNull == false
	 */
	@SuppressWarnings("unchecked")
	public static <T extends JValue> T getArgumentValue(
		Argument[] args, int index, boolean throwOnNull){
		if (args == null || index < 0 || index >= args.length) {
			throw new JSEError("Trying to get argument at an index outside the range.");
		}
		
		JValue val = args[index].getValue().deref();
		
		if (val == RefValue.NULL) {
			if (throwOnNull) {
				throw new JArgumentException(args[index].getName());
			} else {
				return null;
			}
		}
		
		return (T) val;
	}
}
