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

package info.julang.modulesystem;

import info.julang.langspec.ast.JulianParser.Include_statementContext;
import info.julang.parser.AstInfo;

/**
 * Represent an entry of include statement at the beginning of the script.
 * 
 * @author Ming Zhou
 */
public class IncludedFile {

	public enum ResolutionStrategy {
		
		EXTERNAL_ONLY,
		
		EXTERNAL_THEN_BUILTIN
		
	}
	
	private String path;
	
	private ResolutionStrategy rs;
	
	private AstInfo<Include_statementContext> ast;

	public IncludedFile(ResolutionStrategy rs, String path, AstInfo<Include_statementContext> ast) {
		this.path = path;
		this.rs = rs;
		this.ast = ast;
	}

	public String getFullPath() {
		return path;
	}

	public ResolutionStrategy getResultionStrategy() {
		return rs;
	}

	public AstInfo<Include_statementContext> getAstInfo() {
		return ast;
	}
}
