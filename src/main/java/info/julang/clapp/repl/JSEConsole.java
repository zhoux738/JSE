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

package info.julang.clapp.repl;

import java.io.IOException;
import java.util.NoSuchElementException;

import info.julang.VersionUtility;
import info.julang.clapp.CLParsingException;
import info.julang.clapp.repl.os.ANSIConsole;
import info.julang.clapp.repl.os.CmdConsole;
import info.julang.clapp.repl.os.FontColor;
import info.julang.clapp.repl.pparser.BlockPreparser;
import info.julang.clapp.repl.pparser.RegularPreparser;
import info.julang.external.JulianScriptEngine;
import info.julang.external.exceptions.JSEException;
import info.julang.util.OSTool;

/**
 * The Julian scripting engine console to run scripts in an interactive REPL mode.
 * 
 * @author Ming Zhou
 */
public class JSEConsole implements IConsole {

	private JulianScriptEngine engine;
	private IPreparser pparser;
	private MetaCommandProcessor mcp;
	private IShellConsole shConsole;
	private ConsoleSession session;
	private boolean posix;

	public JSEConsole(JulianScriptEngine engine, boolean supportAnsiEscapeSeq){
		this.engine = engine;
		pparser = RegularPreparser.INSTANCE;
		session = new ConsoleSession();
		posix = !OSTool.isWindows();
		shConsole = supportAnsiEscapeSeq || posix ? new ANSIConsole(posix, session) : new CmdConsole();
		BlockPreparser.INSTANCE.setShellConsole(shConsole);
	}
	
	public void run(){
		// Print version info 
		String info = getVersionInfo();
		println(info);
		
		while(true){
			// Print prompt
			pparser.prompt(this);
			
			// Read line
			String input = null;
			
			try {
				// The following call is blocking, but can abort abruptly if the JVM is killed.
				input = readln();
			} catch (NoSuchElementException | IllegalStateException ex){
				// These two exceptions normally suggest a SIGKILL or other kinds of forced shutdown.
				break;
			} catch (Exception ex){
				// Otherwise, it's necessary to report this to user.
				errorln("Interactive console is aborted due to an error: " + ex.getMessage());
				break;			
			}
			
			// Pre-parse
			String processed = null;
			
			if (input != null) {
				try {
					if (input.trim().startsWith(".")){
						if (mcp == null) {
							mcp = MetaCommandProcessor.getInstance(session, posix); 
						}
						
						mcp.runCommand(input, this, engine);
					} else {
						processed = pparser.parse(this, input);
					}
				} catch (CLParsingException e) {
					errorln(e.getMessage());
				}
			}
			 
			if (processed == null){
				// If null, go to the next iteration; otherwise, send to the engine
				continue;
			} else {
				try {
					engine.runScript(processed, new String[0]);
				} catch (JSEException e) {
					errorln(e.getMessage());
				}
			}
		}
	}

	//------------------------ IConsole -----------------------//
	
	@Override
	public void setPreparser(IPreparser parser) {
		pparser = parser;
	}

	@Override
	public void println(String str){
		shConsole.print(str);
		shConsole.newLine();
	}
	
	//------------------------ IPrinter -----------------------//

	@Override
	public void input(char c) {
		try {
			shConsole.input(c);
		} catch (IOException e) {
			// Ignore
		}
	}
	
	@Override
	public void print(String str){
		shConsole.print(str);
	}

	@Override
	public void errorln(String s) {
		setColor(FontColor.RED);
		shConsole.errorln(s);
		setColor(FontColor.DEFAULT);
	}

	//---------------- Platform shell dependent ---------------//

	@Override
	public void setColor(FontColor color) {
		shConsole.setColor(color);
	}
	
	@Override
	public String readln(){
		return shConsole.readln();
	}

	@Override
	public void clear() {
		shConsole.clear();
	}

	@Override
	public void onExit() {
		shConsole.onExit();
	}

	@Override
	public void newLine() {
		shConsole.newLine();
	}

	@Override
	public void input(String str) {
		shConsole.input(str);
	}
	
	//-------------------- Private Members --------------------//
	
	private String getVersionInfo(){
		StringBuilder sb = new StringBuilder("Julian ");
		sb.append(VersionUtility.getVersion());
		sb.append(" (Java: ");
		sb.append(System.getProperty("java.version"));
		sb.append(")");
		sb.append(System.lineSeparator());
		sb.append("Dedicated to Julia");
		sb.append(System.lineSeparator());
		sb.append("Type \".help\" to list available meta-commands.");
		return sb.toString();
	}
}
