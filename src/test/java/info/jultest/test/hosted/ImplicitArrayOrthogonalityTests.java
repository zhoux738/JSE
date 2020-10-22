package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import org.junit.Assert;

// The purpose of this test suite is to verify that for all operations against 
// primitive-arrays, both JSE array and the platform array can be used.
public class ImplicitArrayOrthogonalityTests {
	
	private static final String FEATURE = "Implicit";
	
	// Basic tests
	@Test
	public void platformArrayTypeTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {1, 2, 3};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_adv_1.jul"));
		
		Assert.assertEquals(10, arr[0]);
		Assert.assertEquals(2, arr[1]);
		
		validateBoolValue(gvt, "c1", true); // value before assignment
		validateBoolValue(gvt, "c2", true); // value after assignment
	}
	
	// variable, function arg and return
	@Test
	public void useImplicitPrimitiveArrayAsJSEArrayTest1() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {0, 1, 2, 3};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_adv_2.jul"));
		
		org.junit.Assert.assertArrayEquals(new int[] {0, 1000, 2000, 3000}, arr);
	}
	
	// class member, ctor arg, method arg and return
	@Test
	public void useImplicitPrimitiveArrayAsJSEArrayTest2() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {0, 1};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_adv_3.jul"));
		
		org.junit.Assert.assertArrayEquals(new int[] {50, 100}, arr);
	}

	// array copy
	@Test
	public void arrayFunctionsTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {0, 1, 2, 3, 4};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_adv_4.jul"));
		
		org.junit.Assert.assertArrayEquals(new int[] {10, 20, 30, 3, 4}, arr);
		validateIntArrayValue(gvt, "brr", new int[] {0, 1, 2, 3, 4});
	}

	// use IIterable, IIndexable API explicitly
	@Test
	public void arrayInterfacesTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {0, 1, 2, 3, 4};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_adv_5.jul"));

		org.junit.Assert.assertArrayEquals(new int[] {0, 1, 20, 3, 4}, arr);
		validateIntValue(gvt, "e1", 1);
		validateIntValue(gvt, "en", 4);
	}
}
