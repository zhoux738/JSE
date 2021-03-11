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

import java.util.Map.Entry;

import info.julang.execution.symboltable.LocalBindingTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.JValueBase;
import info.julang.memory.value.LambdaValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.typesystem.JType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.JParameter;

/**
 * A helper class used to bind a function with 'this' and arguments.
 * It creates a new function value that may or may not share the function type as the previous one.
 * 
 * @author Ming Zhou
 */
public final class FunctionBinder {

	private FunctionBinder() {}
	
	/**
	 * Bind a function value with, optionally, a new 'this' reference and the first N arguments, where N >= 0.
	 * 
	 * @param runtime The thread runtime
	 * @param funcValue The function value
	 * @param thisToBindVal The new 'this' value. If null or {@link RefValue#Null null} (JSE), do not bind it.
	 * @param argsToBindVal The array of arguments to bind to the first N parameters.
	 * 
	 * @return A new function value that is potentially having a new function type due to signature change.
	 */
	public static FuncValue bind(
		ThreadRuntime runtime, FuncValue funcValue, JValue thisToBindVal, ArrayValue argsToBindVal) {

		JFunctionType jft = (JFunctionType)funcValue.getType();
		
		boolean noArgs = argsToBindVal == null || argsToBindVal.getLength() == 0;
		
		boolean isDynThis = false;
		
		switch (jft.getFunctionKind()) {
		case FUNCTION:
			{
				// Replicate the local bindings, potentially with 'this' overwritten
				PresetLocalBindingTable lbt = createBindingTable(runtime.getHeap(), funcValue, thisToBindVal);
				
				// If there are bound arguments, create a new JFunctionType
				JFunctionType newFuncType;
				if (noArgs) {
					newFuncType = jft;
				} else {
					JType[] typs = extractBindingTypes(jft, lbt, argsToBindVal, false);
					newFuncType = jft.bindParams(typs);
				}
				
				// Create a new function value
				FuncValue fv = FuncValue.createGlobalFuncValue(runtime.getHeap(), newFuncType);
				fv.setLocalBindingTable(lbt);
				return fv;
			}
			
		case LAMBDA:
			{
				// Replicate the local bindings, potentially with 'this' overwritten
				PresetLocalBindingTable lbt = createBindingTable(runtime.getHeap(), funcValue, thisToBindVal);
				
				// If there are bound arguments, create a new JLambdaType
				JLambdaType jlt = (JLambdaType) jft;
				JLambdaType newLambdaType;
				if (noArgs) {
					newLambdaType = jlt;
				} else {
					JType[] typs = extractBindingTypes(jft, lbt, argsToBindVal, false);
					newLambdaType = jlt.bindParams(typs);
				}

				// Create a new lambda value
				LambdaValue lv = LambdaValue.createLambdaValue(
					runtime.getHeap(), newLambdaType, ((LambdaValue)funcValue).getDisplay());
				lv.setLocalBindingTable(lbt);
				return lv;
			}
			
		case METHOD:
			{
				JMethodType jmt = (JMethodType) jft;				
				if (jmt.isHosted()) {
					throw IllegalBindingException.cannotBindHostedFunction();
				}
				
				boolean inst = !jmt.getMethodExecutable().isStatic();
				
				// Determine the new 'this' reference. It will stay same if not being re-bound.
				ObjectValue newThisValue = null;
				if (inst) {
					if (thisToBindVal != null && thisToBindVal != RefValue.NULL) {
						// If we are trying to rebind 'this', must make sure the type is consistent
						//
						// NOTES: in an earlier design this was made to support DOWNGRADE types, i.e. types derived from the type of
						// current 'this'. This has proven to be problematic, as a child class's instance may not visit its parent's 
						// private members. It also creates chaos, or at least non-straightforward rules about member access across
						// the inheritance chain. Until these design issues are solved, we will only support EQUIVALENT convertibility.
						Convertibility conv = (thisToBindVal.getType()).getConvertibilityTo(jmt.getContainingType());
						if (conv == Convertibility.EQUIVALENT) {
							newThisValue = (ObjectValue)thisToBindVal;
						} else if (JDynamicType.isDynamicType(thisToBindVal)){
							// EXCEPTION: always allow binding to a Dynamic object
							isDynThis = true;
							// Preserve the existing 'this', even if it will be effectively shadowed by the local bound 'this'
							newThisValue = (ObjectValue) ((MethodValue)funcValue).getThisValue();
						} else {
							throw IllegalBindingException.cannotBindThisWithIncompatibleType(
								thisToBindVal.getType(), jmt.getContainingType());
						}
					} else {
						// This call doesn't try to re-bind 'this'. So preserve the existing one.
						newThisValue = (ObjectValue) ((MethodValue)funcValue).getThisValue();
					}
				}

				// Beware of the difference from other cases: with the exception of Dynamic, we do not 
				// put 'this' into local binding, since the method value natively supports storing 'this' 
				// value, and we require type compatibility.
				PresetLocalBindingTable lbt = (inst && !isDynThis)
					? new PresetLocalBindingTable(runtime.getHeap(), funcValue.getLocalBindings())
					: createBindingTable(runtime.getHeap(), funcValue, thisToBindVal);
				
				// If there are bound arguments, create a new JMethodType
				JMethodType newMethodType;
				if (noArgs) {
					newMethodType = jmt;
				} else {
					JType[] typs = extractBindingTypes(jmt, lbt, argsToBindVal, inst);
					newMethodType = jmt.bindParams(typs);
				}

				// Create a new method value
				MethodValue mv = null;
				if (inst) {
					mv = MethodValue.createInstanceMethodValue(
						runtime.getHeap(), newMethodType, newThisValue, true);
				} else {
					mv = MethodValue.createStaticMethodValue(
						runtime.getHeap(), newMethodType, true);
				}
				
				mv.setLocalBindingTable(lbt);
				return mv;
			}
			
		case CONSTRUCTOR:
		case METHOD_GROUP:
		default:
			// Disallow binding to these function types
			throw IllegalBindingException.bindIsNotAllowed(jft);
		}
	}

	private static PresetLocalBindingTable createBindingTable(MemoryArea mem, FuncValue funcValue, JValue thisToBindVal) {
		PresetLocalBindingTable lbt = new PresetLocalBindingTable(mem, funcValue.getLocalBindings());
		if (thisToBindVal != null && thisToBindVal != RefValue.NULL) {
			lbt.addVariable("this", thisToBindVal, thisToBindVal.getType());
		}
		
		return lbt;
	}
	
	private static JType[] extractBindingTypes(
		JFunctionType jft, PresetLocalBindingTable lbt, ArrayValue argsToBindVal, boolean skipFirst) {
		int asize = argsToBindVal != null ? argsToBindVal.getLength() : 0;
		JParameter[] pms = jft.getParams();
		int psize = pms.length;
		JType[] typs = new JType[asize];
		for (int aindex = 0, pindex = skipFirst ? 1 : 0; 
			aindex < asize && pindex < psize; 
			aindex++, pindex++) {
			JValue val = ((JValue)argsToBindVal.get(aindex)).deref();
			typs[aindex] = val == RefValue.NULL ? getParamType(pms[pindex]) : val.getType();
			String name = pms[pindex].getName();
			lbt.addVariable(name, val, typs[aindex]);
		}
		
		return typs;
	}
	
	private static JType getParamType(JParameter pm) {
		JType tp = pm.getType();
		if (tp.isObject()) {
			return tp;
		} else {
			return JObjectType.getInstance();
		}
	}
	
	/**
	 * A local binding table which replicates bound variables from an existing table.
	 */
	private static class PresetLocalBindingTable extends LocalBindingTable {
		
		private MemoryArea mem;
		
		private PresetLocalBindingTable(MemoryArea mem, LocalBindingTable current) {
			super();
			
			this.mem = mem;
			
			// Copy the current bindings over
			if (current != null) {
				for (Entry<String, JValue> entry : current.getAll().entrySet()) {
					String key = entry.getKey();
					addVariable(key, entry.getValue(), entry.getValue().getType());
				}
			}
		}
		
		private void addVariable(String name, JValue val, JType type) {
			val = replicate(val, type, "this".equals(name));
			this.map.put(name, val);
		}
		
		private JValue replicate(JValue val, JType type, boolean setConst) {
			val = ValueUtilities.replicateValue(val, type, mem);
			
			if (setConst && val instanceof JValueBase) {
				((JValueBase)val).setConst(true);
			}
			
			return val;
		}
	}
}
