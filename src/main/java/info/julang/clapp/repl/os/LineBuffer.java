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

import info.julang.clapp.repl.IPrinter;

import java.util.LinkedList;
import java.util.List;

/**
 * The data model for the input line. The class tracks the current input and 
 * the position of cursor, which may not necessarily appear at the end. The 
 * screen will be re-rendered off of the model whenever there is data change.
 * <p>
 * In the current version this class is also responsible for rendering. In 
 * future we may adopt a view-model separation if the logic complexity expands
 * extensively.
 * 
 * @author Ming Zhou
 */
public class LineBuffer {
	
	private List<Character> buffer;
	/** The position of buffer index, 0-based. (absPosBase + pos) = cursor's position on screen */
	private int pos;
	private ITermController term;
	private IPrinter printer;
	private int absPosBase;
	
	public LineBuffer(int absPosBase, ITermController term, IPrinter printer){
		this.absPosBase = absPosBase + 1; // +1 since screen coordinate is 1-based and this field is only used of screen relocation
		buffer = new LinkedList<Character>();
		pos = 0;
		this.term = term;
		this.printer = printer;
	}

	public String reset(boolean getBuffer) {
		String res = null;
		if (getBuffer) {
			StringBuilder sb = new StringBuilder();
			for(char c : buffer){
				sb.append(c);
			}
			
			res = sb.toString();
		}
		
		buffer.clear();
		pos = 0;
		
		return res;
	}
	
	public void replace(String whole){
		// Put the whole string into stdout
		term.setCursor(-1, absPosBase);
		term.clearScreen(ClearingOption.TO_END);
		printer.print(whole);
		
		// Replace the contents in buffer
		buffer.clear();
		int len = whole.length();
		pos = len;
		for(int i = 0; i < len; i++){
			buffer.add(whole.charAt(i));
		}
	}
	
	public void deletePrev(){
		int prevPos = pos - 1;
		if (prevPos >= 0) {
			// remove the previous char
			buffer.remove(prevPos);
			
			// set cursor to the new position
			pos--;
			int newAbsPos = absPosBase + pos;
			term.setCursor(-1, newAbsPos);
			
			// clear screen (from cursor to end)
			term.clearScreen(ClearingOption.TO_END);
			
			// reprint the buffer from current position
			for(int i = pos; i < buffer.size(); i++){
				printChar0(buffer.get(i));
			}
			
			// reset the cursor
			term.setCursor(-1, newAbsPos);
		}
		
		// If we are already at the beginning position, nothing will happen
	}
	
	public void deleteCurr() {
		// remove the current char
		if (pos < buffer.size()) {
			buffer.remove(pos);
			
			int oldPos = absPosBase + pos;
			term.setCursor(-1, oldPos);
			
			// clear screen (from cursor to end)
			term.clearScreen(ClearingOption.TO_END);
			
			// reprint the buffer from current position
			for(int i = pos; i < buffer.size(); i++){
				printChar0(buffer.get(i));
			}
			
			// reset the cursor
			term.setCursor(-1, oldPos);
		}
	}

	public void printChar(char c) {
		if (pos >= buffer.size()) {
			// append to the end of stdout
			printChar0(c);
			
			buffer.add(pos, c);
			pos++;
		} else {
			// clear screen (from cursor to end)
			term.clearScreen(ClearingOption.TO_END);
			
			printChar0(c);
			
			// reprint the buffer from current position
			for(int i = pos; i < buffer.size(); i++){
				printChar0(buffer.get(i));
			}
			
			buffer.add(pos, c);
			pos++;
			
			// reset the cursor
			term.setCursor(-1, absPosBase + pos);
		}
	}
	
	// Print the char and move cursor forward by one
	protected void printChar0(char c) {
		System.out.print(c);
	}

	public void moveCursor(int distance) {
		int newPos = pos + distance;
		if (newPos < 0){
			newPos = 0;
		} else if (newPos > buffer.size()){
			newPos = buffer.size();
		} 

		moveCursorHorizontally(newPos);
	}
	
	public void setCursor(boolean startOrEnd){
		int newPos = startOrEnd ? 0 : buffer.size();
		
		moveCursorHorizontally(newPos);
	}
	
	private void moveCursorHorizontally(int newPos){
		term.setCursor(-1, absPosBase + newPos);
		pos = newPos;
	}
}
