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

import info.julang.interpretation.RuntimeCheckException;
import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A builder used to create a class type step by step.
 */
public class JClassTypeBuilder extends JInterfaceTypeBuilder {
	
	private boolean skipSanityCheck;
	
	/** Just a more concrete reference to compoundType in the parent type. */
	private JClassType classType;
	
	private JClassConstructorMember defaultCtor;
	
	/**
	 * Create a class type builder that can build up a class type step by step.
	 * @param name the full name of this class.
	 * @param classTypePrototype the prototype, which contains no members yet and to be updated by this builder.
	 * @param skipSanityCheck skip sanity checks during the process of building. This should be specified as
	 * true only for bootstrapping built in types, in order to avoid triggering the loading and sealing of 
	 * class members.
	 */
	public JClassTypeBuilder(String name, JClassType classTypePrototype, boolean skipSanityCheck){
		super(name, classTypePrototype);
		this.skipSanityCheck = skipSanityCheck;
		this.classType = classTypePrototype;
	}

	//--------------------------- ICompoundTypeBuilder ---------------------------//
	
	@Override
	public boolean isClassType(){
		return true;
	}
	
	@Override
	public JClassType getStub(){
		return classType;
	}
	
	@Override
	public Map<String, OneOrMoreList<JClassMember>> getDeclaredInstanceMembers() {
		return classType.instanceMemberMap;
	}
	
	@Override
	public Map<String, OneOrMoreList<JClassMember>> getDeclaredStaticMembers() {
		return classType.staticMemberMap;
	}
	
	@Override
	public void addStaticMember(JClassMember member){
		checkSealed();
		
		if(classType.staticMemberMap==null){
			classType.staticMemberMap = new HashMap<String, OneOrMoreList<JClassMember>>();
		}
		
		String name = member.getName();
		Map<String, OneOrMoreList<JClassMember>> staticMembers = classType.staticMemberMap;
		
		addOverloadedMember(name, member, staticMembers);
	}
	
	@Override
	public void addInstanceMember(JClassMember member){
		checkSealed();
		
		if(classType.instanceMemberMap==null){
			classType.instanceMemberMap = new HashMap<String, OneOrMoreList<JClassMember>>();
		}
		
		String name = member.getName();
		Map<String, OneOrMoreList<JClassMember>> instMembers = classType.instanceMemberMap;
		JClassType parent = classType.parent;
		// Sanity checks: a member of ancestors is incompatible with this one:
		if(parent != null && !skipSanityCheck){
			// Skipping sanity check is very important here in that it will not trigger the loading of class members since
			// at this moment the members are not fully added yet .
			
			// 1) If loading built-in classes we skip sanity check altogether.
			// 2) If loading user-defined classes, perform this check only for a type whose parent type is sealed or fully parsed.
			//    This is because at the moment we perform the check the parent type may be still being parsed, so such checking
			//    makes no sense at all. Plus getInstanceMemberByName(name) has the side effect of freezing the member array.
			
			boolean performCheck = false;
			if (parent instanceof IDefinedType){
				IDefinedType idt = (IDefinedType)parent;
				ICompoundTypeBuilder builder = idt.getBuilder();
				performCheck = builder.isSealed() || builder.isParsed();
			}

			if (performCheck){
				OneOrMoreList<ClassMemberLoaded> cmls = parent.getMembers(false).getLoadedMemberByName(name);
				for (ClassMemberLoaded cml : cmls) {
					JClassMember pmember = cml.getClassMember();
					boolean visible = pmember.getAccessibility().isSubclassVisible();
					// 2.1) the member is non-private and yet it has a different type than this one
					if(visible){
						MemberType localMemTyp = member.getMemberType();
						MemberType parentMemTyp = pmember.getMemberType();
						if(localMemTyp != parentMemTyp){
							throw new RuntimeCheckException(
								"A member of incompatible type with name \"" + name + 
								"\" is already defined in the parent class of " + classType.getName());
						}
						
						if(localMemTyp == MemberType.FIELD) {
							throw new RuntimeCheckException(
								"A non-private field member of with name \"" + name + 
								"\" is already defined in the parent class of " + classType.getName());
						}
					}
					// 2.2) the member is non-private and its visibility is reduced by the new member
					if(visible && Accessibility.isAbsolutelyLessVisibleThan(
						member.getAccessibility(), pmember.getAccessibility())){
						throw new RuntimeCheckException(
							"A member with name \"" + name + 
							"\" is defined in class " + classType.getName() + 
							" with a reduced visibility than the member of same name defined in its parent.");
					}
				}	
			}
		}
		
		addOverloadedMember(name, member, instMembers);
	}
	
	//---------------------- JClassType ----------------------//
	
	public void setParent(JClassType parent){
		classType.parent = parent;
	}
	
	public void setAbstract(boolean isAbstract){
		classType.properties.setAbstract(isAbstract);
	}
	
	public void setFinal(boolean isFinal){
		classType.properties.setFinal(isFinal);
	}
	
	public void setStatic(boolean isStatic){
		classType.properties.setStatic(isStatic);
	}
	
	public void setHosted(boolean isHosted){
		classType.properties.setHosted(isHosted);
	}
	
	public void setClassStaticConstructorMember(JClassStaticConstructorMember staticCtor){
		classType.staticConstructor = staticCtor;
	}
	
	public void addInstanceConstructor(JClassConstructorMember member) {
		if(classType.constructorMemberList==null){
			classType.constructorMemberList = new ArrayList<JClassConstructorMember>();
		}
		classType.constructorMemberList.add(member);
	}
	
	public void setDefaultInstanceConstructor(JClassConstructorMember ctor){
		this.defaultCtor = ctor;
	}
	
	public JClassConstructorMember getDefaultInstanceConstructor(){
		return this.defaultCtor;
	}
	
	/**
	 * Get a list of all abstract methods.
	 * @return never null
	 */
	public List<JClassMethodMember> getAbstractMethods(){
		Map<MemberKey, JClassMethodMember> interfaceMembers = null;
		InterfaceMemberMap imm = classType.getInterfaceMembers();
		JClassMethodMember[] jcmms = imm.getAllMembers();
		if (jcmms != null && jcmms.length > 0){
			interfaceMembers = new HashMap<MemberKey, JClassMethodMember>();
			for(JClassMethodMember jcmm : jcmms){
				interfaceMembers.put(jcmm.getKey(), jcmm);
			}
		}
		
		ClassMemberMap imems = classType.getMembers(false);
		JClassMember[] members = imems.getClassMembers();
		List<JClassMethodMember> absMethods = new ArrayList<JClassMethodMember>();		
		for(JClassMember jcm : members){
			if(jcm.getMemberType() == MemberType.METHOD){
				JClassMethodMember jcmm = (JClassMethodMember) jcm;
				if(jcmm.isAbstract()){
					absMethods.add(jcmm);
				}
			}
			
			if (interfaceMembers!= null && jcm.getAccessibility() == Accessibility.PUBLIC){
				interfaceMembers.remove(jcm.getKey());
			}
		}
		
		// Add un-implemented interface methods
		if (interfaceMembers != null && !interfaceMembers.isEmpty()){
			for(JClassMethodMember entry : interfaceMembers.values()){
				absMethods.add(entry);
			}
		}
		
		return absMethods;
	}
	
	//---------------------- Private members ----------------------//
	
	private void addOverloadedMember(String name, JClassMember member, Map<String, OneOrMoreList<JClassMember>> members){
		OneOrMoreList<JClassMember> overloads = members.get(name);
		if(overloads == null){
			overloads = new OneOrMoreList<>(member);
			members.put(name, overloads);
		} else {
			JClassMember first = overloads.getFirst();
			if (first.getMemberType() == MemberType.FIELD || 
				member.getMemberType() == MemberType.FIELD){
				throw new RuntimeCheckException(
					"A field member with name \"" + name + "\" cannot be defined more than once in class " + classType.getName());
			}
			
			MemberKey thisKey = member.getKey();
			for(JClassMember mem : overloads){
				if(mem.getKey().equals(thisKey)){
					throw new RuntimeCheckException(
						"A member with name \"" + name + "\" and same type is defined more than once in class " + classType.getName());
				}
			}
			
			// Overloading must conform to same visibility
			// (This is an additional restriction added by Julian. Allowing method overloading with different visibility
			// will cause great confusion during runtime for an interpreted language.)
			if (first.getAccessibility() != member.getAccessibility()){
				throw new RuntimeCheckException(
					"Multiple members with name \"" + name + "\" are defined with different visibility in class " + classType.getName());
			}
			
			overloads.add(member);
		}
	}
}
