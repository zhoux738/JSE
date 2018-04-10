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
import java.util.Map;

import info.julang.execution.namespace.NamespacePool;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.loading.IClassOrInterface;
import info.julang.typesystem.loading.ISemanticChecker;
import info.julang.util.OneOrMoreList;

public interface ICompoundTypeBuilder extends IClassOrInterface {
	
	/**
	 * Get the type stub.
	 * <p>
	 * This method is used when the type being built needs to refer to itself or another which is also being built. 
	 * The caller of builder can use this stub in place of the actual type, which is yet to be built, when creating 
	 * class members.
	 */
	ICompoundType getStub();
	
	/**
	 * Set the namespace pool which the declared type is referencing.
	 * 
	 * @param nsPool
	 */
	void setNamespacePool(NamespacePool nsPool);
	
	/**
	 * Set the accessibility of this type.
	 * 
	 * @param acc
	 */
	void setAccessibility(Accessibility acc);
	
	/**
	 * Add a static member to the type.
	 * 
	 * @param member
	 */
	void addStaticMember(JClassMember member);
	
	/**
	 * Add an instance member to the type.
	 * 
	 * @param member
	 */
	void addInstanceMember(JClassMember member);
	
	/**
	 * Get instance members declared on this type.
	 * 
	 * @return can be null
	 */
	Map<String, OneOrMoreList<JClassMember>> getDeclaredInstanceMembers();
	
	/**
	 * Get static members declared on this type.
	 * 
	 * @return can be null
	 */
	Map<String, OneOrMoreList<JClassMember>> getDeclaredStaticMembers();
	
	/**
	 * Set mapped platform class.
	 * 
	 * @param clazz
	 */
	void setMappedPlatformClass(Class<?> clazz);
	
	/**
	 * Add initializer member to the type.
	 * 
	 * @param member
	 */
	void addInitializerMember(JClassInitializerMember member);
	
	/**
	 * Add type-targeted annotation.
	 * 
	 * @param annotation
	 */
	void addClassAnnotation(JAnnotation annotation);
	
	/**
	 * Add an interface the built type is to extend/implement.
	 * 
	 * @param interfaceType
	 */
	void addInterface(JInterfaceType interfaceType);
	
	/**
	 * List all interfaces added so far. Can be null.
	 */
	List<JInterfaceType> getInterfaces();
	
	/**
	 * Add a semantic checker to be invoked later.
	 * 
	 * @param checker
	 */
	void addSemanticChecker(ISemanticChecker checker);
	
	/**
	 * Run all the semantic checkers.
	 */
	void runSemanticCheckers();
	
	/**
	 * Build the type, optionally sealing it.
	 * 
	 * @return same as {@link #getStub()}.
	 */
	ICompoundType build(boolean sealNow);
	
	/**
	 * Seal the type.
	 */
	void seal();
	
	/**
	 * Mark the type as parsed.
	 */
	void setParsed();
	
	/**
	 * Whether the type has been parsed.
	 * @return
	 */
	boolean isParsed();
	
	/**
	 * Whether the type has been sealed.
	 */
	boolean isSealed();

	/**
	 * Set module name.
	 * 
	 * @param modName
	 */
	void setModuleName(String modName);

	/**
	 * Set location info for this type. This refers to the location of type definition's starting position.
	 * 
	 * @param loc
	 */
	void setLocationInfo(IHasLocationInfo loc);
	
	/**
	 * Get the location info for this type. (The location of type definition's starting position)
	 * 
	 * @return
	 */
	IHasLocationInfo getLocationInfo();
}
