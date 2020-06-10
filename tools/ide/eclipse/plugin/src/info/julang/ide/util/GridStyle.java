/*
MIT License

Copyright (c) 2020 Ming Zhou

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

package info.julang.ide.util;

import org.eclipse.swt.layout.GridData;

/**
 * An enum to enforce the correct use of grid style. Add more if needed.
 * 
 * @author Ming Zhou
 */
public enum GridStyle {

	HORIZONTAL_ALIGN_END(GridData.HORIZONTAL_ALIGN_END),
	FILL_HORIZONTAL(GridData.FILL_HORIZONTAL),
	FILL_BOTH(GridData.FILL_BOTH);
	
	private int value;
	
	private GridStyle(int value) {
		this.value = value;
	}
	
	public int toSWTStyle() {
		return this.value;
	}
}
