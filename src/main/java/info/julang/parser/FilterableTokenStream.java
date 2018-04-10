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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.WritableToken;

import info.julang.langspec.ast.JulianLexer;

public class FilterableTokenStream extends CommonTokenStream {

	public FilterableTokenStream(TokenSource tokenSource) {
		super(tokenSource);
	}
	
	private List<Token> ftokens = new ArrayList<Token>(100);

	/*
	 * The majority of this piece of code is copied directly from BufferedTokenStream.fetch(int).
	 * This is due to fact that we cannot customize the behavior of token adding operation.
	 * 
	 * Original copyright information, as is required by BSD license:
	 * 
	 * [The "BSD license"]
	 *  Copyright (c) 2012 Terence Parr
	 *  Copyright (c) 2012 Sam Harwell
	 *  All rights reserved. 
	 */
	@Override
    protected int fetch(int n) {
		if (fetchedEOF) {
			return 0;
		}

        for (int i = 0; i < n; i++) {
            Token t = tokenSource.nextToken();
            if ( t instanceof WritableToken ) {
                ((WritableToken)t).setTokenIndex(tokens.size());
            }
            tokens.add(t);

            // This 'if' branch is the only difference from the parent class
            if ( t.getChannel()==JulianLexer.DEFAULT_TOKEN_CHANNEL ) {
                ftokens.add(t);
            }
            
            if ( t.getType()==Token.EOF ) {
				fetchedEOF = true;
				return i + 1;
			}
        }

		return n;
    }
	
	public List<Token> getFilteredTokens(){
		return ftokens;
	}
    
}
