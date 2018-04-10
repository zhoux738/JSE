package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;

import java.io.IOException;

import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

public class System_Collection_Queue_Tests {

	private static final String FEATURE = "Foundation/Collection";
	
	@Test
	public void enqueueDequeueTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "queue_1.jul"));
		
		validateIntValue(gvt, "v0", 3);
		validateStringValue(gvt, "v1", "abc");
		validateNullValue(gvt, "v2");
		validateIntValue(gvt, "size1", 2);
		validateIntValue(gvt, "size2", 0);
		validateIntValue(gvt, "size3", 0);
	}
	
	@Test
	public void enqueueDequeueBlockingTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "queue_2.jul"));
		
		validateIntValue(gvt, "sum", 500);
	}
}
