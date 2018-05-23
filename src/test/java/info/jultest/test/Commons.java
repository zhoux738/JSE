package info.jultest.test;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Assert;

import info.julang.execution.EngineRuntime;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.CharValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.UntypedValue;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.ModuleManager;

public final class Commons {

	// Change this per local settings.
	private static String HC_SRC_REPO_ROOT; 

	static {
		HC_SRC_REPO_ROOT = new File("").getAbsolutePath() + "/src/test/";

    // (Keep the following for debugging)
	/* *
		System.out.println(HC_SRC_REPO_ROOT);
		try {
			System.out.println("Input any char to continue:");
			System.in.read();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
	//*/
	}
	
	public final static String SRC_REPO_ROOT = HC_SRC_REPO_ROOT + "julian/";
	
	public final static String SCRIPT_ROOT = SRC_REPO_ROOT + "Interpret/";
	
	public final static String SCANNING_ROOT = SRC_REPO_ROOT + "Scanning/";
	
	public final static String PARSING_ROOT = SRC_REPO_ROOT + "Parsing/";
	
	public static class Groups {
		public static final String APPLICATION = "Application";
		public static final String FUNCTIONAL  = "Functional";
		public static final String HOSTING     = "Hosting";
		public static final String IMPERATIVE  = "Imperative";
		public static final String OO          = "OO";
		public static final String THREADING   = "Threading";
	}
	
	public static TypeTable DummyTypeTable = new TypeTable(null);
	
	static String makeScriptPath(String group, String feature, String scriptName){
		if(feature == null){
			feature = ".";
		}

		return SCRIPT_ROOT + group + "/" + feature + "/" + scriptName;
	}
	
	public static FileScriptProvider getScriptFile(String group, String feature, String scriptName){
		String path = makeScriptPath(group, feature, scriptName);
		return FileScriptProvider.create(path);
	}
	
	public static SimpleScriptEngine makeSimpleEngine(VariableTable gvt, ModuleManager mm, boolean reentry){
		EngineRuntime rt = new SimpleEngineRuntime(new SimpleHeapArea(), gvt, mm);
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption(reentry, true, false));
		return engine;
	}
	
	/**
	 * Create a simple engine with optional prepared arguments
	 * @param heap heap memory. Can be null as long as tt is also null.
	 * @param gvt global variable table. Can be null.
	 * @param tt type table. Can be null.
	 * @param mm module manager. Can be null.
	 * @return
	 */
	public static SimpleScriptEngine makeSimpleEngine(HeapArea heap, VariableTable gvt, TypeTable tt, ModuleManager mm){
		if(heap == null){
			if(tt != null){
				Assert.fail("Engine initialization failure: must also provide heap if type table is provided.");
			}
			heap = new SimpleHeapArea();
		}
		if(gvt == null){
			gvt = new VariableTable(null);
		}
		if(tt == null){
			tt = new TypeTable(heap);
		}
		if(mm == null){
			mm = new ModuleManager();
		}
		EngineRuntime rt = new SimpleEngineRuntime(heap, gvt, tt, mm);
		SimpleScriptEngine engine = new SimpleScriptEngine(rt, new EngineInitializationOption());
		return engine;
	}
	
	public static SimpleScriptEngine makeSimpleEngine(VariableTable gvt){
		return makeSimpleEngine(gvt, false);
	}
	
	public static SimpleScriptEngine makeSimpleEngine(VariableTable gvt, boolean reentry){
		return makeSimpleEngine(gvt, new ModuleManager(), reentry);
	}
	
	public static VariableTable runInline(String feature, String script) throws EngineInvocationError{
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		engine.run(getScriptFile(Commons.Groups.OO, feature, script));
		return gvt;
	}
	
	public static void validateBoolArrayValue(VariableTable vt, String varName, boolean[] array){
		ArrayValue avalue = validateArrayValue(vt, varName, array.length);
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			boolean iv = ((BoolValue)v).getBoolValue();
			assertEquals(array[i], iv);
		}
	}
	
	public static void validateIntArrayValue(VariableTable vt, String varName, int[] array){
		ArrayValue avalue = validateArrayValue(vt, varName, array.length);
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			int iv = ((IntValue)v).getIntValue();
			assertEquals(array[i], iv);
		}
	}

	public static void validateByteArrayValue(VariableTable vt, String varName, int[] array){
		validateByteArrayValue(vt, varName, array, array.length);
	}
	
	/**
	 * Validate an array variable by comparing each of the elements in the expected array to its corresponding
	 * one in the script array. Note this can return true if the given array comes shorter than the script array.
	 * 
	 * @param vt
	 * @param varName
	 * @param array the array to compare with the script array at each element. Can be shorter than the script array.
	 * @param scriptArrayLength the expected length of script array. Can be longer than the array passed in.
	 */
	public static void validateByteArrayValue(VariableTable vt, String varName, int[] array, int scriptArrayLength){
		ArrayValue avalue = validateArrayValue(vt, varName, scriptArrayLength);
		// each element
		for(int i = 0; i<array.length; i++){
			JValue v = avalue.getValueAt(i);
			int iv = ((ByteValue)v).getByteValue();
			assertEquals(array[i], iv);
		}
	}
	
	public static interface ValueValidator {
		boolean validate(ObjectValue ov, Object expectedRaw);
	}
	
	public static class StringValueValidator implements ValueValidator {
		
		public final static StringValueValidator INSTANCE = new StringValueValidator();
		
		@Override
		public boolean validate(ObjectValue ov, Object expectedRaw) {
			if(expectedRaw == null){
				return ov == RefValue.NULL;
			}
			
			Assert.assertTrue(ov instanceof StringValue);
			StringValue sv = (StringValue) ov;
			return sv.getStringValue().equals(expectedRaw);
		}
	}
	
	/**
	 * Validate an object array using a customized validator against a native array.
	 * 
	 * @param vt
	 * @param varName
	 * @param array the expected raw value array. This doesn't have to be a JValue array. The validator
	 * may perform any kind of transformation on top of this array before asserting. An example is {@link StringValueValidator}.
	 * @param validator
	 */
	public static void validateObjectArrayValue(VariableTable vt, String varName, Object[] array, ValueValidator validator){
		ArrayValue avalue = validateArrayValue(vt, varName, array.length);
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i).deref();
			assertTrue(v instanceof ObjectValue);
			ObjectValue ov = (ObjectValue) v;
			assertTrue(
				"Value at index " + i + " doesn't meet the expected (" + (array[i] == null ? "null" : array[i].toString()) + ")", 
				validator.validate(ov, array[i]));
		}
	}
	
	public static void validateCharArrayValue(VariableTable vt, String varName, char[] array){
		ArrayValue avalue = validateArrayValue(vt, varName, array.length);
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			char cv = ((CharValue)v).getCharValue();
			assertEquals(array[i], cv);
		}
	}
	
	public static void validateStringArrayValue(VariableTable vt, String varName, String[] array){
		ArrayValue avalue = validateArrayValue(vt, varName, array.length);
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			String sv = (StringValue.dereference(v, true)).getStringValue();
			assertEquals(array[i], sv);
		}
	}
	
	public static ArrayValue validateArrayValue(VariableTable vt, String varName, int length){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		assertEquals(RefValue.class, value.getClass());
		ArrayValue avalue = (ArrayValue)(((RefValue) value).getReferredValue());
		// length
		assertEquals(length, avalue.getLength());
		
		return avalue;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T validateHostedValue(VariableTable vt, String varName, Class<T> clazz){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		assertEquals(RefValue.class, value.getClass());
		HostedValue hvalue = (HostedValue)(((RefValue) value).getReferredValue());
		
		Object obj = hvalue.getHostedObject();
		assertEquals(obj.getClass(), clazz);
		
		return (T) obj;
	}
	
	public static void validateBoolValue(VariableTable vt, String varName, boolean v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		BoolValue bvalue = (BoolValue) value;
		assertEquals(v, bvalue.getBoolValue());
	}
	
	public static void validateCharValue(VariableTable vt, String varName, char v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		assertEquals(CharValue.class, value.getClass());
		CharValue bvalue = (CharValue) value;
		assertEquals(v, bvalue.getCharValue());
	}
	
	public static String getStringValue(VariableTable vt, String varName){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		value = value.deref();
		StringValue svalue = (StringValue) value;
		return svalue.getStringValue();
	}
	
	public static void validateStringValue(VariableTable vt, String varName, String v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		value = value.deref();
		StringValue svalue = (StringValue) value;
		assertEquals(v, svalue.getStringValue());
	}
	
	public static void validateIntValue(VariableTable vt, String varName, int v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		IntValue ivalue = (IntValue) value;
		assertEquals(v, ivalue.getIntValue());
	}
	
	/** can validate both int and var with RT type = int. */
	public static void validateIntValueEx(VariableTable vt, String varName, int v){
		JValue value = getValue(vt, varName);
		IntValue ivalue = (IntValue) value;
		assertEquals(v, ivalue.getIntValue());
	}
	
	/** can validate both bool and var with RT type = bool. */
	public static void validateBoolValueEx(VariableTable vt, String varName, boolean v){
		JValue value = getValue(vt, varName);
		BoolValue bvalue = (BoolValue) value;
		assertEquals(v, bvalue.getBoolValue());
	}
	
	/** range in format of [1,2) */
	public static void validateIntValueWithinRange(VariableTable vt, String varName, String range){
		JValue value = vt.getVariable(varName).deref();
		assertNotNull(value);
		IntValue ivalue = (IntValue) value;
		IntRange irange = IntRange.parse(range);
		irange.validate(ivalue.getIntValue());
	}

	/** This method accepts an integer but will downcast it to a byte internally. */
	public static void validateByteValue(VariableTable vt, String varName, int v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		ByteValue bvalue = (ByteValue) value;
		byte b = (byte)v;
		assertEquals(b, bvalue.getByteValue());
	}
	
	public static void validateFloatValue(VariableTable vt, String varName, float v){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		FloatValue fvalue = (FloatValue) value;
		org.junit.Assert.assertEquals(v, fvalue.getFloatValue(), 0.001f);
	}
	
	public static JValue validateUntypedValue(VariableTable vt, String varName){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		assertEquals(UntypedValue.class, value.getClass());
		UntypedValue uvalue = (UntypedValue) value;
		return uvalue.getActual();
	}
	
	public static void validateIntArrayValue(JValue val, int[] array){
		assertTrue(ArrayValue.class.isAssignableFrom(val.getClass()));
		ArrayValue avalue = (ArrayValue)val;
		// each element
		for(int i = 0; i<avalue.getLength(); i++){
			JValue v = avalue.getValueAt(i);
			int iv = ((IntValue)v).getIntValue();
			assertEquals(array[i], iv);
		}
	}
	
	public static void validateBoolValue(JValue value, boolean v){
		assertNotNull(value);
		//assertEquals(BoolValue.class, value.getClass());
		BoolValue bvalue = (BoolValue) value;
		assertEquals(v, bvalue.getBoolValue());
	}
	
	public static void validateStringValue(JValue value, String v){
		assertNotNull(value);
		value = RefValue.tryDereference(value);
		assertEquals(StringValue.class, value.getClass());
		StringValue svalue = (StringValue) value;
		assertEquals(v, svalue.getStringValue());
	}
	
	public static void validateIntValue(JValue value, int v){
		assertNotNull(value);
		IntValue ivalue = (IntValue) value;
		assertEquals(v, ivalue.getIntValue());
	}
	
	public static int getIntValue(JValue value){
		assertNotNull(value);
		assertEquals(IntValue.class, value.getClass());
		IntValue ivalue = (IntValue) value;
		return ivalue.getIntValue();
	}
	
	public static void validateNullValue(VariableTable vt, String varName){
		JValue value = vt.getVariable(varName);
		assertNotNull(value);
		assertEquals(RefValue.NULL, value.deref());
	}
	
	public static void verifyDetectedClass(ModuleManager manager, String fullName, String simpleName){
		List<ClassInfo> ciXa = manager.getClassesByNFQName(simpleName);
		assertEquals(1, ciXa.size());
		assertEquals(fullName, ciXa.get(0).getFQName());
	}
	
	private static JValue getValue(VariableTable vt, String varName){
		JValue value = vt.getVariable(varName);
		assertNotNull("Variable " + varName + " not defined?", value);
		assertEquals(UntypedValue.class, value.getClass());
		if (value instanceof UntypedValue){
			UntypedValue uvalue = (UntypedValue) value;
			value = uvalue.getActual();
		}
		return value;
	}
	
}
