package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValueEx;
import static info.jultest.test.Commons.validateIntValueEx;

import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

// Promise API
public class PromiseAdvancedTests extends ThreadingTestBase {

	private static final String FEATURE = "Promise";
	
	@Test
	public void prompromTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "promprom_1.jul"));
		
		validateIntValueEx(gvt, "result", 7);
	}
	
	@Test
	public void deferTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "prom_defer_1.jul"));

		validateIntValueEx(gvt, "r1", 7);
		validateBoolValueEx(gvt, "r2", true);
	}
}
