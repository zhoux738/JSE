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

package info.julang.execution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.interpretation.GlobalScriptExecutable;
import info.julang.parser.ANTLRParser;
import info.julang.parser.LazyAstInfo;
import info.julang.util.OSTool;


/**
 * The file script provider provides an executable from a file.
 * 
 * @author Ming Zhou
 */
public class FileScriptProvider implements ScriptProvider {
	
	private String filePathName;
	
	public static FileScriptProvider create(String filePathName){
		return new FileScriptProvider(filePathName);
	}
	
	private FileScriptProvider(String filePathName){
		this.filePathName = filePathName;
	}
	
	public String getFilePathName(boolean shouldCanonicalize){
		return shouldCanonicalize ? OSTool.canonicalizePath(filePathName) : filePathName;
	}
	
	@Override
	public GlobalScriptExecutable getExecutable(boolean allowReentry) throws ScriptNotFoundException {
		try {
			FileInputStream fis = new FileInputStream(filePathName);
			ANTLRParser ap = new ANTLRParser(filePathName, fis, true);
			LazyAstInfo lainfo = ap.scan(false);
			ap.parse(true, false);
			
			return new GlobalScriptExecutable(lainfo, allowReentry, false);
		} catch (FileNotFoundException e) {
			throw new ScriptNotFoundException("Script not found", e);
		}
	}
	
	@Override
	public String getDefaultModulePath() {
		try {
			File f = new File(filePathName);
			String ppath = f.getParentFile().getCanonicalPath();
			ppath = OSTool.canonicalizePath(ppath) + File.separator + IExtModuleManager.DefaultModuleDirectoryName;
			return ppath;
		} catch (IOException e) {
			// If this does happen, getExecutable() would fail too.
			return null;
		}
	}	
}
