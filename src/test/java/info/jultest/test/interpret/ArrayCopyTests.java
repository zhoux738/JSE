package info.jultest.test.interpret;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolArrayValue;
import static info.jultest.test.Commons.validateByteArrayValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateObjectArrayValue;

import org.junit.Assume;
import org.junit.Test;

import info.jultest.test.Commons;
import info.jultest.test.Commons.StringValueValidator;
import info.jultest.test.Commons.ValueValidator;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import org.junit.Assert;

//(Uncomment @RunWith for reliability test)
// @RunWith(Parameterized.class)
public class ArrayCopyTests {

	private static final String FEATURE = "ArrayCopy";
	
//	// (Uncomment data() for reliability test)
//	@Parameterized.Parameters
//	public static List<Object[]> data() {
//	  return Arrays.asList(new Object[20][0]);
//	}
	
	@Test
	public void copy1DIntArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_01.jul"));
		
		/*
		// Copy full or partial
		Array.copy(src, 0, dst1, 0, 3);
		Array.copy(src, 0, dst2, 1, 2);
		Array.copy(src, 1, dst3, 1, 2);
		Array.copy(src, 1, dst4, 0, 2);
		Array.copy(src, 1, dst5, 1, 4);
		
		// No actual copy
		Array.copy(src, 3, tgt1, 0, 3);
		Array.copy(src, 0, tgt2, 3, 3);
		*/
		
		validateIntArrayValue(gvt, "dst1", new int[]{10,20,30});
		validateIntArrayValue(gvt, "dst2", new int[]{ 0,10,20});
		validateIntArrayValue(gvt, "dst3", new int[]{ 0,20,30});
		validateIntArrayValue(gvt, "dst4", new int[]{20,30, 0});
		validateIntArrayValue(gvt, "dst5", new int[]{ 0,20,30});
		validateIntArrayValue(gvt, "tgt1", new int[]{ 0, 0, 0});
		validateIntArrayValue(gvt, "tgt2", new int[]{ 0, 0, 0});
	}
	
	@Test
	public void copy1DByteArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_02.jul"));
		
		validateByteArrayValue(gvt, "dst1", new int[]{10,20,30});
		validateByteArrayValue(gvt, "dst2", new int[]{ 0,10,20});
		validateByteArrayValue(gvt, "dst3", new int[]{ 0,20,30});
		validateByteArrayValue(gvt, "dst4", new int[]{20,30, 0});
		validateByteArrayValue(gvt, "dst5", new int[]{ 0,20,30});
		validateByteArrayValue(gvt, "tgt1", new int[]{ 0, 0, 0});
		validateByteArrayValue(gvt, "tgt2", new int[]{ 0, 0, 0});
	}
	
	@Test
	public void copy1DObjectArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_03.jul"));
		
		/*
		// Copy full or partial
		Array.copy(src, 0, dst1, 0, 3);
		Array.copy(src, 0, dst2, 1, 2);
		Array.copy(src, 1, dst3, 1, 2);
		Array.copy(src, 1, dst4, 0, 2);
		Array.copy(src, 1, dst5, 1, 4);

		// No actual copy
		Array.copy(src, 3, tgt1, 0, 3);
		Array.copy(src, 0, tgt2, 3, 3);
		*/
		
		validateObjectArrayValue(gvt, "dst1", new Object[]{"Alice",    "Bob", "Charlie"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst2", new Object[]{   null,  "Alice",     "Bob"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst3", new Object[]{   null,    "Bob", "Charlie"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst4", new Object[]{  "Bob", "Charlie",     null}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst5", new Object[]{   null,    "Bob", "Charlie"}, PersonValueValidator.INSTANCE);
		
		validateObjectArrayValue(gvt, "tgt1", new Object[]{   null,     null,      null}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "tgt2", new Object[]{   null,     null,      null}, PersonValueValidator.INSTANCE);
	}
	
	@Test
	public void copy1DStringArrayBasicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_04.jul"));

		validateObjectArrayValue(gvt, "dst1", new Object[]{"Alice",    "Bob", "Charlie"}, StringValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst2", new Object[]{   null,  "Alice",     "Bob"}, StringValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst3", new Object[]{   null,    "Bob", "Charlie"}, StringValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst4", new Object[]{  "Bob", "Charlie",     null}, StringValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst5", new Object[]{   null,    "Bob", "Charlie"}, StringValueValidator.INSTANCE);
		
		validateObjectArrayValue(gvt, "tgt1", new Object[]{   null,     null,      null}, StringValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "tgt2", new Object[]{   null,     null,      null}, StringValueValidator.INSTANCE);
	}
	
	private static class PersonValueValidator implements ValueValidator {
		
		private static final PersonValueValidator INSTANCE = new PersonValueValidator();
		
		@Override
		public boolean validate(ObjectValue ov, Object expectedRaw) {
			if(expectedRaw == null){
				return ov == RefValue.NULL;
			}
			
			JValue val = ov.getMemberValue("name");
			val = RefValue.tryDereference(val);
			Assert.assertTrue(val instanceof StringValue);
			StringValue sv = (StringValue) val;
			Assert.assertEquals(expectedRaw, sv.getStringValue());
			return true;
		}
	}
	
	@Test
	public void copyObjectArrayByReferenceTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_05.jul"));

		validateIntValue(gvt, "total1", 3);
		validateIntValue(gvt, "total2", 3);
		validateObjectArrayValue(gvt, "src",  new Object[]{"Albert", "Bill", "Chris"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst1", new Object[]{"Albert", "Bill", "Chris"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst2", new Object[]{"Albert", "Bill", "Chris"}, PersonValueValidator.INSTANCE);
	}
	
	@Test
	public void copyObjectArrayByReferenceTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_06.jul"));

		validateObjectArrayValue(gvt, "src",  new Object[]{"Albert",  "Bob", "Charlie"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst1", new Object[]{ "Alice", "Bill", "Charlie"}, PersonValueValidator.INSTANCE);
		validateObjectArrayValue(gvt, "dst2", new Object[]{ "Alice",  "Bob",   "Chris"}, PersonValueValidator.INSTANCE);
	}
	
	@Test
	public void illegalArgTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_err_01.jul"));
		
		validateBoolArrayValue(gvt, "res", new boolean[]{true,true,true});
	}
	
	@Test
	public void illegalArgTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_err_02.jul"));
		
		validateBoolArrayValue(gvt, "res", new boolean[]{true,true,true});
	}
	
	@Test
	public void illegalArgTest3() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "ac_err_03.jul"));
		
		validateBoolArrayValue(gvt, "res", new boolean[]{true,true,true,true});
	}
}
