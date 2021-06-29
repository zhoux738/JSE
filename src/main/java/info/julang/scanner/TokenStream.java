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

package info.julang.scanner;

import java.util.List;

import org.antlr.v4.runtime.Token;

import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.FilterableTokenStream;

/**
 * The base token stream implements the {@link ITokenStream} interface.
 * <p>
 * This class is not thread safe.
 * 
 * @see ITokenStream
 * 
 * @author Ming Zhou
 */
public class TokenStream implements ITokenStream {
	
	/**
	 * Program Counter points to the location where next token is to be read (instead of having been read).
	 */
	private int PC = 0;
	
	/**
	 * The all constituting tokens of the stream.
	 */
	private List<Token> tokens;
	
	/**
	 * An index larger than this value is out of right bound.
	 */
	private int max;

	private int marked = UNMARKED;
	
	private static final int UNMARKED = -100;
	
	/**
	 * Create a new stream directly from the original ANTLR token stream.
	 * 
	 * @param cts the original ANTLR token stream.
	 */
	public TokenStream(FilterableTokenStream cts) {
		cts.fill();
		tokens = cts.getFilteredTokens();
		max = tokens.size() - 1;
	}
	
	@Override
	public Token next() {
		Token tok = null;
		if (PC > max){
			tok = tokens.get(max); 
		} else {
			tok = tokens.get(PC);
			PC++;
		}

		return tok;
	}

	@Override
	public Token peek() { // The implementation is different from next() only in that PC doesn't move.
		Token tok = null;
		if (PC > max){
			tok = tokens.get(max); 
		} else {
			tok = tokens.get(PC);
		}
		
		return tok;
	}
	
	@Override
	public boolean move(StreamDirection direction, int offset) {		
		// 1) determine the direction
		boolean forward = true;
		if(direction == StreamDirection.FORWARD && offset > 0 ||
		   direction == StreamDirection.BACKWARD && offset < 0){
			forward = true;
		} else if(direction == StreamDirection.FORWARD && offset < 0 ||
				  direction == StreamDirection.BACKWARD && offset > 0){
			forward = false;
		} else {
			// No need to move
			return true;
		}
		offset = Math.abs(offset);
		
		// 2) move based on direction 
		if(!forward){
			// good. we are moving backward and don't need call next() at all.
			PC -= offset;
			if(PC < 0){
				PC = 0;
				return false; // moved beyond start.
			} else {
				return true;			
			}
		} else {
			PC += offset;
			if (PC > max){
				PC = max;
				return false; // moved beyond end.
			} else {
				return true;
			}
		}
	}
	
	@Override
	public boolean seek(StreamPosition pos, int offset) {
		if (pos == StreamPosition.START){
			PC = 0;
		}
		
		PC += offset;
		if (PC > max){
			PC = max;
			return false; // moved beyond end.
		} else if (PC < 0){
			PC = 0;
			return false; // moved beyond end.
		} else {
			return true;
		}
	}

	@Override
	public boolean backoff() {
		if(PC == 0){
			return false;
		}
		PC--;
		return true;
	}
	
	@Override
	public Token prev() {
		Token tok = null;
		if (PC > 0){
			tok = tokens.get(PC - 1);
		}

		return tok; // can be null
	}

	@Override
	public boolean locate(StreamDirection direction, int kind) {
		int type = JulianLexer.EOF;
		
		if(direction == StreamDirection.FORWARD){
			while (true) {
				type = next().getType();
				if (type == JulianLexer.EOF){
					return false; // Didn't find.
				}
				
				if (type == kind){
					PC--; // backup one token
					return true;
				}
			}
		} 
		
		if (direction == StreamDirection.BACKWARD){
			Token tok = null;
			PC--;
			while(PC >= 0){
				tok = tokens.get(PC);
				if (tok.getType() == kind){
					return true;
				}
				
				PC--;
			}
		}
		
		return false;
	}
	
	@Override
	public void mark() {
		marked = PC;
	}
	
	@Override
	public void reset() {
		if (marked != UNMARKED){
			PC = marked;
		}
		
		marked = UNMARKED; // Once reset, the previous mark will be gone. Calling reset() again won't affect PC.
	}
}
