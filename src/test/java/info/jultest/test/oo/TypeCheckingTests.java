package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class TypeCheckingTests {

	private static final String FEATURE = "Checking";

	@Test
	public void isBasicTypeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_basic.jul"));
		
		validateBoolValue(gvt, "ic", true);
		validateBoolValue(gvt, "bc", true);
		validateBoolValue(gvt, "ic2", false);
	}
	
	@Test
	public void isClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_1.jul"));
		
		validateBoolValue(gvt, "cr", true);
	}
	
	@Test
	public void isParentClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_2.jul"));
		
		validateBoolValue(gvt, "cr", true);
	}
	
	@Test
	public void nullIsNotAnythingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_3.jul"));
		
		validateBoolValue(gvt, "v1", false);
		validateBoolValue(gvt, "v2", false);
		validateBoolValue(gvt, "v3", false);
		validateBoolValue(gvt, "v4", false);
		validateBoolValue(gvt, "v5", false);
		validateBoolValue(gvt, "v6", false);
		validateBoolValue(gvt, "mark", true);
	}
	
	@Test
	public void objectCheckTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_4.jul"));
		
		validateBoolValue(gvt, "v1", true);
		validateBoolValue(gvt, "mark", true);
	}
	
	@Test
	public void anyCheckTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "is_any.jul"));
		
		validateBoolValue(gvt, "ic", true);
		validateBoolValue(gvt, "sc", true);
		validateBoolValue(gvt, "cc", true);
	}
}
