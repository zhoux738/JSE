package info.jultest.test.threading;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateArrayValue;
import static info.jultest.test.Commons.validateHostedValue;
import static info.jultest.test.Commons.validateIntValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.FaultedThreadRecord;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.UntypedValue;
import info.julang.typesystem.jclass.jufc.System.Collection.JList;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

//(Uncomment @RunWith for reliability test)
//@RunWith(Parameterized.class)
// Multiple thread access tests
public class MemoryAccessTests extends ExceptionTestsBase {

	private static final String FEATURE = "MemoryAccess";

	@Before
	public void shouldRun() {
		Assume.assumeTrue(GlobalSetting.EnableMultiThreadingTests);
	}
	
	//(Uncomment data() for reliability test)
//	@Parameterized.Parameters
//	public static List<Object[]> data() {
//	  return Arrays.asList(new Object[20][0]);
//	}
	
	@Test
	public void twoThreadsAccArrayTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_acc_int_array.jul"));
		
		validateArrayValues(gvt, "values", new Integer[]{10, 20}, 100, new IValueGetter(){
			@Override
			public Object get(JValue val) {
				return ((IntValue)val).getIntValue();
			}		
		});
	}
	
	@Test
	public void twoThreadsAccArrayTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "2_threads_acc_string_array.jul"));
		
		validateArrayValues(gvt, "values", new String[]{"a", "b"}, 100, new IValueGetter(){
			@Override
			public Object get(JValue val) {
				return (StringValue.dereference(val, true)).getStringValue();
			}		
		});
	}
	
	@Test
	public void twoThreadsAccListByForTest() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "list_by_for.jul"));
		
		validateIntListValue(gvt, new int[]{10, 20});
	}
	
	// If this test finished as expected, the logs should contain something like:
	//   t1 is to wait. towait = 1
	//   t2 aborted. Notify the other. towait = 0
	@Test
	public void twoThreadsAccListByForeachTest1() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "list_by_foreach_1.jul"));
		
		validateIntListValue(gvt, new int[]{10, 20});
		
		List<FaultedThreadRecord> records = engine.getRuntime().getThreadManager().getFaultedThreads();
		assertEquals(1, records.size());
		Exception ex = records.get(0).getException();
		assertEquals(JulianScriptException.class, ex.getClass());
		TestExceptionHandler teh = new TestExceptionHandler();
		teh.onException((JulianScriptException)ex);
		
		assertException(teh, "System.Collection.ConcurrentModificationException");
	}
	
	@Test
	public void twoThreadsAccListByForeachTest2() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "list_by_foreach_2.jul"));
		
		validateIntListValue(gvt, new int[]{20});
	}
	
	@Test
	public void twoThreadsAccListByForeachTest3() throws EngineInvocationError, IOException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.THREADING, FEATURE, "list_by_foreach_3.jul"));
		
		validateIntValue(gvt, "sum", 1000);
	}
	
	private void validateIntListValue(VariableTable vt, int[] possibleValues){
		JList list = validateHostedValue(vt, "values", JList.class);

		// create possible value set
		Set<Integer> set = new HashSet<>();
		for(Integer i : possibleValues){
			set.add(i);
		}
		
		// check each element, which must be one of the possible values
		for(int i = 0; i < list.size(); i++){
			JValue v = list.get(i);
			IntValue iv = (IntValue)UntypedValue.unwrap(v);
			assertTrue(set.contains(iv.getIntValue()));
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void validateArrayValues(VariableTable vt, String varName, Object[] possibleValues, int length, IValueGetter vg){
		// create value set
		Set set = new HashSet();
		for(Object i : possibleValues){
			set.add(i);
		}
		
		ArrayValue avalue = validateArrayValue(vt, varName, length);
		
		// check each element, which must be one of the possible values
		for(int i = 0; i < avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			Object iv = vg.get(v);
			assertTrue(set.contains(iv));
		}
	}
	
	private static interface IValueGetter {
		
		Object get(JValue val);
		
	}
}
