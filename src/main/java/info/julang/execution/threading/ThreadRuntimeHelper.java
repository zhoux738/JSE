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

package info.julang.execution.threading;

import info.julang.execution.Argument;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectArrayValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;

/**
 * Provides convenient methods on top of thread runtime API.
 * 
 * @author Ming Zhou
 */
// IMPLEMENTATION NOTES
// These methods can be implemented as Java 8 default methods on ThreadRuntime 
// interface, but until we move on to Java 8 this helper class is the way to go.
public final class ThreadRuntimeHelper {

	/**
	 * Load a System type.
	 * 
	 * If the type is already loaded, this method returns fast. Otherwise, creates a 
	 * {@link Context#createSystemLoadingContext(ThreadRuntime) system context}
	 * to load the type from module paths.
	 * 
	 * Must call this to load a compound type. Non-compound type will result in {@link JSEError}.
	 * 
	 * @param rt
	 * @param name
	 * @return the loaded type.
	 */
	public static ICompoundType loadSystemType(ThreadRuntime rt, String name){
		JType typ = rt.getTypeTable().getType(name);
		if (typ != null) {
			return checkAndCast(typ);
		}
		
		Context ctxt = Context.createSystemLoadingContext(rt);
		typ = ctxt.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(name));
		
		return checkAndCast(typ);
	}
	
	/**
	 * Create an instance of System type with specified arguments. Will try to find the first matching
	 * constructor based on the arguments.
	 * 
	 * @param rt
	 * @param name
	 * @param args If null, call the first ctor without any arguments. This will only work if the class has a single
	 * ctor which takes no parameters. A class without any explicit ctor applies to this case.
	 * @return
	 * @throws {@link info.julang.typesystem.jclass.ConstructorNotFoundException ConstructorNotFoundException}
	 */
	public static ObjectValue instantiateSystemType(ThreadRuntime rt, String name, JValue[] args){
		JType typ = rt.getTypeTable().getType(name);
		if (typ == null) {
	        Context ctxt = Context.createSystemLoadingContext(rt);
			typ = ctxt.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(name));
		}
		
		ICompoundType ctyp = checkAndCast(typ);

		ObjectValue val = null;
		NewObjExecutor noe = new NewObjExecutor(rt);
		if (args == null) {
		    JClassType jct = (JClassType)ctyp;
	        JClassConstructorMember ctor = jct.getClassConstructors()[0];
	        val = noe.newObjectInternal(jct, ctor, new Argument[0]);
		} else {
		    val = noe.newObject(rt.getHeap(), ctyp, args);
		}
		
		return val;
	}
	
	/**
	 * Get the script object of type <code><font color="green">System.Type</font></code>.
	 * <p>
	 * If the type has not been loaded, it will be loaded from a system context.
	 */
	public static ObjectValue getScriptTypeObject(ThreadRuntime rt, JType typ){
		return rt.getTypeTable().getValue(typ.getName()).getScriptTypeObject(rt);
	}
	
	public static ArrayValue createAndPopulateObjectArrayValue(
		ThreadRuntime rt, int len, JClassType eleTyp, JClassConstructorMember eleTypCtor, IObjectPopulater pop){
		// 1) Create an array
		ITypeTable tt = rt.getTypeTable();
		MemoryArea mem = rt.getHeap();
		ObjectArrayValue array = (ObjectArrayValue)ArrayValueFactory.createArrayValue(mem, tt, eleTyp, len);
		
		// 2) Populate the array with ctors
		for (int i = 0 ; i < len; i++) {
			NewObjExecutor noe = new NewObjExecutor(rt);
			ObjectValue val = noe.newObjectInternal(eleTyp, eleTypCtor, pop.getArguments(i));
			pop.postCreation(i, val);
			RefValue rv = new RefValue(mem, val);		
			rv.assignTo(array.getValueAt(i));
		}
		
		return array;
	}
	
	public static ArrayValue createAndPopulateArrayValue(
		ThreadRuntime rt, JClassType eleTyp, JValue[] values){
		// 1) Create an array of same length as given values
		ITypeTable tt = rt.getTypeTable();
		MemoryArea mem = rt.getHeap();
		int len = values.length;
		ObjectArrayValue array = (ObjectArrayValue)ArrayValueFactory.createArrayValue(mem, tt, eleTyp, len);
		
		// 2) Populate the array with given values
		for (int i = 0 ; i < len; i++) {
			JValue val = values[i].deref();
			if (val.getKind() == JValueKind.OBJECT) {
				val = new RefValue(mem, (ObjectValue)val);	
			}
			
			val.assignTo(array.getValueAt(i));
		}
		
		return array;
	}
	
	private static ICompoundType checkAndCast(JType typ){
		if (typ.isObject()) {
			ICompoundType ict = (ICompoundType)typ;
			return ict;
		} else {
			throw new JSEError("Type " + typ.getName() + " is not a compound type.");
		}
	}
	
	/**
	 * Used along with {@link ThreadRuntimeHelper#createAndPopulateObjectArrayValue createAndPopulateObjectArrayValue()}.
	 * 
	 * @author Ming Zhou
	 */
	public static interface IObjectPopulater {
		
		/**
		 * Provides arguments at the given index.
		 */
		Argument[] getArguments(int index);
		
		/**
		 * A callback post object creation
		 */
		void postCreation(int index, ObjectValue ov);
	}
}
