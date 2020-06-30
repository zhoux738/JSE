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

package info.julang.memory.value;

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.IExtValue.IRefVal;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.JNullReferenceException;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value that refers to some other object values.
 * <p/>
 * A RefValue must refer to some other object value. It cannot refer to either basic value or another reference value.
 * <p/>
 * The static member {@link #NULL} is the internal representation for null literal in Julian scripts. It points at nothing.
 * 
 * @author Ming Zhou
 */
public class RefValue extends JValueBase implements IRefVal {
	
	private ObjectValue referred;
	
	private ICompoundType referredType;
	
	/**
	 * The null value in Julian runtime.
	 */
	public final static NullValue NULL = NullValue.INSTANCE;
	
	static class NullValue extends ObjectValue {

		private static NullValue INSTANCE = new NullValue();
		
		private NullValue() {
			super(null, null, false);
		}
		
		@Override
		protected void initialize(JType type, MemoryArea memory) {
			// Do nothing.
		}
		
		@Override
		public String toString(){
			return "null (jul)";
		}
		
		@Override
		public boolean isConst() {
			return true;
		}
		
		@Override
		public boolean isNull(){
			return true;
		}
		
		String toScriptString(){
			return "null";
		}
		
	}
	
	/**
	 * Used only for creating null reference
	 * @param memory
	 * @param type the type of value to be referenced
	 */
	private RefValue(MemoryArea memory, ICompoundType type) {
		super(memory, type, false);
		referred = NULL;
		referredType = type;
	}
	
	/**
	 * Create a null value used in Julian scripts. Null value is a special instance of RefValue.
	 * <p/>
	 * Always use code similar to below to determine whether a ref value is null.
	 * <pre><code>
	 * if(ref.isNull()){
	 *   System.out.println("it is a null value.");
	 * }
	 * </code></ore>
	 */
	public static RefValue makeNullRefValue(MemoryArea memory, ICompoundType type){
		return new RefValue(memory, type);
	}
	
	/**
	 * Create a ref value pointing to a given object value and using type from the same object value.
	 * 
	 * @param memory
	 * @param objValue
	 */
	public RefValue(MemoryArea memory, ObjectValue objValue) {
		super(memory, objValue.getType(), false);
		referred = objValue;
	}
	
	/**
	 * Create a ref value pointing to a given object value and overriding type using a specified one.
	 * @param memory
	 * @param objValue
	 * @param referredType
	 */
	public RefValue(MemoryArea memory, ObjectValue objValue, ICompoundType referredType) {
		this(memory, referredType);
		if(!objValue.getClassType().isDerivedFrom(referredType, true)){
			throw new JSEError(
				"Cannot create a reference value whose actual type is not derived from the specified overriding type.");
		}
		referred = objValue;
	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		referredType = (ICompoundType) type;
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.REFERENCE;
	}
	
	@Override
	public boolean isBasic() {
		return false;
	}
	
	@Override
	public boolean isNull(){
		return referred == NULL;
	}

	/**
	 * Get the type of referred value as it is <b>originally</b> declared.
	 * <p>
	 * Of particular note, this type is not necessarily the runtime type of the instance being referenced, which  
	 * is accessible through <code>{@link #getRuntimeType()}</code>. The various runtime checks applied elsewhere, 
	 * however, would guarantee that the runtime type can be derived from the declared type.
	 */
	@Override
	public JType getType() {
		return referredType;
	}
	
	/**
	 * Get the runtime type of the referred value.
	 */
	public JClassType getRuntimeType(){
		return (JClassType) referred.getType();
	}

	/**
	 * Get the actual value referred by this RefValue.
	 */
	public ObjectValue getReferredValue() {
		return referred;
	}

	@Override
	public boolean assignTo(JValue assignee) throws IllegalAssignmentException {
		if(super.assignTo(assignee)){
			return true;
		}
		
		if(assignee.getKind() != JValueKind.REFERENCE){
			throw new IllegalAssignmentException(referredType, assignee.getType());
		}
		
		RefValue assigneeVal = (RefValue) assignee;
		ICompoundType assigneeTyp = (ICompoundType) assigneeVal.getType();

		// Special case: assign a generic null value (A ref value pointing to RefValue.NULL without type)
		if(isGenericNull(this)){
			assigneeVal.referred = referred;
			//assigneeVal.setRuntimeType(null);
			return true;			
		} else if(referredType.equals(assigneeTyp)){
			// (type checking)
			// Can only assign ref a (this) to ref b if 
			//   (1) a's type is exactly that of b, or ...
			
			if(referredType == JStringType.getInstance() && referred != RefValue.NULL){
				// Special treatment for string value: create a new one.
				StringValue strRep = new StringValue(
					referred.getMemoryArea(),
					((StringValue)referred).getStringValue());
				assigneeVal.referred = strRep;
			} else {
				assigneeVal.referred = referred;
			}
			//assigneeVal.setRuntimeType(null);
			
			return true;
		} else if(referredType.isDerivedFrom(assigneeTyp, false)){
			//   (2) ... a's type is b's some subclass.
			assigneeVal.referred = referred;
			//assigneeVal.setRuntimeType(assigneeTyp);
			return false; // class down-graded
		}
		
		throw new IllegalAssignmentException(referredType, assignee.getType());
	}

	@Override
	public boolean isEqualTo(JValue another){
		// Returns true in the following cases where one value is null.
		//   1) ref(null, generic) == ref(null, generic)
		//   2) ref(null, any type) == ref(null, generic)
		//   3) ref(null, type A) == ref(null, type A)
		//   4) ref(null, type A) == ref(null, type P), where A inherits from P
		// Returns false in other cases where null is involved, for example:
		//   ref(null, type A) != ref(null, type B), where A and B are not related
		
		if(another.getKind() == JValueKind.REFERENCE){
			RefValue revAnother = (RefValue) another;
			
			boolean thisNull = isGenericNull(this) || this.isNull();
			boolean anotherNull = isGenericNull(revAnother) || revAnother.isNull();
			boolean genercicNull = isGenericNull(this) || isGenericNull(revAnother);
			if(thisNull && anotherNull && genercicNull){
				// 1) & 2)
				return true;
			} else if (thisNull ^ anotherNull){
				// if one is null but the other is not, returns false
				return false;
			}
			
			// 3) & 4)
			if(this.isNull() && revAnother.isNull()){
				if(this.referredType.isDerivedFrom(revAnother.referredType, true) ||
					revAnother.referredType.isDerivedFrom(this.referredType, true)){
					return true;
				} else {
					return false;
				}
			}
		}
		
		// Forward the call the ObjectValue, which will make a simple JVM-reference-based comparison.
		return this.getReferredValue().isEqualTo(another);
	}
	
	@Override
	public JValue deref(){
		return referred;
	}
	
	@Override
	public String toString(){
		return referred.toString();
	}
	
	@Override
	public int hashCode(){
		return referred.hashCode();
	}
	
	@Override
	public boolean equals(Object anObject){	
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			return referred.equals(av);
		}
		
		return false;
	}
	
	@Override
	public IObjectVal getReferred() {
		return referred;
	}

	/**
	 * Dereference this value, returning the referred value.
	 * 
	 * @throws JNullReferenceException if the referred is NULL.
	 * @return
	 */
	public ObjectValue dereference() {
		if(!isNull()){
			return referred;
		}
		throw new JNullReferenceException();
	}
	
	/**
	 * Dereference a given value, only if it is a reference value. If it is an object value, return as is.
	 * 
	 * @param val
	 * @throws JNullReferenceException if the referred is null.
	 * @return null if it is neither a reference value, nor an object value.
	 */
	public static ObjectValue dereference(JValue val){
		if(val.getKind() == JValueKind.REFERENCE){
			return ((RefValue) val).dereference();
		}
		
		if(val.getKind() == JValueKind.OBJECT){
			return (ObjectValue) val;
		}
		
		return null;
	}
	
	/**
	 * Dereference a given value, only if it is a reference value. If it is an object value, return as is.
	 * <p/>
	 * If it is null reference, returns {@link #NULL}.
	 * @param val
	 * @return null if it is neither a reference value, not an object value.
	 */
	public static ObjectValue tryDereference(JValue val){
		if(val.getKind() == JValueKind.REFERENCE){
			return ((RefValue) val).referred;
		}
		
		if(val.getKind() == JValueKind.OBJECT){
			return (ObjectValue) val;
		}
		
		return null;
	}

	private static boolean isGenericNull(RefValue refValue) {
		return refValue.referred == RefValue.NULL && refValue.referredType == null;
	}
	
	/**
	 * Check if the value is a null without type info.
	 * @param value
	 * @return true if the value is a null without type info.
	 */
	public static boolean isGenericNull(JValue value) {
		if(value.getKind() == JValueKind.REFERENCE){
			return isGenericNull((RefValue)value);
		}
		
		return false;
	}
	
	/**
	 * Check if the value is a null without type info, and if so cast it to a ref value.
	 * @param value
	 * @return The ref value cast from the given value.
	 */
	public static RefValue asGenericNull(JValue value) {
		if(value.getKind() == JValueKind.REFERENCE){
			RefValue rv = (RefValue) value;
			if(isGenericNull(rv)){
				return rv;
			}
		}
		
		return null;
	}
}
