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

package info.julang.typesystem.jclass.jufc.System.Network;

import info.julang.execution.threading.IOThreadHandle;

public interface IAsyncSocketCallback {

	/**
	 * Called back upon read.
	 *  
	 * @param buffer The buffer holding the read bytes.
	 * @param read The total bytes read. Contents in buffer with index >= read are undefined. -1 if EOF.
	 */
	void onRead(IOThreadHandle iothread, byte[] buffer, int read);

	/**
	 * Called before performing a READ operation. After this callback the data will be copied between
	 * system buffer and application buffer.
	 * 
	 * @param write True if this is a WRITE operation; false READ.
	 */
	void beforeRead();
	
	/**
	 * Called when an exception was thrown from the framework. This is most likely caused by the underlying IO error.
	 * 
	 * @param ex
	 */
	void onError(Exception ex);

	/**
	 * Called after a WRITE operation is done.
	 * 
	 * @param written
	 */
    void afterWrite(int written);
	
}
