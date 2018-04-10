package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateFloatValue;
import static info.jultest.test.Commons.validateStringValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleManager;

import org.junit.Test;

public class ApplicationTests {
	
	/**
	 * Bubble sort.
	 */
	@Test
	public void bubbleSortTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "bubble_sort.jul"));
		
		//95,45,15,78,84,51,24,12
		//->
		//12,15,24,45,51,78,84,95
		validateIntArrayValue(gvt, "number", new int[]{12,15,24,45,51,78,84,95});
	}
	
	/**
	 * A set of classes simulating stock market and the investor. (v0.0.3)
	 */	
	@Test
	public void stockInvesting0_0_3Test() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "stock_sim.jul"));
		
		validateIntValue(gvt, "total1", 2200);
		validateIntValue(gvt, "total2", 2400);
		validateIntValue(gvt, "total3", 2520);
	}
	
	/**
	 * A set of classes simulating stock market and the investor. (v0.0.6)
	 */	
	@Test
	public void stockInvesting0_0_6Test() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "stock_sim2.jul"));
		
		validateFloatValue(gvt, "cash", 150041.3f);
	}
	
	/**
	 * Merge sort.
	 * <p/>
	 * Original source: Wikipedia. Original language: C++. See <br/>
	 * http://zh.wikipedia.org/wiki/%E5%BD%92%E5%B9%B6%E6%8E%92%E5%BA%8F#C.2B.2B.E8.AA.9E.E8.A8.80
	 */
	@Test
	public void mergeSortTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "merge_sort.jul"));
		
		//[3, 5, 3, 6, 4, 7, 5, 7, 4]
		//->
		//[3, 3, 4, 4, 5, 5, 6, 7, 7]
		validateIntArrayValue(gvt, "a", new int[]{3,3,4,4,5,5,6,7,7});
	}

	/**
	 * Quick sort.
	 * <p/>
	 * Original source: Wikipedia. Original language: C#. See <br/>
	 * http://zh.wikipedia.org/wiki/%E5%BF%AB%E9%80%9F%E6%8E%92%E5%BA%8F#C.23
	 */
	@Test
	public void quickSortTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "quick_sort.jul"));
		
		//[3, 5, 3, 6, 4, 7, 5, 7, 4]
		//->
		//[3, 3, 4, 4, 5, 5, 6, 7, 7]
		validateIntArrayValue(gvt, "a", new int[]{3,3,4,4,5,5,6,7,7});
	}
	
	/**
	 * Heap sort.
	 * <p/>
	 * Original source: Wikipedia. Original language: Java. See <br/>
	 * http://zh.wikipedia.org/wiki/%E5%A0%86%E6%8E%92%E5%BA%8F#Java.E8.AF.AD.E8.A8.80
	 */
	@Test
	public void heapSortTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "heap_sort.jul"));
		
		//[3, 5, 3, 6, 4, 7, 5, 7, 4]
		//->
		//[3, 3, 4, 4, 5, 5, 6, 7, 7]
		validateIntArrayValue(gvt, "a", new int[]{3,3,4,4,5,5,6,7,7});
	}
	
	/**
	 * Binary search tree: insert, search and delete.
	 * <p/>
	 * Original source: V.S.Adamchik, CMU. Original language: Java. See <br/>
	 * https://www.cs.cmu.edu/~adamchik/15-121/lectures/Trees/code/BST.java
	 */
	@Test
	public void bstTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "bst.jul"));
		
		validateIntValue(gvt, "a1", 3);
		validateIntValue(gvt, "a2", 20);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", false);
		validateIntValue(gvt, "a3", 8);
		validateIntValue(gvt, "a4", 14);
	}
	
	/**
	 * Treat an integer array as a number with each element being a digit, then add one to it
	 * to get another array corresponding to the arithmetic result of adding one to that number.
	 * <p/>
	 * For example, adding 1 to {1,5,9} returns {1,6,0}.
	 *  
	 * @throws EngineInvocationError
	 */
	@Test
	public void plusOneTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "plus_one.jul"));
		
		validateIntArrayValue(gvt, "a", new int[]{1,0,0,0,0,0,0,0,0,0,0});
	}
	
	/**
	 * Given numRows, generate the first numRows of Yanghui's triangle.
	 * 
	 * For example, given numRows = 5, Return
	 * <pre>
	 *      [1], 
	 *     [1,1], 
	 *    [1,2,1], 
	 *   [1,3,3,1], 
	 *  [1,4,6,4,1] 
	 * </pre>
	 */
	@Test
	public void yanghuiTriangleTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "yhsj.jul"));
		
		String expected = "    [1]\n   [1,1]\n  [1,2,1]\n [1,3,3,1]\n[1,4,6,4,1]";
		validateStringValue(gvt, "result", expected);
	}
	
	/**
	 * Remove duplicate elements from an array.
	 */
	@Test
	public void removeDupTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "remove_dup.jul"));
		
		validateStringValue(gvt, "ra", "abcdjko");
		validateStringValue(gvt, "rb", "abcdjko");
		validateStringValue(gvt, "rc", "abccdjkko");
	}
	
	/**
	 * A set of classes simulating banking service.
	 * Scenario 1: user opens account, deposits and withdraws.
	 */	
	@Test
	public void bankingTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "banking_1.jul"));
		
		validateStringValue(gvt, "res1", "Current balance: 1000.0");
		validateStringValue(gvt, "res2", "Current balance: 700.0");
	}
	
	/**
	 * A set of classes simulating banking service.
	 * Scenario 2: user transfers between accounts
	 */	
	@Test
	public void bankingTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.APPLICATION, null, "banking_2.jul"));
		
		validateStringValue(gvt, "res1", "Current balance: 650.0");
		validateStringValue(gvt, "res2", "Current balance: 350.0");
	}
}
