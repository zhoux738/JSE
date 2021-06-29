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

package info.julang.modulesystem.scripts;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import info.julang.external.exceptions.JSEError;

/**
 * Responsible for loading a built-in script provided by the engine for public use.
 * 
 * @author Ming Zhou
 */
public class InternalScriptLoader {

	public final static String ScriptsRoot = "/info/julang/modulesystem/scripts/";
	
	private final static Map<String, URL> Scripts;
	
	static {
		Scripts = new HashMap<String, URL>();
		InternalScriptLoaderInitializer.init(Scripts);
	}

	public static String canonicalize(String rpath) {
		return addExt(rpath);
	}
	
	/**
	 * Check whether a given path is matched to an internal script.
	 * 
	 * @param path
	 * @return true if matched to an internal script.
	 */
	public static boolean hasScript(String path) {
		path = addExt(path);
		return Scripts.containsKey(path);
	}
	
	/**
	 * Load an internal script as input stream from the given path.
	 * 
	 * @param path The canonicalized path
	 * @return null if the given path is not found to be matching an internal script.
	 */
	public static InputStream openStream(String path) {
		try {
			if (Scripts.containsKey(path)) {
				// It is a registered script
				URL url = Scripts.get(path);
				if (url == null) {
					String fullPath = ScriptsRoot + path;
					url = InternalScriptLoader.class.getResource(fullPath);
					Scripts.put(path, url);
				}
				
				return url.openStream();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new JSEError("Cannot load an embedded script: " + path, e);
		}
	}
	
	private static String addExt(String orgPath) {
		String path;
		if (orgPath.endsWith(".jul")) {
			path = orgPath;
		} else {
			path = orgPath + ".jul";
		}
		
		return path;
	}
}
