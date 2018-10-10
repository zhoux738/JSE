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
import info.julang.eng.mvnplugin.docgen.DocModel;
import info.julang.eng.mvnplugin.docgen.DocModel.AnchorType;
import info.julang.eng.mvnplugin.docgen.DocModel.AttributeType;
import info.julang.eng.mvnplugin.docgen.DocModel.EnumEntry;
import info.julang.eng.mvnplugin.docgen.DocModel.EnumType;
import info.julang.eng.mvnplugin.docgen.DocModel.Field;
import info.julang.eng.mvnplugin.docgen.DocModel.TutorialPseudoType;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeDescription;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeRef;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.eng.mvnplugin.docgen.TypeInfo;
import info.julang.eng.mvnplugin.htmlgen.WebsiteResources;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.ChapterInfo;
import info.julang.execution.namespace.NamespacePool;
import info.julang.modulesystem.BadNameException;
import info.julang.modulesystem.naming.FQName;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

/**
 * Convert a in-doc link to an markdown link.
 * 
 * @author Ming Zhou
 */
public class InDocLink implements IParsedDocSection {

	private static final String TypeLink = "type:";
	private static final String ParamLink = "param:";
	private static final String FuncLink = "func:";
	private static final String ReturnLink = "return";
	private static final String TutorialLink = "tutorial:";
	private static final String Version = "version:";
	private static final String IndexTable = "index:";
	private static final String VersionCurrent = "current";
	private static final String TutorialIndexLink = "Julian Tutorial";
	
	private static final String BrokenLink = "broken-link";
	
	private ModuleContext mc;
	private TutorialInfo tut;
	private NamespacePool np;
	private DocModel.Type typ;
	private DocModel.Member mem;
	private String txt;
	private String rawlnk;
	private int indexLevel = -1;
	
	/**
	 * Create a doc link in the given context as defined by type and member.
	 * 
	 * @param str the original link text.
	 * @param typ The type from which this doc is extracted.
	 * @param mem If null, the doc is for the type itself; otherwise for this particular member.
	 */
	public InDocLink(
		ModuleContext mc, TutorialInfo tut, NamespacePool np, 
		String str, DocModel.Type typ, DocModel.Member mem) {
		this.mc = mc;
		this.np = np;
		this.tut = tut;
		this.typ = typ;
		this.mem = mem;
		
		int index = str.indexOf(']');
		txt = str.substring(1, index).trim();
		if (index + 2 <= str.length() - 1){
			rawlnk = str.substring(index + 2, str.length() - 1).trim();
		} else {
			if (txt.startsWith(ParamLink)){
				// [param: a] => [a](param: a)
				rawlnk = txt;
				txt = rawlnk.substring(ParamLink.length()).trim();
			} else if (txt.startsWith(TypeLink)) {
				// [type: A] => [A](type: A)
				rawlnk = txt;
				txt = rawlnk.substring(TypeLink.length()).trim();
			} else if (txt.startsWith(FuncLink)) {
				// [func: int, System.IO.File] => generate a string in the form of 'Function(int, File)' with each type linked.
				String params = txt.substring(FuncLink.length()).trim();
				txt = formatFunction(params);
				// (Intentionally leave rawlnk as null)
			} else if (txt.startsWith(TutorialLink)) {
				// [tutorial: Data Type] => generate a link to a certain chapter of tutorial
				String keyword = txt.substring(TutorialLink.length()).trim();
				txt = getTutLink(keyword, false);
				// (Intentionally leave rawlnk as null)
			} else if (TutorialIndexLink.equals(txt)) {
				// [Julian Tutorial] => generate a link to tutorial's entrance
				txt = getTutLink(null, false);
				// (Intentionally leave rawlnk as null)
			} else if (txt.startsWith(Version)) {
				// [version: current] => generate a string for version
				String ver = txt.substring(Version.length()).trim();
				txt = getVersionText(ver);
				// (Intentionally leave rawlnk as null)
			} else if (txt.startsWith(IndexTable)) {
				// [index: 3] => generate an index table corresponding to lines started with ### 
				indexLevel = Integer.parseInt(txt.substring(IndexTable.length()).trim());
				// (Intentionally leave rawlnk as null)
			} else {
				// []
				rawlnk = txt;
			}
		}
	}

	@Override
	public void appendTo(MarkdownWriter markdownWriter) throws IOException{
		if (this.indexLevel > 0) {
			@SuppressWarnings("unchecked")
			List<String> ssNames = (List<String>)markdownWriter.getAttachment(MarkdownWriter.AttachementType.INDEX_TABLE_3);
			if (ssNames != null) {
				StylizedString[] sss = new StylizedString[ssNames.size()];
				int i = 0;
				for(String str : ssNames) {
					sss[i] = StylizedString.create(str,  false).toLink("#" + str.replaceAll(" ", "_")).inTag("<li>");
					i++;
				}

				PreStylizedString pss = StylizedString.mergeAll(sss);
				pss = new PreStylizedString("<ul style=\"list-style-type:disc\">" + pss + "</ul>");
				markdownWriter.add(pss);
			}
		} else {
			markdownWriter.add(StylizedString.create(getText(), false).toLink(getLink()));
		}
	}

	private String getVersionText(String ver) {
		String vertext = VersionCurrent.equals(ver.toLowerCase()) ?
			mc.getMetatdata(ModuleContext.MD_VERSION) : ver;
			
		StylizedString ss = StylizedString.create(vertext).addColor(new Color(0, 191, 255, 255)); // "deep sky blue"	
		return ss.toString();
	}
	
	private String getTutLink(String keyword, boolean linkTargetOnly) {
		if (keyword == null){
			String tgt = "../" + WebsiteResources.tutorial.name() + ".html";
			return linkTargetOnly ? tgt : StylizedString.create("Julian Tutorial").toLink(tgt).toString();
		}
		
		ChapterInfo chap = tut.getChapterByKeyword(keyword);
		if (chap == null){
			GlobalLogger.get().warn("No tutorial link can be found for keyword \"" + keyword + "\".");
			return BrokenLink;
		} else {
			String cname = chap.getLinkName() + ".html";
			if (typ == TutorialPseudoType.INSTANCE){
				// If linking from tutorial, the link points to the same level
				return linkTargetOnly ? cname : StylizedString.create(chap.getTitle()).toLink(cname).toString();
			} else {
				// If linking from API docs, must navigate to Tutorial resource
				String modName = typ.getModuleName();
				String link = null;
				if (modName == null || "".equals(modName)){
					link = "../" + WebsiteResources.tutorial.name() + "/" + cname;
				} else {
					link = "../../" + WebsiteResources.tutorial.name() + "/" + cname;
				}
				
				return linkTargetOnly ? link : StylizedString.create(chap.getTitle()).toLink(link).toString();
			}
		}
	}
	
	private String formatFunction(String params) {
		String fun = "fun(" + params + ")";
		StringBuilder sb = new StringBuilder();
		sb.append("<code>");
		// [Function](Function)
		NamespacePool ns = new NamespacePool();
		ns.addNamespace(typ.getModuleName());
		TypeInfo tinfo = mc.getTypeInfo("Function", 0, ns);
		String link = getLinkPath(tinfo.getFullName());
		StylizedString ss1 = StylizedString.create(tinfo.getFullName()).toLink(link);
		sb.append(ss1.toString());

		sb.append('(');
		ParsedMember pm = this.parseMember(fun);
		DocModel.Method m = pm.asMethod();
		if (m != null) {
			List<TypeDescription> pms = m.params;
			int i = pms.size() - 1;
			for (TypeDescription td : pms) {
				TypeRef ptyp = td.type;
				String plink = getLinkPath(ptyp.getFullName());
				StylizedString pss = StylizedString.create(ptyp.simpleName).toLink(plink);
				sb.append(pss.toString());
				if (i > 0) {
					sb.append(", ");
				}
				i--;
			}
		}
		sb.append(')');
		sb.append("</code>");
		
		return sb.toString();
	}
	
	private String getText(){
		return txt;
	}
	
	private String getLink(){
		// Supported link types:
		// 1) [TTT]        same as [TTT](type: TTT)
		// 2) [TTT](LLL)   LLL can be
		// 2.1) AAA#BBB    It's a member of name BBB on FQTN AAA
		// 2.2) #BBB       It's a member of name BBB on the current type
		// 2.4) param: PPP A param with name PPP. This only applies to ctor/member doc
		// 2.5) type: AAA  The FQTN AAA
		// 2.6) return     Return value. This only applies to member doc 
		// 2.7) scheme://  External link
		// 2.8) starting with ./ or ../ 
		//                 Relative link
		if (rawlnk == null) {
			return null;
		}
		
		String link = null;
		if (rawlnk.startsWith("./") || rawlnk.startsWith(".//") || rawlnk.startsWith("../") || rawlnk.startsWith("..//")){
			// Relative link
			link = rawlnk;
		} else if (rawlnk.startsWith("http://") || rawlnk.startsWith("https://") || rawlnk.startsWith("file://")) {
			// External & absolute link
			link = rawlnk;
//			try {
//				URLEncoder.encode(rawlnk, StandardCharsets.UTF_8.toString());
//			} catch (UnsupportedEncodingException e) {
//				// This should not happen
//			}
		} else if (rawlnk.startsWith(TypeLink)){
			String typName = rawlnk.substring(TypeLink.length()).trim();
			link = typeNameToLink(typName, null);
		} else if (rawlnk.startsWith(ParamLink)){
			String paramName = rawlnk.substring(ParamLink.length()).trim();
			link = "#" + mem.getAnchorName(AnchorType.Parameter, paramName);
		} else if (ReturnLink.equals(rawlnk)){
			link = "#" + mem.getAnchorName(AnchorType.Return, null);
		} else if (rawlnk.startsWith(TutorialLink)) {
			String keyword = rawlnk.substring(TutorialLink.length()).trim();
			link = getTutLink(keyword, true);
		} 
		
		if (link == null && rawlnk.contains("#")){
			String typeName = rawlnk;
			if (rawlnk.startsWith(TypeLink)){
				typeName = rawlnk.substring(TypeLink.length()).trim();
			}
			String[] sections = typeName.split("#");
			if (sections.length == 2){
				String typName = sections[0];
				String memName = sections[1];
				link = typeNameToLink(typName, memName);
			}
		}
		
		if (link == null){
			// Treat this as a type link without 'type:' prefix. Issue a warning since we need phase this out. 
			// GlobalLogger.get().warn("Cannot find a link to type \'" + rawlnk + "\' from \'" + typ.getFullName() + "\'.");
			link = typeNameToLink(rawlnk.trim(), null);
		}
		
		return link;
	}
	
	/*
	 * Generate a type/member link.
	 * 
	 * The type name can be non-qualified. Resolved it against the context.
	 * The member name can be just a name. We need iterate over members on the type to find the one matching this name.
	 * The member name can also be a signature. If so we iterate over members on the type and compare each's signature with this one.
	 */
	private String typeNameToLink(String typeName, String memName) {
		NamespacePool ns = new NamespacePool();
		TypeInfo tinfo = null;
		if (typeName == null || "".equals(typeName)){
			ns.addNamespace(typ.getModuleName());
			tinfo = this.mc.getTypeInfo(typ.name, 0, ns);
		} else {
			FQName fn;
			try {
				fn = new FQName(typeName);
			} catch (BadNameException e) {
				return null;
			}
			
			String sn = fn.getSimpleName();
			if (!sn.equals(typeName)){
				// This is an FQ name
				ns.addNamespace(typeName.substring(0, typeName.length() - sn.length() - 1));
			} else {
				// This is an NFQ name
				// (Ideally we should also provide an NS same as the source code, but we can defer this. If the name is NQ, just use current module.)
				ns.addNamespace(typ.getModuleName());
			}
			
			tinfo = this.mc.getTypeInfo(typeName, 0, ns);			
		}

		if (tinfo == null){
			// Cannot resolve this type
			return null;
		}
		
		DocModel.Type typ = tinfo.getTypeDoc();
		String link = getLinkPath(typ.getFullName());

		// A link to type + member
		if (memName != null) {
			String memLink = null;
			ParsedMember pm = parseMember(memName);
			switch(typ.subtype){
			case ATTRIBUTE:
				DocModel.AttributeType at = (DocModel.AttributeType)typ;
				memLink = getMemberLink(at, pm);
				break;
			case CLASS:
				memLink = getMemberLink(typ, pm, true);
				break;
			case ENUM:
				DocModel.EnumType et = (DocModel.EnumType)typ;
				memLink = getMemberLink(et, pm);
				break;
			case INTERFACE:
				memLink = getMemberLink(typ, pm, false);
				break;
			}
			
			if (memLink != null) {
				link += "#" + memLink;
			}
		}
		
		return link;
	}

	private String getMemberLink(EnumType et, ParsedMember pm) {
		String f = pm.asField();
		if (f != null) {
			List<EnumEntry> entries = et.entries;
			for (EnumEntry ee : entries) {
				if (f.equals(ee.name)) {
					return ee.getAnchorName();
				}
			}
		}
		
		return null;
	}

	private String getMemberLink(AttributeType at, ParsedMember pm) {
		String f = pm.asField();
		if (f != null) {
			List<Field> fds = at.fields;
			for (Field fd : fds) {
				if (f.equals(fd.name)) {
					return fd.getAnchorName();
				}
			}
		}
		
		return null;
	}

	private String getMemberLink(DocModel.Type typ, ParsedMember pm, boolean isClass) {
		if (isClass) {
			DocModel.ClassType ct = (DocModel.ClassType)typ;
			String f = pm.asField();
			if (f != null) {
				List<Field> fds = ct.fields;
				for (Field fd : fds) {
					if (f.equals(fd.name)) {
						return fd.getAnchorName();
					}
				}
			}

			DocModel.Constructor ctor = pm.asConstructor();
			if (ctor != null) {
				List<DocModel.Constructor> ctors = ct.ctors;
				for (DocModel.Constructor con : ctors) {
					if (ctor.equals(con)) {
						return con.getAnchorName();
					}
				}
			}
		}
		
		DocModel.Method mtd = pm.asMethod();
		if (mtd != null) {
			// Strict mapping
			DocModel.InterfaceType it = (DocModel.InterfaceType)typ;
			List<DocModel.Method> methods = it.methods;
			for (DocModel.Method m : methods) {
				if (m.equals(mtd)) {
					return m.getAnchorName();
				}
			}
			
			// Loose mapping - this is to tolerate the doc authoring where the method's param list is empty
			if (mtd.params.size() == 0) {
				DocModel.Method tgt = null;
				for (DocModel.Method m : methods) {
					if (m.name.equals(mtd.name)) {
						if (tgt == null) {
							tgt = m;
						} else {
							// If there are more than one method with the same name, we cannot make a default selection
							GlobalLogger.get().warn("In type " + typ.getFullName() + ", two or more methods have name \"" + mtd.name + "\". Replace the doc reference with precise signature.");
							return null;
						}
					}
				}
				
				if (tgt != null) {
					return tgt.getAnchorName();
				}
			}
		}
		
		return null;
	}
	
	private ParsedMember parseMember(String memName){
		// memName can be one of three cases:
		//   1) just a name, such 'fun'
		//   2) a signature, such as 'fun(int)'
		//   3) an extended signature, such as 'fun(int val)'
		// 
		// Strategy:
		// - For 2) and 3), construct a method to compare against each member of same name. The method must
		//   contain these information to ensure equality: name, static, params
		// - Use common technique to parse memName, without using a parser. While a parser-based parsing is more capable,
		//   the reality is we don't have sufficient information to ensure its correctness (such as namespace).
		
		DocModel.Constructor ctor = null;
		boolean maybeField = false;
		int index = memName.indexOf('(');
		if (index != -1) {
			// Has parameter list part. Could be a ctor or method.
			NamespacePool ns = new NamespacePool();
			ns.addNamespace(typ.getModuleName());

			int endIndex = memName.indexOf(')', index);
			String plist = memName.substring(index + 1, endIndex).trim();
			
			memName = memName.substring(0, index);
			if (memName.equals(this.typ.name)){
				ctor = new DocModel.Constructor(memName);
			} else {
				ctor = new DocModel.Method();
				ctor.name = memName;
			}

			if (!"".equals(plist)){
				String[] sections = plist.split(",");
				for (String sec : sections) {
					String[] kvp = sec.trim().split("[ \t]+");
					if (kvp.length <= 2) {
						String ptype = kvp[0].trim(); // at least one
						int dim = 0;
						while (ptype.endsWith("[]")) {
							ptype = ptype.substring(0, ptype.length() - 2);
							dim++;
						}
						
						// must use a new namespace if the type is a FQN.
						TypeInfo tinfo = null;
						FQName fqn = new FQName(ptype);
						String sn = fqn.getSimpleName();
						if (!sn.equals(ptype)) {
							NamespacePool ns2 = new NamespacePool();
							ns2.addNamespace(ptype.substring(0, ptype.length() - sn.length() - 1));
							tinfo = mc.getTypeInfo(sn, dim, ns2);
						} else {
							tinfo = mc.getTypeInfo(ptype, dim, ns);
						}
						
						if (tinfo != null) {
							ctor.params.add(new TypeDescription("", "", tinfo.getFullName())); // name and summary are irrelevant here.
						}
					}
				}
			}
		} else {
			// No parameter list part. Could be a ctor, method or field.
			if (memName.equals(this.typ.name)){
				ctor = new DocModel.Constructor(memName);
			} else {
				ctor = new DocModel.Method();
				ctor.name = memName;
			}
			
			maybeField = true;
		}
		
		ParsedMember pm = new ParsedMember(ctor, maybeField);
		return pm;
	}
	
	private static class ParsedMember {
		
		private boolean maybeField;
		private boolean maybeCtor;
		private DocModel.Constructor ctor;
		
		ParsedMember (DocModel.Constructor ctor, boolean maybeField){
			this.ctor = ctor;
			this.maybeCtor = ctor.getClass() == DocModel.Constructor.class;
			this.maybeField = maybeField;
		}
		
		String asField(){
			if (maybeField) {
				return ctor.name;
			} else {
				return null;
			}
		}
		
		DocModel.Method asMethod(){
			if (ctor instanceof DocModel.Method) {
				return (DocModel.Method)ctor;
			} else {
				return null;
			}
		}
		
		DocModel.Constructor asConstructor(){
			return maybeCtor ? ctor : null;
		}
		
	}
	
	private String getLinkPath(String fullName){
		TypeRef tref = TypeRef.makeFromFullName(fullName);
		tref = tref.resolve(np, mc);
		String link = tref.getLinkPath(typ, null);
		return link;
	}
}
