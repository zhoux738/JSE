package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.resetTypeSystem;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.oo.ExceptionTestsBase;

public class System_Util_IIterable_Tests extends ExceptionTestsBase {

	private static final String FEATURE = "Iterable";
	
	//--------------- Functionality tests ---------------//
	
	// This the baseline test. It also verifies that:
	//  - can chain-call
	//  - lazy-evaluate
	@Test
	public void filterAndMapTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_01.jul"));
		
		validateStringArrayValue(gvt, "res", new String[] {
			"1:baic_4", "2:chery_5", "3:foton_5", "4:geely_5", "5:saic_4"
		});
	}
	
	@Test
	public void flattenTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_02.jul"));
		
		validateStringArrayValue(gvt, "res", new String[] {
			"bmw", "mb", "vw", "volvo", "hyundai"
		});
	}
	
	@Test
	public void appendAndConcatTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_03.jul"));
	
		validateStringArrayValue(gvt, "res1", new String[] {"baic","byd","chery","faw","foton"});
		validateStringArrayValue(gvt, "res2", new String[] {"baic","byd","chery","faw","geely","saic"});
		validateStringArrayValue(gvt, "res3", new String[] {"baic","byd","chery","faw","foton"});
		validateStringArrayValue(gvt, "res4", new String[] {"baic","byd","chery","faw"});
		validateBoolValue(gvt, "res4LastElementChecked", true);
	}

	@Test
	public void appendAndConcatNullTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_04.jul"));
		
		validateStringValue(gvt, "res1", "");
		validateStringValue(gvt, "res2", "");
		validateIntValue(gvt, "index", 2);
	}

	@Test
	public void chainConcatTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_05.jul"));
		
		validateStringArrayValue(gvt, "res1", new String[] {"geely","saic","baic","byd","chery","geely","saic","faw"});
	}
	
	@Test
	public void takeAndSkipTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_06.jul"));

		validateStringArrayValue(gvt, "sa1", new String[] {"faw","foton"});
		validateStringArrayValue(gvt, "sa2", new String[] {"foton","geely"});
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", true);
	}
	
	@Test
	public void reduceAndCountTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_07.jul"));

		validateStringArrayValue(gvt, "res1", new String[] {"de=3", "se=1", "us=2"});
		validateIntValue(gvt, "count", 3);
		validateBoolValue(gvt, "check", true);
	}
	
	@Test
	public void allAndAnyTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_08.jul"));
		
		validateBoolValue(gvt, "allTrue", true);
		validateBoolValue(gvt, "allFalse", false);
		validateBoolValue(gvt, "anyTrue", true);
		validateBoolValue(gvt, "allFalse", false);
		validateIntValue(gvt, "count", 1);
	}
	
	@Test
	public void toMapTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_09.jul"));

		validateStringArrayValue(gvt, "res1", new String[] {"de=v", "se=v", "us=g"});
		validateStringArrayValue(gvt, "res2", new String[] {"de=vw", "se=volvo", "us=gm"});
	}
	
	@Test
	public void toListTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_10.jul"));

		validateStringArrayValue(gvt, "res1", new String[] {"baic", "byd", "chery", "faw", "foton", "geely", "saic"});
		validateStringArrayValue(gvt, "res2", new String[] {"4", "3", "5", "3", "5", "5", "4"});
		validateIntValue(gvt, "length", 7);
	}
	
	@Test
	public void toArrayTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_11.jul"));

		validateStringArrayValue(gvt, "a1", new String[] {"baic", "byd", "chery", "faw"});
		validateBoolValue(gvt, "c1", true);
		validateBoolValue(gvt, "c2", true);
		validateBoolValue(gvt, "c3", true);
		validateBoolValue(gvt, "c4", true);
	}
	
	@Test
	public void firstAndLastTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_12.jul"));
		
		validateNullValue(gvt, "s3");
		validateNullValue(gvt, "s4");
		validateNullValue(gvt, "s5");
		validateNullValue(gvt, "s6");
		validateStringValue(gvt, "s1", "baic");
		validateStringValue(gvt, "s2", "faw");
		validateBoolValue(gvt, "b1", true);
	}
	
	@Test
	public void zipTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_13.jul"));

		validateStringArrayValue(gvt, "a1", new String[] {"PK_baic", "SZ_byd", "AH_chery", "CC_faw"});
		validateStringArrayValue(gvt, "a2", new String[] {"PK_baic", "SZ_byd", "AH_chery"});
		validateStringArrayValue(gvt, "a3", new String[] {"PK_baic", "SZ_byd", "AH_chery", "CC_faw"});
	}
	
	//--------------- Failure tests ---------------//
	
	// demand more than available.
	@Test
	public void failInsideCallCheckTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_20.jul"));
		validateIntValue(gvt, "i", 5);
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", true);
		validateBoolValue(gvt, "b3", true);
	}
	
	// 'this' is null;
	// function is unsatisfactory;
	@Test
	public void failPrecallCheckTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_21.jul"));

		validateBoolValue(gvt, "res1", true);
		validateBoolValue(gvt, "res2", true);
	}
	
	//--------------- Comprehensive tests ---------------//
	
	@Test
	public void comprehensiveTest1() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "iter_30.jul"));

		validateStringArrayValue(gvt, "res1", new String[] {"target1", "target2"});
		validateStringArrayValue(gvt, "res2", new String[] {"target1target1", "target2target2"});
	}
}
