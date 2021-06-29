package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ModuleDefErrorTests extends ExceptionTestsBase {

	private static final String FEATURE = "ClassDefError";

	@Test
	public void illegalSystemClassTest() throws EngineInvocationError {
		validateException("system_1.jul");
	}
	
	@Test
	public void illegalLooseScriptTest() throws EngineInvocationError {
		validateException("loose_1.jul");
	}

	private void validateException(String script) throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);

		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));

		assertException(teh, "System.IllegalModuleException");
	}

}
