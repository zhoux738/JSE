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

package info.julang.interpretation.internal;

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * An executor used to create a new variable in current scope, with its value either 
 * {@link #newVar(Context, String, JValue, boolean) provided by the caller}, or 
 * automatically allocated in current frame using default value for the 
 * {@link #newVar(Context, String, JType) given} or 
 * {@link #newVar(Context, String, ParsedTypeName) derived} type. 
 * 
 * @author Ming Zhou
 */
public class NewVarExecutor {

	public NewVarExecutor(){
		
	}
	
	/**
	 * Add a new variable in current scope, with its value allocated in current frame 
	 * using default value for the type resolved from the given {@link ParsedTypeName}.
	 * 
	 * @param context
	 * @param varId
	 * @param varTypName
	 */
	public JValue newVar(Context context, String varId, ParsedTypeName varTypName) {
		if(varTypName == ParsedTypeName.ANY){
			return newVar(context, varId, null, AnyType.getInstance());
		} else {
			return newVar(context, varId, null, context.getTypeResolver().resolveType(varTypName));
		}
	}
	
	/**
	 * Add a new variable in current scope with given value and type.
	 * <p>
	 * If a non-null value is provided, add the value as is into variable table, unless the provided type is
	 * a class or {@link AnyType}. If the type is a class, wrap the value into a reference; if it's untyped, 
	 * wrap into an {@link UntypedValue} instance on top of reference (if the initializing value is of class type).
	 * <p>
	 * If a null value is provided, try to deduce the default value from the given type. Throw if the type is null.
	 * 
	 * @param context
	 * @param varId the variable name to add into current scope.
	 * @param val can be null only if type is provided.
	 * @param type if {@link AnyType untyped}, wrap the value into an {@link UntypedValue} instance.
	 */
	public JValue newVar(Context context, String varId, JValue val, JType type){
		if(type == AnyType.getInstance()){
			if (val == null){
				val = RefValue.NULL;
			}
			
			ObjectValue ov = RefValue.tryDereference(val);
			if (ov != null && ov.getType() == JStringType.getInstance()){
				val = new RefValue(val.getMemoryArea(), ov);
			}
			
			val = new UntypedValue(context.getFrame(), val);
		} else if (val == null) {
			if (type == null){
				throw new JSEError("Cannot initialize a variable with both null type and null value.");
			}
			
			val = ValueUtilities.makeDefaultValue(context.getFrame(), type, false, context.getTypTable());
		} else if (val.getKind() == JValueKind.OBJECT){
			val = new RefValue(context.getFrame(), (ObjectValue)val);
		}
		
		context.getVarTable().addVariable(varId, val);
		return val;
	}
}
