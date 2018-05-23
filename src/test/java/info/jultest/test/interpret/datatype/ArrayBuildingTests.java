package info.jultest.test.interpret.datatype;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.EngineRuntime;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueBuilder;
import info.julang.memory.value.ArrayValueBuilderHelper;
import info.julang.memory.value.ObjectArrayValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JStringType;

import org.junit.Test;

public class ArrayBuildingTests {

	private static final String FEATURE = "DataType";
	
	@Test
	public void objectArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();	
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		JArrayType strArrTyp = JArrayType.createJArrayType(tt, JStringType.getInstance(), false);
		
		ObjectArrayValue.Builder builder0 = new ObjectArrayValue.Builder(
			engine.getRuntime().getHeap(), JStringType.getInstance(), tt);
		builder0.setLength(3);
		builder0.setValue(0, TempValueFactory.createTempStringValue("a"));
		builder0.setValue(1, TempValueFactory.createTempStringValue("b"));
		builder0.setValue(2, TempValueFactory.createTempStringValue("c"));
		ObjectArrayValue oav0 = builder0.getResult();
		
		ObjectArrayValue.Builder builder1 = new ObjectArrayValue.Builder(
			engine.getRuntime().getHeap(), JStringType.getInstance(), tt);
		builder1.setLength(3);
		builder1.setValue(0, TempValueFactory.createTempStringValue("d"));
		builder1.setValue(1, TempValueFactory.createTempStringValue("e"));
		builder1.setValue(2, TempValueFactory.createTempStringValue("f"));
		ObjectArrayValue oav1 = builder1.getResult();
		
		ObjectArrayValue.Builder builder = new ObjectArrayValue.Builder(
			engine.getRuntime().getHeap(), strArrTyp, tt);
		builder.setLength(2);
		builder.setValue(0, oav0);
		builder.setValue(1, oav1);
		ObjectArrayValue oav = builder.getResult();
		
		gvt.addVariable("oav", oav);

		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_01.jul"));
		validateStringValue(gvt, "pos_1_2", "f");
		validateStringValue(gvt, "pos_0_1", "x");
	}
	
	@Test
	public void intArrayTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();	
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		EngineRuntime rt = engine.getRuntime();
		ITypeTable tt = rt.getTypeTable();
		tt.initialize(rt);
		
		ArrayValueBuilder builder = ArrayValueBuilderHelper.getBuilder(IntType.getInstance(), rt.getHeap(), tt);
		
		builder.setLength(3);
		builder.setValue(0, TempValueFactory.createTempIntValue(11));
		builder.setValue(1, TempValueFactory.createTempIntValue(13));
		builder.setValue(2, TempValueFactory.createTempIntValue(17));
		ArrayValue av = builder.getResult();
		
		gvt.addVariable("av", av);

		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "array_02.jul"));
		validateIntValue(gvt, "total", 41);
	}
	
}
