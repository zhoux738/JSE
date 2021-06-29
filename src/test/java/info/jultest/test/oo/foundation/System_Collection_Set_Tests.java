package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateUntypedValue;
import static org.junit.Assert.assertEquals;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class System_Collection_Set_Tests {

	private static final String FEATURE = "Foundation/Collection";
	
	@Test
	public void basicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "set_1.jul"));
		
		validateBoolValue(gvt, "t1", true);
		validateBoolValue(gvt, "t2", true);
		validateBoolValue(gvt, "t3", true);
		validateBoolValue(gvt, "t4", true);
		validateBoolValue(gvt, "t5", true);
		validateBoolValue(gvt, "f1", false);
		validateBoolValue(gvt, "f2", false);
		validateBoolValue(gvt, "f3", false);
		validateBoolValue(gvt, "f4", false);
		validateIntValue(gvt, "i3a", 3);
		validateIntValue(gvt, "i3b", 3);
		validateIntValue(gvt, "i2", 2);
	}
	
	@Test
	public void iterateTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "set_2.jul"));
		
		validateBoolValue(gvt, "res", true);
		validateIntValue(gvt, "i0", 0);
	}
}
