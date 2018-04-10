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

package info.julang.langspec;

import info.julang.external.exceptions.JSEError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Defines the rule for character escaping.
 * 
 * @author Ming Zhou
 */
public class CharacterEscaping {
	
	/**
	 * Convert a Java String into a Julian string literal.
	 * @param prefix can be null
	 * @param original can be null
	 * @param suffix can be null
	 * @return
	 */
	public static String encodeAsStringLiteral(String prefix, String original, String suffix){
		ByteArrayOutputStream output = new ByteArrayOutputStream(original != null ? original.length() + 32 : 32);
		
		if(prefix != null) {
			try {
				output.write(prefix.getBytes());
			} catch (IOException e) {
				throw new JSEError(e);
			}			
		}
		
		if (original != null){
			output.write('"');
			int i = 0;
			ByteArrayInputStream input = new ByteArrayInputStream(original.getBytes());
			while((i = input.read()) != -1){
				char c = (char) i;
				switch(c){
				case '\0': output.write('\\'); output.write('0'); break;
				case '\b': output.write('\\'); output.write('b'); break;
				case '\t': output.write('\\'); output.write('t'); break;
				case '\n': output.write('\\'); output.write('n'); break;
				case '\f': output.write('\\'); output.write('f'); break;
				case '\r': output.write('\\'); output.write('r'); break;
				case '"': output.write('\\'); output.write('"'); break;
				case '\\' : output.write('\\'); output.write('\\'); break;
				default: output.write(c); break;
				}
			}
			output.write('"');		
		}
		
		if(suffix != null) {
			try {
				output.write(suffix.getBytes());
			} catch (IOException e) {
				throw new JSEError(e);
			}			
		}
		
		String result = output.toString();
		return result;
	}
	
}
