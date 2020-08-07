package info.jultest.test.types;

import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JFunctionType;

public class JClassTypeMemberFilter {

	/** 
	 * Get all functions.
	 * @param members
	 * @return
	 */
	public static JClassMember[] getFunctions(JClassMember[] members){
		return filterByCriteria(members, funcCriteria);
	}
	
	/**
	 * Get all (non-function) fields.
	 * @param members
	 * @return
	 */
	public static JClassMember[] getFields(JClassMember[] members){
		return filterByCriteria(members, fieldCriteria);
	}
	
	/**
	 * Filter an array of class members by given criteria.
	 * @return filtered class members
	 */
	static JClassMember[] filterByCriteria(JClassMember[] members, ClassMemberCriteria criteria){
		boolean[] marks = new boolean[members.length];
		int matched = 0;
		for(int i=0;i<members.length;i++){
			marks[i] = criteria.matches(members[i]);
			if(marks[i]){
				matched++;
			}
		}
		JClassMember[] results = new JClassMember[matched];
		for(int i=0, j=0;i<members.length;i++){
			if(marks[i]){
				results[j] = members[i];
				j++;
			}
		}
		return results;
	}
	
	private static ClassMemberCriteria funcCriteria = 
		ClassMemberCriteria.create(true).setClassScope().setParentType(JFunctionType.getInstance());
	
	private static ClassMemberCriteria fieldCriteria = 
		ClassMemberCriteria.create(true).setClassScope().setParentType(JFunctionType.getInstance()).negate();
	
	static class ClassMemberCriteria {

		private boolean[] allowedAccessibility;
		
		private boolean isClassScope;
		
		private boolean isInstanceScope;
		
		private boolean negated;
		
		private JType type;

		private JClassType parent;
		
		private ClassMemberCriteria(){
			
		}
		
		/**
		 * Create a new criteria.
		 * <p/>
		 * This factory method only accepts one argument (isInstance) to specify the scope of member.
		 * To add other aspects, call those chain-able setters on the returned criteria.<p/>
		 * An example - get all public function members, regardless of the scope:
		 * <code>
		 * ClassMemberCriteria(true).setClassScope(true).addAccessibility(Accessibility.PUBLC).setType(JFunctionType.DEFAULT);
		 * </pre>
		 * @param isInstance whether the member should have instance scope.
		 * @return a new criteria
		 */
		static ClassMemberCriteria create(boolean isInstance){
			ClassMemberCriteria criteria = new ClassMemberCriteria();
			if(isInstance){
				criteria.isInstanceScope = true;
			} else {
				criteria.isClassScope = true;
			}
			return criteria;
		}
		
		ClassMemberCriteria setClassScope(){
			isClassScope = true;
			return this;
		}
		
		ClassMemberCriteria setInstanceScope(){
			isInstanceScope = true;
			return this;
		}
		
		ClassMemberCriteria negate(){
			negated = true;
			return this;
		}
		
		ClassMemberCriteria addAccessibility(Accessibility acc){
			if(allowedAccessibility==null){
				allowedAccessibility = new boolean[Accessibility.TotalNumberOfKinds];
			}
			
			allowedAccessibility[acc.ordinal()] = true;
			return this;
		}
		
		ClassMemberCriteria setType(JType type){
			this.type = type;
			return this;
		}
		
		/**
		 * If a parent type P is set, when matching the queried type T,
		 * all the types that are ancestor of T (along the hierarchy tree), plus T itself, would be 
		 * checked to see if it is equal to P. If so, this term is considered qualified.
		 * 
		 * @param parent
		 * @return
		 */
		ClassMemberCriteria setParentType(JClassType parent){
			this.parent = parent;
			return this;
		}
		
		public boolean[] getAllowedAccessibility() {
			return allowedAccessibility;
		}

		public boolean isClassScope() {
			return isClassScope;
		}

		public boolean isInstanceScope() {
			return isInstanceScope;
		}
		
		public boolean isNegated() {
			return negated;
		}

		public JType getType() {
			return type;
		}
		
		boolean matches(JClassMember member){
			boolean result = matches0(member);
			return !negated ? result : !result;
		}
		
		private boolean matches0(JClassMember member){
			if(allowedAccessibility != null){
				Accessibility acc = member.getAccessibility();
				if(!allowedAccessibility[acc.ordinal()]){
					return false;
				}		
			}
			if(member.isStatic() && !isClassScope){
				return false;
			}
			if(!member.isStatic() && !isInstanceScope){
				return false;
			}
			
			if(type != null){
				if(member.getType() != type){
					return false;
				}	
			}
			
			if(parent != null){
				if(member.getType() instanceof JClassType){
					if(!hasParent((JClassType) member.getType(), parent)){
						return false;
					}	
				} else {
					// basic types
					return false;
				}
			}
			
			return true;
		}

		private boolean hasParent(JClassType type, JType parent) {
			if(type == parent){
				return true; 
			}
			JClassType base = type.getParent();
			if(base == null){
				return false;
			} else {
				return hasParent(base, parent);
			}
		}
		
	}
}
