package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JAttributeType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.jultest.test.Commons;

import java.util.List;

import org.junit.Test;

/*
 * Tests in this class use internal API to inspect the attributes on annotated classes.
 * See tests in System_Reflection_Attribute_ReflTestSuite which use public Reflection API.
 */
public class AttributeTests extends AttributeTestsBase {
	
	@Test
	public void basicClassAttributeDeclarationTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_1.jul"));
		
		JType typ = tt.getType(getModName() +".Author");
		assertNotNull(typ);
		assertEquals(getModName() +".Author", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		validateAnnotated(jct, 1);
		
		validateTypeValue(tt, null);
	}
	
	@Test
	public void multipleAttributeDeclarationsTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_5.jul"));
		
		JType typ = tt.getType(getModName() +".Author");
		assertNotNull(typ);
		assertEquals(getModName() +".Author", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = tt.getType(getModName() +".Owner");
		assertNotNull(typ);
		assertEquals(getModName() +".Owner", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		validateAnnotated(jct, 2);
		validateTypeValue(tt, null, 2);
		
		JClassMember mem = jct.getInstanceMemberByName("doSomething");
		validateAnnotated(mem, 2);
		validateTypeValue(tt, mem.getKey(), 2);
	}
	
	@Test
	public void basicMethodAttributeDeclarationTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_2.jul"));
		
		JType typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		JClassMember mem = jct.getInstanceMemberByName("doSomething");
		validateAnnotated(mem, 1);
		
		validateTypeValue(tt, mem.getKey());
	}
	
	@Test
	public void basicFieldAttributeDeclarationTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_3.jul"));
		
		JType typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		JClassMember mem = jct.getInstanceMemberByName("field1");
		validateAnnotated(mem, 1);
		
		validateTypeValue(tt, mem.getKey());
	}
	
	@Test
	public void basicCtorAttributeDeclarationTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_4.jul"));
		
		JType typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		JClassMember mem = jct.getClassConstructors()[0];
		validateAnnotated(mem, 1);
		
		validateTypeValue(tt, mem.getKey());
	}
	
	@Test
	public void emptyAttributeTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_6.jul"));
		
		JType typ = tt.getType(getModName() +".Unchecked");
		assertNotNull(typ);
		assertEquals(getModName() +".Unchecked", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = validateClass(tt, getModName() +".MyClass1");
		JClassType jct = (JClassType) typ;
		validateUnchecked(jct);
		
		typ = validateClass(tt, getModName() +".MyClass2");
		jct = (JClassType) typ;
		validateUnchecked(jct);
	}
	
	@Test
	public void attributeAndEnumTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_enum_1.jul"));
		
		JType typ = tt.getType(getModName() +".Unchecked");
		assertNotNull(typ);
		assertEquals(getModName() +".Unchecked", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = tt.getType(getModName() +".UCType");
		assertNotNull(typ);
		assertEquals(getModName() +".UCType", typ.getName());
		assertEquals(JEnumType.class, typ.getClass());
		
		typ = validateClass(tt, getModName() +".MyClass");
		JClassType jct = (JClassType) typ;

		JAnnotation[] annos = jct.getAnnotations();
		assertEquals(1, annos.length);
		JAnnotation anno = annos[0];
		assertEquals(getModName() +".Unchecked", anno.getAttributeType().getName());
		
		
		JValue val = tt.getValue(getModName() +".MyClass");
		assertEquals(TypeValue.class, val.getClass());
		TypeValue tv = (TypeValue) val;
		List<AttrValue> list = tv.getClassAttrValues();
		assertNotNull(list);
		assertEquals(1, list.size());

		AttrValue av = list.get(0);
		EnumValue name = (EnumValue) RefValue.tryDereference(av.getMemberValue("type"));
		
		assertEquals("Return", name.getLiteral());
	}
	
	/*
	 * Apply an attribute on an attribute type
	 * 
	 * Attribute
	 */
	@Test
	public void basicAttributeAttributeDeclarationTest() throws EngineInvocationError {
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, null, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "attr_attr_1.jul"));
		
		JType typ = tt.getType(getModName() +".Unchecked");
		assertNotNull(typ);
		assertEquals(getModName() +".Unchecked", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = tt.getType(getModName() +".Author");
		assertNotNull(typ);
		assertEquals(getModName() +".Author", typ.getName());
		assertEquals(JAttributeType.class, typ.getClass());
		
		typ = validateClass(tt, getModName() +".MyClass");
		
		JClassType jct = (JClassType) typ;
		validateAnnotated(jct, 1);
		
		validateTypeValue(tt, null);
	}
	
	@Test
	public void illegalClassAttributeDeclarationTest() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		
 		runner.executeAndExpect(
 			"attr_illegal_1.jul", 
 			"System.ClassLoadingException", 
 			12,
 			"System.IllegalAttributeUsageException",
 			-1);
	}
	
	@Test
	public void illegalAttributeMemberInDeclarationTest() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		
 		runner.executeAndExpect(
 			"attr_illegal_2.jul", 
 			"System.ClassLoadingException", 
 			7,
 			"System.IllegalAttributeUsageException",
 			-1);
	}
}