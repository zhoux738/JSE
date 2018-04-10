package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static org.junit.Assert.assertEquals;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class NullTests {

	private static final String FEATURE = "Null";
	
	// Assign null to a ref value, then dereferencing it should raise exception.
	@Test
	public void assignNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_1.jul"));
		
		// string s = p.name;
		validateStringValue(gvt, "s", "Rudolf");
		
		// Person p = null;
		// string s2 = p.name;
		assertEquals("System.NullReferenceException", teh.getTypeName());
	}
	
	// Person p1, p2;
	// p1 = p2 = null;
	// res = p1 == p2;
	@Test
	public void compareRefTypANullToRefTypeANullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_2.jul"));
		
		validateBoolValue(gvt, "res", true);
	}
	
	//	Person p1 = null;
	//	bool res1 = p1 == null;
	//	bool res2 = null == p1;
	@Test
	public void compareRefTypANullAndGenericNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_3.jul"));
		
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", true);
	}
	
	//	Person p1 = null;
	//	Car c1 = null;
	//	bool res = p1 == c1;
	@Test
	public void compareRefTypANullToRefTypeBNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_4.jul"));
		
		validateBoolValue(gvt, "res", false);
	}
	
	//	bool res1 = null == null;
	//	bool res2 = null != null;
	@Test
	public void compareGenericNullAndGenericNullTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_5.jul"));
		
		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", false);
	}
	
	@Test
	public void passNullValueAsArgTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_6.jul"));
		
		validateIntValue(gvt, "r", 5);
	}
	
	@Test
	public void returnNullValueTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_7.jul"));
		
		validateIntValue(gvt, "r", 5);
	}
	
	@Test
	public void passNullValueAsArgTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_8.jul"));
		
		validateIntValue(gvt, "r1", 10);
		validateIntValue(gvt, "r2", -1);
	}
	
	@Test
	public void nullTypeChecklTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "null_9.jul"));
		
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", false);
		validateBoolValue(gvt, "b3", false);
		validateBoolValue(gvt, "mark", true);
	}
}
