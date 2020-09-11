package info.jultest.test.oo;

import org.junit.Test;

import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.jultest.test.Commons;

public class ArgumentTests {

	private static final String FEATURE = "Argument";
	
	@Test
	public void unmatchTest() throws EngineInvocationError {
	    ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
	    runner.executeAndExpect(
	        "arg_unmtach_1.jul", 
	        KnownJSException.RuntimeCheck, 
	        null, 
	        "Wrong number of arguments");
	}
}
