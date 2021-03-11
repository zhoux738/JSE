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
import info.julang.hosting.HostedExecutable;
import info.julang.hosting.mapped.exec.MappedExecutableBase;
import info.julang.langspec.Keywords;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.ICompoundType;
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
		this.containingType = containingType;
		this.typed = !untyped;
	}

	public JMethodType(String name, JParameter[] params, JType returnType, HostedExecutable executable, JType containingType) {
		this(name, params, returnType, executable, containingType, false);
	}
	
	public JMethodType(String name, JParameter[] params, JType returnType, HostedExecutable executable, JType containingType, boolean untyped) {
		super(name, params, returnType, executable); //set parent to be default function (used to be JObjectType.getInstance())		
		this.containingType = containingType;
		this.hosted = true;
		this.typed = !untyped;
	}
	
	@Override
	public JMethodType bindParams(JType[] paramTypesToRemove) {
		JParameter[] params = removeParams(paramTypesToRemove, !this.getMethodExecutable().isStatic());
		if (hosted) {
			// This should be already avoided at an upper layer. Hitting here is a bug.
			throw new JSEError("Cannot bind a hosted method.");
		} else {
			return new JMethodType(
				getName(), params, getReturnType(), (MethodExecutable)getExecutable(), containingType, !typed);
		}
	}
	
	/**
	 * Get the type to which the method belongs.
	 * @return
	 */
	public JType getContainingType() {
		return containingType;
	}
	
	/**
	 * If this method is a bridged implementation on the hosting platform (implemented by platform language, not Julian).
	 * @return true if bridged
	 */
	public boolean isBridged(){
		return hosted;
	}
	
	/**
	 * If this method is mapped to an implementation on the hosting platform (implemented by platform language, not Julian).
	 * @return true if mapped
	 */
	public boolean isMapped(){
		return this.getExecutable() instanceof MappedExecutableBase;
	}
	
	/**
	 * If this method is implemented on the hosting platform, a.k.a. "hosted".
	 * There are two ways a method can be hosted in JSE: by bridging or mapping.
	 * 
	 * @return true of it's hosted, whether bridged or mapped.
	 */
	public boolean isHosted() {
		return isBridged() || isMapped();
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
		String fn = super.getName();
		String sn = "(" + JParameter.getSignature(this.getParams(), skipFirst) + ")";
		// System.out.println("getSignature: " + fn + " - " + sn);
		return fn + sn;
	}
	
	/**
	 * Get the fully qualified method name. Use double colon to represent a static method.
	 */
	@Override
	public String getFullFunctionName(boolean includeParams) {
		boolean isSta = false;
		if (this.isHosted()) {
			HostedExecutable exe = this.getHostedExecutable();
			if (exe != null && exe.isStatic()) {
				isSta = true;
			}
		} else {
			MethodExecutable exe = this.getMethodExecutable();
			if (exe != null && exe.isStatic()) {
				isSta = true;
			}
		}
		
		String name = this.containingType.getName() 
			+ (isSta ? "::" : ".")
			+ (includeParams ? getSignature() : super.getName());
		
		// System.out.println("getFullFunctionName: " + name);
		return name;
	}
	
	/**
	 * The type name of a method is its full function name (FQ containing type name + signature)
	 */
	@Override
	public String getName() {
		return getFullFunctionName(true);
	}

	/**
	 * Check if this method can serve as the extension for the specified type. An extension method for type T must satisfy
	 * multiple criteria, and this method only covers a part of them.
	 * <pre>
	 *   - Declared in a static class .................... (checked elsewhere)
	 *   - Declared as a static public method ............ (checked elsewhere)
	 *   - The first parameter is named "this" ........... (checked by this method)
	 *   - The first parameter is an Object type ......... (checked by this method)
	 * </pre>
	 * 
	 * @param extendee The type to check if this method may extend
	 * @return
	 */
	public boolean mayExtend(ICompoundType extendee) {
		JParameter[] params = this.getParams();
		if (params != null && params.length > 0) {
			JParameter first = params[0];
			if (first.getName().equals(Keywords.THIS)) {
				JType firstTyp = first.getType();
				if (firstTyp.isObject()) {
					ICompoundType tgtTyp = (ICompoundType)firstTyp;
					return extendee.isDerivedFrom(tgtTyp, true);
				}
			}
		}
		
		return false;
	}
}
