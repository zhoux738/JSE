package info.jultest.test.parser;

import static org.junit.Assert.fail;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.parser.JSEParsingHandlerBase;

public abstract class JulianParserRunner extends ParserRunnerBase {

	@Override
	public ParserRuleContext parse(CharStream input) {
		JulianLexer lexer = new JulianLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		JulianParser parser = new JulianParser(tokens);
		parser.addErrorListener(AbortingErrorHandler.INSTANCE);
		//parser.addErrorListener(new DiagnosticErrorListener());
		ProgramContext exeCtxt = parser.program();
 		//exeCtxt.inspect(parser);
		return exeCtxt;
	}
	
	static class AbortingErrorHandler extends JSEParsingHandlerBase {

		final static AbortingErrorHandler INSTANCE = new AbortingErrorHandler();
		
		private AbortingErrorHandler(){
			
		}

		@Override
		public void syntaxError(Recognizer<?, ?> arg0, Object arg1, int arg2,
			int arg3, String arg4, RecognitionException arg5) {
			fail("Parser encountered a syntax error.");	
		}
	}

}
