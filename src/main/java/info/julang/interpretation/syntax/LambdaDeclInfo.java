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

package info.julang.interpretation.syntax;

import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

/**
 * Declaration information about a lambda. {@link #getStartLocation()} returns the position after =>, 
 * where the lambda body starts.
 * <p/>
 * While this class derives from {@link MethodDeclInfo}, it is not a member of class, but more like a variable definition.
 * 
 * @author Ming Zhou
 */
public class LambdaDeclInfo extends MethodDeclInfo {
	
	public static enum LambdaType {
		BLOCK,
		RETURN,
		THROW
	}
	
	private LambdaType ltyp;
	private AstInfo<ExpressionContext> exc;
	private AstInfo<BlockContext> bc;
	
	/**
	 * WThe structure of lambda body: <br>
	 * &nbsp;&nbsp;(1) contained in a pair of curly brackets (<code>{...}</code>), or<br>
	 * &nbsp;&nbsp;(2) in a single <code>return</code> statement, or<br>
	 * &nbsp;&nbsp;(3) in a single <code>throw</code> statement<br>
	 * @return
	 */
	public LambdaType LambdaType(){
		return ltyp;
	}
	
	public void addUntypedParameter(String paramName){
		addParameter(ParsedTypeName.ANY, paramName);
	}
	
	/**
	 * @return always {@link ParsedTypeName#ANY}.
	 */
	@Override
	public ParsedTypeName getReturnTypeName(){
		return ParsedTypeName.ANY;
	}
	
	public void setASTs(AstInfo<ExpressionContext> exc, AstInfo<BlockContext> bc, LambdaType ltyp) {
		this.exc = exc;
		this.bc = bc;
		if (bc != null){
			super.ec = bc.getAST();
		} else {
			super.ec = exc.getAST();
		}

		this.ltyp = ltyp;
	}
	
	public AstInfo<ExpressionContext> getExpressionContext(){
		return exc;
	}
	
	public AstInfo<BlockContext> getBlockContext(){
		return bc;
	}
	
	public LambdaType getLambdaType(){
		return ltyp;
	}
}
