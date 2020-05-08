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

package info.julang.interpretation.errorhandling;

/**
 * Provides more detailed location information than {@link IHasLocationInfo}.
 * 
 * @author Ming Zhou
 */
public interface IHasLocationInfoEx extends IHasLocationInfo {

	/**
	 * The length of the text. This location covers the exact text range starting 
	 * from position <code>{{@link #getLineNumber()}, {@link #getColumnNumber()}}</code> to an offset equal to <code>starting position + getLength() - 1</code>.
	 */
	int getLength();
	
	/**
	 * The line number from the script file. 1-based.
	 */
	int getColumnNumber();
	
	/**
	 * The offset relative to the start of the input, typically the head of file. 0-based.
	 */
	int getOffset();
	
}
