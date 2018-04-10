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

import info.julang.eng.mvnplugin.GlobalLogger;


/**
 * The target file's type of serialization.
 * 
 * @author Ming Zhou
 */
public enum SerializationType {

	RAW,
	
	MARKDOWN,

	WEBSITE;
	
	public static SerializationType parseFromString(String format){
		if (format == null || "".equals(format)){
			GlobalLogger.get().info("Target format not specified. Output only JSON for default.");
			return SerializationType.RAW;
		}
		
		format = format.trim().toLowerCase();
		switch (format) {
		case "raw":
		case "json":
			return SerializationType.RAW;
		case "md":
		case "markdown":
			return SerializationType.MARKDOWN;
		case "html":
		case "website":
			return SerializationType.WEBSITE;
		default:
			throw new DocGenException("Target format \"" + format + "\" is not recognized. Choose one of following: json, md, html");
		}
	}
	
}
