package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.KnownJSException;

import org.junit.Test;

public class ExtensionFailureTests {

	private static final String FEATURE = "Extension";
	
	@Test
	public void unsatisfyingExtensionTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("extfail_01.jul", "System.UnknownMemberException", 34, null, -1);
	}
	
	@Test
	public void callExtWhilePassingThisTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		// 0.1.28 message
		// Cannot find an overloaded version that accepts arguments of type (<default>.MyCls) (occurred when calling method "getVal")
		// 0.1.32 message
		// Wrong number of arguments when calling getVal.
		runner.executeAndExpect("extfail_03.jul", KnownJSException.RuntimeCheck, null, "Wrong number of arguments", "getVal");
	}
	
	@Test
	public void callExtWithoutUsingAddressingSyntaxTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extfail_04.jul"));

		validateBoolValue(gvt, "succ", true);
		validateBoolValue(gvt, "failed", true);
	}
	
	@Test
	public void callExtOverloadedWithInherentMemberTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extfail_05.jul"));

		validateIntValue(gvt, "r5", 5);
	}
	
	@Test
	public void callExtOverloadedWithInherentMemberTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extfail_06.jul"));

		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", true);
	}
	
	@Test
	public void callExtOverloadedWithInherentMemberTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "extfail_07.jul"));

		validateIntValue(gvt, "r10", 10);
		validateIntValue(gvt, "r20", 20);
	}
}
