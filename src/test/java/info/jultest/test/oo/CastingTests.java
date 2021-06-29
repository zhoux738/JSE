package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.JIllegalCastingException;
import info.julang.typesystem.jclass.builtin.JStringType;

import org.junit.Assume;
import org.junit.Test;

public class CastingTests extends ExceptionTestsBase {

	private static final String FEATURE = "Casting";

	//	Car c2 = (Car) c; // c is Car
	@Test
	public void castToSameClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_1.jul"));
		
		validateStringValue(gvt, "n", "BYD");
	}

	//	Machine m0 = new Car("BYD"); // Machine : Car
	//  Car c0 = (Car) m0;
	@Test
	public void castToSubClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_2a.jul"));
		
		validateStringValue(gvt, "n", "BYD");
	}
	
	//	Car c0 = new Car("BYD"); // Machine : Car
	//  Machine m0 = (Car) c0;
	@Test
	public void castToParentClassTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_2b.jul"));
		
		assertException(teh, "System.UnknownMemberException");
	}
	
	//	Car c0 = new Car("BYD"); // Machine : Car
	//  Machine m0 = c0;
	@Test
	public void implicitCastToParentClassTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "implicit_cast_1.jul"));
		
		assertException(teh, "System.UnknownMemberException");
	}
	
	//	Car c0 = new Car("BYD"); // Machine : Car
	//  Machine m0 = c0;
	//  Car c1 = m0;
	@Test
	public void implicitCastToParentAndBackTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "implicit_cast_2.jul"));
		
		assertException(teh, "System.IllegalAssignmentException");
	}
	
	//	Car c0 = new Car("BYD"); // Machine : Car
	//  Machine m0 = c0;
	//  Car c1 = (Car) m0;
	@Test
	public void implicitCastToParentAndExplicitCastBackTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "implicit_cast_3.jul"));
		
		validateStringValue(gvt, "n", "car:BYD");
	}
	
	//	Car c2 = (Car) m; // m is Machine, which is not related to Car
	@Test
	public void castToOtherClassTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("cast_3.jul", new JIllegalCastingException(
			JStringType.getInstance(), JStringType.getInstance())); 
			// The types used above are not the right one, but we don't check them either.
	}
	
	@Test
	public void intToStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_4.jul"));
		
		validateStringValue(gvt, "s", "5");
	}
	
	@Test
	public void untypedToIntAndStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_5.jul"));
		
		validateIntValue(gvt, "j", 5);
		validateStringValue(gvt, "s", "5");
	}
	
	@Test
	public void castingKeepsIdentityTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_6.jul"));
		
		validateBoolValue(gvt, "res", true);
	}
	
	@Test
	public void castingNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_7.jul"));

		validateIntValue(gvt, "i1", 1);
		validateIntValue(gvt, "i2", 2);
	}
	
	@Test
	public void objToStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_8.jul"));
		
		validateStringValue(gvt, "s1", "Suzuki");
		validateStringValue(gvt, "s2", "Suzuki");
	}
	
	@Test
	public void toAnyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "cast_to_any.jul"));
		
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", true);
	}
}
