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

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.regex.Pattern;

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.HostedExecutable;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.JIllegalStateException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.BasicArrayValueExposer;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.IArrayValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JArgumentException;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.conversion.TypeIncompatibleException;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.JObjectType.MethodNames;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMemberDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMembersDoc;
import info.julang.typesystem.jclass.jufc.SystemTypeNames;
import info.julang.typesystem.loading.ITypeResolver;

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
+ "\n * manipulating is implied (such as [trim](#trim)), it always mean to create a new string as the result of "
+ "\n * manipulation. The original string instance always remains unchanged."
+ "\n * "
+ "\n * String is iterable. This means one can use foreach grammar on a string, or get a character out of string directly:"
+ "\n * [code]"
+ "\n * for (char c : str) {"
+ "\n *   ..."
+ "\n * }"
+ "\n * char c0 = str[0];"
+ "\n * str[0] = 'a'; // This will not cause a runtime error, but won't have any effect either."
+ "\n * [code: end]"
+ "\n * "
+ "\n * String supports concatenation operation by '```+```', which can also be used along with values of other types, as long as"
+ "\n * at least one operand is string."
+ "\n */",
interfaces = { "System.Util.IComparable", "System.Util.IIndexable", "System.Util.IIterable" }
)
@JulianFieldMembersDoc(
	@JulianFieldMemberDoc(
		name = "length",
		summary = "/* The length of this string. */"
	)
)
public class JStringType extends JClassType implements IDeferredBuildable {

	public static FQName FQNAME = new FQName("String");
	
	private static JStringType INSTANCE;

	public static JStringType getInstance() {
		return INSTANCE;
	}
	
	private JStringType() {
		super();
		initialized = false;
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
	
	public static class BootstrapingBuilder implements TypeBootstrapper {
		
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
	
	         //fromBytes (static)
            builder.addStaticMember(
                new JClassMethodMember(
                    builder.getStub(), 
                    "fromBytes", Accessibility.PUBLIC, true, false,
                    new JMethodType(
                        "fromBytes",
                        new JParameter[]{
                            new JParameter("bytes", farm.getArrayType(BuiltinTypes.BYTE)),
                            new JParameter("charset", stringType),
                            new JParameter("offset", IntType.getInstance()),
                            new JParameter("length", IntType.getInstance())
                        }, 
                        stringType,
                        METHOD_fromBytes2,
                        stringType),
                    null));
            
            //fromBytes (static)
            builder.addStaticMember(
                new JClassMethodMember(
                    builder.getStub(), 
                    "fromBytes", Accessibility.PUBLIC, true, false,
                    new JMethodType(
                        "fromBytes",
                        new JParameter[]{
                            new JParameter("bytes", farm.getArrayType(BuiltinTypes.BYTE)),
                        }, 
                        stringType,
                        METHOD_fromBytes,
                        stringType),
                    null));
    
            //toBytes
            builder.addInstanceMember(
                new JClassMethodMember(
                    builder.getStub(), 
                    "toBytes", Accessibility.PUBLIC, false, false,
                    new JMethodType(
                        "toBytes",
                        new JParameter[]{
                            new JParameter("this", stringType),
                            new JParameter("charset", stringType)
                        }, 
                        farm.getArrayType(BuiltinTypes.BYTE),
                        METHOD_toBytes2,
                        stringType),
                    null));
            
            //toBytes
            builder.addInstanceMember(
                new JClassMethodMember(
                    builder.getStub(), 
                    "toBytes", Accessibility.PUBLIC, false, false,
                    new JMethodType(
                        "toBytes",
                        new JParameter[]{
                            new JParameter("this", stringType)
                        }, 
                        farm.getArrayType(BuiltinTypes.BYTE),
                        METHOD_toBytes,
                        stringType),
                    null));
            
			//fromChars (static)
			builder.addStaticMember(
				new JClassMethodMember(
					builder.getStub(), 
					"fromChars", Accessibility.PUBLIC, true, false,
					new JMethodType(
						"fromChars",
						new JParameter[]{
							new JParameter("chars", farm.getArrayType(BuiltinTypes.CHAR))
						}, 
						stringType,
					    METHOD_fromChars,
					    stringType),
				    null));
			
			//toChars
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"toChars", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"toChars",
						new JParameter[]{
							new JParameter("this", stringType)
						}, 
						farm.getArrayType(BuiltinTypes.CHAR),
					    METHOD_toChars,
					    stringType),
				    null));
			
			//isEmpty (static)
			builder.addStaticMember(
				new JClassMethodMember(
					builder.getStub(), 
					"isEmpty", Accessibility.PUBLIC, true, false,
					new JMethodType(
						"isEmpty",
						new JParameter[]{
							new JParameter("str", stringType)
						}, 
					    BoolType.getInstance(), 
					    METHOD_isEmpty, 
					    stringType),
				    null));
			
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

			//compare
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"compare", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"compare",
						new JParameter[]{
							new JParameter("this", stringType),
							new JParameter("another", AnyType.getInstance()), 
						}, 
						IntType.getInstance(), 
					    METHOD_compare,
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

			//lastIndexOf
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), 
					"lastIndexOf", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"lastIndexOf",
						new JParameter[]{
							new JParameter("this", stringType), 
							new JParameter("search"), 
							new JParameter("offset", IntType.getInstance()), 
						}, 
						IntType.getInstance(), 
					    METHOD_lastIndexOf,
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

	        //replace
            builder.addInstanceMember(
                new JClassMethodMember(
                    builder.getStub(), 
                    "replace", Accessibility.PUBLIC, false, false,
                    new JMethodType(
                        "replace",
                        new JParameter[]{
                            new JParameter("this", stringType), 
                            new JParameter("oldStr", stringType), 
                            new JParameter("newStr", stringType), 
                        }, 
                        stringType, 
                        METHOD_replace,
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
		public void bootstrapItself(JClassTypeBuilder builder){
			if(JStringType.INSTANCE == null){
				JStringType jst = (JStringType) builder.build(false);
				jst.setBuilder(builder);
				JStringType.INSTANCE = jst;
			}
		}
		
		@Override
		public void reset() {
			JStringType.INSTANCE = null;
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
	   
    // fromBytes
    @JulianDoc(
        summary =   "/*"
                + "\n * Create a string from an array of [bytes](type: byte) in specified encoding."
                + "\n */",
        params = {"An array of bytes.",
                  "The charset name. Charset names should have been registered by *RFC 2278: IANA Charset Registration Procedures*.",
                  "The offset in the array to start converting.",
                  "The total count of bytes to use."},
        paramTypes = {"[byte]", "string", "int", "int"},
        isStatic = true,
        returns = "A string comprised of the given chars.",
        exceptions = {
        	"System.NullReferenceException: if the parameter is null.",
        	"System.ArgumentException: if charset is not recognized/supported."
        }
    )
    private static HostedExecutable METHOD_fromBytes2  = new HostedExecutable(FQNAME, "fromBytes") {
        @Override
        protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
        	IArrayValue av = (IArrayValue)RefValue.dereference(args[0].getValue());
            BasicArrayValueExposer exp = new BasicArrayValueExposer((BasicArrayValue)av);
            StringValue sv = (StringValue)RefValue.dereference(args[1].getValue());
            int offset = ((IntValue)args[2].getValue().deref()).getIntValue();
            int length = ((IntValue)args[3].getValue().deref()).getIntValue();
            
            byte[] bytes = exp.getByteArray();
            if (offset < 0 || offset >= bytes.length) {
                throw new JArgumentException("offset");
            }
            if (length < 0) {
                throw new JArgumentException("length");
            } else if (length > bytes.length) {
                length = bytes.length;
            }

            try {
                String str = new String(bytes, offset, length, sv.getStringValue());
                return new Result(TempValueFactory.createTempStringValue(str));
            } catch (UnsupportedEncodingException e) {
                throw new JArgumentException("charset");
            }
        }
    };
    
    // fromBytes
    @JulianDoc(
        summary =   "/*"
                + "\n * Create a string from an array of [bytes](type: byte) in ASCII encoding."
                + "\n */",
        params = {"An array of bytes."},
        paramTypes = {"[byte]"},
        isStatic = true,
        returns = "A string comprised of the given chars decoded from the byte array.",
        exceptions = {"System.NullReferenceException: if the parameter is null."}
    )
    private static HostedExecutable METHOD_fromBytes  = new HostedExecutable(FQNAME, "fromBytes") {
        @Override
        protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
        	IArrayValue av = (IArrayValue)RefValue.dereference(args[0].getValue());
            BasicArrayValueExposer exp = new BasicArrayValueExposer((BasicArrayValue)av);
            byte[] bytes = exp.getByteArray();
            try {
                String str = new String(bytes, "ascii");
                return new Result(TempValueFactory.createTempStringValue(str));
            } catch (UnsupportedEncodingException e) {
                throw new JSEError("The platform doesn't support ASCII encoding.");
            }
        }
    };
    
    // toBytes
    @JulianDoc(
        summary =   "/*"
                + "\n * Convert this string to an array of [bytes](type: byte) in ASCII encoding."
                + "\n */",
        params = { },
        returns = "A byte array consisting of all the bytes in this string.",
        exceptions = { }
    )
    private static HostedExecutable METHOD_toBytes  = new HostedExecutable(FQNAME, "toBytes") {
        @Override
        protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
            // Extract arguments
            StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
            String value = thisVal.getStringValue();
            
            byte[] barray = value.getBytes();
            int len = barray.length;

            BasicArrayValue bav = (BasicArrayValue)ArrayValueFactory.createArrayValue(
                runtime.getStackMemory(), runtime.getTypeTable(), ByteType.getInstance(), len);
            byte[] tarray = (byte[])bav.getPlatformArrayObject();
                
            System.arraycopy(barray, 0, tarray, 0, len);
            
            // Convert the result to Julian type
            return new Result(bav);
        }
    };
  
    // toBytes
    @JulianDoc(
        summary =   "/*"
                + "\n * Convert this string to an array of [bytes](type: byte) using specified charset."
                + "\n */",
        params = { "string" },
        returns = "A byte array consisting of all the bytes in this string, encoded with the specified charset.",
        exceptions = {
            	"System.ArgumentException: if charset is not recognized/supported.",
        		"System.Lang.RuntimeCheckException: if string cannot be converted using the charset."
        	}
    )
    private static HostedExecutable METHOD_toBytes2  = new HostedExecutable(FQNAME, "toBytes") {
        @Override
        protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
            // Extract arguments
            StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
            String value = thisVal.getStringValue();
            
            StringValue sv = (StringValue)RefValue.dereference(args[1].getValue());
            String charsetName = sv.toString();
            
            byte[] barray = null;
            
            try {
                CharsetEncoder encoder = Charset.forName(charsetName).newEncoder()
                	.onMalformedInput(CodingErrorAction.REPORT)
                	.onUnmappableCharacter(CodingErrorAction.REPORT);
                CharBuffer buffer = CharBuffer.wrap(value);
                ByteBuffer bb = encoder.encode(buffer);
                barray = bb.array();
            } catch (UnsupportedOperationException e) { 
        		throw new JArgumentException("charset");
            } catch (CharacterCodingException e) {
        		throw new JIllegalStateException("Unable to convert this string to a byte array with charset '" + charsetName + "'.");
            }
            
            int len = barray.length;

            BasicArrayValue bav = (BasicArrayValue)ArrayValueFactory.createArrayValue(
                runtime.getStackMemory(), runtime.getTypeTable(), ByteType.getInstance(), len);
            byte[] tarray = (byte[])bav.getPlatformArrayObject();
                
            System.arraycopy(barray, 0, tarray, 0, len);
            
            // Convert the result to Julian type
            return new Result(bav);
        }
    };
    
	// fromChars
	@JulianDoc(
		summary =   "/*"
				+ "\n * Create a string from an array of [chars](type: char)."
				+ "\n */",
		params = {"An array of chars."},
		isStatic = true,
		returns = "A string comprised of the given chars.",
		exceptions = {"System.NullReferenceException: if the parameter is null."}
	)
	private static HostedExecutable METHOD_fromChars  = new HostedExecutable(FQNAME, "fromChars") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			Argument arg = args[0];
			IArrayValue av = (IArrayValue)arg.getValue().deref();
			BasicArrayValueExposer exp = new BasicArrayValueExposer((BasicArrayValue)av);
			char[] chars = exp.getCharArray();
			String str = new String(chars);
			return new Result(TempValueFactory.createTempStringValue(str));
		}
	};
		
	// toChars
	@JulianDoc(
		summary =   "/*"
				+ "\n * Convert this string to a char array."
				+ "\n */",
		params = { },
		returns = "A char array consisting of all the characters in this string.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_toChars  = new HostedExecutable(FQNAME, "toChars") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			char[] src = thisVal.getStringValue().toCharArray();
			int len = src.length;
			
			BasicArrayValue bav = (BasicArrayValue)ArrayValueFactory.createArrayValue(
				runtime.getStackMemory(), runtime.getTypeTable(), CharType.getInstance(), len);
			
			char[] dest = (char[])bav.getPlatformArrayObject();
			System.arraycopy(src, 0, dest, 0, len);
			
			// Convert the result to Julian type
			return new Result(bav);
		}
	};
	
	// isEmpty
	@JulianDoc(
		summary =   "/*"
				+ "\n * Check if the string is null or empty (containing no characters)."
				+ "\n */",
		params = {"The string to check."},
		isStatic = true,
		returns = "true if the checked string is null or empty.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_isEmpty  = new HostedExecutable(FQNAME, "isEmpty") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			JValue ret = args[0].getValue().deref();
			if (ret.isNull()) {
				return new Result(TempValueFactory.createTempBoolValue(true));
			} else {
				String str = ((StringValue)ret).getStringValue();
				if ("".equals(str)) {
					return new Result(TempValueFactory.createTempBoolValue(true));
				}
			}

			return new Result(TempValueFactory.createTempBoolValue(false));
		}
	};
	
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
	
	// compare
	@JulianDoc(
		summary =   "/*"
				+ "\n * Compare this string against another value, which can be either a string or a char. If the"
				+ "\n * other value is neither string nor char, returns 0."
				+ "\n * "
				+ "\n * This method implements [System.Util.IComparable]."
				+ "\n */",
		params = {"The other value to compare to."},
		returns = "If a negative value, this string is alphabetically less than the other; if positive, larger; if 0, equal or incomparable.",
		exceptions = { }
	)
	private static HostedExecutable METHOD_compare  = new HostedExecutable(FQNAME, "compare") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// String value implements Comparable<JValue>, allowing itself to compare to string or char
			Comparable<JValue> thisVal = (Comparable<JValue>)ArgumentUtil.<StringValue>getThisValue(args);
			JValue val = args[1].getValue().deref();
			
			// If the other value is neither string nor char, this method would return 0
			int res = thisVal.compareTo(val);
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempIntValue(res));
		}
	};
	
	// indexOf
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get the starting index (0-based) of the first occurence of the given string or character within this string."
				+ "\n * "
				+ "\n * If only checking for the existence within the entire string, consider using [contains()](#contains) instead."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to search within this string from the specified index, from left to right. "
				+ "Note this method is special in that it can take two different types.",
				"The index on this string from which the forward search will be performed."},
		returns = "If a non-negative value, it's the index marking the start of the first occurence of "
		        + "the given string/character; if negative, the given stirng/character doesn't exist.",
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
	
	// lastIndexOf
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get the starting index (0-based) of the last occurence of the given string or character within this string."
				+ "\n * "
				+ "\n * If only checking for the existence within the entire string, consider using [contains()](#contains) instead."
				+ "\n */",
		params = {"The sub-string, or a single chracater, to search within this string from the specified index, from right to left. "
				+ "Note this method is special in that it can take two different types.",
				"The index on the this string from which the backward search will be performed."},
		returns = "If a non-negative value, it's the index marking the start of the last occurence of "
		        + "the given string/character; if negative, the given stirng/character doesn't exist.",
		exceptions = {"System.TypeIncompatibleException: if the parameter has a type which is neither string nor char."}
	)
	private static HostedExecutable METHOD_lastIndexOf  = new HostedExecutable(FQNAME, "lastIndexOf") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
			StringValue searchVal = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
			IntValue offsetVal = ArgumentUtil.<IntValue>getArgumentValue(2, args);
			
			// Execute in Java
			int res = thisVal.getStringValue().lastIndexOf(searchVal.getStringValue(), offsetVal.getIntValue());
			
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
		        + "the given string/character; if negative, the given stirng/character doesn't exist.",
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

	// replace
    @JulianDoc(
        summary =   "/*"
                + "\n * Replace all occurences of a substring with another one."
                + "\n */",
        params = {"The string to replace.", "The string to replace with"},
        returns = "A string with all occurences of the specified substring replaced with the new substring.",
        exceptions = { }
    )
    private static HostedExecutable METHOD_replace = new HostedExecutable(FQNAME, "replace") {
        @Override
        protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
            // Extract arguments
            StringValue thisVal = ArgumentUtil.<StringValue>getThisValue(args);
            StringValue oldStr = JStringType.coerceStringValue(this.methodName, 1, args, runtime);
            StringValue newStr = JStringType.coerceStringValue(this.methodName, 2, args, runtime);
            
            // Execute in Java
            String res = thisVal.getStringValue().replace(oldStr.getStringValue(), newStr.getStringValue());
            
            // Convert the result to Julian type
            return new Result(TempValueFactory.createTempStringValue(res));
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
		params = { "The boundary char or string to split at, which is not included into the resultant substrings." },
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
			// Java's string split targets regex. Julian the literal. So call Pattern.quote to escape the input.
		    String[] res = thisVal.getStringValue().split(
		    	Pattern.quote(String.valueOf(splitterVal.getCharValue())));
			
			// Convert the result to Julian type
		    ArrayValue av = TempValueFactory.createTemp1DArrayValue(runtime.getTypeTable(), JStringType.getInstance(), res.length);
		    for(int i=0;i<av.getLength();i++){
		    	StringValue sv= TempValueFactory.createTempStringValue(res[i]);
		    	sv.assignTo(av.getValueAt(i));
		    }
		    
			return new Result(TempValueFactory.createTempRefValue(av));
		}
	};
	
	// getIterator() : IIterator
	@JulianDoc(
		name = "getIterator",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get an iterator from this string. The iterator produces characters which consist of this string."
				+ "\n * "
				+ "\n * This is the implementation of [System.Util.IIterable]."
				+ "\n */",
		params = { },
		exceptions = { },
		returns = "A string iterator ready to [move on](type: System.Util.IIterator#next)."
	)
	private static HostedExecutable METHOD_get_iterator = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue src = (StringValue)(args[0].getValue().deref());
			
			// Load StringIterator, an internal type that implements IIterator on top of an array
			ITypeTable tt = runtime.getTypeTable();
			JType typ = tt.getType(SystemTypeNames.System_Util_StringIterator);
			if (typ == null) {
				Context context = Context.createSystemLoadingContext(runtime);
				typ = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_StringIterator));
			}
			
			// Create an instance of StringIterator
			NewObjExecutor noe = new NewObjExecutor(runtime);
			JClassType jct = (JClassType)typ;
			JClassConstructorMember jccm = jct.getClassConstructors()[0];
			ObjectValue result = noe.newObjectInternal(jct, jccm, new Argument[] { new Argument("str", src) } );
			
			return new Result(TempValueFactory.createTempRefValue(result));
		}
	};
	
	// at(var) : var
	@JulianDoc(
		name = "at",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the character at specified index."
				+ "\n * "
				+ "\n * This is the implementation of getter method on [System.Util.IIndexble]."
				+ "\n */",
		params = {"An index at which the character is to be retrieved." },
		paramTypes = {"Any"},
		exceptions = { "System.ArrayOutOfRangeException: When the index is out of range." },
		returns = "The value retrieved."
	)
	private static HostedExecutable METHOD_get = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue src = (StringValue)(args[0].getValue().deref());
			JValue val = args[1].getValue();
			int index = ValueUtilities.getIntValue(val, "index"); // argument exception
			JValue ele = src.getValueAt(index); // array out-of-index exception
			return new Result(ele);
		}
	};
	
	// at(var, var)
	@JulianDoc(
		name = "at",
		isStatic = false,
		summary =   "/*"
				+ "\n * This method is implemented for [System.Util.IIndexble], but due to the immutability of string "
				+ "\n * it will do nothing."
				+ "\n */",
		params = {"An index at which the given value is to be set.", "The value to set."},
		paramTypes = {"Any", "Any"},
		exceptions = { }
	)
	private static HostedExecutable METHOD_set = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			return Result.Void;
		}
	};
	
	// size() : int
	@JulianDoc(
		name = "size",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the length of the string."
				+ "\n * "
				+ "\n * This is the implementation of ```size()``` method on [System.Util.IIndexble]. This is equivalent to [length](#length) field."
				+ "\n */",
		params = { },
		returns = "The length of this array."
	)
	private static HostedExecutable METHOD_size = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.SIZE) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			StringValue src = (StringValue)(args[0].getValue().deref());
			JValue ele = TempValueFactory.createTempIntValue(src.getLength());
			return new Result(ele);
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
	
	//--------------- IDeferredBuildable ---------------//
	
	private ICompoundTypeBuilder builder;
	
	@Override
	public boolean deferBuild(){
		return true;
	}
	
	@Override
	public void completeBuild(Context context) {
		if (builder != null) {
			JInterfaceType jit = null;
			ITypeResolver resolver = context.getTypeResolver();
			jit = (JInterfaceType)resolver.resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IComparable));
			builder.addInterface(jit);
			jit = (JInterfaceType)resolver.resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIndexable));
			builder.addInterface(jit);
			jit = (JInterfaceType)resolver.resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIterable));
			builder.addInterface(jit);
			
			/*
			 * System.Util.IIndexable:
			 *   var at(var)
			 *   void at(var, var)
			 *   int size()
			 */			
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.AT, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.AT,
						new JParameter[]{
							new JParameter("this", JStringType.INSTANCE),
							new JParameter("index"),
						}, 
						AnyType.getInstance(), 
						METHOD_get,
						JStringType.INSTANCE),
				    null));
			
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.AT, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.AT,
						new JParameter[]{
							new JParameter("this", JStringType.INSTANCE),
							new JParameter("index"),
							new JParameter("value"),
						}, 
						VoidType.getInstance(), 
						METHOD_set,
						JStringType.INSTANCE),
				    null));
	
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.SIZE, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.SIZE,
						new JParameter[]{
							new JParameter("this", JStringType.INSTANCE),
						}, 
						IntType.getInstance(), 
						METHOD_size,
						JStringType.INSTANCE),
				    null));
			
			// System.Util.IIterable
			// IIterator getIterator()

			jit = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIterator));
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.GET_ITERATOR, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.GET_ITERATOR,
						new JParameter[]{
							new JParameter("this", JStringType.INSTANCE),
						}, 
						jit, 
						METHOD_get_iterator,
						JStringType.INSTANCE),
				    null));
			
			builder.seal();
			builder = null;
		}
	}

	@Override
	public void setBuilder(ICompoundTypeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void preInitialize() {
		this.initialized = true;
	}
}
