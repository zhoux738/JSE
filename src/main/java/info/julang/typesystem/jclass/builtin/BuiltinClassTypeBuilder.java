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
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;

/**
 * The class type builder used during type system bootstrapping when building built-in class types (Array, String, etc.).
 * 
 * @author Ming Zhou
 */
public class BuiltinClassTypeBuilder extends JClassTypeBuilder {

	public BuiltinClassTypeBuilder(String name, JClassType classTypePrototype) {
		super(name, classTypePrototype, true);// skip sanity check
	}

	/**
	 * Create a public instance method without attributes.
	 * <p>
	 * If possible, prefer to using this in place of addInstanceMember.
	 * 
	 * @param methodName The method's name
	 * @param parameters The method's parameters, excluding 'this' 
	 * @param returnType The return type of the method.
	 * @param exec The executable
	 */
	public void addBuiltinInstanceMethod(
		String methodName, JParameter[] parameters, JType returnType, HostedExecutable exec) {
		JClassType thisType = this.getStub();
		JParameter[] pms = new JParameter[parameters.length + 1];
		System.arraycopy(parameters, 0, pms, 1, parameters.length);
		pms[0] = new JParameter("src", thisType);
		super.addInstanceMember(
			new JClassMethodMember(
				thisType,
				methodName, Accessibility.PUBLIC, false, false,
				new JMethodType(
					methodName,
					pms, 
					returnType, 
					exec,
					thisType),
			    null));
	}

	/**
	 * Create a public static method without attributes.
	 * <p>
	 * If possible, prefer to using this in place of addStaticMember.
	 * 
	 * @param methodName The method's name
	 * @param parameters The method's parameters
	 * @param returnType The return type of the method.
	 * @param exec The executable
	 */
	public void addBuiltinStaticMethod(
		String methodName, JParameter[] parameters, JType returnType, HostedExecutable exec) {
		JClassType thisType = this.getStub();
		super.addStaticMember(
			new JClassMethodMember(
				thisType,
				methodName, Accessibility.PUBLIC, true, false,
				new JMethodType(
					methodName,
					parameters, 
					returnType, 
					exec,
					thisType),
			    null));
	}
}
