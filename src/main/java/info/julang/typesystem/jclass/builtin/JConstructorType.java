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

import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedExecutable;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.JReturn;
import info.julang.typesystem.jclass.MethodExecutable;

/**
 * The Constructor type is for constructor defined in a class
 * 
 * @author Ming Zhou
 */
public class JConstructorType extends JFunctionType implements ExecutableType {
	
	private boolean hosted;
	private JType containingType;
	
	/**
	 * Create a new Constructor type with script-defined executable.
	 * 
	 * @param params the parameter list
	 * @param executable a method executable to run when invoking this method
	 * @param containingType the type to which the method belongs.
	 */
	public JConstructorType(String name, JParameter[] params, MethodExecutable executable, JType containingType) {
		super(name, params, null, executable);
		this.containingType = containingType;
	}
	
	/**
	 * Create a new Constructor type with hosted executable.
	 * 
	 * @param params the parameter list
	 * @param executable a hosted executable to run when invoking this method
	 * @param containingType the type to which the method belongs.
	 */
	public JConstructorType(String name, JParameter[] params, HostedExecutable executable, JType containingType) {
		super(name, params, null, executable);
		hosted = true;
		this.containingType = containingType;
	}
	
	@Override
	public JFunctionType bindParams(JType[] paramTypesToRemove) {
		// JFunctionType.bind() should have already avoided calling this.
		throw new JSEError("Should never bind a constructor");
	}
	
	/**
	 * Always return null.
	 */
	@Override
	public JReturn getReturn() {
		return null;
	}
	
	/**
	 * Always return Void.
	 */
	@Override
	public JType getReturnType() {
		return VoidType.getInstance();
	}
	
	/**
	 * If this method is hosted (implemented by platform language, not Julian).
	 * @return true if hosted
	 */
	public boolean isHosted(){
		return hosted;
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
		return FunctionKind.CONSTRUCTOR;
	}
	
	/**
	 * Get the type that contains this constructor.
	 */
	public JType getContainingType(){
		return containingType;
	}
	
	/**
	 * Get a signature of this constructor, including name and param list.
	 */
	@Override
	public String getSignature() {
		return containingType.getName() + "(" + JParameter.getSignature(this.getParams(), true) + ")";
	}
	
	/**
	 * Get the fully qualified ctor name.
	 */
	@Override
	public String getFullFunctionName(boolean includeParams) {
		return includeParams ? getSignature() : containingType.getName();
	}
}
