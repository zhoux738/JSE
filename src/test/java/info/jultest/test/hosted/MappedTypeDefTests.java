package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntArrayValue;

import org.junit.Assert;
import org.junit.Test;

import info.jultest.test.AssertHelper;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;

public class MappedTypeDefTests {
	
	private static final String FEATURE = "ClassDef";
	
	// Map java.io.File
	@Test
	public void mapFoundationClassTest() throws EngineInvocationError {
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_1.jul"));
		
		JType type = rt.getTypeTable().getType(ModuleInfo.DEFAULT_MODULE_NAME + ".MyFile");
		
		Assert.assertNotNull(type);
		Assert.assertEquals(JTypeKind.CLASS, type.getKind()); 
		
		JClassType jct = (JClassType) type;
		JClassMember[] imems = jct.getClassInstanceMembers();
		JClassMember[] smems = jct.getClassStaticMembers();
		JClassConstructorMember[] ctors = jct.getClassConstructors();
		
		// This is a basic sanity check. As of Java 1.8 java.io.File has more then 37 public 
		// instance members, 4 public static members and 3 constructors, but no members can 
		// be guaranteed to be not deprecated.
		Assert.assertTrue(imems.length > 20); 
		Assert.assertTrue(smems.length > 0);
		Assert.assertTrue(ctors.length > 0);
		
		// Also make sure we don't have a default ctor.
		for(JClassConstructorMember cm : ctors) {
			Assert.assertFalse(cm.isDefault());
		}
		
		for(JClassMember jcm : imems){
			if (jcm.getName().equals("notifyAll")) {
				Assert.fail("A member of name \"notifyAll\" is not supposed to be added.");
			}
		}
	}
	
	@Test
	public void javaObjectMethodsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_1.jul"));

		validateStringValue(gvt, "s", "obj1-string");
		validateIntValue(gvt, "hc", 100);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", false);
		validateBoolValue(gvt, "b3", false);
	}
	
	@Test
	public void platfromObjectMethodsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_2.jul"));

		validateStringValue(gvt, "s", "obj1-string");
		validateIntValue(gvt, "hc", 100);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", false);
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", false);
	}
	
	@Test
	public void platfromObjectMethodsForwardingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_3.jul"));

		validateStringValue(gvt, "s", "aobj1-string");
		validateIntValue(gvt, "hc", 101);
		validateBoolValue(gvt, "b1", false);
		validateBoolValue(gvt, "b2", true);
	}
	
	@Test
	public void objectMethodsExplicitDefFailedTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_illegal_1.jul"));

		String str = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(str, 
			"System.ClassLoadingException", "When mapping a type, the method", "must not be explicitly defined");
	}
	
	@Test
	public void objectMethodsExplicitDefFailedTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_illegal_2.jul"));

		String str = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(str, 
			"System.ClassLoadingException", "When implementing System.PlatformObject on a mapped type, the method", "must not be explicitly defined");
	}
	
	@Test
	public void staticFieldsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_2_static_fields.jul"));

		validateStringValue(gvt, "c", "10STR");
		validateStringValue(gvt, "b", "STR");
		validateIntValue(gvt, "a", 10);
	}

	// Map info.jultest.test.hosted.Enclosing$StaticallyEnclosed
	@Test
	public void staticallyNestedTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "def_3_statically_nested.jul"));

		validateIntValue(gvt, "v", 100);
	}
}
