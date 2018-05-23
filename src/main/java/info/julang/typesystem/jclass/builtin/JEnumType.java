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

import java.util.HashMap;
import java.util.Map;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedExecutable;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassInitializerMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.JParameter;

/**
 * The Enum type in Julian scripting language.
 * <p>
 * All JEnumTypes are {@link info.julang.typesystem.jclass.JClassType}. They have 
 * N static fields each with the name defined in the enum declaration. Each such field stores 
 * a {@link RefValue} that points to a {@link EnumValue}, which itself also contains two fields: 
 * <code>literal</code> (<code style="color: green">int</code>) and 
 * <code>ordinal</code> (<code style="color: green">String</code>).
 * 
 * @author Ming Zhou
 */
public class JEnumType extends JDefinedClassType {

	private String defaultLiteral;
	private int defaultOrdinal;
	private Map<String, Integer> enums;
	
	public JEnumType(String name) {
		super(name);
	}

	@Override
	public Convertibility getConvertibilityTo(JType type) {
		if(type.getKind() == JTypeKind.INTEGER || type == JStringType.getInstance()){
			return Convertibility.CASTABLE;
		}
		
		return super.getConvertibilityTo(type);
	}
	
	/**
	 * Populate an Enum type by
	 * <p>
	 * 1) adding static enum fields corresponding to each enum value <br/>
	 * 2) adding initializers for each enum field.
	 * <p>
	 * Note this method doesn't finish initializing an enum type. The initializers are yet to
	 * be invoked by type loader.
	 * 
	 * @param fqname the fully qualified name of this enum type.
	 * @param acc the accessibility of this enum type.
	 * @param literals it's assumed this array has a length >= 1, and it's of same length as <code>ordinals</code>. These conditions are guaranteed at the callsite.
	 * @param ordinals it's assumed this array has a length >= 1, and it's of same length as <code>literals</code>. These conditions are guaranteed at the callsite.
	 * @param enumTyp the enum type object
	 * @param builder the builder responsible for building this enum type.
	 */
	public static void populateEnumType(
		FQName fqname, 
		Accessibility acc, 
		String[] literals, 
		int[] ordinals, 
		JEnumType enumTyp, 
		JClassTypeBuilder builder){
	
		int total = literals.length;
		
		builder.setParent(JEnumBaseType.getInstance());
		builder.setAccessibility(acc);
		builder.setFinal(true);
		builder.setAbstract(false);
		
		Map<String, Integer> enums = new HashMap<String, Integer>();
		
		JParameter[] params = new JParameter[0];
		JType retType = VoidType.getInstance();
		
		for(int i=0;i<total;i++){
			String fieldName = literals[i];
			int fieldOrd = ordinals[i];
			
			// 1) static field member
			JClassMember sfield = new JClassFieldMember(
				builder.getStub(), 
				fieldName,
				Accessibility.PUBLIC,
				true, // static field
				true, // constant
				enumTyp, // of this type
				null);	// annotations
			builder.addStaticMember(sfield);
			
			// 2) initializer
			EnumInitializerExecutable exec = new EnumInitializerExecutable(fqname, fieldName, fieldOrd);
			JMethodType mType = new JMethodType("<init>-" + fieldName, params, retType, exec, enumTyp);
			
			JClassInitializerMember mmember = new JClassInitializerMember(
				builder.getStub(),
				fieldName,  // field name
				true, 		// static
				mType);
			builder.addInitializerMember(mmember);
			
			enums.put(fieldName, fieldOrd);
			
			if(i == 0){
				enumTyp.defaultLiteral = fieldName;
				enumTyp.defaultOrdinal = fieldOrd;
			}
		}
		
		enumTyp.enums = enums;
	}
	
	/**
	 * Create an Enum type. This will create all the static fields for each enum values, but will not initialize them.
	 * Initializing happens when creating {@link TypeValue} instance.
	 * 
	 * @param fqname
	 * @param acc
	 * @param literals
	 * @param ordinals
	 * @return
	 */
	public static JEnumType createEnumType(FQName fqname, Accessibility acc, String[] literals, int[] ordinals){
		int total = literals.length;
		if (ordinals == null){
			// If no explicit ordinal values are given, use a 0-based index with interval = 1.
			ordinals = new int[total];
			for(int i=0;i<total;i++){
				ordinals[i] = i;
			}
		} else {
			// Basic checks
			if(total != ordinals.length){
				throw new JSEError("Illegal enum values for enum type: " + fqname);
			}
		}
		
		JEnumType enumTyp = new JEnumType(fqname.toString());
		JClassTypeBuilder builder = enumTyp.getBuilder();
		
		populateEnumType(
			fqname, 
			acc, 
			literals, 
			ordinals, 
			enumTyp, 
			builder);
		
		return enumTyp;
	}

	/**
	 * Get a map with enum's literal-ordinal pairs.
	 * @return
	 */
	public Map<String, Integer> getEnums() {
		return enums;
	}
	
	public String getDefaultLiteral() {
		return defaultLiteral;
	}

	public int getDefaultOrdinal() {
		return defaultOrdinal;
	}
	
	/**
	 * Check if a type is Enum.
	 * @param typ
	 * @return
	 */
	public static boolean isEnumType(JType typ){
		if(typ instanceof JEnumType){
			return true;
		}
		
		return false;
	}
	
	// enum field initializer
	private static class EnumInitializerExecutable extends HostedExecutable  {
		
		private String literal;
		private int ordinal;
		
		private EnumInitializerExecutable(FQName fqname, String literal, int ordinal){
			super(fqname, ".init_"+ literal);
			this.literal = literal;
			this.ordinal = ordinal;
		}
		
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			String fn = className.toString();
			ITypeTable tt = runtime.getTypeTable();
			JEnumType etype = (JEnumType) tt.getType(fn);
			TypeValue tVal = tt.getValue(fn);
			
			// Get the static enum field of given name, e.g. Mars (literal="Mars") in Planet.
			// Note the enum value, whose members (literal/ordinal) are now set to default values, is contained in a reference value. 
			RefValue val = (RefValue) tVal.getMemberValue(literal);
			
			// Create a new enum value using the ordinal and literal for this enum value.
			EnumValue eVal = new EnumValue(runtime.getHeap(), etype, ordinal, literal);
			RefValue rVal = TempValueFactory.createTempRefValue(eVal);
			
			// Replace the default enum value.
			rVal.assignTo(val);
			
			return Result.Void;
		}
		
	}
	
}
