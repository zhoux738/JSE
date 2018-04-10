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

package info.julang.eng.mvnplugin.mdgen;

import java.io.IOException;

/**
 * A section out of a summary part in the original doc. Sections are cut out based
 * on certain markers. For example, Markdown link sequences, [A](L), will be parsed
 * out to an individual IParsedDocSection. 
 * <p>
 * Each section is appended to the writer in order, but the type of that section
 * may affect how the texts are rendered.
 * 
 * @author Ming Zhou
 */
public interface IParsedDocSection {

	/**
	 * Append this section to an MD writter.
	 * 
	 * @param markdownWriter
	 * @throws IOException 
	 */
	void appendTo(MarkdownWriter markdownWriter) throws IOException;
	
}
