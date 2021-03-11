package info.jultest.test.types;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.resetTypeSystem;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.jultest.test.Commons;

public class BuiltinTypesAPITests {

	private static final String FEATURE = "BuiltIns";
	
	@Test
	public void getTypeTest() throws Exception {
		resetTypeSystem();
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "metadata.jul"));
		
		validateBoolValue(gvt, "succ", true);
	}
	
}
