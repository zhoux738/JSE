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

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;

/**
 * A container object used to carry AST node along with associated information. Essentially a very narrow 
 * implementation of attribute grammar.
 * 
 * @author Ming Zhou
 */
public class AstInfo<T extends ParserRuleContext> implements IHasLocationInfo {

	protected T ast;
	protected BadSyntaxException bse;
	private String fileName;
	
	/**
	 * Create a new instance that carries successfully created AST.
	 * 
	 * @param ast
	 * @param fileName
	 */
	public static <T extends ParserRuleContext> AstInfo<T> succ(T ast, String fileName){
		return new AstInfo<T>(ast, fileName, null);
	}
	
	/**
	 * Create a new instance that carries the exception during AST creation.
	 * 
	 * @param bse the BadSyntaxException encountered when crafting the AST.
	 * @param fileName
	 */
	static <T extends ParserRuleContext> AstInfo<T> fail(BadSyntaxException bse, String fileName){
		return new AstInfo<T>(null, fileName, bse);
	}
	
	protected AstInfo(T ast, String fileName, BadSyntaxException bse){
		this.ast = ast;
		this.fileName = fileName;
		this.bse = bse;
	}
	
	/**
	 * Create another AstInfo object that carries the same set of shared info, including file name and exception.
	 * 
	 * @param anotherAst if null, the returned object will only carry file name and exception info.
	 */
	public <R extends ParserRuleContext> AstInfo<R> create(R anotherAst){
		return new AstInfo<R>(anotherAst, fileName, bse);
	}
	
	/**
	 * @return null if exception is thrown.
	 */
	public T getAST(){
		return ast;
	}
	
	/**
	 * @return null if there is no exception.
	 */
	public BadSyntaxException getBadSyntaxException(){
		return bse;
	}
	
	//------------ IHasLocationInfo ------------//
	
	/**
	 * @return the source script file name. can be null.
	 */
	public String getFileName(){
		return fileName;
	}

	@Override
	public int getLineNumber() {
		if (bse != null) {
			return bse.getLineNumber();
		} else if (ast != null) {
			return ast.start.getLine();
		} else {
			return -1;
		}
	}
}
