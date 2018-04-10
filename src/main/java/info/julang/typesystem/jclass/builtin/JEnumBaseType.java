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
import info.julang.hosting.HostedExecutable;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMemberDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMembersDoc;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;

/**
 * The Enum type as in<p/>
 * <code>
 *   enum Planet { 
 *     Mars,
 *     Earth,
 *     Venus
 *   }
 * </code>
 * <p/>
 * Enum in Julian is implemented as a special class. It has <font color="green">Object</font> as parent class,
 * contains two constant fields: <font color="green">Enum</font>.ordinal (int) and <font color="green">Enum</font>.literal (string).
 * <p/>
 * The language provides a syntax for enum declaration. The code <code> enum Planet { Mars, Earth, Venus } </code>
 * is actually equivalent to <pre><code>class Planet : Enum { 
 * 
 *   static const Mars = new(0, "Mars");
 *   
 *   static const Earth = new(1, "Earth");
 *   
 *   static const Venus = new(2, "Venus");
 *   
 *   private int ordinal;
 *   private string literal;
 *   
 *   Planet(int ordinal, string literal){
 *     this.ordinal = ordinal;
 *     this.literal = literal;
 *   }  
 *   
 * } </code></pre>
 * 
 * However, users cannot write this directly in their scripts.
 * <p/>
 * This base class contains most data and logic shared among concrete Enum types. Note while <font color="green">Enum</font>
 * inherits from <font color="green">Object</font> in Julian, in the implementation {@link JEnumBaseType} inherits from
 * {@link JClassType}.
 * <pre>
 *       [Julian type hierarchy]    [Implemented by]
 *              Object           ==   JObjectType   ---------->  JClassType
 *                /|\                                               /|\
 *                 |                                                 |
 *                 |                                                 |
 *               Enum            ==  JEnumBaseType  -----------------+
 *              /  |  \                                              |
 *             /   |   \                                             | 
 *            /    |    \                                            | 
 *           /     |     \                                           | 
 *          /      |      \                                          | 
 *      MyEnum  Planet   Mode    ==   JEnumType     -----------------+
 * </pre>
 * @author Ming Zhou
 */
@JulianDoc(
name = "Enum",
summary = 
    "/*"
+ "\n * Enum is a special class type which defines an ordered enumeration of names, each associated with an integer value."
+ "\n * Enum inherits [Object](Object), but cannot be instantiated. Its use is strictly limited to immutable access to"
+ "\n * its members in a static context."
+ "\n * "
+ "\n * To declare an enum, "
+ "\n * [code]"
+ "\n * enum Color {"
+ "\n *   RED = 0,"
+ "\n *   YELLOW = 1,"
+ "\n *   GREEN = 2"
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * The integer value defined for each name is not required. If not provided, it's defaulted to the value of previous"
+ "\n * name plus 1. In the case of first name, it's defaulted to 0. Each value must be distinct within the enum, otherwise"
+ "\n * would cause definition exception when the type is loaded."
+ "\n * "
+ "\n * The name of an enum can be referred to by normal addressing syntax, such as"
+ "\n * [code]"
+ "\n * Color c = Color.YELLOW;"
+ "\n * [code: end]"
+ "\n * "
+ "\n * However, these names cannot be used as a left value."
+ "\n * "
+ "\n * An enum variable, when defined without an initializer, will be initialized to the first name of that enum type. The"
+ "\n * null value cannot be assigned to an enum variable."
+ "\n * "
+ "\n * An enum's member has two constant fields. [literal](#literal) exposes the name as a [string], [ordinal](#ordinal)"
+ "\n * the associated integer value."
+ "\n * "
+ "\n * Julian provides syntax-level support for switch logic based on an enum variable:"
+ "\n * [code]"
+ "\n * Color c = ...;"
+ "\n * switch(c){"
+ "\n * case RED: ..."
+ "\n * case YELLOW: ..."
+ "\n * case GREEN: ..."
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * For more detailed description on Enum, see [tutorial on Enum](tutorial: enum)."
+ "\n */"
)
@JulianFieldMembersDoc({
	@JulianFieldMemberDoc(
		name = "ordinal",
		summary = "/* The integer value associated with this member. */"
	),
	@JulianFieldMemberDoc(
		name = "literal",
		summary = "/* The name of this member, exactly as is defined in the type's definition. */"
	)
})
public class JEnumBaseType extends JClassType {

	public final static FQName FQNAME = new FQName("Enum");
	
	public final static String FIELD_ORDINAL = "ordinal";
	
	public final static String FIELD_LITERAL = "literal";
	
	private static JEnumBaseType INSTANCE;

	public static JEnumBaseType getInstance() {
		return INSTANCE;
	}
	
	private JEnumBaseType() {
		
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {

		private JEnumBaseType proto;
		
		@Override
		public JClassType providePrototype(){
			if(proto == null){
				proto = new JEnumBaseType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){			
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));
			
			//Disallow inheritance from this class in script
			builder.setFinal(true);
			
			JClassType objectType = farm.getStub(BuiltinTypes.OBJECT);
			JClassType stringType = farm.getStub(BuiltinTypes.STRING);
			
			//Field
			builder.addInstanceMember(
				JClassFieldMember.makeInstanceConstField(
					builder.getStub(), FIELD_ORDINAL, Accessibility.PUBLIC, IntType.getInstance()));
			
			builder.addInstanceMember(
				JClassFieldMember.makeInstanceConstField(
					builder.getStub(), FIELD_LITERAL, Accessibility.PUBLIC, stringType));
			
			//Method
			//Object.toString
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), "toString", Accessibility.PUBLIC, false, false,
					new JMethodType(
						"toString",
						new JParameter[]{
							new JParameter("this", objectType)
						}, 
						stringType, 
					    METHOD_toString, 
					    stringType), 
					    null));
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JEnumBaseType.INSTANCE == null){
				JEnumBaseType.INSTANCE = (JEnumBaseType) builder.build(true);
			}
		}
		
		@Override
		public String getTypeName() {
			return FQNAME.toString();
		}
		
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}
	
	// Object.toString
	private static HostedExecutable METHOD_toString  = new HostedExecutable(FQNAME, "toString") {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			EnumValue thisVal = ArgumentUtil.<EnumValue>getThisValue(args);
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(thisVal.getLiteral()));
		}
	};
}
