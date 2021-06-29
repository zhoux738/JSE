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

package info.julang.parser;

import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.scanner.ITokenStream;
import info.julang.scanner.TokenStream;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Initially an instance of this class carries only token stream. Whenever any of the methods 
 * exposed on parent class (except {@link #getFileName()}) is called, a full parsing will be 
 * triggered and an AST generated from the stream, barring no errors.
 * <p>
 * The expected usage of this class is to call {@link #getTokenStream()} to get a stream to
 * consume its tokens, and at one point call {@link #getAST()} to demand the AST.
 * 
 * @author Ming Zhou
 */
public class LazyAstInfo extends AstInfo<ProgramContext> {

	private ANTLRParser parser;
	
	public LazyAstInfo(ANTLRParser parser, String fileName, BadSyntaxException bse){
		super(null, fileName, bse);
		this.parser = parser;
	}
	
	/**
	 * Get the backing token stream for this AST.
	 */
	public ITokenStream getTokenStream(){
		return new TokenStream(parser.getTokenStream());
	}
	
	/**
	 * Get syntax exception that was encountered during either scanning (<code>parse = false</code>) or 
	 * scanning plus parsing (<code>parse = true</code>).
	 * 
	 * @param parse if true, will trigger parsing
	 * @return
	 */
	public BadSyntaxException getBadSyntaxException(boolean parse){
		if (parse){
			return getBadSyntaxException();
		} else {
			return bse;
		}
	}
	
	// trigger parsing
	@Override
	public <R extends ParserRuleContext> AstInfo<R> create(R anotherAst){
		parse();
		return super.create(anotherAst);
	}
	
	// trigger parsing
	@Override
	public ProgramContext getAST(){
		parse();
		return ast;
	}
	
	// trigger parsing
	@Override
	public BadSyntaxException getBadSyntaxException(){
		parse();
		return bse;
	}
	
	// trigger parsing
	/**
	 * Get the documentation for a given node, which must be part of this AST.
	 * 
	 * @param prc
	 * @return null if no documentation is found.
	 */
	public String getDoc(ParserRuleContext prc){
		parse();
		return parser.getDoc(prc.getStart());
	}
	
	// Parse the tree to get either AST or exception
	private void parse(){
		if (ast == null && bse == null) {
			parser.parse(true, false);
			AstInfo<ProgramContext> ainfo = parser.getAstInfo();
			ast = ainfo.getAST();
			bse = ainfo.getBadSyntaxException();
		}
	}
}
