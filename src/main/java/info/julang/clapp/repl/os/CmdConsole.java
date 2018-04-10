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

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Scanner;

import info.julang.clapp.repl.IShellConsole;

/**
 * The console doesn't support ANSI escaping sequence. This is the case with cmd shell
 * on almost all versions of Windows system.
 * 
 * @author Ming Zhou
 */
public class CmdConsole implements IShellConsole {

	private Scanner scanner = new Scanner(System.in);

	@Override
	public void clear() {
		try {
			ProcessBuilder pb = (new ProcessBuilder()).command("cmd", "/c", "cls").inheritIO();
			Process p = pb.start();
			p.waitFor();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String readln() {
		return scanner.nextLine();
	}

	@Override
	public void onExit() {
		// No-op
	}

	@Override
	public void setColor(FontColor color) {
		// No-op
	}

	@Override
	public void input(char c) throws IOException {
		System.out.print(c);
	}

	@Override
	public void print(String s) {
		System.out.print(s);
	}

	@Override
	public void errorln(String s) {
		System.err.print(s);
	}

	@Override
	public void newLine() {
		System.out.println();
	}

	@Override
	public void input(String str) {
		try {
			Robot r = new Robot();
			int keyCode = -1;
			for(byte b : str.getBytes()){
				switch(b){
				case ' ':
					keyCode = KeyEvent.VK_SPACE;
					break;
				case '\t':
					keyCode = KeyEvent.VK_TAB;
					break;
				}
				
				if (keyCode != -1){
  			        r.keyPress(keyCode);
			        r.keyRelease(keyCode);
				}
			}
		} catch (AWTException e) {
			// Ignore. This can happen if we are in a headless environment.
		}
	}

}
