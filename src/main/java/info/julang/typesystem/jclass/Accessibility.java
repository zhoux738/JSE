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

import org.antlr.v4.runtime.Token;

import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.ExecutionContextType;
import info.julang.interpretation.context.MethodContext;
import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.ANTLRHelper;
import info.julang.typesystem.IllegalMemberAccessException;
import info.julang.typesystem.IllegalTypeAccessException;
import info.julang.typesystem.UnknownMemberException;
import info.julang.util.OneOrMoreList;

/**
 * The accessibility of an object type's member: public, protected or private
 * 
 * @author Ming Zhou
 */
public enum Accessibility {

	/**
	 * The member is accessible by anyone
	 */
	PUBLIC(true, "public"),
	
	/**
	 * The member is accessible by member in same class, and derived classes
	 */
	PROTECTED(true, "protected"),
	
	/**
	 * The member is accessible by member in same module
	 */
	MODULE(false, "internal"),
	
	/**
	 * The member is only accessible by member in same class
	 */
	PRIVATE(false, "private"),
	
	/**
	 * The member is not accessible from script
	 */
	HIDDEN(false, "[hidden]");
	
	/**
	 * The total number of accessibility kinds 
	 */
	public static int TotalNumberOfKinds = 5;
	
	private String name;
	
	private boolean subclassVisible;
	
	private Accessibility(boolean subclassVisible, String name){
		this.subclassVisible = subclassVisible;
		this.name = name;
	}
	
	/**
	 * Get name as is used in the script (except for HIDDEN). All in lower-case.
	 */
	@Override
	public String toString(){
		return name;
	}
	
	/**
	 * Get the name as is defined in the enum source. All in upper-case..
	 * @return
	 */
	public String getName(){
		return this.name();
	}
	
	/**
	 * Whether this is visible to subclasses.
	 * @return
	 */
	public boolean isSubclassVisible(){
		return subclassVisible;
	}
	
	public static Accessibility parse(Token tok){
		switch(tok.getType()){
		case JulianLexer.PUBLIC:
			return PUBLIC;
		case JulianLexer.PROTECTED:
			return PROTECTED;
		case JulianLexer.PRIVATE:
			return PRIVATE;
		case JulianLexer.INTERNAL:
			return MODULE;
		default:
			throw new BadSyntaxException("Unknown accesibility modifier: " + ANTLRHelper.getTokenTypeName(tok));
		}
	}
	
	/**
	 * A utility method to determine if one accessibility is absolutely less visible than another.
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public static boolean isAbsolutelyLessVisibleThan(Accessibility a1, Accessibility a2){
		switch(a1){
		case PUBLIC:
			// public is either more visible than or equally visible to others 
			return false; 
		case MODULE:
		case PROTECTED:
			// protected/module is less visible than public
			return a2 == Accessibility.PUBLIC;
		case PRIVATE:
			// private is less visible than public/protected/module
			return a2.ordinal() < Accessibility.PRIVATE.ordinal();
		default:
		// case HIDDEN:
			// hidden is less visible than others
			return a2 != Accessibility.HIDDEN;
		}
	}
	
	/**
	 * Check the accessibility to a type.
	 * <p>
	 * Such checks should happen whenever the type is first used in a lexical context. This includes type used as parent,
	 * parameter or return value in type/function definition; type used at local variable declaration; type to cast to.
	 * <p>
	 * By design as of 0.1.9, this check is not performed during type checking by 'is' keyword.
	 *  
	 * @param declaredType the type to try to access to.
	 * @param containingType the type from whose lexical scope the access is about to happen. null if from global context.
	 * @param throwIfNotFound if true, this method will throw {@link IllegalTypeAccessException} instead of returning false
	 * 
	 * @throws IllegalTypeAccessException if the member is invisible.
	 * 
	 * @return true if containingType is confirmed to have access to declaredType
	 */
	public static boolean checkTypeVisibility(
		ICompoundType declaredType,
		ICompoundType containingType,
		boolean throwIfNotFound){
		if (declaredType.getClassProperties().getAccessibility() == Accessibility.MODULE){
			String m1 = declaredType.getModuleName();
			String m2 = containingType != null ? containingType.getModuleName() : null;
			if (m2 == null || !m2.equals(m1)){
				if (throwIfNotFound) {
					throw new IllegalTypeAccessException(declaredType.getName());
				} else {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Check the accessibility to a member of the given object.
	 * <p>
	 * Parameter <code>containingType</code> is the type from where we try to refer to the object's member. For 
	 * global functions this is null, and the checking will fail if the member's accessibility is not public.
	 * <p>
	 * Otherwise, first determine the type on which the specified member is defined. This type is called <code>
	 * definingType</code>.
	 * <p>
	 * Then, for private member we require:<p><code>
	 * &nbsp;&nbsp;containingType == definingType</code><p>
	 * for protected member we require:<p><code>
	 * &nbsp;&nbsp;containingType.isDerivedFrom(definingType, true)</code><p>
	 * and for module member we require:<p><code>
	 * &nbsp;&nbsp;containingType.getModule() == definingType.getModule()</code><br>
	 * 
	 * @param declaredType the declared type of object on which the member is to be accessed. For example, if we 
	 * have <code>P p = new C()</code>, p's declared type is P, albeit its runtime type being C.
	 * @param memberName
	 * @param containingType the type in whose syntactic context a member of the object is to be accessed. For 
	 * example, if the script is making a method call in the a method defined by P, the current containingType, 
	 * regardless of the actual runtime type of the instance, is invariably P. In other words, this type is 
	 * strictly the type of class whose script is executing.
	 * @param context
	 * @param contextType
	 * @param isStatic
	 * @param throwIfNotFound
	 * 
	 * @throws UnknownMemberException if member doesn't exist.
	 * @throws IllegalMemberAccessException if the member is invisible.
	 * 
	 * @return the actual type on which this member is to be accessed.
	 */
	public static ICompoundType checkMemberAccess(
		ICompoundType declaredType, 
		String memberName, 
		ICompoundType containingType, 
		Context context, 
		ContextType contextType, 
		boolean isStatic, 
		boolean throwIfNotFound){

		ClassMemberMap cmm = null;
		ClassMemberLoaded cml = null;
		
		// First, check if the member is found at containing type and is private. This is because we don't
		// want to go through the inheritance chain if the target member is private, in which case only the 
		// member of current class should be accessed.
		if (containingType != null && declaredType.isDerivedFrom(containingType, false)){
			boolean checkPrivate = false;
			if(context != null && context instanceof MethodContext && context.getContextType() == ContextType.IMETHOD){
				MethodContext mc = (MethodContext)context;
				checkPrivate = mc.getExecutionContextType() == ExecutionContextType.InMethodBody;
			}
			
			if (checkPrivate){
				cmm = ((JClassType)containingType).getMembers(isStatic);// FIXME - may not be class.
				cml = cmm.getLoadedMemberByName(memberName).getFirst(); // All members have same accessibility.
				// If this member is not found, abort now. If we want a parent class's method to access a subclass's member, this member
				// must be defined already in the parent class and is overridden in the subclass.
				if(cml == null){
					if(throwIfNotFound){
						throw new UnknownMemberException(declaredType, memberName, isStatic);		
					} else {
						return null;
					}
				}
				
				// If this member is private, return now.
				if (cml.getClassMember().getAccessibility() == Accessibility.PRIVATE) {
					return containingType;
				}
			}
		}
		
		if (declaredType.isClassType()){
			cmm = ((JClassType)declaredType).getMembers(isStatic);
			// Here we only check the 1st one because the type loading process ensures that 
			// overloaded methods are of same visibility (Julian restriction).
			OneOrMoreList<ClassMemberLoaded> all = cmm.getLoadedMemberByName(memberName);
			cml = all.getFirst();
			if(cml == null){
				if(throwIfNotFound){
					throw new UnknownMemberException(declaredType, memberName, isStatic);		
				} else {
					return null;
				}
			}
			
			JClassType definingType = cmm.getContributingTypes()[cml.getRank()];
			Accessibility acc = cml.getClassMember().getAccessibility();
			switch(acc){
			case HIDDEN:
				// If the member is hidden, we throw as if it were not existing.
				throw new UnknownMemberException(declaredType, memberName, isStatic);
			case PRIVATE:
				checkInheritanceAccessbility(declaredType, definingType, memberName, containingType, contextType, true);
				break;
			case PROTECTED:
				checkInheritanceAccessbility(declaredType, definingType, memberName, containingType, contextType, false);
				break;
			case MODULE: 
				String m1 = definingType.getModuleName();
				String m2 = containingType != null ? containingType.getModuleName() : null;
				if (!(m1 != null && m2 != null && m1.equals(m2))){
					throw IllegalMemberAccessException.referInvisibleMemberEx(declaredType.getName(), memberName);
				}
				break;
			case PUBLIC:
				// NO-OP
			}
			
			return definingType;
		} else { // Interface - we check existence only, since all interface members are public.
			JClassMember mem = declaredType.getInstanceMemberByName(memberName);
			if(mem == null && throwIfNotFound){
				throw new UnknownMemberException(declaredType, memberName, isStatic);		
			}
			
			return declaredType;
		}
	}
	
	private static void checkInheritanceAccessbility(
		ICompoundType declaredType, 
		ICompoundType definingType, 
		String memberName, 
		ICompoundType containingType, 
		ContextType contextType, 
		boolean isPrivate){
		// If the member is private, it can be only accessed from a method in the same class.
		if(contextType == ContextType.IMETHOD || contextType == ContextType.SMETHOD){
			if((isPrivate && containingType != definingType)       ||    // 1) private member defined in the definingType
			   !containingType.isDerivedFrom(definingType, true)   &&    // 2) protected member defined in the definingType, but 
			   !(definingType.isClassType() &&                           //    NOT case where parent refers to a child's protected 
			     definingType.isDerivedFrom(containingType, false) &&    //    method which is defined in the parent too  
			     checkMemberDefinition(                                 
			    	 (JClassType)containingType, 
			    	 memberName, 
			    	 contextType == ContextType.SMETHOD))){
				throw IllegalMemberAccessException.referInvisibleMemberEx(declaredType.getName(), memberName);
			}
		} else if(contextType == ContextType.FUNCTION) {
			throw IllegalMemberAccessException.referInvisibleMemberEx(declaredType.getName(), memberName);
		}
	}

	// [BUG]
	// TODO - this check is not correct. If the child class defined a protected method of some name that happens to be
	// used by a method of different signature in the parent class then the parent class would accidentally have access
	// to this protected method on the child class too!
	private static boolean checkMemberDefinition(
		JClassType definingType, String memberName, boolean isStatic) {
		ClassMemberMap cmp = definingType.getMembers(isStatic);
		if (cmp.getLoadedMemberByName(memberName) != null) {
			return true;
		}
		
		return false;
	}
	
}
