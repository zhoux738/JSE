package info.jultest.test;

import junit.framework.Assert;

public final class AssertHelper {

	/**
	 * Serially search the input and ascertain that it contains each given string in that order.
	 * 
	 * @param input
	 * @param strings
	 */
	public static void validateStringOccurences(String input, String... strings){
		int index = 0;
		for(String str : strings){
			int found = input.indexOf(str, index);
			Assert.assertTrue("Cannot find " + str, found >= 0);
			index = found + str.length();
		}
	}
	
}
