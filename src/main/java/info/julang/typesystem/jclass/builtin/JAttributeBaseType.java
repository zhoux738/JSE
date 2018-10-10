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

import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;

/**
 * This base class contains most data and logic shared among concrete Attribute types. Note while <font color="green">Attribute</font>
 * inherits from <font color="green">Object</font> in Julian, in the implementation {@link JAttributeBaseType} inherits from
 * {@link JClassType}.
 * <pre>
 *       [Julian type hierarchy]    [Implemented by] (extending)
 *              Object           ==   JObjectType   ---------->  JClassType
 *                /|\                                               /|\
 *                 |                                                 |
 *                 |                                                 |
 *             Attribute         ==  JAttributeBaseType   -----------+
 *              /  |  \                                             /|\
 *             /   |   \                                             |
 *            /    |    \                                    JDefinedClassType
 *           /     |     \                                           |
 *          /      |      \                                          | 
 *      Owner   Author  Logging  ==   JAttributeType   --------------+
 * </pre>
 * @author Ming Zhou
 */
@JulianDoc(
name = "Attribute",
summary = 
    "/*"
+ "\n * Attribute is a special class type which provides the ability to define meta-data for other ordinary types."
+ "\n * Attribute inherits [Object](Object), but cannot be instantiated directly by 'new' operator. Its use is "
+ "\n * strictly limited at certain prescribed sites at class and member definition."
+ "\n * "
+ "\n * An attribute can contain zero or more fields:"
+ "\n * [code]"
+ "\n * attribute Copyright {"
+ "\n *   string name; "
+ "\n *   int year;"
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * And it's used with annotation syntax when defining other types:"
+ "\n * [code]"
+ "\n * [Copyright(name = \"James Kucan\", year=2010)]"
+ "\n * class ServiceModel {"
+ "\n *   [Copyright(name = \"Larson J. Fisher\", year=2011)]"
+ "\n *   void report(){"
+ "\n *     "
+ "\n *   }"
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * The usage of attributes, such as use-site and repeatability, can be controlled by [meta-attribute](System.AttributeType)."
+ "\n * "
+ "\n * Since attributes are initialized during type loading, only a small subset of types can be used, either as type of an "
+ "\n * attribute member, or referenced through an initializer. The allowed types include all primitive types, module-less Object"
+ "\n * types such as [Object] and [String], as well as all [Enum] types including those defined by users. Also, despite being "
+ "\n * not an Enum, [System.AttributeTarget] is also allowed. One-dimensional arrays of any of allowed scalar types are allowed too. " 
+ "\n * All the other types, whether from the system (```System.*```) or defined by a user, are denied access in an attribute "
+ "\n * source context, either at definition or in initializer. Attempts to use such types will result in exceptions faulting type "
+ "\n * loading."
+ "\n * [code]"
+ "\n * attribute MyAttribute {"
+ "\n *   string[] name; // OK: one-dimensional array of allowed scalar type is also allowed."
+ "\n *   Object[][] objs; // ERROR: multi-dimensional arrays are not allowed."
+ "\n * }"
+ "\n * "
+ "\n * [MyAttribute(name = (new NameBuilder()).getName())] // ERROR: attmpet to use disallowed types in initializer."
+ "\n * class MyObject {"
+ "\n * "
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Attributes can be retrieved during runtime through reflection API. For more detailed description on Attribute, "
+ "\n * see [tutorial on Attribute](tutorial: attribute)."
+ "\n */",
references = { "System.AttributeType" }
)
public class JAttributeBaseType extends JClassType {
	
	public static final String Name = "Attribute";
	
	private static JAttributeBaseType INSTANCE;

//	protected final static String InternalField_Bool_AllowMultiple = "h-allowMultiple";
//	protected final static String InternalField_Int_ApplicableTargets = "h-applicableTargets";
	
	public static JAttributeBaseType getInstance() {
		return INSTANCE;
	}
	
	private JAttributeBaseType() {
		
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
//	/**
//	 * Get default members of <font color="green">Attribute</font>.
//	 * 
//	 * @return
//	 */
//	public static JClassMember[] getDefaultMembers(){
//		JClassFieldMember fmember1 = new JClassFieldMember(
//			InternalField_Bool_AllowMultiple, 
//			Accessibility.HIDDEN, 
//			true, 
//			true, // static and const can co-exist.
//			BoolType.getInstance(),
//			null);	
//		
//		JClassFieldMember fmember2 = new JClassFieldMember(
//			InternalField_Int_ApplicableTargets, 
//			Accessibility.HIDDEN, 
//			true, 
//			true, // static and const can co-exist.
//			IntType.getInstance(),
//			null);
//		
//		return new JClassMember[]{fmember1, fmember2};
//	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {

		private JAttributeBaseType proto;
		
		@Override
		public JClassType providePrototype(){
			if(proto == null){
				proto = new JAttributeBaseType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){			
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));
			
			//Disallow inheritance from this class in script
			builder.setFinal(true);
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JAttributeBaseType.INSTANCE == null){
				JAttributeBaseType.INSTANCE = (JAttributeBaseType) builder.build(true);
			}
		}
		
		@Override
		public String getTypeName() {
			return Name;
		}
		
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}
}
