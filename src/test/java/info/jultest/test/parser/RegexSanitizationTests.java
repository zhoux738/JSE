package info.jultest.test.parser;

import java.util.regex.Pattern;

import org.junit.Test;

import info.julang.typesystem.jclass.jufc.System.Util.RegexSanitizer;
import info.julang.typesystem.jclass.jufc.System.Util.UnrecognizedRegexException;
import junit.framework.Assert;

public class RegexSanitizationTests {
	
	// Compatible regexes between Julian and Java
	@Test
	public void basicRegexTransformationTest() {
		transform("\\\\p", "\\p");
		transform("{1,2}", "{1,2}");
		transform("^abc*d?e+$", "abcccde");
		transform("abc|def", "abc");
		transform("(abc)|(def)", "def");
		transform("[1-9]*[a-zEFG]?", "357c", "E", "9a");
	}
	
	// Regexes supported by Java which must be trated literally in Julian
	@Test
	public void regexTransformationAsLiteralTest() {
		transform("\\\\d", "\\d", "false:3");
		transform("X{3}", "X{3}", "false:XXX");
	}

	// Regexes supported by Java but not by Julian
	@Test
	public void illegalRegexTransformationTest() {
		transformFail("[1-9&&[a-z]]");
		transformFail("\\p{Lower}");
		transformFail("X??");
		transformFail("(?:X)");
		transformFail("\\2");
		transformFail("\\d");
	}
	
	private void transform(String julRegex, String... inputs){
		String str = RegexSanitizer.sanitize(julRegex);
		//System.out.println(str);
		
		Pattern p = Pattern.compile(str);
		for(String input : inputs) {
			if (input.startsWith("false:")) {
				input = input.substring("false:".length());
				Assert.assertFalse(p.matcher(input).matches());
			} else{
				Assert.assertTrue(p.matcher(input).matches());
			}
		}
	}

	private void transformFail(String julRegex, String... inputs){
		try {
			String str = RegexSanitizer.sanitize(julRegex);
			Pattern.compile(str);
		} catch (UnrecognizedRegexException ex) {
			return;
		}
		
		Assert.fail("Passed while it shouldn't.");
	}
	
}
