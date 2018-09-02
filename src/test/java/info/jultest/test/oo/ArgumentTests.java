package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.memory.value.AttemptToChangeConstException;

import org.junit.Assume;
import org.junit.Test;

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
