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

package info.julang.interpretation.statement;

public class StatementOption {
	
	public void setAllowClassDef(boolean allowClassDef) {
		this.allowClassDef = allowClassDef;
	}
	
	public void setAllowFunctionDef(boolean allowFunctionDef) {
		this.allowFunctionDef = allowFunctionDef;
	}
	
	public void setAllowBreak(boolean allowBreak) {
		this.allowBreak = allowBreak;
	}
	
	public void setAllowContinue(boolean allowContinue) {
		this.allowContinue = allowContinue;
	}
	
	public void setAllowReturn(boolean allowReturn) {
		this.allowReturn = allowReturn;
	}
	
	public void setNoSequential(boolean noSequential) {
		this.noSequential = noSequential;
	}
	
	public void setPreserveStmtResult(boolean preserveStmtResult) {
		this.preserveStmtResult = preserveStmtResult;
	}
	
	public void setIgnoreCase(boolean ignoreCase) {
		this.ignoreCase = ignoreCase;
	}
	
	public void setIgnoreDefault(boolean ignoreDefault) {
		this.ignoreDefault = ignoreDefault;	
	}
	
	public void setAllowEndExprByEOF(boolean allowEndExprByEOF) {
		this.allowEndExprByEOF = allowEndExprByEOF;
	}
	
	/**
	 * Create an option inheriting from the given one. 
	 * Only three properties, {@link #allowBreak}, {@link #allowContinue}, and {@link #allowReturn} will be inherited. 
	 * Other properties are always set to false.
	 * 
	 * @param parentOption
	 * @return
	 */
	public static StatementOption createInheritedOption(StatementOption parentOption){
		StatementOption nestedOption = new StatementOption();
		nestedOption.setAllowBreak(parentOption.allowBreak);
		nestedOption.setAllowContinue(parentOption.allowContinue);
		nestedOption.setAllowReturn(parentOption.allowReturn);
		return nestedOption;
	}
	
	/**
	 * The block can contain class definition statement.
	 */
	boolean allowClassDef;
	
	/**
	 * The block can contain function definition statement.
	 */
	boolean allowFunctionDef;
	
	/**
	 * The block can contain break statement.
	 */
	boolean allowBreak;
	
	/**
	 * The block can contain continue statement.
	 */
	boolean allowContinue;
	
	/**
	 * The block can contain return statement.
	 */
	boolean allowReturn;
	
	/**
	 * The block contains only one statement.
	 */
	boolean noSequential;

	/**
	 * Instead of only preserving result from <code>return</code> statement, the block 
	 * should preserve the result for any statement that implements {@link IHasResult}.
	 */
	boolean preserveStmtResult;
	
	/**
	 * Ignore all the case labels (in the form of "case ...:") in this block.
	 */
	boolean ignoreCase;

	/**
	 * Ignore all the default labels (in the form of "default:") in this block.
	 */
	boolean ignoreDefault;

	/**
	 * Allow ending an expression by EOF.
	 */
	boolean allowEndExprByEOF;

}
