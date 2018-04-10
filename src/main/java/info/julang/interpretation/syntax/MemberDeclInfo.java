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

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.typesystem.jclass.MemberType;

/**
 * Basic info about a class member.
 * 
 * @author Ming Zhou
 */
public abstract class MemberDeclInfo extends DeclInfo {
	
	public abstract MemberType getMemberType();
	
	private ParsedTypeName typeName;
	
	void setTypeName(ParsedTypeName name){
		this.typeName = name;
	}
	
	public ParsedTypeName getTypeName(){
		return typeName;
	}
	
	void copyTo(MemberDeclInfo other) {
		super.copyTo(other);
		other.typeName = this.typeName;
	}
	
	ParserRuleContext ec;
	
	/**
	 * Set AST for executable body. This can be an Method_bodyContext in case of ctor and method, but
	 * an expression or block in case of lambda.
	 * @param ec
	 */
	void setAST(ParserRuleContext ec){
		this.ec = ec;
	}
	
	public ParserRuleContext getAST(){
		return ec;
	}
	
}
