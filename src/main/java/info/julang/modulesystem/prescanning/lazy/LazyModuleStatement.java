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

package info.julang.modulesystem.prescanning.lazy;

import java.io.File;

import info.julang.external.interfaces.IExtModuleManager;
import info.julang.modulesystem.prescanning.IllegalModuleFileException;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo.Option;
import info.julang.scanner.ITokenStream;

public class LazyModuleStatement implements LazyPrescanStatement {

	private ModuleNameReader reader;
	private String implicitModuleName;
	
	public LazyModuleStatement(ModuleNameReader reader, String implicitModuleName){
		this.reader = reader;
		this.implicitModuleName = implicitModuleName;
	}

	// Starts right after the module keyword.
	@Override
	public void prescan(ITokenStream stream, RawScriptInfo info) {
		StringBuilder fullName = new StringBuilder();
		stream.mark();
		reader.readModuleName(stream, fullName, true);
		
		String fname = fullName.toString();
		
		if ("".equals(fname)) { // Implicit module name
			if (implicitModuleName != null) {
				info.setModuleName(fname = implicitModuleName);
			} else {
				throw new IllegalModuleFileException(
					info, stream,
					"The module name is not defined. Default module declaration "
					+ "(module;) can only be used for module files found under the default module path, i.e. '"
					+ IExtModuleManager.DefaultModuleDirectoryName 
					+ File.separator 
					+ "' co-located with the invoked script.");	
			}
		} else {
			if (info.getModuleName() == null) {
				info.setModuleName(fname);
			}
		}		
		
		Option opt = info.getOption();
		
		if(!opt.isAllowSystemModule() && fname.toLowerCase().startsWith("system.")){
			stream.reset();
			throw new IllegalModuleFileException(
				info, stream, "The module name \"" + fullName.toString() + "\" is reserved.");	
		}
		
		if(!opt.isAllowNameInconsistency()){
			if(!info.getModuleName().equals(fname)){
				stream.reset();
				throw new IllegalModuleFileException(
					info, stream, "The module name (\"" + fullName.toString() + 
					"\") is not consistent with the file's directory structure. " + 
					"This may be caused by a wrong configuration of script repository.");	 
			}
		}
	}

}
