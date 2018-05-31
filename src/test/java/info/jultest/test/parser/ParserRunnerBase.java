package info.jultest.test.parser;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import info.jultest.test.Commons;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;

public abstract class ParserRunnerBase {

	public abstract String getFeature();
	
	public abstract ParserRuleContext parse(CharStream input);
	
	public void parseFile(String fileName){
	    try {
	    	String path = Commons.PARSING_ROOT + getFeature() + "/" + fileName;
			CharStream input = new ANTLRFileStream(path);			
			invokeParser0(input);
		} catch (IOException e) {
			fail(e.getMessage());
		}
	}
	
	public void parseScript(String script){
		CharStream input = new ANTLRInputStream(script);			
		invokeParser0(input);
	}
	
	private void invokeParser0(CharStream input){
	    try {		
			ParserRuleContext exeCtxt = parse(input);
			assertNull(exeCtxt.exception);
		} catch (RecognitionException e) {
			fail(e.getMessage());
		}
	}
	
}
