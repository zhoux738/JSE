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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.julang.eng.mvnplugin.GlobalLogger;
import info.julang.eng.mvnplugin.docgen.DocModel;
import info.julang.eng.mvnplugin.docgen.DocModel.AnchorType;
import info.julang.eng.mvnplugin.docgen.DocModel.ICoreSignatureDecorator;
import info.julang.eng.mvnplugin.docgen.DocModel.IMemberTypeDecorator;
import info.julang.eng.mvnplugin.docgen.DocModel.PrimitiveType;
import info.julang.eng.mvnplugin.docgen.DocModel.Type;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeDescription;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeRef;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.eng.mvnplugin.docgen.TypeInfo;
import info.julang.execution.namespace.NamespacePool;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.util.Pair;

/*
 * A sample output:
 * 
 * # String <font size= "3" color="#C8C8C8">CLASS</font>
 * 
 * **Language-level Alias** - <font size= "3" color="#dark-purple">**`string`**</font>
 * 
 * String represents a fixed array of characters.<br><br>String is a very special class type in Julian. Its assignment behavior is copy-by-value, instead of copy-by-references. This means assigning a string SA to string SB would first create a bit-level copy of the value that SA points to, then let the copy be pointed by SB, discarding whatever SB was previously referring.<br><br>String is immutable. The methods exposed by this class are for reading its contents in various ways, and if any manipulating is implied (such as [replace](#repleace)), it always mean to create a new string as the result of manipulation. The original string instance always remain unchanged.<br><br>String supports concatenaton operation by '+', which can also be used along with values of other types, as long as at least one operand is string:<pre><code>&nbsp;&nbsp;<font color="dark-purple"><strong>string</strong></font> s = "a" + "b" + 3;
 * &nbsp;&nbsp;<font color="dark-purple"><strong>for</strong></font> ( ) {
 * &nbsp;&nbsp;&nbsp;&nbsp;a =
 * &nbsp;&nbsp;}</code></pre>
 * Some more texts...
 * 
 * ## All Members
 * 
 * |*Type*|*Name*|*Signature*|
 * |:---|:---|:---------
 * |**field**<sup><font color="purple">c</font></sup>|[*length*](#length-1)|`public const int length`
 * |**method**|*contains*|`public bool contains(string search, int offset)`
 * |**method**<sup><font color="purple">s</font></sup>|*tests*|`public static bool tests(string search, int offset)`
 * 
 * ## Fields
 * <a name="length-1"></a>
 * ### `public const `[`int`](../Integer) _`length`_
 * The length of this string.
 * 
 * ## Methods
 * ### `public `[`bool`](../Bool) <code>_contains_([string](#string) search, [int](#Integer) offset)</code>
 * Check if the string contains the given string or character.
 * 
 * **Parameters**
 * 
 * - _search_<br>The sub-string, or a single characater, to search within this string. Note this method is special in that it can take two different types.
 * - _offset_<br>The offset from which to start search.
 * 
 * **Returns**
 * 
 * true if the searched string/character is found.
 * 
 * **Throws**
 * 
 * - [System.TypeIncompatibleException](TypeIncompatibleException)<br>If the parameter has a type which is neither string nor char.
 * 
 */
public class MarkdownConverter implements AutoCloseable {

	private class SignatureDecorator implements IMemberTypeDecorator, ICoreSignatureDecorator{
		
		@Override
		public String decParamType(String raw, TypeRef tref) {
			if (tref.isVoid()){
				return raw;
			} else {
				String linkPath = resolve(tref).getLinkPath(MarkdownConverter.this.typ, null);
				if (tref.dimension > 0) {
					raw += tref.getArrayDimension();
				}
				
				StylizedString ss = StylizedString.create(raw).toLink(linkPath);
				return ss.toString();
			}
		}

		@Override
		public String decMemberName(String raw) {
			StylizedString ss = StylizedString.create(raw).toItalic();
			return ss.toString();
		}

		@Override
		public String decMemberType(String raw, TypeRef tref) {
			if (tref.dimension > 0) {
				raw += tref.getArrayDimension();
			}
			
			return raw;
		}
		
	};
	
	private MarkdownWriter writer;
	private Type typ;
	private IOException ioe;
	private ModuleContext mc;
	private TutorialInfo tut;
	
	private NamespacePool np;
	
	public MarkdownConverter(ModuleContext mc, TutorialInfo tut, Type typ, File file) {
		this.mc = mc;
		this.tut = tut;
		this.typ = typ;
		try {
			writer = new MarkdownWriter(file);
		} catch (IOException e) {
			ioe = e;
		}
	}

	@Override
	public void close() {
		if (writer != null) {
			try {
				writer.flush();
			} catch (IOException e) {
				// no-op
			}
			
			try {
				writer.close();
			} catch (IOException e) {
				// no-op
			}
		}
	}

	/**
	 * Write the type doc to the file in Markdown format.
	 * 
	 * @throws IOException
	 */
	public void write() throws IOException {
		if (ioe != null) {
			throw ioe;
		}

		// Title
		// Example:
		// # String <font size= "3" color="#C8C8C8">CLASS</font>
		String name = typ.name;
		writer.addHeader(name, 1);
		writer.write(" ");
		if (typ.subtype != null) {
			String subtitle = typ.subtype.name();
			if (typ.subtype == ClassSubtype.CLASS) {
				DocModel.ClassType ctype = (DocModel.ClassType)typ;
				if (ctype.isStatic) {
					subtitle = "STATIC " + subtitle;
				}
			}
			writer.add(StylizedString.create(subtitle).addColor(Color.decode("0xC8C8C8")).setSize(3));
		} else if (typ instanceof PrimitiveType && !typ.name.toLowerCase().equals("any")){
			writer.add(StylizedString.create("PRIMITIVE").addColor(Color.decode("0xC8C8C8")).setSize(3));
		}
		writer.nextLine(2, false);
		
		// Type name alias (only applicable to some built-in types)
		if (typ instanceof PrimitiveType) {
			PrimitiveType pt = (PrimitiveType)typ;
			String alias = pt.alias;
			if (alias != null && !"".equals(alias)){
				//**Language-level Alias** - <font size= "3" color="#dark-purple">**`string`**</font>
				writer.add(StylizedString.create("Language-level Alias").toBold());
				writer.write(" - ");
				writer.add(StylizedString.create(alias, false).toBold().toCode().setSize(3).addColor(CodeSnippet.KeywordColor));
				writer.nextLine(2, false);
			}
		}
		
		// Type summary
		String summary = typ.getSummary();
		if (summary != null) {
			List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), summary, typ, null);
			writer.append(list);
			writer.nextLine(2, false);
		}
		
		List<Pair<DocModel.ClassType, TypeRef>> extMethods = null;
		
		// Parent types
		if (typ.subtype != null) {
			switch(typ.subtype) {
			case CLASS:
				DocModel.ClassType clsfTyp = (DocModel.ClassType)typ;
				TypeRef parent = clsfTyp.parent;
				
				if (!typ.getFullName().equals("Object")) {
					if (parent == null || "".equals(parent)){
						parent = TypeRef.makeFromFullName("Object");
					}
					
					writer.add(StylizedString.create("Parent Class").toBold());
					writer.nextLine(2, false);
					String link = resolve(parent).getLinkPath(typ, null);
					writer.addItem(StylizedString.create(parent.getFullName()).toLink(link), 0, null);
					writer.nextLine(2, false);			
				}
				// fall thru
			case INTERFACE:
				DocModel.InterfaceType itfTyp = (DocModel.InterfaceType)typ;
				List<TypeRef> intfs = itfTyp.interfaces;
				if (intfs != null && intfs.size() > 0){
					writer.add(StylizedString.create("Parent Interfaces").toBold());
					writer.nextLine(2, false);
					for (TypeRef tref : intfs) {
						String link = resolve(tref).getLinkPath(typ, null);
						writer.addItem(StylizedString.create(tref.getFullName()).toLink(link), 0, null);
					}
					
					writer.nextLine(2, false);
				}
				
				List<TypeRef> exts = itfTyp.extensions;
				if (exts != null && exts.size() > 0){
					writer.add(StylizedString.create("Known Extensions").toBold());
					writer.nextLine(2, false);
					for (TypeRef extRef : exts) {
						String link = resolve(extRef).getLinkPath(typ, null);
						writer.addItem(StylizedString.create(extRef.getFullName()).toLink(link), 0, null);
						
						TypeInfo extTypInfo = mc.getTypeInfo(extRef.simpleName, 0, typ.getNamespacePool());
						DocModel.ClassType clsTyp = (DocModel.ClassType)extTypInfo.getTypeDoc();
						if (clsTyp.methods.size() > 0) {
							if (extMethods == null) {
								extMethods = new ArrayList<>();
							}
							extMethods.add(new Pair<>(clsTyp, extRef));
						}
					}
					
					writer.nextLine(2, false);
				}
				break;
			default:
				break;
			}
		}
		
		// References ("See Also ...")
		if (typ.references != null && typ.references.size() > 0) {
			addReferences(typ.references);
			writer.nextLine(2, false);
		}
		
		/*
		 * ## All Members
		 * |*Type*|*Name*|*Signature*|
		 * |:---|:---|:---------
		 * |**constructor**|[*TypeName*](#c-void)|`public TypeName()`
		 * |**field**<sup><font color="purple">c</font></sup>|[*length*](#f-length)|`public const int length`
		 * |**method**|[*contains*](#m-contains-string-int)|`public bool contains(string search, int offset)`
		 */
		List<DocModel.Method> methods = null;
		List<DocModel.Constructor> ctors = null;
		List<DocModel.Field> fields = null;
		List<DocModel.EnumEntry> enumEntries = null;
		if (typ.subtype != null) {
			switch(typ.subtype) {
			case ATTRIBUTE:
				DocModel.AttributeType attrTyp = (DocModel.AttributeType)typ;
				fields = attrTyp.fields;
				break;
			case CLASS:
				DocModel.ClassType clsfTyp = (DocModel.ClassType)typ;
				methods = clsfTyp.methods;
				fields = clsfTyp.fields;
				ctors = clsfTyp.ctors;
				break;
			case ENUM:
				DocModel.EnumType enmTyp = (DocModel.EnumType)typ;
				enumEntries = enmTyp.entries;
				break;
			case INTERFACE:
				DocModel.InterfaceType itfTyp = (DocModel.InterfaceType)typ;
				methods = itfTyp.methods;
				break;
			default:
				break;
			}
		}
		
		// Early return if no members. Note if it's an ACEI type these lists can be empty but non-null. 
		// We intentionally output "All Members" here even if all of them are empty.
		if ((methods == null || methods.size() == 0)
			&& (ctors == null || ctors.size() == 0) 
			&& (fields == null || fields.size() == 0)
			&& (enumEntries == null || enumEntries.size() == 0)){
			return;
		}
		
		writer.addHeader("All Members", 2);
		writer.nextLine(false);
		writer.divide(2);
		writer.addTableHeaders(
			StylizedString.create("Type").toBold(), 
			StylizedString.create("Name").toBold(), 
			StylizedString.create(typ.subtype == ClassSubtype.ENUM ? "Value" : "Signature").toBold());
		
//		 * |**constructor**|[*TypeName*](#c-void)|`public TypeName()`
//		 * |**field**<sup><font color="purple">c</font></sup>|[*length*](#f-length)|`public const int length`
//		 * |**method**|[*contains*](#m-contains-string-int)|`public bool contains(string search, int offset)`
//		 * |**enum**|[*RED*](#e-RED)|`RED`
		 
		if (enumEntries != null){
			for (DocModel.EnumEntry enm : enumEntries) {
				StylizedString c0 = StylizedString.create("constant").toItalic();
				String linkTgt = enm.getAnchorName();
				StylizedString c1 = StylizedString.create(enm.name, false).toBold().toLink("#"+ linkTgt);
				StylizedString c2 = StylizedString.create(String.valueOf(enm.ordinal)).toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}
		
		if (ctors != null){
			for (DocModel.Constructor ctor : ctors) {
				StylizedString c0 = StylizedString.create("constructor").toItalic();
				String linkTgt = ctor.getAnchorName();
				StylizedString c1 = StylizedString.create(typ.name, false).toBold().toLink("#"+ linkTgt);
				String sig = ctor.getExtendedSignature(false);
				StylizedString c2 = StylizedString.create(sig).toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}
	
		if (fields != null){
			for (DocModel.Field field : fields) {
				StylizedString c0_ = StylizedString.create("field").toItalic();
				StylizedString c0s = field.isStatic ? StylizedString.create("&nbsp;S").toSup().addColor(Color.decode("#800080")) : PreStylizedString.EMPTY;
				StylizedString c0c = field.isConst ? StylizedString.create("&nbsp;C").toSup().addColor(Color.decode("#ff9900")) : PreStylizedString.EMPTY;
				StylizedString c0 = c0_.merge(c0s, c0c);
				String linkTgt = field.getAnchorName();
				StylizedString c1 = StylizedString.create(field.name, false).toBold().toLink("#"+ linkTgt);
				String sig = field.getExtendedSignature(false);
				StylizedString c2 = StylizedString.create(sig).toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}
		
		if (methods != null){
			for (DocModel.Method method : methods) {
				StylizedString c0_ = StylizedString.create("method").toItalic();
				StylizedString c0s = method.isStatic ? StylizedString.create("&nbsp;S").toSup().addColor(Color.decode("#800080")) : PreStylizedString.EMPTY;
				StylizedString c0a = method.isAbstract ? StylizedString.create("&nbsp;A").toSup().addColor(Color.decode("#990033")) : PreStylizedString.EMPTY;
				StylizedString c0 = c0_.merge(c0s, c0a);
				String linkTgt = method.getAnchorName();
				StylizedString c1 = StylizedString.create(method.name, false).toBold().toLink("#"+ linkTgt);
				String sig = method.getExtendedSignature(false);
				StylizedString c2 = StylizedString.create(sig).toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}
		
		if (extMethods != null){
			for (Pair<DocModel.ClassType, TypeRef> pair : extMethods) {
				for (DocModel.Method method : pair.getFirst().methods) {
					if (method.isStatic 
						&& method.visibility == Accessibility.PUBLIC
						&& method.params.size() > 0 
						&& method.params.get(0).type.getFullName().equals(typ.getFullName())) {
						StylizedString c0_ = StylizedString.create("method").toItalic();
						StylizedString c0e = StylizedString.create("&nbsp;E").toSup().addColor(Color.decode("#40826D")); // Viridian
						StylizedString c0 = c0_.merge(c0e);
						String linkTgt = method.getAnchorName();
						String link = pair.getSecond().getLinkPath(typ, null);
						StylizedString c1 = StylizedString.create(method.name, false).toBold().toLink(link + "#" + linkTgt);
						String sig = method.getExtendedSignature(true);
						StylizedString c2 = StylizedString.create(sig).toCode();
						writer.addTableRow(c0, c1, c2);
					} else {
						// This actually should not be an error. 
						// But let's output it for now in case we have a bug somewhere causing an extension member to be not added.
						GlobalLogger.get().warn(
							"The member " + method.name + " from " + pair.getFirst().getFullName() + " is not qualified as an extension.");
					}
				}
			}
		}

		writer.nextLine(1, false);
		
		SignatureDecorator decorator = new SignatureDecorator();

		ClassSubtype cst = typ.getSubtype();
		
		if (cst == ClassSubtype.ENUM && enumEntries != null && enumEntries.size() > 0) {
			//	## Fields
			writer.addHeader("Constants", 2);
			writer.nextLine(false);
			writer.divide(2);
			for (DocModel.EnumEntry enm : enumEntries) {
				//	<a name="e-RED"></a>
				writer.addAnchor(enm.getAnchorName());	
				writer.nextLine(false);
				//	### <code>public const [MyEnum](./MyEnum) RED</code>
				//	The red color.
				StringBuilder header = new StringBuilder();
				// `public const `
				header.append("<code>");
				header.append("public const ");
				// [MyEnum](./MyEnum)
				header.append(typ.name + " ");
				// RED
				header.append(enm.name);
				header.append(" = ");
				header.append(enm.ordinal);
				header.append("</code>");
				writer.addHeader(header.toString(), 3);
				writer.nextLine(false);
				List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), enm.getSummary(), typ, null);
				writer.append(list);
				writer.nextLine(false);
			}
			writer.nextLine(false);
		}
		
		if (cst == ClassSubtype.CLASS && ctors != null && ctors.size() > 0) {
			//	## Ctors
			writer.addHeader("Constructors", 2);
			writer.nextLine(false);
			writer.divide(2);
			for (DocModel.Constructor ctor : ctors) {
				writer.addAnchor(ctor.getAnchorName());
				writer.nextLine(false);

				//	### <code>public _MyType_([string](#string) raw)</code>
				//	Check if the string contains the given string or character.
				StringBuilder header = new StringBuilder();
				header.append("<code>");
				header.append(ctor.getMemberHeader(true));
				header.append(ctor.getCoreSignature(true, null, decorator));
				header.append("</code>");
				writer.addHeader(header.toString(), 3);
				writer.nextLine(false);
				List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), ctor.getSummary(), typ, ctor);
				writer.append(list);
				writer.nextLine(2, false);
				
				boolean hasExtra = writeSubEntries(ctor);
				addLineBreakByTag(hasExtra ? 2 : 1);
			}
		}
		
		if (cst == ClassSubtype.CLASS || cst == ClassSubtype.ATTRIBUTE) {
			if (fields != null && fields.size() > 0) {
				//	## Fields
				writer.addHeader("Fields", 2);
				writer.nextLine(false);
				writer.divide(2);
				for (DocModel.Field field : fields) {
					//	<a name="length-1"></a>
					writer.addAnchor(field.getAnchorName());	
					writer.nextLine(false);
					//	### <code>public const [int](../Integer) length</code>
					//	The length of this string.
					StringBuilder header = new StringBuilder();
					// `public const `
					header.append("<code>");
					header.append(field.getMemberHeader(true));
					// [`int`](../Integer)
					header.append(field.getMemberType(true, decorator));
					// _`length`_
					header.append(field.getCoreSignature(true, null, decorator));
					header.append("</code>");
					writer.addHeader(header.toString(), 3);
					writer.nextLine(false);
					List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), field.getSummary(), typ, field);
					writer.append(list);
					addLineBreakByTag(2);
				}
			}
		}

		if (cst == ClassSubtype.CLASS || cst == ClassSubtype.INTERFACE) {
			if (methods != null && methods.size() > 0) {
				//	## Methods
				writer.addHeader("Methods", 2);
				writer.nextLine(false);
				writer.divide(2);
				for (DocModel.Method method : methods) {
					//	<a name="fun-1"></a>
					writer.addAnchor(method.getAnchorName());	
					writer.nextLine(false);
					
					//	### <code>public [bool](../Bool) contains([string](#string) search, [int](#Integer) offset)</code>
					//	Check if the string contains the given string or character.
					StringBuilder header = new StringBuilder();
					header.append("<code>");
					// public
					header.append(method.getMemberHeader(true));
					// [bool](../Bool)
					header.append(method.getMemberType(true, decorator));
					// contains([string](#string) _search_, [int](#Integer) _offset_)
					header.append(method.getCoreSignature(true, null, decorator));
					header.append("</code>");
					writer.addHeader(header.toString(), 3);
					writer.nextLine(false);
					String msum = method.getSummary();
					List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), msum, typ, method);
					writer.append(list);
					writer.nextLine(2, false);
					
					boolean hasExtra = writeSubEntries(method);
					addLineBreakByTag(hasExtra ? 2 : 1);
				}	
			}		
		}
	}
	
	// Write sub-entries of a constructor/method: params, return, exceptions and references.
	private boolean writeSubEntries(DocModel.Constructor ctor) throws IOException{
		boolean hasExtra = false;
		
		//	**Parameters**
		//
		//	- _search_<br>The sub-string, or a single character, to search within this string. Note this method is special in that it can take two different types.
		//	- _offset_<br>The offset from which to start search.
		if (ctor.params != null && ctor.params.size() > 0) {
			writer.add(StylizedString.create("Parameters").toBold());
			writer.nextLine(2, false);
			for (TypeDescription td : ctor.params){
				writer.addAnchor(ctor.getAnchorName(AnchorType.Parameter, td.name));
				writer.nextLine(false);
				List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), td.summary, typ, ctor);
				writer.addItem(StylizedString.create(td.name).toBold(), 0, sum);
			}
			writer.nextLine(false);
			hasExtra = true;
		}
		
		//	**Returns**
		//
		//	true if the searched string/character is found.
		if (ctor instanceof DocModel.Method){
			DocModel.Method m = (DocModel.Method)ctor;
			if (m.returnType != null && m.returnType.type != null && !m.returnType.type.isVoid()){
				writer.add(StylizedString.create("Returns").toBold());
				writer.nextLine(2, false);
				writer.addAnchor(ctor.getAnchorName(AnchorType.Return, null));
				List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), m.returnType.summary, typ, ctor);
				writer.append(sum);
				writer.nextLine(2, false);
				hasExtra = true;
			}
		}
		
		//	**Throws**
		//
		//	- [System.TypeIncompatibleException](TypeIncompatibleException)<br>If the parameter has a type which is neither string nor char.
		if (ctor.exceptions != null && ctor.exceptions.size() > 0) {
			writer.add(StylizedString.create("Throws").toBold());
			writer.nextLine(2, false);
			for (TypeDescription td : ctor.exceptions){
				String link = resolve(td.type).getLinkPath(typ, null);
				List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), td.summary, typ, ctor);
				writer.addItem(StylizedString.create(td.type.getFullName()).toLink(link), 0, sum);
			}
			writer.nextLine(false);
			hasExtra = true;
		}
		
		/// **References**
		if (ctor.references != null && ctor.references.size() > 0) {
			addReferences(ctor.references);
			writer.nextLine(false);
			hasExtra = true;
		}
		
		return hasExtra;
	}
	
	// Add a <br> in a new line, then follow with two textual line breaks (\n). Example:
	// [Before]
	// sample text_
	// [After]
	// sample text
	// <br>
	//
	// _
	private void addLineBreakByTag(int leadingLineBreaks) throws IOException {
		writer.nextLine(leadingLineBreaks, false);
		writer.nextLine(true);
		writer.nextLine(2, false);
	}

	private void addReferences(List<TypeRef> references) throws IOException {
		writer.add(StylizedString.create("See Also").toBold());
		writer.nextLine(2, false);
		for (TypeRef tr : references){
			String link = resolve(tr).getLinkPath(typ, null);
			writer.addItem(StylizedString.create(tr.getFullName()).toLink(link), 0, null);
		}
	}
	
	private NamespacePool getNamespacePool(){
		if (np == null) {
			NamespacePool tnp = typ.getNamespacePool();
			if (tnp != null) {
				List<String> nss = typ.getNamespacePool().getNamespaces();
				String mn = typ.getModuleName();
				np = new NamespacePool();
				boolean foundSys = false;
				boolean foundThis = false;
				for (String ns : nss) {
					np.addNamespace(ns);
					if (ns.equals("System")){
						foundSys = true;
					} else if (ns.equals(mn)){
						foundThis = true;
					}
				}
				
				if (!foundSys) {
					np.addNamespace("System");
				}
				
				if (mn != null && !"".equals(mn) && !foundThis){
					np.addNamespace(mn);
				}
			} else {
				// A null namespace can happen if it's a built-in type.
				String mn = typ.getModuleName();
				np = new NamespacePool();
				np.addNamespace("System");
				if (mn != null && !"".equals(mn)){
					np.addNamespace(mn);
				}
			}
		}
		
		return np;
	}
	
	// If the TypeRef is not resolvable, try to resolve it within the current module.
	private TypeRef resolve(TypeRef tr){
		return tr.resolve(getNamespacePool(), mc);
	}
}
