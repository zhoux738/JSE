package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static org.junit.Assert.assertEquals;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.modulesystem.ModuleInfo;

import org.junit.Assert;
import org.junit.Test;

public class ClassAccTests {

	private static final String FEATURE = "ClassAcc";
	
	/**
	 * New up an object using parameter-less constructor.
	 */
	@Test
	public void basicObjectCreationTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_1.jul"));
		
		JValue val = gvt.getVariable("p");
		Assert.assertEquals(JValueKind.REFERENCE, val.getKind());
		ObjectValue ov = ((RefValue) val).getReferredValue();
		Assert.assertEquals(ModuleInfo.DEFAULT_MODULE_NAME + ".Person", ov.getType().getName());
		JValue ageVal = ov.getMemberValue("age");
		assertEquals(ageVal.getClass(), IntValue.class);
		IntValue ivalue = (IntValue) ageVal;
		assertEquals(32, ivalue.getIntValue());
	}
	
	@Test
	public void basicMethodCallTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "simplemath_1.jul"));
		
		validateIntValue(gvt, "x", 10);
	}
	
	@Test
	public void protectedMethodCallTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "machine_1.jul"));
		
		validateIntValue(gvt, "i", 10);
	}
	
	@Test
	public void accessInstanceMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_2.jul"));
		
		//	void setName1(string n){
		//		name = n;
		//	}
		//  p.setName1("abc");
		//  string pn1 = p.name;
		validateStringValue(gvt, "pn1", "abc");
		
		//	void setName2(string n){
		//		setName1(n);
		//	}
		//  p.setName2("def");
		//  string pn2 = p.name;
		validateStringValue(gvt, "pn2", "def");
	}
	
	// Lawyer : Person
	// Both defines getDesc();
	// Lawyer.getDesc() calls super.getDesc()
	@Test
	public void accessToParentBySuperTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_3.jul"));
		
		validateStringValue(gvt, "desc", "Lawyer: Oris");
	}
	
	// Lawyer : Employee: Person
	// Lawyer and Person define getDesc(); Employee doesn't
	// Lawyer.getDesc() calls super.getDesc()
	@Test
	public void accessToAncestorBySuperTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "person_4.jul"));
		
		validateStringValue(gvt, "desc", "Lawyer: Oris");
	}

	//  class Settings {
	//    static int GlobalFactor;
	//    static string GlobalPrefix;
	//	}
	@Test
	public void accessStaticFieldTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "settings_1.jul"));
		
		//	Settings.GlobalFactor = 5;
		//	int factor = Settings.GlobalFactor;
		validateIntValue(gvt, "factor", 5);
		
		//	Settings.GlobalPrefix = "abc";
		//	string prefix = Settings.GlobalPrefix;
		validateStringValue(gvt, "prefix", "abc");
	}

	//  class Settings {
	//    static int configure(){ return 5; }
	//    static int configureWithArg(int x){ return x; }
	//	}
	@Test
	public void invokeStaticMethodTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "settings_2.jul"));
		
		//	int confVal = Settings.configure();
		validateIntValue(gvt, "confVal", 5);
		
		//	int confVal = Settings.configureWithArg(7);
		validateIntValue(gvt, "confVal2", 7);
	}
	
	@Test
	public void accessStaticMemberTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "settings_3.jul"));
		
		//  Settings.volume = 10;
		//  int volVal = Settings.getVolume();
		validateIntValue(gvt, "volVal", 10);
		
		//	Settings.setVolume(20);
		//	int volVal2 = Settings.getVolume2(); // getVolume2() simply calls getVolume()
		validateIntValue(gvt, "volVal2", 20);
	}
	
	//	class Settings {
	//		static int volume = 100;
	//		static int getVolume(){
	//			return volume;
	//		}
	//	}
	@Test
	public void staticMemberInitTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "settings_4.jul"));
		
		//  int vv1 = Settings.volume;
		validateIntValue(gvt, "vv1", 100);
		
		//	int vv2 = Settings.getVolume();
		validateIntValue(gvt, "vv2", 100);
	}
	
	//	class Settings {
	//		static int getVolume(){
	//			return Settings.volume;
	//		}
	//	}
	@Test
	public void refStaticMemberByClassTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "settings_5.jul"));
		
		//	int vv2 = Settings.getVolume();
		validateIntValue(gvt, "vv2", 100);
	}
	
	@Test
	public void classInitTest1() throws EngineInvocationError {
 		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("class_init_1.jul", KnownJSException.RuntimeCheck, null, "static method");
	}

}
