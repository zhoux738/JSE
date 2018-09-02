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

package info.julang.interpretation;

import info.julang.execution.Argument;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.parser.AstInfo;

/**
 * The executable wrapping a (global) function in an unstructured script file.
 * 
 * @author Ming Zhou
 */
public class GlobalFunctionExecutable extends InterpretedExecutable {

	private NamespacePool nsPool;
	
	/**
	 * Create an executable wrapping a function script.
	 * <p/>
	 * This script can return from the middle (using <code>return</code> statement). It cannot define new types.
	 * 
	 * @param stream
	 * @return
	 */
	public GlobalFunctionExecutable(AstInfo<ExecutableContext> ec, NamespacePool nsPool){
		super(ec, false, true);
		this.nsPool = nsPool;
	}
	
	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		super.preExecute(runtime, option, args);
		
		runtime.getThreadStack().setNamespacePool(nsPool);
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//
	
	@Override
	public boolean isFromLooseScript() {
		return true;
	}
}
