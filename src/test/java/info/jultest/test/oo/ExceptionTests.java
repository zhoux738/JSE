package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static org.junit.Assert.*;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.simple.SimpleScriptEngineInstrumentation;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ExceptionTests extends ExceptionTestsBase {

	private static final String FEATURE = "Exception";
	
	@Test
	public void throwExceptionByEngineTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_1.jul"));
		
		validateException(
			teh, 
			"System.DivByZeroException",
			"Cannot divide by zero.",
			new String[]{
				"funC(Integer)  (/.../exception_2.jul, 12)",
				"funB(Integer)  (/.../exception_2.jul, 8)",
				"funA(Integer)  (/.../exception_2.jul, 4)"
			},
			null,
			15);
	}
	
	@Test
	public void throwExceptionByUserTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_2.jul"));
		
		validateException(
			teh, 
			"System.DivByZeroException",
			"Cannot divide by zero.",
			new String[]{
				"funC(Integer)  (/.../exception_2.jul, 12)",
				"funB(Integer)  (/.../exception_2.jul, 8)",
				"funA(Integer)  (/.../exception_2.jul, 4)"
			},
			null,
			15);
	}

	// raising exception should pop stack properly
	@Test
	public void throwStackTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_3.jul"));
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		
		assertEquals(1, stack.getDepth());
	}
	
	// raising exception should exit scope properly
	@Test
	public void throwScopeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		SimpleScriptEngineInstrumentation instru = new SimpleScriptEngineInstrumentation();
		engine.setInstrumentation(instru);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_4.jul"));
		
		ThreadRuntime rt = instru.getThreadRuntime();
		ThreadStack stack = rt.getThreadStack();
		
		int level = stack.currentFrame().getVariableTable().getNestLevel();
		assertEquals(1, level);
	}
	
	@Test
	public void exceptionWithCauseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "exception_5.jul"));
		
		validateException(
			teh, 
			"System.Exception",
			"funC failed.",
			new String[]{
				"funB(Integer)  (/.../exception_5.jul, 11)",
				"funA(Integer)  (/.../exception_5.jul, 4)"
			},
			null,
			19);
		
		validateCause(
			teh, 
			"System.DivByZeroException",
			"Cannot divide by zero.",
			new String[]{
				"funC(Integer)  (/.../exception_5.jul, 16)"
			},
			null,
			-1);
	}
}
