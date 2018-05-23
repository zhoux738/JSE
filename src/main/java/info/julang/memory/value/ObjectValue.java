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
import info.julang.external.interfaces.IExtValue.IObjectVal;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.indexable.ObjectIndexable;
import info.julang.memory.value.iterable.IIterator;
import info.julang.memory.value.iterable.ObjectIterable;
import info.julang.memory.value.iterable.ObjectIterator;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.List;

/**
 * A value holding per-instance data, i.e. non-static members, of class type.
 * 
 * @author Ming Zhou
 */
public class ObjectValue extends JValueBase implements IObjectVal {
	
	protected JType type;
	
	protected ObjectMemberStorage members;
	
	public ObjectValue(MemoryArea memory, JType type, boolean delayAllocation) {
		super(memory, type, delayAllocation);
	}

	@Override
	protected void initialize(JType type, MemoryArea memory) {
		initializeMembers(type, memory, true);
	}
	
	protected JClassType convertToClassType(JType type){
		if(type == null || type.getKind() != JTypeKind.CLASS){
			throw new JSEError("Cannot initialize an object that has no class.", this.getClass());
		}
		this.type = type;
		JClassType classTyp = (JClassType) type;
		return classTyp;
	}
	
	/**
	 * Initialize all the instance fields.
	 * 
	 * @param type
	 * @param memory
	 * @param addMethods true to add method members
	 */
	protected void initializeMembers(JType type, MemoryArea memory, boolean addMethods){	
		if(type == null || type.getKind() != JTypeKind.CLASS){
			throw new JSEError("Cannot initialize an object that has no class.", this.getClass());
		}
		this.type = type;
		JClassType classTyp = (JClassType) type;
		
		// Initialize using ClassMemberMap
		members = new ObjectMemberStorage(
			memory, 
			classTyp, 
			this,               // instance members
			addMethods,         // include method members
			shouldSealConst()); // seal the const value?
	}
	
	protected boolean shouldSealConst(){
		return true;
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.OBJECT;
	}
	
	@Override
	public boolean isBasic() {
		return false;
	}
	
	@Override
	public boolean isNull(){
		return false;
	}

	@Override
	public JType getType() {
		return type;
	}
	
	public JClassType getClassType(){
		return (JClassType)type;
	}
	
	/**
	 * Get the value of a member specified by the given name. If there is more than one member matching
	 * the name, only the first one, in the declaration order, will be returned.
	 * <p>
	 * To know all the available names, use {@link JClassType#getClassInstanceMembers()} or 
	 * {@link JClassType#getMembers}. To retrieve all overloaded members of the given name, call
	 * {@link #getMethodMemberValues(String)} instead.
	 * 
	 * @param name
	 * @return null if the named member doesn't exist.
	 */
	public JValue getMemberValue(String name){
		ObjectMember om = null;
		OneOrMoreList<ObjectMember> oms = getMemberValueByClass(name, null, true);
		if(oms != null && oms.size() > 0){
			om = oms.getFirst();
		}
		
		return om != null ? om.getValue() : null;
	}
	
	/**
	 * Get the values for each overloaded method member by the given name.
	 * 
	 * @param name
	 * @return
	 */
	public MethodValue[] getMethodMemberValues(String name){
		OneOrMoreList<ObjectMember> oms = getMemberValueByClass(name, null, true);
		List<MethodValue> list = new ArrayList<MethodValue>();
		if(oms != null){
			for(ObjectMember om : oms){
				JValue jval = om.getValue().deref();
				if (jval instanceof MethodValue){
					list.add((MethodValue)jval);
				}
			}
		}
		
		MethodValue[] arr = new MethodValue[list.size()];
		list.toArray(arr);
		
		return arr;
	}
	
	/**
	 * Get the value of a member specified by the given name and 
	 * defined at a class equal to or above the given one in the hierarchy.
	 * <p/>
	 * If we have class C : F : G, all of them defining a method named x, and 
	 * an instance c of type C, then calling this method against c with name = 
	 * x and typ = C (or null) would end up retrieving C.x; but if calling by 
	 * (x, F) we will get F.x; if (x, G), G.x.
	 * <p/>
	 * If F doesn't define x, calling with (x, F) or (x, G) will both get G.x.
	 * 
	 * @param name
	 * @param typ
	 * @param includeNonvisible If true, include all private members from ancestor classes also
	 * @return
	 */
	public OneOrMoreList<ObjectMember> getMemberValueByClass(String name, ICompoundType typ, boolean includeNonvisible){
		// If we filter out the private members from ancestors, must first determine the current type.
		if (!includeNonvisible && typ == null) {
			typ = this.getClassType();
		}
		return members.getMemberByName(name, typ, false);
	}

	/**
	 * Allowed: assign to a reference value.
	 */
	@Override
	public boolean assignTo(JValue assignee) {
		if(super.assignTo(assignee)){
			return true;
		}
		
		// The only scenario where we can assign an object to another is that the assignee is a reference value.
		// In that case, we create a temporary reference value pointing at this value and assign it to the target.
		if(assignee.getKind() == JValueKind.REFERENCE){
			RefValue refThis = TempValueFactory.createTempRefValue(this);
			return refThis.assignTo(assignee);
		}
		
		if(assignee.isBasic()){
			throw new IllegalAssignmentException(type, assignee.getType());
		}
		
		// This should never happen.
		throw new JSEError("Attempted to assign to an object value.");
	}
	
	@Override
	public boolean isEqualTo(JValue another){
		JType anotherType = another.getType();
		
		if(RefValue.isGenericNull(another)){
			return this == RefValue.NULL;
		}
		
		if(anotherType.getKind() == JTypeKind.CLASS){
			if(another.getKind() == JValueKind.REFERENCE){
				// if the other value is a reference, dereference it and call this method again.
				RefValue val = (RefValue) another;
				return this.isEqualTo(val.getReferredValue());
			} else {
				// otherwise, simply compare by JVM-reference.
				ObjectValue val = (ObjectValue) another;
				return compareToDeref(val);	
			}
		}
		
		return false;
	}
	
	protected boolean compareToDeref(JValue anotherValue){
		return this == anotherValue;
	}
	
	/**
	 * Get the built-in value kind. This will return a value that is not {@link JValueKind#OBJECT}
	 * only if the value is of a built-in type of Julian Script Language, such as 
	 * {@link info.julang.typesystem.jclass.builtin.JStringType String} or 
	 * {@link info.julang.typesystem.jclass.builtin.JFunctionType Function}. 
	 * @return
	 */
	public JValueKind getBuiltInValueKind(){
		return JValueKind.OBJECT;
	}
	
	@Override
	public IIndexable asIndexer(){
		String fname = type.getName();
		if (hasInterface("System.Util.IIndexable")){
			// If a user-defined class implements System.Util.Indexer, convert it to ObjectIndexable			
			ObjectIndexable oi = new ObjectIndexable(this, fname);
			return oi;
		}
		
		return null;
	}
	
	@Override
	public IIterator asIterator(){
		if (hasInterface("System.Util.IIterable")){
			String fname = type.getName();
			ObjectIterable oie = new ObjectIterable(this, fname);
			return oie.getIterator();
		}
		
		if (hasInterface("System.Util.IIterator")){
			String fname = type.getName();
			ObjectIterator oi = new ObjectIterator(this, fname);
			return oi;
		}
		
		return null;
	}
	
	private boolean hasInterface(String interfaceName){
		if (type.isObject()) {
			JClassType jct = (JClassType)type;
			JInterfaceType[] intfs = jct.getInterfaces();
			for(JInterfaceType intf : intfs) {
				if (interfaceName.equals(intf.getName())){
					return true;
				}
			}
		}
		
		return false;
	}

}
