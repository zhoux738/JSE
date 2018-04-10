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
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;

/**
 * The Object type is the root type in Julian's type system
 * 
 * @author Ming Zhou
 */
@JulianDoc(
name = "Object",
summary = 
    "/*"
+ "\n * Object is the root class in Julian's typing system. All classes are derived from Object."
+ "\n * "
+ "\n * The class-based typing system in Julian is foundamentally analogous to that of Java/C#. It only supports single-inheritance"
+ "\n * so that a class can only have one parent class. If no such parent is specified in the delcaration, Object is used by default."
+ "\n * A class, however, can have unlimited number of interfaces. Similarly,  an interface can also extend zero or more interfaces."
+ "\n * "
+ "\n * The method calling supports dynamic dispatching. The exact method to be called is dependent on the current runtime type. This"
+ "\n * can be illustrated by a classical example. Say we have class C and P, where C extends P. Then in the following code snippet,"
+ "\n * [code]"
+ "\n * P p = new C();"
+ "\n * p.fun();"
+ "\n * [code: end]"
+ "\n * "
+ "\n * what is being really called is implementation of fun() in C, should C have really re-implemented that method."
+ "\n * "
+ "\n * The members of a class can be categorized to static and non-static, a.k.a. instance-based. Static members can and should be"
+ "\n * be called from the class itself, while an instance-based member can only be called against the instance. Within the instance"
+ "\n * method body, one can refer to members of this class by 'this', and those of parent classes by 'super'. Recall, again, that"
+ "\n * such references are dynamically dispatched, so it's impossible to specify the member provided by a certain ancestor along the"
+ "\n * inheritance chain."
+ "\n * "
+ "\n * New instances of an Object can be created by 'new' operator, which is followed by a constructor call. A constructor may also"
+ "\n * call other constructors, which can be referenced by either 'this' or 'super' keyword:"
+ "\n * [code]"
+ "\n * class C : P {"
+ "\n *   C() : this(5){}"
+ "\n *   C(int i) : super(i){}"
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Object is allocated in the heap area and therefore is always passed through references on the call stack. Assiging an object"
+ "\n * to another merely passes along the reference. The one prominent exception to this is for [String](String), which is copied"
+ "\n * by value."
+ "\n * "
+ "\n * Object provides a bunch of basic methods with default implementations based on the underlying platform. This means these methods"
+ "\n * do not have to be implemented by the extending classes, but in certain circumstances such implementation is highly recommeded."
+ "\n * manipulation. The original string instance always remain unchanged."
+ "\n * "
+ "\n * While Object is the root of class hierarchy, it's not the root of all types. All primitive types and [Any](Any) are not classes."
+ "\n * Assigning Object values to primitive typed variables would usually result in illegal assignement exception."
+ "\n * "
+ "\n * For more detailed description on Object and typing system in general, see [Julian Tutorial]."
+ "\n */"
)
public class JObjectType extends JClassType {

	private static final String TYPE_NAME = "Object";
	
	// Names of methods on Object
	public static enum MethodNames {
		toString,
		equals,
		hashCode
	}
	
	public static final FQName FQNAME = new FQName(TYPE_NAME);
	
	private static JObjectType INSTANCE;

	public static JObjectType getInstance() {
		return INSTANCE;
	}
	
	private JObjectType() {
		
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JClassType proto;
		
		@Override
		public JClassType providePrototype(){
			if(proto == null){
				proto = new JObjectType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Parent - no parent, Object is the root of all.
			builder.setParent(null);
			
			//TODO: add the following methods
			/*
			 * getType() : Type
			 */
			
			JClassType objectType = farm.getStub(BuiltinTypes.OBJECT);
			JClassType stringType = farm.getStub(BuiltinTypes.STRING);
			
			// Constructor
			JConstructorType cType = new JConstructorType(
				TYPE_NAME,
				new JParameter[]{
					new JParameter("this", objectType)
				}, 
				CONSTRUCTOR, 
				objectType);
			JClassConstructorMember cmember = new JClassConstructorMember(
				builder.getStub(), 
				getTypeName(), 
				Accessibility.PUBLIC, 
				false,
				cType,
				null,
				true, // This is a default constructor
				null);	// annotations
			builder.addInstanceConstructor(cmember);
			
			// Methods
			// (DEV NOTES: If adding any new instance members, update ClassTestBase.createMembers accordingly)
			
			// toString()
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodNames.toString.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.toString.name(),
						new JParameter[]{
							new JParameter("this", objectType)
						}, 
						stringType, 
					    METHOD_toString, 
					    objectType), 
					null));
			
			// equals(any)
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodNames.equals.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.equals.name(),
						new JParameter[]{
							new JParameter("this", objectType),
							new JParameter("another", AnyType.getInstance())
						}, 
						BoolType.getInstance(), 
					    METHOD_equals, 
					    objectType), 
					null));
			
			// hashCode()
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(), MethodNames.hashCode.name(), Accessibility.PUBLIC, false, false,
					new JMethodType(
						MethodNames.hashCode.name(),
						new JParameter[]{
							new JParameter("this", objectType)
						}, 
						IntType.getInstance(), 
					    METHOD_hashCode, 
					    objectType), 
					null));
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JObjectType.INSTANCE == null){
				JObjectType.INSTANCE = (JObjectType) builder.build(true);
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
	
	// constructor
	@JulianDoc(
		summary =   "/*"
				+ "\n * Create a new Object instance."
				+ "\n */",
		name = "Object"
	)
	private static HostedExecutable CONSTRUCTOR = new HostedExecutable(FQNAME, FQNAME.toString()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// A no-op constructor
			return Result.Void;
		}
	};
	
	// toString() : string
	@JulianDoc(
		summary =   "/*"
				+ "\n * Get the string-formed representation for this object."
				+ "\n * "
				+ "\n * It's recommended that a cutomized object have a deterministic string representation, with its"
				+ "\n * purposes up to the implementors. The string may provide a visualized insight into the contents"
				+ "\n * of the object to facilitate developing process, or be essentially the very semantic value that"
				+ "\n * the object is intended to carry."
				+ "\n * "
				+ "\n * If the subclass doesn't override this method, the default implementation is up to the platform."
				+ "\n * If overriden, the implementation should be idempotent and deterministic."
				+ "\n */",
		params = { },
		returns = "A string that represents the runtime value of this object."
	)
	private static HostedExecutable METHOD_toString = new HostedExecutable(FQNAME, MethodNames.toString.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ObjectValue thisVal = ArgumentUtil.<ObjectValue>getThisValue(args);
			
			String str = thisVal.getType().getName() + "@" + thisVal.hashCode();
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempStringValue(str));
		}
	};
	
	// equals(another Object) : bool
	@JulianDoc(
		summary =   "/*"
				+ "\n * Determine the logical equality between this object and anther."
				+ "\n * "
				+ "\n * In Julian, with the exception of [string](String), objects are compared by reference when '='"
				+ "\n * operator is used. To check the semantic equality based on the internal value contained by the"
				+ "\n * instance, this method should be implemented and called in place of the operator. It's recommended"
				+ "\n * to override this method instead of coming up with a customized equality-check method because"
				+ "\n * this method is also called by a bunch of System classes, such as [Map](System.Collection.Map)."
				+ "\n * "
				+ "\n * If the subclass doesn't override this method, the default implementation is to perform"
				+ "\n * equality check by reference. If overriden, the implementation should be idempotent and deterministic."
				+ "\n */",
		params = { "The other object against which the equality is checked." },
		returns = "true if the two objects are considered equal, however equality means as far as the types of the" +
				  "two participating objects are concerned."
	)
	private static HostedExecutable METHOD_equals = new HostedExecutable(FQNAME, MethodNames.equals.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			JValue thisValue = args[0].getValue().deref();
			JValue thatValue = args[1].getValue().deref();
			
			// Resort to JVM reference equality
			return new Result(TempValueFactory.createTempBoolValue(thisValue == thatValue));
		}
	};
	
	// hashCode() : int
	@JulianDoc(
		summary =   "/*"
				+ "\n * Compute a hash code from this object."
				+ "\n * "
				+ "\n * The hash code is used by certain system classes, most prominently [Map](System.Collection.Map),"
				+ "\n * which needs to project an item at a slot in the map based on the hash value. Not meant for"
				+ "\n * cryptographical purpose, the hash code is neither necessarily unique, nor one-directional."
				+ "\n * However, for the perfomance consideration, a high-quality implementation usually produces"
				+ "\n * less collision than a poor one."
				+ "\n * "
				+ "\n * If the subclass doesn't override this method, the default implementation is up to the platform."
				+ "\n * If overriden, the implementation should be idempotent and deterministic."
				+ "\n */",
		params = { },
		returns = "An integer that represents the hash code of this object. Different instances might produce same hash code."
	)
	private static HostedExecutable METHOD_hashCode = new HostedExecutable(FQNAME, MethodNames.hashCode.name()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			JValue thisValue = args[0].getValue().deref();
			
			// Resort to JVM hashCode()
			return new Result(TempValueFactory.createTempIntValue(thisValue.hashCode()));
		}
	};
}
