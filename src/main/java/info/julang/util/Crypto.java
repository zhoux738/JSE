/*
MIT License

Copyright (c) 2017 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import info.julang.external.exceptions.JSEError;

public final class Crypto {

	private Crypto() { }

	/**
	 * Calculate Secure Hash 256 in hexadecimal string from then given input.
	 * 
	 * @param input
	 * @param digitsToPreserve The even number of hexadecimal digits to return. 
	 * [1, 64], increment by one if it's odd. Any number beyond the range is 
	 * treated as to return all.
	 * @return A hexadecimal string representing the SHA256 result from the input.
	 */
	public static String sha256(String input, int digitsToPreserve) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(input.getBytes(StandardCharsets.US_ASCII));
			
			int count;
			if (digitsToPreserve <= 0 || digitsToPreserve > hash.length) {
				count = hash.length;
			} else {
				digitsToPreserve = (digitsToPreserve >> 1) + ((digitsToPreserve & 1) == 1 ? 1 : 0);
				count = (digitsToPreserve < 0 || digitsToPreserve > hash.length) ? hash.length : digitsToPreserve;
			}
			
			char[] cs = new char[count << 1];
			for (int j = 0; j < count; j++) {
				byte b = hash[j];
				int i = j << 1;
				int bh = (b & 0xF0) >> 4;
				if (bh <= 9) {
					char c = (char)((int)'0' + bh);
					cs[i] = c;
				} else {
					char c = (char)((int)'A' + (bh - 10));
					cs[i] = c;
				}
				
				int bl = b & 0x0F;
				if (bl <= 9) {
					char c = (char)((int)'0' + bl);
					cs[i + 1] = c;
				} else {
					char c = (char)((int)'A' + (bl - 10));
					cs[i + 1] = c;
				}
			}

			String hex = String.copyValueOf(cs);
			return hex;
		} catch (NoSuchAlgorithmException e) {
			throw new JSEError("Cannot produce hash value.", e);
		}
	}	
}
