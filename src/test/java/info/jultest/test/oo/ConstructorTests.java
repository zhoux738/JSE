package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class ConstructorTests {

	private static final String FEATURE = "Constructor";
	
	/*
	 * 	Person(int age){
	 *	  this.age = age;
	 *	}
	 */
	@Test
	public void basicConstructorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ctor_1.jul"));
		
		//	int age = p.age;
		validateIntValue(gvt, "age", 32);
	}
	
	/*
	 * Employee(int age, string job) : super(age) {
	 *	 this.job = job;
	 * }
	 */
	@Test
	public void superConstructorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ctor_2.jul"));
		
		//	int age = p.age;
		validateIntValue(gvt, "age", 32);
		validateStringValue(gvt, "job", "Programmer");
	}
	
	/*
	 * 	Person(string name) : this(name, 100) {
	 * 
	 *  }
	 *  
	 *  Person(string name, int id){
	 *	  this.name = name;
	 *	  this.id = id;
	 *  }
	 */
	@Test
	public void thisConstructorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ctor_3.jul"));
		
		validateIntValue(gvt, "id", 100);
		validateStringValue(gvt, "name", "Tyler");
	}
	
	/*
	 * super and this
	 */
	@Test
	public void thisAndSuperConstructorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ctor_4.jul"));
		
		validateIntValue(gvt, "age", 32);
		validateStringValue(gvt, "job", "Programmer");
		validateBoolValue(gvt, "legal", true);
	}
	
	/*
	 * default super
	 */
	@Test
	public void defaultSuperTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "ctor_5.jul"));
		
		validateIntValue(gvt, "age", 34);
		validateStringValue(gvt, "sp", "human");
	}

	/*
	 * default super failed
	 */
	@Test
	public void defaultSuperFailedTest() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndValidate(
			"ctor_6.jul", 
			"System.Lang.RuntimeCheckException", 
			null, 
			null, 
			false, 
			21);
	}
	
}
