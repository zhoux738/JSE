package info.jultest.test.threading;

import static info.jultest.test.EFCommons.prepareViaFactory;

import java.io.IOException;

import org.junit.Test;

import info.julang.external.EngineFactory.EngineParamPair;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtVariableTable;
import info.jultest.test.Commons;
import info.jultest.test.EFCommons;

//(Uncomment @RunWith for reliability test)
//@org.junit.runner.RunWith(org.junit.runners.Parameterized.class)
public class CarshByLimitTestSuite extends ThreadingTestBase {

	private static final String FEATURE = "CrashByLimit";

	@Test
	public void crashMainThreadTest() throws EngineInvocationError, IOException, InterruptedException {
		EngineParamPair pair = prepareViaFactory(null);
		String path = Commons.makeScriptPath(Commons.Groups.THREADING, FEATURE, "limit_1.jul");
		
		IExtScriptEngine eng = pair.getFirst();
		IExtVariableTable gvt = pair.getSecond().getGlobalVariableTable();
		eng.setLimit("max.threads", 2);
		eng.runFile(path);
		
		EFCommons.validateBoolValue(gvt, "caught", true);
		EFCommons.validateBoolValue(gvt, "done", true);
	}
}
