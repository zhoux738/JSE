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

package info.julang.util;

/**
 * Utility method collections handling OS specifics.
 * 
 * @author Ming Zhou
 */
public final class OSTool {
	
	/**
	 * Canonicalize the file system path. In particular, use back slash (\) on Windows, and forward slash (/) on other OSes. 
	 * 
	 * @param fileName The file path and name
	 * @return a canonicalized file system path, with unified path separator.
	 */
	public static String canonicalizePath(String fileName) {
		return isWindows() ? fileName.replace('/', '\\') : fileName.replace('\\', '/');
	}

	/**
	 * Check whether the OS is Windows.
	 * 
	 * @return true if it is Windows, false others.
	 */
	public static boolean isWindows() {
		return is(OSType.WINDOWS);
	}
	
	/**
     * Check whether the OS is any of the specified OSes.
     * 
     * @param types The array of OS types.
     * @return true if it is any of the specified OS.
     */
    public static boolean is(OSType... types) {
        String[] typs = new String[types.length];
        for(int i = 0; i < types.length; i++){
            typs[i] = types[i].keyword;
        }
        
        return isOS(typs);
    }
    
    public static enum OSType {
        WINDOWS("windows"),
        MAC("mac"),
        LINUX("linux");
        
        OSType(String keyword){
            this.keyword = keyword;
        }
        
        String keyword;
    }
	
	private static boolean isOS(String... keywords){
        String ver = System.getProperty("os.name");
        if (ver != null){
            String lver = ver.toLowerCase();
            for (String kw : keywords) {
                if (lver.contains(kw)) {
                    return true;
                }
            }
        }
        
        return false;
	}

	public static String getFileSimpleName(String filePathName, boolean isCanonicalized) {
		String fpath = isCanonicalized? filePathName : canonicalizePath(filePathName);
		int index = fpath.lastIndexOf(isWindows() ? '\\' : '/');
		if (index >= 0 && index + 1 < fpath.length()) {
			return fpath.substring(index + 1);
		} else {
			return filePathName;
		}
	}
	
	/**
	 * The word size of the running computer.
	 */
	public static final int WordSize;
	
	static {
		// Unless we are sure about it being 32 bits, always assume 64 bits.
		if ("32".equals(System.getProperty(
			"sun.arch.data.model", // Oracle HotSpot
			"com.ibm.vm.bitmode")) // IBM J9
			|| "x86".equals(System.getProperty("os.arch"))) {
			WordSize = 4;
		} else {
			WordSize = 8;
		}
	}
}
