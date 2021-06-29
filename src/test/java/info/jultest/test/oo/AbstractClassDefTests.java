package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assume;
import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.jultest.test.module.TestLoadingState;
import info.julang.dev.GlobalSetting;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.loading.ClassLoadingException;

public class AbstractClassDefTests extends ClassTestBase {

	private static final String FEATURE = "ClassDef";
	
	/*
	 * abstract class Item {
	 *     abstract string doWork();
	 * }
	 */
	@Test
	public void basicAbsClassDefTest() throws EngineInvocationError {		
		EngineRuntime rt = SimpleEngineRuntime.createDefault();
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "item_1.jul"));
		
		JClassType ctype = assertClassTypeDefined(rt, ModuleInfo.DEFAULT_MODULE_NAME + ".Item");
		
		validateTypeDefinition(
			ctype,createMembers(true, new MemberInfo[]{
				MemberInfo.createMethod("doWork").isAbstract(true),
			}));
	}
	
	/*
	 * Cannot instantiate an abstract class.
	 */
	@Test
	public void instantiateAbsClassTest() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("item_2.jul", new RuntimeCheckException(null));
	}
	
	/*
	 * class Car : Item
	 * Car is concrete, Item abstract. Car implements the abstract method defined by Item.
	 */
	@Test
	public void absClassInheritanceTest() throws EngineInvocationError {		
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "item_3.jul"));
		
		validateStringValue(gvt, "res", "Car");
	}
	
	/*
	 * class Car : Item
	 * Car is concrete, Item abstract. Car doesn't implement the abstract method defined by Item.
	 */
	@Test
	public void absClassInheritanceTest2() throws Exception {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("item_4.jul", new ClassLoadingException(new TestLoadingState(ModuleInfo.DEFAULT_MODULE_NAME + ".Car", null, false)));
	}
	
	@Test
	public void absClassInheritanceTest3() throws Exception {	
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("item_5.jul", new ClassLoadingException(new TestLoadingState(ModuleInfo.DEFAULT_MODULE_NAME + ".Car", null, false)));
	}
	
	@Test
	public void absClassInheritanceTest4() throws Exception {	
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("item_6.jul", new ClassLoadingException(new TestLoadingState(ModuleInfo.DEFAULT_MODULE_NAME + ".Car", null, false)));
	}
}
