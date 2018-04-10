package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.simple.SimpleScriptEngineInstrumentation;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;

import org.junit.Test;

public class TryCatchFinallyTests {

	private static final String FEATURE = "Try";
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception ex) {
	 *   ... // (hit)
	 * }
	 */
	@Test
	public void tryCatchHitTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_1.jul"));
		
		validateIntValue(gvt, "x", 70); // catch block executed
		validateIntValue(gvt, "y", 20); // try block after exception skipped
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception ex) {
	 *   ... // (miss)
	 * }
	 */
	@Test
	public void tryCatchMissTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_2.jul"));
		
		validateIntValue(gvt, "x", 10); // catch block not executed
		validateIntValue(gvt, "y", 20); // try block after exception skipped
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception1 ex1) {
	 *   ... // (miss)
	 * } catch (Exception2 ex2) {
	 *   ... // (hit)
	 * }
	 */
	@Test
	public void tryCatchMissCatchHitTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_3.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 20); // catch block 1 not executed
		validateIntValue(gvt, "z", 210); // catch block 2 executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNull(jse);
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception1 ex1) {
	 *   ... // (hit)
	 * } catch (Exception2 ex2) {
	 *   ... // (miss)
	 * }
	 */
	@Test
	public void tryCatchHitCatchMissTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_4.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 140); // catch block 1 executed
		validateIntValue(gvt, "z", 30); // catch block 2 not executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNull(jse);
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception1 ex1) {
	 *   ... // (miss)
	 * } catch (Exception2 ex2) {
	 *   ... // (miss)
	 * }
	 */
	@Test
	public void tryCatchMissCatchMissTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_5.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 20); // catch block 1 executed
		validateIntValue(gvt, "z", 30); // catch block 2 not executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
	}
	
	/*
	 * try {
	 *   ... (not throw)
	 * } finally {
	 *   ...
	 * }
	 */
	@Test
	public void tryFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "finally_1.jul"));
		
		validateIntValue(gvt, "x", 70); // try block after exception skipped
		validateIntValue(gvt, "y", 140); // catch block 1 executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
	}
	
	/*
	 * try {
	 *   ... (throw)
	 * } finally {
	 *   ...
	 * }
	 */
	@Test
	public void tryFinally2Test() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "finally_2.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 140); // catch block 1 executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception1 ex1) {
	 *   ... // (hit)
	 * } catch (Exception2 ex2) {
	 *   ... // (miss)
	 * } finally {
	 *   ...
	 * }
	 */
	@Test
	public void tryCatchHitCatchMissFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_catch_finally_1.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 140); // catch block 1 not executed
		validateIntValue(gvt, "z", 30); // catch block 2 not executed
		validateIntValue(gvt, "u", 280); // finally block executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNull(jse);
	}
	
	/*
	 * try {
	 *   ...
	 * } catch (Exception1 ex1) {
	 *   ... // (miss)
	 * } catch (Exception2 ex2) {
	 *   ... // (miss)
	 * } finally {
	 *   ...
	 * }
	 */
	@Test
	public void tryCatchMissCatchMissFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_catch_finally_2.jul"));
		
		validateIntValue(gvt, "x", 10); // try block after exception skipped
		validateIntValue(gvt, "y", 20); // catch block 1 not executed
		validateIntValue(gvt, "z", 30); // catch block 2 not executed
		validateIntValue(gvt, "u", 280); // finally block executed
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		assertEquals(1, stack.getDepth()); // scope exits properly
		
		JulianScriptException jse = engine.getContext().getException();
		assertNotNull(jse);
	}
	
	@Test
	public void returnFromTryTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_ret_1.jul"));
		
		validateIntValue(gvt, "x1", 4);
	}
	
	@Test
	public void returnFromCatchTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_ret_2.jul"));
		
		validateIntValue(gvt, "x1", 8);
	}
	
	@Test
	public void returnFromFinallyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_ret_3.jul"));
		
		validateIntValue(gvt, "x1", 12);
	}
	
	@Test
	public void returnFromTryWithFinallyTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_ret_4.jul"));
		
		validateIntValue(gvt, "x", 5);
		validateIntValue(gvt, "y", 10);
	}
	
	@Test
	public void returnFromTryWithFinallyTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "try_ret_5.jul"));
		
		validateIntValue(gvt, "x", 5);
		validateIntValue(gvt, "global", 100);
		validateIntValue(gvt, "global2", 500);
	}
}
