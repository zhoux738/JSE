package info.jultest.test.functional;

import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;

import org.junit.Test;

public class FirstClassFunctionExceptionTests {
	
	private static final String FEATURE = "FirstClassFunction";
	
	@Test
	public void staceTraceTest1() throws EngineInvocationError {		
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