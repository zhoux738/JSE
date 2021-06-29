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

package info.julang.modulesystem.prescanning.lazy;

import org.antlr.v4.runtime.Token;

import info.julang.langspec.ast.JulianLexer;
import info.julang.modulesystem.prescanning.IllegalModuleFileException;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.scanner.ITokenStream;

public class ModuleNameReader {

	public static enum ErrorType {
		
		ILLEGAL_MODULE_NAME("The module name must contain legal identifier and be separated by '.'."),
		
		CANNOT_SPECIFY_ALIAS("Illegal place to specify alias for module."),
		
		ILLEGAL_ALIAS("The alias must be a legal identifier."),
		
		NOT_ENDED_WITH_SEMICOLON("The name must be followed by ';'."),
		
		NOT_EMPTY_NAME("The module name must not be empty.");
		
		private String message;
		
		public String getMessage(){
			return message;
		}
		
		private ErrorType(String msg){
			message = msg;
		}
	}
	
	private RawScriptInfo info;
	private ITokenStream stream;
	
	public ModuleNameReader(RawScriptInfo info){
		this.info = info;
	}
	
	private void reportError(ErrorType type) {
		if (stream != null ){
			stream.reset();
		}
		throw new IllegalModuleFileException(info, stream, type.getMessage());
	}
	
	/**
	 * Read the module name into string builder.
	 * 
	 * @param stream Token stream
	 * @param fullName The full name to be built out of the stream input
	 * @param isModuleOrImport whether this method is called for module (true) or import statement (false). 
	 * module supports implicit module naming (module;); import accepts alias definition (AA.BB.CDE as XY).
	 * @return an alias of the module name if available (<code>ABC</code> in "<code>import A.B.C as ABC</code>"), 
	 * or null if the module statement doesn't contain a module name.
	 */
	public String readModuleName(ITokenStream stream, StringBuilder fullName, boolean isModuleOrImport){
		this.stream = stream;
		stream.mark();
		Token tok = null;
		String alias = null;
		boolean first = isModuleOrImport;
		while(true){
			// Read module's name by section
			tok = stream.next();
			if(tok.getType() != JulianLexer.IDENTIFIER){
				if (first && tok.getType() == JulianLexer.SEMICOLON) {
					return null;
				}
				
				reportError(ErrorType.ILLEGAL_MODULE_NAME);
			}
			
			fullName.append(tok.getText());
			if (isModuleOrImport) {
				first = false;
			}
			
			tok = stream.next();
			int type = tok.getType();
			if(type == JulianLexer.DOT){
				fullName.append(tok.getText());
			} else if (type == JulianLexer.SEMICOLON){
				break;
			} else if (type == JulianLexer.AS){
				if (isModuleOrImport) {
					reportError(ErrorType.CANNOT_SPECIFY_ALIAS);
				} else {
					tok = stream.next();
					if(tok.getType() != JulianLexer.IDENTIFIER){
						reportError(ErrorType.ILLEGAL_ALIAS);
					}
					alias = tok.getText();
					
					tok = stream.next();
					if(tok.getType() != JulianLexer.SEMICOLON){
						reportError(ErrorType.NOT_ENDED_WITH_SEMICOLON);
					}
					break;
				}
			} else {
				reportError(ErrorType.ILLEGAL_MODULE_NAME);	
			}
		}
		
		if(fullName.length()==0){
			reportError(ErrorType.NOT_EMPTY_NAME);		
		}
		
		return alias;
	}
}
