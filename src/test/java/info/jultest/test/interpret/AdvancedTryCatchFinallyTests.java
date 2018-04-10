package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;

import org.junit.Test;

public class AdvancedTryCatchFinallyTests {

	private static final String FEATURE = "Try";
	
	/*
	 * try {
	 *   return ...;
	 * } finally {
	 *   ... // (hit)
	 * }
	 */
	@Test
	public void returnBeforeFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "adv_ret_before_finally.jul"));
		
		validateIntValue(gvt, "x", 5); // try block returned
		validateIntValue(gvt, "y", 1); // finally block always executed.
	}
	
	@Test
	public void finallyCannotChangeReturnedValueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "adv_finally_cannot_change_return_value.jul"));
		
		validateIntValue(gvt, "x", 5); // try block returned
	}
	
	/*
	 * try {
	 *   return ...;
	 * } finally {
	 *   ... // (hit)
	 * }
	 */
	@Test
	public void returnFromCatchWithFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "adv_ret_from_catch_with_finally.jul"));
		
		validateIntValue(gvt, "x", 5); // try block returned
		validateIntValue(gvt, "y", 1); // finally block always executed.
	}
	
	/*
	 * try {
	 *   throw ex1;
	 * } catch (Exception e) { // capture ex1
	 *   throw ex2;
	 * }
	 */
	@Test
	public void rethrowFromCatchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "adv_rethrow_from_catch.jul"));
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
		assertEquals("Ex 2", jse.getExceptionMessage()); // scope exits properly
	}
	
	/*
	 * try {
	 *   throw ex1;
	 * } finally {
	 *   throw ex2;
	 * }
	 */
	@Test
	public void throwFromFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "adv_throw_from_finally.jul"));
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
		assertEquals("Ex 2", jse.getExceptionMessage()); // scope exits properly
	}
}
