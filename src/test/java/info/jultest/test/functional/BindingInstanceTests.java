package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.resetTypeSystem;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateNullValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

public class BindingInstanceTests {

	private static final String FEATURE = "BindingInstance";

	@Test
	public void instMethodBindTest01() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_01.jul"));

		validateIntValue(gvt, "res", 8);
	}

	@Test
	public void instMethodBindTest02() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_02.jul"));

		validateIntValue(gvt, "res", 3);
	}
	
	@Test
	public void instMethodBindTest03() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_03.jul"));

		validateStringValue(gvt, "res", "pf10-z");
	}

	
	// bind twice from the original one. all three are unrelated
	// after binding, the original function remains same
	@Test
	public void instMethodBindTest04() throws EngineInvocationError {
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
	public void instMethodBindTest05() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_05.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "xyz", "xyz");
	}

	// bound arguments are not const but are replicated
	@Test
	public void instMethodBindTest06() throws EngineInvocationError {
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
	public void instMethodBindTest07() throws EngineInvocationError {
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
	public void instMethodBindTest08() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_08.jul"));

		validateStringValue(gvt, "ab", "ab");
		validateStringValue(gvt, "mn", "m3");
		validateStringValue(gvt, "xy", "x5");
	}

	// bind with compatible but not exact same typed args
	@Test
	public void instMethodBindTest09() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_09.jul"));

		validateStringValue(gvt, "res", "mu-mu");
	}
	
	// bind, then bind another on top of it. all three are unrelated
	@Test
	public void instMethodBindTest10() throws EngineInvocationError {
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
	public void instMethodBindTest11() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_11.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "res", "xyz");
	}
	
	// bind (in)compatible 'this' references
	@Test
	public void instMethodBindTest12() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_12.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateBoolValue(gvt, "succ2", true);
		validateBoolValue(gvt, "succ3", true);
		validateBoolValue(gvt, "succ4", true);
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
	
	// argument type incompatible
	@Test
	public void instMethodBindFailedTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fail_1.jul"));

		validateBoolValue(gvt, "succ1", true);
	}
	
	@Test
	public void instMethodBindFailedTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "fail_2.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateBoolValue(gvt, "succ2", true);
	}
}