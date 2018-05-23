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

package info.julang.typesystem.jclass;

import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;

/**
 * This class describes the properties of parameter for a function/method.
 *
 * @author Ming Zhou
 */
public class JParameter implements IUntyped {

	private String name;
	
	private JType type;

	private boolean untyped;
	
	/**
	 * Create a JParameter with particular type.
	 * @param name
	 * @param type
	 */
	public JParameter(String name, JType type) {
		this.name = name;
		this.type = type;
	}
	
	/**
	 * Create a JParameter that is untyped.
	 * @param name
	 */
	public JParameter(String name) {
		this.name = name;
		this.type = AnyType.getInstance();
		this.untyped = true;
	}

	public String getName() {
		return name;
	}

	public JType getType() {
		return type;
	}
	
	/**
	 * If true, {@link #getType()} returns {@link AnyType}.
	 * @return
	 */
	@Override
	public boolean isUntyped() {
		return untyped;
	}
	
	// Only for type builder
	void setType(JType type) {
		this.type = type;
	}
	
	/**
	 * Get parameter names in an array.
	 * 
	 * @param params
	 * @return
	 */
	public static String[] getParamNames(JParameter[] params) {
		String[] names = new String[params.length];
		for(int i = 0; i<params.length; i++){
			names[i] = params[i].getType().getName();
		}
		return names;
	}
	
	public static String getSignature(JParameter[] params, boolean skipFirst) {
		int start = skipFirst ? 1 : 0;
		StringBuilder sig = new StringBuilder();
		int last = params.length - 1;
		for(int i = start; i<last; i++){
			sig.append(params[i].getType().getName());
			sig.append(", ");
		}
		
		if (last >= start) {
			sig.append(params[last].getType().getName());
		}
		
		return sig.toString();
	}
	
	//--------------- Object (for debugging) ---------------//
	
	@Override
	public String toString(){
		return name + ": " + type.getName();
	}
}
