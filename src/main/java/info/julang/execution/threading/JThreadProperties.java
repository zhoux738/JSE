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

package info.julang.execution.threading;

public class JThreadProperties {

	private JThreadPriority pripority;
	
	private boolean isDaemon;
	
	private int rc;

	public JThreadPriority getPripority() {
		return pripority;
	}

	public void setPripority(JThreadPriority pripority) {
		this.pripority = pripority;
	}

	public boolean isDaemon() {
		return isDaemon;
	}
	
	// WARNING: we are using isDaemon() to determine if this is main thread. This will no 
	// longer be true if we ever to change the threading model in Julian to allow other 
	// non-daemon threads (threads blocking the main engine)
	public boolean isMain() {
		return !isDaemon;
	}

	public void setDaemon(boolean isDaemon) {
		this.isDaemon = isDaemon;
	}

	public void setRunCount(int rc) {
		this.rc = rc;
	}
	
	public int getRunCount() {
		return rc;
	}
}
