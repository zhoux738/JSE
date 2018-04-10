package info.jultest.test.types;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;

import org.junit.Test;

public class ObjectTypeTests {
	
	private static final String FEATURE = "BuiltIns";
	
	/**
	 * Object o = new Object();
	 */
	@Test
	public void objectCtorTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "objecttests_1.jul"));
		
		JValue value = RefValue.dereference(gvt.getVariable("s"));
		assertEquals(value.getClass(), StringValue.class);
		StringValue svalue = (StringValue) value;
		String s = svalue.getStringValue();
		assertTrue(s.startsWith("Object@"));
	}
	
	/**
	 * string's toString()
	 */
	@Test
	public void stringToStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_3.jul"));
		
		validateStringValue(gvt, "s2", "abcdef");
	}
	
	/**
	 * array's toString()
	 */
	@Test
	public void arrayToStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "arraytests_1.jul"));
		
		JValue value = RefValue.dereference(gvt.getVariable("s"));
		assertEquals(value.getClass(), StringValue.class);
		StringValue svalue = (StringValue) value;
		String s = svalue.getStringValue();
		assertTrue(s.startsWith("[Integer]@"));
	}
	
}
