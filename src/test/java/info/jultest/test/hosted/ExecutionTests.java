package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateNullValue;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.RuntimeCheckException;

import org.junit.Test;

public class ExecutionTests {
	
	private static final String FEATURE = "Execution";
	
	// Map info.jultest.test.hosted.MyClass1
	@Test
	public void emptyClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_1.jul"));
		
		validateBoolValue(gvt, "succ", true);
	}
	
	// Map info.jultest.test.hosted.MyClass2
	@Test
	public void simpleStatelessClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_2.jul"));

		validateIntValue(gvt, "i1", 5);
		validateIntValue(gvt, "i2", -1);
		validateByteValue(gvt, "b1", 1);
		validateByteValue(gvt, "b2", 2);
		validateCharValue(gvt, "c1", 'c');
		validateCharValue(gvt, "c2", 'z');
		validateBoolValue(gvt, "z1", false);
		validateBoolValue(gvt, "z2", true);
		validateStringValue(gvt, "s1", "hello");
		validateStringValue(gvt, "s2", "world");
	}
	
	// Map info.jultest.test.hosted.Node
	@Test
	public void selfReferencingClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_3.jul"));

		validateIntValue(gvt, "total", 6);
		validateIntValue(gvt, "total2", 4);
	}
	
	// Map info.jultest.test.hosted.MyClass4
	// Array value is passed by value for each element.
	@Test
	public void arrayTypeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_4.jul"));

		// The original shall remain unchanged
		validateIntArrayValue(gvt, "iarr", new int[]{1,2,3});
		// The returned reflects the latest changes
		validateIntArrayValue(gvt, "iarr2", new int[]{6,7,8});
	}
	
	// Map info.jultest.test.hosted.MyClass5
	// Array value is passed by value for each element.
	@Test
	public void twoDimArrayTypeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_5.jul"));

		validateStringValue(gvt, "s1", "1234");
		validateStringValue(gvt, "s2", "1z2z3z4z");
	}
	
	// Map info.jultest.test.hosted.Node/Clustor
	@Test
	public void crossReferencingClassTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_6.jul"));

		validateIntValue(gvt, "t", 10);
	}
	
	// Map info.jultest.test.hosted.Node/Clustor
	@Test
	public void crossReferencingClassTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_7.jul"));

		validateIntValue(gvt, "t", 10);
	}
	
	// Map info.jultest.test.hosted.NodeBox
	// If the return type has not been mapped, cannot assign it anywhere
	@Test
	public void returnUnmappedClassTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.HOSTING, FEATURE);
		runner.executeAndExpect("exec_8a.jul", new RuntimeCheckException(""));
	}

	// Map info.jultest.test.hosted.NodeBox
	@Test
	public void returnMappedClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_8b.jul"));

		validateIntValue(gvt, "v", 7);
	}

	// Map info.jultest.test.hosted.NodeBox
	@Test
	public void passAndReturnNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "exec_9.jul"));

		validateNullValue(gvt, "o");
	}
}
