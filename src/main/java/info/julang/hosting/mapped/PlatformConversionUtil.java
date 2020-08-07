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

package info.julang.hosting.mapped;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.attributes.HostedAttributeUtil;
import info.julang.interpretation.context.Context;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueBuilder;
import info.julang.memory.value.ArrayValueBuilderHelper;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A set of utilities to convert types/values between platform and script engine.
 * <p>
 * Basic type values are passed by value. Array values are also passed by value for each 
 * element. Object value is passed by reference.
 * <p>
 * As of design by 0.2.0, the mapping type is not intended to track the state of mapped
 * object. To enforce this, instance fields are not allowed on mapping types. Since array 
 * is essentially an object and contains the state (the backing storage, length, etc.),
 * we must completely forego these aspects and let the platform type have a mirroring of
 * the state by reconstructing the whole array object, recursively if multi-dimensional.
 * Symmetrically upon returning, construct a new engine array based on the platform array.
 * <p>
 * The hosted method manager would use a special class loader so that Julian engine 
 * classes, such as {@link JValue}, are prevented from any mapping. With this further 
 * protection it's ensured that only basic types, non-JSE classes, and array of such 
 * classes are among the legal mapping targets.
 * 
 * @author Ming Zhou
 */
public final class PlatformConversionUtil {
	
	/*
	 * Implementation Notes:
	 * 
	 * Converting from script engine to platform type is straightforward. Basic types map to
	 * basic types; hosted values get the hosted objects extracted out and passed to platform.
	 * The platform array can be also assembled by first constructing a list out of the engine
	 * array and then populated through reflection.
	 * 
	 * Converting from platform to script engine is a little more involved. If what we get 
	 * from the platform call is a non-basic Java type, we check against the hosted method 
	 * manager to find out if a script type has been mapped to it. If so, create a new hosted 
	 * object to carry the JVM object; if not, create a late-binding platform type placeholder.
	 * Also, a script array is built up through an array value builder. 
	 */
	
	/**
	 * Convert platform type into a script type. (JVM => JSE)
	 */
	public static JType fromPlatformType(Class<?> cls, Context context, NewTypeGroup group) throws MappedTypeConversionException {
		if (cls.isArray()){
			Class<?> ecls = cls.getComponentType();
			JType etyp = fromPlatformType(ecls, context, group);
			JArrayType jat = JArrayType.createJArrayType(context.getTypTable(), etyp, true);
			return jat;
		} else {
			String name = cls.getName();
			switch(name){
			case "int": 
			case "java.lang.Integer":
				return IntType.getInstance();
			case "boolean": 
			case "java.lang.Boolean":
				return BoolType.getInstance();
			case "byte": 
			case "java.lang.Byte":	
				return ByteType.getInstance();
			case "float": 
			case "java.lang.Float":	
				return FloatType.getInstance();
			case "char": 
			case "java.lang.Character":	
				return CharType.getInstance();
			case "java.lang.String": 
				return JStringType.getInstance();
			case "void": 
			case "java.lang.Void":
				return VoidType.getInstance();
			default:
				HostedMethodManager hmm = context.getModManager().getHostedMethodManager();
				FQName fqname = hmm.getMappedTypeName(name);
				if (fqname != null) {
					JType typ = null;
					String strName = fqname.toString();
					
					// Try new type group
					if (group != null) {
						typ = group.getType(strName);
					}
					
					// Try type table
					if (typ == null) {
						typ = context.getTypTable().getType(strName);
						if (typ == null) {
							throw new JSEError("A platform type " + name + " has been mapped to script type " + fqname + ", which however cannot be found.");
						}
					}
					
					return typ;
				}
				
				return hmm.getPlatformType(name);
			}
		}
	}

	/**
	 * Convert platform object into a script value. (JVM => JSE)
	 * 
	 * @return a script value converted from the platform object, stored in heap.
	 */
	public static JValue fromPlatformObject(Object obj, Context context) throws MappedTypeConversionException {
		if (obj == null) {
			// Create a generic null
			return RefValue.makeNullRefValue(context.getHeap(), null);
		}
		
		JValue src = null;
		Class<?> cls = obj.getClass();
		if (cls.isArray()){
			int len = Array.getLength(obj);
			// Based on the element type, decide what builder to use ( ObjectArrayValue / Primitive )
			JType etyp = fromPlatformType(cls.getComponentType(), context, null);
			ArrayValueBuilder builder = ArrayValueBuilderHelper.getBuilder(etyp, context.getHeap(), context.getTypTable());
			builder.setLength(len);
			
			for(int i = 0; i < len; i++){
				Object eleObj = Array.get(obj, i);
				JValue eleVal = fromPlatformObject(eleObj, context);
				builder.setValue(i, eleVal);
			}
			
			return builder.getResult();
		}
		
		String name = cls.getName();
		switch(name){
		case "int": 
		case "java.lang.Integer":
			src = TempValueFactory.createTempIntValue((int)obj); break;
		case "boolean": 
		case "java.lang.Boolean":
			src = TempValueFactory.createTempBoolValue((boolean)obj); break;
		case "byte": 
		case "java.lang.Byte":	
			src = TempValueFactory.createTempByteValue((byte)obj); break;	
		case "float": 
		case "java.lang.Float":	
			src = TempValueFactory.createTempFloatValue((float)obj); break;	
		case "char": 
		case "java.lang.Character":	
			src = TempValueFactory.createTempCharValue((char)obj); break;
		case "java.lang.String": 
			src = TempValueFactory.createTempStringValue((String)obj); break;
		case "void":
		case "java.lang.Void":
			return VoidValue.DEFAULT;
		default:
			// Check if the platform type is mapped to a JSE type.
			// If so, create a hosted object and set the platform object into it.
			HostedMethodManager hmm = context.getModManager().getHostedMethodManager();
			FQName fqname = hmm.getMappedTypeName(name);
			if (fqname != null) {
				JType typ = context.getTypTable().getType(fqname.toString());
				if (typ == null) {
					throw new JSEError("A platform type " + name + " has been mapped to script type " + fqname + ", which however cannot be found.");
				}
				
				HostedValue ho = new HostedValue(context.getHeap(), typ);
				ho.setHostedObject(obj);
				
				src = ho;
			}
			
			break;
		}
		
		if (src == null) {
			throw new MappedTypeConversionException("Unable to convert " + name + " to a script type.");
		}
		
		JValue tgt = ValueUtilities.makeDefaultValue(context.getHeap(), src.getType(), false);
		src.assignTo(tgt);		
		return tgt;
	}
	
//	/**
//	 * Convert a script type into platform type.  (JSE => JVM)
//	 */
//	public static JType toPlatformType(IMappedType mt, Context context) {
//		return null;
//	}
	
	/**
	 * Convert a script value into platform object.  (JSE => JVM)
	 * 
	 * @param val a script value
	 * @return a platform object
	 */
	public static Object toPlatformObject(JValue val, ITypeTable tt, HostedMethodManager hmm) 
		throws MappedTypeConversionException {
		ConversionContext cc = new ConversionContext();
		
		Object obj = toPlatformObject0(val, cc, 0);
		
		if (cc.scriptElementType != null) {
			// Convert a List to a multi-dimensional array
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)obj;
			
			JType etyp = cc.scriptElementType;
			Class<?> eclazz = toPlatformScalarType(etyp, tt, hmm);
			
			List<Integer> ranks = cc.arrayRanks;
			int[] dims = new int[ranks.size()];
			for(int i = 0 ; i < dims.length; i++){
				dims[i] = ranks.get(i);
			}
			
			// Allocate array with a dimension/rank shape corresponding to the list
			Object array = Array.newInstance(eclazz, dims);
			populateArray(array, dims, 0, list);
			
			obj = array;
		}
		
		return obj;
	}
	
	//--------------------------- Private Members ---------------------------//

	private static Object toPlatformObject0(JValue val, ConversionContext cc, int depth) throws MappedTypeConversionException {
		val = val.deref();
		
		// Special - convert null
		if (val == RefValue.NULL){
			return null;
		}
		
		switch(val.getKind()){
		case BOOLEAN:
			return ((BoolValue)val).getBoolValue();
		case BYTE:
			return ((ByteValue)val).getByteValue();
		case CHAR:
			return ((CharValue)val).getCharValue();
		case FLOAT:
			return ((FloatValue)val).getFloatValue();
		case INTEGER:
			return ((IntValue)val).getIntValue();
		case OBJECT:
			ObjectValue ov = (ObjectValue)val;
			switch (ov.getBuiltInValueKind()){
			case ARRAY:
				// Use a list to temporarily store elements of an array
				ArrayValue av = (ArrayValue)val;
				JArrayType jat = (JArrayType)av.getType();
				cc.scriptElementType = jat.getElementType();
				int len = av.getLength();
				cc.addArrayRank(len, depth);
				int ndepth = depth + 1;
				List<Object> list = new ArrayList<Object>(len);
				for(int i = 0; i < len; i++){
					JValue eleVal = av.getValueAt(i); 
					Object eleObj = toPlatformObject0(eleVal, cc, ndepth);
					list.add(eleObj);
				}
				
				return list;
			case STRING:
				return ((StringValue)val).getStringValue();
			case HOSTED:
				HostedValue hv = (HostedValue)val;
				return hv.getHostedObject();
			default:
				break;
			}
			break;
		default:
			break;
		}
		
		throw new MappedTypeConversionException("Unable to convert " + val.getType().getName() + " to a platform type.");
	}
	
	private static void populateArray(Object array, int[] dims, int index, List<Object> list) {
		int rank = dims[index];
		if (index < dims.length - 1) {
			// Intermediate dimension - keep drilling down
			for (int i = 0; i < rank; i++){
				Object elementArray = Array.get(array, i);
				@SuppressWarnings("unchecked")
				List<Object> elementList = (List<Object>)list.get(i);
				populateArray(elementArray, dims, index + 1, elementList);
			}
		} else {
			// Last dimension - set the scalar value
			for (int i = 0; i < rank; i++){
				Array.set(array, i, list.get(i));
			}
		}
	}
	
	private static Class<?> toPlatformScalarType(JType type, ITypeTable tt, HostedMethodManager hmm){
		switch(type.getKind()){
		case BOOLEAN:
			return boolean.class;
		case BYTE:
			return byte.class;
		case CHAR:
			return char.class;
		case FLOAT:
			return float.class;
		case INTEGER:
			return int.class;
		case CLASS:
			if (type == JStringType.getInstance()) {
				return String.class;
			}
			
			TypeValue tvalue = tt.getValue(type.getName());
			List<AttrValue> attrs = tvalue.getClassAttrValues();
			if (attrs != null) {
				for(AttrValue attr : attrs) {
					String pcname = HostedAttributeUtil.getMappedClassName(attr);
					if (pcname != null) { // the type is mapped to a platform class. look it up.
						Class<?> clazz = hmm.getMappedPlatformClass(pcname);
						return clazz;
					}
				}
			}
				
			// FALL THROUGH
		default:
		}
		
		return Object.class;
	}
	
	private static class ConversionContext {
		private JType scriptElementType;
		private List<Integer> arrayRanks;
		private int depth;
		void addArrayRank(int len, int depth){ 
			// depth is to make sure only the first call at any given depth will have effect.
			// at the end arrayRanks.size() should be equal to the dimension of original array.
			if (arrayRanks == null) {
				arrayRanks = new ArrayList<Integer>();
			}
			if (this.depth == depth) {
				arrayRanks.add(len);
				this.depth++;
			}
		}
	}
}
