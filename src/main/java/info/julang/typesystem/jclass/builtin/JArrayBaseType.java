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
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedExecutable;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectArrayValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper.TypeFarm;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMemberDoc;
import info.julang.typesystem.jclass.builtin.doc.JulianFieldMembersDoc;
import info.julang.typesystem.jclass.jufc.SystemTypeNames;

/**
 * ArrayBase type is the parent type of all other array types. The internal name of this type is just "ARRAY".
 * <p/>
 * <pre>
 *       [Julian type hierarchy]    [Implemented by]
 *              Object           ==   JObjectType   ---------->  JClassType
 *                /|\                                               /|\
 *                 |                                                 |
 *                 |                                                 |
 *               Array           ==  JArrayBaseType -----------------+
 *              /  |  \                  /|\ 
 *             /   |   \                  |
 *            /    |    \                 |
 *           /     |     \                |
 *          /      |      \               |
 *      int[]  String[]  int[][] ==   JArrayType
 * </pre>
 * @author Ming Zhou
 */
@JulianDoc(
name = "Array",
summary = 
    "/*"
+ "\n * Array is an indexable vector structure with direct language support. It inherits from [Object](Object),"
+ "\n * and is instantiated using new expression with a special grammar."
+ "\n * "
+ "\n * To declare an array of one or two dimentions, "
+ "\n * [code]"
+ "\n * int[] ia;"
+ "\n * string[][] saa;"
+ "\n * [code: end]"
+ "\n * "
+ "\n * The [] grammar can be expanded to any degree."
+ "\n * "
+ "\n * To intialize an array, there are two ways. First an all-empty array can be initialized by specifying lengths"
+ "\n * at each dimension in a new expression, "
+ "\n * [code]"
+ "\n * int[] ia = new int[3];"
+ "\n * string[][] saa = new string[2][10];"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Alternatively, an array initializer can be used to specify values at each index."
+ "\n * While Julian does support jagged arrays, length inconsistency is disallowed when using an initializer. If the"
+ "\n * lengths at the same dimension are not aligned, it would incur a runtime exception."
+ "\n * [code]"
+ "\n * int[] ia = new int[]{10, 20};"
+ "\n * string[][] saa = new string[][]{new string[]{\"a\", \"b\"}, new string[]{\"c\", \"d\"}};"
+ "\n * string[][] saa = new string[][]{new string[]{\"a\", \"b\"}, new string[]{\"c\"}};"
+ "\n * // If you want to create jagged array, do something like this"
+ "\n * string[][] saa = new string[2][0];"
+ "\n * saa[0] = new string[3];"
+ "\n * saa[1] = new string[4];"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Note, however, that these two syntaxes cannot be mixed in any fashion."
+ "\n * "
+ "\n * To access an element on an array, use '[]' syntax, a.k.a. indexer:"
+ "\n * [code]"
+ "\n * int i = ia[2];"
+ "\n * ia[f()+3] = g();"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Array is iteratible. This means one can use foreach grammar on an array."
+ "\n * [code]"
+ "\n * for(int i : ia) {"
+ "\n *   ..."
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Array's length is immutable and fixed during initialization. To use a scalable structure consider [List](System.Collection.List)."
+ "\n * "
+ "\n * For more detailed description on Array, see [Julian Tutorial](tutorial: array)."
+ "\n */",
interfaces = { "System.Util.IIndexable", "System.Util.IIterable" }
)
@JulianFieldMembersDoc(
	@JulianFieldMemberDoc(
		name = "length",
		summary = "/* The length of this array. Note for multi-dimensional array this refers to the length of the first dimension. */"
	)
)
public class JArrayBaseType extends JClassType implements IDeferredBuildable {

	public static final FQName FQNAME = new FQName("Array");
	
	private static final String METHOD_NAME_COPY = "copy";
	private static final String METHOD_NAME_FILL = "fill";
	private static final String	METHOD_NAME_SORT = "sort";
	
	private static JArrayBaseType INSTANCE;

	/**
	 * Used only by concrete array types.
	 * @param name
	 */
	protected JArrayBaseType(String name, JClassType parentType) {
		super(name, parentType == null ? JArrayBaseType.getInstance() : parentType, new JClassMember[0]);
		this.initialized = false;
	}
	
	private JArrayBaseType(){
		
	}

	public static JArrayBaseType getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	public static class BoostrapingBuilder implements TypeBootstrapper {
		
		private JArrayBaseType proto;
		
		@Override
		public JArrayBaseType providePrototype(){
			if(proto == null){
				proto = new JArrayBaseType();
			}
			return proto;
		}
		
		@Override
		public void implementItself(JClassTypeBuilder builder, TypeFarm farm){
			//Parent
			builder.setParent(farm.getStub(BuiltinTypes.OBJECT));
			builder.setFinal(true); // The various array types do inherit from the base Array. 
			                        // This flag is only to prevent users from extending the class in script.
			
			//Field
			builder.addInstanceMember(
				JClassFieldMember.makeInstanceConstField(
					builder.getStub(), "length", Accessibility.PUBLIC, IntType.getInstance()));
			
			//Method
			JClassType arrayType = farm.getStub(BuiltinTypes.ARRAY);

			//static copy
			builder.addStaticMember(
				new JClassMethodMember(
					builder.getStub(),
					METHOD_NAME_COPY, Accessibility.PUBLIC, true, false,
					new JMethodType(
						METHOD_NAME_COPY,
						new JParameter[]{
							new JParameter("src", arrayType),
							new JParameter("srcOffset", IntType.getInstance()),
							new JParameter("dst", arrayType),
							new JParameter("dstOffset", IntType.getInstance()),
							new JParameter("count", IntType.getInstance()),
						}, 
						IntType.getInstance(), 
						METHOD_copy,
						arrayType),
				    null));
			
			//static fill
			builder.addStaticMember(
				new JClassMethodMember(
					builder.getStub(),
					METHOD_NAME_FILL, Accessibility.PUBLIC, true, false,
					new JMethodType(
							METHOD_NAME_FILL,
						new JParameter[]{
							new JParameter("src", arrayType),
							new JParameter("val", AnyType.getInstance()),
						}, 
						VoidType.getInstance(), 
						METHOD_fill,
						arrayType),
				    null));
			
			//static sort
			builder.addStaticMember(
				new JClassMethodMember(
					builder.getStub(),
					METHOD_NAME_SORT, Accessibility.PUBLIC, true, false,
					new JMethodType(
							METHOD_NAME_SORT,
						new JParameter[]{
							new JParameter("src", arrayType),
							new JParameter("desc", BoolType.getInstance()),
						}, 
						VoidType.getInstance(), 
						METHOD_sort,
						arrayType),
				    null));
			
			//Method
			//TODO: add the following methods
			/*
			 * hashcode
			 */
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JArrayBaseType.INSTANCE == null){
				JArrayBaseType jabt = (JArrayBaseType) builder.build(false);
				jabt.setBuilder(builder);
				JArrayBaseType.INSTANCE = jabt;
			}
		}

		@Override
		public String getTypeName() {
			return "Array";
		}
		
		@Override
		public boolean initiateArrayType() {
			return false;
		}
	}

	// copy
	@JulianDoc(
		name = "copy",
		isStatic = true,
		summary =   "/*"
				+ "\n * Copy certain section of one array to that of another."
				+ "\n */",
		params = {"The source array.",
				  "The offset at source array to start copy from. 0-based.",
				  "The target array.",
				  "The offset at target array to start copy to. 0-based.",
				  "The total length to copy over."
				 },
		returns = "The number of elements copied. This number is less than or equal to [the specified count](param: count).",
		exceptions = {
				"System.Lang.RuntimeCheckException: if the parameters are not meeting the requirements, such as" +
				"type being incompatible, values overlapping, illegal values or illegal combination thereof, of parametrers, etc."
				}
	)
	private static HostedExecutable METHOD_copy  = new HostedExecutable(FQNAME, METHOD_NAME_COPY) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			IntValue offsetSrc = (IntValue)args[1].getValue();
			ArrayValue dst = (ArrayValue)(args[2].getValue().deref());
			IntValue offsetDst = (IntValue)args[3].getValue();
			IntValue countDst = (IntValue)args[4].getValue();
			
			JClassType srcType = src.getClassType();
			JClassType dstType = dst.getClassType();

			// These two checks may not be necessary
			if (!JArrayType.isArrayType(srcType)){
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Value of source is not an array.");
			}
			if (!JArrayType.isArrayType(dstType)){
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Value of destination is not an array.");
			}
			if (srcType != dstType){
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Values of source and destination are of different types.");
			}
			if (src == dst){
				// We don't allow this as of 0.1.6 (although Java does)
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Source and destination arrays must not refer to the same one.");
			}
			
			int oss = offsetSrc.getIntValue();
			int osd = offsetDst.getIntValue();
			int cnt = countDst.getIntValue();
			
			// Basic sanity checks against oss, osd and cnt.
			if (oss < 0) {
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Value of source offset cannot be negative");
			}
			if (osd < 0) {
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Value of destination offset cannot be negative");
			}
			if (cnt < 0) {
				throw new IllegalArgumentsException(METHOD_NAME_COPY, "Value of count cannot be negative");
			}
			int cnt1 = cnt, cnt2 = cnt;
			int srcToCopy = oss + cnt1;
			if(srcToCopy > src.getLength()){
				cnt1 = Math.max(src.getLength() - oss, 0);
			}
			int dstToCopy = osd + cnt2;
			if(dstToCopy > dst.getLength()){
				cnt2 = Math.max(dst.getLength() - osd, 0);
			}
			cnt = Math.min(cnt1, cnt2); // This is the actual number of elements to be copied.
			
			// Copy the array
			if (cnt > 0){
				JArrayType at = (JArrayType) srcType;
				if (at.getElementType().isBasic()){
					// This is a single dimensional basic type array. We are able to perform native copy.
					BasicArrayValue srcBav = (BasicArrayValue)src;
					Object srcObj = srcBav.getPlatformArrayObject();
					BasicArrayValue dstBav = (BasicArrayValue)dst;
					Object dstObj = dstBav.getPlatformArrayObject();
					
					System.arraycopy(srcObj, oss, dstObj, osd, cnt);
				} else {
					// Have to copy object values one by one.
					ObjectArrayValue srcOav = (ObjectArrayValue)src;
					ObjectArrayValue dstBav = (ObjectArrayValue)dst;
					
					for(int i=0; i<cnt; i++){
						JValue vs = srcOav.getValueAt(oss + i);
						JValue vd = dstBav.getValueAt(osd + i);
						vs.assignTo(vd);
					}
				}
			}
			
			// Convert the result to Julian type
			return new Result(TempValueFactory.createTempIntValue(cnt));
		}
	};
	
	// fill
	@JulianDoc(
		name = "fill",
		isStatic = true,
		summary =   "/*"
				+ "\n * Populate the entire array with a single value."
				+ "\n */",
		params = {"The array to populate.",
				  "The value used to fill out each element."
				 },
		exceptions = {
				"System.IllegalAssignmentException: if the filling value is not incompatible with array's element type."
				}
	)
	private static HostedExecutable METHOD_fill = new HostedExecutable(FQNAME, METHOD_NAME_FILL) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			JValue val = args[1].getValue();
			val = UntypedValue.unwrap(val);
			
			JClassType srcType = src.getClassType();
			JArrayType at = (JArrayType) srcType;
			if (at.getElementType().isBasic()){
				// This is a single dimensional basic type array. We are able to perform native filling.
				BasicArrayValue srcBav = (BasicArrayValue)src;
				srcBav.fill(val);
			} else {
				// Have to fill out object values one by one.
				ObjectArrayValue srcOav = (ObjectArrayValue)src;
				int len = src.getLength();
				for(int i=0; i<len; i++){
					JValue vs = srcOav.getValueAt(i);
					val.assignTo(vs);
				}
			}
			
			return Result.Void;
		}
	};
	
	// sort
	@JulianDoc(
		name = "sort",
		isStatic = true,
		summary =   "/*"
				+ "\n * Sort the given array."
				+ "\n * "
				+ "\n * To sort an array, it requires that the elements be able to compare to each other. Certain primitive"
				+ "\n * types and built-in types are comparable to some others. For example, int, float and bytes types are"
				+ "\n * mutually comparable. However, the user-defined types are not naturally comparable. To add comparability"
				+ "\n * to these types, one must implement [System.Util.IComparable]."
				+ "\n * "
				+ "\n * The sorting process is tolerant of incomparability, but the result will not be even partially"
				+ "\n * correct if any pair of elements is found to be incomparable to each other."
				+ "\n * "
				+ "\n * This method is to sort the given array in place. In particular, it's not thread safe and therefore must"
				+ "\n * be protected by [locks](type: System.Concurrency.Lock)."
				+ "\n */",
		params = {"The array to sort in place.",
				  "If false, sort in ascendingly order; if true, in descending order."
				 },
		exceptions = { }
	)
	private static HostedExecutable METHOD_sort = new HostedExecutable(FQNAME, METHOD_NAME_SORT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			JValue val = args[1].getValue();
			boolean desc = ((BoolValue)val.deref()).getBoolValue();

			src.sort(runtime, desc);
			
			return Result.Void;
		}
	};
	
	// getIterator() : IIterator
	@JulianDoc(
		name = "getIterator",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get an iterator from this array."
				+ "\n * "
				+ "\n * This is the implementation of [System.Util.IIterable]. To boost efficiency, applying 'fast-for' loop"
				+ "\n * over an array will not actually use an iterator produced from this method. To explicitly use this"
				+ "\n * iterator one must call getIterator() on an array to get the iterator object."
				+ "\n */",
		params = { },
		exceptions = { },
		returns = "An array iterator ready to [move on](type: System.Util.IIterator#next)."
	)
	private static HostedExecutable METHOD_get_iterator = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			
			// Load ArrayIterator, an internal type that implements IIterator on top of an array
			ITypeTable tt = runtime.getTypeTable();
			JType typ = tt.getType(SystemTypeNames.System_Util_ArrayIterator);
			if (typ == null) {
				Context context = Context.createSystemLoadingContext(runtime);
				typ = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_ArrayIterator));
			}
			
			// Create an instance of ArrayIterator
			NewObjExecutor noe = new NewObjExecutor(runtime);
			JClassType jct = (JClassType)typ;
			JClassConstructorMember jccm = jct.getClassConstructors()[0];
			ObjectValue result = noe.newObjectInternal(jct, jccm, new Argument[] { new Argument("array", src) } );
			
			return new Result(TempValueFactory.createTempRefValue(result));
		}
	};
	
	// at(var) : var
	@JulianDoc(
		name = "at",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the element at specified index."
				+ "\n * "
				+ "\n * This is the implementation of getter method on [System.Util.IIndexble]. Normally an array is accessed by"
				+ "\n * indexer syntax ```[]```, but the method-based access can prove useful in certain cases such as reflection."
				+ "\n */",
		params = {"An index at which the element is to be retrieved."},
		paramTypes = {"Any"},
		exceptions = { "System.ArrayOutOfRangeException: When the index is out of range."},
		returns = "The value retrieved."
	)
	private static HostedExecutable METHOD_get = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
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
				+ "\n * Set the element at specified index."
				+ "\n * "
				+ "\n * This is the implementation of setter method on [System.Util.IIndexble]. Normally an array is accessed by"
				+ "\n * indexer syntax ```[]```, but the method-based access can prove useful in certain cases such as reflection."
				+ "\n */",
		params = {"An index at which the given value is to be set.", "The value to set."},
		paramTypes = {"Any", "Any"},
		exceptions = { "System.ArrayOutOfRangeException: When the index is out of range."}
	)
	private static HostedExecutable METHOD_set = new HostedExecutable(FQNAME, SystemTypeNames.MemberNames.AT) {
		@Override
		protected Result executeOnPlatform(ThreadRuntime runtime, Argument[] args) {
			// Extract arguments
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			
			// index
			JValue val = args[1].getValue();
			int index = ValueUtilities.getIntValue(val, "index"); // argument exception

			// value
			val = args[2].getValue();

			// current value
			JValue ele = src.getValueAt(index); // array out-of-index exception
			val.assignTo(ele);
			
			return Result.Void;
		}
	};
	
	// size() : int
	@JulianDoc(
		name = "size",
		isStatic = false,
		summary =   "/*"
				+ "\n * Get the size of the array."
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
			ArrayValue src = (ArrayValue)(args[0].getValue().deref());
			JValue ele = TempValueFactory.createTempIntValue(src.getLength());
			return new Result(ele);
		}
	};
	
	//--------------- IDeferredBuildable ---------------//
	
	private ICompoundTypeBuilder builder;
	
	@Override
	public boolean deferBuild(){
		return true;
	}
	
	@Override
	public void completeBuild(Context context) {
		if (builder != null) {
			JInterfaceType jit = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIndexable));
			builder.addInterface(jit);
			jit = (JInterfaceType)context.getTypeResolver().resolveType(ParsedTypeName.makeFromFullName(SystemTypeNames.System_Util_IIterable));
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
							new JParameter("this", JArrayBaseType.INSTANCE),
							new JParameter("index"),
						}, 
						AnyType.getInstance(), 
						METHOD_get,
						JArrayBaseType.INSTANCE),
				    null));
			
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.AT, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.AT,
						new JParameter[]{
							new JParameter("this", JArrayBaseType.INSTANCE),
							new JParameter("index"),
							new JParameter("value"),
						}, 
						VoidType.getInstance(), 
						METHOD_set,
						JArrayBaseType.INSTANCE),
				    null));
	
			builder.addInstanceMember(
				new JClassMethodMember(
					builder.getStub(),
					SystemTypeNames.MemberNames.SIZE, Accessibility.PUBLIC, false, false,
					new JMethodType(
						SystemTypeNames.MemberNames.SIZE,
						new JParameter[]{
							new JParameter("this", JArrayBaseType.INSTANCE),
						}, 
						IntType.getInstance(), 
						METHOD_size,
						JArrayBaseType.INSTANCE),
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
							new JParameter("this", JArrayBaseType.INSTANCE),
						}, 
						jit, 
						METHOD_get_iterator,
						JArrayBaseType.INSTANCE),
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
