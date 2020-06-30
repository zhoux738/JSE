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

import java.util.List;

import info.julang.execution.namespace.NamespacePool;
import info.julang.external.interfaces.JValueKind;
import info.julang.typesystem.IMapped;
import info.julang.typesystem.JType;
import info.julang.typesystem.PlatformType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.util.OneOrMoreList;

/*
 * IMPLEMENTATION NOTES:
 * 
 * All built-in classes which are to add Julian documentation must follow certain patterns due to the limited 
 * ability of doc-gen tool. All the platform-based methods MUST be placed in a field with an initializer that
 * creates an anonymous subclass of HostedExecutable. Apply JulianDoc both on the outermost class and each
 * field with anonymous class initializer. See JStringType and JObjectType for examples.
 */
/**
 * Represents a class type defined in Julian scripts.
 * <p/>
 * Theoretically, any class can be implemented by this class, since it contains all the information of 
 * a class: its name, its parent class ({@link #getParent()}), its members, be it static and instance scope.
 * However, for convenience of implementation, there are also several classes which inherit from this one, 
 * such as {@link info.julang.typesystem.jclass.builtin.JStringType JStringType} or 
 * {@link info.julang.typesystem.jclass.builtin.JObjectType JObjectType}. It should be noted
 * that the class hierarchy in the Java implementation has nothing particular to do with the class hierarchy
 * in Julian. For example, both JStringType and JObjectType inherit from JClassType, but in Julian, String 
 * inherits from Object. These special subclasses of JClassType are purely for easing programming.
 * 
 * @author Ming Zhou
 */
public class JClassType extends JInterfaceType implements IMapped {

	//------------ package-accessible members to allow incremental build-up ------------//
	
	JClassType parent;
	
	List<JClassConstructorMember> constructorMemberList;
	
	JClassStaticConstructorMember staticConstructor;
	
	//------------ private members holding class information ------------//
	
	private ClassMemberMap staticMembers;
	
	private ClassMemberMap instanceMembers;

	private JClassMember[] staticMemberArray;
	
	private JClassMember[] instanceMemberArray;
	
	private JClassConstructorMember[] constructorArray;
	
	/**
	 * A flag used to mark the state of initialization. As long as this field is set to true, the class
	 * is free to freeze its properties and generate some others with permanent effect. Set this flag
	 * to false for built-in types which involves multi-staged bootstrapping process and use 
	 * {@link info.julang.typesystem.jclass.builtin.IDeferredBuildable#preInitialize() preInitialize()} 
	 * to flip it back. <code>preInitialize()</code> will be called when the type is eventually sealed.
	 */
	protected boolean initialized;
	
	//----------------------- Constructors -----------------------//
	
	/**
	 * Create a new JClassType
	 * @param name the fully qualified name of this class
	 * @param parent the parent class. Default to ObjectType if null.
	 * @param members the class members 
	 */
	public JClassType(String name, JClassType parent, JClassMember[] members){
		this(name, parent, members, new JClassProperties(Accessibility.PUBLIC));
	}
	
	/**
	 * Create a new JClassType
	 * @param name the fully qualified name of this class
	 * @param parent the parent class. Default to ObjectType if null.
	 * @param members the class members 
	 * @param props the class properties
	 */
	public JClassType(String name, JClassType parent, JClassMember[] members, JClassProperties props){
		super(name, members, new JInterfaceType[0], props);
		this.parent = parent == null ? JObjectType.getInstance() : parent;
		this.initialized = true;
	}
	
	/**
	 * Create a new JClassType. Only used by Builder.
	 */
	protected JClassType(){
		this.initialized = true;
	}
	
	//----------------------- Members only allowed in a class -----------------------//
	
	/**
	 * Get class constructors.
	 * <p>
	 * This includes all constructors defined explicitly or implicitly by this class, 
	 * regardless of its accessibility.
	 *
	 * @return never null
	 */
	public JClassConstructorMember[] getClassConstructors(){
		// Note:
		// Since Julian doesn't support dynamic type modification (adding a class member), we cache 
		// the members in a separate array to save some time for later listing operations.
		if(constructorArray == null){
			if(constructorMemberList != null){
				constructorArray = new JClassConstructorMember[constructorMemberList.size()];
				constructorArray = constructorMemberList.toArray(constructorArray);
			} else {
				return new JClassConstructorMember[0];
			}
		}
		
		return constructorArray;
	}
	
	/**
	 * Get the static constructor.
	 * 
	 * @return can be null if no one is declared.
	 */
	public JClassStaticConstructorMember getClassStaticConstructor(){
		return staticConstructor;
	}

	//-------------------- IInterfaceType --------------------//
	
	@Override
	public boolean isClassType(){
		return true;
	}
	
	@Override
	public JClassMember getInstanceMemberByName(String name) {
		return getMemberByName(name, true, false, false);
	}
	
	@Override
	public JClassMember getStaticMemberByName(String name) {
		return getMemberByName(name, false, true, false);
	}
	
	// Return all overloaded method members with the specified name
	@Override
	public OneOrMoreList<JClassMethodMember> getStaticMethodMembersByName(String name) {
		ClassMemberMap memMap = getMembers(true);
		OneOrMoreList<ClassMemberLoaded> cmls = memMap.getLoadedMemberByName(name);
		return getAllMethodMembers(cmls);
	}
	
	/**
	 * Get all instance methods on visible on this class. This include all methods defined locally 
	 * and any public/protected method defined above the hierarchy.
	 * 
	 * @param name the method's name
	 */
	public OneOrMoreList<JClassMethodMember> getInstanceMethodMembersByName(String name) {
		ClassMemberMap memMap = getMembers(false);
		OneOrMoreList<ClassMemberLoaded> cmls = memMap.getLoadedMemberByName(this, name, false);
		return getAllMethodMembers(cmls);
	}
	
	@Override
	public JClassMember[] getClassStaticMembers(){
		// Note:
		// Since Julian doesn't support dynamic type modification (adding a class member), we cache 
		// the members in a separate array to save some time for later listing operations.
		if(staticMemberArray == null){
			ClassMemberMap members = getMembers(true);
			staticMemberArray = members.getClassMembers();
		}
		
		return staticMemberArray;
	}
	
	@Override
	public JClassMember[] getClassInstanceMembers(){
		// Note:
		// Since Julian doesn't support dynamic type modification (adding a class member), we cache 
		// the members in a separate array to save some time for later listing operations.
		if(instanceMemberArray == null){
			ClassMemberMap members = getMembers(false);
			instanceMemberArray = members.getClassMembers();
		}
		
		return instanceMemberArray;
	}
	
	/**
	 * Get all the members in a structural definition map.
	 * 
	 * @param isStatic true to get all the members defined as static.
	 * @return
	 */
	public ClassMemberMap getMembers(boolean isStatic){
		if(isStatic){
			if(staticMembers == null){
				ClassMemberMap cmp = new ClassMemberMap(this, isStatic);
				if (this.initialized) {
					staticMembers = cmp;
				} else {
					return cmp;
				}
			}
			return staticMembers;
		} else {
			if(instanceMembers == null){
				ClassMemberMap cmp = new ClassMemberMap(this, isStatic);
				if (this.initialized) {
					instanceMembers = cmp;
				} else {
					return cmp;
				}
			}
			return instanceMembers;
		}
	}
	
	/*
	 * An example-based algorithm description (not Javadoc):
	 * 
	 * Assume we have classes C0[1-3] and interfaces I[1-3][1-2] which form a hierarchy like below:
	 * 
	 *   C01 : I11, I12
	 *   /|\
	 *    | 
	 *   C02 : I21, I22
	 *   /|\
	 *    |
	 *   C03 : I31, I32
	 * 
	 * Now when we call C03.isDerivedFrom(T), this algorithm would follow these steps:
	 * <1> check if C03 is T => true to terminate
	 *   <2> check if C02 is T => true to terminate
	 *     <3> check if C01 is T => true to terminate
	 *     <3> check if I11, I12 is T => true to terminate
	 *   <2> check if I21, I22 is T => true to terminate
	 * <1> check if I11, I12 is T => true to terminate
	 */
	@Override
	public boolean isDerivedFrom(ICompoundType potentialParent, boolean includeIdentical){		
		JClassType thisType = this;
		
		// 1) Check if it is the same type
		if(thisType.equals(potentialParent)){
			if(includeIdentical){
				return true;
			} else {
				return false;
			}
		}
		
		// 2) Recursively check the parent
		thisType = thisType.getParent();
		if (thisType != null){
			if (thisType.isDerivedFrom(potentialParent, true)){
				return true;
			}
		}
		
		// 3) Check all the interfaces *directly* implemented/extended by this type.
		return super.isDerivedFrom(potentialParent, false);
	}
	
	@Override
	public boolean canDerive(ICompoundType potentialChild, boolean includeIdentical){
		boolean result = potentialChild.isDerivedFrom(this, includeIdentical);
		return result || super.isDerivedFrom(this, false);
	}

	@Override
	public NamespacePool getNamespacePool() {
		return nsPool;
	}
	
	@Override
	public JClassType getParent(){
		return parent;
	}
	
	/**
	 * Get the corresponding value's kind for this type.
	 * <p/>
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
	
	@Override
	public Convertibility getConvertibilityTo(JType another){
		if (another instanceof PlatformType){
			PlatformType pt = (PlatformType)another;
			if (mappingTarget != null) {
				Convertibility cvt = pt.canBeAssignedBy(mappingTarget);
				if (cvt != null) {
					return cvt;
				}
			}
		}
		
		return super.getConvertibilityTo(another);
	}
	
	/**
	 * If this returns true, part of the type is to be built after the initialization. This is only applicable to 
	 * built-in types.The caveat is that if this returns, this instance can be cast to 
	 * {@link info.julang.typesystem.jclass.builtin.IDeferredBuildable IDeferredBuildable}. 
	 */
	public boolean deferBuild(){
		return false;
	}
	
	/**
	 * Try to get a class member by name.
	 * <p/>
	 * If the member is overloaded, returns the first one defined in the class.
	 * 
	 * @param name
	 * @param checkInst include instance members
	 * @param checkStatic include static members
	 * @param checkCtor include constructors
	 * @return null if not found
	 */
	private JClassMember getMemberByName(String name, boolean checkInst, boolean checkStatic, boolean checkCtor) {
		if(checkInst){
			ClassMemberMap memMap = getMembers(false);
			OneOrMoreList<ClassMemberLoaded> cmls = memMap.getLoadedMemberByName(name);
			if(cmls != null){
				// Return the very first member.
				if(cmls.size() > 0){
					return cmls.getFirst().getClassMember();
				}
			}		
		}
		
		if(checkStatic){
			ClassMemberMap memMap = getMembers(true);
			OneOrMoreList<ClassMemberLoaded> cmls = memMap.getLoadedMemberByName(name);
			if(cmls != null){
				// Return the very first member.
				if(cmls.size() > 0){
					return cmls.getFirst().getClassMember();
				}
			}
		}
		
		if(checkCtor){
			JClassMember[] members = getClassConstructors();
			for(JClassMember member : members){
				if(member.getName().equals(name)){
					return member;
				}
			}
		}
		
		return null;
	}
	
	private OneOrMoreList<JClassMethodMember> getAllMethodMembers(OneOrMoreList<ClassMemberLoaded> cmls){
		OneOrMoreList<JClassMethodMember> res = new OneOrMoreList<JClassMethodMember>();
		if(cmls != null){
			for(ClassMemberLoaded cml : cmls){
				JClassMember jcm = cml.getClassMember();
				if (jcm.getMemberType() == MemberType.METHOD){
					res.add((JClassMethodMember)jcm);
				}
			}
		}
		
		return res;
	}

}
