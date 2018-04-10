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
import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an interface type defined in Julian scripts.
 * <p/>
 * This class defines all properties that are required and/or allowed in the definition of an interface.
 * This includes name, interfaces to extend from, static members, and abstract instance methods.
 * 
 * @author Ming Zhou
 */
public class JInterfaceType implements ICompoundType {

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
	
	//------------ private members holding interface information ------------//
	
	private JInterfaceType[] interfaces;
	
	private InterfaceMemberMap interfaceMembers;
	
	private JClassMethodMember[] interfaceMemberArray;
	
	private JClassInitializerMember[] instanceInitializerArray;
	
	private JClassInitializerMember[] staticInitializerArray;

	private JAnnotation[] annotationArray;
	
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
	public boolean isClassType(){
		return false;
	}
	
	@Override
	public JClassProperties getClassProperties(){
		return properties;
	}
	
	@Override
	public JClassInitializerMember[] getClassInitializers(boolean isStatic){
		// Note:
		// Since Julian doesn't support dynamic type modification (adding a class member), we cache 
		// the members in a separate array to save some time for later listing operations.
		
		if(initializerMemberList != null){
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
		} else {
			return new JClassInitializerMember[0];
		}
		
		return isStatic ? staticInitializerArray : instanceInitializerArray;
	}
	
	@Override
	public JClassMember getInstanceMemberByName(String name) {
		if (interfaceMembers == null) {
			interfaceMembers = getInterfaceMembers();
		}
		
		OneOrMoreList<InstanceMemberLoaded> list = interfaceMembers.getMembersByName(name);
		
		return list != null ? list.getFirst().getMember() : null;
	}
	
	@Override
	public JClassMember[] getClassInstanceMembers(){
		if (interfaceMembers == null) {
			interfaceMembers = getInterfaceMembers();
			interfaceMemberArray = interfaceMembers.getAllMembers();
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
	 * <br/><br/>For example, if we have<pre>
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
	
	@Override
	public JInterfaceType[] getInterfaces(){
		if(interfaces == null){
			if (interfaceList != null){
				interfaces = new JInterfaceType[interfaceList.size()];
				interfaceList.toArray(interfaces);
			} else {
				interfaces = new JInterfaceType[0];
			}
		}
		
		return interfaces;
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
	
	//-------------------- IAnnotated --------------------//
	
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
