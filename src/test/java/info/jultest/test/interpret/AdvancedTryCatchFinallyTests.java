package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.jultest.test.AssertHelper;
import info.jultest.test.Commons;

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
	
	@Test
	public void outputExceptionTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		engine.setRedirection(null, ps, null);
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "print_1.jul"));
		
		ps.flush();
		String trace = new String(baos.toByteArray());
		AssertHelper.validateStringOccurences(
			trace,
			"System.Exception: Totally Unexpected", System.lineSeparator(),
			"  at funz()", "src/test/julian/Interpret/Imperative/Try/print_1.jul, 4", System.lineSeparator(),
			"  at funy()", "src/test/julian/Interpret/Imperative/Try/print_1.jul, 8", System.lineSeparator(),
			"  at funx()", "src/test/julian/Interpret/Imperative/Try/print_1.jul, 12", System.lineSeparator(),
			"  from", "src/test/julian/Interpret/Imperative/Try/print_1.jul, 16", System.lineSeparator());
	}
	
	/*
	System.Exception: Failed due to underlying issue
	  at funx()  (<root>/src/test/julian/Interpret/Imperative/Try/print_2.jul, 13)
	  from  (<root>/src/test/julian/Interpret/Imperative/Try/print_2.jul, 18)
	Caused by:
	System.Exception: Root Cause
	  at funz()  (<root>/src/test/julian/Interpret/Imperative/Try/print_2.jul, 2)
	  at funy()  (<root>/src/test/julian/Interpret/Imperative/Try/print_2.jul, 6)
	  from  (<root>/src/test/julian/Interpret/Imperative/Try/print_2.jul, 11)
	*/
	@Test
	public void outputExceptionTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		engine.setRedirection(null, ps, null);
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "print_2.jul"));

		ps.flush();
		String trace = new String(baos.toByteArray());
		AssertHelper.validateStringOccurences(
			trace,
			"System.Exception: Failed due to underlying issue", System.lineSeparator(),
			"  from", "src/test/julian/Interpret/Imperative/Try/print_2.jul, 20", System.lineSeparator(),
			"Caused by:", System.lineSeparator(),
			"System.Exception: Root Cause", System.lineSeparator(),
			"  from", "src/test/julian/Interpret/Imperative/Try/print_2.jul, 13", System.lineSeparator());
	}
}
