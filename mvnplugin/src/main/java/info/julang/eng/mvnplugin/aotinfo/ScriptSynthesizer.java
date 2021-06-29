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
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;

import info.julang.eng.mvnplugin.TemplateMergeTool;

/**
 * Synthesize scripts to be placed in the built-in script folder.
 * 
 * @author Ming Zhou
 */
public class ScriptSynthesizer {
	
	private final static String All_JUL = "all.jul";
	private final static String InitializerFile = "InternalScriptLoaderInitializer";
	
	private File scriptRoot;
	private Log logger;
	
	public ScriptSynthesizer(File scriptRoot, Log logger){
		this.scriptRoot = scriptRoot;
		this.logger = logger;
	}

	public void synthesizeAll() throws MojoExecutionException {
		List<String> names = loadScriptNames();
		
		String absPath = scriptRoot.getAbsolutePath() + "/" + All_JUL;
		VelocityContext context = new VelocityContext();
		context.put("tv_scripts", names);
		names.sort(null); // The actual order doesn't matter, but there must be an order.
		TemplateMergeTool.mergeToFile("source/AllScripts.vm", context, new File(absPath), logger);
		logger.info("Generated 1 script file (" + All_JUL + ").");
		
		absPath = scriptRoot.getAbsolutePath() + "/" + InitializerFile + ".java";
		names.add(All_JUL);
		context = new VelocityContext();
		context.put("tv_scripts", names);
		TemplateMergeTool.mergeToFile("source/" + InitializerFile + ".vm", context, new File(absPath), logger);
		logger.info("Generated 1 source file (" + InitializerFile + ".java).");
	}
	
	// Include all non-synthesized scripts
    private List<String> loadScriptNames() throws MojoExecutionException {
        File[] children = scriptRoot.listFiles(new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isFile()) {
		        	String name = f.getName();
		        	if (name.endsWith(".jul") && !All_JUL.equals(name)) {
		        		return true;
		        	}
				}
				
				return false;
			}	
        });
        
    	List<String> flist = new ArrayList<>(children.length);
        for(File f : children){
    		flist.add(f.getName());
        }
        
        return flist;
    }
}
