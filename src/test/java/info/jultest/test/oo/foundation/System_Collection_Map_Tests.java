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

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

public class System_Collection_Map_Tests {

	private static final String FEATURE = "Foundation/Collection";
	
	// put (overwrite), get, remove, size
	@Test
	public void mapBasicOpWithStringKeyTest() throws EngineInvocationError {
		mapBasicOpWithKeyTest("map_1.jul");
	}
	
	// put (overwrite), get, remove, size
	@Test
	public void mapBasicOpWithVarObjectKeyTest() throws EngineInvocationError {
		mapBasicOpWithKeyTest("map_2.jul");
	}
	
	// put (overwrite), get, remove, size
	@Test
	public void mapBasicOpWithObjectKeyTest() throws EngineInvocationError {
		mapBasicOpWithKeyTest("map_3.jul");
	}
	
	// put (overwrite), get, remove, size
	@Test
	public void mapBasicOpWithIncongruousKeysTest() throws EngineInvocationError {
		mapBasicOpWithKeyTest("map_4.jul");
	}
	
	@Test
	public void mapAccessByIndexerTest() throws EngineInvocationError {
		mapBasicOpWithKeyTest("map_5.jul");
	}
	
	// iterate over a Map
	@Test 
	public void mapIterationTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, null, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_6.jul"));
		
		List<String> list = new LinkedList<String>();
		for(int i=0;i<3;i++){
			list.add(((StringValue) RefValue.dereference(gvt.getVariable("s" + i))).getStringValue());
		}
		list.sort(new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		
		assertEquals(list.get(0), "100:3");
		assertEquals(list.get(1), "b:xyz");
		assertEquals(list.get(2), "true:true");
	}
	
	// override equals(var) and hashCode()
	@Test
	public void overrideKeyMethodsTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_7.jul"));
		
		validateIntValue(gvt, "size1", 2);
		validateIntValue(gvt, "size2", 1);
		validateIntValue(gvt, "size3", 1);
		validateStringValue(gvt, "val", "obj2");
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
	}
	
	// override equals(var) and hashCode()
	@Test
	public void overrideKeyMethodsTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_8.jul"));
		
		validateIntValue(gvt, "size1", 1);
		validateIntValue(gvt, "size0", 0);
		validateStringValue(gvt, "val", "obj2");
	}
	
	// new Map() { ... }
	@Test
	public void initByMapTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_9.jul"));
		
		validateIntValue(gvt, "i37", 37);
		validateIntValue(gvt, "i37_2", 37);
		validateIntValue(gvt, "i41", 41);
		validateIntValue(gvt, "i100", 100);
		validateIntValue(gvt, "i123", 123);
		validateIntValue(gvt, "i150", 150);
		validateStringValue(gvt, "xyz", "xyz");
		validateStringValue(gvt, "abc", "abc");
	}
	
	@Test
	public void nullKeyTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_null_key.jul"));

		validateIntValue(gvt, "count", 1);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
	}
	
	@Test
	public void getEntriesTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_iter_1.jul"));
		
		validateStringArrayValue(gvt, "all", new String[] {
			"A",
			"1",
			"c"
		});
	}
	
	@Test
	public void getKeysTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "map_iter_2.jul"));
		
		validateStringArrayValue(gvt, "all", new String[] {
			"A",
			"1",
			"c"
		});
	}
	
	private void mapBasicOpWithKeyTest(String fileName) throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, null, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, fileName));
		
		// put, put, put
		JValue a = validateUntypedValue(gvt, "a");
		JValue b = validateUntypedValue(gvt, "b");
		JValue c = validateUntypedValue(gvt, "c");
		validateIntValue(a, 3);
		validateStringValue(b, "xyz");
		validateBoolValue(c, true);
		
		// size
		validateIntValue(gvt, "size1", 3);
		
		// put
		JValue d = validateUntypedValue(gvt, "d");
		validateStringValue(d, "gamma");
		
		// put, remove
		JValue e1 = validateUntypedValue(gvt, "e1");
		validateStringValue(e1, "epsilon");
		JValue nex = validateUntypedValue(gvt, "nex");
		validateBoolValue(nex, true);
		
		// size
		validateIntValue(gvt, "size2", 4);
	}
}
