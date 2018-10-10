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
import java.util.ArrayList;
import java.util.List;

public class PlainSection implements IParsedDocSection {

	private String str;
	
	public PlainSection(String str) {
		this.str = str;
	}
	
	@Override
	public void appendTo(MarkdownWriter markdownWriter) throws IOException{
		markdownWriter.write(str);
	}

	@Override
	public String toString(){
		return str;
	}
	
	/**
	 * Get a list of sub-section names at specified level. For example, when level == 2 this 
	 * will retrieve all titles started by "##".
	 * 
	 * @param level The level of titles to collect.
	 * @param addAnchor Add an anchor before each title. So if true, this method will have 
	 * the side-effect of changing the contents in the plain section.
	 */
	public List<String> getSubSectionNames(int level, boolean addAnchor){
		String tag = null;
		switch(level){
		case 1: tag = "#"; break;
		case 2: tag = "##"; break;
		case 3: tag = "###"; break;
		case 4: tag = "####"; break;
		case 5: tag = "#####"; break;
		default:
			return null;
		}
		
		List<String> sections = null;
		List<Integer> offsets = null;
		int index = -1, offset = 0;
		int max = str.length() - 1;
		while(offset <= max && (index = str.indexOf(tag, offset)) >= offset){
			// Skip the case where the tag is part of a longer tag
			int findex = index + tag.length();
			if (findex <= max && str.charAt(findex) == '#'){
				while(findex <= max) {
					if (str.charAt(findex) == '#') {
						findex++;
					} else {
						break;
					}
				}
				offset = findex;
				continue;
			}

			offset = index;
			
			if (sections == null) {
				sections = new ArrayList<String>();
				offsets = new ArrayList<Integer>();
			}
			
			index = str.indexOf("\n", offset);
			if (index > offset) {
				String title = str.substring(offset + tag.length(), index + 1).trim();
				sections.add(title);
				offsets.add(offset);
				offset = index + 1;
			} else {
				String title = str.substring(offset + tag.length()).trim();
				sections.add(title);
				offsets.add(offset);
				offset = str.length();
			}
		}
		
		if (sections != null && addAnchor) {
			// Add an anchor before each offset
			int start = 0;
			StringBuilder builder = new StringBuilder();
			
			index = 0;
			for(int pos : offsets) {
				String sec = str.substring(start, pos);
				builder.append(sec);
				String title = sections.get(index);
				String anchor = "<a name=" + title.replaceAll(" ", "_") + "></a>" + System.lineSeparator();
				builder.append(anchor);
				start = pos;
				index++;
			}
			
			String sec = str.substring(start, str.length());
			builder.append(sec);
			
			str = builder.toString();
		}

		return sections;
	}
}
