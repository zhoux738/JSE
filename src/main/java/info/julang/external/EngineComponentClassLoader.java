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

package info.julang.external;

import info.julang.dev.GlobalSetting;
import info.julang.external.exceptions.JSEError;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A customized class loader used in Julian script engine. When loading JSE internal classes, it disregards the
 * standard delegation model and probes locally from URL paths. This way, multiple Julian engine instances will 
 * each maintain its own set of internal classes.
 * 
 * @author Ming Zhou
 */
public class EngineComponentClassLoader extends URLClassLoader {

	private static final AtomicInteger COUNTER = new AtomicInteger();
	
	// Classes from the following packages, as well as packages underneath them, will
	// be delegated to the parent loader.
	private static final String PKG_EXTERNAL_PREFIX = "info.julang.external.";
	private static final String PKG_UTIL_PREFIX = "info.julang.util.";

	private int seq;
	
	public EngineComponentClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		seq = COUNTER.incrementAndGet();
	}
	
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// Follow standard delegation model if it is not a Julian component, or ...
		if (!name.startsWith(GlobalSetting.PKG_PREFIX)) {
			return super.loadClass(name);
		}
		
		// ... belongs to certain packages.
		if (name.startsWith(PKG_EXTERNAL_PREFIX) ||
			name.startsWith(PKG_UTIL_PREFIX)) {
			return super.loadClass(name);
		}
		
		// Otherwise, first check if it is already loaded
        Class<?> clazz = findLoadedClass(name);
        if (clazz == null) {
            try {
            	// If not yet, load it locally using URLClassLoader's search logic
                clazz = findClass(name);
            } catch (ClassNotFoundException e) {
            	throw new JSEError("Cannot find engine component: " + name);
            }
        }
        
        if (clazz == null) {
        	throw new JSEError("Cannot find engine component: " + name);
        }
        
        return clazz;
    }
	
	public String toString(){
		return "JSE ClassLoader - " + seq;
	}
}
