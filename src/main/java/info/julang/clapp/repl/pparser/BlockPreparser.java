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

package info.julang.clapp.repl.pparser;

import java.io.ByteArrayInputStream;

import org.antlr.v4.runtime.Token;

import info.julang.clapp.repl.IPreparser;
import info.julang.clapp.repl.IPreparserReplaceable;
import info.julang.clapp.repl.IPrinter;
import info.julang.clapp.repl.IShellConsole;
import info.julang.clapp.repl.REPLParsingException;
import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.parser.ANTLRParser;
import info.julang.parser.AstInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.scanner.ITokenStream;
import info.julang.util.Pair;

/**
 * The block pre-parser processes by these rules:  
 * <p>
 * 1. Reads every character in the line and keep a pair count on '{'/'}'.<br>
 * 2. Ignore the ending '\', if any. '\' does signal that the user wants to 
 *    continue the input after the current line.<br>
 * 3. Finishes reading once the matching '}' is hit. After that, transform back to 
 * {@link RegularPreparser} and return an accumulated string, which may or may not
 * be syntactically legal.
 * <p>
 * Although limited, this pre-parser also supports smart indentation. The new 
 * continuation line will always start with same number of space as the previous 
 * line, except in case of such a line ending with '{', add four more spaces to 
 * the leading spaces.
 * 
 * @author Ming Zhou
 */
public class BlockPreparser implements IPreparser {

    private final static String NEW_BLOCK_INDENT = "  ";
    public final static BlockPreparser INSTANCE = new BlockPreparser();
    
    private StringBuilder sb;
    private int openPairs;
    private String lead;
    private IShellConsole shConsole;
    
    private BlockPreparser(){}
    
    public String parse(IPreparserReplaceable console, String raw) throws REPLParsingException {
    	// 1) Prepare the input - ignore the trailing '\'
    	//    The input can end with one or more \ concatenated together, but they may be separated from the rest by a blank char
    	//    
    	//    text  \      legal
    	//    text \\\     legal
    	//    text\        illegal
    	//    text\\\      illegal
    	//    text \ \     legal, but will likely cause syntax error
    	
    	Pair<String, Boolean> pair = PreparsingHelper.removeTrailingBackslashes(raw);
    	String s = pair.getFirst();
    	boolean hasBackSlash = pair.getSecond();
    	
        if ("".equals(s)){
            return null;
        }
        
        // 2) Scan the input
        LazyAstInfo laInfo = null;
        ByteArrayInputStream bais = new ByteArrayInputStream(s.getBytes());
        ANTLRParser ap = new ANTLRParser("<temp>", bais, false);
        try {
            laInfo = ap.scan(true);
        } catch (BadSyntaxException bse) {
            console.setPreparser(RegularPreparser.INSTANCE);
            throw new REPLParsingException("Illegal tokens detected: " + bse.getMessage());
        }
        
        // 3) Count the number of { and }
        ITokenStream stream = laInfo.getTokenStream();
        boolean shouldContinue = true;
        while(shouldContinue){
            Token tok = stream.next();
            switch(tok.getType()){
            case JulianLexer.LEFT_CURLY:
                openPairs++;
                break;
            case JulianLexer.RIGHT_CURLY:
                openPairs--;
                if (openPairs < 0){
                    // Excessive '}'. The input cannot be legitimate.
                    shouldContinue = false;
                    break;
                }
                break;
            case JulianLexer.EOF:
                shouldContinue = false;
                break;
            }
        }
        
        // 4) Verdict
        // 4.1) openPairs == 0: pairs are balanced and at least one type definition is concluded.
    	//      stop parsing with this pre-parser and wrap up the input unless we end with a '{', 
    	//      indicating the user has more to input. 
        // 4.2) openPairs < 0: excessive '}'. The input cannot be legitimate. But we still send 
        //      the input back and let the regular pre-parser to handle the syntax errors.
        shouldContinue = openPairs > 0;
        // 4.3) openPairs > 0: more '}' are needed.
        
        // The verdict is overwritten if the user explicitly asks for continuation
        shouldContinue = shouldContinue || hasBackSlash;
        
        // 5) Accumulate input and return
        sb.append(System.lineSeparator());
        sb.append(s);
        
        if (shouldContinue) {
            // adjust smart indentation
            byte[] bs = raw.getBytes();
            int i = 0;
            int len = bs.length;
            for(; i < len; i++){
                switch(bs[i]){
                case ' ':
                case '\t':
                    continue;
                default:
                	len = -1;
                	i--;
                    break;
                }
            }
            
            lead = i > 0 ? raw.substring(0, i) : "";
            
            if (s.endsWith("{")) {
                lead += NEW_BLOCK_INDENT;
            }
        	
            return null;
        } else {
            if (!s.endsWith("}") && !s.endsWith(";")){
                sb.append(";");
            }
            console.setPreparser(RegularPreparser.INSTANCE);
            return sb.toString();
        }
    }
    
    public boolean reset(String raw) {
        openPairs = 0;
        sb = null;
        lead = NEW_BLOCK_INDENT;
        
        String sc = raw + "}";
        ByteArrayInputStream bais = new ByteArrayInputStream(sc.getBytes());
        ANTLRParser ap = new ANTLRParser("<temp>", bais, false);
        ap.parse(false, false); // Parse only - no AST needed here.
        AstInfo<ProgramContext> ainfo = ap.getAstInfo();
        BadSyntaxException bse = ainfo.getBadSyntaxException();
        
        if (bse == null) {
            // If passed, prepare to start further parsing.
            sb = new StringBuilder();
            sb.append(raw.trim());
            openPairs = 1; // Since by complementing with just a single '}' the whole input 
                           // gets parsed, it's safe to say that there is only one open pair.
            return true;
        } else {
            return false;
        }
    }
    
    public void prompt(IPrinter console) {
    	shConsole.print(PROMPT_CONT);

        // The leading spaces must be input by simulating keyboard input events. If printing 
    	// the whole string out not only will they become part of output and unmodifiable, 
    	// in the next iteration we will not get them from the input.
    	shConsole.input(lead);
    }

	public void setShellConsole(IShellConsole shConsole) {
		this.shConsole = shConsole;
	}
}
