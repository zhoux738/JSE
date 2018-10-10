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

package info.julang.eng.mvnplugin.docgen;

import info.julang.eng.mvnplugin.GlobalLogger;
import info.julang.eng.mvnplugin.htmlgen.WebsiteResources;
import info.julang.execution.namespace.NamespacePool;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Documentation models to be serialized.
 * <p>
 * Some classes implement {@link Object#equals(Object)} only for the purpose of method signature matching, therefore
 * members such as visibility, parameter name and return types are not counted in. 
 * 
 * @author Ming Zhou
 */
public final class DocModel {
	
	/* An example of serialized data:

	 {
		"name" : "YY",
		"parent" : "System.PP",
		"interfaces" : 
			[
				{ "module" : "System.KK", "name" : "I3" }, 
				{ ... }
			],
		"type" : "class",
		"visibility" : "public",
		"summary" : "Main description of the type, in paragraphs. Retaining the format of original source.",
		"see" : 
			[
				{ "module" : "System.ZZ", "name" : "UU" }, 
				{ ... }
			],
		"ctors" : 
			[
				{ 
					"summary" : "Main description of the ctor.",
					"visibility" : "public",
					"params" : [
						{
							"name" : "param-name", 
							"type" : { "module" : "System.ZZ", "name" : "UU" }, 
							"description" : "description of the param"
						},
						{
							... ...
						}
					]	
				},
				{
					...
				},
			],
		"methods" : 
			[
				{ 
					"name" : "method1",
					"summary" : "Main description of the method.",
					"visibility" : "public",
					"static" : false,
					"params" : 
						[
							{
								"name" : "param-name", 
								"type" : { "module" : "System.ZZ", "name" : "UU" }, 
								"description" : "description of the param"
							},
							{
								... ...
							}
						],
					"return" : 
						{
							"type" : { "module" : "System.ZZ", "name" : "UU" }, 
							"description" : "description of the param"
						},
					"throws" :
						[	
							{
								"type" : { "module" : "System.HH", "name" : "T1Exception" }, 
								"description" : "when/why this exception would be thrown"
							}
							{
								... ...
							}
						]	
				},
				{
					...
				},
			],
		"fields" : 
			[
				{ 
					"name" : "field1",
					"summary" : "Main description of the field.",
					"visibility" : "public",
					"static" : false,
					"type" : { "module" : "System.ZZ", "name" : "UU" },	
				},
				{
					...
				}
			]
	 }
	*/
	
	public static class Keys {
		static final String SEE = "see";
		static final String SEE_ALSO = "see also";
		static final String PARAM = "param";
		static final String RETURN = "return";
		static final String THROWS = "throws";
		static final String INHERITED = "inherited";
		static final String NO_DOC = "nodoc";
		
		public static final String CODE = "code";
		public static final String CODE_END = "end";
		
		static final Set<String> ALL = new HashSet<String>();
		
		// Make sure this contains all the package-visible keys
		static {
			ALL.add(SEE);
			ALL.add(SEE_ALSO);
			ALL.add(PARAM);
			ALL.add(RETURN);
			ALL.add(THROWS);
			ALL.add(INHERITED);
			ALL.add(NO_DOC);
		}
	}
	
	public static class Metadata {
		public String version;
	}
	
	public static class TypeRef {
		/** The module name of this type. */
		public String moduleName;
		/** The simple name of this type. */
		public String simpleName;
		/** 0 if scalar, 1+ if it's an array. */
		public int dimension;
		
		private TypeRef() {
			
		}
		
		public TypeRef(TypeInfo ti) {
			this.moduleName = ti.getModuleName();
			this.simpleName = ti.getSimpleName();
			this.dimension = ti.getDimension();
		}
		
		public TypeRef clone(){
			TypeRef clone = new TypeRef();
			clone.moduleName = this.moduleName;
			clone.simpleName = this.simpleName;
			clone.dimension = this.dimension;
			return clone;
		}

		public static TypeRef makeFromFullName(String fname){
			TypeRef ref = new TypeRef();

			int dim = 0;
			while (fname.endsWith("[]")){
				fname = fname.substring(0, fname.length() - 2);
				dim++;
			}
			
			int index = fname.lastIndexOf('.');
			if (index > 0) {
				ref.moduleName = fname.substring(0, index);
				ref.simpleName = fname.substring(index + 1);
			} else {
				ref.simpleName = fname;
			}

			ref.dimension = dim;
			
			return ref;
		}
		
		public String getFullName(){
			String typeName = getFullScalarName();
			typeName += this.getArrayDimension();
			
			return typeName;
		}

		/** Check if this type is 'void', which is not a real type.*/
		public boolean isVoid() {
			return (moduleName == null || "".equals(moduleName)) && "void".equals(simpleName.toLowerCase());
		}
		
		/**
		 * Get a link path to this type from the given type's doc.
		 * @param typ the doc the link will be put into
		 */
		public String getLinkPath(DocModel.Type typ, String suffix){
			// The type ref's name can be either an alias or real name. Ideally we should use
			// the parser to parse a code snippet (int a;) to derive the type part and resolve it
			// against a type resolver. But this is hugely expensive and is the rewards is little.
			// So just apply the special treatment for aliased types here.
			String tgtMod = null;
			String simpleName = null;
			String fname = this.getFullScalarName();
			switch(fname){
			case "int":    simpleName = IntType.getInstance().getName(); break;
			case "bool":   simpleName = BoolType.getInstance().getName(); break;
			case "char":   simpleName = CharType.getInstance().getName(); break;
			case "string": simpleName = JStringType.getInstance().getName(); break;
			case "byte":   simpleName = ByteType.getInstance().getName(); break;
			case "var":    simpleName = AnyType.getInstance().getName(); break;
			case "float":  simpleName = FloatType.getInstance().getName(); break;
			default:       break;
			}
			
			if (simpleName == null) {
				tgtMod = this.moduleName;
				simpleName = this.simpleName;
			}

			StringBuilder result = new StringBuilder();
			int tgtDepth = tgtMod != null && tgtMod.length() > 0 ? 2 : 1;
			if (typ == TutorialPseudoType.INSTANCE) {
				// Special - if we are linking from tutorial, must start from api resource
				result.append("../" + WebsiteResources.api.name() + "/");
				if (tgtMod != null && !"".equals(tgtMod)) {
					result.append(tgtMod + "/");
				}
			} else {
				// Determine the relative path. Since all the modules are flatly laid out, we only
				// have two levels: the built-in types sit in first level, the other the second.
				String srcMod = typ.getModuleName();
				int srcDepth = srcMod != null && srcMod.length() > 0 ? 2 : 1;
				int levelDiff = tgtDepth - srcDepth;
				switch(levelDiff){
				case 0:
					// Same depth
					if (tgtMod != null && !"".equals(tgtMod)) { // && !srcMod.equals(tgtMod)
						result.append("../" + tgtMod + "/");
					}
					break;
				case -1:
					// The source is deeper
					result.append("../");
					// (fall thru)
				case 1:
					// The target is deeper
					if (tgtMod != null && !"".equals(tgtMod)) {
						result.append(tgtMod + "/");
					}
					break;
				}		
			}
			
			result.append(simpleName);
			if (suffix == null) {
				suffix = ".html";
			}
			if (!"".equals(suffix)) {
				result.append(suffix);
			}
			
			return result.toString();
		}
		
		public TypeRef resolve(NamespacePool np, ModuleContext mc){
			String fn = this.getFullScalarName();
			if (!fn.contains(".")) { // Only resolve simple name; partial names are not handled for now.
				TypeInfo ti = mc.getTypeInfo(fn, 0, np);
				if (ti != null) {
					this.moduleName = ti.getModuleName();
					this.simpleName = ti.getSimpleName();
				} else {
					GlobalLogger.get().warn("Cannot resolve type reference: " + fn);
				}
			}
			
			return this;
		}
		
		/**
		 * Get only the scalar part of this type's name. For example, type "A.B[][]" returns "A.B". 
		 */
		String getFullScalarName(){
			String typeName =
				moduleName != null && !"".equals(moduleName) ? 
				moduleName + "." + simpleName : 
				simpleName;
			
			return typeName;
		}

		/**
		 * Get only the dimension part of this type's name. For example, type "A.B[][]" returns "[][]". 
		 */
		public String getArrayDimension() {
			String res = "";
			int dim = dimension;
			while(dim > 0){
				res += "[]";
				dim--;
			}
			return res;
		}
		
		//--------------- Object.equals: moduleName, simpleName, dimension ---------------//

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + dimension;
			result = prime * result + ((moduleName == null) ? 0 : moduleName.hashCode());
			result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeRef other = (TypeRef) obj;
			if (dimension != other.dimension)
				return false;
			if (moduleName == null) {
				if (other.moduleName != null)
					return false;
			} else if (!moduleName.equals(other.moduleName))
				return false;
			if (simpleName == null) {
				if (other.simpleName != null)
					return false;
			} else if (!simpleName.equals(other.simpleName))
				return false;
			return true;
		}
		
		@Override
		public String toString(){
			return getFullName();
		}
	}
	
	public abstract static class Documented {
		
		// [nodoc]
		private static final Pair<String, String> NO_DOC = new Pair<String, String>(Keys.NO_DOC, "");

		private static final String NEW_LINE = "<br>";
		private static final String NEW_LINES = NEW_LINE + NEW_LINE;
		
		/** The simple name of the documented element. */
		public String name;
		/** The accessibility (public/private/etc.) */
		public Accessibility visibility;
		/** The main description of this element. */
		protected String summary;
		/** The types the documentation also suggests users to read. */
		public List<TypeRef> references = new ArrayList<TypeRef>();

		//---------------- Transitional Storage ----------------//
		private StringBuilder currentSummary;
		private StringBuilder currentEntry;
		private Map<Pair<String, String>, StringBuilder> map = new HashMap<Pair<String, String>, StringBuilder>();
		private boolean nodoc;
		private boolean newSection;
		private boolean codeMode;
		
		public boolean isDocDisabled(){
			return nodoc;
		}
		
		public String getSummary() {
			return summary;
		}

		/**
		 * Process the documentation block for this syntax element. The input is a string enclosed by
		 * &#47;* and *&#47;
		 */
		public void processRawDoc(String raw){
			String[] lines = raw.split("\n");
			int count = lines.length;
			String line0 = lines[0];
			if (count == 1) {
				// Remove the leading '/' and the trailing '*/', so that the line starts with a '*'
				String line = line0.substring(1, line0.length() - 3); 
				processLine(line, true);
			} else {
				boolean lineStartsFromSecond = false;
				
				// Remove the leading '/', so that the line starts with a '*'
				String line = line0.substring(1);
				if (line.trim().equals("*")){
					// This is blank line with /* only, so we can practically ignore it.
					lineStartsFromSecond = true;
				} else {
					processLine(line, true);
				}
				
				// If the first is merely '/*', 
				if (lineStartsFromSecond && lines.length >= 2) {
					processLine(lines[1], true);
				}
				
				// For lines in between, process normally.
				for(int i = 1 + (lineStartsFromSecond ? 1 : 0); i < count - 1; i++) {
					processLine(lines[i], false);
				}

				// Remove the trailing '*/', so that the line looks same as lines in between
				String endingLine = lines[count - 1];
				line = endingLine.substring(0, endingLine.length() - 2);
				if (!line.trim().equals("")){
					processLine(line, false);
				} // otherwise, this is blank line with */ only, so we can practically ignore it.
			}

			if (currentSummary != null) {
				this.summary = currentSummary.toString();
				if (this.summary.endsWith(NEW_LINES)) {
					int len = currentSummary.length();
					this.summary = this.summary.substring(0, len - NEW_LINES.length());
				}
			} else {
				this.summary = "";
			}
			if(map.containsKey(NO_DOC)){
				this.nodoc = true;
			} else {
				processDocEntries(map);
			}
		}
		
		/**
		 * Process a line that has been stripped of the leading "&#47;" if it's the starting line, and 
		 * trailing "*&#47;", if there is any.
		 * <p>
		 * This line will be treated as a doc entry if it's started with [xxx], where xxx is a valid key
		 * as defined in {@link Keys}. Otherwise it's considered a continuation of the previous section,
		 * which is either the summary or a doc entry.
		 * <p>
		 * Once a doc entry is encountered, all the remaining lines will be added as doc entries. If the 
		 * summary somehow starts a line with a valid doc entry header, it will incidentally terminate 
		 * the summary and starts a doc entry with that header. This is a defect of the current 
		 * documentation format and should be avoided.
		 */
		private void processLine(String line, boolean first) {
			int index = line.indexOf('*');
			if (index >= 0) {
				// If there are only spaces/tabs before the first *, remove 
				// all of them. If there is anything else, keep all of them.
				boolean ledBySpaces = true;
				String s = line.substring(0, index);
				for (byte b : s.getBytes()) {
					if (!(b == ' '|| b == '\t')){
						ledBySpaces = false;
						break;
					}
				}
				
				// If the character immediately following first * is space, 
				// remove it too. Otherwise, keep it.
				if (ledBySpaces) {
					int extra = 0;
					int next = index + 1;
					if (next < line.length()){
						char second = line.charAt(index + 1);
						if (second == ' '|| second == '\t'){
							extra = 1;
						}
						
						line = line.substring(index + 1 + extra); // the substring after "* "
					} else {
						// This line contains nothing beyond *
						line = "";
					}
				}
			}
			
			// Remove '\r' since we split over '\n'. This way we don't need to enforce line breaking characters 
			// on Julian files, as long as we always edit them on Windows, Linux or macOS (OS X+)
			if (line.endsWith(" \r") || line.endsWith("\t\r")) {
				line = line.substring(0, line.length() - 2); // Also remove the last space
			} else if (line.endsWith(" ") || line.endsWith("\r") ||  line.endsWith("\t")) {
				line = line.substring(0, line.length() - 1);
			}
			
			String l2 = line.trim();
			
			// Check if this line is for a doc entry
			if (l2.startsWith("[")) {
				index = l2.indexOf("]");
				if (index > 0) {
					int mark = index + 1;
					// [*key: value*] contents
					//  *--header--*
					String header = l2.substring(0, mark).substring(1, mark - 1).trim();
					if (header.length() > 0) { // Header detected
						String contents = "";
						if (mark < l2.length()){
							contents = l2.substring(mark).trim() + ' ';
						}
						
						Pair<String, String> kpair = null;
						int ind = header.indexOf(":");
						if (ind > 0){
							String key = header.substring(0,  ind).trim();
							String value = header.substring(ind + 1).trim();
							if (Keys.ALL.contains(key)){ // Header verified (a key-value header)
								kpair = new Pair<>(key, value);
							} else { // [code: end]
								l2 = CODE_END_TAG;
							}
						} else {
							if (Keys.ALL.contains(header)){ // Header verified (a key-only header)
								kpair = new Pair<>(header, "");
							} else if (!codeMode && Keys.CODE.equals(header)){ // [code]
								appendToCurrentSection("", "", first);
								codeMode = true;
								l2 = CODE_TAG;
							}
						}
						
						if (kpair != null) {
							currentEntry = new StringBuilder();
							currentEntry.append(contents);

							map.put(kpair, currentEntry);
							return;
						}
					}
				}
			}
			
			// Not an entry header, just continuation from previous line
			appendToCurrentSection(l2, line, l2 == CODE_TAG || l2 == CODE_END_TAG ? true : first);
			
			if (codeMode && l2 == CODE_END_TAG){
				// appendToCurrentSection("", "", true);
				codeMode = false;
			}
		}
		
		private static final String CODE_END_TAG = "[" + Keys.CODE + ": " + Keys.CODE_END + "]";
		private static final String CODE_TAG = "[" + Keys.CODE + "]";
		
		/** Add a new line to the current section, which might be summary of a doc entry. */
		private void appendToCurrentSection(String trimmedLine, String rawLine, boolean first){
			// Not an entry header, just continuation from previous line
			if (currentEntry != null) {
				// From previous entry
				appendNewLine(currentEntry, trimmedLine, rawLine, first);
			} else {
				// Start or continuation of the summary
				if (currentSummary == null) {
					currentSummary = new StringBuilder();
				}
				
				appendNewLine(currentSummary, trimmedLine, rawLine, first);
			}
		}
		
		/** Add a new line to the string builder which represents the current section. */
		private void appendNewLine(StringBuilder sb, String trimmedLine, String rawLine, boolean first){
			if ("".equals(trimmedLine)) {
				// An empty line is treated as paragraph separator
				newSection = true;
				if (!first) {
					sb.append(codeMode ? "\n\n" : NEW_LINES);
				}
			} else {
				// Otherwise, concatenate to the previous line, separated by a space, 
				// unless it's in code mode, where the line breaks should be preserved.
				if (!first && !newSection) {
					sb.append(codeMode ? "\n" : " ");
				}
				sb.append(rawLine);
				newSection = false;
			}
		}

		/**
		 * Process formatted entries found in the documentation block. These entries take the format of
		 * <pre><code>[key: value] contents</code></pre>or<pre><code>[key] contents</code></pre>
		 * where contents may span over multiple lines, as in<pre><code>[key: value] contents line 1
		 *contents line 2
		 *contents line 3
		 * </code></pre>
		 * 
		 * @param map a map with key being the [key: value] pair and the value a string builder that 
		 * contains all the contents for this entry.
		 */
		protected abstract void processDocEntries(Map<Pair<String, String>, StringBuilder> map);
		
		//--------------- Object.equals: name ---------------//
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			Documented other = (Documented) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
	}
	
	public abstract static class Type extends Documented {
		/** The subtype (class/interface/etc.) */
		public ClassSubtype subtype;
		private String modName;
		private NamespacePool ns;
		
		protected Type(ClassSubtype stype){
			super();
			subtype = stype;
		}
		
		public ClassSubtype getSubtype(){
			return subtype;
		}

		/**
		 * Set a namespace pool for this type. The pool is derived from the script file where this type is defined.
		 * 
		 * @param ns
		 */
		public void setNamespacePool(NamespacePool ns) {
			this.ns = ns;
		}
		
		/**
		 * Get the namespace pool for this type.
		 */
		public NamespacePool getNamespacePool(){
			return ns;
		}
		
		/**
		 * Set module name for this type. The module of this type is usually known information 
		 * at the site where the type is being processed. But for convenience this redundancy is
		 * sometimes required.
		 * 
		 * @param modName
		 */
		public void setModuleName(String modName){
			this.modName = modName;
		}
		
		/**
		 * Get module name. This method may return null if the module name has not been set. Use
		 * it with caution.
		 */
		public String getModuleName(){
			return modName;
		}
		
		/**
		 * Get qualified name. This method may return partially qualified name if the module name 
		 * has not been set. Use it with caution.
		 */
		public String getFullName(){
			return modName != null && !"".equals(modName) ? modName + "." + name : name;
		}
		
		/**
		 * Whether the type is built-in. A built-in type doesn't have module name.
		 */
		public abstract boolean isBuiltIn();
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			Set<Pair<String, String>> keys = map.keySet();
			
			for(Pair<String, String> key : keys){
				String k = key.getFirst(); 
				switch(k){
				case Keys.SEE:
				case Keys.SEE_ALSO:
					String v = key.getSecond();
					TypeRef ref = TypeRef.makeFromFullName(v);				
					references.add(ref);
				}
			}
		}
		
		@Override
		public String toString(){
			return getFullName();
		}
	}
	
	public static class TypeDescription {
		/** A name for this entry, its meaning up to the entry. */
		public String name;
		/** The main description of this parameter. */
		public String summary;
		/** The type of this parameter. */
		public TypeRef type;
		
		public TypeDescription(String name, String summary, String typeFullName) {
			this.name = name;
			this.summary = summary;
			this.type = TypeRef.makeFromFullName(typeFullName);
		}

		//--------------- Object.equals: type ---------------//
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			TypeDescription other = (TypeDescription) obj;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}
	}
	
	public interface IMemberTypeDecorator {
		/**
		 * Decorate the raw string representing the name of member's type, which in case of method is the retur type.
		 * @param raw the raw string, which is a type name in either full or simple form.
		 * @param tref the type reference corresponding to the raw type name.
		 * @return A decorated string built on top of the raw input.
		 */
		String decMemberType(String raw, TypeRef tref);
	}
	
	public interface ICoreSignatureDecorator {
		/**
		 * Decorate the raw string representing the name of a param's type
		 * @param raw the raw string, which is a type name in either full or simple form.
		 * @param tref the type reference corresponding to the raw type name.
		 * @return A decorated string built on top of the raw input.
		 */
		String decParamType(String raw, TypeRef tref);
		
		/**
		 * Decorate the raw string representing the member's name.
		 * @param raw the raw string, which is the member's name.
		 * @return A decorated string built on top of the raw input.
		 */
		String decMemberName(String raw);
	}
	
	public static enum AnchorType {
		Member,
		Parameter,
		Return
	}
	
	public abstract static class Member extends Documented {
		/** If the member is static */
		public boolean isStatic;
		
		// [nodoc]
		protected MemberType mtype;

		protected Member(){
			super();
		}
		
		protected String simplifyTypeName(String name){
			switch(name.toLowerCase()){
			case "integer": return "int"; 
			case "byte": return "byte"; 
			case "float": return "float";  
			case "bool": return "bool";   
			case "char": return "char";    
			case "void": return "void";   
			case "any": return "var";
			default:
			}
			
			return name;
		}
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			Set<Pair<String, String>> keys = map.keySet();
			
			for(Pair<String, String> key : keys){
				String k = key.getFirst(); 
				switch(k){
				case Keys.SEE:
				case Keys.SEE_ALSO:
					String v = key.getSecond();
					TypeRef ref = TypeRef.makeFromFullName(v);				
					references.add(ref);
				}
			}
		}
		
		//--------------- Object.equals: isStatic, mtype ---------------//
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + (isStatic ? 1231 : 1237);
			result = prime * result + ((mtype == null) ? 0 : mtype.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			Member other = (Member) obj;
			if (isStatic != other.isStatic)
				return false;
			if (mtype != other.mtype)
				return false;
			return true;
		}
		
		/**
		 * Get the anchor's name for this member.
		 */
		public String getAnchorName(){
			return 	getAnchorName(AnchorType.Member, "");
		}
		
		/**
		 * Get the anchor's name for this member or a certain part (return/parameter) of this member.
		 */
		public String getAnchorName(AnchorType anchorTyp, String extra){
			StringBuilder sb = new StringBuilder();
			switch(this.mtype){
			case CONSTRUCTOR:
				sb.append("c-");
				break;
			case FIELD:
				sb.append("f-");
				break;
			case INITIALIZER:
				break;
			case METHOD:
				sb.append("m-");
				break;
			case STATIC_CONSTRUCTOR:
				break;
			default:
				break;
			}
			
			sb.append(this.getCoreSignature(true, "-", null));
			switch(anchorTyp){
			case Member:
				break;
			case Parameter:
				sb.append("-p-");
				sb.append(extra);
				break;
			case Return:
				sb.append("-r");
				break;
			default:
				break;
			}
			
			return sb.toString();
		}
		
		/**
		 * Get an extended signature for this member, which contains information such as 
		 * visibility, scope, abstractness, return type and the core signature (name and parameters).
		 */
		public String getExtendedSignature(){
			StringBuilder sb = new StringBuilder();
			sb.append(getMemberHeader(false));
			sb.append(getMemberType(false, null));
			sb.append(getCoreSignature(false, null, null));
			return sb.toString();
		}

		/**
		 * Get the header of a member, which contains information such as visibility, scope, abstractness and mutability.
		 * @param useSimpleTypeName
		 * @param decorator
		 * @return
		 */
		public abstract String getMemberHeader(boolean useSimpleTypeName);
		
		/**
		 * Get the type (for field) or return type (for method).
		 * @param useSimpleTypeName
		 * @param decorator
		 * @return
		 */
		public abstract String getMemberType(boolean useSimpleTypeName, IMemberTypeDecorator decorator);
		
		/**
		 * Get the core signature for this member, which contains name and parameters. The signature
		 * is output in the default format, unless a linkage string is provided, so that all the individual
		 * members will be concatenated by the linkage string instead. 
		 * 
		 * @param useSimpleTypeName If true, use simple name for each parameter; false to use full type name.
		 * @param linkage Optional. If not-null, the linkage string will be used to concatenate all the individual parts,
		 * including name and each parameter; otherwise, the signature will be output in default format.
		 * @param decorator Optional. If not-null, its functional method will be called to output a customized
		 * string out of the original type name (which is either simple or full depending on <code>useSimpleTypeName</code>).
		 */
		public abstract String getCoreSignature(boolean useSimpleTypeName, String linkage, ICoreSignatureDecorator decorator);
		
		@Override
		public String toString(){
			return "(" + this.mtype.name() + ") " + name;
		}
	}
	
	public static class Field extends Member {
		/** The type of field. */
		public TypeRef type;
		public boolean isConst;

		public Field(){
			mtype = MemberType.FIELD;
		}
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map) {
			// Nothing special for fields
		}

		//----- Signatures derived from model information -----//
		//IMemberTypeDecorator ICoreSignatureDecorator
		@Override
		public String getMemberHeader(boolean useSimpleTypeName) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.visibility.toString());
			sb.append(" ");
			if (this.isStatic) {
				sb.append("static");
				sb.append(" ");
			}
			if (this.isConst) {
				sb.append("const");
				sb.append(" ");
			}
			
			return sb.toString();
		}
		
		@Override
		public String getMemberType(boolean useSimpleTypeName, IMemberTypeDecorator decorator){
			String out = simplifyTypeName(useSimpleTypeName ? this.type.simpleName : this.type.getFullName());
			if (decorator != null) {
				out = decorator.decMemberType(out, this.type);
			}
			return out + " ";
		}

		@Override
		public String getCoreSignature(boolean useSimpleTypeName, String linkage, ICoreSignatureDecorator decorator) {
			return this.name; // decorator != null ? decorator.decMemberName(this.name) : 
		}
	}
	
	public static class Constructor extends Member {
		/** Parameters. */
		public List<TypeDescription> params;
		/** Exceptions that can be thrown. */
		public List<TypeDescription> exceptions;
		
		public Constructor(String name){
			this.name = name;
			params = new ArrayList<TypeDescription>();
			exceptions = new ArrayList<TypeDescription>();
			mtype = MemberType.CONSTRUCTOR;
		}
		
		// throws
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map) {
			super.processDocEntries(map);
			
			Set<Pair<String, String>> keys = map.keySet();
			for(Pair<String, String> key : keys){
				String k = key.getFirst(); 
				switch(k){
				case Keys.THROWS:
					String v = key.getSecond();
					TypeDescription tdef = new TypeDescription("", map.get(key).toString(), v);
					if (exceptions == null) {
						exceptions = new ArrayList<TypeDescription>();
					}
				
					exceptions.add(tdef);
					break;
				case Keys.PARAM:
					String name = key.getSecond();
					for(TypeDescription td : params){ // Inefficient, but OK for a small total
						if (name.equals(td.name)){
							td.summary = map.get(key).toString();
						}
					}
					break;
				}
			}
		}
		
		//----- Signatures derived from model information -----//

		@Override
		public String getMemberHeader(boolean useSimpleTypeName) {
			StringBuilder sb = new StringBuilder();
			sb.append(this.visibility.toString());
			sb.append(" ");
			if (this.isStatic) {
				sb.append("static");
				sb.append(" ");
			}
			sb.append(getMethodSpecifics());
			
			return sb.toString();
		}
		
		@Override
		public String getMemberType(boolean useSimpleTypeName, IMemberTypeDecorator decorator){
			return "";
		}

		@Override
		public String getCoreSignature(boolean useSimpleTypeName, String linkage, ICoreSignatureDecorator decorator) {
			String pre = linkage == null ? "(" :linkage;
			String infix = linkage == null ? ", " : "-";
			
			StringBuilder sb = new StringBuilder();
			List<TypeDescription> params = this.params;
			int total = params.size();
			int last = total - 1;
			sb.append(this.name + pre);
			for(int i = 0; i < total; i++){
				TypeDescription param = params.get(i);
				String ptname = simplifyTypeName(useSimpleTypeName ? param.type.simpleName : param.type.getFullName());
				if (decorator != null) {
					sb.append(decorator.decParamType(ptname, param.type));
					if (linkage == null) {
						// If decorator is provided and the linkage is default, also output parameter name
						sb.append(" ");
						sb.append(decorator.decMemberName(param.name));
					}
				} else {
					sb.append(ptname);
				}
				if (i < last) { // Not the last one
					sb.append(infix);
				}
			}

			// For the ending, if a linkage is provided but there are no parameters, use 'void'.
			// This way, a nullary method fun will have core signature = "fun-void", while in default 
			// format it would be "fun()".
			String post = linkage == null ? ")" : total > 0 ? "" : "void";
			sb.append(post);
			return sb.toString();
		}

		/** Extension point for method member */
		protected String getMethodSpecifics() {
			return "";
		}
		
		//--------------- Object.equals: params ---------------//

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = super.hashCode();
			result = prime * result + ((params == null) ? 0 : params.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			Constructor other = (Constructor) obj;
			if (params == null) {
				if (other.params != null)
					return false;
			} else if (!compareParams(other.params))
				return false;
			return true;
		}
		
		private boolean compareParams(List<TypeDescription> params2){
			if(params2 == null) {
				return false;
			}
			
			int size = params.size();
			if (size != params2.size()) {
				return false;
			}
			
			for(int i = 0; i < size; i++){
				TypeDescription td1 = params.get(i);
				TypeDescription td2 = params2.get(i);
				if (!td1.equals(td2)){
					return false;
				}
			}
			
			return true;
		}
	}
	
	public static class Method extends Constructor {
		/** Return type and description */
		public TypeDescription returnType;
		public boolean isAbstract;
		
		/** True if the documentation for this method inherits from a base type */ // [nodoc]
		boolean isInherited;
		
		public Method(){
			super(null); // The name will be set at outside ctor.
			mtype = MemberType.METHOD;
		}
		
		// return, inherited
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map) {
			super.processDocEntries(map);
			StringBuilder sb = map.get(new Pair<>(Keys.RETURN, ""));
			if (returnType != null) {
				returnType.summary = sb != null ? sb.toString() : "";
			} else {
				throw new DocGenException(
					"The documentation extraction for method's return can be only applied on top of an existing type description.");
			}
			
			sb = map.get(new Pair<>(Keys.INHERITED, ""));
			isInherited = sb != null; // The doc for this method is inherited.
		}

		// Since return type is not part of method signature, do not implement Object.equals here.
		
		//----- Signatures derived from model information -----//
		/** More info only applicable to method member */
		@Override
		protected String getMethodSpecifics() {
			return this.isAbstract ?  "abstract " : "";
		}
		
		@Override
		public String getMemberType(boolean useSimpleTypeName, IMemberTypeDecorator decorator){
			TypeRef tref = this.returnType.type;
			String sname = tref.simpleName;
			String tname = simplifyTypeName(sname); // Use simple name, in line with parameters.
			
			if (!tname.equals(sname)) {
				tref = tref.clone();
				tref.simpleName = tname;
			}
			
			if (decorator != null) {
				return decorator.decMemberType(tname, tref) + " ";
			} else {
				return tref.getFullName() + " ";
			}
		}
	}
	
	public static class InterfaceType extends Type {
		/** The interfaces implemented by this type. */
		public List<TypeRef> interfaces;
		/** The methods declared by this type. */
		public List<Method> methods;
		
		boolean isInherited;
		
		protected InterfaceType(ClassSubtype stype){
			super(stype);
			interfaces = new ArrayList<TypeRef>();
			methods = new ArrayList<Method>();
		}
		
		public InterfaceType(){
			this(ClassSubtype.INTERFACE);
		}
		
		@Override
		public boolean isBuiltIn(){
			return false;
		}
	}
	
	public static class ClassType extends InterfaceType {
		/** The parent type of this class. */
		public TypeRef parent;
		/** The constructors to instantiate this class. */
		public List<Constructor> ctors;
		/** The fields declared by this class. */
		public List<Field> fields;
		
		public ClassType(){
			super(ClassSubtype.CLASS);
			ctors = new ArrayList<Constructor>();
			fields = new ArrayList<Field>();
		}
	}
	
	public static class TutorialPseudoType extends ClassType {
		
		public static TutorialPseudoType INSTANCE = new TutorialPseudoType();
		
		private TutorialPseudoType(){
			super();
			this.name = "<Tutorial>";
			this.setModuleName("");
		}
	}
	
	public static class AttributeType extends Type {
		/** The fields declared by this attribute. */
		public List<Field> fields;

		protected AttributeType() {
			super(ClassSubtype.ATTRIBUTE);
			fields = new ArrayList<Field>();
		}
		
		@Override
		public boolean isBuiltIn(){
			return false; // This class is not used for Attribute itself, but any class inheriting Attribute. Attribute itself is a built-in type.
		}
	}
	
	public static class EnumEntry extends Documented {
		/** The ordinal value of this enum entry. */
		public int ordinal;

		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map) {
			// NO-OP
		}

		public String getAnchorName() {
			return "e-" + name;
		}
	}
	
	public static class EnumType extends Type {
		/** The entries declared by this enum. */
		public List<EnumEntry> entries;
		
		protected EnumType() {
			super(ClassSubtype.ENUM);
			entries = new ArrayList<EnumEntry>();
		}
		
		@Override
		public boolean isBuiltIn(){
			return false; // This class is not used for Enum itself, but any class inheriting Enum. Enum itself is a built-in type.
		}
	}
	
	//------------------------ Built-in types ------------------------//
	
	public static class PrimitiveType extends Type {
		/** The language-level alias of this type. */
		public String alias;
		
		public PrimitiveType(AnnotationKVMap attributes) {
			super(null); // No class sub-type
			
			alias = attributes.getString(AnnotationKVMap.Keys.ALIAS);
			name = attributes.getString(AnnotationKVMap.Keys.NAME);
			this.processRawDoc(attributes.getString(AnnotationKVMap.Keys.SUMMARY));
		}
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			// Disable processing for doc entries, which are provided through annotation's properties.
		}
		
		@Override
		public boolean isBuiltIn(){
			return true;
		}
	}
	
	public static class BuiltInClassType extends ClassType {
		/** The language-level alias of this type. */
		public String alias;
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			// Disable processing for doc entries, which are provided through annotation's properties.
		}
		
		public BuiltInClassType(AnnotationKVMap attributes){
			super();
			
			alias = attributes.getString(AnnotationKVMap.Keys.ALIAS);
			name = attributes.getString(AnnotationKVMap.Keys.NAME);
			this.processRawDoc(attributes.getString(AnnotationKVMap.Keys.SUMMARY));
			ConversionHelper.addTypeRefsFromArray(
				attributes.getStringArray(AnnotationKVMap.Keys.REFERENCES),
				this.references);
			ConversionHelper.addTypeRefsFromArray(
				attributes.getStringArray(AnnotationKVMap.Keys.INTERFACES),
				this.interfaces);
		}
		
		@Override
		public boolean isBuiltIn(){
			return true;
		}
	}
	
	public static class BuiltInConstructor extends Constructor {
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			// Disable processing for doc entries, which are provided through annotation's properties.
		}
		
		public BuiltInConstructor(JClassConstructorMember jccm, AnnotationKVMap attributes){
			super(new FQName(jccm.getDefiningType().getName()).getSimpleName());
			
			// Basic info
			this.visibility = jccm.getAccessibility();
			this.processRawDoc(attributes.getString(AnnotationKVMap.Keys.SUMMARY));
			ConversionHelper.addTypeRefsFromArray(
				attributes.getStringArray(AnnotationKVMap.Keys.REFERENCES),
				this.references);
	
			// Parameters
			String[] parray = attributes.getStringArray(AnnotationKVMap.Keys.PARAMS);
			JParameter[] params = jccm.getCtorType().getParams();
			int len = params.length - 1;
			if (parray.length != len) {
				throw new DocGenException(
					"Parameter documentation for a constructor of type '" + jccm.getDefiningType().getName() + 
					"' doesn't contain the exact number of parameters as declared for this constructor.");
			} else {
				for (int i = 0; i < len; i++) {
					JParameter jp = params[i + 1];
					String n = jp.getName();
					String s = parray[i];
					String tname = ConversionHelper.convertTypeName(jp.getType());
					TypeDescription td = new TypeDescription(n, s, tname);
					this.params.add(td);
				}				
			}
			
			// Exceptions
			com.github.javaparser.utils.Pair<String, String>[] exs = attributes.getStringPairArray(AnnotationKVMap.Keys.EXCEPTIONS);			
			for (com.github.javaparser.utils.Pair<String, String> ex : exs){
				TypeDescription td = new TypeDescription("", ex.b, ex.a);
				this.exceptions.add(td);
			}
		}
	}
	
	public static class BuiltInMethod extends Method {
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			// Disable processing for doc entries, which are provided through annotation's properties.
		}
		
		public BuiltInMethod(String name, JClassMethodMember jcmm, AnnotationKVMap attributes){
			super();
			JMethodType mtyp = jcmm.getMethodType();
			
			// Basic info
			this.name = name;
			this.visibility = jcmm.getAccessibility();
			this.isStatic = jcmm.isStatic();
			this.isAbstract = jcmm.isAbstract();
			this.processRawDoc(attributes.getString(AnnotationKVMap.Keys.SUMMARY));
			ConversionHelper.addTypeRefsFromArray(
				attributes.getStringArray(AnnotationKVMap.Keys.REFERENCES),
				this.references);
			
			// Return
			this.returnType = new TypeDescription(
				"", attributes.getString(AnnotationKVMap.Keys.RETURNS), ConversionHelper.convertTypeName(mtyp.getReturnType()));
			
			// Parameters
			String[] parray = attributes.getStringArray(AnnotationKVMap.Keys.PARAMS);
			JParameter[] params = mtyp.getParams();
			int offset = isStatic ? 0 : 1;
			int len = params.length - offset;
			if (parray.length != len) {
				if (jcmm.getDefiningType().getName().equals(JFunctionType.FQNAME.toString()) && 
					JFunctionType.MethodName_invoke.equals(name)) { 
					// Special case - Function.invoke declares 0 param, but the doc contains 1 entry.
					TypeDescription td = new TypeDescription("args", parray[0], "Any[]");
					this.params.add(td);
				} else {
					throw new DocGenException(
							"Parameter documentation for method '" + name + 
							"' doesn't contain the exact number of parameters as declared for this method.");
				}
			} else {
				for (int i = 0; i < len; i++) {
					JParameter jp = params[i + offset];
					String n = jp.getName();
					String s = parray[i];
					String tname = ConversionHelper.convertTypeName(jp.getType());
					TypeDescription td = new TypeDescription(n, s, tname);
					this.params.add(td);
				}				
			}
			
			// Exceptions
			com.github.javaparser.utils.Pair<String, String>[] exs = attributes.getStringPairArray(AnnotationKVMap.Keys.EXCEPTIONS);			
			for (com.github.javaparser.utils.Pair<String, String> ex : exs){
				TypeDescription td = new TypeDescription("", ex.b, ex.a);
				this.exceptions.add(td);
			}
		}
	}
	
	public static class BuiltInField extends Field {
		
		@Override
		protected void processDocEntries(Map<Pair<String, String>, StringBuilder> map){
			// Disable processing for doc entries, which are provided through annotation's properties.
		}
		
		public BuiltInField(String name, JClassFieldMember jcmm, AnnotationKVMap attributes){
			super();
			
			// Basic info
			this.name = name;
			this.visibility = jcmm.getAccessibility();
			this.isConst = jcmm.isConst();
			this.isStatic = jcmm.isStatic();
			this.processRawDoc(attributes.getString(AnnotationKVMap.Keys.SUMMARY));
			ConversionHelper.addTypeRefsFromArray(
				attributes.getStringArray(AnnotationKVMap.Keys.REFERENCES),
				this.references);
			
			// Type
			this.type = TypeRef.makeFromFullName(ConversionHelper.convertTypeName(jcmm.getType()));
		}
	}
	
	static class ConversionHelper {
		
		static void addTypeRefsFromArray(String[] arr, List<TypeRef> list){
			for(String s : arr){
				list.add(TypeRef.makeFromFullName(s));
			}
		}
		
		/** Convert from [[T]] to T[][] */
		static String convertTypeName(JType typ){
			if (typ == null) {
				return "Any";
			}
			
			String org = typ.getName();
			int i = 0;
			while(org.startsWith("[", i)){
				i++;
			}
			
			if (i > 0) {
				org = org.substring(i, org.length() - i);
				while(i > 0){
					org += "[]";
					i--;
				}
			}
			
			return org;
		}
	}
}
