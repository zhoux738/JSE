package info.jultest.test.threading;

import info.julang.dev.GlobalSetting;

import org.junit.Assume;
import org.junit.Before;

public class ThreadingTestBase {

	@Before
	public void shouldRun() {
		Assume.assumeTrue(GlobalSetting.EnableMultiThreadingTests);
	}
	
}
