package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;

import org.junit.Assume;
import org.junit.Test;

public class ExpressionTests {

	private static final String FEATURE = "Expression";
	
	@Test
	public void basicAddTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "add_01.jul"));
		
		validateStringValue(gvt, "s1", "abcdef");
		validateStringValue(gvt, "s2", "abc4falsep");
		validateStringValue(gvt, "s3", "ab");
		validateStringValue(gvt, "s4a", "a4");
		validateStringValue(gvt, "s4b", "4a");
		validateStringValue(gvt, "s5a", "afalse");
		validateStringValue(gvt, "s5b", "falsea");
	}
	
	@Test
	public void basicLogicalTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "logic_01.jul"));
		
		validateBoolValue(gvt, "c1", false);
		validateBoolValue(gvt, "c2", true);
		validateBoolValue(gvt, "c3", true);
		validateBoolValue(gvt, "c4", true);
		validateBoolValue(gvt, "c5", true);
		validateBoolValue(gvt, "c6", true);
		validateBoolValue(gvt, "c7", false);
		validateBoolValue(gvt, "c8", false);
	}
	
	@Test
	public void shortcutLogicalTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "logic_02.jul"));
		
		validateIntValue(gvt, "a1", 0);
		validateIntValue(gvt, "a2", 1);
		validateIntValue(gvt, "a3", 1);
		validateIntValue(gvt, "a4", 3);
		validateIntValue(gvt, "a5", 4);
		
		validateBoolValue(gvt, "res1", false);
		validateBoolValue(gvt, "res2", false);
		validateBoolValue(gvt, "res3", false);
		validateBoolValue(gvt, "res4", false);
		validateBoolValue(gvt, "res5", true);
	}
	
	@Test
	public void shortcutLogicalTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "logic_03.jul"));
		
		validateIntValue(gvt, "a1", 1);
		validateIntValue(gvt, "b1", 0);
		validateIntValue(gvt, "c1", 1);
		validateIntValue(gvt, "a2", 2);
		validateIntValue(gvt, "b2", 0);
		validateIntValue(gvt, "c2", 1);
		validateIntValue(gvt, "a3", 2);
		validateIntValue(gvt, "b3", 1);
		validateIntValue(gvt, "c3", 2);
	}
	
	@Test
	public void basicArithmeticTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "arithmetic_01.jul"));
		
		validateIntValue(gvt, "a", 10);
		validateIntValue(gvt, "b", 20);
		validateIntValue(gvt, "c", 30);
		validateIntValue(gvt, "d", 40);
	}
	
	@Test
	public void nestedArithmeticTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "arithmetic_02.jul"));
		
		validateIntValue(gvt, "a1", 7);
		validateIntValue(gvt, "b1", 9);
		validateIntValue(gvt, "c1", 7);
		validateIntValue(gvt, "a2", 3);
		validateIntValue(gvt, "b2", -3);
	}
	
	@Test
	public void advancedAddressingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "addressing_01.jul"));
		
		validateStringValue(gvt, "s0", "Toyota");
		validateStringValue(gvt, "s1", "Honda");
		validateStringValue(gvt, "s2", "num=100");
		validateStringValue(gvt, "s3", "num=100");
	}
	
	/*
	int x = 0b1010;
	int y = 0b0011;

	int ra = x & y;
	int ro = x | y;
	int rx = x ^ y;
	
	int rl = x << 2;
	int rr = x >> 2;
	*/
	@Test
	public void bitwiseTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "bitwise_01.jul"));
		
		validateIntValue(gvt, "ra", 0b1010 & 0b0011);
		validateIntValue(gvt, "ro", 0b1010 | 0b0011);
		validateIntValue(gvt, "rx", 0b1010 ^ 0b0011);
		
		validateIntValue(gvt, "rl", 0b1010 << 2);
		validateIntValue(gvt, "rr", 0b1010 >> 2);
	}
	
	@Test
	public void bitwiseCompoundTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "bitwise_02.jul"));
		
		int x = 0b1010;
		int y = 0b0011;
		int x1 = x, x2 = x, x3 = x, x4 = x, x5 = x;
		validateIntValue(gvt, "x1", x1 &= y);
		validateIntValue(gvt, "x2", x2 |= y);
		validateIntValue(gvt, "x3", x3 ^= y);
		
		validateIntValue(gvt, "x4", x4 <<= 2);
		validateIntValue(gvt, "x5", x5 >>= 2);
	}
	
	@Test
	public void bitwiseComplementTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "bitwise_03_complement.jul"));

		validateIntValue(gvt, "z", ~0b1010);
	}
	
	/*
	 * int i = 64;
	 * int s = i += 10; // 74
	 * int d = i -= 20; // 54
	 * int p = i *= 5;  // 270
	 * int q = i /= 2;  // 135
	 */
	@Test
	public void compoundOpTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "compound_01.jul"));
		
		validateIntValue(gvt, "s", 74);
		validateIntValue(gvt, "d", 54);
		validateIntValue(gvt, "p", 270);
		validateIntValue(gvt, "q", 135);
		validateIntValue(gvt, "m", 15);
		
		validateIntValue(gvt, "i", 15);
	}
	
	@Test
	public void basicCondOpTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "cond_01.jul"));
		
		validateIntValue(gvt, "x1", 20);
		validateIntValue(gvt, "x2", 30);
		validateIntValue(gvt, "x3", 20);
		validateIntValue(gvt, "x4", 30);
		
		validateIntValue(gvt, "x5", 50);
		validateIntValue(gvt, "x6", 600);
		validateIntValue(gvt, "x7", 80);
		validateIntValue(gvt, "x8", 602);
	}
	
	@Test
	public void nestedCondOpTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "cond_02.jul"));
		
		validateIntValue(gvt, "x1", 10);
		validateIntValue(gvt, "x2", 20);
		validateIntValue(gvt, "x3", 20);
		validateIntValue(gvt, "x4", 30);
	}
	
	@Test
	public void nestedCondOpTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "cond_03.jul"));
		
		validateIntValue(gvt, "x1", 11);
		validateIntValue(gvt, "x2", 21);
		validateIntValue(gvt, "x3", 21);
		validateIntValue(gvt, "x4", 31);
		
		validateIntValue(gvt, "x5", 11);
		validateIntValue(gvt, "x6", 21);
		validateIntValue(gvt, "x7", 21);
		validateIntValue(gvt, "x8", 31);
	}
	
	@Test
	public void shortcutCondOpTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "cond_04.jul"));
		
		validateIntValue(gvt, "x1", 20);
		validateIntValue(gvt, "x2", 30);
		
		validateIntValue(gvt, "x3", 10);
		validateIntValue(gvt, "x4", 20);
		validateIntValue(gvt, "x5", 20);
		validateIntValue(gvt, "x6", 30);
	}
	
	@Test
	public void nestedCondOpTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "cond_05.jul"));
		
		validateIntValue(gvt, "x1", 10);
		validateIntValue(gvt, "x2", 20);
	}
	
	@Test
	public void postIncrementTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "postinc_01.jul"));
		
		validateIntValue(gvt, "a", 7);
		validateIntValue(gvt, "b", 6);
		validateIntValue(gvt, "c", 107);
	}
	
	@Test
	public void postIncrementTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "postinc_02.jul"));
		
		validateIntArrayValue(gvt, "arr", new int[]{101,201});
	}
	
	@Test
	public void postIncrementTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "postinc_03.jul"));
		
		validateIntValue(gvt, "a", 101);
		validateIntValue(gvt, "b", 201);
	}
	
	@Test
	public void postIncrementFailTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndValidate(
			"postinc_fail_01.jul", 
			"System.Lang.IllegalOperandException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	@Test
	public void postIncrementFailTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.IMPERATIVE, FEATURE);
		runner.executeAndValidate(
			"postinc_fail_02.jul", 
			"System.Lang.IllegalOperandException", 
			null, 
			null, 
			false, 
			-1);
	}
	
	// call non-function
	// index non-indexable
	// use non-boolean as condition
	@Test
	public void sematicErrorTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "semantic_error_1.jul"));
		
		validateBoolValue(gvt, "b0", true);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
}
