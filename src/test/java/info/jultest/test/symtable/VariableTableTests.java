package info.jultest.test.symtable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.StackAreaFactory;
import info.julang.execution.threading.ThreadFrame;
import info.julang.execution.threading.ThreadStack;
import info.julang.memory.FrameMemoryArea;
import info.julang.memory.StackArea;
import info.julang.memory.simple.SimpleStackArea;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;

public class VariableTableTests {

	/**
	 * This tests simulates Julian code like this
	 * <pre><code>
	 * int a = 37;
	 * void fun(){
	 *   // can refer to global a here
	 *   ...
	 *   // can cloak the global var
	 *   int a = 58; 
	 *   ...
	 * }
	 * fun();// call fun
	 * ...
	 * </code></pre>
	 */
	@Test
	public void varTableGlobalScopeTest() {
		VariableTable gvt = new VariableTable(null);
		ThreadStack ts = new ThreadStack(new StackAreaFactory(){
			@Override
			public StackArea createStackArea() {
				return new SimpleStackArea();
			}
		}, gvt);
		
		// enter global scope
		ts.pushFrame(gvt, true);
		ThreadFrame frame = ts.currentFrame();
		FrameMemoryArea frameMemory1 = frame.getMemory();
		IVariableTable vt1 = frame.getVariableTable();
		
		// create new variables with value stored in current frame.
		IntValue varA = new IntValue(frameMemory1, 37);
		vt1.addVariable("varA", varA);
		
		// enter new frame
		ts.pushFrame();
		retrieveVariableAndValidateValue(ts, "varA", 37);
		
		varA = new IntValue(frameMemory1, 58);
		IVariableTable vt2 = ts.currentFrame().getVariableTable();
		vt2.addVariable("varA", varA);
		retrieveVariableAndValidateValue(ts, "varA", 58);
		
		// exit current frame
		ts.popFrame();
		retrieveVariableAndValidateValue(ts, "varA", 37);
	}
	
	/**
	 * This tests simulates Julian code like this
	 * <pre><code>
	 * // enter scope 1
	 * int a = 37;
	 * int b = 50;
	 * {
	 *   // enter scope 2
	 *   int a = 137;
	 *   int c = 200;
	 *   ...
	 * }
	 * // exist scope 2
	 * ...
	 * </code></pre>
	 */
	@Test
	public void varTableScopingTest() {
		ThreadStack ts = new ThreadStack(new StackAreaFactory(){
			@Override
			public StackArea createStackArea() {
				return new SimpleStackArea();
			}			
		}, new VariableTable(null));
		
		// enter scope 1
		ts.pushFrame();
		ThreadFrame frame = ts.currentFrame();
		FrameMemoryArea frameMemory1 = frame.getMemory();
		IVariableTable vt1 = frame.getVariableTable();
		
		// create new variables with value stored in current frame.
		IntValue varA = new IntValue(frameMemory1, 37);
		vt1.addVariable("varA", varA);
		IntValue varB = new IntValue(frameMemory1, 50);
		vt1.addVariable("varB", varB);
		
		// retrieve and validate variable 
		retrieveVariableAndValidateValue(ts, "varA", 37);
		retrieveVariableAndValidateValue(ts, "varB", 50);
		
		// enter scope 2
		vt1.enterScope();	
		
		// create new variables with value stored in current frame.
		varA = new IntValue(frameMemory1, 137);
		vt1.addVariable("varA", varA);
		IntValue varC = new IntValue(frameMemory1, 200);
		vt1.addVariable("varC", varC);
		
		retrieveVariableAndValidateValue(ts, "varA", 137);
		retrieveVariableAndValidateValue(ts, "varB", 50);
		retrieveVariableAndValidateValue(ts, "varC", 200);
		
		// exit scope 2
		vt1.exitScope();
		retrieveVariableAndValidateValue(ts, "varA", 37);
		retrieveVariableAndValidateValue(ts, "varB", 50);
		assertVariableNotExisting(ts, "varC");
	}
	
	private void retrieveVariableAndValidateValue(ThreadStack ts, String varName, int v){
		JValue value = getVariableTable(ts).getVariable(varName);
		assertEquals(value.getClass(), IntValue.class);
		IntValue ivalue = (IntValue) value;
		assertEquals(v, ivalue.getIntValue());
	}
	
	private void assertVariableNotExisting(ThreadStack ts, String varName){
		JValue value = getVariableTable(ts).getVariable(varName);
		assertNull(value);
	}
	
	private IVariableTable getVariableTable(ThreadStack ts){
		return ts.currentFrame().getVariableTable();
	}

}
