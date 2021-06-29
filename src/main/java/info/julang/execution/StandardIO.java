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

package info.julang.execution;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Provides streams for standard input/output.
 * <p>
 * This class doesn't manage the life cycle of the underlying streams.
 * 
 * @author Ming Zhou
 */
public class StandardIO {

	// Raw (can be null)
	private InputStream in;
	protected OutputStream out;
	protected OutputStream err;
	
	// Decorated
	private PrintStream outPs;
	private PrintStream errPs;
	
	/**
	 * Create a new StandardIO instance. All arguments are optional. If not provided, default to the platform IO.
	 * 
	 * @param in input stream
	 * @param out output stream
	 * @param err error stream
	 */
	public StandardIO(InputStream in, OutputStream out, OutputStream err) {
		this.in = in;
		this.out = out;
		this.err = err;
	}
	
	/**
	 * Create a new StandardIO instance without any redirection.
	 */
	public StandardIO() {
		this(null, null, null);
	}
	
	public PrintStream getOut() {
		if (out == null) {
			return System.out;
		}
		
		if (outPs == null) {
			synchronized(StandardIO.class) {
				if (outPs == null) {
					outPs = out instanceof PrintStream ? (PrintStream)out : new PrintStream(out);
				}
			}
		}

		return outPs;
	}
	
	public PrintStream getError() {
		if (err == null) {
			return System.err;
		}
		
		if (errPs == null) {
			synchronized(StandardIO.class) {
				if (errPs == null) {
					errPs = err instanceof PrintStream ? (PrintStream)err : new PrintStream(err);
				}
			}
		}
		
		return errPs;
	}
	
	public InputStream getIn() {
		return in != null ? in : System.in;
	}
}
