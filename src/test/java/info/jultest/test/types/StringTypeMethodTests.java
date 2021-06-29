package info.jultest.test.types;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestRunner;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.AttemptToChangeConstException;

import org.junit.Before;
import org.junit.Test;

public class StringTypeMethodTests {

	private static final String FEATURE = "BuiltIns";

	private VariableTable gvt;
	private SimpleScriptEngine engine;
	
	private EngineInvocationError exception;
	
	@Before
	public void setUp() {
		gvt = new VariableTable(null);
		engine = makeSimpleEngine(gvt, true);
		try {
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "stringtests_1.jul"));
		} catch (EngineInvocationError e) {
			exception = e;
		}
	}
	
	/**
	 * hashCode()
	 */
	@Test
	public void hashCodeTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateBoolValue(gvt, "b5", true);
		validateBoolValue(gvt, "b6", true);
	}
	
	/**
	 * length
	 */
	@Test
	public void lengthTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateIntValue(gvt, "len", 6);
	}
	
	/**
	 * cannot assign a value to string's length field
	 */
	@Test
	public void lengthTest2() throws EngineInvocationError {
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("stringtests_2.jul", new AttemptToChangeConstException());
	}
	
	/**
	 * endsWith(string)
	 */
	@Test
	public void endsWithTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateBoolValue(gvt, "b1", true);
		validateBoolValue(gvt, "b2", false);
	}
	
	/**
	 * startsWith(string)
	 */
	@Test
	public void startsWithTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateBoolValue(gvt, "b3", true);
		validateBoolValue(gvt, "b4", false);
	}
	
	/**
	 * indexOf(string, int)
	 */
	@Test
	public void indexOfTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateIntValue(gvt, "i1", 0);
		validateIntValue(gvt, "i2", 1);
		validateIntValue(gvt, "i3", -1);
	}
	
	/**
	 * firstOf(string)
	 */
	@Test
	public void firstOfTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateIntValue(gvt, "i4", 0);
		validateIntValue(gvt, "i5", 1);
		validateIntValue(gvt, "i6", -1);
	}
	
	/**
	 * trim()
	 */
	@Test
	public void trimTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateStringValue(gvt, "s1", "abc");
		validateStringValue(gvt, "s2", "abc");
	}
	
	/**
	 * toUpper(), toLower()
	 */
	@Test
	public void toCaseTest() throws EngineInvocationError {
		throwIfFailed();
		
		validateStringValue(gvt, "ls", "abcdef");
		validateStringValue(gvt, "us", "ABCDEF");
	}
	
	/**
     * replace(string, string)
     */
    @Test
    public void replace() throws EngineInvocationError {
        throwIfFailed();
        
        validateStringValue(gvt, "s5", "x-yz-");
        validateStringValue(gvt, "s6", "xabcyzabc");
        validateStringValue(gvt, "s7", "0yz0");
    }
	
    /**
     * split(string)
     */
	@Test
	public void splitTest() throws EngineInvocationError {
		throwIfFailed();
	
		// string[] strs0 = "ab".split(':');
	    // string[] strs1 = "ab:cd".split(':');
	    // string[] strs2 = "ab:cd:ef".split(':');
	    // string[] strs3 = ":ab:cd:ef:".split(':');
		validateStringArrayValue(gvt, "strs0", new String[]{"ab"});
		validateStringArrayValue(gvt, "strs1", new String[]{"ab", "cd"});
		validateStringArrayValue(gvt, "strs2", new String[]{"ab", "cd", "ef"});
		validateStringArrayValue(gvt, "strs3", new String[]{"", "ab", "cd", "ef"});
	}
	
	private void throwIfFailed() throws EngineInvocationError {
		if(exception != null){
			throw exception;
		}
	}
	
}
