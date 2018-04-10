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

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;

public abstract class JSEParsingHandlerBase implements ANTLRErrorListener {
	
	@Override
	public void reportAttemptingFullContext(
		Parser arg0, DFA arg1, int arg2, int arg3, BitSet arg4, ATNConfigSet arg5) {
		// NO-OP
	}

	@Override
	public void reportContextSensitivity(
		Parser arg0, DFA arg1, int arg2, int arg3, int arg4, ATNConfigSet arg5) {
		// NO-OP
	}
	
	@Override
	public void reportAmbiguity(
		Parser recognizer, DFA dfa, int startIndex, int stopIndex, 
		boolean exact, BitSet ambigAlts, ATNConfigSet configs){
		if (!exact && excludeAmbiguity(getDecisionRuleName(recognizer, dfa))) {
			return;
		}
		  
		throw new BadSyntaxException("Parser encountered an ambiguity issue.");
	}
	
	private String getDecisionRuleName(Parser recognizer, DFA dfa) {
		int decision = dfa.decision;
		int ruleIndex = dfa.atnStartState.ruleIndex;

		String[] ruleNames = recognizer.getRuleNames();
		if (ruleIndex < 0 || ruleIndex >= ruleNames.length) {
			return String.valueOf(decision);
		}

		return ruleNames[ruleIndex];
	}
	
	private boolean excludeAmbiguity(String name){
		if("array_creation_expression".equals(name) || "expression".equals(name)){
			return true;
		}
		
		return false;
	}
}
