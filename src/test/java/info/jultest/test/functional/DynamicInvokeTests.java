package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class DynamicInvokeTests {
	
	private static final String FEATURE = "DynamicInvoke";
	
	// global function, strict arguments
	@Test
	public void dynamicInvokeTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_01.jul"));
		
		validateIntValue(gvt, "s6", 6);
		validateIntValue(gvt, "s7", 7);
	}
	
	// instance/static methods, strict arguments
	@Test
	public void dynamicInvokeTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_02.jul"));
		
		validateIntValue(gvt, "s8", 8);
		validateIntValue(gvt, "s9", 9);
	}
	
	// lambda defined in global scope, strict arguments
	@Test
	public void dynamicInvokeTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_03.jul"));
		
		validateIntValue(gvt, "s8", 8);
		validateIntValue(gvt, "s9", 9);
	}
	
	// lambda defined in method, strict arguments
	@Test
	public void dynamicInvokeTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_04.jul"));
		
		validateIntValue(gvt, "v8a", 8);
		validateIntValue(gvt, "v8b", 8);
		validateIntValue(gvt, "v14a", 14);
		validateIntValue(gvt, "v14b", 14);
	}
	
	// global function, less arguments than required
	@Test
	public void dynamicInvokeTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_05.jul"));
		
		validateIntValue(gvt, "i", 0);
		validateCharValue(gvt, "c", '\0');
		validateNullValue(gvt, "s");
		validateByteValue(gvt, "b", 0);
		validateBoolValue(gvt, "tf", false);
		validateNullValue(gvt, "bg");
	}
	
	// global function, more arguments than required
	@Test
	public void dynamicInvokeTest6() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_06.jul"));
		
		validateStringValue(gvt, "s1", "1a");
	}
	
	// instance/static methods, less arguments 
	@Test
	public void dynamicInvokeTest7() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_07.jul"));

		validateIntValue(gvt, "s8", 8);
		validateIntValue(gvt, "s9", 9);
		validateIntValue(gvt, "s11", 11);
		validateIntValue(gvt, "s14", 14);
	}
	
	// instance overloaded methods, exact or more arguments
	@Test
	public void dynamicInvokeTest8() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_08.jul"));

		validateIntValue(gvt, "s6", 6);
		validateIntValue(gvt, "s9", 9);
		validateIntValue(gvt, "s14", 14);
	}

	// static overloaded methods, exact, more, or less arguments
	@Test
	public void dynamicInvokeTest9() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_09.jul"));

		validateIntValue(gvt, "s10", 10);
		validateIntValue(gvt, "s14", 14);
		validateIntValue(gvt, "s50", 50);
		validateIntValue(gvt, "s105", 105);
		validateIntValue(gvt, "s200", 200);
	}
	
	// call invoke as first-class object.
	@Test
	public void dynamicInvokeTest10() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_10.jul"));
		validateIntValue(gvt, "res1", 5);
		validateIntValue(gvt, "res2", 5);
	}
	
	// call invoke.invoke as first-class object.
	@Test
	public void dynamicInvokeTest11() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_11.jul"));
		validateIntValue(gvt, "res1", 5);
	}
	
	// call invoke on built in method
	@Test
	public void dynamicInvokeTest12() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_12.jul"));
		validateIntValue(gvt, "res1", 1);
		validateIntValue(gvt, "res2", 1);
	}
	
	// default return value
	@Test
	public void dynamicInvokeTest13() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "test_13.jul"));
		
		validateIntValue(gvt, "j", 0);
		validateNullValue(gvt, "s");
		validateNullValue(gvt, "o");
		validateNullValue(gvt, "v1");
		validateNullValue(gvt, "v2");
	}
}