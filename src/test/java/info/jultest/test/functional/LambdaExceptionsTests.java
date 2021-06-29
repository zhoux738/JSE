package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;

public class LambdaExceptionsTests {
	
	private static final String FEATURE = "LambdaException";
	
	@Test
	public void throwFromLambdaTest() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.FUNCTIONAL, FEATURE);
		
 		//runner.executeOnly("ex_01.jul");
		runner.executeAndValidate(
			"ex_01.jul", 
			"System.Exception", 
			"fake error", 
			null, 
			false, 
			2);
	}
	
	@Test
	public void rethrowFromLambdaTest() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.FUNCTIONAL, FEATURE);
		
 		runner.executeAndExpect(
 			"ex_02.jul", 
 			ModuleInfo.DEFAULT_MODULE_NAME + ".MyException", 
 			14, 
 			"System.Exception", 
 			8);
	}
	
	@Test
	public void catchExceptionFromLambdaTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "ex_03.jul"));
		
		validateStringValue(gvt, "res", "fake error");
	}
}
