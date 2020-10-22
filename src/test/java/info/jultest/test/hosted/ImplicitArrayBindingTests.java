package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.resetTypeSystem;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateBoolValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import org.junit.Assert;

public class ImplicitArrayBindingTests {
	
	private static final String FEATURE = "Implicit";
	
	// Basic tests
	@Test
	public void intArrayBindingTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {1, 2, 3, 40, 50};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_1.jul"));
		
		Assert.assertEquals(5, arr[0]);
		Assert.assertEquals(2, arr[1]); // unchanged
		Assert.assertEquals(10, arr[2]);
		Assert.assertEquals(50, arr[3]); // swapped with arr[4]
		Assert.assertEquals(40, arr[4]); // swapped with arr[3]
		
		validateIntValue(gvt, "a0", 1); // value before assignment
		validateIntValue(gvt, "a2", 10); // value after assignment
	}
	
	// Test iterablility
	@Test
	public void intArrayBindingForEachTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {-7, 1, 5, 13};
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_2.jul"));
		
		int expected = 1;
		for (int i : arr) {
			expected *= i;
		}
		
		validateIntValue(gvt, "product", expected);
	}
	
	// Test length field
	@Test
	public void intArrayBindingForTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[] arr = new int[] {-7, 1, 5, 13};
		int[] arrExp = new int[arr.length];
		for (int i = 0; i < arrExp.length; i++) {
			arrExp[i] = arr[i] * 2;
		}
		
		engine.getContext().addBinding("arr", new ObjectBinding(arr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_3.jul"));
		
		org.junit.Assert.assertArrayEquals(arrExp, arr);
		validateIntValue(gvt, "len", 4);
	}
	
	@Test
	public void stringArrayBindingTest() throws EngineInvocationError {
		resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		String[] arr = new String[] {"abc", "", "00xyz"};
		int len = arr.length;
		String[] arrExp = new String[len];
		for (int i = 0; i < len; i++) {
			String s = arr[i];
			arrExp[i] = s + s.length();
		}
		
		engine.getContext().addBinding("arr", new ObjectBinding(arr));		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_4.jul"));
		
		validateIntValue(gvt, "length", arr.length);
		validateBoolValue(gvt, "jstr", true);
		org.junit.Assert.assertArrayEquals(arrExp, arr);
	}
	
	@Test
	public void objectArrayBindingTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		ImplicitBindingTests.IBTClass1[] arr = new ImplicitBindingTests.IBTClass1[] {
			new ImplicitBindingTests.IBTClass1(10),
			new ImplicitBindingTests.IBTClass1(20),
			new ImplicitBindingTests.IBTClass1(30)};
		
		engine.getContext().addBinding("arr", new ObjectBinding(arr));		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_5.jul"));
		
		validateIntValue(gvt, "length", arr.length);
		org.junit.Assert.assertEquals(arr[0].get(), 17);
		org.junit.Assert.assertEquals(arr[1].get(), 27);
		org.junit.Assert.assertEquals(arr[2].get(), 37);
	}
	
	@Test
	public void twoDimensionArrayBindingTest() throws EngineInvocationError {
		//resetTypeSystem();
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		int[][] iarr = new int[][] {
			new int[] { 10, 50 },
			new int[] { 1, 3 },
		};
		
		String[][] sarr = new String[][] {
			new String[] { "A", "B" },
			new String[] { "a", "b" },
		};
		
		ImplicitBindingTests.IBTClass1[][] oarr = 
			new ImplicitBindingTests.IBTClass1[][] {	
				new ImplicitBindingTests.IBTClass1[] {
					new ImplicitBindingTests.IBTClass1(10),
					new ImplicitBindingTests.IBTClass1(50)},
				new ImplicitBindingTests.IBTClass1[] {
					new ImplicitBindingTests.IBTClass1(1),
					new ImplicitBindingTests.IBTClass1(3)}
		};
		
		engine.getContext().addBinding("iarr", new ObjectBinding(iarr));
		engine.getContext().addBinding("sarr", new ObjectBinding(sarr));
		engine.getContext().addBinding("oarr", new ObjectBinding(oarr));
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "array_6.jul"));
		
		validateIntValue(gvt, "icap", 4);
		org.junit.Assert.assertEquals(20, iarr[0][0]);
		org.junit.Assert.assertEquals(6, iarr[1][1]);
		
		validateIntValue(gvt, "scap", 4);
		org.junit.Assert.assertEquals("A_", sarr[0][0]);
		org.junit.Assert.assertEquals("b_", sarr[1][1]);

		validateIntValue(gvt, "ocap", 4);
		org.junit.Assert.assertEquals(20, oarr[0][0].get());
		org.junit.Assert.assertEquals(6, oarr[1][1].get());
	}
}
