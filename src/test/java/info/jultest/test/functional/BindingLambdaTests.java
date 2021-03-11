package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.resetTypeSystem;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

public class BindingLambdaTests {

	private static final String FEATURE = "BindingLambda";

	@Test
	public void instLambdaBindTest01() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_01.jul"));

		validateIntValue(gvt, "i8", 8);
		validateIntValue(gvt, "i15", 15);
	}

	@Test
	public void instLambdaBindTest02() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_02.jul"));

		validateIntValue(gvt, "i30", 30);
		validateIntValue(gvt, "i50", 50);
		validateIntValue(gvt, "i300", 300);
		validateIntValue(gvt, "i500", 500);
		validateIntValue(gvt, "i90", 90);
		validateIntValue(gvt, "i900", 900);
		validateIntValue(gvt, "i70", 70);
		validateIntValue(gvt, "i160", 160);
		validateIntValue(gvt, "i190", 190);
	}

	@Test
	public void instLambdaBindTest03() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_03.jul"));

		validateStringValue(gvt, "res1", "pf10-z");
		validateStringValue(gvt, "res2", "pf11_y");
		validateStringValue(gvt, "res3", "pf12%x");
		validateStringValue(gvt, "acc", "-_%");
	}

	// bind twice from the original one. all three are unrelated
	// after binding, the original function remains same
	@Test
	public void instLambdaBindTest04() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_04.jul"));

		validateStringValue(gvt, "res", "pf10-z");
		validateStringValue(gvt, "res3", "pf10-z");
		validateStringValue(gvt, "res4", "pf20ax");
		validateStringValue(gvt, "res5", "pf11_z");
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i0", 0);
		validateIntValue(gvt, "i60", 60);
		validateIntValue(gvt, "i71", 71);
		validateIntValue(gvt, "i91", 91);
	}
	
	// bound 'this' is const
	@Test
	public void instLambdaBindTest05() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_05.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "xyz", "xyz");
	}

	// bound arguments are not const but are replicated
	@Test
	public void instLambdaBindTest06() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_06.jul"));

		validateIntValue(gvt, "i3a", 3);
		validateIntValue(gvt, "i3b", 3);
		validateIntValue(gvt, "t3", 3);
		validateIntValue(gvt, "t6", 6);
	}

	// bound arguments are not const but are replicated
	// bound args are replicated as usual: primitive by value, compound by ref
	// (except string).
	@Test
	public void instLambdaBindTest07() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_07.jul"));

		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "foo", "foo");
		validateStringValue(gvt, "bar1", "bar");
		validateStringValue(gvt, "bar2", "bar");
	}
	
	// bind with more args (SUCC)
	@Test
	public void instLambdaBindTest08() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_08.jul"));

		validateStringValue(gvt, "ab", "ab");
		validateStringValue(gvt, "mn", "m3");
		validateStringValue(gvt, "xy", "x5");
	}

	// bind with compatible but not exact same typed args
	@Test
	public void instLambdaBindTest09() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_09.jul"));

		validateStringValue(gvt, "res1", "mu-mu");
		validateStringValue(gvt, "res2", "mu-mu");
	}
	
	// bind, then bind another on top of it. all three are unrelated
	@Test
	public void instLambdaBindTest10() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_10.jul"));

		validateStringValue(gvt, "pf2abcx", "pf2abcx");
		validateStringValue(gvt, "pf3abcg", "pf3abcg");
		validateStringValue(gvt, "pf5abcg", "pf5abcg");
		validateStringValue(gvt, "pf7ghiz", "pf7ghiz");
		
		validateIntValue(gvt, "i2", 2);
		validateIntValue(gvt, "i5", 5);
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i17", 17);
	}

	// bind failure: null for this in bind(thisvar)
	@Test
	public void instLambdaBindTest11() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_11.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "res", "xyz");
	}
	
	// argument type incompatible
	@Test
	public void instMethodBindFailedTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_12.jul"));

		validateBoolValue(gvt, "succ1", true);
	}
	
	// bind Dynamic to 'this'
	@Test
	public void instMethodBindTest13() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_13.jul"));

		validateIntValue(gvt, "i7", 7);
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i507", 507);
		validateBoolValue(gvt, "succ1", true);
		validateNullValue(gvt, "nl");
		validateStringValue(gvt, "theval", "theval");
	}
	
	// closure
	@Test
	public void instMethodBindTest14() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_14.jul"));

		validateIntValue(gvt, "i17", 17);
		validateIntValue(gvt, "i_1", -1);
	}
}