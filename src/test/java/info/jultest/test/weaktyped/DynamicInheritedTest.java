package info.jultest.test.weaktyped;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.*;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;

public class DynamicInheritedTest {

	private static final String FEATURE = "DynamicInherited";

	@Test
	public void dynSubclassTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_1.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateStringValue(gvt, "def", "def");
		validateStringValue(gvt, "abc", "abc");
		validateIntValue(gvt, "i10", 10);
		validateCharValue(gvt, "z", 'z');
	}
	
	// map initializer
	@Test
	public void dynSubclassTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_2.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateStringValue(gvt, "def", "def");
		validateStringValue(gvt, "ghi", "ghi");
		validateStringValue(gvt, "abc", "abc");
		validateIntValue(gvt, "i10", 10);
	}

	// inherited members working fine?
	@Test
	public void dynSubclassTest3() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_3.jul"));
		
		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "def", "def");
		validateStringValue(gvt, "ghi", "ghi");
		validateStringValue(gvt, "abcdefghi", "abcdefghi");
		validateBoolValue(gvt, "succ1", true);
	}
	
	// override members
	@Test
	public void dynSubclassTest4() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_4.jul"));
		
		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "xyz", "xyz");
		validateBoolValue(gvt, "succ1", true);
		validateIntValue(gvt, "i1", 1);
		validateIntValue(gvt, "i2", 2);
		validateIntValue(gvt, "i3", 3);
		validateIntValue(gvt, "i4", 4);
		validateIntValue(gvt, "i432", 432);
	}
	
	// compromise dynamic functionality: change semantics of at and initByMap
	@Test
	public void dynSubclassTest5() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_5.jul"));
		
		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "def", "def");
		validateIntValue(gvt, "inset", 1);
		validateIntValue(gvt, "inget", 2);
		validateNullValue(gvt, "result1");
		validateNullValue(gvt, "result2");
		validateNullValue(gvt, "result3");
		validateNullValue(gvt, "result4");
	}
	
	// ctor with config
	@Test
	public void dynSubclassTest6() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_6.jul"));
		
		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "def", "def");
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
	
	// member/property access across hierarchy
	@Test
	public void dynSubclassTest7() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_7.jul"));

		validateStringValue(gvt, "abc", "abc");
		validateStringValue(gvt, "def", "def");
		validateStringValue(gvt, "ghi", "ghi");
		validateIntValue(gvt, "i4", 4);
		validateIntValue(gvt, "i10", 10);
	}
	
	// type inspection
	@Test
	public void dynSubclassTest8() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "subclass_8.jul"));

		validateBoolArrayValue(gvt, "checks", new boolean[] { true, true, true, true, true });
	}
	
	// extend dynamic
	@Test
	public void dynExtensionTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "extend_1.jul"));
		
		validateStringValue(gvt, "abc", "abc");
		validateIntValue(gvt, "i10", 10);
	}
	
	@Test
	public void dynExtensionTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "extend_2.jul"));

		validateNullValue(gvt, "nl");
		validateIntValue(gvt, "i30", 30);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
}
