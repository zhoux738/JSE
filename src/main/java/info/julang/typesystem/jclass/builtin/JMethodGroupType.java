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

package info.julang.typesystem.jclass.builtin;

import info.julang.execution.Executable;
import info.julang.external.exceptions.JSEError;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JParameter;

/**
 * A method group type is a special built-in type. It is not registered in the type table,
 * and is most commonly generated in the fly when a method group value is created. The main
 * purpose of this type is for reflective operations on method group value.
 * 
 * @author Ming Zhou
 */
public class JMethodGroupType extends JFunctionType {

	private JMethodType[] methodTypes;
	private JType containingType;
	
	/**
	 * Create a method group type for the specified method name.
	 * 
	 * @param name
	 * @param params
	 * @param returnType
	 * @param containingType
	 * @param methodTypes
	 */
	public JMethodGroupType(String name, JType returnType, JType containingType, JMethodType[] methodTypes) {
		super(name, null, returnType, null);
		this.methodTypes = methodTypes;
		this.containingType = containingType;
	}

	public JType getContainingType(){
		return containingType;
	}
	
	public FunctionKind getFunctionKind(){
		return FunctionKind.METHOD_GROUP;
	}
	
	public JMethodType[] getJMethodTypes(){
		return methodTypes;
	}
	
	@Override
	public Executable getExecutable() {
		throw new JSEError("Cannot demand executable from a method group directly.");
	}

	@Override
	public JParameter[] getParams() {
		throw new JSEError("Cannot demand parameters from a method group directly.");
	}
}
