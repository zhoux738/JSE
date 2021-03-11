package info.jultest.test.weaktyped;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateNullValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

public class UntypedArrayTests {

	private static final String FEATURE = "Untyped";
	
	@Test
	public void untypedArrayTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_array_1.jul"));
		
		validateStringValue(gvt, "str0", "a_12_obj=5");
		validateStringValue(gvt, "str1", "a_12_obj=5");
		validateStringValue(gvt, "str2", "y_13_obj=7");
		validateStringValue(gvt, "str3", "x_7_obj=6");
	}
	
	@Test
	public void untypedArrayTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_array_2.jul"));
		
		validateStringValue(gvt, "str0", "a_12_obj=5");
		validateStringValue(gvt, "str1", "a_12_obj=5");
		validateStringValue(gvt, "str2", "y_13_obj=7");
		validateStringValue(gvt, "str3", "x_7_obj=6");
		validateStringValue(gvt, "str2", "y_13_obj=7");
	}
	
	@Test
	public void untyped2DArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_array_3.jul"));
		
		validateStringValue(gvt, "sa", "a");
		validateStringValue(gvt, "sb", "b");
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateIntValue(gvt, "ires", 6);
	}
	
	@Test
	public void untypedArrayCastingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "var_array_4.jul"));
		
		validateStringValue(gvt, "sa", "a");
		validateStringValue(gvt, "sb", "b");
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateIntValue(gvt, "i1", 1);
		validateIntValue(gvt, "i2", 2);
		validateNullValue(gvt, "obj_null");
	}
}
