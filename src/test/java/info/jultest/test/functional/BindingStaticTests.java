package info.jultest.test.functional;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class BindingStaticTests {

	private static final String FEATURE = "BindingStatic";

	@Test
	public void staticMethodBindTest01() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_01.jul"));

		validateIntValue(gvt, "res", 8);
	}

	@Test
	public void staticMethodBindTest02() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_02.jul"));

		validateIntValue(gvt, "res", 3);
	}

	@Test
	public void staticMethodBindTest03() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_03.jul"));

		validateStringValue(gvt, "res", "pf10-z");
	}
	
	// bind twice from the original one. all three are unrelated
	// after binding, the original function remains same
	@Test
	public void staticMethodBindTest04() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_04.jul"));

		validateStringValue(gvt, "res", "pf10-z");
		validateStringValue(gvt, "res2", "pf10-z");
		validateStringValue(gvt, "res3", "pf10-z");
		validateStringValue(gvt, "res4", "pf20ax");
		validateStringValue(gvt, "res5", "pf11_z");
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i20", 20);
		validateIntValue(gvt, "i60", 60);
		validateIntValue(gvt, "i80", 80);
		validateIntValue(gvt, "i91", 91);
	}
	
	// bound 'this' is const
	@Test
	public void staticMethodBindTest05() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_05.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "xyz", "xyz");
	}

	// bound arguments are not const but are replicated
	@Test
	public void staticMethodBindTest06() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_06.jul"));

		validateIntValue(gvt, "i1", 3);
		validateIntValue(gvt, "i2", 3);
	}

	// bound arguments are not const but are replicated
	// bound args are replicated as usual: primitive by value, compound by ref
	// (except string).
	@Test
	public void staticMethodBindTest07() throws EngineInvocationError {
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
	public void staticMethodBindTest08() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_08.jul"));

		validateStringValue(gvt, "ab", "ab");
		validateStringValue(gvt, "mn", "mn");
		validateStringValue(gvt, "xy", "xy");
	}

	// bind with compatible but not exact same typed args
	@Test
	public void staticMethodBindTest09() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_09.jul"));

		validateStringValue(gvt, "res", "abc-xyz");
	}

	// bind, then bind another on top of it. all three are unrelated
	@Test
	public void staticMethodBindTest10() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_10.jul"));

		validateStringValue(gvt, "pf10defx", "pf10defx");
		validateStringValue(gvt, "pf10abcy", "pf10abcy");
		validateStringValue(gvt, "pf10abcg", "pf10abcg");
		validateStringValue(gvt, "pf11ghiz", "pf11ghiz");
	}

	// bind failure: null for this in bind(thisvar)
	@Test
	public void staticMethodBindTest11() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_11.jul"));

		validateBoolValue(gvt, "succ1", true);
		validateStringValue(gvt, "res", "xyz");
	}

	// bind failure: type incompatible
	@Test
	public void staticMethodBindTest12() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.FUNCTIONAL, FEATURE, "func_12.jul"));

		validateBoolValue(gvt, "succ1", true);
	}
}