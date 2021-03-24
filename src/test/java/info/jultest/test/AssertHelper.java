package info.jultest.test;

import info.julang.util.OSTool;
import org.junit.Assert;

public final class AssertHelper {

	/**
	 * Serially search the input and ascertain that it contains each given string in that order.
	 * The forward and backward slashes in each given string will be replaced with the correct separater in the current OS.
	 * 
	 * @param input
	 * @param strings
	 */
	public static void validateStringOccurences(String input, String... strings){
		int index = 0;
		for(String str : strings){
			str = canonicalize(str);
			int found = input.indexOf(str, index);
			Assert.assertTrue("Cannot find " + str, found >= 0);
			index = found + str.length();
		}
	}
	
	/**
	 * Serially search the input and ascertain that it contains each given string without specific order.
	 * The forward and backward slashes in each given string will be replaced with the correct separater in the current OS.
	 * 
	 * @param input
	 * @param strings
	 */
	public static void validateStringOccurencesUnordered(String input, String... strings){
		for(String str : strings){
			str = canonicalize(str);
			int found = input.indexOf(str, 0);
			Assert.assertTrue("Cannot find " + str, found >= 0);
		}
	}
	
	private static String canonicalize(String str) {
		boolean isWin = OSTool.isWindows();
		if (isWin && str.contains("/") || !isWin && str.contains("\\")) {
			str = OSTool.canonicalizePath(str);
		}
		
		return str;
	}
}
