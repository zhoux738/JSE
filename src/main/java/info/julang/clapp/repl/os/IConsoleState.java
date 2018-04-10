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

/**
 * The core interface to implement a state pattern for identifying the ANSI escape sequence 
 * and convert to the intended input.
 * <p>
 * On a POSIX terminal, ESC is used to lead a character sequence which encodes a command 
 * perceivable by the terminal. For example, "ESC [ A" (three chars) translates to a command
 * to move the cursor up. The default Linux terminal utilizes this and encodes certain keys
 * in such sequences, among which, the UP arrow key to "ESC [ A".
 * <p>
 * The dilemma here is that the cross-platform Java API only supports line-read mode, and will
 * wait until an EOL is received. When the user presses UP key, it gets translated to "ESC [ A"
 * by the terminal, which is then sent to the application's standard input that is configured
 * in line-read mode. The end result is that the escaped input will be not consumed and get 
 * shown on the terminal input (this is due to a different terminal setting, however).
 * <p>
 * This is not a problem on Windows, since its terminal console (cmd and Powershell) is able to
 * capture such keys as UP and interpret its meaning by default. Also, it will not encode
 * the key in escape sequences at all. So the app won't receive the key input in either raw
 * or escaped form.
 * <p>
 * The state transition is as follows:
 * <pre>
 *    +---------------------------------------+
 *    +----------------------+                |
 *    |                (other chars) (other chars/special chars)
 *   \|/                     |                |
 * {@link RegularState} -'ESC'-> {@link EscState} -'['-> {@link CsiState}
 *   /|\    |            /|\   |              |
 *    | (other chars)     |  'ESC'          'ESC' 
 *    +-----+             +----+              |
 *                        +-------------------+</pre>
 *                        
 * <p>
 * The complete standard is available at 
 * <a href="http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-048.pdf">here</a>.
 * 
 * @author Ming Zhou
 */
interface IConsoleState {
	
    static final int KC_EOT             = 3;
    static final int KC_TAB             = 9;
    static final int KC_LF              = 10;
    static final int KC_ERASE           = 21; // Device Control 1, which is assigned to Ctrl-U, used to delete the whole line
    static final int KC_ESC             = 27;
    static final int KC_SPACE           = 32;
    static final int KC_SEMICOLON       = 59;
    static final int KC_0               = 48;
    static final int KC_9               = 57;
    static final int KC_A               = 65;
    static final int KC_B               = 66;
    static final int KC_C               = 67;
    static final int KC_D               = 68;
    static final int KC_F               = 70;
    static final int KC_H               = 72;
    static final int KC_LEFT_BRACKET    = 91;
    static final int KC_TILDE           = 126;
    static final int KC_DEL             = 127;
	
	ControlKey accept(int next) throws IOException;
}

class RegularState implements IConsoleState {
	
	static RegularState INSTANCE;
	
	static void init(StatefulConsole console){
		INSTANCE = new RegularState(console);
	}
	
	private StatefulConsole console;
	
	RegularState(StatefulConsole console){
		this.console = console;
	}
	
	@Override
	public ControlKey accept(int next) throws IOException {
		switch (next) {
		case KC_ESC:
			console.setState(EscState.INSTANCE);
			return null;
		case KC_EOT:
			return ControlKey.KILL;
		case KC_DEL:
			return ControlKey.BACKDELETE;
		case KC_ERASE:
			return ControlKey.ERASE;
		case KC_LF:
			return ControlKey.LINE_BREAK;
		case KC_TAB:
			// Covert to four spaces in straight. Save a real 
			console.input(' ');
			console.input(' ');
			console.input(' ');
			console.input(' ');
			return null;
		default:
			if (next >= KC_SPACE) {
				console.input((char)next);
			}
			return null;
		}
	}
}

class EscState implements IConsoleState {

	static EscState INSTANCE;
	
	static void init(StatefulConsole console){
		INSTANCE = new EscState(console);
	}
	
	private StatefulConsole console;
	
	EscState(StatefulConsole console){
		this.console = console;
	}
	
	@Override
	public ControlKey accept(int next) throws IOException {
		switch (next) {
		case KC_ESC:
			// If repeating ESC, no state change
			return null;
		case KC_EOT:
			console.setState(RegularState.INSTANCE);
			return ControlKey.KILL;
		case KC_DEL:
			console.setState(RegularState.INSTANCE);
			return ControlKey.BACKDELETE;
		case KC_LF:
			console.setState(RegularState.INSTANCE);
			return ControlKey.LINE_BREAK;
		case KC_LEFT_BRACKET:
			console.setState(CsiState.INSTANCE);
			return null;
		default:
			// Unrecognized esc seq, restore to regular state
			console.setState(RegularState.INSTANCE);
			return null;
		}
	}
}

// Csi stands for "Control Sequence Introducer". Most device control commands are encoded in this format (led by "ESC [")
class CsiState implements IConsoleState {
	
	static CsiState INSTANCE;
	
	static void init(StatefulConsole console){
		INSTANCE = new CsiState(console);
	}
	
	private StatefulConsole console;
	
	CsiState(StatefulConsole console){
		this.console = console;
	}

	@Override
	public ControlKey accept(int next) throws IOException {
		switch (next) {
		case KC_ESC:
			// Reset to ESC start
			return rere(EscState.INSTANCE, null);
		case KC_EOT:
			return rere(RegularState.INSTANCE, ControlKey.KILL);
		case KC_DEL:
			return rere(RegularState.INSTANCE, ControlKey.BACKDELETE);
		case KC_TILDE:
			return interpretSeqEndedWithTilde();
		case KC_LF:
			return rere(RegularState.INSTANCE, ControlKey.LINE_BREAK);
		case KC_SEMICOLON:
			if (finishX()) {
				return null;
			} else {
				// double ';'
				return rere(RegularState.INSTANCE, null);
			}
		case KC_A:
			return rere(RegularState.INSTANCE, ControlKey.UP);
		case KC_B:
			return rere(RegularState.INSTANCE, ControlKey.DOWN);
		case KC_C:
			return rere(RegularState.INSTANCE, ControlKey.FORWARD);
		case KC_D:
			return rere(RegularState.INSTANCE, ControlKey.BACKWARD);
		case KC_F:
			return rere(RegularState.INSTANCE, ControlKey.END);
		case KC_H:
			return rere(RegularState.INSTANCE, ControlKey.HOME);
		default:
			if (KC_0 <= next && next <= KC_9){
				addDigit(next);
				return null;
			}
			
			// Unrecognized
			return rere(RegularState.INSTANCE, null);
		}
	}
	
	//------------------------------------ Coordinate parsing ------------------------------------//
	//
	// In ANSI escape sequence, there could be a coordinate section which takes form "n;m", where n 
	// and m are two 1-based numbers. These numbers sometimes represent the coordinates, but are not 
	// limited to this purpose. For example, keyboard modifiers will be represented by a bit value 
	// added to Y in the coordinate while X remains 1.
	// 
	// With
	//   CTRL  - 4
	//   ALT   - 2
	//   SHIFT - 1
	// and
	//   X = 1
	//   Y = ( CTRL | ALT | SHIFT ) + 1
	//
	// So if CTRL is pressed, n;m = 1;5. If ALT + SHIFT is pressed, n;m = 1;4.
	//
	// As of 0.1.11 we don't process these coordinates yet.We only identify and skip them accordingly. 
	
	private int x, y;
	private boolean onY;
	
	private void addDigit(int next) {
		if (onY) {
			y = y*10 + (next - KC_0);
		} else {
			x = x*10 + (next - KC_0);
		}
	}

	private boolean finishX() {
		if (onY) {
			// Do not allow finishing twice
			return false;
		}
		onY = true;
		return true;
	}
	
	/** Reset and return */
	private ControlKey rere(IConsoleState state, ControlKey ck){
		x = 0;
		y = 0;
		onY = false;
		console.setState(state);
		return ck;
	}
	
	//--------------------------------- Sequences interpretation ---------------------------------//

	/*
	 * On Ubuntu, the Delete key is translated to "CSI 3 ~". In the standard, however, 07/14 (the 
	 * bit-table notation for ASCII 126 '~') is never used after a position notation. This needs
	 * more investigation and testing across all main stream POSIX systems. (TODO)
	 */
	private ControlKey interpretSeqEndedWithTilde() {
		if (y == 0){
			switch(x){
			case 3:
				return rere(RegularState.INSTANCE, ControlKey.DELETE);
			}
		}
		
		return rere(RegularState.INSTANCE, null);
	}
}