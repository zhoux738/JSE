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

package info.julang.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import info.julang.dev.GlobalSetting;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.util.OSTool;

/**
 * The class to parse Julian script. The input must be a syntactically complete unit that 
 * potentially contains preamble, declaration and executable section. This means a global 
 * script, a module file, a function body, or an expression is parseable by this class. But
 * a code snippet that doesn't constitute any of above by itself is not. For example, while
 * <code>f(1, "a")</code> is totally legitimate, <code>(1, "a")</code> is not.
 * <p/>
 * First call {@link #parse(boolean)}, then call {@link #getAstInfo()} to get the result.
 * 
 * @author Ming Zhou
 */
public class ANTLRParser {

	private BadSyntaxException bse;
	private ProgramContext exeCtxt;
	private String fileName;
	private InputStream stream;
	private JSEParsingHandler handler;
	private FilterableTokenStream cts;
	
	/**
	 * Create a new parser instance. Use a different instance for each input stream.
	 * 
	 * @param fileName
	 * @param stream the input to parse.
	 */
	public ANTLRParser(String fileName, InputStream stream, boolean shouldCanonicalize){
		this.fileName = shouldCanonicalize ? OSTool.canonicalizePath(fileName) : fileName;
		this.stream = stream;
		handler = new JSEParsingHandler();
	}

	/**
	 * Scan the given text to create a token stream.
	 * 
	 * @param throwNow
	 * @return an AST info from which an AST can be demanded further.
	 */
	public LazyAstInfo scan(boolean throwNow){
		parse0(ParsingPhase.SCAN, throwNow);
		
		LazyAstInfo ainfo = new LazyAstInfo(this, fileName, bse);
		return ainfo;
	}
	
	/**
	 * Get the immediately preceding documentation block for the given token.
	 * 
	 * @param startTok the token to its right a search will be performed.
	 * @return the documentation block in its original form, including the enclosing 
	 * <code>&#047;*</code> and <code>*&#047;</code>. null if no documentation is found.
	 */
	public String getDoc(Token startTok){
		parse0(ParsingPhase.SCAN, true);
		int sindex = startTok.getTokenIndex();
		List<Token> list = cts.getHiddenTokensToLeft(sindex, JulianLexer.JULDOC);
		if (list != null){
			int total = list.size();
			if (total >= 0) {
				Token tok = list.get(total - 1);
				int index = tok.getTokenIndex();
				
				// The comment block must be the (logically) immediately preceding 
				// token. Otherwise it's not considered to be the doc for this node.
				int target = sindex - 1;
				if (index < target) {
					// If not, check if all the tokens in between are actually skipped tokens.
					List<Token> list2 = cts.getHiddenTokensToLeft(sindex, JulianLexer.SKIPPED);
					int required = target - index;
					int len = list2.size();
					for (int i = len - 1; i >=0; i--) {
						Token t2 = list2.get(i);
						if (t2.getTokenIndex() > index) {
							// A hit of skipped token within the range
							required--;
							if (required == 0) {
								break;
							}
						} else {
							// Already moved to the left of block comment.
							break;
						}
					}
					
					if (required == 0) {
						index = target;
					}
				}
				
				if (index == target) {
					// The comment block must be the (logically) immediately preceding 
					// token. Otherwise it's not considered to be the doc for this node.
					String doc = tok.getText();
					return doc != null ? doc : "";
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Parse the given text to build AST. This can be called without {@link #scan(boolean)} getting called first.
	 * 
	 * @param stream
	 * @param throwNow if true, throw the exception if parsing failed; otherwise, the exception can be demanded from the result.
	 */
	public void parse(boolean buildTree, boolean throwNow){
		parse0(buildTree ? ParsingPhase.TREE_BUILD : ParsingPhase.PARSE, throwNow);
	}
	
	/**
	 * Get the parsed AST, along with other info, after {@link #parse} is called.
	 * 
	 * @return AstInfo that may contain either the successfully parsed AST, or the exception thrown during parsing. 
	 */
	public AstInfo<ProgramContext> getAstInfo(){
		if (bse != null){
			return AstInfo.fail(bse, fileName);
		} else if (exeCtxt != null) {
			return AstInfo.succ(exeCtxt, fileName);
		} else {
			throw new JSEError("The AST is demanded before parsing.");
		}
	}
	
	/**
	 * Get the token stream.
	 * 
	 * @return A {@link FilterableTokenStream}, which can return either a stream comprised of only filtered tokens, or one that contains all.
	 * @throws BadSyntaxException if a scanning error occurs.
	 */
	public FilterableTokenStream getTokenStream(){
    	if (cts == null) {
    		parse0(ParsingPhase.SCAN, true);
    	}
    	
		return cts;
	}
	
	// scan, and potentially parse
	private void parse0(ParsingPhase phase, boolean throwNow){
	    try {
	    	// Scan
	    	if (cts == null) {
				CharStream input = new ANTLRInputStream(stream);
				JulianLexer lexer = new JulianLexer(input);
				if (!GlobalSetting.EnableANTLRDefaultErrorReport){
					lexer.removeErrorListeners();
					lexer.addErrorListener(handler);			
				}
				cts = new FilterableTokenStream(lexer);    		
	    	}

	    	// Parse
			if (phase != ParsingPhase.SCAN) {
				parse1(phase, cts, handler, throwNow);
			}
		} catch (IOException e) {
			throw new JSEError(e);
		} catch (RecognitionException e) {
			throw new BadSyntaxException("Parser encountered a syntax error: " + e.getMessage());
		}
	}
	
	// parse and build AST
	private void parse1(ParsingPhase phase, CommonTokenStream cts, JSEParsingHandler handler, boolean throwNow){
		if (exeCtxt == null && bse == null){
			JulianParser parser = new JulianParser(cts);

			parser.setBuildParseTree(phase == ParsingPhase.TREE_BUILD);
			
			if (!GlobalSetting.EnableANTLRDefaultErrorReport){
				parser.removeErrorListeners();
				parser.addErrorListener(handler);
			}
			
			// Two-stage parsing: SLL => LL
			// TODO - We should use BailErrorStrategy when parsing SLL, but our syntax doesn't use explicit EOF so that
			// this can pass on a sub tree without faulting. To fix this, must add EOF to syntax rule. But we have multiple
			// places where the contents to parse do not come from a file, thus an artificial EOF is required.
			// SLL => parser.setErrorHandler(new DefaultErrorStrategy());
			// LL  => parser.setErrorHandler(new BailErrorStrategy());
			try {
				parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
				exeCtxt = parser.program();
			} catch (Exception ce) {
				parser.reset();
				parser.getInterpreter().setPredictionMode(PredictionMode.LL);
				exeCtxt = parser.program();
			}
			
			// Uncomment the following to visualize the tree during debugging.
			// exeCtxt.inspect(parser);		
		}
		
		if (bse != null && throwNow){
			throw bse;
		}
	}
	
	/**
	 * A simple parsing handler that filters certain LL-ambiguity issues and 
	 * throws {@link BadSyntaxException} on parsing errors.
	 * 
	 * @author Ming Zhou
	 */
	private class JSEParsingHandler extends JSEParsingHandlerBase {
		@Override
		public void syntaxError(
			Recognizer<?, ?> arg0, Object tok, int lineNo, int columnNo, String msg, RecognitionException ex) {
			// Remember the first offending token. A parsing error is fatal and not recoverable, 
			// so it is pointless to keep track of other issues.
			if (ANTLRParser.this.bse == null){
				org.antlr.v4.runtime.Token faultingTok = null;
				if (tok != null && tok instanceof org.antlr.v4.runtime.Token){
					faultingTok = (org.antlr.v4.runtime.Token)tok;
				} else {
					tok = UnknowToken.INSTANCE;
				}

				ANTLRParser.this.bse = new BadSyntaxException(
					"Encountered a syntax error during parsing.", fileName, faultingTok);
			}
		}
	}
	
	private static class UnknowToken implements org.antlr.v4.runtime.Token {
		private static final UnknowToken INSTANCE = new UnknowToken();
		private UnknowToken(){}
		public String getText() { return ""; }
		public int getType() { return 0; }
		public int getLine() { return 0; }
		public int getCharPositionInLine() { return 0; }
		public int getChannel() { return 0; }
		public int getTokenIndex() { return 0; }
		public int getStartIndex() { return 0; }
		public int getStopIndex() { return 0;}
		public TokenSource getTokenSource() { return null; }
		public CharStream getInputStream() { return null; }
	}
}