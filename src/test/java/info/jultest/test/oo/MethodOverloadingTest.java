package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Assume;
import org.junit.Test;

public class MethodOverloadingTest extends ExceptionTestsBase {

	private static final String FEATURE = "Overloading";
	
	@Test
	public void basicOverloadingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_1.jul"));

		validateIntValue(gvt, "speed11", 50);
		validateIntValue(gvt, "speed12", 75);
		validateIntValue(gvt, "speed21", 100);
		validateIntValue(gvt, "speed22", 50);
	}
	
	@Test
	public void basicOverloadingTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_2.jul"));

		validateIntValue(gvt, "speed1", 50);
		validateIntValue(gvt, "speed2", 100);
	}
	
	@Test
	public void basicOverloadingTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_3.jul"));

		validateIntValue(gvt, "speed1", 10);
		validateIntValue(gvt, "speed2", 20);
		validateIntValue(gvt, "speed3", 30);
	}
	
	@Test
	public void basicOverloadingTest4() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_4.jul"));

		validateIntValue(gvt, "speed1", 10);
		validateIntValue(gvt, "speed2", 20);
		validateIntValue(gvt, "speed3", 30);
		validateIntValue(gvt, "speed4", 40);
	}
	
	@Test
	public void basicOverloadingTest5() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_5.jul"));

		validateIntValue(gvt, "speed1", 10);
		validateIntValue(gvt, "speed2", 20);
		validateIntValue(gvt, "speed3", 30);
		validateIntValue(gvt, "speed4", 40);
	}
	
	@Test
	public void untypedOverloadingTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_6.jul"));
		
		validateStringValue(gvt, "s", "hello");
		validateIntValue(gvt, "i", 5);
	}
	
	@Test
	public void rtErrorOverloadingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_rt_error_1.jul"));
	}
	
	@Test
	public void illegalOverloadingTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_illegal_1.jul"));
		
		assertException(teh, "System.ClassLoadingException");
	}
	
	@Test
	public void illegalOverloadingTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_illegal_2.jul"));
		
		assertException(teh, "System.ClassLoadingException");
	}
	
	@Test
	public void illegalOverloadingTest3() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_illegal_3.jul"));
		
		assertException(teh, "System.ClassLoadingException");
	}
	
	@Test
	public void illegalOverloadingTest4a() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_illegal_4a.jul"));
		
		assertException(teh, "System.ClassLoadingException");
	}
	
	@Test
	public void illegalOverloadingTest4b() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_illegal_4b.jul"));
		
		assertException(teh, "System.ClassLoadingException");
	}
	
	@Test
	public void staticOverloadingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "overload_static_1.jul"));

		validateIntValue(gvt, "s200", 200);
	}
}
