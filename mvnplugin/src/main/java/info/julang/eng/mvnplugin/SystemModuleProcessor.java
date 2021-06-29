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

package info.julang.eng.mvnplugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;

import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.prescanning.CollectScriptInfoStatement;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.typesystem.jclass.jufc.FoundationClassParser;

/**
 * A processor that can read information about Julian's system modules from the code repository.
 * It also provides some helper methods.
 * 
 * @author Ming Zhou
 * @param <T> The object type to hold information about each JuFC script file.
 */
public abstract class SystemModuleProcessor<T extends ScriptInfoBag> implements ITemplateMerger {
    
    protected File rootDir;
    protected Log logger;
	private int prefixLength;
    
    public SystemModuleProcessor(File moduleRoot, Log logger){
        this.logger = logger;
        rootDir = moduleRoot;
		prefixLength = rootDir.getAbsolutePath().length() - FoundationClassParser.JuFCRoot.length() + 1; // +1 because of the leading '/' of JuFCRoot
    }
    
    protected String absPathToModName(String absPath){
    	String modName = absPath.substring(prefixLength + 1, absPath.length());
		modName = modName.replace('\\', '.');
		modName = modName.replace('/', '.');
		return modName;
    }
    
    /**
     * A subclass calls this method to merge a template.
     */
    @Override
    public void mergeToFile(String templatePath, VelocityContext context, File file) throws MojoExecutionException{
    	TemplateMergeTool.mergeToFile(templatePath, context, file, logger);
    }
    
    /**
     * A subclass calls this method to load all System modules into the list.
     * 
     * @param root
     * @param scripts
     */
    protected void loadAll(File root, List<T> scripts) throws MojoExecutionException {
    	loadAll0(root, null, scripts);
    }

    protected abstract T makeScriptInfoBag(RawScriptInfo rsi, File root);
    
    private void loadAll0(File root, FQName modName, List<T> scripts) throws MojoExecutionException {
        if (root.isFile()){
            String fname = root.getName();
            if (fname.endsWith(".jul")){
                RawScriptInfo rsi = load(modName, root);
                T sib = makeScriptInfoBag(rsi, root);
                scripts.add(sib);
            }
        } else if (root.isDirectory()){
            File[] children = root.listFiles();
            FQName smodName = 
                root != rootDir ?
                    modName != null ? new FQName(modName.toString(), root.getName()) : new FQName(root.getName()) :
                    null;
            for(File f : children){
            	loadAll0(f, smodName, scripts);
            }
        }
    }
    
    private RawScriptInfo load(FQName modName, File file) throws MojoExecutionException {
        RawScriptInfo info = new RawScriptInfo(modName.toString(), false); // Must set to false so that we load from FS
        info.getOption().setAllowSystemModule(true);
        
        try {
			info.initialize(file.getAbsolutePath());
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException("Source file not found.", e);
		}
        
        CollectScriptInfoStatement csis = new CollectScriptInfoStatement(false, false);
        csis.prescan(info);
        
        return info;
    }
}
