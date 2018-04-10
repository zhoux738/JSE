package info.jultest.test.parser;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import info.jultest.test.Commons;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.parser.JSEParsingHandlerBase;

import java.io.IOException;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public abstract class ParserRunner {

	public abstract String getFeature();
	
	public void invokeParser(String fileName){
	    try {
	    	String path = Commons.PARSING_ROOT + getFeature() + "/" + fileName;
			CharStream input = new ANTLRFileStream(path);
			JulianLexer lexer = new JulianLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JulianParser parser = new JulianParser(tokens);
			parser.addErrorListener(AbortingErrorHandler.INSTANCE);
			//parser.addErrorListener(new DiagnosticErrorListener());
			ProgramContext exeCtxt = parser.program();
     		//exeCtxt.inspect(parser);
			assertNull(exeCtxt.exception);
			return;
		} catch (RecognitionException e) {
			fail(e.getMessage());
		} catch (IOException e) {
			fail(e.getMessage());
		}
	    
		fail();
	}
	
	private static class AbortingErrorHandler extends JSEParsingHandlerBase {

		private final static AbortingErrorHandler INSTANCE = new AbortingErrorHandler();
		
		private AbortingErrorHandler(){
			
		}

		@Override
		public void syntaxError(Recognizer<?, ?> arg0, Object arg1, int arg2,
			int arg3, String arg4, RecognitionException arg5) {
			fail("Parser encountered a syntax error.");	
		}
	}
	
}
