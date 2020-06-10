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

package info.julang.ide.wizards;

/**
 * A thin wrapper of a Julian module name.
 * 
 * @author Ming Zhou
 */
public class ModuleName {
	
	private String name;
	
	ModuleName(String name) {
		this.name = name;
	}
	
	public String toPathString() {
		StringBuilder pathBuilder = new StringBuilder();
		byte[] bytes = name.getBytes();
		int max = bytes.length - 1;
		boolean canAddSeparator = false; // "can add separator" also means "NOT the 1st char of a section"
		for (int i = 0; i <= max; i++) {
			char b = (char)bytes[i];
			if (!canAddSeparator) { // 1st char of a section
				if ((b >= 'a' && b <= 'z') 
			       ||(b >= 'A' && b <= 'Z')
				   ||(b == '_')) {
					pathBuilder.append(b);
					canAddSeparator = true;
				} else {
					// Not a legal char to start a section
					return null;
				}
			} else {
				if ((b >= 'a' && b <= 'z') 
			       ||(b >= 'A' && b <= 'Z')
				   ||(b >= '0' && b <= '9')
				   ||(b == '_')) {
					pathBuilder.append(b);
				} else if (b == '.') {
					if (i == max) {
						// Cannot end with dot
						return null;
					}
					
					pathBuilder.append('/'); // Replace '.' with '/'
					canAddSeparator = false;
				} else {
					// Not a legal char in a section
					return null;
				}
			}
		}
		
		return pathBuilder.toString();
	}
	
	@Override
	public String toString() {
		return name;
	}
}

