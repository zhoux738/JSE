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
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.annotation.IAnnotated;
import info.julang.typesystem.loading.IClassOrInterface;
import info.julang.util.OneOrMoreList;

/**
 * The interface defines properties shared among compound (non-basic) types.
 * 
 * @author Ming Zhou
 */
public interface ICompoundType extends JType, IAnnotated, IClassOrInterface {
	
	/**
	 * An incremental value that changes everytime the type got updated during the runtime.
	 * @return
	 */
	int getStamp();
	
	/**
	 * Get class/interface properties.
	 */
	JClassProperties getClassProperties();
	
	/**
	 * Try to get a class instance member by name.
	 * <p>
	 * If the member is overloaded, returns the first one defined in the class.
	 * 
	 * @param name
	 * @return null if not found
	 */
	JClassMember getInstanceMemberByName(String name);
	
	/**
	 * Try to get a class static member by name.
	 * <p>
	 * If the member is overloaded, returns the first one defined in the class.
	 * 
	 * @param name
	 * @return null if not found
	 */
	JClassMember getStaticMemberByName(String name);
	
	/**
	 * Try to get all the class static members with the specified name.
	 * <p>
	 * If the member is overloaded, returns all of them.
	 * 
	 * @param name
	 * @return never null. the list may contain 0+ class members.
	 */
	OneOrMoreList<JClassMethodMember> getStaticMethodMembersByName(String name);

	/**
	 * Get class field initializers.
	 * <p>
	 * The returned initializers are in the order they are declared in the class.
	 * 
	 * @param isStatic true to return initializers for static fields, false for instance fields
	 * @return
	 */
	JClassInitializerMember[] getClassInitializers(boolean isStatic);
	
	/**
	 * Get class static members.
	 * <p>
	 * This includes all static members defined by this class, regardless of its accessibility, and all
	 * static members which are (1) defined by any class in the hierarchy tree above this class and (2) 
	 * visible to this class.
	 * 
	 * @return never null
	 */
	JClassMember[] getClassStaticMembers();
	
	/**
	 * Get class instance members.
	 * <p>
	 * These include all instance members defined by the class, regardless of its accessibility, and all
	 * inheritable instance members defined by any type in the hierarchy tree above this class.
	 * <p>
	 * Inheritable means {@link info.julang.typesystem.jclass.Accessibility#PUBLIC PUBLIC}, 
	 * {@link info.julang.typesystem.jclass.Accessibility#PROTECTED PROTECTED}, or 
	 * {@link info.julang.typesystem.jclass.Accessibility#MODULE MODULE}. By using same name 
	 * for a member, subclass can override a visible definition from ancestor class. This method will
	 * only return the one that hides the others.
	 * 
	 * @return never null
	 */
	JClassMember[] getClassInstanceMembers();
	
	/**
	 * Get type of parent class.
	 * @return the type of parent class. Null if and only if the current type is 
	 * <code style="color:green">Object</code>.
	 */
	JClassType getParent();
	
	/**
	 * Get all the interfaces this interface extends from.
	 * @return never null
	 */
	JInterfaceType[] getInterfaces();
	
	/**
	 * Get extension classes which are directly installed to this type.
	 * @return never null
	 */
	OneOrMoreList<JClassType> getExtensionClasses();
	
	/**
	 * Get all extension classes which are installed to this type and its parent type.
	 * 
	 * @return An array of this type's extension types installed to itself and all of its ancestor types, 
	 * deduplicated and ordered based on proximity and text order. Never null, but can be empty.
	 */
	public JClassType[] getAllExtensionClasses();
	
	/**
	 * Determine if this type derives from another one.
	 * 
	 * @see {@link #canDerive(JClassType, boolean)}
	 * @param potentialParent the other type that is to be determined if it is a parent class of this one.
	 * @param includeIdentical	If true, returns true when this type is exactly the given one; 
	 * If false, returns false when this type is exactly the given one;
	 * @return true if the type derives from <code>potentialParent</code> 
	 */
	boolean isDerivedFrom(ICompoundType potentialParent, boolean includeIdentical);
	
	/**
	 * Determine if this type can derive another one.
	 * 
	 * @see {@link #isDerivedFrom(JClassType, boolean)}
	 * @param potentialParent the other type that is to be determined if it is a sub-class of this one.
	 * @param includeIdentical	If true, returns true when this type is exactly the given one; 
	 * If false, returns false when this type is exactly the given one;
	 * @return true if this type can derive <code>potentialChild</code> 
	 */
	boolean canDerive(ICompoundType potentialChild, boolean includeIdentical);
	
	/**
	 * Get the namespace pool this type references.
	 */
	NamespacePool getNamespacePool();
	
	/**
	 * Get the module name.
	 * <p>
	 * In future, we will expand this to incorporate other information about the module.
	 *  
	 * @return
	 */
	String getModuleName();
	
	/**
	 * Get the mapped platform type.
	 * 
	 * @return
	 */
	Class<?> getMappedPlatformClass();
}
