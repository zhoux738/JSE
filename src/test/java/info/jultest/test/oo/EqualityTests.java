package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class EqualityTests {

	private static final String FEATURE = "Equality";
	
	@Test
	public void defaultEqualsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "equals_1.jul"));
		
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", false);
	}
	
	@Test
	public void overriddenEqualsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "equals_2.jul"));
		
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", true);
	}
	
	@Test
	public void hierarchicalEqualsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "equals_3.jul"));
		
		validateBoolValue(gvt, "res1", false);
		validateBoolValue(gvt, "res2", false);
		validateBoolValue(gvt, "res3", true);
		validateBoolValue(gvt, "res4", false);
	}
	
	@Test
	public void nullEqualsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "equals_4.jul"));
		
		validateBoolValue(gvt, "c1", false);
		validateBoolValue(gvt, "c2", false);
		validateBoolValue(gvt, "c3", false);
		validateBoolValue(gvt, "c4", false);
		validateBoolValue(gvt, "i1", false);
		validateBoolValue(gvt, "i2", false);
		validateBoolValue(gvt, "i3", false);
		validateBoolValue(gvt, "i4", false);
		validateBoolValue(gvt, "done", true);
	}
}
