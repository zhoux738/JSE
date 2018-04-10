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
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedExecutable;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectArrayValue;
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
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JClassTypeBuilder;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.TypeBootstrapper;

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
+ "\n * Since Julian doesn't support jagged arrays, the user should use extra caution when using an initializer. If the"
+ "\n * lengths at the same dimension are not consistent, it would incur a runtime exception."
+ "\n * [code]"
+ "\n * int[] ia = new int[]{10, 20};"
+ "\n * string[][] saa = new string[][]{new string[]{\"a\", \"b\"}, new string[]{\"c\", \"d\"}};"
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
+ "\n * Array is iteratible. This mean one can use foreach grammar on an array."
+ "\n * [code]"
+ "\n * for(int i : ia) {"
+ "\n *   ..."
+ "\n * }"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Array's length is immutable and fixed during initialization. To use a scalable structure consider [List](System.Collection.List)."
+ "\n * "
+ "\n * For more detailed description on Array, see [Julian Tutorial](tutorial: array)."
+ "\n */"
)
@JulianFieldMembersDoc(
	@JulianFieldMemberDoc(
		name = "length",
		summary = "/* The length of this array. Note for multi-dimensional array this refers to the length of the first dimension. */"
	)
)
public class JArrayBaseType extends JClassType {

	public static final FQName FQNAME = new FQName("Array");
	
	private static final String METHOD_NAME_COPY = "copy";
	
	private static JArrayBaseType INSTANCE;

	/**
	 * Used only by concrete array types.
	 * @param name
	 */
	protected JArrayBaseType(String name, JClassType parentType) {
		super(name, parentType == null ? JArrayBaseType.getInstance() : parentType, new JClassMember[0]);
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
			
			//Method
			//TODO: add the following methods
			/*
			 * hashcode
			 * getType
			 */
		}
		
		@Override
		public void boostrapItself(JClassTypeBuilder builder){
			if(JArrayBaseType.INSTANCE == null){
				JArrayBaseType.INSTANCE = (JArrayBaseType) builder.build(true);
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
				  "The total length to copy over"
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
}
