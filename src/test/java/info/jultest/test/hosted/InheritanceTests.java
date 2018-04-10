package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateByteValue;
import static info.jultest.test.Commons.validateCharValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntArrayValue;

import org.junit.Assert;
import org.junit.Test;

import info.jultest.test.AssertHelper;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;

public class InheritanceTests {
	
	private static final String FEATURE = "ClassDef";
	
	@Test
	public void inheritanceTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_inh_1.jul"));

		validateIntValue(gvt, "v1", 50);
		validateIntValue(gvt, "v2", 0);
		validateStringValue(gvt, "s", "veh:AA");
	}
	
	@Test
	public void inheritanceTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_inh_2.jul"));

		String str = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(str, 
			"System.ClassLoadingException", "The inheritance of mapped type doesn't align with their platform counterparts");
	}
	
	// Mirror platform type hierarchy: A->B->I
	@Test
	public void inheritanceTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "obj_inh_3.jul"));

		validateIntValue(gvt, "v", 60);
	}
}
