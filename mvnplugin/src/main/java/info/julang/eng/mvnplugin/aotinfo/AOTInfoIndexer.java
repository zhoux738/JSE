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

package info.julang.eng.mvnplugin.aotinfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;

import info.julang.eng.mvnplugin.ScriptInfoBag;
import info.julang.eng.mvnplugin.SystemModuleProcessor;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;

/**
 * Generate Java classes containing critical information about JuFC modules and types within.
 * <p>
 * The purpose of this ahead-of-time module scanning is for boosting the performance.
 * 
 * @author Ming Zhou
 */
public class AOTInfoIndexer extends SystemModuleProcessor<ScriptInfoBagEx> {
	
	public AOTInfoIndexer(File moduleRoot, Log logger){
		super(moduleRoot, logger);
	}
	
	public void loadAll() throws MojoExecutionException {
		// Load all JuFC scripts
		List<ScriptInfoBagEx> scripts = new ArrayList<ScriptInfoBagEx>();
		loadAll(rootDir, scripts);
		
		// Emit raw script info classes
		for(ScriptInfoBagEx sib : scripts){
			emitSummaryClass(sib);
		}
		
		// Emit the coordinating class
		emitCoordinatingClass(scripts);
	}

	@Override
	protected ScriptInfoBagEx makeScriptInfoBag(RawScriptInfo rsi, File root) {
		return new ScriptInfoBagEx(rsi, root);
	}

	private void emitSummaryClass(ScriptInfoBagEx sib) throws MojoExecutionException {	  
		RawScriptInfo rsi = sib.getRawScriptInfo();
		
		File dir = sib.getDirectory();
		String absPath = dir.getAbsolutePath();
		String fileName = sib.getFileName();
		
		String className = "AOTRawScriptInfo$" + fileName;
		String modName = absPathToModName(absPath);
		
		VelocityContext context = new VelocityContext();
		context.put("tv_pkgname", modName);
		context.put("tv_classname", className);
		context.put("tv_requirments", rsi.getRequirements());
		context.put("tv_moduleName", rsi.getModuleName());
		String path = rsi.getModuleName().replace('.', '/') + "/" + fileName + ".jul";
		context.put("tv_scriptFilePath", path);
		sib.setEmbeddedScriptPath(path);
		
		List<RawClassInfo> cinfos = rsi.getClasses();
		context.put("tv_raw_classes", cinfos);
		
		mergeToFile("AOTRawScriptInfo.vm", context, new File(absPath + "/" + className + ".java"));
	}
	
	private void emitCoordinatingClass(List<ScriptInfoBagEx> scripts) throws MojoExecutionException {
		String absPath = rootDir.getAbsolutePath() + "/SystemRawScriptInfoInitializer.java";
		VelocityContext context = new VelocityContext();
		
		List<ClassInfo> cis = new ArrayList<ClassInfo>();
		for(ScriptInfoBagEx bag : scripts){
			RawScriptInfo rsi = bag.getRawScriptInfo();
			String modName = rsi.getModuleName();
			String className = bag.getFileName();
			String path = bag.getEmbeddedScriptPath();
			ClassInfo ci = new ClassInfo(modName, className, path);
			cis.add(ci);
		}

		context.put("tv_class_infos", cis);
		
		mergeToFile("SystemRawScriptInfoInitializer.vm", context, new File(absPath));
	}
}

class ScriptInfoBagEx extends ScriptInfoBag {
	
	public ScriptInfoBagEx(RawScriptInfo rsi, File file) {
		super(rsi, file);
	}
	
	//------ Set during later processing ------//
	
	private String embeddedScriptPath;
	
	public void setEmbeddedScriptPath(String path) {
		embeddedScriptPath = path;
	}
	
	public String getEmbeddedScriptPath() {
		return embeddedScriptPath;
	}
}
