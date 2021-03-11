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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.HostedExecutable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.DynamicValue;
import info.julang.memory.value.FuncValue;
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
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.jufc.SystemTypeNames;
import info.julang.typesystem.jclass.jufc.SystemTypeUtility;
import info.julang.typesystem.loading.ITypeResolver;

/**
 * The Dynamic type as in
 * <pre><code>
 *   var d1 = new { x = "abc", y = 15, c = new Car() }; // create with an initializer
 *   var d2 = new Dynamic();
 *   d2.x = "abc"; // add a new member
 *   var val = d2.x;
 *   d2.x = true; // overwrite with different type
 * </code></pre>
 * 
 * @author Ming Zhou
 */
@JulianDoc(
// alias = (No language alias)
name = "Dynamic",
summary = 
    "/*"
+ "\n * Dynamic is a scripting-friendly class that bypasses most member access checks (existence, typing, visibility) during runtime."
+ "\n * "
+ "\n * In the following example, we first create a Dynamic object using its default constructor, then dynamically add and update a few \"fields\","
+ "\n * which, in the parlance of Julian, are referred to as **properties**:"
+ "\n * [code]"
+ "\n * Dynamic dynObj = new Dynamic();"
+ "\n * dynObj.tag = \"test\";"
+ "\n * dynObj.doubleFun = (int x) => x * 2;"
+ "\n * Console.println(dynObj.tag); // print \"test\""
+ "\n * Console.println(dynObj.doubleFun(10)); // print \"10\""
+ "\n * dynObj.tag = 25;"
+ "\n * Console.println(dynObj.tag); // print \"25\""
+ "\n * [code: end]"
+ "\n * "
+ "\n * As this example shows, the properties can be accessed and mutated without definition. This behavior remarkably"
+ "\n * constrasts to the regular class object where all the class members must be defined in the class body. In fact, JSE treats"
+ "\n * Dynamic object in a special way when it comes to member access. It won't check the member's existence, and will disregard"
+ "\n * the type of the existing value if it's to be overwritten. The regular members inherited from the parent class (Object) or"
+ "\n * implemented by Dynamic itself, such as [toString](Object#toString) and [getIterator](IIterable#getIterator), cannot be overwritten by a property."
+ "\n * "
+ "\n * Properties can't be accessed via reflection, for they are not class members."
+ "\n * "
+ "\n * Since Dynamic implements [System.Util.IMapInitializable], one may also create the object with initial properties in a single" 
+ "\n * statement:"
+ "\n * [code]"
+ "\n * Dynamic dynObj = new Dynamic(){"
+ "\n *   \"tag\" = \"test\","
+ "\n *   doubleFun = (int x) => x * 2"
+ "\n * };"
+ "\n * [code: end]"
+ "\n * "
+ "\n * The left side of `=` can have a few different forms: single quoted char (`'a'`), double quoted string (`\"key\"`), an identifier or an expression"
+ "\n * encapsulated in parentheses. IMapInitializable would usually accept more forms of the left side than these four, but Dynamic "
+ "\n * is special and would treat other kinds of key that are totally legal in, say [System.Collection.Map], as input error."
+ "\n * "
+ "\n * In the case of `char` type, the engine will coerce it into a string. For identifier, the engine also re-interprets it as a "
+ "\n * string. This is true even if in the current scope there is actually a defined identifier of the same name that happens to"
+ "\n * represent a string value. If a variable has to be used to provide the key, one may enclose it with parentheses, so that the"
+ "\n * entire left side will be evaluated as an expression, as demonstrated in the ensuing example:"
+ "\n * [code]"
+ "\n * string key_a = \"nam_a\","
+ "\n * Dynamic dynObj = new Dynamic(){"
+ "\n *   key_a = \"val0\", // key_a treated as \"key_a\""
+ "\n *   key_b = \"val1\", // key_b treated as \"key_b\""
+ "\n *   (key_a) = \"val2\", // key_a evaluated to \"nam_a\""
+ "\n * };"
+ "\n * Console.println(dynObj.key_a); // val0"
+ "\n * Console.println(dynObj.key_b); // val1"
+ "\n * Console.println(dynObj.nam_a); // val2"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Since all keys in Dynamic must be of type [string], failing to provide a string-typed key will result in [System.Lang.RuntimeCheckException]."
+ "\n * "
+ "\n * Dynamic implements [System.Util.IIndexable], such that the members can be retrieved using standard index syntax or the underlying"
+ "\n * access methods. This alternative is useful when the key is not a legal identifier."
+ "\n * [code]"
+ "\n * dynObj[\"tag\"] = \"test\";"
+ "\n * var tag = dynObj.at(\"tag\");"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Dynamic also implements [System.Util.IIterable], so enumerating its consituent members is supported at the language level."
+ "\n * [code]"
+ "\n * for (var entry : dynObj){"
+ "\n *   Console.println(entry.key + \"=\" + entry.value);"
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * There are still a few subtleties surrounding the practical usage of Dynamic class: what would happen if we access to a property"
+ "\n * that doesn't exist (the default behavior is to return `null`); what `this` means inside a lambda that is set to a Dynamic object"
+ "\n * (the default behavior is to bind with the dynamic object itself, if it has not been bound yet). These behaviors, however, can be"
+ "\n * fine-tuned using the second constructor."
+ "\n * "
+ "\n * For more information about Dynamic class and its usage, refer to [the tutorial](tutorial: dynamic)."
+ "\n */",
interfaces = { "System.Util.IMapInitializable", "System.Util.IIndexable", "System.Util.IIterable" }
)
public class JDynamicType extends JClassType implements IDeferredBuildable {

	public static FQName FQNAME = new FQName("Dynamic");
	
	private static JDynamicType INSTANCE;

	public static JDynamicType getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Check whether a value is a Dynamic.
	 * 
	 * @param value The value to be checked.
	 * @return true if the value has a type equal to or derived from Dynamic.
	 * false otherwise (including the value being null).
	 */
	public static boolean isDynamicType(JValue value) {
		value = value.deref();
		if (value != null) {
			JType rawTyp = value.getType();
			if (rawTyp == INSTANCE) { // Using Dynamic directly is a very common case.
				return true;
			}
			
			if (value.getKind() == JValueKind.OBJECT) {
				ICompoundType typ = (ICompoundType)rawTyp;
				return INSTANCE.canDerive(typ, false);
			}
		}
		
		return false;
	}
	
	/**
	 * Check whether a type is a Dynamic.
	 * 
	 * @param type The type to be checked.
	 * @return true if the given type is equal to or derived from Dynamic.
	 */
	public static boolean isDynamicType(ICompoundType type) {
		if (type == INSTANCE) { // Using Dynamic directly is a very common case.
			return true;
		}

		return INSTANCE.canDerive(type, false);
	}
	
	private JDynamicType() {
		super();
		initialized = false;
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BootstrapingBuilder implements TypeBootstrapper {
		
		private JDynamicType proto;
		
		@Override
		public JClassType providePrototype(){
			if(proto == null){
				proto = new JDynamicType();
			}
			return proto;
		}
		
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Self
			//builder.setFinal(true); // Inheritable
			
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));

			JClassType dynamicType = farm.getStub(BuiltinTypes.DYNAMIC);
			
			//Constructors
			JConstructorType cType1 = new JConstructorType(
				FQNAME.toString(),
				new JParameter[]{
					new JParameter("this", dynamicType)
				}, 
				CONSTRUCTOR_DEFAULT, 
				dynamicType);
			JClassConstructorMember cmember1 = new JClassConstructorMember(
				builder.getStub(), 
				getTypeName(), 
				Accessibility.PUBLIC, 
				false,
				cType1,
				null,
				true, // This is the default constructor
				null);// annotations
			builder.addInstanceConstructor(cmember1);
			
			JConstructorType cType2 = new JConstructorType(
				FQNAME.toString(),
				new JParameter[]{
					new JParameter("this", dynamicType),
					new JParameter("config", dynamicType)
				}, 
				CONSTRUCTOR_SECONDARY,
				dynamicType);
			JClassConstructorMember cmember2 = new JClassConstructorMember(
				builder.getStub(), 
				getTypeName(), 
				Accessibility.PUBLIC, 
				false,
				cType2,
				null,
				false, // This is not the default constructor
				null); // annotations
			builder.addInstanceConstructor(cmember2);
		}
		
		@Override
		public void bootstrapItself(JClassTypeBuilder builder){
			if(JDynamicType.INSTANCE == null){
				JDynamicType jst = (JDynamicType) builder.build(false);
				jst.setBuilder(builder);
				JDynamicType.INSTANCE = jst;
			}
		}
		
		@Override
		public void reset() {
			JDynamicType.INSTANCE = null;
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
	
	// constructor (default)
	@JulianDoc(
		summary =   "/*"
				+ "\n * Create a new Dynamic instance with default behavior."
				+ "\n * "
				+ "\n * The default behavior refers to: returning `null` when accessing to"
				+ "\n * an unset member; no automatic binding to `this` argument."
				+ "\n */",
		name = "Dynamic"
	)
	private static HostedExecutable CONSTRUCTOR_DEFAULT = new HostedExecutable(FQNAME, FQNAME.toString()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// A no-op constructor
			return Result.Void;
		}
	};
	
	// constructor (configurable)
	@JulianDoc(
		summary =   "/*"
				+ "\n * Create a new Dynamic instance with specified behavior. The argument is itself a Dynamic object."
				+ "\n * All accepted properties are of `bool` type with default value = `false`."
				+ "\n * "
				+ "\n * <ul>"
				+ "\n * <li><span class=\"l\">autobind</span><span class=\"r\">"
				+ "If set, automatically bind the reference to `this` appearing in the function body of a [Function] "
				+ "object to the current Dynamic object, regardless of the function's kind (lambda, member method, "
				+ "global function). If not set, only literal lambda will be bound.</span></li>"
				+ "\n * <li><span class=\"l\">sealed</span><span class=\"r\">"
				+ "If set, will throw [System.IllegalMemberAccessException] when overwriting any property after "
				+ "instantiation. Therefore all properties must be populated using the initializer syntax.</span></li>"
				+ "\n * <li><span class=\"l\">throwOnUndefined</span><span class=\"r\">"
				+ "If set, will throw [System.UnknownMemberException] when trying to retrieve a property "
				+ "that has not been added.</span></li>"
				+ "\n * </ul>"
				+ "\n */",
		name = "Dynamic",
		paramTypes = {"config"},
		params = {"See summary for an overview. See [the tutorial](tutorial: dynamic) for more details."}
	)
	private static HostedExecutable CONSTRUCTOR_SECONDARY = new HostedExecutable(FQNAME, FQNAME.toString()) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			DynamicValue val = (DynamicValue)ArgumentUtil.getThis(args).getValue().deref();
			DynamicValue conf = (DynamicValue) ArgumentUtil.getArgument("config", args).getValue().deref();
			val.init(conf);
			return Result.Void;
		}
	};
	
	//--------------- Methods ---------------//
	
	// at(var) : var
	@JulianDoc(
		name = "at",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the value by key of string type. This is equivalent to retrieving the member by direct access using addressing syntax."
				+ "\n * "
				+ "\n * This is the implementation of getter method on [System.Util.IIndexble]."
				+ "\n */",
		params = {"The key. Must be of [string] type." },
		paramTypes = {"Any"},
		exceptions = {
				"System.ArgumentException: When the key is not a string.",
				"System.Lang.RuntimeCheckException: When the key doesn't exist. Only thrown if the object is created with `throwIfNotExist == true`." },
		returns = "The value retrieved."
	)
	private static HostedExecutable METHOD_get = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Key should be a string
			JValue key = args[1].getValue();
			StringValue keyVal = StringValue.dereference(key, false);
			if (keyVal == null) {
				throw new JArgumentException("key");
			}
			String keyStr = keyVal.getStringValue();
			
			DynamicValue dv = (DynamicValue)ArgumentUtil.getThis(args).getValue();
			JValue resVal = dv.get(keyStr);
			if (resVal == null) {
				if (dv.shouldThrowIfNotExist()) {
					throw new UnknownPropertyException(dv, keyStr);
				} else {
					resVal = RefValue.NULL;
				}
			}

			return new Result(resVal);
		}
	};
	
	// at(var, var)
	@JulianDoc(
		name = "at",
		isStatic = false,
		summary =   "/*"
				+ "\n * Set the value by key of string type."
				+ "\n */",
		params = {"They key by which the given value is to be set.", "The value to set."},
		paramTypes = {"Any", "Any"},
		exceptions = { }
	)
	private static HostedExecutable METHOD_set = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Key should be a string
			JValue key = args[1].getValue();
			StringValue keyVal = StringValue.dereference(key, false);
			if (keyVal == null) {
				throw new JArgumentException("key");
			}
			String keyStr = keyVal.getStringValue();
			
			// Value can be anything
			JValue val = args[2].getValue();
			
			DynamicValue dv = (DynamicValue)ArgumentUtil.getThis(args).getValue();
			// Before HostedExecutable is called, all args are already duplicated on the stack. 
			// However, since Dynamic stores the kv map outside regular memory management 
			// ObjectMemberStorage), we must duplicate them again on heap
			JValue dupVal = ValueUtilities.replicateValue(val, null, runtime.getHeap());
			dv.set(keyStr, dupVal);

			return Result.Void;
		}
	};
	
	// size() : int
	@JulianDoc(
		name = "size",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the count of members of this Dynamic instance, ecluding those inherited from the parent class."
				+ "\n * "
				+ "\n * This is the implementation of ```size()``` method on [System.Util.IIndexble]."
				+ "\n */",
		params = { },
		returns = "The count of no-inherited members."
	)
	private static HostedExecutable METHOD_size = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.SIZE) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			DynamicValue dv = (DynamicValue)ArgumentUtil.getThis(args).getValue();
			int cnt = dv.count();
			JValue ele = TempValueFactory.createTempIntValue(cnt);
			return new Result(ele);
		}
	};
	
	// initByMap() : void
	@JulianDoc(
		name = "initByMap",
		isStatic = false,
		summary =   "/*"
				+ "\n * Initliaze the dynamic object with an array of key-value pairs."
				+ "\n * "
				+ "\n * Only key of string type is allowed. Null key is not allowed. If duplicated by key, the last entry by"
				+ "\n * the textual order wins. When invoked by the scripting engine as a translation of map initializer"
				+ "\n * (the map-like syntax structure immediately following the constructor call), identifiers"
				+ "\n * and char literals will be re-interpreted as strings, while other key expressions which do not"
				+ "\n * evaluate to a string or char will be considered illegal input, incurring"
				+ "\n * [RuntimeCheckException](System.Lang.RuntimeCheckException)."
				+ "\n */",
		params = { "System.Util.Entry[]" },
		returns = "",
		exceptions = {"System.ArgumentException: the key is null or not a string type."}
	)
	private static HostedExecutable METHOD_initByMap = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.SIZE) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			DynamicValue dv = (DynamicValue)ArgumentUtil.getThis(args).getValue();
			ArrayValue val = (ArrayValue)RefValue.dereference(args[1].getValue());
			
			// First put all key-value pairs to a map. We use this staging area to 
			// 1) ensure all-or-none initialization
			// 2) de-duplicate keys (last wins)
			Map<String, JValue> map = new HashMap<String, JValue>();
			
			int len = val.getLength();
			for (int i = 0; i < len; i++) {
				JValue kvpVal = null;
				
				try {
					kvpVal = ((JValue)val.get(i)).deref();
				} catch (NullPointerException | ClassCastException ex) {
					throw new JSEError(
						"Dynamic object is initialized with null or object of unexpected type.",
						JDynamicType.class);
				}
				
				if (kvpVal == RefValue.NULL) {
					// Skip null entry
					continue;
				}
				
				if (kvpVal.getKind() != JValueKind.OBJECT) {
					throw new JSEError("Dynamic object is initialized with non-Entry object.", JDynamicType.class);
				}
				
				ObjectValue kvpOv = (ObjectValue)kvpVal;
				
				JValue keyVal = kvpOv.getMemberValue("key");
				StringValue key = StringValue.dereference(keyVal, false);
				if (key == null) {
					throw new JArgumentException("Dynamic object is initialized with non-String key.");
				}
				
				JValue valVal = kvpOv.getMemberValue("value");
				
				// Bind the function value
				if (dv.shouldBindByIndex(i)) {
					JValue deref = valVal.deref();
					if (deref instanceof FuncValue) {
						FuncValue fv = (FuncValue)deref;
						valVal = FunctionBinder.bind(runtime, fv, dv, null);
					}
				}
				
				JValue dupVal = ValueUtilities.replicateValue(valVal, null, runtime.getHeap());
				map.put(key.getStringValue(), dupVal);
			}
			
			for (Entry<String, JValue> entry : map.entrySet()) {
				dv.set(entry.getKey(), entry.getValue());
			}
			
			return Result.Void;
		}
	};
	
	// getIterator() : IIterator
	@JulianDoc(
		name = "getIterator",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get an iterator from this dynamic object."
				+ "\n * "
				+ "\n * As the implementation of [System.Util.IIterable], this method returns an iterator of [key-value pairs](System.Util.Entry)."
				+ "\n * The iterator will be immediately populated with all the dynamic properties which have been set at the timing of invocation. Beware"
				+ "\n * the implications of this behavior: (1) It's not a scalable method. But the programmer is not advised to use a dyanmic object"
				+ "\n * as a massive data storage in the first place; (2) It's not showing the up-to-date data contained in an dynamic object, but as"
				+ "\n * an upside it's not affected by concurrent modification either."
				+ "\n * "
				+ "\n * Members which are not added through [the indexable API](System.Uitl.IIndexable), or its syntax sugar such as index-based"
				+ "\n * or direct member access, won't show up in the resulting iterator."
				+ "\n */",
		params = { },
		exceptions = { },
		returns = "An array iterator ready to [move on](type: System.Util.IIterator#next), producing one [key-value pair](System.Util.Entry) in each iteration."
	)
	private static HostedExecutable METHOD_get_iterator = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.GET_ITERATOR) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			DynamicValue dynVal = (DynamicValue)(args[0].getValue().deref());

			// Load system types needed for completing this operation
			JType arrIterTyp = SystemTypeUtility.ensureTypeBeLoaded(runtime, SystemTypeNames.System_Util_ArrayIterator);
			JType entryTyp = SystemTypeUtility.ensureTypeBeLoaded(runtime, SystemTypeNames.System_Util_Entry);
			
			// Determine the data set to iterate over
			List<Entry<String, JValue>> entries = new ArrayList<Entry<String, JValue>>();
			for (Entry<String, JValue> entry : dynVal) {
				entries.add(entry);
			}
			
			// Create an array of type Entry[]
			int size = entries.size();
			MemoryArea mem = runtime.getHeap();
			ArrayValue entryArrVal = ArrayValueFactory.createArrayValue(
				runtime.getHeap(), runtime.getTypeTable(), entryTyp, entries.size());
			
			// Initialize the array with values to iterate over
			NewObjExecutor newExe = new NewObjExecutor(runtime);
			for (int i = 0; i < size; i++) {
				// Create an Entry object
				Entry<String, JValue> entry = entries.get(i);
				StringValue keyVal = new StringValue(mem, entry.getKey());
				ObjectValue src = newExe.newObject(mem, entryTyp, new JValue[] { keyVal, entry.getValue() });

				// Get the target value at the array
				RefValue dst = (RefValue)entryArrVal.get(i);
				
				// src => dst
				src.assignTo(dst);
			}
			
			// Create an instance of ArrayIterator
			NewObjExecutor noe = new NewObjExecutor(runtime);
			JClassType jct = (JClassType)arrIterTyp;
			JClassConstructorMember jccm = jct.getClassConstructors()[0];
			ObjectValue result = noe.newObjectInternal(jct, jccm, new Argument[] { new Argument("array", entryArrVal) } );
			
			return new Result(TempValueFactory.createTempRefValue(result));
		}
	};
	
	//--------------- IDeferredBuildable ---------------//
	
	private ICompoundTypeBuilder builder;
	private boolean sealable;
	
	@Override
	public boolean deferBuild(){
		return true;
	}
	
	@Override
	public void postBuild(Context context) {
		if (!sealable) {
			if (builder == null) {
				sealable = true;
				return;
			}
			
			JInterfaceType jit = null;
			ITypeResolver resolver = context.getTypeResolver();
			jit = (JInterfaceType)resolver.resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIndexable));
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
							new JParameter("this", JDynamicType.INSTANCE),
							new JParameter("key"),
						}, 
						AnyType.getInstance(), 
						METHOD_get,
						JDynamicType.INSTANCE),
				    null));
			
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.AT, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.AT,
						new JParameter[]{
							new JParameter("this", JDynamicType.INSTANCE),
							new JParameter("key"),
							new JParameter("value"),
						}, 
						VoidType.getInstance(), 
						METHOD_set,
						JDynamicType.INSTANCE),
				    null));
	
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.SIZE, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.SIZE,
						new JParameter[]{
							new JParameter("this", JDynamicType.INSTANCE),
						}, 
						IntType.getInstance(), 
						METHOD_size,
						JDynamicType.INSTANCE),
				    null));

			/*
			 * System.Util.IMapInitializable:
			 *   void initByMap(System.Util.Entry[])	
			 */	
			jit = (JInterfaceType)resolver.resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IMapInitializable));
			builder.addInterface(jit);
			
			// entryTyp := System.Util.Entry
			ITypeTable tt = context.getTypTable();
			JType entryTyp = tt.getType(SystemTypeNames.System_Util_Entry);
			if (entryTyp == null) {
				entryTyp = context.getTypeResolver().resolveType(
					ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_Entry),
					true);
			}
			
			// arrTyp := System.Util.Entry[]
			JArrayType arrTyp = tt.getArrayType(entryTyp);
			if (arrTyp == null) {
				tt.addArrayType(JArrayType.createJArrayType(tt, entryTyp, false));
				arrTyp = tt.getArrayType(entryTyp);
			}
		
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.INIT_BT_MAP, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.INIT_BT_MAP,
						new JParameter[]{
							new JParameter("this", JDynamicType.INSTANCE),
							new JParameter("entries", arrTyp),
						}, 
						VoidType.getInstance(), 
						METHOD_initByMap,
						JDynamicType.INSTANCE),
				    null));
	
			// System.Util.IIterable:
			//   IIterator getIterator()
			jit = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIterable));
			builder.addInterface(jit);
			
			jit = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIterator));
			
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.GET_ITERATOR, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.GET_ITERATOR,
						new JParameter[]{
							new JParameter("this", JDynamicType.INSTANCE),
						}, 
						jit, 
						METHOD_get_iterator,
						JDynamicType.INSTANCE),
				    null));

			sealable = true;
		}
	}
	
	@Override
	public void seal() {
		if (!sealable) {
			throw new JSEError("Couldn't seal built-in type. Building was not complete.", JDynamicType.class);
		}

		if (builder != null) {
			builder.seal();
			builder = null;
		}

		sealable = false;
	}

	@Override
	public void setBuilder(ICompoundTypeBuilder builder) {
		this.builder = builder;
	}

	@Override
	public void preInitialize() {
		this.initialized = true;
	}
	
	@Override
	public BuiltinTypes getBuiltinType() {
		return BuiltinTypes.DYNAMIC;
	}
	
	// Like UnknownMemberException, this is also translated to Julian's System.UnknownMemberException
	private static class UnknownPropertyException extends JSERuntimeException {
		
		private static final long serialVersionUID = 8472552903287639931L;

		public UnknownPropertyException(DynamicValue dv, String mName) {
			super(createMsg(dv, mName));
		}

		private static String createMsg(DynamicValue dv, String mName) {
			StringBuilder sb = new StringBuilder();
			sb.append("The following properties are found: ");
			boolean hasNone = true;
			for (Entry<String, JValue> entry : dv){
				sb.append(entry.getKey());
				sb.append(", ");
				if (hasNone) {
					hasNone = false;
				}
			}
			
			String extInfo;
			if (hasNone) {
				extInfo = "The object doesn't contain any defined property.";
			} else {
				sb.setLength(sb.length() - 2);
				extInfo = sb.toString();
			}
			
			return "No property of name \"" + mName + "\" is defined on this " + FQNAME.toString() + " object. " + extInfo;
		}
		
		@Override
		public KnownJSException getKnownJSException() {
			return KnownJSException.UnknownMember;
		}
	}
}
