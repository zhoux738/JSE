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

package info.julang.memory.value.iterable;

import info.julang.memory.value.JValue;
import info.julang.memory.value.operable.JulianObjectAdaptor;

/**
 * An interface mirroring <font color="green"><code>System.Util.IIterator</code></font>.
 * 
 * @author Ming Zhou
 */
public interface IIterator extends JulianObjectAdaptor {

	/**
	 * Check if there is more.
	 * @return true if there is more elements to return from {@link #next()}.
	 */
	boolean hasNext();
	
	/**
	 * Get the next element.
	 * @return The next element. If there isn't more, returns null.
	 */
	JValue next();
	
	/**
	 * An optional method which disposes of the underlying resource upon being released.
	 */
	void dispose();
}
