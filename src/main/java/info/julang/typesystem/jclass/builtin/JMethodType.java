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

import info.julang.hosting.HostedExecutable;
import info.julang.langspec.Keywords;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MethodExecutable;

/**
 * The Method type is for method defined in a class
 * <p/>
 * The 1st parameter returned by {@link #getParams()} refers to <b>this</b> if it is an instance method.
 * 
 * @author Ming Zhou
 */
public class JMethodType extends JFunctionType implements ExecutableType {
	
	private boolean hosted;
	
	private boolean typed;
	
	private JType containingType;
	
	public JMethodType(String name, JParameter[] params, JType returnType, MethodExecutable executable, JType containingType) {
		this(name, params, returnType, executable, containingType, false);
	}
	
	/**
	 * Create a new Method type.
	 * 
	 * @param params the parameter list
	 * @param returnType the return type
	 * @param executable a method executable to run when invoking this method
	 * @param containingType the type to which the method belongs.
	 */
	public JMethodType(String name, JParameter[] params, JType returnType, MethodExecutable executable, JType containingType, boolean untyped) {
		super(name, params, returnType, executable); //set parent to be default function (used to be JObjectType.getInstance())
		//TODO: add methods in future after the inheritance system is complete, and Type type is available
		/*
		 * equalsTo
		 * toString
		 * getType
		 * getParameters()
		 * getReturnType()
		 */
		
		this.containingType = containingType;
		this.typed = !untyped;
	}

	public JMethodType(String name, JParameter[] params, JType returnType, HostedExecutable executable, JType containingType) {
		this(name, params, returnType, executable, containingType, false);
	}
	
	public JMethodType(String name, JParameter[] params, JType returnType, HostedExecutable executable, JType containingType, boolean untyped) {
		super(name, params, returnType, executable); //set parent to be default function (used to be JObjectType.getInstance())
		//TODO: add methods in future after the inheritance system is complete, and Type type is available
		/*
		 * equalsTo
		 * toString
		 * getType
		 * getParameters()
		 * getReturnType()
		 */
		
		this.containingType = containingType;
		this.hosted = true;
		this.typed = !untyped;
	}
	
	/**
	 * Get the type to which the method belongs.
	 * @return
	 */
	public JType getContainingType() {
		return containingType;
	}
	
	/**
	 * If this method is hosted (implemented by platform language, not Julian).
	 * @return true if hosted
	 */
	public boolean isHosted(){
		return hosted;
	}
	
	/**
	 * If this method is typed (checking type for arguments according to declaration). 
	 * <p/>
	 * True by default. And can only be false for hosted methods.
	 * @return
	 */
	@Override
	public boolean isTyped(){
		return typed;
	}
	
	/**
	 * Get executable as {@link MethodExecutable}. 
	 * @return null if it is hosted.
	 */
	public MethodExecutable getMethodExecutable(){
		return hosted ? null : (MethodExecutable) getExecutable();
	}
	
	/**
	 * Get executable as {@link HostedExecutable}. 
	 * @return null if it is not hosted.
	 */
	public HostedExecutable getHostedExecutable(){
		return hosted ? (HostedExecutable) getExecutable() : null;
	}
	
	@Override
	public FunctionKind getFunctionKind(){
		return FunctionKind.METHOD;
	}
	
	@Override
	public String getSignature() {
		JParameter[] params = this.getParams();
		boolean skipFirst = params != null && params.length > 0 && params[0].getName().equals(Keywords.THIS);
		return getName() + "(" + JParameter.getSignature(this.getParams(), skipFirst) + ")";
	}
}
