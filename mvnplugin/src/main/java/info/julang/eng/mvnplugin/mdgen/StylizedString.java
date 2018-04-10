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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import info.julang.eng.mvnplugin.GlobalLogger;

/**
 * A string that can be marked by various WEBSITE tags. This class is also a builder of its own instance.
 * 
 * @author Ming Zhou
 */
public class StylizedString {
	
	protected String txt;
	private String color;
	private String size;
	private String link;
	private boolean byMarkdown;
	private List<String> htmlTags;
	
	/**
	 * Create a string that can be stylized in Markdown syntax.
	 */
	public static StylizedString create(String txt){
		return new StylizedString(txt, true);
	}
	
	/**
	 * Create a string that can be stylized in either Markdown or WEBSITE syntax (if byTag = true).
	 */
	public static StylizedString create(String txt, boolean byMarkdown){
		return new StylizedString(txt, byMarkdown);
	}
	
	protected StylizedString(String txt, boolean byMarkdown){
		this.txt = txt;
		this.byMarkdown = byMarkdown;
	}
	
	public StylizedString toBold(boolean value){
		if (value) { 
			txt = byMarkdown ? "**" + txt + "**" : "<b>" + txt + "</b>";
		}
		return this;
	}
	
	public StylizedString toBold(){
		return toBold(true);
	}
	
	public StylizedString toItalic(boolean value){
		if (value) {
			txt = byMarkdown ? "*" + txt + "*" : "<i>" + txt + "</i>";
		}
		return this;
	}
	
	public StylizedString toItalic(){
		return toItalic(true);
	}
	
	public StylizedString addColor(Color color){
		if(color != null){
			this.color = "#" + Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
		}
		return this;
	}
	
	public StylizedString toSub(){
		txt = "<sub>" + txt + "</sub>";
		return this;
	}
	
	public StylizedString toSup(){
		txt = "<sup>" + txt + "</sup>";
		return this;
	}
	
	public StylizedString toCode(){
		return toCode(true);
	}
	
	public StylizedString toCode(boolean codify){
		if (codify) {
			txt = byMarkdown ? "`" + txt + "`" : "<code>" + txt + "</code>";
		}
		return this;
	}

	public StylizedString setSize(int i) {
		if (i > 0) {
			size = String.valueOf(i);
		}
		return this;
	}
	
	public StylizedString toLink(String link) {
		this.link = link;
		return this;
	}
	
	/**
	 * Call this with pre-tag only. The post-tag will be added automatically.
	 * 
	 * @param tag such as &lt;p&gt; or &lt;p style="margin: 10px;"&gt;
	 * @return
	 */
	public StylizedString inTag(String tag) {
		if (byMarkdown) {
			// Only working if styling in HTML
			return this;
		}
		
		if (htmlTags == null) {
			htmlTags = new ArrayList<String>();
		}
		
		htmlTags.add(tag);
		return this;
	}
	
	public PreStylizedString merge(StylizedString... others) {
		StringBuilder sb = new StringBuilder(this.toString());
		for(StylizedString ss : others){
			sb.append(ss.toString());
		}
		return new PreStylizedString(sb.toString());
	}
	
	@Override
	public String toString(){
		String res = (color != null || size != null) ? 
			// Apply <font></font> only if color or size is customized.
			"<font" + (color != null ? " color=\"" + color + "\"" : "") 
			+ (size != null ? " size=\"" + size + "\"" : "") 
			+ ">" + txt + "</font>" 
			: txt;
		if (link != null) {
			res = byMarkdown ? "[" + res + "](" + link + ")" : "<a href=\"" + link + "\">" + res + "</a>";
		}
		
		if (!byMarkdown && this.htmlTags != null) {
			for (String tag : htmlTags) {
				String endTag = null;
				int index = tag.indexOf('<');
				if (index != -1) {
					int len = tag.length();
					int search = index + 1;
					int start = -1;
					while(search < len){
						char c = tag.charAt(search);
						if (c == ' ' || c == '\t' || c == '\r' || c == '\t') {
							search++;
						} else {
							start = search;
							break;
						}
					}

					search = start;
					int end = -1;
					while(search < len){
						char c = tag.charAt(search);
						if (c == ' ' || c == '\t' || c == '\r' || c == '\t' || c == '>') {
							end = search;
							break;
						} else {
							search++;
						}
					}
					
					if (start >= 0 & end >= 1) {
						String t = tag.substring(start, end);
						if (t.length() > 0) {
							endTag = "</" + t + ">";
						}
					}
				}
				
				if (endTag != null) {
					res = tag + res + endTag;
				} else {
					GlobalLogger.get().warn(tag + " is not a valid HTML tag.");
				}
			}
		}
		
		return res;
	}
	
}
