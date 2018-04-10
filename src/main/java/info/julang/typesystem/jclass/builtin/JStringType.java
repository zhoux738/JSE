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

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.HostedExecutable;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JArgumentException;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.conversion.TypeIncompatibleException;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.JObjectType.MethodNames;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMemberDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMembersDoc;

/**
 * The String type as in<br/>
 * <code>
 *   string s = "sample";
 * </code>
 * 
 * @author Ming Zhou
 */
@JulianDoc(
alias = "string",
name = "String",
summary = 
    "/*"
+ "\n * String represents a fixed array of characters."
+ "\n * "
+ "\n * String is a very special class type in Julian. Its assignment behavior is copy-by-value, instead of copy-by-references."
+ "\n * This means assigning a string ```SA``` to string ```SB``` would first create a bit-level copy of the value that ```SA```"
+ "\n * points to, then let the copy be pointed by ```SB```, discarding whatever ```SB``` was previously referring."
+ "\n * "
+ "\n * String is immutable. The methods exposed by this class are for reading its contents in various ways, and if any "
+ "\n * manipulating is implied (such as [replace](#repleace)), it always mean to create a new string as the result of "
+ "\n * manipulation. The original string instance always remain unchanged."
+ "\n * "
+ "\n * String supports concatenaton operation by '+', which can also be used along with values of other types, as long as at"
+ "\n * least one operand is string."
+ "\n */"
)
@JulianFieldMembersDoc(
	@JulianFieldMemberDoc(
		name = "length",
		summary = "/* The length of this string. */"
	)
)
public class JStringType extends JClassType {

	public static FQName FQNAME = new FQName("String");
	
	private static JStringType INSTANCE;

	public static JStringType getInstance() {
		return INSTANCE;
	}
	
	private JStringType() {
		
	}
	
	public static boolean isStringType(JType type){
		return type == INSTANCE;
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	@Override
	public Convertibility getConvertibilityTo(JType type) {
		if(type.isBasic()){
			return Convertibility.CASTABLE;
		} else {
			return super.getConvertibilityTo(type);
		}
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JStringType proto;
		
		@Override
		public JClassType providePrototype(){
			if(proto == null){
				proto = new JStringType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){		
			//Self
			builder.setFinal(true);
			
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));
			
			//Fields
			builder.addInstanceMember(
				JClassFieldMember.makeInstanceConstField(
					builder.getStub(), "length", Accessibility.PUBLIC, IntType.getInstance()));
			
			//Methods
			JClassType stringType = farm.getStub(BuiltinTypes.STRING);
			JClassType objectType = farm.getStub(BuiltinTypes.OBJECT);
	
			//-------------------- Object --------------------//
			
			//toString
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					MethodNames.toString.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.toString.name(),
						new JParameter[]{
							new JParameter("this", objectType)
						}, 
						stringType, 
					    METHOD_toString, 
					    stringType),
				    null));
			
			//equals
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					MethodNames.equals.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.equals.name(),
						new JParameter[]{
							new JParameter("this", objectType),
							new JParameter("another", objectType)
						}, 
						BoolType.getInstance(), 
					    METHOD_equals, 
					    stringType),
				    null));

			//hashCode
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					MethodNames.hashCode.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.hashCode.name(),
						new JParameter[]{
							new JParameter("this", objectType)
						}, 
						IntType.getInstance(), 
					    METHOD_hashCode, 
					    stringType),
				    null));
			
			//-------------------- String --------------------//
			
			//contains
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"contains", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"contains",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("search"), 
						}, 
					    BoolType.getInstance(), 
					    METHOD_contains, 
					    stringType),
				    null));
			
			//endsWith
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"endsWith", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"endsWith",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("suffix"), 
						}, 
					    BoolType.getInstance(), 
					    METHOD_endsWith, 
					    stringType),
				    null));
			
			//startsWith
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"startsWith", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"startsWith",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("prefix"), 
						}, 
					    BoolType.getInstance(), 
					    METHOD_startsWith, 
					    stringType),
				    null));

			//indexOf
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"indexOf", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"indexOf",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("search"), 
							new JParameter("offset", IntType.getInstance()), 
						}, 
						IntType.getInstance(), 
					    METHOD_indexOf,
					    stringType),
				    null));

			//firstOf
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"firstOf", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"firstOf",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("search"),
						}, 
						IntType.getInstance(), 
					    METHOD_firstOf,
					    stringType),
				    null));
			
			//substring
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"substring", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"substring",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("start", IntType.getInstance()), 
							new JParameter("end", IntType.getInstance()), 
						}, 
						stringType, 
						METHOD_substring,
					    stringType),
				    null));

			//trim
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"trim", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"trim",
						new JParameter[]{
							new JParameter("this", stringType),
						}, 
						stringType, 
						METHOD_trim,
					    stringType),
				    null));

			//toLower
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"toLower", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"toLower",
						new JParameter[]{
							new JParameter("this", stringType),
						}, 
						stringType, 
						METHOD_toLower,
					    stringType),
				    null));
			
			//toUpper
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"toUpper", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"toUpper",
						new JParameter[]{
							new JParameter("this", stringType),
						}, 
						stringType, 
						METHOD_toUpper,
					    stringType),
				    null));
			
			//split
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"split", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"split",
						new JParameter[]{
							new JParameter("this", stringType),
							new JParameter("splitter"),
						}, 
						farm.getArrayType(BuiltinTypes.STRING),
						METHOD_split,
					    stringType),
				    null));
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JStringType.INSTANCE == null){
				JStringType.INSTANCE = (JStringType) builder.build(true);
			}
		}
		
		@Override
		public String getTypeName() {
			return FQNAME.toString();
		}
	
		@Override
		public boolean initiateArrayType() {
			return true;
		}
	}
	
	// contains
	@JulianDoc(
		summary =   "/*"
				+ "\n * Check if the string contains the given string or character."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to search within this string. "
				+ "Note this method is special in that it can take two different types."},
		returns = "true if the searched string/character is found.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_contains  = new HostedExecutable(FQNAME, "contains") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue searchVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			
			// Execute in Java
			boolean res = thisVal.getStringValue().contains(searchVal.getStringValue());
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempBoolValue(res));
		}
	};
	
	// endsWith
	@JulianDoc(
		summary =   "/*"
				+ "\n * Check if the string is ended with the given string or character."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to match to the end of this string. "
				+ "Note this method is special in that it can take two different types."},
		returns = "true if the given string/character is matched to the ending sequence of this string.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_endsWith  = new HostedExecutable(FQNAME, "endsWith") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue suffixVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			
			// Execute in Java
			boolean res = thisVal.getStringValue().endsWith(suffixVal.getStringValue());
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempBoolValue(res));
		}
	};
	
	// startsWith
	@JulianDoc(
		summary =   "/*"
				+ "\n * Check if the string is started with the given string or character."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to match to the start of this string. "
				+ "Note this method is special in that it can take two different types."},
		returns = "true if the given string/character is matched to the starting sequence of this string.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_startsWith  = new HostedExecutable(FQNAME, "startsWith") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue prefixVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			
			// Execute in Java
			boolean res = thisVal.getStringValue().startsWith(prefixVal.getStringValue());
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempBoolValue(res));
		}
	};
	
	// indexOf
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get the starting index (0-based) of the given string or character within this string."
				+ "\n * "
				+ "\n * If only checking for the existence within the entire string, consider using [contains()](#contains) instead."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to search with this string. "
				+ "Note this method is special in that it can take two different types.",
				"The index on the this string from which the search will be performed."},
		returns = "If a non-negative value, it's the index marking the start of the first occurence of "
		        + "the given string/character; if negative, the given stirng/chracter doesn't exist.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_indexOf  = new HostedExecutable(FQNAME, "indexOf") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue searchVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			IntValue offsetVal = ArgumentUtil.<IntValue>getArgumentValue(2, args);
			
			// Execute in Java
			int res = thisVal.getStringValue().indexOf(searchVal.getStringValue(), offsetVal.getIntValue());
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempIntValue(res));
		}
	};

	// firstOf
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get the starting index (0-based) of the first occurence of the given string or character within this string."
				+ "\n * "
				+ "\n * To search only within a certain scope of this string, call [indexOf()](#indexOf) instead."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to search with this string. "
				+ "Note this method is special in that it can take two different types."},
		returns = "If a non-negative value, it's the index marking the start of the first occurence of "
		        + "the given string/character; if negative, the given stirng/chracter doesn't exist.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_firstOf  = new HostedExecutable(FQNAME, "firstOf") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue searchVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			
			// Execute in Java
			int res = thisVal.getStringValue().indexOf(searchVal.getStringValue());
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempIntValue(res));
		}
	};
	
	// substring
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get a substring out of this string."
				+ "\n */",
		params = {"The starting index (inclusive).", "The ending index (exclusive)."},
		returns = "A substring out of this string.",
		exceptions = {"System.ArgumentException: if the arguments contain negative value, or violates the order."}
	)
	private static HostedExecutable METHOD_substring = new HostedExecutable(FQNAME, "substring") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			IntValue sVal = ArgumentUtil.<IntValue>getArgumentValue(1, args);
			IntValue eVal = ArgumentUtil.<IntValue>getArgumentValue(2, args);
			
			// Execute in Java
			try {
			    String res = thisVal.getStringValue().substring(sVal.getIntValue(), eVal.getIntValue());
				
				// Convert the result to Julian type
				return new Result(TempValueFactory.createTempStringValue(res));
			} catch (IndexOutOfBoundsException e) {
				throw new JArgumentException("start' or 'end");
			}
		}
	};
	
	// trim
	@JulianDoc(
		summary =   "/*"
				+ "\n * Trim the start and end of this tring, removing all the leading and trailing blank chracacters."
				+ "\n * "
				+ "\n * Blank characters are line feed (\\r), carriage return (\\n), horizontal tabulation (\\t) and space (\040)."
				+ "\n */",
		params = { },
		returns = "A trimmed string.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_trim = new HostedExecutable(FQNAME, "trim") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			
			// Execute in Java
		    String res = thisVal.getStringValue().trim();
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(res));
		}
	};
	
	// toLower
	@JulianDoc(
		summary =   "/*"
				+ "\n * Convert the string to lower case."
				+ "\n */",
		params = { },
		returns = "A string with same characters, except in lower case.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_toLower = new HostedExecutable(FQNAME, "toLower") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			
			// Execute in Java
		    String res = thisVal.getStringValue().toLowerCase();
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(res));
		}
	};
	
	// toUpper
	@JulianDoc(
		summary =   "/*"
				+ "\n * Convert the string to upper case."
				+ "\n */",
		params = { },
		returns = "A string with same characters, except in upper case.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_toUpper = new HostedExecutable(FQNAME, "toUpper") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			
			// Execute in Java
		    String res = thisVal.getStringValue().toUpperCase();
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(res));
		}
	};
	
	// split
	@JulianDoc(
		summary =   "/*"
				+ "\n * Split the string into multiple substrings at the specified boundary."
				+ "\n */",
		params = { "The boundary to split at, which is not included into the resultant substrings." },
		returns = "An array of strings split out of this string.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_split = new HostedExecutable(FQNAME, "split") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			CharValue splitterVal = JStringType.coerceCharValue(this.methodName, 1, args, runtime);
			
			// Execute in Java
		    String[] res = thisVal.getStringValue().split(String.valueOf(splitterVal.getCharValue()));
			
			// Convert the result to Julian type
		    ArrayValue av = TempValueFactory.createTemp1DArrayValue(runtime.getTypeTable(), JStringType.getInstance(), res.length);
		    for(int i=0;i<av.getLength();i++){
		    	StringValue sv= TempValueFactory.createTempStringValue(res[i]);
		    	sv.assignTo(av.getValueAt(i));
		    }
		    
			return new Result(TempValueFactory.createTempRefValue(av));
		}
	};
	
	//------------------------------- Object -------------------------------//
	
	// toString() : string
	private static HostedExecutable METHOD_toString = new HostedExecutable(FQNAME, MethodNames.toString.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(thisVal.getStringValue()));
		}
	};
	
	// equals(another Object) : bool
	private static HostedExecutable METHOD_equals  = new HostedExecutable(FQNAME, MethodNames.equals.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			JValue thisValue = args[0].getValue().deref();
			JValue thatValue = args[1].getValue().deref();

			// Resort to Java String's equals()
			boolean res = false;
			StringValue sv = (StringValue)thisValue;
			if (thatValue.getType() == JStringType.INSTANCE){
				StringValue sv2 = (StringValue)thatValue;
				res = sv.getStringValue().equals(sv2.getStringValue());
			}
			
			return new Result(TempValueFactory.createTempBoolValue(res));
		}
	};
	
	// hashCode() : int
	private static HostedExecutable METHOD_hashCode  = new HostedExecutable(FQNAME, MethodNames.hashCode.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			JValue thisValue = args[0].getValue().deref();
			
			// Resort to Java String's hashCode()
			return new Result(TempValueFactory.createTempIntValue(
				((StringValue)thisValue).getStringValue().hashCode()));
		}
	};
	
	/**
	 * Get value of the argument from the given array that matches the specified index, and coerce that
	 * to a string value.
	 * 
	 * @return null if the index is illegal or the value is not coerceable to string.
	 */
	private static StringValue coerceStringValue(String methodName, int index, Argument[] args, ThreadRuntime runtime){
		if(index >= 0 && index < args.length){
			JValue v = args[index].getValue().deref();
			if(v.getKind() == JValueKind.CHAR){
				// return a string value copied from the original char value
				CharValue cv = (CharValue)v;
				return (StringValue) cv.replicateAs(JStringType.getInstance(), runtime.getStackMemory().currentFrame());
			} else if (JStringType.isStringType(v.getType())){
				// return original value if it is a string
				return (StringValue) v;
			}
			
			throw new TypeIncompatibleException(v.getType(), CharType.getInstance());
		}
		
		throw new IllegalArgumentsException(methodName, "Wrong number of arguments");
	}
	
	/**
	 * Get value of the argument from the given array that matches the specified index, and coerce that
	 * to a char value.
	 * 
	 * @return null if the index is illegal or the value is not coerceable to char.
	 */
	private static CharValue coerceCharValue(String methodName, int index, Argument[] args, ThreadRuntime runtime){
		if(index >= 0 && index < args.length){
			JValue v = args[index].getValue().deref();
			if(v.getKind() == JValueKind.CHAR){
				// return original value if it is a char
				return (CharValue)v;
			} else if (JStringType.isStringType(v.getType())){
				// return a char value which is copied from the original string value if its length is 1
				String sv = ((StringValue)v).getStringValue();
				if(sv.length() > 0){
					char c = sv.charAt(0);
					return new CharValue(runtime.getStackMemory().currentFrame(), c);
				}
			}
			
			throw new TypeIncompatibleException(v.getType(), JStringType.getInstance());
		}
		
		throw new IllegalArgumentsException(methodName, "Wrong number of arguments");
	}
}
