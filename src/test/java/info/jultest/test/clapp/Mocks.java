package info.jultest.test.clapp;

import java.io.IOException;

import info.julang.clapp.repl.IInputer;
import info.julang.clapp.repl.IPrinter;
import info.julang.clapp.repl.os.ClearingOption;
import info.julang.clapp.repl.os.Direction;
import info.julang.clapp.repl.os.FontColor;
import info.julang.clapp.repl.os.ITermController;
import info.julang.clapp.repl.os.LineBuffer;
import info.julang.util.Pair;
import junit.framework.Assert;

public class Mocks {
	
	private Terminal term;
	private MockTermController controller;
	private MockPrinter printer;
	
	public static Mocks createSuite(int height, int width){
		Mocks mocks = new Mocks();
		mocks.term = new Terminal(height, width);
		mocks.controller = new MockTermController(mocks.term);
		mocks.printer = new MockPrinter(mocks.term);
		return mocks;
	}

	public Terminal getTerminal() {
		return term;
	}

	public ITermController getController() {
		return controller;
	}

	public IPrinter getPrinter() {
		return printer;
	}
}

class MockLineBuffer extends LineBuffer {

	private Terminal term;
	
	public MockLineBuffer(int absPosBase, Terminal term, ITermController ctrl, IPrinter printer) {
		super(absPosBase, ctrl, printer);
		this.term = term;
	}
	
	protected void printChar0(char c) {
		term.writeAndMoveCursor(c);
	}
}


class Coord extends Pair<Integer, Integer> {

	public Coord(int x, int y) {
		super(x, y);
	}
	
	int x(){
		return getFirst();
	}
	
	int y(){
		return getSecond();
	}
}

/**
 * The starting state:
 * <pre>
 * [_................]
 * [.................]
 * [.................]
 * [.................]
 * </pre>
 * After some inputs, reaching to the end of screen
 * <pre>
 * [av...............]
 * [xyuv.............]
 * [12345............]
 * [mnop_............]
 * </pre>
 * If hitting line feed, adding a new line at the bottom, removing the first line
 * <pre>
 * [xyuv.............]
 * [12345............]
 * [mnop.............]
 * [_................]
 * </pre>
 * Of course, cleaning the screen will have the effect to repositioning the cursor to (1,1).
 */
class Terminal {

	int height;
	int width;
	
	private char[][] screen;
	private int startLine; // 0-based. This is physical position that is directly mapped to screen array.
	int cursorX;   // 1-based. This is the logical position, so it's always within range [1, height] 
				   // and must be converted to a physical position that is applicable on screen array.
	int cursorY;   // 1-based. This is the logical position, so it's always within range [1, width] 
				   // and must be converted to a physical position that is applicable on screen array.
	
	Terminal(int height, int width) {
		this.height = height;
		this.width = width;
		screen = new char[height][width];
		reset(true);
	}
	
	char read(int x, int y){
		Coord co = checkLogicalCoords(x, y);
		return screen[co.x()][co.y()];
	}
	
	void write(char c, int x, int y) {
		Coord co = checkLogicalCoords(x, y);
		screen[co.x()][co.y()] = c;
	}
	
	void writeAndMoveCursor(char c) {
		Coord co = checkLogicalCoords(cursorX, cursorY);
		screen[co.x()][co.y()] = c;
		int y = cursorY + 1;
		if (y > width) {
			throw new IllegalArgumentException("y is outside the acceptable range.");
		}
		cursorY = y;
	}

	void newLine() {
		cursorY = cursorY + 1;
		if (cursorY > height) {
			cursorY = height;
			// We have reached the bottom of screen. To accommodate the new line we must scroll the screen. 
			// This is simulated here by emptying the starting line and moving it downward by one line.
			for(int j = 0; j < width; j++){
				screen[startLine][j] = '\0';
			}
			startLine++;
			if (startLine >= height) {
				startLine = 0;
			}
		}
	}
	
	void reset(boolean resetCursor){
		for(int i = 0; i < height; i++){
			for(int j = 0; j < width; j++){
				screen[i][j] = '\0';
			}
		}
		
		startLine = 0;
		
		if (resetCursor) {
			cursorX = 1;
			cursorY = 1;
		}
	}
	
	private Coord checkLogicalCoords(int x, int y){
		if (x < 1 || x > height) {
			throw new IllegalArgumentException("x is outside the acceptable range.");
		}
		if (y < 1 || y > width) {
			throw new IllegalArgumentException("y is outside the acceptable range.");
		}
		
		x = startLine + (x - 1); // convert to real x, offset from the starting line
		if (x >= height) {
			x = x - height; // if overflowing, loop back 
		}
		
		y = y - 1; // convert to real y
		
		return new Coord(x, y);
	}
	
	/**
	 * The character '*' represents cursor.
	 */
	void validateScreen(String[] lines){
		int cx = 0, cy = 0;
		int endLine = startLine > 0 ? startLine - 1 : height > 1 ? height - 1 : -1;
		for (int i = startLine; i != endLine; ){
			String line = lines[i];
			char[] chars = screen[i];
			int k = 0;
			for(int j = 0; j < line.length(); j++, k++) { 
				// j is index on the given array, which can jump if encountering a cursor symbol
				// k is index on screen array, which always increments by 1
				char c = line.charAt(j);
				switch(c) {
				case '*':
					// cursor under a character
					c = line.charAt(j + 1);
					Assert.assertEquals(chars[k], c);
					j += 2;
					Assert.assertTrue("Encountered cursor more than once.", 0 == cx && 0 == cy);
					cx = i + 1; cy = k + 1;
					break;
				case '_':
					// cursor under no character (this is where the next output would go)
					Assert.assertTrue("Encountered cursor more than once.", 0 == cx && 0 == cy);
					cx = i + 1; cy = k + 1;
					c = '\0';
					Assert.assertEquals(chars[k], c);
					break;
				default:
					Assert.assertEquals(chars[k], c);
					break;
				}
			}
			
			// The rest of line which remains empty
			for(;k < chars.length; k++) { 
				Assert.assertEquals('\0', chars[k]);
			}
			
			i++;
			if (i == height) {
				i = height > 1 ? 0 : -1;
			}
		}
	}
}

class MockPrinter implements IPrinter, IInputer {

	private Terminal term;
	
	MockPrinter(Terminal term){
		this.term = term;
	}
	
	@Override
	public void input(char c) throws IOException {
		term.writeAndMoveCursor(c);
	}

	@Override
	public void input(String str) {
		for (byte b : str.getBytes()){
			term.writeAndMoveCursor((char)b);
		}
	}

	@Override
	public void print(String s) {
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			term.writeAndMoveCursor(c);
		}
	}

	@Override
	public void errorln(String s) {
		print(s);
	}
}

class MockTermController implements ITermController {
	
	private Terminal term;
	
	MockTermController(Terminal term){
		this.term = term;
	}
	
	@Override
	public void moveCursor(Direction direction, int distance) {
		switch(direction){
		case UP:
			int nv = term.cursorX - distance;
			if (nv < 1) { nv = 1; } else if (nv > term.height) { nv = term.height; }
			term.cursorX = nv;
			break;
		case DOWN:
			nv = term.cursorX + distance;
			if (nv < 1) { nv = 1; } else if (nv > term.height) { nv = term.height; }
			term.cursorX = nv;
			break;
		case FORWARD:
			nv = term.cursorY - distance;
			if (nv < 1) { nv = 1; } else if (nv > term.width) { nv = term.width; }
			term.cursorY = nv;
			break;
		case BACK:
			nv = term.cursorY + distance;
			if (nv < 1) { nv = 1; } else if (nv > term.width) { nv = term.width; }
			term.cursorY = nv;
			break;
		default:
			break;
		}
	}

	@Override
	public void setCursor(int x, int y) {
		if (x > 0) { // Ignore minus value
			if (x > term.height) { 
				x = term.height; 
			}
			term.cursorX = x;
		}
		
		if (y > 0) { // Ignore minus value
			if (y > term.width) { 
				y = term.width; 
			}
			term.cursorY = y;
		}
	}

	@Override
	public void newLine() {
		term.newLine();
	}

	@Override
	public void clearScreen(ClearingOption option) {
		switch(option){
		case TO_BEGINNIG:
			throw new IllegalArgumentException("ClearingOption.TO_BEGINNIG is not mocked.");
		case TO_END:
			for(int i = term.cursorX - 1; i < term.height; i++){
				for(int j = term.cursorY - 1; j < term.width; j++){
					term.write('\0', i + 1, j + 1);
				}
			}
			break;
		case ALL:
			term.reset(true);
			break;
		case ALL_AND_SCROLL_BUFFER:
			term.reset(true);
			break;
		default:
			break;
		}
	}

	@Override
	public void setColor(FontColor color) {
		// No testing for this.
	}
}
