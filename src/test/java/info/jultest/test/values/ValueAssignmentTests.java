package info.jultest.test.values;

import static org.junit.Assert.assertEquals;

import info.jultest.test.Commons;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper;

import org.junit.Before;
import org.junit.Test;

public class ValueAssignmentTests {

	private MemoryArea memory;
	
	@Before
	public void setUp() throws Exception {
		BuiltinTypeBootstrapper.bootstrapClassTypes();
		memory = new DummyMemoryArea();
	}
	
	/**
	 * <pre><code>
	 * int a = ...
	 * float b = ...
	 * a = b;
	 * (alt) b = a;
	 * </code></pre>
	 */
	@Test
	public void intFloatAssignmentTest() {
		IntValue i1 = new IntValue(memory, 3);
		FloatValue f1 = new FloatValue(memory, 2.7f);
		f1.assignTo(i1);
		assertEquals(i1.getIntValue(), (int) 2.7f);
		
		IntValue i2 = new IntValue(memory, 5);
		i2.assignTo(f1);
		assertEquals(5.0f, f1.getFloatValue(), 0.00001);
	}
	
	/**
	 * <pre><code>
	 * string a = ...
	 * string b = ...
	 * a = b;
	 * </code></pre>
	 */
	@Test
	public void stringValueAssignmentTest() {
		StringValue foo = new StringValue(memory, "foo");
		StringValue notbar = new StringValue(memory, "notbar");
		RefValue fooRef = new RefValue(memory, foo);
		RefValue notbarRef = new RefValue(memory, notbar);
		fooRef.assignTo(notbarRef);
		
		// The assignment changes reference value
		StringValue sv = StringValue.dereference(notbarRef);
		assertEquals("foo", sv.getStringValue());
		IntValue len = (IntValue) sv.getMemberValue("length");
		assertEquals(3, len.getIntValue());
		
		// The assignment doesn't change string value
		assertEquals("notbar", notbar.getStringValue());
		len = (IntValue) notbar.getMemberValue("length");
		assertEquals(6, len.getIntValue());
	}
	
	/**
	 * <pre><code>
	 * int[][] a = new int[3][2];
	 * int[][] b = new int[5][10];
	 * b[1][2] = 17;
	 * a = b;
	 * a[1][2] == 17 ? SUCCESS : ERROR
	 * </code></pre>
	 */
	@Test
	public void arrayValueAssignmentTest() {
		ArrayValue arrA = ArrayValueFactory.createArrayValue(memory, Commons.DummyTypeTable, IntType.getInstance(), new int[]{3,2});
		RefValue a = new RefValue(memory, arrA);
		
		ArrayValue arrB = ArrayValueFactory.createArrayValue(memory, Commons.DummyTypeTable, IntType.getInstance(), new int[]{5,10});
		RefValue b = new RefValue(memory, arrB);
		
		int[] index = new int[]{1,2};
		
		setValue(b, index, 17);
		
		b.assignTo(a);
		
		int v = getValue(a, index);
		
		assertEquals(17, v);
	}
	
	/**
	 * <pre><code>
	 * int[][] a = new int[3][2];
	 * int[][] b = new int[5][10];
	 * a[0][0] = 23;
	 * b[1][2] = 17;
	 * a[1] = b[1];
	 * a[0][0] == 23 ? SUCCESS : ERROR  //a[0] remains unchanged
	 * a[1][2] == 17 ? SUCCESS : ERROR	//a[1] points to another array
	 * b[1][2] == 17 ? SUCCESS : ERROR  //b[1] remains unchanged
	 * </code></pre>
	 */
	@Test
	public void arrayValueAssignmentTest2() {
		ArrayValue arrA = ArrayValueFactory.createArrayValue(memory, Commons.DummyTypeTable, IntType.getInstance(), new int[]{3,2});
		RefValue a = new RefValue(memory, arrA);
		
		ArrayValue arrB = ArrayValueFactory.createArrayValue(memory, Commons.DummyTypeTable, IntType.getInstance(), new int[]{5,10});
		RefValue b = new RefValue(memory, arrB);
		
		int[] index0 = new int[]{0,0};
		int[] index1 = new int[]{1,2};
		
		setValue(a, index0, 23);
		setValue(b, index1, 17);
		
		RefValue a1 = (RefValue) ((ArrayValue) a.getReferredValue()).getValueAt(1);
		RefValue b1 = (RefValue) ((ArrayValue) b.getReferredValue()).getValueAt(1);
		b1.assignTo(a1);
		
		int v = getValue(a, index0);	//a[0] remains unchanged
		assertEquals(23, v);
		
		v = getValue(a, index1);	//a[1] points to another array
		assertEquals(17, v);
		
		v = getValue(b, index1);
		assertEquals(17, v);	//b[1] remains unchanged
	}
	
	private void setValue(RefValue ref, int[] indices, int value){
		IntValue val = (IntValue) ((ArrayValue) ((RefValue) (((ArrayValue) 
			ref.getReferredValue()).getValueAt(indices[0]))).getReferredValue()).getValueAt(indices[1]);
		IntValue temp = TempValueFactory.createTempIntValue(value);
		temp.assignTo(val);
	}
	
	private int getValue(RefValue ref, int[] indices){
		IntValue val = (IntValue) ((ArrayValue) ((RefValue) (((ArrayValue) 
			ref.getReferredValue()).getValueAt(indices[0]))).getReferredValue()).getValueAt(indices[1]);
		return val.getIntValue();
	}

}
