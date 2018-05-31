package info.jultest.test.parser;

import org.junit.Test;

public class RegexBasicParsingTests extends RegexParserRunner {
	
	@Override
	public String getFeature() {
		return "";
	}
	
	// Some most basic patterns
	@Test
	public void regexParserBasicTest() {
		// sequence and qualifiers
	    parseScript("abc*d+e?fgh");
	    
	    // union
	    parseScript("abc|def|ghi");
	    
	    // grouping
	    parseScript("(abc|d(e*)f)|ghi");
	    
	    // range
	    parseScript("k*[a-zA-Z]e?");
	    
	    // start and end
	    parseScript("^[^0-9\\.]$");
	}
	
	// Special chars
	@Test
	public void regexParserSpecialCharsTest() {
		String chars = new String("[]()?+*|.^$\\");
		StringBuilder sb = new StringBuilder();
		for(char c : chars.toCharArray()){
			sb.append('\\');
			sb.append(c);
		}
		
	    parseScript(sb.toString());
	    
	    parseIllegalInput("\\a");
	}
	
	// Illegal input
	@Test
	public void regexParserIllegalInputTest() {
		parseIllegalInput("[^_^]");
		parseIllegalInput("(a()");
		parseIllegalInput("[$]");
	}
}
