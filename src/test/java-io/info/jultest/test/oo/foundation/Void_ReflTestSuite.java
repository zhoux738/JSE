package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateHostedValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.EFCommons.runViaFactory;
import static info.jultest.test.EFCommons.validateIntValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.typesystem.jclass.jufc.System.ScriptType;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

import java.io.IOException;

import org.junit.Test;

public class Void_ReflTestSuite extends ExceptionTestsBase {

	private static final String FEATURE = "Reflection/Void";
	
	// Return type is void
	@Test
	public void voidTest1() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "void_1.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "r0", true);
	}
	
	// Call method returning void
	@Test
	public void voidTest2() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "void_2.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		
		EFCommons.validateBoolValue(gvt, "r0", true);
		EFCommons.validateBoolValue(gvt, "r1", true);
	}

	// Return void that is returned from a void-returning method called via reflection
	@Test
	public void voidTest3() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "void_3.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateIntValue(gvt, "value", 10);
	}
	
	// Apply various reflection methods on Void
	@Test
	public void voidTest4() throws EngineInvocationError, IOException {		
		IExtEngineRuntime rt = runViaFactory(Commons.Groups.OO, FEATURE, "void_4.jul", Commons.SRC_REPO_ROOT);
		IExtVariableTable gvt = rt.getGlobalVariableTable();

		EFCommons.validateStringValue(gvt, "s0", "Void");
		EFCommons.validateStringValue(gvt, "s1", "Void");
		
		EFCommons.validateBoolValue(gvt, "b0", false);
		EFCommons.validateBoolValue(gvt, "b1", false);
		EFCommons.validateBoolValue(gvt, "b2", false);
		EFCommons.validateBoolValue(gvt, "b3", false);
		EFCommons.validateBoolValue(gvt, "b4", true);
		EFCommons.validateBoolValue(gvt, "b5", true);
		EFCommons.validateBoolValue(gvt, "b6", true);

		EFCommons.validateIntValue(gvt, "i1", 0);
		EFCommons.validateIntValue(gvt, "i2", 0);
		EFCommons.validateIntValue(gvt, "i3", 0);
	}
	
}
