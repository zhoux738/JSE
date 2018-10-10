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

import info.julang.eng.mvnplugin.docgen.DocModel;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.eng.mvnplugin.docgen.DocModel.TutorialPseudoType;
import info.julang.execution.namespace.NamespacePool;
import info.julang.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert a Julian Doc summary into multiple {@link IParsedDocSection parsed sections}.
 * <p>
 * A summary can appear at a number of places, including type's or member's general description, 
 * parameter description, return description and exception description. The summary may contain
 * specially marked sections that will be rendered by certain rules.
 * 
 * @author Ming Zhou
 */
public class SummaryConvertor {
	
	private final static String CODE = DocModel.Keys.CODE;
	private final static String CODE_END = DocModel.Keys.CODE_END;

	/*
	 * IMPLEMENTATION NOTES:
	 * 
	 * The idea is to use a regex to match tag sequences, namely [.+], in the raw string, and analyze each match. There several possibilities:
	 *   1) the tag is an explicit link
	 *   2) the tag is an implicit link
	 *   3) the tag represents start or end of a code section
	 * 
	 * Each section will be passed into an appropriate IParsedDocSection, which will take care of extracting/formatting the contents inside.
	 * 
	 * This method is able to handle a couple of corner cases:
	 *   1) code section contains tag-matching sequences.
	 *   2) explicit link contains tag-matching sequences.
	 *   3) [ or ] is escaped by `.
	 */
	public static List<IParsedDocSection> convert(
		ModuleContext mc, TutorialInfo tut, NamespacePool np,
		String summary, DocModel.Type typ, DocModel.Member mem){		
		Pattern pat = Pattern.compile("\\[[^\\]]+\\]");
		Matcher matcher = pat.matcher(summary);
		int plainStart = 0;
		List<IParsedDocSection> list = new ArrayList<IParsedDocSection>();
		int len = summary.length();
		int bracksToSkip = 0;
		while(matcher.find()){			
			// skip a certain count of matches
			if (bracksToSkip > 0) {
				bracksToSkip--;
				continue;
			}
			
			// exclude escaped patterns
			int start = matcher.start();
			int end = matcher.end();
			boolean escaped = start >= 1 && summary.charAt(start - 1) == '`' || summary.charAt(end - 1) == '`';
			if (escaped) {
				continue;
			}

			// add the previous plain section
			String sub = summary.substring(plainStart, start);
			if (sub.length() > 0) {
				list.add(new PlainSection(sub));
			}
			
			// analyze the tag: [xxx]
			String grp = matcher.group();
			Pair<String, String> tag = getTag(grp);
			IParsedDocSection section = null;
			if (CODE.equals(tag.getFirst()) && !CODE_END.equals(tag.getSecond())){
				sub = null;
				// go find the ending tag while ignoring everything in between.
				int end2 = -1;
				while(matcher.find()){
					sub = matcher.group();
					Pair<String, String> tag2 = getTag(sub);
					if (CODE.equals(tag2.getFirst()) && CODE_END.equals(tag2.getSecond())){
						end2 = matcher.end();
						break;
					}
				}
				
				if (end2 != -1){
					// found the ending tag
					sub = summary.substring(start, end2);
					section = new CodeSnippet(sub);
					end = end2;
				}
			} else {
				sub = null;
				int index = end;
				char c = '\0';
				if (index < summary.length()) {
					c = summary.charAt(index);
				}
				if (c == '('){
					// [AAA] (LLL)
					int total = 1;
					index++;
					boolean excludeBrackets = false;
					while(index < len){
						c = summary.charAt(index);
						switch(c){
						case '(':
							total++;
							break;
						case ')':
							total--;
							if (total == 0){
								end = index + 1;
								sub = summary.substring(start, end);
								section = new InDocLink(mc, tut, np, sub, typ, mem);
							}
							break;
						case '[':
							excludeBrackets = true;
							break;
						}
						
						if (sub != null) {
							break;
						} else {
							index++;
						}
					}
					
					if (sub != null && excludeBrackets) {
						Matcher innerMatcher = pat.matcher(sub);
						while(innerMatcher.find()){
							bracksToSkip++;
						}
						
						bracksToSkip--;
					}
				} else {
					// [AAA]
					sub = summary.substring(start, end);
					section = new InDocLink(mc, tut, np, sub, typ, mem);
				}
			}
			
			if (section == null) {
				sub = summary.substring(start, end);
				list.add(new PlainSection(sub));
			} else {
				list.add(section);
			}
			
			plainStart = end;
		}
		
		String sub = summary.substring(plainStart);
		if (sub.length() > 0) {
			list.add(new PlainSection(sub));
		}
		
		return list;
	}
	
	private static Pair<String, String> getTag(String raw){
		String[] sections = raw.substring(1, raw.length() - 1).split(":");
		return new Pair<String, String>(sections[0].trim(), sections.length >= 2 ? sections[1].trim() : null);
	}
}
