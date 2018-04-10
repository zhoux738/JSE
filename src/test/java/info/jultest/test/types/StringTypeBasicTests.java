package info.jultest.test.types;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateBoolValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class StringTypeBasicTests {

	private static final String FEATURE = "BuiltIns";
	
	@Test
	public void stringImmutabilityTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_4_immutability_a.jul"));
		
		validateStringValue(gvt, "s1", "xyz");
		validateStringValue(gvt, "s2", "abc");
		
		validateStringValue(gvt, "s3", "abc");
		validateStringValue(gvt, "s4", "uvw");
		validateStringValue(gvt, "s5", "abc");
		validateStringValue(gvt, "s6", "abc");
	}
	
	@Test
	public void stringImmutabilityTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_4_immutability_b.jul"));
		
		validateStringValue(gvt, "s1", "xyz");
		validateStringValue(gvt, "s2", "abc");
		validateStringValue(gvt, "s3", "uvw");
		validateStringValue(gvt, "s4", "xyz");
		validateStringValue(gvt, "s5", "abc");
		validateStringValue(gvt, "s6", "uvw");		
		validateStringValue(gvt, "s7", "def");
		validateStringValue(gvt, "s8", "ghi");
	}
	
	@Test
	public void stringImmutabilityTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_4_immutability_c.jul"));
		
		validateStringValue(gvt, "s1", "xyz");
		validateStringValue(gvt, "s2", "abc");
		
		validateStringValue(gvt, "s3", "abc");
		validateStringValue(gvt, "s4", "uvw");
		validateStringValue(gvt, "s5", "abc");
		validateStringValue(gvt, "s6", "abc");
	}
	
	@Test
	public void stringImmutabilityTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_4_immutability_d.jul"));
		
		validateStringValue(gvt, "r1", "a");
		validateStringValue(gvt, "r2", "a");
	}
	
	@Test
	public void stringNullabilityTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_5_nullability_a.jul"));
		
		validateStringValue(gvt, "s1", "abc");
		validateNullValue(gvt, "s2");
		validateStringValue(gvt, "s3", "def");
		validateNullValue(gvt, "s4");
	}
	
	@Test
	public void stringConcatTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_6_concatenation_a.jul"));
		
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", true);
		validateBoolValue(gvt, "res3", true);
	}
	
	@Test
	public void stringConcatTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_6_concatenation_b.jul"));

		validateStringValue(gvt, "abc_t_x_zero_pi", "abctruex03.14");
		validateStringValue(gvt, "f_abc_five_x_e",  "falseabc5x2.72");
		validateStringValue(gvt, "five_abc_y_abc_e_abc_abc",  "5abcyabc2.72abcabc");
	}
	
	@Test
	public void stringEqualsTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_7_equality.jul"));
		
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
	}
}
