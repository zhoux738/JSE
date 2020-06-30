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
import java.util.List;
import java.util.Map;

import info.julang.execution.namespace.NamespacePool;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.IDeferredBuildable;
import info.julang.typesystem.loading.ISemanticChecker;
import info.julang.util.OneOrMoreList;

/**
 * A builder used to create an interface type step by step.
 */
public class JInterfaceTypeBuilder implements ICompoundTypeBuilder {

	protected boolean parsed;
	
	private boolean sealed;
	
	private JInterfaceType compoundType;
	
	private List<ISemanticChecker> checkers;
	
	private IHasLocationInfo loc;
	
	/**
	 * Create a class type builder that can build up a class type step by step.
	 * @param name the full name of this class.
	 * @param classTypePrototype the prototype, which contains no members yet and to be updated by this builder.
	 */
	public JInterfaceTypeBuilder(String name, JInterfaceType typePrototype){
		compoundType = typePrototype;
		compoundType.name = name;
		compoundType.properties = new JClassProperties(Accessibility.PUBLIC);
	}
	
	@Override
	public void setMappedPlatformClass(Class<?> clazz){
		compoundType.mappingTarget = clazz;
	}

	@Override
	public void setLocationInfo(IHasLocationInfo loc){
		this.loc = loc;
	}

	@Override
	public IHasLocationInfo getLocationInfo(){
		return loc;
	}
	
	@Override
	public void addSemanticChecker(ISemanticChecker checker){
		if (checkers == null) {
			checkers = new ArrayList<ISemanticChecker>();
		}
		
		checkers.add(checker);
	}
	
	@Override
	public void runSemanticCheckers(){
		if (checkers != null) {
			for(ISemanticChecker c : checkers){
				c.check();
			}
		}
	}

	@Override
	public boolean isClassType(){
		return false;
	}
	
	@Override
	public JInterfaceType getStub(){
		return compoundType;
	}
	
	@Override
	public void setNamespacePool(NamespacePool nsPool){
		compoundType.nsPool = nsPool;
	}
	
	@Override
	public void setAccessibility(Accessibility acc) {
		compoundType.properties.setAccessibility(acc);
	}
	
	public void setHosted(boolean isHosted){
		// NO-OP
	}
	
	@Override
	public void addStaticMember(JClassMember member){
		// We have check during class parsing to ensure this method will not be called. So it's a bug if this ever happens.
		throw new JSEError("Cannot add static member to interface.");
	}
	
	@Override
	public void addInstanceMember(JClassMember member){
		checkSealed();
		
		Map<String, OneOrMoreList<JClassMember>> map = compoundType.instanceMemberMap;
		if(map==null){
			compoundType.instanceMemberMap = map = new HashMap<String, OneOrMoreList<JClassMember>>();
		}
		
		String name = member.getName();
		OneOrMoreList<JClassMember> list = map.get(name);
		if (list == null){
			map.put(name, new OneOrMoreList<JClassMember>(member));
		} else {
			MemberKey thisKey = member.getKey();
			for(JClassMember mem : list){
				if(mem.getKey().equals(thisKey)){
					throw new RuntimeCheckException(
						"A member with name \"" + name + 
						"\" and same type is defined more than once in interface " + compoundType.getName());
				}
			}
			
			list.add(member);
		}
	}
	
	@Override
	public Map<String, OneOrMoreList<JClassMember>> getDeclaredInstanceMembers() {
		return compoundType.instanceMemberMap;
	}
	
	@Override
	public Map<String, OneOrMoreList<JClassMember>> getDeclaredStaticMembers() {
		return null;
	}
	
	@Override
	public void addInterface(JInterfaceType interfaceType){
		checkSealed();
		
		if(compoundType.interfaceList == null){
			compoundType.interfaceList = new ArrayList<JInterfaceType>();
		}
		compoundType.interfaceList.add(interfaceType);
	}
	
	@Override
	public void addExtension(JClassType classType){
		checkSealed();
		
		if(compoundType.extensions == null){
			compoundType.extensions = new OneOrMoreList<JClassType>(classType);
		} else {
			compoundType.extensions.add(classType);
		}
	}
	
	@Override
	public List<JInterfaceType> getInterfaces(){
		return compoundType.interfaceList;
	}
	
	@Override
	public void addInitializerMember(JClassInitializerMember member){
		if(compoundType.initializerMemberList==null){
			compoundType.initializerMemberList = new ArrayList<JClassInitializerMember>();
		}
		compoundType.initializerMemberList.add(member);
	}
	
	@Override
	public void addClassAnnotation(JAnnotation annotation) {
		if(compoundType.annotationList==null){
			compoundType.annotationList = new ArrayList<JAnnotation>();
		}
		compoundType.annotationList.add(annotation);
	}

	@Override
	public JInterfaceType build(boolean sealNow){
		sealed = sealNow;
		if (sealed && compoundType instanceof IDeferredBuildable) {
			IDeferredBuildable deferred = (IDeferredBuildable) compoundType;
			deferred.preInitialize();
		}
		return compoundType;
	}
	

	@Override
	public void seal(){
		sealed = true;
		if (sealed && compoundType instanceof IDeferredBuildable) {
			IDeferredBuildable deferred = (IDeferredBuildable) compoundType;
			deferred.preInitialize();
		}
	}
	
	@Override
	public void setParsed(){
		parsed = true;
	}

	@Override
	public boolean isParsed() {
		return parsed;
	}
	
	@Override
	public boolean isSealed(){
		return sealed;
	}
	
	protected void checkSealed(){	
		if (sealed){
			throw new JSEError("The type (" + compoundType.getName() + ") is sealed. Cannot add more members to it.");
		}
	}

	@Override
	public void setModuleName(String modName) {
		compoundType.moduleName = modName;
	}
}
