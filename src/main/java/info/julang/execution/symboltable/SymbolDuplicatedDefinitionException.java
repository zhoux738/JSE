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

package info.julang.execution.symboltable;

import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.ModuleInfo;

/**
 * A symbol, such as variable, function, or type name, is defined more than once within current context.
 * 
 * @author Ming Zhou
 */
public class SymbolDuplicatedDefinitionException extends SymbolBindingException {

	private static final long serialVersionUID = -3643175426194682276L;

	public SymbolDuplicatedDefinitionException(String symbol) {
		super("\"" + symbol + "\" is defined more than once in current scope.");
	}
	
	public SymbolDuplicatedDefinitionException(ClassInfo ci, ModuleInfo mi) {
		super("Type \'" + ci.getName() + "\' is defined more than once in module '" + mi.getName() + "'.");
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.DuplicateSymbol;
	}

}
