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

package info.julang.modulesystem.prescanning;

import java.io.File;

import info.julang.external.interfaces.IExtModuleManager;
import info.julang.langspec.ast.JulianParser.Composite_idContext;
import info.julang.langspec.ast.JulianParser.Module_definitionContext;
import info.julang.modulesystem.prescanning.RawScriptInfo.Option;
import info.julang.parser.AstInfo;

/**
 * Read the script file and inspect the very first statement, making sure it is a 
 * "<code>module ...;</code>" with module name matching the given one in {@link RawScriptInfo}.
 * 
 * @author Ming Zhou
 */
public class ModuleStatement implements PrescanStatement {

	private AstInfo<Module_definitionContext> ainfo;
	private String implicitModuleName;
	
	public ModuleStatement(AstInfo<Module_definitionContext> ainfo, String implicitModuleName){
		this.ainfo = ainfo;
		this.implicitModuleName = implicitModuleName;
	}
	
	@Override
	public void prescan(RawScriptInfo info) {
		Module_definitionContext module = ainfo.getAST();
		Composite_idContext cid = module.composite_id();
		
		String fname;
		if (cid == null) {
			if (implicitModuleName != null) {
				fname = implicitModuleName;
			} else {
				throw new IllegalModuleFileException(
					info, module,
					"The module name is not defined. Default module declaration "
					+ "(module;) can only be used for module files found under the default module path, i.e. '"
					+ IExtModuleManager.DefaultModuleDirectoryName 
					+ File.separator 
					+ "' co-located with the invoked script.");	
			}
		} else {
			fname = cid.getText();
		}
		
		if(info.getModuleName() == null){
			info.setModuleName(fname);
		}
		
		Option opt = info.getOption();
		
		if(!opt.isAllowSystemModule() && fname.toLowerCase().startsWith("system.")){
			throw new IllegalModuleFileException(
				info, cid != null ? cid : module, "The module name (\"" + fname + "\") is reserved.");	
		}
		
		if(!opt.isAllowNameInconsistency()){
			if(!info.getModuleName().equals(fname)){
				throw new IllegalModuleFileException(
					info, cid != null ? cid : module, "The module name (\"" + fname + 
					"\") is not consistent with the file's directory structure. " + 
					"This may be caused by a wrong configuration of script repository.");	 
			}
		}
	}
}
