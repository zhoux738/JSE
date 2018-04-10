package info.jultest.test.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JArrayBaseType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JEnumBaseType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.jultest.test.Commons;

import org.junit.Before;
import org.junit.Test;

public class BuiltInTypesTests {

	@Before
	public void setUp() {
		BuiltinTypeBootstrapper.bootstrapClassTypes();
	}
	
	@Test
	public void objectTypeTest() {
		JObjectType objType = JObjectType.getInstance();
		assertNotNull(objType);
	}
	
	@Test
	public void arrayTypeTest() {	
		JArrayBaseType arrType = JArrayBaseType.getInstance();
		assertNotNull(arrType);
		validateArrayType(arrType);
		
		JArrayType intArrType = JArrayType.createJArrayType(Commons.DummyTypeTable, IntType.getInstance(), false);
		validateArrayType(intArrType);
	}
	
	@Test
	public void enumTypeTest() {	
		JEnumBaseType arrType = JEnumBaseType.getInstance();
		assertNotNull(arrType);
		validateEnumType(arrType);
		
		JEnumType planets = JEnumType.createEnumType(
			new FQName("Planet"), 
			Accessibility.PUBLIC, 
			new String[]{"Mars", "Earth", "Venus"}, 
			null);
		validateEnumType(planets);
		
		boolean[] checks = new boolean[]{false, false, false};
		JClassMember[] smembers = planets.getClassStaticMembers();
		for(JClassMember member : smembers){
			if("Mars".equals(member.getName())){
				checks[0] = true;
			} else if("Earth".equals(member.getName())){
				checks[1] = true;
			} else if("Venus".equals(member.getName())){
				checks[2] = true;
			}
		}
		
		assertTrue(checks[0] && checks[1] && checks[2]);
	}
	
	private void validateEnumType(JClassType enumType){
		JClassMember[] members = JClassTypeMemberFilter.getFields(enumType.getClassInstanceMembers());
		JClassMember ordinalField = null;
		JClassMember literalField = null;
		for(JClassMember member : members){
			if(JEnumBaseType.FIELD_ORDINAL.equals(member.getName())){
				ordinalField = member;
			} else if(JEnumBaseType.FIELD_LITERAL.equals(member.getName())){
				literalField = member;
			}
		}
		assertNotNull(ordinalField);
		assertNotNull(literalField);
		
		assertEquals(IntType.getInstance(), ordinalField.getType());
		assertEquals(JStringType.getInstance(), literalField.getType());
	}
	
	private void validateArrayType(JArrayBaseType arrType){
		JClassMember[] members = JClassTypeMemberFilter.getFields(arrType.getClassInstanceMembers());
		JClassMember lengthField = null;
		for(JClassMember member : members){
			if("length".equals(member.getName())){
				lengthField = member;
				break;
			}
		}
		assertNotNull(lengthField);
		assertEquals(IntType.getInstance(), lengthField.getType());
	}
	
	@Test
	public void stringTypeTest() {		
		JStringType strType = JStringType.getInstance();
		assertNotNull(strType);
		
		JClassMember[] members = JClassTypeMemberFilter.getFields(strType.getClassInstanceMembers());
		JClassMember lengthField = null;
		for(JClassMember member : members){
			if("length".equals(member.getName())){
				lengthField = member;
				break;
			}
		}
		assertNotNull(lengthField);
		assertEquals(IntType.getInstance(), lengthField.getType());
		
		members = JClassTypeMemberFilter.getFunctions(strType.getClassInstanceMembers());
		JClassMember endsWithFunc = null;
		for(JClassMember member : members){
			if("endsWith".equals(member.getName())){
				endsWithFunc = member;
				break;
			}
		}
		assertNotNull(endsWithFunc);
		// is of function type
		assertEquals(JMethodType.class, endsWithFunc.getType().getClass());
		JMethodType funcType = (JMethodType) endsWithFunc.getType();
		// returns a boolean
		assertEquals(BoolType.getInstance(), funcType.getReturn().getReturnType());
		JParameter[] params = funcType.getParams();
		// has two parameters (this and suffix)
		assertEquals(2, params.length);
		// which is of string type
		assertEquals(JStringType.getInstance(), params[0].getType());
	}

}
