package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class FirstClassFunctionAdvancedTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	@Test
	public void advPassFunctionAsArgTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "adv_01.jul"));
		
		validateIntValue(gvt, "r1", 5050);
		validateIntValue(gvt, "r2", 5050);
	}
	
	@Test
	public void advPassFunctionAsArgTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "adv_02.jul"));
		
		validateIntValue(gvt, "r1", 5050);
		validateIntValue(gvt, "r2", 5050);
	}

}