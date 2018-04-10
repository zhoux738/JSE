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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import info.julang.eng.mvnplugin.aotinfo.AOTInfoIndexer;
import info.julang.typesystem.jclass.jufc.FoundationClassParser;

/**
 * Perform AOT (ahead-of-time) scanning to emit Java source files containing hard-coded module information 
 * about Julian Foundation Classes.
 * <p>
 * The AOT script info should be used as the first alternative, but not the only option. To avoid any
 * cross-dependency issue blocking a successful build of JSE, this mojo can opt to emit empty classes and
 * corresponding coordinator class that returns null for any query.
 * 
 * @author Ming Zhou
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JSECodeGenMojo extends AbstractMojo {
	
    /**
     * Location of the source directory.
     */
    @Parameter( defaultValue = "${project.build.sourceDirectory}", property = "srcDir", required = false )
    private File srcDirectory;
    
    /**
     * Whether the generation should be disabled. Mainly useful during bootstrapping.
     */
    @Parameter( defaultValue = "false", property = "skip", required = false )
    private boolean skip;

    public void execute() throws MojoExecutionException {
    	Log logger = getLog();

    	if (skip) {
    		logger.info("Pre-compile source generation disabled.");
    		return;
    	}
    	
    	String path = srcDirectory.getAbsolutePath() + FoundationClassParser.JuFCRoot;
    	File jufcRoot = new File(path);
    	
    	// Pre-scan each module directly from file system, then emit a synthesized RawScriptInfo class.
    	// At the end emit an entrance class that can return an instance of RSI based on the path passed in.
    	try {
        	AOTInfoIndexer loader = new AOTInfoIndexer(jufcRoot, logger);
        	loader.loadAll();
    	} catch (Throwable ex) {
        	logger.error(ex);
        	throw ex;
    	}
    }
}
