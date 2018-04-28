package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

public class System_Util_IIndexable_Tests extends ExceptionTestsBase {

	private static final String FEATURE = "Indexing";
	
	@Test
	public void customizedIndexerBasicTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "index_1.jul"));
		
		validateStringValue(gvt, "s1", "abc");
		validateStringValue(gvt, "s2", "abcdef");
		validateStringValue(gvt, "s3", "uvwxyz");
	}
	
	@Test
	public void customizedIndexerBasicTest2() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "index_2.jul"));
		
		validateIntValue(gvt, "i1", 3);
		validateIntValue(gvt, "i2", 8);
		validateIntValue(gvt, "i3", 18);
	}
	
	@Test
	public void customizedIndexerBasicTest3() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "index_3.jul"));

		validateIntValue(gvt, "i20", 20);
		validateIntValue(gvt, "i300", 300);
		validateIntValue(gvt, "i4k", 4000);
		validateIntValue(gvt, "i10k", 10000);
	}
	
	// Verify that indexer syntax ([index]) is interpreted as function call seamlessly.
	@Test
	public void customizedIndexerFaultTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		TestExceptionHandler teh = installExceptionHandler(engine);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "index_fault_1.jul"));
		
		validateException(
			teh, 
			"<default>.InvalidIndexException",
			"The index must be non-negative.",
			new String[]{
				"at(<default>.MyContainer,Any)  (/.../index_fault_1.jul, 22)"
			},
			null,
			34);
	}
}
