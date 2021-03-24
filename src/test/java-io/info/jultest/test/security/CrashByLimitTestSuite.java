package info.jultest.test.security;

import static info.jultest.test.EFCommons.prepareViaFactory;

import java.io.IOException;

import org.junit.Test;

import info.julang.execution.security.EngineLimit;
import info.julang.external.EngineFactory.EngineParamPair;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtVariableTable;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;
import info.jultest.test.threading.ThreadingTestBase;

//(Uncomment @RunWith for reliability test)
//@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CrashByLimitTestSuite extends ThreadingTestBase {

	private static final String FEATURE = "ResourceLimit";

	@Test
	public void maxThreadTest() throws EngineInvocationError, IOException, InterruptedException {
		EngineParamPair pair = prepareViaFactory(null);
		String path = Commons.makeScriptPath(Commons.Groups.HOSTING, FEATURE, "limit_1.jul");
		
		IExtScriptEngine eng = pair.getFirst();
		IExtVariableTable gvt = pair.getSecond().getGlobalVariableTable();
		eng.setLimit(EngineLimit.MAX_THREADS.getPublicName(), 2);
		eng.runFile(path);
		
		EFCommons.validateBoolValue(gvt, "caught", true);
		EFCommons.validateBoolValue(gvt, "done", true);
	}
	
	@Test
	public void maxUsedMemoryTest() throws EngineInvocationError, IOException, InterruptedException {
		EngineParamPair pair = prepareViaFactory(null);
		String path = Commons.makeScriptPath(Commons.Groups.HOSTING, FEATURE, "limit_2.jul");
		
		IExtScriptEngine eng = pair.getFirst();
		IExtVariableTable gvt = pair.getSecond().getGlobalVariableTable();
		eng.setLimit(EngineLimit.MAX_USED_MEMORY_IN_BYTE.getPublicName(), 1024);
		eng.runFile(path);
		
		EFCommons.validateBoolValue(gvt, "caught", true);
		EFCommons.validateBoolValue(gvt, "done", true);
	}
}
