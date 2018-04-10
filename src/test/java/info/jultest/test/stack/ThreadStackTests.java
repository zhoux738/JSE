package info.jultest.test.stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

public class ThreadStackTests {

	@Test
	public void memoryManagementTest() {
		ThreadStack ts = createNewStack();
		
		// frame 1
		ts.pushFrame();
		ThreadFrame frame = ts.currentFrame();
		FrameMemoryArea frameMemory1 = frame.getMemory();
		IVariableTable vt1 = frame.getVariableTable();
		
		// create new variables with value stored in current frame.
		IntValue varA = new IntValue(frameMemory1, 37);
		vt1.addVariable("varA", varA);
		IntValue varB = new IntValue(frameMemory1, 50);
		vt1.addVariable("varB", varB);
		
		// frame 2
		ts.pushFrame();
		frame = ts.currentFrame();
		assertNotNull(frame);
		FrameMemoryArea frameMemory2 = frame.getMemory();
		IVariableTable vt2 = frame.getVariableTable();	
		
		// create new variables with value stored in current frame.
		varA = new IntValue(frameMemory2, 137);
		vt2.addVariable("varA", varA);
		IntValue varC = new IntValue(frameMemory2, 200);
		vt2.addVariable("varC", varC);		
		
		ts.popFrame();
		assertFalse(varA.isStored());
		assertFalse(varC.isStored());
		assertTrue(varB.isStored());
	}
	
	@Test
	public void declareVarInFrameTest() {
		ThreadStack ts = createNewStack();
		
		// no frame has been pushed in so far
		ThreadFrame frame = ts.currentFrame();
		assertEquals(null, frame);
		
		// frame 1
		ts.pushFrame();
		frame = ts.currentFrame();
		assertNotNull(frame);
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
		
		// frame 2
		ts.pushFrame();
		frame = ts.currentFrame();
		assertNotNull(frame);
		FrameMemoryArea frameMemory2 = frame.getMemory();
		IVariableTable vt2 = frame.getVariableTable();	
		
		// create new variables with value stored in current frame.
		varA = new IntValue(frameMemory2, 137);
		vt2.addVariable("varA", varA);
		IntValue varC = new IntValue(frameMemory2, 200);
		vt2.addVariable("varC", varC);
		
		retrieveVariableAndValidateValue(ts, "varA", 137);
		assertVariableNotExisting(ts, "varB");
		retrieveVariableAndValidateValue(ts, "varC", 200);
	}
	
	private ThreadStack createNewStack(){
		return new ThreadStack(new StackAreaFactory(){
			@Override
			public StackArea createStackArea() {
				return new SimpleStackArea();
			}			
		}, new VariableTable(null));
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
