package info.jultest.test.parser;

import info.julang.langspec.regex.ast.RegexLexer;
import info.julang.langspec.regex.ast.RegexParser;
import info.julang.langspec.regex.ast.RegexParser.RegexContext;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.junit.Assert;

public abstract class RegexParserRunner extends ParserRunnerBase {

	@Override
	public ParserRuleContext parse(CharStream input) {
		return parse(input, false);
	}
	
	public void parseIllegalInput(String script){
		CharStream input = new ANTLRInputStream(script);			
		try {
			parse(input, true);
			Assert.fail("Passed while it shouldn't.");
		} catch (RecognitionException | RegexParsingException ex) {
			return;
		} 
	}
	
	private ParserRuleContext parse(CharStream input, boolean rethrow) {
		RegexLexer lexer = new RegexLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RegexParser parser = new RegexParser(tokens);
		RegexErrorListener listener = new RegexErrorListener(rethrow);
		//(Uncomment the following to get error in stdout)
		parser.removeErrorListeners();
		parser.addErrorListener(listener);
		//parser.addErrorListener(new DiagnosticErrorListener());
		RegexContext exeCtxt = parser.regex();
		//(Uncomment the following to inspect AST in visualization)
 		//exeCtxt.inspect(parser);
		listener.mayThrow();
		return exeCtxt;
	}

	static class RegexParsingException extends RuntimeException {

		private static final long serialVersionUID = 1L;
		
		RegexParsingException(String message){
			super(message);
		}
	}
	
	static class RegexErrorListener implements ANTLRErrorListener {

		private boolean rethrow;
		
		private RuntimeException exception;
		
		RegexErrorListener(boolean rethrow){
			this.rethrow = rethrow;
		}
		
		public void mayThrow() {
			if (exception != null){
				throw exception;
			}
		}

		@Override
		public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
				String msg, RecognitionException e) {
			// If invoked due to parsing error, e is null.
			if (rethrow && this.exception == null) {
				if (e == null) {
					this.exception = new RegexParsingException(msg);
				} else { 
					this.exception = e;
				}
			}
		}

		@Override
		public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
				BitSet ambigAlts, ATNConfigSet configs) {
			// NO-OP for now
			System.out.println("reportAmbiguity");
		}

		@Override
		public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
				BitSet conflictingAlts, ATNConfigSet configs) {
			// NO-OP			
			System.out.println("reportAttemptingFullContext");
		}

		@Override
		public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
				ATNConfigSet configs) {
			// NO-OP
			System.out.println("reportContextSensitivity");
		}
		
	}
}
