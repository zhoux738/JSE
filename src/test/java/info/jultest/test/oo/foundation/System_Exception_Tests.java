package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class System_Exception_Tests extends ExceptionTestsBase {

	private static final String FEATURE = "Foundation";
	
	/**
	 * New up an Exception
	 */
	@Test
	public void createExceptionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_1.jul"));
		
		validateStringValue(gvt, "msg", "Div by zero!");
	}
	
	/**
	 * Should throw <font color="green">ArrayOutOfRangeException</font> if accessing an index out of range.
	 */
	@Test
	public void arrayOutOfRangeExceptionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_ArrayOutOfRange.jul"));
		
		validateException(
			teh, 
			"System.ArrayOutOfRangeException",
			"Access to array out of range. Index=3, Max=1.",
			new String[]{
				"funB(Integer)  (/.../exception_ArrayOutOfRange.jul, 9)",
				"funA(Integer)  (/.../exception_ArrayOutOfRange.jul, 4)"
			},
			null,
			12);
	}
	
	/**
	 * Should throw <font color="green">NullReferenceException</font> if dereferencing a null value.
	 */
	@Test
	public void nullReferenceExceptionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_NullReference.jul"));
		
		validateException(
			teh, 
			"System.NullReferenceException",
			null,
			new String[]{
				"funB(Integer)  (/.../exception_NullReference.jul, 13)",
				"funA(Integer)  (/.../exception_NullReference.jul, 8)"
			},
			null,
			16);
	}
	
}
