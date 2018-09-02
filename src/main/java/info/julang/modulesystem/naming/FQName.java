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

package info.julang.modulesystem.naming;

import info.julang.modulesystem.BadNameException;

public class FQName extends QNameBase {

	private NFQName leafLevel;
	
	@Override
	public boolean isFullyQualified() {
		return true;
	}
	
	public FQName(String sec1, String sec2){
		this(sec1 + "." + sec2);
	}
	
	public FQName(String name){
		String[] levels = name.split("\\.");
		NFQName subLevel = NFQName.END;
		int total = levels.length - 1;
		if (total == 0) {
			leafLevel = subLevel;
		} else {
			for(int i = total; i>=1; i--){
				String n = levels[i];
				if(n == null || n.isEmpty()){
					throw new BadNameException(name);
				}
				subLevel = new NFQName(n, subLevel);
				if (i == total) {
					leafLevel = subLevel;
				}
			}
		}
		
		this.name = levels[0];
		this.nextLevel = subLevel;
	}
	
	/**
	 * Get the not-fully-qualified simple name. For example, The simple name for FQ name "a.b.c" is "c".
	 * 
	 * @return
	 */
	public String getSimpleName(){
		return leafLevel == NFQName.END ? name : leafLevel.name;
	}

	@Override
	public String toString(){
		return name + (nextLevel == NFQName.END ? "" : "." + nextLevel);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		String thisName = toString();
		String otherName = obj.toString();
		return thisName.equals(otherName);
	}

	/**
	 * Get the module name ('A.B' of 'A.B.C').
	 */
    public String getModuleName() {
        String fn = toString();
        String sn = getSimpleName();
        int flen = fn.length();
        int slen = sn.length();
        int mlen = flen - slen - 1;
        if (mlen > 0) {
            return fn.substring(0, mlen);
        } else {
            return null;
        }
    }
	
}
