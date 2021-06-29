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
import java.util.List;

import info.julang.eng.mvnplugin.docgen.DocModel;
import info.julang.eng.mvnplugin.docgen.DocModel.AnchorType;
import info.julang.eng.mvnplugin.docgen.DocModel.ICoreSignatureDecorator;
import info.julang.eng.mvnplugin.docgen.DocModel.IMemberTypeDecorator;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeDescription;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeRef;
import info.julang.eng.mvnplugin.docgen.ModuleContext;
import info.julang.execution.namespace.NamespacePool;

/**
 * Produce a Markdown file for a built-in global script.
 * 
 * @author Ming Zhou
 */
/*
 * A sample output:
 * 
 * # assert.jul <font size= "3" color="#C8C8C8">SCRIPT</font>
 * 
 * Some texts...
 * Some more texts...
 * 
 * ## All Members
 * 
 * |*Type*|*Name*|*Signature*|
 * |:---|:---|:---------
 * |**function**|[*assertTrue*](#assertTrue-gf)|`void assertTrue(bool predicate)`
 * |**function**|[*assertEqual*](#assertEqual-gf)|`void assertEqual(var expected, var actual)`
 * 
 * ## Functions
 * <a name="assertTrue-gf"></a>
 * ### [`void`](../Void) _`assertTrue(bool predicate)`_
 * Assert that the predicate is true.
 * 
 * **Parameters**
 * 
 * - _predicate_<br>The predicate to assert on.
 * 
 * **Throws**
 * 
 * - [System.AssertException]<br>If the assertion fails.
 * 
 * Other functions...
 * 
 */
public class ScriptDocMarkdownConverter implements AutoCloseable {

	private class SignatureDecorator implements IMemberTypeDecorator, ICoreSignatureDecorator{
		
		@Override
		public String decParamType(String raw, TypeRef tref) {
			if (tref.isVoid()){
				return raw;
			} else {
				String linkPath = resolve(tref).getLinkPath(ScriptDocMarkdownConverter.this.script, null);
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
			if (tref.isVoid()) {
				return raw;
			}
			
			if (tref.dimension > 0) {
				raw += tref.getArrayDimension();
			}

			String linkPath = resolve(tref).getLinkPath(ScriptDocMarkdownConverter.this.script, null);
			StylizedString ss = StylizedString.create(raw).toLink(linkPath);
			return ss.toString();
		}
		
	};
	
	private MarkdownWriter writer;
	private DocModel.Script script;
	private IOException ioe;
	private ModuleContext mc;
	private TutorialInfo tut;
	
	private NamespacePool np;
	
	public ScriptDocMarkdownConverter(ModuleContext mc, TutorialInfo tut, DocModel.Script script, File file) {
		this.mc = mc;
		this.tut = tut;
		this.script = script;
		this.np = new NamespacePool();
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
		// # assert.jul <font size= "3" color="#C8C8C8">SCRIPT</font>
		String name = script.name;
		writer.addHeader(name, 1);
		writer.write(" ");
		writer.add(StylizedString.create("SCRIPT").addColor(Color.decode("0xC8C8C8")).setSizeByPercent(40));
		writer.nextLine(2, false);
		
		// Script summary
		String summary = script.getSummary();
		if (summary != null) {
			List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), summary, script, null);
			writer.append(list);
			writer.nextLine(2, false);
		}
		
		// References ("See Also ...")
		if (script.references != null && script.references.size() > 0) {
			addReferences(script.references);
			writer.nextLine(2, false);
		}
		
		/*
		 * ## All Definitions
		 * |*Type*|*Name*|*Signature*|
		 * |:---|:---|:---------
		 */
		writer.addHeader("All Definitions", 2);
		writer.nextLine(false);
		writer.divide(2);
		writer.addTableHeaders(
			StylizedString.create("Type").toBold(), 
			StylizedString.create("Name").toBold(), 
			StylizedString.create("Signature").toBold());
		
		// * |**script**|[*print.jul*](print.jul)|-
		List<DocModel.Script> includes = script.includes;
		if (includes != null){
			for (DocModel.Script include : includes) {
				StylizedString c0 = StylizedString.create("script").toItalic();
				StylizedString c1 = StylizedString.create(include.name, false).toBold().toLink(
					"../" + include.getDocFolderName() + "/" + include.name + ".html");
				StylizedString c2 = StylizedString.create("-").toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}
		
		// * |**function**|[*toString*](#gf-toString-var)|`string toString(var value)`
		List<DocModel.Function> functions = script.functions;
		if (functions != null){
			for (DocModel.Function function : functions) {
				StylizedString c0 = StylizedString.create("function").toItalic();
				String linkTgt = function.getAnchorName();
				StylizedString c1 = StylizedString.create(function.name, false).toBold().toLink("#"+ linkTgt);
				String sig = function.getExtendedSignature(false);
				StylizedString c2 = StylizedString.create(sig).toCode();
				writer.addTableRow(c0, c1, c2);
			}
		}

		writer.nextLine(1, false);
		
		SignatureDecorator decorator = new SignatureDecorator();

		if (functions != null && functions.size() > 0) {
			//	## Functions
			writer.addHeader("Functions", 2);
			writer.nextLine(false);
			writer.divide(2);
			for (DocModel.Function function : functions) {
				//	<a name="fun-1"></a>
				writer.addAnchor(function.getAnchorName());	
				writer.nextLine(false);
				
				//	### <code>[bool](../Bool) contains([string](#string) search, [int](#Integer) offset)</code>
				//	Check if the string contains the given string or character.
				StringBuilder header = new StringBuilder();
				header.append("<code>");
				// public
				header.append(function.getMemberHeader(true));
				// [bool](../Bool)
				header.append(function.getMemberType(true, decorator));
				// contains([string](#string) _search_, [int](#Integer) _offset_)
				header.append(function.getCoreSignature(true, null, decorator));
				header.append("</code>");
				writer.addHeader(header.toString(), 3);
				writer.nextLine(false);
				String msum = function.getSummary();
				List<IParsedDocSection> list = SummaryConvertor.convert(mc, tut, getNamespacePool(), msum, script, function);
				writer.append(list);
				writer.nextLine(2, false);
				
				boolean hasExtra = writeSubEntries(function);
				addLineBreakByTag(hasExtra ? 2 : 1);
			}	
		}
	}
	
	// Write sub-entries of a constructor/method: params, return, exceptions and references.
	private boolean writeSubEntries(DocModel.Function func) throws IOException{
		boolean hasExtra = false;
		
		//	**Required Policies**
		//
		//	- System.Environment/read, System.IO/read
		if (func.accesses != null) {
			int size = func.accesses.size();
			if (size > 0) {
				writer.add(StylizedString.create("Required Policies").toBold());
				writer.nextLine(2, false);
				
				StylizedString[] sss = new StylizedString[size * 2 - 1];
				for (int i = 0; i < size; i++){
					sss[i] = StylizedString.create(func.accesses.get(i)).toCode().addBackgroundColor(Color.decode("#DFDFDF"));

					i++;
					if (i < size) {
						sss[i] = StylizedString.create(", ");
					}
				}
				
				writer.addItem(StylizedString.mergeAll(sss), 0, null);
				
				writer.nextLine(false);
				hasExtra = true;
			}
		}
		
		//	**Parameters**
		//
		//	- _search_<br>The sub-string, or a single character, to search within this string. Note this method is special in that it can take two different types.
		//	- _offset_<br>The offset from which to start search.
		if (func.params != null && func.params.size() > 0) {
			writer.add(StylizedString.create("Parameters").toBold());
			writer.nextLine(2, false);
			for (TypeDescription td : func.params){
				writer.addAnchor(func.getAnchorName(AnchorType.Parameter, td.name));
				writer.nextLine(false);
				List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), td.summary, script, func);
				writer.addItem(StylizedString.create(td.name).toBold(), 0, sum);
			}
			writer.nextLine(false);
			hasExtra = true;
		}
		
		//	**Returns**
		//
		//	true if the searched string/character is found.
		if (func.returnType != null && func.returnType.type != null && !func.returnType.type.isVoid()){
			writer.add(StylizedString.create("Returns").toBold());
			writer.nextLine(2, false);
			writer.addAnchor(func.getAnchorName(AnchorType.Return, null));
			List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), func.returnType.summary, script, func);
			
			// A single item table
			writer.nextLine(false);
			writer.write("- ");
			writer.append(sum);
			
			writer.nextLine(2, false);
			hasExtra = true;
		}
		
		//	**Throws**
		//
		//	- [System.TypeIncompatibleException](TypeIncompatibleException)<br>If the parameter has a type which is neither string nor char.
		if (func.exceptions != null && func.exceptions.size() > 0) {
			writer.add(StylizedString.create("Throws").toBold());
			writer.nextLine(2, false);
			for (TypeDescription td : func.exceptions){
				String link = resolve(td.type).getLinkPath(script, null);
				List<IParsedDocSection> sum = SummaryConvertor.convert(mc, tut, getNamespacePool(), td.summary, script, func);
				writer.addItem(StylizedString.create(td.type.getFullName()).toLink(link), 0, sum);
			}
			writer.nextLine(false);
			hasExtra = true;
		}
		
		/// **References**
		if (func.references != null && func.references.size() > 0) {
			addReferences(func.references);
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
			String link = resolve(tr).getLinkPath(script, null);
			writer.addItem(StylizedString.create(tr.getFullName()).toLink(link), 0, null);
		}
	}
	
	private NamespacePool getNamespacePool() {
		return np;
	}
	
	// If the TypeRef is not resolvable, try to resolve it within the current module.
	private TypeRef resolve(TypeRef tr){
		return tr.resolve(getNamespacePool(), mc);
	}
}
