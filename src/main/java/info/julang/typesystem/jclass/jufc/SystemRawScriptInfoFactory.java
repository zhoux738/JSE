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

package info.julang.typesystem.jclass.jufc;

import info.julang.external.exceptions.JSEError;
import info.julang.modulesystem.prescanning.RawScriptInfo;

/**
 * A synchronized factory that creates pre-generated RawScriptInfo instance for JuFC types.
 * 
 * @author Ming Zhou
 */
class SystemRawScriptInfoFactory<T extends RawScriptInfo> {

	private RawScriptInfo rsi;
	private final Class<T> clazz; // Due to runtime type erasure, we can only demand Class of T by this workaround.
	
	public SystemRawScriptInfoFactory(Class<T> clazz){
		this.clazz = clazz;
	}
	
	public RawScriptInfo create(){
		if (rsi == null){
			synchronized(this){
				if (rsi == null){
					try {
						rsi = clazz.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						throw new JSEError("Cannot create " + clazz.getName());
					}
				}
			}
		}
		
		return rsi;
	}
}
