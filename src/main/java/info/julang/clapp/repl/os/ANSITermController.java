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

/**
 * A terminal controller for systems where ANSI escape sequences are supported. 
 * <p>
 * Note wherever a coordinate is used we are referring to a 1-based Display Coordinate System.
 * <pre>
 *    1 2 3 4 . N 
 *  1 + + + + . +
 *  2 + +[_]+ . + 
 *  3 + + + + . + 
 *  4 + + + + . +  
 *  . . . . . . + 
 *  M + + + + + +</pre>
 * The cursor is at (2,3) in the display with resolution M * N above.
 * <p>
 * @author Ming Zhou
 */
public class ANSITermController implements ITermController {

	@Override
	public void moveCursor(Direction direction, int distance) {
		String seq = "\033[" + distance + direction.symbol();
		System.out.print(seq);
	}

	@Override
	public void clearScreen(ClearingOption option) {
		String seq = "";
		if (option.ordinal() >= ClearingOption.ALL.ordinal()){
			// If clearing the entire screen, reposition the cursor to (1,1) of screen. 
			seq += "\033[H";
		}
		seq += "\033[" + option.value() + 'J';
		System.out.print(seq);
	}

	@Override
	public void setCursor(int x, int y) {
		String seq = null;
		if (x <= 0) {
			// Move horizontally only
			seq = "\033[" + y + "G";
		} else {
			seq = "\033[" + x + ";" + y + "H";
		}

		System.out.print(seq);
	}

	@Override
	public void newLine() {
		System.out.println();
	}

	@Override
	public void setColor(FontColor color) {
		switch(color){
		case DEFAULT:
			System.out.print("\033[0m");
			break;
		case RED:
			System.out.print("\033[31m");
			break;
		case YELLOW:
			System.out.print("\033[33m");
			break;
		default:
			break;
		}
	}

}
