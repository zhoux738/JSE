package info.jultest.test.external;

// import static info.jultest.test.Commons.SRC_REPO_ROOT;
import static info.jultest.test.EFCommons.runViaFactory;
import static info.jultest.test.EFCommons.validateIntValue;
import static info.jultest.test.EFCommons.validateStringValue;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtVariableTable;

import org.junit.Test;

public class RunViaEngineFactoryTestSuite {
	
	// Script execution, system class loading, test infrastructure
	@Test
	public void basicScriptTest() throws EngineInvocationError {
		IExtEngineRuntime rt = runViaFactory("ft_1.jul");
		IExtVariableTable gvt = rt.getGlobalVariableTable();
		validateIntValue(gvt, "i", 5);
		validateStringValue(gvt, "s", "abc");
	}
}
