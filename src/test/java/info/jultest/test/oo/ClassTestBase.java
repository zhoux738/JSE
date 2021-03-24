package info.jultest.test.oo;

import info.julang.execution.EngineRuntime;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.util.OSTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Assert;

public class ClassTestBase {

	// The comparison algorithm must sync with MemberInfoComparator
	private static class ClassMemberComparator implements Comparator<JClassMember> {
		private final static ClassMemberComparator INSTANCE = new ClassMemberComparator();
		
		@Override
		public int compare(JClassMember c1, JClassMember c2) {
			return c1.getName().compareTo(c2.getName());
		}
	}
	
	// The comparison algorithm must sync with ClassMemberComparator
	private static class MemberInfoComparator implements Comparator<MemberInfo> {
		private final static MemberInfoComparator INSTANCE = new MemberInfoComparator();
		
		@Override
		public int compare(MemberInfo m1, MemberInfo m2) {
			return m1.name.compareTo(m2.name);
		}
	}
	
	protected void validateTypeDefinition(ICompoundType ctype, MemberInfo[] expected) {
		JClassMember[] members = ctype.getClassInstanceMembers();
		
		Assert.assertEquals(expected.length, members.length);

		Arrays.sort(expected, MemberInfoComparator.INSTANCE);
		Arrays.sort(members, ClassMemberComparator.INSTANCE);
		
		for(int i=0; i<expected.length; i++){
			JClassMember m = members[i];
			expected[i].validate(m);
		}
	}
	
	protected JClassType assertClassTypeDefined(EngineRuntime rt, String fqname){
		JType type = rt.getTypeTable().getType(fqname);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(JTypeKind.CLASS, type.getKind()); 
		
		return (JClassType) type;
	}
	
	protected JInterfaceType assertInterfaceTypeDefined(EngineRuntime rt, String fqname){
		JType type = rt.getTypeTable().getType(fqname);
		
		Assert.assertNotNull(type);
		Assert.assertEquals(JTypeKind.CLASS, type.getKind()); 
		
		return (JInterfaceType) type;
	}
	
	/**
	 * Create an array of class members, including all the members inherited from <font color="green">System.Object</font>.
	 * 
	 * @param addObjectMembers
	 * @param members
	 * @return
	 */
	protected MemberInfo[] createMembers(boolean addObjectMembers, MemberInfo[] members){
		List<MemberInfo> all = new ArrayList<MemberInfo>();
		for(MemberInfo m : members){
			all.add(m);
		}
		if(addObjectMembers){
			all.add(MemberInfo.createMethod("toString").returns(JStringType.getInstance()));
			all.add(MemberInfo.createMethod("equals").returns(BoolType.getInstance()));
			all.add(MemberInfo.createMethod("hashCode").returns(IntType.getInstance()));
			all.add(MemberInfo.createMethod("getType").returns(new LoadedType("System.Type")));
		}
		MemberInfo[] ret = new MemberInfo[all.size()];
		return all.toArray(ret);
	}
	
	static class MemberInfo {		

		public void validate(JClassMember m) {
			Assert.assertEquals(name, m.getName());
			Assert.assertEquals(type, m.getMemberType());
			if (acc != null){
				Assert.assertEquals(acc, m.getAccessibility());
			}
		}
		
		static FieldMemberInfo createField(String name){
			return new FieldMemberInfo(name);
		}
		
		static MethodMemberInfo createMethod(String name){
			return new MethodMemberInfo(name);
		}
		
		CtorMemberInfo createCtor(String name){
			return new CtorMemberInfo(name);
		}
		
		MemberInfo(String name, MemberType type){
			this.name = name;
			this.type = type;
		}
		
		public MemberInfo isAccessible(Accessibility acc){
			this.acc = acc;
			return this;
		}
		
		private String name;
		private MemberType type;
		private Accessibility acc;
	}
	
	static class FieldMemberInfo extends MemberInfo {
		
		@Override
		public void validate(JClassMember m) {
			super.validate(m);
			JClassFieldMember mm = (JClassFieldMember)m;
			
			if(fieldType != null){
				Assert.assertSame(mm.getType(), fieldType);
			}
		}
		
		FieldMemberInfo(String name){
			super(name, MemberType.FIELD);
		}
		
		public FieldMemberInfo is(JType type){
			fieldType = type;
			return this;
		}
		
		private JType fieldType;
		
	}
	
	static class MethodMemberInfo extends MemberInfo {
		
		@Override
		public void validate(JClassMember m) {
			super.validate(m);
			JClassMethodMember mm = (JClassMethodMember)m;
			JFunctionType ft = mm.getMethodType();
			
			if(returnType != null){
				if (returnType == AnyType.getInstance()) {
					Assert.assertTrue(ft.getReturn().isUntyped());
				} else if (returnType instanceof LoadedType){
					Assert.assertEquals(returnType.getName(), returnType.getName());
				} else {
					Assert.assertSame(returnType, ft.getReturnType());
				}
			}
			
			if(TripleState.isRelevant(abs)){
				Assert.assertEquals(abs.toBool(), mm.isAbstract());
			}
			if(TripleState.isRelevant(sta)){
				Assert.assertEquals(sta.toBool(), mm.isStatic());
			}
		}
		
		MethodMemberInfo(String name){
			super(name, MemberType.METHOD);
		}
		
		protected MethodMemberInfo(String name, MemberType type){
			super(name, type);
		}
		
		public MethodMemberInfo returns(JType type){
			returnType = type;
			return this;
		}
		
		public MethodMemberInfo isAbstract(boolean abs){
			this.abs = TripleState.fromBool(abs);
			return this;
		}
		
		public MethodMemberInfo isStatic(boolean sta){
			this.sta = TripleState.fromBool(sta);
			return this;
		}
		
		public MethodMemberInfo isAccessible(Accessibility acc){
			return (MethodMemberInfo)super.isAccessible(acc);
		}
		
		private JType returnType;
		
		private TripleState abs;
		private TripleState sta;	
		
	}
	
	static class CtorMemberInfo extends MethodMemberInfo {
		
		@Override
		public void validate(JClassMember m) {
			super.validate(m);
		}
		
		CtorMemberInfo(String name){
			super(name, MemberType.CONSTRUCTOR);
		}
		
		@Override
		public MethodMemberInfo returns(JType type){
			return this;
		}
		
	}
	
	static enum TripleState {
		IRRELEVANT, TRUE, FALSE;
		
		boolean toBool(){
			return this.ordinal() == 1;
		}
		
		static boolean isRelevant(TripleState ts){
			return ts == TRUE || ts == FALSE;
		}
		
		static TripleState fromBool(boolean b){
			return b ? TRUE : FALSE;
		}
	}
	
	static class LoadedType implements JType {

		private String name;
		
		LoadedType(String name){
			this.name = name;
		}
		
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
			return 0;
		}
	}
}
