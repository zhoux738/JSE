package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringArrayValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

public class System_Util_IIterator_Tests extends ExceptionTestsBase {

	private static final String FEATURE = "Iterator";
	
	@Test
	public void customizedIteratorBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_1.jul"));
		
		validateStringArrayValue(gvt, "strs", new String[] {
			"aaa",
			"bbb",
			"ccc"
		});
	}
	
	@Test
	public void customizedIterableBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_2.jul"));
		
		validateStringArrayValue(gvt, "strs1", new String[] {
			"aaa",
			"bbb",
			"ccc"
		});
		validateStringArrayValue(gvt, "strs2", new String[] {
			"aaa",
			"bbb",
			"ccc"
		});
	}
	
	@Test
	public void customizedIteratorFaultTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_fault_1.jul"));
		
		validateException(
			teh, 
			"System.ArrayOutOfRangeException",
			"Access to array out of range. Index=3, Max=2.",
			new String[]{
				"next(<default>.MyStream)  (/.../iter_fault_1.jul, 18)"
			},
			"iter_fault_1.jul",
			28);
	}
}
