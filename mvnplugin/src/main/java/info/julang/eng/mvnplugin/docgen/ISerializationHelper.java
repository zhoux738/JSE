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

package info.julang.eng.mvnplugin.docgen;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;

import info.julang.eng.mvnplugin.docgen.DocModel.Type;

/**
 * The interface that provides means for serialization.
 * 
 * @author Ming Zhou
 */
public interface ISerializationHelper {

	/**
	 * Given the module's name, get local directory to put the serialized documents into. 
	 * The directory will be created if not existing yet.
	 * 
	 * @param modName
	 * @return
	 */
	File getModuleRoot(String modName, SerializationType format);
	
	/**
	 * Serialize the type model object to a JSON file under the module root.
	 *  
	 * @param modRoot
	 * @param typ
	 * @throws MojoExecutionException
	 */
	void serialize(File modRoot, Type typ, SerializationType format) throws MojoExecutionException;
	
}
