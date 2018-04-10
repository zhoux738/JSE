package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ReferenceComparisonTests {

	private static final String FEATURE = "RefCompare";
	
	@Test
	public void compareRefTypAToRefTypATest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "compare_1.jul"));
		
		validateBoolValue(gvt, "res1", false);
		validateBoolValue(gvt, "res2", true);
		validateBoolValue(gvt, "res3", true);
	}
	
	@Test
	public void compareRefTypAToRefTypBTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "compare_2.jul"));
		
		validateBoolValue(gvt, "res1", false);
	}

}
