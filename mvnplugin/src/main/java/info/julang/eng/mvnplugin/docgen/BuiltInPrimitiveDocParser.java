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

import info.julang.eng.mvnplugin.docgen.DocModel.PrimitiveType;

import java.io.File;

import org.apache.maven.plugin.logging.Log;

/**
 * Extract and serialize documentation for primitive types.
 * 
 * @author Ming Zhou
 */
public class BuiltInPrimitiveDocParser extends BuiltInDocParserBase {

	BuiltInPrimitiveDocParser(File srcDirectory, ISerializationHelper serializer, ModuleContext mc, Log logger) {
		super(srcDirectory, serializer, mc, logger);
	}

	@Override
	protected String getRelativeSrcDir( ) {
		return "info/julang/typesystem/basic";
	}

	@Override
	protected DocModel.Type createDocModel(AnnotationKVMap map){
		return new PrimitiveType(map);
	}
}
