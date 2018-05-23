package info.jultest.test.oo;

import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.julang.execution.symboltable.TypeTable;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassMember.MemberKey;
import info.julang.typesystem.jclass.JDefinedClassType;
import info.julang.typesystem.jclass.annotation.IAnnotated;
import info.julang.typesystem.jclass.annotation.JAnnotation;

import java.util.List;

public class AttributeTestsBase {

	protected static final String FEATURE = "Attribute";
	
	protected void validateUnchecked(IAnnotated ia){
		JAnnotation[] annos = ia.getAnnotations();
		assertEquals(1, annos.length);
		JAnnotation anno = annos[0];
		assertEquals(getModName() + ".Unchecked", anno.getAttributeType().getName());
	}
	
	protected JType validateClass(TypeTable tt, String name){
		JType typ = tt.getType(name);
		assertNotNull(typ);
		assertEquals(name, typ.getName());
		assertEquals(JDefinedClassType.class, typ.getClass());
		return typ;
	}
	
	protected void validateAnnotated(IAnnotated ia, int size){
		JAnnotation[] annos = ia.getAnnotations();
		assertEquals(size, annos.length);
		JAnnotation anno = annos[0];
		assertEquals(getModName() + ".Author", anno.getAttributeType().getName());
		
		if(size == 2){
			anno = annos[1];
			assertEquals(getModName() + ".Owner", anno.getAttributeType().getName());
		}
	}
	
	protected void validateTypeValue(TypeTable tt, MemberKey memberName){
		validateTypeValue(tt, memberName, 1);
	}
	
	protected void validateTypeValue(TypeTable tt, MemberKey memberName, int size){
		JValue val = tt.getValue(getModName() + ".MyClass");
		assertEquals(TypeValue.class, val.getClass());
		TypeValue tv = (TypeValue) val;
		List<AttrValue> list = memberName == null ? tv.getClassAttrValues() : tv.getMemberAttrValues(memberName);
		assertNotNull(list);
		
		assertEquals(size, list.size());

		AttrValue av = list.get(0);
		JValue name = av.getMemberValue("name");
		JValue year = av.getMemberValue("year");
		
		validateStringValue(name, "Liam");
		validateIntValue(year, 1980);
		
		if(size == 2){
			av = list.get(1);
			name = av.getMemberValue("name");
			
			validateStringValue(name, "Scot");
		}
	}
	
	protected String getModName(){
		return ModuleInfo.DEFAULT_MODULE_NAME;
	}
}
