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

package info.julang.hosting.interop;

import java.util.HashMap;
import java.util.Map;

import info.julang.execution.threading.SystemInitiatedThreadRuntime;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JMethodType;

/**
 * A generic wrapper used to facilitate interop between platform and Julian.
 * <p>
 * To use this class, make a customized one that inherits from it. In the initialization section,
 * call {@link #registerMethod(String, String, boolean, JType[]) registerMethod(...)} against each
 * method to be exposed for interop. Then, define the methods with Java typed parameters that 
 * forwards the call to registered methods via {@link #runMethod(String, JValue...) runMethod(...)}.
 * <p>
 * When creating an instance of this, it's important to know in what thread it will be actually 
 * invoked. If it's not the original JSE-managed thread, one must specify <code>
 * useIndependentThreadRuntime = true</code>. Otherwise the thread runtime (<code>rt</code> in the 
 * {@link #JSEObjectWrapper(String, ThreadRuntime, ObjectValue, boolean) constructor}) object will 
 * be accessed from a different thread. This <code>rt</code> object, however, is completely 
 * thread-unsafe and wide open to various race conditions. 
 * <p>
 * It's possible to generate Java API against a JSE class definition, built on top of this class.
 *   
 * @author Ming Zhou
 */
public class JSEObjectWrapper {
	
	private FuncCallExecutor exec;
	private Map<String, MethodProvider> methods;
	private JClassType jcp;
	private ObjectValue ov;
	
	/**
	 * Create a new wrapper for <code>ov</code>, whose Julian type name is <code>fullClassName</code>.
	 * 
	 * @param fullClassName
	 * @param rt thread runtime to invoke with
	 * @param ov the object to invoke method against (i.e. "this" object). If provided, a type equality
	 * check will be performed to determine if the type found in the runtime is consistent with the given
	 * object; if null, try to resolve the type first, and throws if not found.
	 * @param useIndependentThreadRuntime if true, create a new thread runtime on top of the given one.
	 */
	public JSEObjectWrapper(String fullClassName, ThreadRuntime rt, ObjectValue ov, boolean useIndependentThreadRuntime){
		boolean elevated = false;
		if (ov != null){
			JClassType otyp = ov.getClassType();
			String fname = otyp.getName();
			JType typ = rt.getTypeTable().getType(fname);
			if (typ != otyp || !fname.equals(fullClassName)){
				throw new JSEError("Cannot create an JSE object wrapper using a value that contains a type which cannot be found in the current runtime."); 
			} else {
				jcp = otyp; 
			}
			
			this.ov = ov;
		} else {
			jcp = (JClassType)rt.getTypeTable().getType(fullClassName);
			if (jcp == null){
				// Resolve the type
				Context cntx = Context.createSystemLoadingContext(rt);
				rt = new SystemInitiatedThreadRuntime(cntx);
				elevated = true;
				try {
					jcp = (JClassType)rt.getTypeResolver().resolveType(cntx, new ParsedTypeName(fullClassName), true);
				} catch (Exception ex){
					throw new JSEError("Cannot create an JSE object wrapper using a type which cannot be found in the current runtime.", ex); 
				}
			}
		}
		
		if (useIndependentThreadRuntime && !elevated) {
			Context cntx = Context.createSystemLoadingContext(rt);
			rt = new SystemInitiatedThreadRuntime(cntx);
		}
		
		exec = new FuncCallExecutor(rt);
	}
	
	/**
	 * Get the object value being wrapped.
	 */
	public ObjectValue getObjectValue(){
		return ov;
	}
	
	/**
	 * Register a method with specified signature.
	 * 
	 * @param key Any value as long as it's unique among all the calls to this method against the same instance.
	 * @param name The name of method as declared in the script.
	 * @param isStatic true if this is static method; false an instance one.
	 * @param types The parameter types, without 'this' in case of instance method.
	 */
	protected void registerMethod(String key, String name, boolean isStatic, JType[] types){
		if (methods == null){
			methods = new HashMap<String, MethodProvider>();
		}
		
		MethodProvider provider = new MethodProvider(name, jcp, isStatic, types);
		methods.put(key, provider);
	}
	
	/**
	 * Run a registered method.
	 * 
	 * @param key The same key used to {@link #registerMethod(String, String, boolean, JType[]) register the method}.
	 * @param values The arguments, without 'this'.
	 */
	protected JValue runMethod(String key, JValue... values){
		if (methods == null){
			throw new JSEError("No method of key \"" + key + "\" is registered with this JSE object wrapper."); 
		} 
		
		MethodProvider provider = methods.get(key);
		String name = provider.getMethodName();
		JMethodType mtyp = provider.provide();

		JValue val = exec.invokeMethodInternal(mtyp, name, values, provider.isStatic() ? null : ov);
		
		return val;
	}
}
