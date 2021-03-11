package info.jultest.test.components;

import org.junit.Assert;
import org.junit.Test;

import info.julang.external.exceptions.EngineInvocationError;
import info.julang.util.Crypto;

public class UtilTests {

	@Test
	public void sha256Test() throws EngineInvocationError {
		String hash1 = Crypto.sha256("hello world", 8);
		Assert.assertEquals("B94D27B9", hash1.toUpperCase());
		String hash2 = Crypto.sha256("hello world", 7);
		Assert.assertEquals("B94D27B9", hash2.toUpperCase());
		String hash3 = Crypto.sha256("hello world", 9);
		Assert.assertEquals("B94D27B993", hash3.toUpperCase());
		String hash4 = Crypto.sha256("hello world", 0);
		Assert.assertEquals("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9", hash4.toLowerCase());
	}
	
}
