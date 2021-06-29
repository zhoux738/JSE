package info.jultest.test.module;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestIO;

/**
 * Tests for built-in scripts.
 */
public class IncludeBuiltInTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "BuiltinScript";

	// print.jul
	@Test
	public void printTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		TestIO io = new TestIO(engine);
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "01_Print/main.jul"));
		Assert.assertTrue(io.getOutputString().contains("objabc1"));
	}
	
	// script.jul/incl()
	@Test
	public void inclTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "02_Incl_1/main.jul"));
		validateIntValue(gvt, "i5", 5);
	}
	
	// all.jul => any.jul
	@Test
	public void allTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "99_All/main.jul"));
		validateStringValue(gvt, "s_obj", "obj");
		validateStringValue(gvt, "s_abc", "abc");
		validateStringValue(gvt, "s_1", "1");
	}
}
