package info.jultest.test.types;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;

import org.junit.Test;

public class StringTypeConversionTests {

	private static final String FEATURE = "BuiltIns";
	
	@Test
	public void stringFromBytesTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "string_bytes_1.jul"));
		
		validateStringValue(gvt, "s1", "abc");
		validateStringValue(gvt, "s2", "bcd");
	}
	
    @Test
    public void stringFromBytesTest2() throws EngineInvocationError {
        VariableTable gvt = new VariableTable(null);
        SimpleScriptEngine engine = makeSimpleEngine(gvt);
        engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
        
        engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "string_bytes_2.jul"));
        
        validateBoolValue(gvt, "b0", true);
        validateBoolValue(gvt, "b1", true);
        validateBoolValue(gvt, "b2", true);
        validateBoolValue(gvt, "b3", true);
        validateBoolValue(gvt, "b4", true);
    }
    
    @Test
    public void stringFromIntTest() throws EngineInvocationError {
        VariableTable gvt = new VariableTable(null);
        SimpleScriptEngine engine = makeSimpleEngine(gvt);
        engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
        
        engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "string_int.jul"));
        
        validateIntValue(gvt, "i1", 32);
        validateIntValue(gvt, "i2", 35);
        validateBoolValue(gvt, "b3", true);
    }
}
