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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import info.julang.util.OSTool;

public class FSUtil {
	
	/**
	 * Concatenate the absolute file system paths into a single string with OS-specific separator.
	 */
	public static String toAbsoluteFSPathArray(File[] files) {
		char c = OSTool.isWindows() ? ';' : ':';
		
		StringBuilder sb = new StringBuilder();
		
		int max = files.length - 1;
		if (max >= 0) {
			sb.append(files[0].getAbsolutePath());
		}
		
		for (int i = 1; i < files.length; i++) {
			sb.append(c);
			sb.append(files[i].getAbsolutePath());
		}
		
		return sb.toString();
	}
	
	/**
	 * Map an OS-specific path array into File array. Note the path may be relative.
	 */
	public static File[] fromFSPathArray(String arg) {
		if (null == arg || arg.isEmpty()) {
			return new File[0];
		}
		
		String c = OSTool.isWindows() ? ";" : ":";
		
		String[] arr = arg.split(c);
		
		List<File> files = new ArrayList<File>();
		for (String path : arr) {
			if (path != null && !path.isEmpty()) {
				files.add(new File(path));
			}
		}
		
		File[] res = new File[files.size()];
		return files.toArray(res);
	}

	/**
	 * Check if the file <code>child</code> is a potential child of the file <code>parent</code>.
	 */
	public static boolean isPotentialChildOf(File child, File parent, boolean includesEqual) {
		if (child == null || parent == null) {
			return false;
		}
		
		File grand = parent.getParentFile();
		
		return isPotentialChildOf0(child, parent, grand, includesEqual);
	}
	
	private static boolean isPotentialChildOf0(File child, File parent, File grand, boolean includesEqual) {
		if (child.equals(grand)) {
			// Misdirection
			return false;
		} else if (child.equals(parent) && includesEqual) {
			// Same file
			return true;
		}
		
		File p = child.getParentFile();
		if (p != null) {
			if (p.equals(parent)) {
				// Reach parent
				return true;
			} else {
				// Try next level
				return isPotentialChildOf0(p, parent, grand, includesEqual);
			}
		} else {
			// Reach end
			return false;
		}
	}
}
