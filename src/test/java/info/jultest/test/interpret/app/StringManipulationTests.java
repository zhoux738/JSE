package info.jultest.test.interpret.app;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;

import org.junit.Test;

public class StringManipulationTests {

	private static final String FEATURE = "App/StringManipulation";
	
	@Test
	public void reverseWordsInStringTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, "reverse.jul"));
		
		validateStringValue(gvt, "s2", "box the over jumping is fox");
	}
}
