package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateUntypedValue;
import info.jultest.test.Commons;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;

import org.junit.Assume;
import org.junit.Test;

public class UntypedTests {

	private static final String FEATURE = "Untyped";

	@Test
	public void untypedVarDeclTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_1.jul"));
		
		JValue a = validateUntypedValue(gvt, "a");
		JValue b = validateUntypedValue(gvt, "b");
		validateIntValue(a, 3);
		validateStringValue(b, "xyz");
	}
	
	@Test
	public void untypedVarsDeclTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_2.jul"));
		
		JValue a = validateUntypedValue(gvt, "a");
		JValue b = validateUntypedValue(gvt, "b");
		validateIntValue(a, 3);
		validateStringValue(b, "xyz");
	}
	
	// untyped function parameter
	@Test
	public void funUntypedParamTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fun_1.jul"));
		
		validateIntValue(gvt, "a", 3);
	}
	
	// untyped function return
	@Test
	public void funUntypedReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fun_2.jul"));
		
		validateIntValue(gvt, "a", 3);
		validateStringValue(gvt, "b", "xyz");
	}
	
	// reassign an untyped argument in function
	@Test
	public void funUntypedReassignTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fun_3.jul"));
		
		JValue v = validateUntypedValue(gvt, "a");
		validateStringValue(v, "xyz");
	}
	
	// reassign an untyped argument in function
	@Test
	public void funUntypedReassignTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "fun_4.jul"));
		
		JValue z1 = validateUntypedValue(gvt, "z1");
		validateStringValue(z1, "abc");
		validateStringValue(gvt, "z2", "abc");
		validateIntValue(gvt, "x", 3);
	}
	
	// untyped instance field
	@Test
	public void ifieldUntypedTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ifield_1.jul"));
		
		validateIntValue(gvt, "z", 5);
		validateStringValue(gvt, "s", "abc");		
		validateIntValue(gvt, "x", 120);
	}
	
	// untyped instance method parameter/return
	@Test
	public void imethodUntypedReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "imethod_1.jul"));
		
		validateIntValue(gvt, "a", 3);
		validateStringValue(gvt, "b", "xyz");		
		validateIntValue(gvt, "z", 5);
	}
	
	@Test
	public void imethodUntypedReassignTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "imethod_2.jul"));
		
		validateIntValue(gvt, "a", 3);
		validateStringValue(gvt, "b", "xyz");
	}
	
	// untyped static method parameter/return
	@Test
	public void smethodUntypedReturnTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "smethod_1.jul"));
		
		validateIntValue(gvt, "a", 3);
		validateStringValue(gvt, "b", "xyz");
		validateIntValue(gvt, "z", 5);
	}
	
	@Test
	public void smethodUntypedReassignTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "smethod_2.jul"));
		
		JValue a = validateUntypedValue(gvt, "a");
		JValue b = validateUntypedValue(gvt, "b");
		validateIntValue(a, 3);
		validateStringValue(b, "xyz");
	}
	
	@Test
	public void untypedVarAssignFromTypedTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_assign_1.jul"));
		
		validateIntValue(gvt, "a", 3);
		validateStringValue(gvt, "b", "xyz");
	}
	
	@Test
	public void untypedVarAssignToTypedTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_assign_2.jul"));
		
		JValue v0 = validateUntypedValue(gvt, "v0");
		JValue v1 = validateUntypedValue(gvt, "v1");
		validateIntValue(v0, 3);
		validateStringValue(v1, "xyz");
	}
	
	@Test
	public void untypedVarAssignTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_assign_3.jul"));
		
		JValue v1 = validateUntypedValue(gvt, "v1");
		JValue v2 = validateUntypedValue(gvt, "v2");
		JValue v3 = validateUntypedValue(gvt, "v3");
		validateIntValue(v1, 3);
		validateIntValue(v2, 3);
		validateIntValue(v3, 3);
		validateIntValue(gvt, "b", 3);
	}
	
	@Test
	public void objectUntypedVarAssignTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_assign_4.jul"));
		
		validateIntValue(gvt, "a1", 3);
		validateIntValue(gvt, "a2", 4);
		validateIntValue(gvt, "a3", 5);
		validateIntValue(gvt, "a4", 5);
	}
	
	@Test
	public void arrayUntypedVarAssignTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_assign_5.jul"));
		
		validateIntValue(gvt, "v0a", 1);
		validateIntValue(gvt, "v0b", 10);
		validateIntValue(gvt, "v0c", 10);
		validateIntValue(gvt, "v0d", 20);
		
		JValue v = validateUntypedValue(gvt, "v");
		validateStringValue(v, "abc");
	}
	
	@Test
	public void passNullToMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_1.jul"));
		
		validateBoolValue(gvt, "v", true);
	}
	
	@Test
	public void passNullToCtorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_2.jul"));
		
		validateBoolValue(gvt, "v", true);
	}
	
	@Test
	public void passArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "array_1.jul"));

		validateIntValue(gvt, "i", 10);
	}
	
	@Test
	public void returnArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "array_2.jul"));

		validateIntValue(gvt, "i", 10);
	}
	
	@Test
	public void typeCheckArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "array_3.jul"));

		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", false);
		
		validateIntValue(gvt, "v1", 10);
		validateIntValue(gvt, "v2", 20);
	}
}
