/*
MIT License

Copyright (c) 2020 Ming Zhou

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

package info.julang.ide.editors;

import org.eclipse.swt.graphics.RGB;

public interface JulianSourceColors {
	RGB KeywordColor = new RGB(153, 51, 102); // 204,108,29
	RGB CommentColor = new RGB(102, 153, 102); // 128,128,128
	RGB LiteralColor = new RGB(51, 0, 255); // 23,198,163
	RGB RegexLiteralColor = new RGB(0, 206, 209); // keep same ?
	
	String KEYWORD = "julian.editor.keyword_color";
	String COMMENT = "julian.editor.comment_color";
	String LITERAL = "julian.editor.literal_color";
	String REGEX = "julian.editor.regex_color";
}
