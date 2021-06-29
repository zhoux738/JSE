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

import info.julang.eng.mvnplugin.GlobalLogger;
import info.julang.eng.mvnplugin.docgen.DocGenException;
import info.julang.eng.mvnplugin.docgen.DocModel.TutorialPseudoType;
import info.julang.eng.mvnplugin.docgen.IHtmlDocMerger;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.eng.mvnplugin.docgen.SerializationType;
import info.julang.eng.mvnplugin.htmlgen.ApiIndexModel;
import info.julang.eng.mvnplugin.htmlgen.MarkDown2HtmlConverter;
import info.julang.eng.mvnplugin.htmlgen.WebsiteResources;
import info.julang.execution.namespace.NamespacePool;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Information about the tutorial pages.
 * 
 * @author Ming Zhou
 */
public class TutorialInfo {

	public interface IChapterInfo {
		public String getLinkName();
		public String getTitle();
	}
	
	public static interface IChapterInfoProcessor {
		void process(IChapterInfo info);
	}
	
	public class ChapterInfo implements IChapterInfo {
		private String name;
		private File origin;
		private StringBuilder doc;
		private String title;
		ChapterInfo prev;
		ChapterInfo next;
		
		private ChapterInfo(String name, String title, File origin) {
			this.name = name;
			this.origin = origin;
			this.title = title;
			this.doc = new StringBuilder("## " + title + System.lineSeparator());
		}
		
		public String getLinkName(){
			return name;
		}
		
		public String getTitle(){
			return title;
		}
		
		public IChapterInfo getPrev(){
			return prev;
		}
		
		public IChapterInfo getNext(){
			return next;
		}
	}
	
	private File tutDocRoot;
	private File origin;
	private File htmlDocRoot;
	private List<ChapterInfo> chs;
	private Map<String, ChapterInfo> kws;
	private SerializationType stype;
	private boolean sorted;
	private IHtmlDocMerger merger;
	private NamespacePool np;
	
	public TutorialInfo(File tutDocRoot, File htmlDocRoot, SerializationType stype, IHtmlDocMerger merger) {
		origin = new File(tutDocRoot, "original");
		this.tutDocRoot = tutDocRoot;
		this.htmlDocRoot = htmlDocRoot;
		chs = new LinkedList<ChapterInfo>();
		kws = new HashMap<String, ChapterInfo>();
		this.stype = stype;
		this.merger = merger;
		this.np = new NamespacePool();
	}
	
	public void foreach(IChapterInfoProcessor processor){
		if (!sorted){
			sort();
		}
		
		for(ChapterInfo c : chs) {
			processor.process(c);
		}
	}
	
	public void serialize(
		Pattern pat, ModuleContext mc, ApiIndexModel.TutorialIndexModel tutIndex) 
		throws MojoExecutionException {
		
		File dir = new File(tutDocRoot, "markdown");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		if (stype == SerializationType.WEBSITE && !htmlDocRoot.exists()) {
			htmlDocRoot.mkdirs();
		}
		
		try {
			int i = 1;
			for (ChapterInfo ch : chs) {
				if (pat.matcher(ch.title).matches()){
					GlobalLogger.get().info("Generating MD for tutorial: " + ch.title + " ...");
					String input = ch.doc.toString();
					List<IParsedDocSection> list = SummaryConvertor.convert(mc, this, np, input, TutorialPseudoType.INSTANCE, null);
					File output = new File(dir, ch.name + ".md");
					try (MarkdownWriter writer = new MarkdownWriter(output)){
						writer.add(
							StylizedString
							.create("CHAPTER " + i, false)
							.addColor(Color.decode("0xC8C8C8"))
							.inTag("<p style=\"margin-bottom: 0px\">"));
						writer.nextLine(2, false);
						
						List<String> sectionNames = new ArrayList<String>();
						if (list != null) {
							for (IParsedDocSection section : list) {
								if (section instanceof PlainSection) {
									PlainSection ps = (PlainSection)section;
									List<String> ssNames = ps.getSubSectionNames(3, true);
									if (ssNames != null) {
										sectionNames.addAll(ssNames);
									}
								}
							}
						}
						
						writer.addAttachment(MarkdownWriter.AttachementType.INDEX_TABLE_3, sectionNames);
						writer.append(list);
					}
					
					if (stype == SerializationType.WEBSITE) {
						File htmlFile = new File(htmlDocRoot, ch.name + ".html");
						MarkDown2HtmlConverter htmlConv = new MarkDown2HtmlConverter(output);
						String contents = htmlConv.convert();
						merger.mergeDocTemplate(tutIndex, WebsiteResources.tutorial, "", ch.getTitle(), contents, htmlFile, ch);
					}
				}
				
				i++;
			}
		} catch (IOException e) {
			throw new DocGenException("Couldn't generate tutorial MD files.", e);
		}
	}

	private final static String ORG_EXT = ".txt";
	private final static char CH_TITLE_SPLITTER = '_';
	
	public ChapterInfo getChapterByKeyword(String key){
		ChapterInfo ci = kws.get(key);
		return ci;
	}
	
	public void initialize() {
		origin.listFiles(new FileFilter(){

			private Set<Integer> set = new HashSet<Integer>();
			
			@Override
			public boolean accept(File file) {
				String name = file.getName();
				if(name.endsWith(ORG_EXT)){
					int index = name.indexOf(CH_TITLE_SPLITTER);
					String secOne = name.substring(0, index);
					
					try {
						int i = Integer.parseInt(secOne);
						if (set.contains(i)) {
							throw new DocGenException("The chapter's number, " + i + ", is used more than once.");
						}
					} catch (NumberFormatException e) {
						throw new DocGenException(
							"The chapter's name, " + name + ", is not properly started. " + 
							"It must be started with a number to be immediately followed by '" + CH_TITLE_SPLITTER + "'.");
					}
					
					String chName = "ch_" + secOne;
					String title = name
						.substring(index + 1, name.length() - ORG_EXT.length())
						.replace(String.valueOf(CH_TITLE_SPLITTER), " ");
					TutorialInfo.this.addChapter(chName, title, file);
					return true;
				}
				
				return false;
			}
			
		});
		
		try {
			TutorialInfo.this.readAllChapters();
		} catch (IOException e) {
			throw new DocGenException("Couldn't read tutorial files.", e);
		}
	}
	
	private void sort(){
		chs.sort(new Comparator<ChapterInfo>(){

			@Override
			public int compare(ChapterInfo o1, ChapterInfo o2) {
				return o1.name.compareTo(o2.name);
			}
			
		});
		
		int last = chs.size() - 1;
		for(int i = 0; i <= last; i++) {
			if (i < last) {
				chs.get(i).next = chs.get(i + 1);
			}
			if (i > 0) {
				chs.get(i).prev = chs.get(i - 1);
			}
		}
		
		sorted = true;
	}

	private void addChapter(String chName, String title, File file) {
		ChapterInfo ci = new ChapterInfo(chName, title, file);
		chs.add(ci);
	}

	private void readAllChapters() throws FileNotFoundException, IOException {
		if (!sorted){
			sort();
		}
		
		for (ChapterInfo ch : chs) {
			try(BufferedReader reader = new BufferedReader(new FileReader(ch.origin))){
				// Process keywords
				boolean processed = false;
				String line = reader.readLine().trim();
				if (line.startsWith("[") && line.endsWith("]")){
					String[] kvp = line.substring(1, line.length() - 1).split(":");
					String key = kvp[0].trim();
					if (key.equals("keywords")) {
						String[] kws = kvp[1].split(",");
						for(String kw : kws){
							String kwd = kw.trim();
							this.kws.put(kwd, ch);
						}
						processed = true;
					}
				}
				
				if (!processed) {
					throw new DocGenException(
						"Couldn't generate tutorial MD file from origin file: " + 
						ch.origin.getAbsolutePath() + ". No [keywords: ...] line is found inside.");
				}

				// Read everything else into a string
				StringBuilder sb = ch.doc;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
					sb.append(System.lineSeparator());
	            }
			}
		}
	}
}
