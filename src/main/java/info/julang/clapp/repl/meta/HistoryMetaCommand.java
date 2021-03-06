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

package info.julang.clapp.repl.meta;

import java.util.List;

import info.julang.clapp.repl.ConsoleSession;
import info.julang.clapp.repl.IConsole;
import info.julang.clapp.repl.MCArg;
import info.julang.clapp.repl.MCProps;
import info.julang.clapp.repl.MetaCommand;
import info.julang.external.JulianScriptEngine;

@MCProps(description="show the history.")
public class HistoryMetaCommand implements MetaCommand {

	@MCArg
	private ConsoleSession cs;
	
	@MCArg(description="the total records to show, from the latest.")
	private int total = -1;

	public HistoryMetaCommand() {
		
	}

	@Override
	public void execute(IConsole console, JulianScriptEngine engine) {
		List<String> history = cs.getHistoryManager().list();
		if (total < 0) {
			total = history.size();
		}
		
		for(int i = total - 1; i >=0; i--){
			console.print(String.format("%3d", i + 1));
			console.print("  ");
			console.println(history.get(i));
		}
	}
	
}
