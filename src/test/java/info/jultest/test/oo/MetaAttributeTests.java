package info.jultest.test.oo;

import org.junit.Assume;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.dev.GlobalSetting;
import info.julang.external.exceptions.EngineInvocationError;

public class MetaAttributeTests extends AttributeTestsBase {
	
	/*
	 * Apply meta-attribute with wrong type target
	 */
	@Test
	public void applyMetaAttributeFailedTest1() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("attr_meta_1.jul", "System.ClassLoadingException", -1, "System.IllegalAttributeUsageException", -1);
	}
	
	/*
	 * Apply meta-attribute with wrong plurality
	 */
	@Test
	public void applyMetaAttributeFailedTest2() throws EngineInvocationError {
		Assume.assumeTrue(GlobalSetting.EnableJSE);
		
		ExceptionTestRunner runner = new ExceptionTestRunner(Commons.Groups.OO, FEATURE);
		runner.executeAndExpect("attr_meta_2.jul", "System.ClassLoadingException", -1, "System.IllegalAttributeUsageException", -1);
	}
	
}
