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

package info.julang.hosting.mapped.implicit;

import info.julang.modulesystem.ModuleInfo;

/**
 * Helper methods to convert names between Julian type and JVM classes.
 * 
 * @author Ming Zhou
 */
public final class ImplicitTypeNameConvertor {

	public static String getFullTypeName(String typeName) {
		return ModuleInfo.IMPLICIT_MODULE_NAME + "." + typeName;
	}
	
	public static String getSimpleTypeName(String fullName) {
		String prefix = ModuleInfo.IMPLICIT_MODULE_NAME + ".";
		if (fullName.startsWith(prefix)) {
			return fullName.substring(prefix.length());
		}

		return null;
	}
	
	
	/**
	 <pre>
	 Rules:
	   (1) Start with "Mapped_"
	   (2) Append the full name by character with variation:
	     case [a..zA..Z0..9] => unchanged
	     case . => _
	     otherwise => _NNN, where NNN = (int)char
	     
	 Example:
	   A.B.C$D._E
	   =>
	   Mapped_A_B_C_036D__095E
	 </pre>
	*/
	public static String fromClassNameToSimpleTypeName(Class<?> clazz) {
		String origName = clazz.getName();
		
		StringBuilder sb = new StringBuilder("Mapped_");
		for (byte b : origName.getBytes()) {
			if (b >= 'a' && b <= 'z'
			|| b >= 'A' && b <= 'Z'
			|| b >= '0' && b <= '9') {
				sb.append((char)b);
			} else if (b == '.') {
				sb.append('_');
			} else {
				sb.append('_');
				sb.append(String.format("%03d", b));
			}
		}
		
		return sb.toString();
	}
}
