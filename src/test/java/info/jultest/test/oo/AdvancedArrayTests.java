package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.resetTypeSystem;

import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

public class AdvancedArrayTests {

	private static final String FEATURE = "ArrayAdv";
	
	// As of 0.2.0, we do not allow covariance in general. The price of getting this work is too high to worth the actual needs.
	@Test
	public void noCovarianceTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "casting.jul"));
		
		validateBoolValue(gvt, "v1", true);
		validateBoolValue(gvt, "v2", true);
		validateBoolValue(gvt, "v3", true);
	}
	
	@Test
	public void arrayMetaTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "meta.jul"));
		
		validateBoolValue(gvt, "c1", true);
		validateBoolValue(gvt, "c2", true);
		validateBoolValue(gvt, "c3", true);
	}
	
	@Test
	public void arrayDynamicCreateTest() throws EngineInvocationError {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "create.jul"));
		
		validateBoolValue(gvt, "c1", true);
		validateBoolValue(gvt, "c2", true);
		validateBoolValue(gvt, "c3", true);
		validateBoolValue(gvt, "c4", true);
		validateBoolValue(gvt, "c5", true);
		validateBoolValue(gvt, "c6", true);
		validateBoolValue(gvt, "c7", true);
		validateBoolValue(gvt, "c8", true);
		validateBoolValue(gvt, "c9", true);
	}
}
