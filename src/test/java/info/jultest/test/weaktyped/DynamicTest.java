package info.jultest.test.weaktyped;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.*;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;

public class DynamicTest {

	private static final String FEATURE = "Dynamic";

	@Test
	public void dynamicBasicTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "basic_1.jul"));
		
		validateStringValue(gvt, "s", "xyz");
		validateIntValue(gvt, "i", 10);
		validateCharValue(gvt, "z", 'z');
		validateIntValue(gvt, "count", 2);
	}

	@Test
	public void dynamicBasicTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "basic_2.jul"));
		
		validateStringValue(gvt, "s", "xyz");
		validateIntValue(gvt, "i", 10);
		validateCharValue(gvt, "z", 'z');
		validateIntValue(gvt, "count", 2);
	}
	
	@Test
	public void dynamicInitTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "init_1.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateCharValue(gvt, "z", 'z');
		validateCharValue(gvt, "x", 'x');
	}
	
	@Test
	public void dynamicInitTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "init_2.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateStringValue(gvt, "xyz_2", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i10_2", 10);
		validateCharValue(gvt, "c", 'c');
		validateCharValue(gvt, "c_2", 'c');
		validateCharValue(gvt, "x", 'x');
	}
	
	@Test
	public void dynamicInitTest3() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "init_3.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateCharValue(gvt, "z", 'z');
		validateCharValue(gvt, "c", 'c');
	}
	
	@Test
	public void dynamicInitTest4() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "init_4.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateCharValue(gvt, "z", 'z');
	}
	
	@Test
	public void dynamicBadInitTest1() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.SCRIPTING, FEATURE);
		runner.executeAndValidate(
			"init_bad_1.jul", 
			"System.Lang.BadSyntaxException", 
			null, 
			null, 
			false, 
			3);
	}
	
	@Test
	public void dynamicBadInitTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "init_bad_2.jul"));
		
		validateStringValue(gvt, "world", "world");
		validateStringValue(gvt, "all", "all");
		validateBoolValue(gvt, "f1", true);
	}
	
	@Test
	public void dynamicIterateTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "iter_1.jul"));
		
		validateStringValue(gvt, "str1", "a=xyz;b=10;c=false;");
		validateStringValue(gvt, "str2", "a=xyz;b=10;c=false;");
		validateBoolValue(gvt, "succ1", true);
		validateIntValue(gvt, "i", 3);
	}
	
	@Test
	public void dynamicAccessTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "access_1.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateBoolValue(gvt, "succ", true);
		validateStringValue(gvt, "uvw", "uvw");
	}
	
	@Test
	public void dynamicAccessTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "access_2.jul"));
		
		validateBoolValue(gvt, "succ1", true);
		validateBoolValue(gvt, "succ2", true);
	}
	
	@Test
	public void dynamicAccessTest3() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "access_3.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateIntValue(gvt, "i10", 10);
		validateBoolValue(gvt, "succ1", true);
		validateBoolValue(gvt, "succ2", true);
	}
	
	// Bind lambda literal
	@Test
	public void dynamicBindTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "bind_1.jul"));
		
		validateStringValue(gvt, "xyz", "xyz");
		validateStringValue(gvt, "xyz_1", "xyz_1");
	}
	
	// without autobind == true, won't bind any function except for lambda literal
	@Test
	public void dynamicBindTest2() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "bind_2.jul"));

		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
		validateIntValue(gvt, "i20", 20);
	}
	
	// autobind == true
	@Test
	public void dynamicBindTest3() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "bind_3.jul"));

		validateIntValue(gvt, "i10", 10);
		validateIntValue(gvt, "i15", 15);
		validateIntValue(gvt, "i30", 30);
		validateIntValue(gvt, "i100", 100);
		validateStringValue(gvt, "xyz", "xyz");
	}
	
	// re-bind in a loop
	@Test
	public void dynamicBindTest4() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.SCRIPTING, FEATURE, "bind_4.jul"));

		validateIntValue(gvt, "i37", 37);
		validateIntValue(gvt, "i55", 55);
	}
}
