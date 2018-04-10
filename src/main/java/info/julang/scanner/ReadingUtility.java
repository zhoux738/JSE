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

import info.julang.langspec.ast.JulianLexer;

import org.antlr.v4.runtime.Token;

public class ReadingUtility {

	/**
	 * Given a left-end token, read the stream until it locates the matching right-end token.
	 * <p>
	 * For example, if calling this method with leftTok = '[', rightTok = ']', 
	 * <b>startsWithOpenPair</b> = false, and <b>stopBeforeMatch</b> = false,
	 * for the following stream the method will exits at the marked place: <pre><code> Before
	 *    [ [ ] ] [ ]
	 *   ^
	 * After
	 *    [ [ ] ] [ ]
	 *           ^</code></pre>This method changes PC during processing. If it cannot match a 
	 * pair, it will not restore the original PC.
	 *           
	 * @param stream
	 * @param leftTok
	 * @param rightTok
	 * @param startsWithOpenPair 
	 * 		the method starts with a left token already scanned, 
	 *		therefore expecting only a matched right token.
	 * @param stopBeforeMatch 
	 * 		whether to consume the located right-end token
	 * @param customChecker 
	 * 		if not null, will be called against each scanned token. the method 
	 *      will abort and return false if customChecker returns false.
	 * @return true if found, false if not. If not found, PC is pointing at EOF.
	 */
	public static boolean locatePairedToken(
		ITokenStream stream, 
		int leftTok, 
		int rightTok, 
		boolean startsWithOpenPair, 
		boolean stopBeforeMatch) {
		
		int status = startsWithOpenPair ? 1 : -1;
		boolean firstTime = true;
		Token tok = null;
		while((tok = stream.next()).getType() != JulianLexer.EOF){
			if(tok.getType() == leftTok){
				// If it is a left-end token, increment pair count 
				status++;
			} else if(tok.getType() == rightTok){
				// If it is a right-end token,
				if(!firstTime || startsWithOpenPair){
					// and if the first left-end token has been met, decrement pair count
					status--;			
				}
			}
			
			if(status == 0){
				if(firstTime && !startsWithOpenPair){
					// If this is the first time we met left-end token, we increment pair count from 0 to 1
					firstTime = false;
					status++;
					continue;
				}
				// otherwise, a pair is found and closed by now.
				
				if(stopBeforeMatch){
					stream.backoff();
				}
				
				return true;
			}
		}
		
		return false;
	}
}
