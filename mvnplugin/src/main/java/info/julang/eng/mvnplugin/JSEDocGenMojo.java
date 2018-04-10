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

import info.julang.eng.mvnplugin.docgen.DocCollector;
import info.julang.eng.mvnplugin.docgen.SerializationType;

/**
 * Extract Julian documentation into JSON files, one per each type, organized under same module.
 * <p>
 * These JSON files are to be further processed by another tool to generate human-oriented API documentation.
 * 
 * @author Ming Zhou
 */
@Mojo( name = "docgen", defaultPhase = LifecyclePhase.GENERATE_RESOURCES )
public class JSEDocGenMojo extends AbstractMojo {
	
    /**
     * Location of the source directory.
     */
    @Parameter( defaultValue = "${project.build.sourceDirectory}", property = "srcDir", required = false )
    private File srcDirectory;
    
    /**
     * Location of the project's root directory.
     */
    @Parameter( property = "project.basedir" )
    private File projDirectory;
    
    /**
     * The selective module/types to generate documentation.
     */
    @Parameter( defaultValue = "${juleng.docgen.pattern}", property = "pattern", required = false )
    private String typeFilter;
    
    /**
     * true if also to produce Markdown files.
     */
    @Parameter( defaultValue = "${juleng.docgen.format}", property = "format", required = false )
    private String format;
    
    /**
     * Whether the generation should be enabled. Default to false since doc-gen is not an essential part of build process.
     */
    @Parameter( defaultValue = "false", property = "enable", required = false )
    private boolean enable;
    
    /**
     * Whether the generation should be enabled. Default to false..
     */
    @Parameter( defaultValue = "false", property = "clean", required = false )
    private boolean clean;

    public void execute() throws MojoExecutionException {
    	Log logger = getLog();
    	GlobalLogger.set(logger);
    	
    	if (!enable) {
    		logger.info("Documentation extraction is disabled.");
    		return;
    	}
    	
    	try {
    		DocCollector loader = new DocCollector(srcDirectory, projDirectory, logger);
    		SerializationType stype = SerializationType.parseFromString(format);
        	loader.collectAll(typeFilter, stype, clean);
    	} catch (Throwable ex) {
        	logger.error(ex);
        	throw ex;
    	}
    }
}
