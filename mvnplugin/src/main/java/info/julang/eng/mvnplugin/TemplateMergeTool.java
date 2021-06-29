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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * A helper class used to merge template with content and write the result to a file.
 * 
 * @author Ming Zhou
 */
public final class TemplateMergeTool {

    private static final String ENCODING = "ISO-8859-1";
    
    private TemplateMergeTool() {}
    
    /**
     * Merge a template with a context providing template variables, writing the resultant text to the specified file.
     * <p>
     * Some assumptions:
     * <p>
     * 1) The template path is relative to "templates/" folder embedded in side the plugin jar. <br>
     * 2) <code>c_dot</code>, <code>c_dollar</code> and <code>c_sharp</code> are added as template 
     * variables which can be used to represent '.', '$' and '#', respectively. <br>
     * 3) The output is encoded in ISO-8859-1.
     * 
     * @param templatePath a template file to be found under "template/" path embedded inside the plugin jar.
     * @param context providing template variables
     * @param file the target file to output the merged template to
     * @param logger a Maven logger
     * @throws MojoExecutionException
     */
    public static void mergeToFile(
    	String templatePath, VelocityContext context, File file, Log logger)
    	throws MojoExecutionException {
    	
        try {
            // Add escaping chars
            context.put("c_dot", ".");
            context.put("c_dollar", "$");
            context.put("c_sharp", "#");
            
            // Initialize engine
            Properties p = new Properties();
            p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            p.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            VelocityEngine engine = new VelocityEngine();
            engine.init(p);
            
            // Ensure the existence of file
            if (!file.exists()){
                file.createNewFile();
            }
            
            // Merge template with context to the target file
            try (PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))){
                engine.mergeTemplate("templates/" + templatePath, ENCODING, context, writer);
                writer.flush();
            }
        } catch (IOException e) {
            logger.error(e);
            throw new MojoExecutionException(e.getMessage());
        }
    }
}
