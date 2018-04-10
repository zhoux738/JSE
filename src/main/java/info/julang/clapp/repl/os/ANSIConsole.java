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

package info.julang.clapp.repl.os;

import java.io.IOException;
import java.util.Scanner;

import info.julang.clapp.repl.ConsoleSession;
import info.julang.clapp.repl.HistoryManager;
import info.julang.clapp.repl.IShellConsole;

/**
 * The console supports ANSI escaping sequence. This is the case with Linux, macOS and 
 * other POSIX systems. It's also applicable to Windows Powershell.
 * <p>
 * When running in POSIX, it first sets the inherited terminal into a relatively "raw" mode.
 * Every character input will be handled in a consistent input context, and whether echoing 
 * it out to stdout is left to the discretion of this class. 
 * <p>
 * To emulate the line-reading behavior, the character mapping to the pressed key, should 
 * it be displayable, will be put into stdout, and also stored into a character buffer. When 
 * LINEFEED key is received, the whole buffer will be cleared out.    
 * <p>
 * Special keys, such as UP/DOWN arrows, DEL, and CTRL-C receive special treatment in 
 * alignment of their normal expectations. Note since some of those keys are actually sent
 * to the application in encoded ANSI escape sequences, a state-machine-based processing
 * is introduced to handle the sequences. 
 * <p>
 * This class is implemented with references to 
 * <a href="https://en.wikipedia.org/wiki/ANSI_escape_code">
 * https://en.wikipedia.org/wiki/ANSI_escape_code</a> and 
 * <a href="https://linux.die.net/man/1/stty">Linux STTY man page</a>.
 * 
 * @author Ming Zhou
 */
public class ANSIConsole implements IShellConsole, StatefulConsole {

	private static final int ABS_POS_BASE = 4;
	private static final int STTY_SUCC = 0;
	
	private boolean rawModeSet;
	private IConsoleState state;
	private LineBuffer buffer;
	
	private Scanner scanner;
	private ITermController term;
	private HistoryManager history;
	
	public ANSIConsole(boolean isPosix, ConsoleSession session){
		initialize(isPosix, session);
	}

	//----------- Testability -----------//

	protected ANSIConsole(){
		
	}
	
	protected void initialize(boolean isPosix, ConsoleSession session){
		this.term = getTermController();
		if (isPosix) {
			// Switch to raw mode
			if (setRawMode()){
				rawModeSet = true;
				
				RegularState.init(this);
				EscState.init(this);
				CsiState.init(this);
				
				state = RegularState.INSTANCE;
				buffer = getLineBuffer(ABS_POS_BASE);
				history = session.getHistoryManager();
			} else {
				System.out.println("Couldn't set console into proper mode. Certain hot keys (such as UP/DOWN) are disabled.");
				scanner = new Scanner(System.in);
			}
		}
	}
	
	protected LineBuffer getLineBuffer(int absPosBase) {
		return new LineBuffer(absPosBase, term, this);
	}
	
	protected ITermController getTermController() {
		return new ANSITermController();
	}
	
	protected int getAvailableInput() throws IOException {
		return System.in.available();
	}
	
	protected int readNext() throws IOException {
		return System.in.read();
	}
	
	//---------- IShellConsole ----------//

	@Override
	public void clear() {
		term.clearScreen(ClearingOption.ALL);
	}

	@Override
	public String readln() {
		if (rawModeSet){
			try {
				return readln0();
			} catch (IOException e) {
				errorln("Encountered an exception, input invalidated. Cause: " + e.getMessage());
				buffer.reset(false);
				state = RegularState.INSTANCE;
			}
		} else {
			return scanner.nextLine();
		}
		
		return null;
	}
	
	private String readln0() throws IOException {
		ControlKey ck = null;
		while (true) {
			if (getAvailableInput() != 0) {
				// Read one char
				int c = readNext();
				
				// Delegate to state
				ck = state.accept(c);
				if (ck != null) {
					switch(ck){
					case BACKDELETE:
						buffer.deletePrev();
						break;
					case DELETE:
						buffer.deleteCurr();
						break;
					case UP:
						String hist = history.prev();
						if (hist != null) { buffer.replace(hist); }
						break;
					case DOWN:
						hist = history.next();
						if (hist != null) { buffer.replace(hist); }
						break;
					case FORWARD:
						buffer.moveCursor(1);
						break;
					case BACKWARD:
						buffer.moveCursor(-1);
						break;
					case HOME:
						buffer.setCursor(true);
						break;
					case END:
						buffer.setCursor(false);
						break;
					case ERASE:
						buffer.reset(false);
						buffer.setCursor(true);
						term.clearScreen(ClearingOption.TO_END);
						break;
					case KILL:
						term.newLine();
						restoreDefaultMode();
						System.exit(0);
						break;
					case LINE_BREAK:
						term.newLine();
						
						// Extract from and reset buffer
						String str = buffer.reset(true);
						
						// Add new history record
						if (str != null && !"".equals(str.trim())){
							history.add(str);
						}
						history.reset();
						
						return str;
					default:
						break;
					}
				}
			}
		}
	}

	@Override
	public void onExit() {
		restoreDefaultMode();
	}
	
	@Override
	public void setColor(FontColor color) {
		term.setColor(color);
	}

	@Override
	public void newLine() {
		term.newLine();
	}
	
	//------------------ stty ------------------//
	
	// protected for testing
	protected boolean setRawMode() {
		// 1. complete reading with 1 char
		// 2. disable echoing
		// 3. unset key for sending interrupt signal. CTRL-C won't send SIGKILL anymore
		return callStty(
				"-icanon min 1 " +
				"-echo " +
				"intr ^-") == STTY_SUCC;
	}
	
	private void restoreDefaultMode() {
		callStty("sane");
	}

	private static int callStty(String arg) {
		try {
			arg = "stty " + arg + " < /dev/tty";
			ProcessBuilder pb = (new ProcessBuilder()).command("sh", "-c", arg).inheritIO();
			pb.start().waitFor();
			return STTY_SUCC;
		} catch (InterruptedException e) {
			return -1;
		} catch (IOException e) {
			return -2;
		}
	}

	//---------------- StatefulConsole ----------------//

	@Override
	public void input(char c) throws IOException {
		buffer.printChar(c);
	}

	@Override
	public void input(String str) {
		for(byte b : str.getBytes()){
			buffer.printChar((char)b);
		}
	}
	
	// must not use buffer
	@Override
	public void print(String str) {
		System.out.print(str);
	}
	
	// must not use buffer
	@Override
	public void errorln(String str) {
		System.err.print(str);
	}

	@Override
	public void setState(IConsoleState state) {
		this.state = state;
	}
}
