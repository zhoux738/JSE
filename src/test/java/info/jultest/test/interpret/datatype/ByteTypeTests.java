package info.jultest.test.interpret.datatype;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ByteTypeTests {

	private static final String FEATURE = "DataType";
	
	@Test
	public void byteDeclTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_01_decl.jul"));
		
		validateByteValue(gvt, "b0", 0);
		validateByteValue(gvt, "b1", 1);
		validateByteValue(gvt, "b_1", -1);
		validateByteValue(gvt, "b127", 127);
		validateByteValue(gvt, "b128", -128);
		validateByteValue(gvt, "b255", -1);
		validateByteValue(gvt, "b256", 0);
		validateByteValue(gvt, "b257", 1);
	}
	
	@Test
	public void byteArithTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_02_arith.jul"));
		
		validateByteValue(gvt, "r0a_1", -1);
		validateByteValue(gvt, "r0s_1", 1);
		validateByteValue(gvt, "r0m_1", 0);
		validateByteValue(gvt, "r0d_1", 0);
		validateByteValue(gvt, "r10a_1", 9);
		validateByteValue(gvt, "r10s_1", 11);
		validateByteValue(gvt, "r10m_1", -10);
		validateByteValue(gvt, "r10d_1", -10);
		validateIntValue(gvt, "i127a1", 128);
		validateByteValue(gvt, "b127a1", -128);
	}
	
	@Test
	public void byteCompareTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_03_comp.jul"));
		
		validateBoolValue(gvt, "z0a", true);
		validateBoolValue(gvt, "z0b", true);
		validateBoolValue(gvt, "z0c", true);
		validateBoolValue(gvt, "z1a", true);
		validateBoolValue(gvt, "z1b", true);
		validateBoolValue(gvt, "z1c", true);
		validateBoolValue(gvt, "z2", true);
		validateBoolValue(gvt, "z3", true);
		validateBoolValue(gvt, "z4a", false);
		validateBoolValue(gvt, "z4b", true);
		validateBoolValue(gvt, "z5a", true);
		validateBoolValue(gvt, "z5b", true);
		validateBoolValue(gvt, "z6a", true);
		validateBoolValue(gvt, "z6b", true);
	}
	
	@Test
	public void byteBitwiseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_04_bitw.jul"));
		
		validateIntValue(gvt, "i_b0_or_1", 1);
		validateIntValue(gvt, "i_b0_and_1", 0);
		validateIntValue(gvt, "i_b0_ls_1", 0);
		validateIntValue(gvt, "i_b0_rs_1", 0);
		
		validateIntValue(gvt, "i_b1_or_1", 1);
		validateIntValue(gvt, "i_b1_and_1", 1);
		validateIntValue(gvt, "i_b1_ls_1", 2);
		validateIntValue(gvt, "i_b1_rs_1", 0);
		
		validateIntValue(gvt, "i_b127_or_1", 127);
		validateIntValue(gvt, "i_b127_and_1", 1);
		validateIntValue(gvt, "i_b127_ls_1", 254);
		validateIntValue(gvt, "i_b127_rs_1", 63);
		
		validateIntValue(gvt, "i_b_1_or_1", -1); // 11111111 | 00000001 => 11111111
		validateIntValue(gvt, "i_b_1_and_1", 1); // 11111111 & 00000001 => 00000001
		validateIntValue(gvt, "i_b_1_ls_1", -2); // 11111111 > l-shift => 11111110
		validateIntValue(gvt, "i_b_1_rs_1", -1); // 11111111 > singed r-shift => 11111111
	}

	@Test
	public void byteCastingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_05_cast.jul"));
		
		validateIntValue(gvt, "i97", 97);
		validateCharValue(gvt, "c97", 'a');
		validateStringValue(gvt, "s97", "97");
		validateBoolValue(gvt, "z97", true);
		validateByteValue(gvt, "r97", 97);
		
		validateIntValue(gvt, "i97_2", 97);
		validateCharValue(gvt, "c97_2", 'a');
		validateStringValue(gvt, "s97_2", "97");
		validateBoolValue(gvt, "z97_2", true);
		validateByteValue(gvt, "r97_2", 97);
	}
	
	@Test
	public void byteIsTypeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_06_is.jul"));
		
		validateBoolValue(gvt, "z1", true);
		validateBoolValue(gvt, "z2", true);
		validateBoolValue(gvt, "z3", true);
		validateBoolValue(gvt, "z4", true);
		validateBoolValue(gvt, "z5", true);
	}
	
	@Test
	public void byteInFunctionTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_07_arg_ret.jul"));
		
		validateByteValue(gvt, "v0p1", 1);
		validateByteValue(gvt, "v127p1", -128); // overflow
		validateByteValue(gvt, "v_1p1", 0);
		validateIntValue(gvt, "i127p1a", -128);
		validateIntValue(gvt, "i127p1b", -128);
		validateIntValue(gvt, "i127p1c", -128);
	}
	
	@Test
	public void byteAsFieldTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_08_field.jul"));
		
		validateByteValue(gvt, "bmin", -128);
		validateByteValue(gvt, "bmax", 127);
	}
	
	@Test
	public void byteConcatTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_09_concat.jul"));

		validateStringValue(gvt, "b_a", "100a");
		validateStringValue(gvt, "a_b", "a100");
		validateStringValue(gvt, "a_b_a", "a100a");
		validateStringValue(gvt, "b_a_b", "100a100");
		validateStringValue(gvt, "i_b_a", "105a");
		validateStringValue(gvt, "b_a_i", "100a5");
	}
	
	@Test
	public void byteIllegalOpTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "byte_10_illegal.jul"));

		validateBoolValue(gvt, "z0", true);
		validateBoolValue(gvt, "z1", true);
		validateBoolValue(gvt, "z2", true);
	}
}
