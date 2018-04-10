package info.jultest.test.oo;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;

import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

public class CovarianceTests {

	private static final String FEATURE = "Covariance";
	
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
	
}
