package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;

import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

public class PlatformErrorTests extends ExceptionTestsBase {
	
	private static final String FEATURE = "PlatformError";
	
	@Test
	public void stackOverflowTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "stackoverflow.jul"));
		
		assertException(teh, "System.StackOverflowException");
	}

}
