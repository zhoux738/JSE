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

/**
 * A writer decorator to facilitate MD text emission.
 * 
 * @author Ming Zhou
 */
package info.julang.eng.mvnplugin.mdgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class MarkdownWriter extends FileWriter {
	
	public MarkdownWriter(File file) throws IOException {
		super(file);
	}
	
	/**
	 * Example of a level-2 header:<br>
	 * ## All Members
	 */
	public void addHeader(String name, int level) throws IOException {
		while (level > 0) {
			this.write('#');
			level--;
		}
		
		this.write(' ');
		this.write(name);
	}
	
	/** Add a stylized string. */
	public void add(StylizedString str) throws IOException {
		this.write(str.toString());
	}
	
	/**
	 * Append a number of new lines by either tag (&lt;br&gt;) or \t\n.
	 */
	public void nextLine(int times, boolean byTag) throws IOException {
		while(times > 0) {
			this.write(byTag ? "<br>" : "\r\n");
			times--;
		}
	}
	
	/**
	 * Append a new line by either tag (&lt;br&gt;) or \t\n.
	 */
	public void nextLine(boolean byTag) throws IOException {
		this.write(byTag ? "<br>" : "\r\n");
	}
	
	/**
	 * <code>
	 * |*Type*|*Name*|*Signature*
	 * |:-----|:-----|:----------
	 * </code>
	 */
	public void addTableHeaders(StylizedString... headers) throws IOException {
		int[] lens = new int[headers.length];
		int i = 0;
		for(StylizedString ss : headers) {
			String res = ss.toString();
			this.write('|');
			this.write(res);
			lens[i] = res.length() - 1;
			i++;
		}
		this.nextLine(false);
		
		for(i = 0; i < lens.length; i++) {
			this.write("|:");
			int l = lens[i];
			while (l > 0) {
				this.write('-');
				l--;
			}
		}
		this.nextLine(false);
	}
	
	/**
	 * <code>
	 * |**field**|[*length*](#length-1)|`public const int length`
	 * </code>
	 */
	public void addTableRow(StylizedString... columns) throws IOException {
		for(StylizedString ss : columns) {
			String res = ss.toString();
			this.write('|');
			this.write(res);
		}
		
		this.nextLine(false);
	}
	
	/** 
	 * &lt;a name="length-1"&gt;&lt;/a&gt;
	 */
	public void addAnchor(String linkTarget) throws IOException {
		String anchor = "<a name=\"" + linkTarget + "\"></a>";
		this.write(anchor);
	}
	
	/**
	 * - [System.TypeIncompatibleException](TypeIncompatibleException)&lt;br&gt;If the parameter has a type which is neither string nor char.
	 */
	public void addItem(StylizedString name, int depth, List<IParsedDocSection> list) throws IOException {
		while(depth > 0) {
			this.write("  ");
			depth--;
		}
		this.write("- ");
		this.write(name.toString());
		this.nextLine(false);
		this.append(list);
		this.nextLine(false);
	}

	public void append(List<IParsedDocSection> list) throws IOException {
		if (list != null) {
			for (IParsedDocSection section : list) {
				section.appendTo(this);
			}
		}
	}

	public void divide(int linebreaks) throws IOException {
		this.write("<hr>");
		while(linebreaks > 0){
			this.nextLine(false);
			linebreaks--;
		}
	}
}
