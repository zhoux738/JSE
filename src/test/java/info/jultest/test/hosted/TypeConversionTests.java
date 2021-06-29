package info.jultest.test.hosted;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateFloatValue;
import static info.jultest.test.Commons.validateIntArrayValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Assert;
import org.junit.Test;

import info.julang.execution.Argument;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Result;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.hosting.mapped.MappedTypeConversionException;
import info.julang.hosting.mapped.PlatformConversionUtil;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.context.Context;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueBuilder;
import info.julang.memory.value.ArrayValueBuilderHelper;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.jultest.test.Commons;

public class TypeConversionTests {

	private static final String FEATURE = "Components";

	@Test
	public void fromScriptToPlatformTypeTest() throws MappedTypeConversionException {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();	
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		
		Assert.assertEquals(3, toValue(new IntValue(rt.getHeap(), 3)));
		Assert.assertEquals("abc", toValue(new StringValue(rt.getHeap(), "abc")));
		
		MemoryArea mem = rt.getHeap();
		ArrayValueBuilder builder = ArrayValueBuilderHelper.getBuilder(IntType.getInstance(), mem, tt);
		builder.setLength(2);
		builder.setValue(0, new IntValue(mem, 37));
		builder.setValue(1, new IntValue(mem, 41));
		ArrayValue av = builder.getResult();
		Object obj = toValue(av);
		Assert.assertEquals(int[].class, obj.getClass());
		int[] pav = (int[])obj;
		Assert.assertEquals(2, pav.length);
		Assert.assertEquals(37, pav[0]);
		Assert.assertEquals(41, pav[1]);

		builder = ArrayValueBuilderHelper.getBuilder(ByteType.getInstance(), mem, tt);
		builder.setLength(2);
		builder.setValue(0, new ByteValue(mem, (byte)37));
		builder.setValue(1, new ByteValue(mem, (byte)41));
		av = builder.getResult();
		obj = toValue(av);
		Assert.assertEquals(byte[].class, obj.getClass());
		byte[] pav2 = (byte[])obj;
		Assert.assertEquals(2, pav2.length);
		Assert.assertEquals(37, pav2[0]);
		Assert.assertEquals(41, pav2[1]);
	}
	
	@Test
	public void fromScriptToPlatformTypeTest2() throws MappedTypeConversionException, EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "conversion_02.jul"));
		
		JValue val = gvt.getVariable("arr");
		Object obj = toValue(val);
		String[][] arr = (String[][])obj;
		for(int i = 0; i < arr.length; i++) {
			for(int j = 0; j < arr[i].length; j++) {
				Assert.assertEquals(i + "-" + j, arr[i][j]); 
			}
		}
	}
	
	private static Object toValue(JValue val) throws MappedTypeConversionException {
		return PlatformConversionUtil.toPlatformObject(val, null, null); // Tests in this class do not use type table
	}
	
	@Test
	public void fromPlatformToScriptTypeTest() throws MappedTypeConversionException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		
		Context ctxt = makeUnthreadedContext(rt);
		
		validateType(ctxt, int.class, IntType.getInstance());
		validateType(ctxt, Integer.class, IntType.getInstance());
		validateType(ctxt, byte.class, ByteType.getInstance());
		validateType(ctxt, Byte.class, ByteType.getInstance());
		validateType(ctxt, char.class, CharType.getInstance());
		validateType(ctxt, Character.class, CharType.getInstance());
		validateType(ctxt, float.class, FloatType.getInstance());
		validateType(ctxt, Float.class, FloatType.getInstance());
		validateType(ctxt, boolean.class, BoolType.getInstance());
		validateType(ctxt, Boolean.class, BoolType.getInstance());
		validateType(ctxt, String.class, JStringType.getInstance());
		validateType(ctxt, Void.class, VoidType.getInstance());
		
		JArrayType intArrTyp = JArrayType.createJArrayType(tt, IntType.getInstance(), true);
		JArrayType intArrArrTyp = JArrayType.createJArrayType(tt, intArrTyp, true);
		validateType(ctxt, int[][].class, intArrArrTyp);
		

		JArrayType strArrTyp = JArrayType.createJArrayType(tt, JStringType.getInstance(), true);
		JArrayType strArrArrTyp = JArrayType.createJArrayType(tt, strArrTyp, true);
		validateType(ctxt, String[].class, strArrTyp);
		validateType(ctxt, String[][].class, strArrArrTyp);
	}
	
	@Test
	public void fromPlatformToScriptValueTest1() throws MappedTypeConversionException {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		
		Context ctxt = makeUnthreadedContext(rt);
		
		JValue val = toValue(ctxt, 5);
		validateIntValue(val, 5);
		
		val = toValue(ctxt, "HELLO");
		validateStringValue(val, "HELLO");
		
		val = toValue(ctxt, new int[]{1,2});
		validateIntArrayValue(val, new int[]{1,2});
	}
	
	@Test
	public void fromPlatformToScriptValueTest2() throws MappedTypeConversionException, EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();	
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		
		Context ctxt = makeUnthreadedContext(rt);
		
		JValue iaa = toValue(ctxt, new int[][]{{3,5},{7,11}});
		JValue saa = toValue(ctxt, new String[][]{{"3","5"},{"7","11"}});
		JValue caa = toValue(ctxt, new char[][]{{'a','b'},{'z','m'}});
		JValue baa = toValue(ctxt, new boolean[][]{{true,false},{false,true}});
		JValue faa = toValue(ctxt, new float[][]{{1.1f,2.2f},{3.3f,4.4f}});
		
		ctxt.getJThread().getThreadRuntime().getThreadStack().popFrame();
		
		gvt.addVariable("iaa", iaa);
		gvt.addVariable("saa", saa);
		gvt.addVariable("caa", caa);
		gvt.addVariable("baa", baa);
		gvt.addVariable("faa", faa);
		
		engine.run(getScriptFile(Commons.Groups.HOSTING, FEATURE, "conversion_01.jul"));

		validateIntValue(gvt, "total", 26);
		validateStringValue(gvt, "cc", "35711");
		validateStringValue(gvt, "cc2", "abzm");
		validateStringValue(gvt, "cc3", "truefalsefalsetrue");
		validateFloatValue(gvt, "ftotal", 11.0f);
	}
	
	private void validateType(Context ctxt, Class<?> clazz, JType expected) throws MappedTypeConversionException {
		JType typ = PlatformConversionUtil.fromPlatformType(clazz, ctxt, null);
		Assert.assertEquals(expected, typ);
	}
	
	private JValue toValue(Context ctxt, Object obj) throws MappedTypeConversionException {
		return PlatformConversionUtil.fromPlatformObject(obj, ctxt);
	}

	private Context makeUnthreadedContext(EngineRuntime rt){
		JThread jt = rt.getThreadManager().createMain(rt, new InterpretedExecutable(null, null, false, false){

			@Override
			public Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) throws EngineInvocationError {
				return null;
			}
			
		});
		
		jt.getThreadRuntime().getThreadStack().pushFrame();
		
		return Context.createSystemLoadingContext(jt.getThreadRuntime());
	}

}
