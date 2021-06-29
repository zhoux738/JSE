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

package info.julang.typesystem.jclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import info.julang.execution.namespace.NamespacePool;
import info.julang.external.interfaces.JValueKind;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.util.OSTool;
import info.julang.util.OneOrMoreList;

/**
 * Represents an interface type defined in Julian scripts.
 * <p>
 * This class defines all properties that are required and/or allowed in the definition of an interface.
 * This includes name, interfaces to extend from, static members (albeit not used), and abstract instance methods.
 * 
 * @author Ming Zhou
 */
public class JInterfaceType implements ICompoundType {

    // IMPLEMENTATION NOTES
    // 
    // Type loading is locked down to single thread, so in general the build-up process 
    // happens within a single thread. However, methods marked with [LAZY LOADING] may not 
    // be immediately called during type loading and therefore must be protected against  
    // race condition.
    
	//------------ package-accessible members to allow incremental build-up ------------//
	
	String name;

	String moduleName;
	
	JClassProperties properties;
	
	Map<String, OneOrMoreList<JClassMember>> staticMemberMap;
	
	Map<String, OneOrMoreList<JClassMember>> instanceMemberMap;
	
	List<JClassInitializerMember> initializerMemberList;
	
	NamespacePool nsPool;
	
	List<JAnnotation> annotationList;
	
	List<JInterfaceType> interfaceList;
	
	Class<?> mappingTarget;
	
	OneOrMoreList<JClassType> extensions;
	
	//------------ private members holding interface information ------------//
	
	private int stamp;
	
	private JInterfaceType[] interfaces;
	
	private JInterfaceType[] ancestors;
	
	private InterfaceMemberMap interfaceMembers;
	
	private JClassMethodMember[] interfaceMemberArray;
	
	private JClassInitializerMember[] instanceInitializerArray;
	
	private JClassInitializerMember[] staticInitializerArray;

	private JAnnotation[] annotationArray;
	
	private JClassType[] allExtensions;
	
	private Map<String, Boolean> ancestorSet; // key = FQN of type, value = true (class), false (interface)
	
	//----------------------- Constructors -----------------------//
	
	/**
	 * Create a new JInterfaceType
	 * @param name the fully qualified name of this class
	 * @param members the class members 
	 * @param interfaces the interfaces this interface extends from
	 * @param props the class properties
	 */
	public JInterfaceType(
		String name, JClassMember[] members, JInterfaceType[] interfaces, JClassProperties props){
		this.name = name;
		this.interfaces = interfaces != null ? interfaces : new JInterfaceType[0];
		
		if(members != null){
			for(JClassMember member : members){
				if(member.isStatic()){
					if(staticMemberMap==null){
						staticMemberMap = new HashMap<String, OneOrMoreList<JClassMember>>();
					}
					addOverloadedMember(staticMemberMap, name, member);
				} else {
					if(instanceMemberMap==null){
						instanceMemberMap = new HashMap<String, OneOrMoreList<JClassMember>>();
					}
					addOverloadedMember(instanceMemberMap, name, member);
				}
			}		
		}
		
		properties = props;
	}
	
	/**
	 * Create a new JInterfaceType. Only used by Builder.
	 */
	protected JInterfaceType(){
		
	}

	//-------------------- IInterfaceType --------------------//
	
	@Override
	public int getStamp() {
		return stamp;
	}
	
	@Override
	public boolean isClassType(){
		return false;
	}
	
	@Override
	public JClassProperties getClassProperties(){
		return properties;
	}
	
	// [LAZY LOADING]
	@Override
	public JClassInitializerMember[] getClassInitializers(boolean isStatic){
		// Note:
		// Since Julian doesn't support dynamic type modification (adding a class member), we cache 
		// the members in a separate array to save some time for later listing operations.
		
	    if(initializerMemberList == null){
	        return new JClassInitializerMember[0];
	    }
	    
		if(staticInitializerArray == null){
            synchronized(this){
                if(staticInitializerArray == null){
                    List<JClassInitializerMember> instInitMembers = new ArrayList<JClassInitializerMember>();
                    List<JClassInitializerMember> staticInitMembers = new ArrayList<JClassInitializerMember>();
                    
                    for(JClassInitializerMember init : initializerMemberList){
                        if(init.isStatic()){
                            staticInitMembers.add(init);
                        } else {
                            instInitMembers.add(init);
                        }
                    }
                    
                    staticInitializerArray = new JClassInitializerMember[staticInitMembers.size()];
                    staticInitializerArray = staticInitMembers.toArray(staticInitializerArray);
                    instanceInitializerArray = new JClassInitializerMember[instInitMembers.size()];
                    instanceInitializerArray = instInitMembers.toArray(instanceInitializerArray);   
                }
            }
		}
		
		return isStatic ? staticInitializerArray : instanceInitializerArray;
	}

	// [LAZY LOADING]
	@Override
	public JClassMember getInstanceMemberByName(String name) {
		if (interfaceMembers == null) {
            synchronized(this){
                if(interfaceMembers == null){
                    interfaceMembers = getInterfaceMembers();
                }
            }
		}
		
		OneOrMoreList<InstanceMemberLoaded> list = interfaceMembers.getMembersByName(name);
		
		return list != null ? list.getFirst().getMember() : null;
	}
	
	// [LAZY LOADING]
	@Override
	public JClassMember[] getClassInstanceMembers(){
		if (interfaceMembers == null) {
            synchronized(this){
                if(interfaceMembers == null){
                    interfaceMembers = getInterfaceMembers();
                    interfaceMemberArray = interfaceMembers.getAllMembers();
                }
            }
		}
		
		return interfaceMemberArray;
	}
	
	// no static member on interface
	@Override
	public JClassMember getStaticMemberByName(String name) {
		return null;
	}

	// no static member on interface
	@Override
	public JClassMember[] getClassStaticMembers(){
		return new JClassMember[0];
	}

	// no static member on interface
	@Override
	public OneOrMoreList<JClassMethodMember> getStaticMethodMembersByName(String name) {
		return new OneOrMoreList<JClassMethodMember>();
	}
	
	@Override
	public JClassType getParent(){
		return JObjectType.getInstance();
	}
	
	@Override
	public Class<?> getMappedPlatformClass(){
		return mappingTarget;
	}
	
	/**
	 * An interface is derived from another if any interfaces this one extends from is the target interface.
	 * <br><br>For example, if we have<pre>
	 *  I1 : I2, I3;
	 *  I2 : I4;
	 *  I3 : I5, I6;
	 *  I6 : I7
	 *  
	 * Then I1, I3, I5, I6 are all derived from I7.</pre>
	 */
	@Override
	public boolean isDerivedFrom(ICompoundType potentialParent, boolean includeIdentical){		
		JInterfaceType thisType = this;
		if(thisType.equals(potentialParent)){
			if(includeIdentical){
				return true;
			} else {
				return false;
			}
		}
		
		if (getInterfaces()!=null){
			for(JInterfaceType jit : interfaces){
				if(jit.isDerivedFrom(potentialParent, true)){
					return true;
				}
			}			
		}
		
		return false;
	}
	
	@Override
	public boolean canDerive(ICompoundType potentialChild, boolean includeIdentical){
		return potentialChild.isDerivedFrom(this, includeIdentical);
	}

	@Override
	public NamespacePool getNamespacePool() {
		return nsPool;
	}
	
	// [LAZY LOADING]
	@Override
	public JInterfaceType[] getInterfaces(){
		if(interfaces == null){
		    synchronized(this){
		        if(interfaces == null){
		            if (interfaceList != null){
		                interfaces = new JInterfaceType[interfaceList.size()];
		                interfaceList.toArray(interfaces);
		            } else {
		                interfaces = new JInterfaceType[0];
		            }
		        }
		    }
		}
		
		return interfaces;
	}
	
	/**
	 * Check whether a type with name = <code>fullTypeName</code> is the ancestor of this one 
	 * (a class or interface appearing at an upper tier of the type hierarchy).
	 * 
	 * @param fullTypeName
	 * @param isClassOrInterface true to search ancestor class, false ancestor interface
	 * @return true if the given type is an ancestor. 
	 */
	public boolean hasAncestor(String fullTypeName, boolean isClassOrInterface) {
		if (ancestorSet == null) {
		    synchronized(this){
		        if(ancestorSet == null){
					JInterfaceType[] ancTyps = initAncestors();
		        	ancestorSet = new HashMap<>();
			        for (int i = 1; i < ancTyps.length; i++) { // Skip the 1st type, which is itself.
			        	JInterfaceType typ = ancTyps[i];
			        	ancestorSet.put(typ.getName(), typ.isClassType());
			        }
		        }
		    }
		}
		
		Boolean res = ancestorSet.get(fullTypeName);
		if (res == null) {
			return false;
		} else {
			return !(isClassOrInterface ^ res.booleanValue());
		}
	}

	@Override
	public OneOrMoreList<JClassType> getExtensionClasses() {
	    return extensions == null ? new OneOrMoreList<JClassType>() : extensions;
	}

	// [LAZY LOADING] (But can be invalidated - TODO)
	public JClassType[] getAllExtensionClasses() {
		if (allExtensions == null) {
		    synchronized(this){
		        if (allExtensions == null) {
		    		JClassType[] arr = null;
		    		JInterfaceType[] ancs = getAncestors(true);
		    		if (ancs.length > 0) {
			    		TypePrioritySorter sorter = new TypePrioritySorter();
		        		for (int rank = 0; rank < ancs.length; rank++) {
		        			JInterfaceType anc = ancs[rank];
		        			OneOrMoreList<JClassType> exts = anc.getExtensionClasses();
		        			if (exts.size() > 0) {
		        				int order = 0;
		        	    		for(JClassType ext : exts) {
		    	    				TypePriority ep = sorter.next(ext);
		    	    				if (!sorter.contains(ep)) {
		    	    					sorter.add(ep, rank, order);
		    	    					order++;
		    	    				}
		        	    		}
		        			}
		        		}
		        		
		        		arr = sorter.toArray(JClassType.class, true);
		    		} else {
		    			arr = new JClassType[0];
		    		}
		    		
			        allExtensions = arr;
		        }
		    }
		}
		
		return allExtensions;
	}
	
	/*
	 * DESIGN NOTES: Extension Method
	 * 
	 * 1. Extension class (extension for short) can be installed to a class or interface
	 * 2. A class or interface will have access to all extensions installed to it or any of its ancestors.
	 * 3. Resolving to extension method only occur when the following conditions are met
	 *    (1) There is no local or inherited member of the same name. This means extension methods do not
	 *        participate in the overloading with member methods, although they can overload among 
	 *        themselves.
	 *    (2) The addressing syntax is used: "inst.exfun();". This means just calling "exfun()" from an 
	 *        instance method won't work.
	 * 4. Extension methods cannot be exposed as a handle. Doing "var f = inst.exfun" will only return the
	 *    static method. 
	 */
	
	/**
	 * Get all the ancestor types. This includes the parent type and interface type, as well as the parent 
	 * type of interfaces thereof, all the way up to the root type Object. The array is deduplicated, and 
	 * the order is based on the combination of logical proximity and lexical order:
	 * <p>
	 * 1. Logical proximity: the hops between the ancestor type and this type. For example, given the following:
	 * <pre><code>
	 * class A : B;
	 * class B : C;
	 * class C : D;
	 * class B : I;
	 * class D : I;
	 * </code></pre>
	 * Then B is 1 hop from A, C and I 2 and D 3. Note I also appears as the interface of D, but it got eliminated
	 * from the result array.
	 * <p>
	 * 2. Lexical order: the position a type appears in the inheritance syntax. For example, given the following:
	 * <pre><code>
	 * class A : I1, B, I2; // A and B are class, I1 and I2 interface
	 * </code></pre>
	 * Then the order would be B, I1 and I2. The parent type always appears at the first no matter what position it
	 * shows in the code. The interfaces types, however, do follow their text order.
	 * @param includeThis If true, this type will appear as the first element of the returned array.
	 * @return An array of this type's ancestor types, deduplicated and ordered based on proximity and text order.
	 * Never null, but can be empty.
	 */
	public JInterfaceType[] getAncestors(boolean includeThis) {
		if (ancestors == null) {
		    synchronized(this){
	        	ancestors = initAncestors();
		    }
		}
		
		if (includeThis) {
			return ancestors;
		} else {
			JInterfaceType[] ancestorsWithoutSelf = new JInterfaceType[ancestors.length - 1];
			System.arraycopy(ancestors, 1, ancestorsWithoutSelf, 0, ancestorsWithoutSelf.length);
			return ancestorsWithoutSelf;
		}
	}
	
	/**
	 * Not synchronized.
	 */
	private JInterfaceType[] initAncestors() {
        if(ancestors == null){
        	TypePrioritySorter sorter = new TypePrioritySorter();
    		Stack<JInterfaceType> stack = new Stack<JInterfaceType>();
    		stack.push(this);
    		
    		populateAncestors(sorter, stack, 0);

        	ancestors = sorter.toArray(JInterfaceType.class, true);
        }

    	return ancestors;
	}

	private void populateAncestors(TypePrioritySorter sorter, Stack<JInterfaceType> stack, int rank) {
		int order = 0;
		List<JInterfaceType> toAdd = null;
		
		// First, pop each type from the stack and add it into the resultant map, if not added yet.
		// These types come out in the order of priority.
		while (!stack.isEmpty()) {
			JInterfaceType ptyp = stack.pop();
			TypePriority ep = sorter.next(ptyp);
			if (!sorter.contains(ep)) {
				if (toAdd == null) {
					toAdd = new ArrayList<JInterfaceType>();
				}
				toAdd.add(ptyp);
				sorter.add(ep, rank, order);
				order++;
			}
		}
		
		// Then, if any type has been added, find out its parent/interfaces, push them into the stack 
		// in the reversed order of priority, and recurse.
		if (toAdd != null) {
			for (int i = toAdd.size() - 1; i >= 0; i--) {
				JInterfaceType ptyp = toAdd.get(i);
				JInterfaceType[] infs = ptyp.getInterfaces();
				for (int j = infs.length - 1; j >= 0; j--) {
					stack.push(infs[j]);
				}
				
				JClassType parent = ptyp.getParent();
				if (parent != null) {
					stack.push(parent);
				}
			}
		}
		
		if (!stack.empty()) {
			populateAncestors(sorter, stack, rank + 1);
		}
	}

	@Override
	public String getModuleName() {
		return moduleName;
	}
	
	//-------------------- JType --------------------//
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.CLASS;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Convertibility getConvertibilityTo(JType type) {
		if(this == type){
			return Convertibility.EQUIVALENT;
		}
		
		if(type == AnyType.getInstance()){
			return Convertibility.DOWNGRADED;
		}
		
		if(type.isObject()){
			if(this.isDerivedFrom((ICompoundType)type, false)){
				return Convertibility.DOWNGRADED;
			}
		}
		
		return Convertibility.UNCONVERTIBLE;
	}

	@Override
	public boolean isBasic() {
		return false;
	}

	@Override
	public boolean isObject() {
		return true;
	}

	@Override
	public boolean isBuiltIn() {
		return false;
	}
	
	@Override
	public int getSize() {
		int sz = 0;
		JClassMember[] mems = this.getClassInstanceMembers();
		for (JClassMember mem : mems) {
			if (mem.getMemberType() == MemberType.FIELD) {
				sz += mem.getType().getSize();
			} else if (mem.getMemberType() == MemberType.METHOD) {
				sz += OSTool.WordSize;
			}
		}
		
		return sz;
	}
	
	//-------------------- IAnnotated --------------------//
	
	// [LAZY LOADING]
	@Override
	public JAnnotation[] getAnnotations() {
		if(annotationArray == null){
			if(annotationList != null){
				annotationArray = new JAnnotation[annotationList.size()];
				annotationArray = annotationList.toArray(annotationArray);
			} else {
				annotationArray = new JAnnotation[0];
			}
		}
		
		return annotationArray;
	}
	
	//-------------------- Object --------------------//
	
	@Override
	public boolean equals(Object obj){
		if(obj==null || !(obj instanceof ICompoundType)){
			return false;
		}
		
		ICompoundType type = (ICompoundType) obj;
		
		return name.equals(type.getName());
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	/**
	 * Get the corresponding value's kind for this type.
	 * <p>
	 * Will only check special class types which are directly supported by Julian language for type declaration, 
	 * such as Enum or Attribute. Will not check if it is an Array, String or Function, in all of which cases 
	 * simply returns {@link JValueKind#OBJECT}.
	 * 
	 * @return Can be {@link JValueKind#ENUM}, {@link JValueKind#ATTRIBUTE} or {@link JValueKind#OBJECT}.
	 */
	public JValueKind getValueKindForBuiltInType(){
		if(JEnumType.isEnumType(this)){
			return JValueKind.ENUM;
		} else if(JAttributeType.isAttributeType(this)){
			return JValueKind.ATTRIBUTE;
		} else {
			return JValueKind.OBJECT;
		}
	}
	
	protected void setNamespacePool(NamespacePool nsPool) {
		this.nsPool = nsPool;
	}
	
	protected void addOverloadedMember(
		Map<String, OneOrMoreList<JClassMember>> map, String name, JClassMember member){
		OneOrMoreList<JClassMember> overloads = map.get(name);
		if(overloads == null){
			overloads = new OneOrMoreList<JClassMember>(member);
			map.put(name, overloads);
		} else {
			overloads.add(member);
		}
	}
	
	protected InterfaceMemberMap getInterfaceMembers(){
		return new InterfaceMemberMap(this);
	}
}
