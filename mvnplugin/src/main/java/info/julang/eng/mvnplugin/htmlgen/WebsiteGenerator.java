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

package info.julang.eng.mvnplugin.htmlgen;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.velocity.VelocityContext;

import info.julang.eng.mvnplugin.GlobalLogger;
import info.julang.eng.mvnplugin.docgen.DocModel;
import info.julang.eng.mvnplugin.docgen.DocModel.DocType;
import info.julang.eng.mvnplugin.docgen.IHtmlDocMerger;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.eng.mvnplugin.docgen.ModuleContext.TopLevelDocProcessor;
import info.julang.eng.mvnplugin.htmlgen.ApiIndexModel.TutorialIndexModel;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.IChapterInfo;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.IChapterInfoProcessor;
import info.julang.typesystem.jclass.Accessibility;

public class WebsiteGenerator {

	private ModuleContext mc;
	private IHtmlDocMerger merger;
	private File websiteRoot;
	private TutorialInfo tinfo;
	
	public WebsiteGenerator(IHtmlDocMerger merger, ModuleContext mc, TutorialInfo tinfo, File websiteRoot){
		this.merger = merger;
		this.mc = mc;
		this.websiteRoot = websiteRoot;
		this.tinfo = tinfo;
	}
	
	/**
	 * Generate files needed for the website.
	 */
	public void genWebsite(ApiIndexModel api, TutorialIndexModel tut) throws MojoExecutionException {
		GlobalLogger.get().info("Generating top-level resources for website ...");
		if (!websiteRoot.exists()){
			websiteRoot.mkdirs();
		}

		mergeIndexTemplate(api, WebsiteResources.api);
		mergeIndexTemplate(tut, WebsiteResources.tutorial);
		
		genSitemap(api, tut);
	}

	public ApiIndexModel getApiIndexModel() throws MojoExecutionException {
		final ApiIndexModel index = new ApiIndexModel();
		mc.foreach(new TopLevelDocProcessor(){
			@Override
			public void process(String key, DocModel.Documented doc) {
				DocType dtyp = doc.getDocType();
				if (dtyp == DocType.TYPE && doc.visibility == Accessibility.PUBLIC 
					|| dtyp == DocType.SCRIPT) {
					index.addType(doc);
				}
			}
		}, null);

		return index;
	}
	
	public TutorialIndexModel getTutorialIndexModel() throws MojoExecutionException {
		final TutorialIndexModel index = new TutorialIndexModel();
		
		tinfo.foreach(new IChapterInfoProcessor(){
			@Override
			public void process(IChapterInfo info) {
				index.addTutorial(info);
			}
		});

		return index;
	}

	private void genSitemap(ApiIndexModel api, TutorialIndexModel tut) throws MojoExecutionException {
		VelocityContext context = new VelocityContext();
		context.put("tv_tut_root", tut.getRoot());
		context.put("tv_api_root", api.getRoot());
		context.put("tv_hostname", mc.getMetatdata(ModuleContext.OFFICIAL_WEBSITE));
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		String datetime = sdf.format(new Date());
		context.put("tv_lastmod", datetime);

		File file = new File(websiteRoot, "sitemap.xml");
		merger.mergeToFile("website/sitemap.vm", context, file);
	}
	
	private void mergeIndexTemplate(ApiIndexModel model, WebsiteResources resType) throws MojoExecutionException {		
		File htmlFile = new File(websiteRoot, resType + ".html");
		merger.mergeDocTemplate(model, resType, "", "", null, htmlFile, model);
	}
}
