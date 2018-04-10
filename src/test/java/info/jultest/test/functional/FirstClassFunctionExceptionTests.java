package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class FirstClassFunctionExceptionTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	@Test
	public void staceTraceTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "ex_01.jul"));
		
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.FUNCTIONAL, FEATURE);
		runner.executeAndValidate(
			"ex_01.jul", 
			"System.Exception", 
			null, 
			new String[]{
				"f()  (/.../ex_01.jul, 4)",
				"g(Function)  (/.../ex_01.jul, 8)"
			}, 
			false, 
			11);
	}

}