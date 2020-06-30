package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ExtensionTests {

	private static final String FEATURE = "Extension";
	
	@Test
	public void callAsStaticTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_01.jul"));
		
		validateIntValue(gvt, "result", 80);
	}
	
	@Test
	public void callAsExtensionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_02.jul"));
		
		validateIntValue(gvt, "result", 80);
	}
	
	@Test
	public void callOverloadedExtensionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_03.jul"));
		
		validateIntValue(gvt, "result200", 200);
		validateIntValue(gvt, "result100", 100);
	}
	
	@Test
	public void callOverloadedExtensionInstalledToInterfaceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_10.jul"));
		
		validateIntValue(gvt, "result200", 200);
	}
	
	@Test
	public void callOverloadedExtensionInstalledToParentTypeAndInterfaceTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_11.jul"));
		
		validateIntValue(gvt, "result110", 110);
	}
	
	@Test
	public void callChainedExtensionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_04.jul"));
		
		validateIntValue(gvt, "result100", 100);
	}
	
	@Test
	public void callOverloadedExtensionInstalledToParentTypeAndInterfaceTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_05.jul"));
		
		validateIntValue(gvt, "r1", 1);
		validateIntValue(gvt, "r2", 2);
		validateIntValue(gvt, "r3", 3);
		validateIntValue(gvt, "r4", 4);
	}
	
	@Test
	public void callOverloadedExtensionInstalledMoreThanOnceAlongTheHierarchyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_06.jul"));
		
		validateIntValue(gvt, "result", 1);
	}
	
	@Test
	public void callOverloadedExtensionMethodsFromDifferentExtensionClassesTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_07.jul"));
		
		validateStringValue(gvt, "s1", "exa");
		validateStringValue(gvt, "s2", "exb");
	}
	
	@Test
	public void callExtensionMethodsInstalledByExtensionClassTargetingInterfaceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_08.jul"));

		validateIntValue(gvt, "result", 5);
	}
	
	@Test
	public void callExtWithoutUsingAddressingSyntaxTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "comp_09.jul"));

		validateIntValue(gvt, "r6", 6);
		validateIntValue(gvt, "r7", 7);
	}
	
	@Test
	public void callExtensionByFunctionHandleTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extadv_01.jul"));

		validateIntValue(gvt, "r1", 1);
		validateIntValue(gvt, "r2", 2);
	}
	
	@Test
	public void callExtensionByFunctionHandleTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extadv_02.jul"));

		validateIntValue(gvt, "r5", 5);
		validateIntValue(gvt, "r7", 7);
	}
}
