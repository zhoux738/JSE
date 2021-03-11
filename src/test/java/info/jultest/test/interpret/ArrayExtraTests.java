package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateByteArrayValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateObjectArrayValue;

import org.junit.Assume;
import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.Commons.StringValueValidator;
import info.jultest.test.Commons.ValueValidator;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import org.junit.Assert;

public class ArrayExtraTests {

	private static final String FEATURE = "Array";
	
	@Test
	public void fillSortCopyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_extra_01.jul"));
		
		validateBoolValue(gvt, "succ1", true);
		validateBoolValue(gvt, "succ2", true);
		validateBoolValue(gvt, "succ3", true);
		validateBoolValue(gvt, "succ1a", true);
		validateBoolValue(gvt, "succ2a", true);
		validateBoolValue(gvt, "succ3a", true);
		validateBoolValue(gvt, "succ4", true);
	}
}
