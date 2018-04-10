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

package info.julang.clapp.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import info.julang.VersionUtility;
import info.julang.external.exceptions.JSEError;
import info.julang.util.OSTool;

public class WrapperScriptUpdater {

	public static final String VAR_VERSION = "version";
	private static final String SciptRoot = "/info/julang/clapp/update/";
	
	private String srcPath;
	private String tgtPath;
	private Map<String, String> variables;
	
	public WrapperScriptUpdater() {
		String jarDirPath = JarUtil.getCurrentJarDir();
		String scriptExtName = OSTool.isWindows() ? "cmd" : "sh";
		srcPath = SciptRoot + "jse." + scriptExtName + ".template";
		tgtPath = jarDirPath + "/jse." + scriptExtName + ".TEMP";

		addVariable(WrapperScriptUpdater.VAR_VERSION, VersionUtility.getVersion());
	}

	private InputStream openStream() {
		try {		
			URL url = WrapperScriptUpdater.class.getResource(srcPath); 
			return url.openStream();
		} catch (IOException e) {
			throw new JSEError("Cannot load script from " + srcPath, e);
		}
	}
	
	
	/**
	 * Generate a new script file to the directory given in ctor (jarDirPath).
	 * <p>
	 * An I/O error won't fault the call.
	 */
	public int update(){
		try {
			update0();
			return 0;
		} catch (IOException e) {
			System.err.println(
				"Cannot generate new wrapper script file to replace the old one. " + 
				"Update is incomeplete. Please manually fix the wrapper script at " + srcPath + ".");
			return 1;
		}
	}
	
	/**
	 * Add a case-insensitive key and its value.
	 */
	public void addVariable(String key, String value) {
		if (variables == null) {
			variables = new HashMap<String, String>();
		}
		
		variables.put(key.toLowerCase(), value);
	}
	
	protected String getVariable(String key) {
		return variables.get(key.toLowerCase());
	}
	
	private void update0() throws IOException {
		File tgt = new File(tgtPath);
		try (FileWriter fw = new FileWriter(tgt);
			 BufferedReader br = new BufferedReader(new InputStreamReader(openStream()))){
			String s = null;
			while((s = br.readLine()) != null){
				s = process(s);
				fw.write(s);
				fw.write(System.lineSeparator());
			}
		}
	}

	// Replace the variables enclosed by @ in an imitation of Ant's logic. The main point is -
	// if a key is not found between @ and @, the 2nd @ becomes the 1st @ in the next iteration. 
	//
	//    final String replaceWith = (String) hash.get(key.toString());
	//    if (replaceWith != null) {
	//        // Found the key, replace with value
	//	    ... ... 
	//    } else {
	//        // Didn't find the key, replace with the key itself. 
	//        // The char is read from both queuedData and the original stream.
	//        String newData = key.toString() + endToken;
	//        ... ...
	//        queuedData = newData;
	//        ... ...
	//        queueIndex = 0;
	//        ... ...
	//    }
	//
	// See: http://svn.apache.org/viewvc/ant/core/trunk/src/main/org/apache/tools/ant/filters/ReplaceTokens.java?view=markup
	protected String process(String s){
		int index = s.indexOf('@');
		if (index >= 0){
			StringBuilder keybuff = null;
			byte[] bytes = s.getBytes();
			StringBuilder res = new StringBuilder();
			for(byte b : bytes) {
				if (b == '@') {
					if (keybuff == null) {
						// start sub mode
						keybuff = new StringBuilder();
						keybuff.append((char)b);
					} else {
						// finish sub mode
						String k = keybuff.toString().substring(1);
						String val = getVariable(k);
						if (val != null) {
							// found key, replace the value
							res.append(val);
							keybuff = null;
						} else {
							// didn't find key, reset sub mode
							res.append(keybuff);
							keybuff = new StringBuilder();
							keybuff.append((char)b);
						}
					}
				} else {
					if (keybuff == null) {
						// not in sub mode, go directly to result
						res.append((char)b);
					} else {
						// in sub mode, go to key buffer
						keybuff.append((char)b);
					}
				}
			}
			
			if (keybuff != null) {
				res.append(keybuff);
			}
			
			return res.toString();
		} else {
			// No @ is found throughout the line
			return s;
		}
	}
	
}