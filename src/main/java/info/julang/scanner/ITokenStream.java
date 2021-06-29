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

import org.antlr.v4.runtime.Token;

/**
 * A token stream scanned out of a Julian script.
 * <p>
 * This interface defines actions that can be done against a stream of tokens which are converted 
 * from the original script source.
 * <p>
 * An implicit component of any implementation of this interface is a Program Counter, or simply 
 * PC. At any point, PC points to a token that is yet to be read. In the following example, symbol ^
 * represents the location of PC.<pre><code>   [TOK 1] [TOK 2] [TOK 3] [TOK 4] [TOK EOF]
 *          ^
 *   At this moment, calling {@link #next()} will read out [TOK 2] and move PC forward by one token
 *   
 *   [TOK 1] [TOK 2] [TOK 3] [TOK 4] [TOK EOF]</code></pre>PC cannot move beyond the stream's scope. 
 * The two ends are<pre><code> At start
 *   [TOK 1] [TOK 2] [TOK 3] [TOK 4] [TOK EOF]
 *  ^
 * At end
 *   [TOK 1] [TOK 2] [TOK 3] [TOK 4] [TOK EOF]
 *                                            ^ </code></pre>
 * A stream always ends with EOF. Calling {@link next()} when PC is
 * already pointing at the end of stream will still return an EOF. In other words, EOF is used to 
 * signal that the end of stream is reached. In no case will a null value be returned by this 
 * interface.
 * <p>
 * @author Ming Zhou
 */
public interface ITokenStream  {

	/**
	 * Well-known positions in a stream.
	 */
	public enum StreamPosition {
		
		/**
		 * Right before the first token. Reading at this position 
		 * ({@link TokenStream#next() next()}) yields the very first token.
		 */
		START,
		
		/**
		 * The current position of PC, right before the next token to be read.
		 */
		CURRENT
		
	}

	/**
	 * Possible directions by which an action can be done on a stream.
	 */
	public enum StreamDirection {
		
		/**
		 * move forward until end is hit.
		 */
		FORWARD,
		
		/**
		 * move backward until start is hit.
		 */
		BACKWARD
		
	}
	
	/**
	 * Put PC at the given position in stream, adjusted by offset.
	 * <p>
	 * Offset can be positive or negative. If a position adjusted by offset is out of the stream's scope, the exact START
	 * or END position will be set instead. For example, calling {@link #seek(StreamPosition, int)} with StreamPosition = 
	 * {@link StreamPosition#START} and offset = -1 or any any minus value will have same effect as if offset were passed
	 * with 0.
	 * <p>
	 * 
	 * @param pos either of two constants defined in {@link StreamPosition}: START or END
	 * @param offset a positive value if pos is START; negative if END. Otherwise the method always returns false and PC 
	 * will not be moved.
	 * 
	 * @return true if seeking succeeded, whereby PC is repositioned as specified by the arguments. false if seeking failed,
	 * and PC put at the position nearest to the intended destination.
	 */
	boolean seek(StreamPosition pos, int offset);
	
	/**
	 * Move PC from the current location to another one, along the given direction.
	 * <p>
	 * Offset can be positive or negative. If a position adjusted by offset is out of the stream's scope, the exact START
	 * or END position will be set instead. For example, calling {@link #move(StreamDirection, int)} with StreamDirection = 
	 * {@link StreamDirection#BACKWARD} and offset = 1 or any any positive value will have same effect as if offset were passed
	 * with 0.
	 * 
	 * @param direction
	 * @return true if moving succeeded, whereby PC is repositioned as specified by the arguments. false if seeking (partially) 
	 * failed, and PC put at the position nearest to the intended destination.
	 */
	boolean move(StreamDirection direction, int offset);
	
	/**
	 * Read the current token PC points to. PC moves forward by one token, if the end of stream is not hit yet.
	 * <p>
	 * If PC is already pointing at the end before this method is called, the method returns 
	 * {@link Token#EOF EOF} token. PC will not be further moved. This ensures the last
	 * token to be read is always EOF, even if the stream is not ended with EOF.
	 * 
	 * @return the token PC was pointing to before this action.
	 */
	Token next();
	
	/**
	 * Check the next token, but don't move PC.
	 * 
	 * @return
	 */
	Token peek();
	
	/**
	 * Back off PC by one token.
	 * <p>
	 * If PC is already pointing at the start of stream, PC won't be affected.
	 * @return true if PC successfully backed off by one token; false if PC was already pointing at start of stream.
	 */
	boolean backoff();
	
	/**
	 * Re-read the token whose PC = (current PC - 2). This operation does <b>not</b> change PC, 
	 * so calling it multiple times in a sequence will yield the same result.
	 * <p>
	 * Consider a stream before calling prev()<pre><code>   [TOK 1] [TOK 2] [TOK 3] [TOK 4] [TOK EOF]
	 *                          ^</code></pre>Now calling <code>prev</code> will return [TOK 2].
	 * @return null if PC - 2 &lt;= 0
	 */
	Token prev();
	
	/**
	 * Find the next token of the given {@link JulianLexer kind} in the given {@link StreamDirection direction}.
	 * <p>
	 * PC will be pointing at the first token found. If no token is found, PC remains at the original place.
	 * 
	 * @param direction
	 * @param kind
	 * @return true if the token is found; false if not.
	 */
	boolean locate(StreamDirection direction, int tokenKind);

	/**
	 * Mark the current location of PC.
	 */
	void mark();
	
	/**
	 * Set PC to the previously {@link #mark() marked} position.
	 */
	void reset();
}
