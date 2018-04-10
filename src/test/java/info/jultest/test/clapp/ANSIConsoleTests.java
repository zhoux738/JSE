package info.jultest.test.clapp;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import info.julang.clapp.repl.ConsoleSession;
import info.julang.clapp.repl.os.ANSIConsole;
import info.julang.clapp.repl.os.ITermController;
import info.julang.clapp.repl.os.LineBuffer;

public class ANSIConsoleTests {

	/*
	 * Usage: call {@link #setLineInput(String)} to set a character sequences to simulate console input,
	 * which may span over more than one line. Then call {@link #readln()} to drive the processing of 
	 * the input. Be careful: {@link #readln()} can throw if it couldn't see line break before it reaches 
	 * the end of input.
	 */
	static class MockANSIConsole extends ANSIConsole {

		private ITermController mockTermCtrl;
		private Terminal term;
		private byte[] bytes;
		private int index = 0;
		
		public MockANSIConsole(ConsoleSession session, Mocks mocks) {
			super();
			
			mockTermCtrl = mocks.getController();
			term = mocks.getTerminal();
			
			// Must happen after setting mock objects, as it will call the protected methods which become ready only at this point.
			initialize(true, session);
		}
		
		@Override
		protected LineBuffer getLineBuffer(int absPosBase) {
			return new MockLineBuffer(absPosBase, term, mockTermCtrl, this);
		}

		@Override
		protected ITermController getTermController() {
			return mockTermCtrl;
		}

		@Override
		protected int getAvailableInput() throws IOException {
			if (bytes != null && index < bytes.length){
				return 1;
			} else {
				throw new IllegalStateException("No more input available!");
			}
		}

		@Override
		protected int readNext() throws IOException {
			byte b = bytes[index];
			index++;
			return b;
		}

		@Override
		protected boolean setRawMode() {
			return true;
		}
		
		public void setLineInput(String str){
			bytes = str.getBytes();
			index = 0;
		}
	}
	
	@Test
	public void basicInputTest(){
		Mocks m = Mocks.createSuite(3, 7);
		ConsoleSession session = new ConsoleSession();
		MockANSIConsole console = new MockANSIConsole(session, m);
		
		console.setLineInput("abc\n\n    \n");
		
		validateReadLn(console, new String[] { 
			"abc",
			"",
			"    "
		});
	}
	
	@Test
	public void controlInputTest(){
		Mocks m = Mocks.createSuite(4, 8);
		ConsoleSession session = new ConsoleSession();
		MockANSIConsole console = new MockANSIConsole(session, m);
		
		console.setLineInput("\t\nabc\025def\n\033[Aghi\nxyz\177\n");
		                   //0  //1         //2        //3
		
		validateReadLn(console, new String[] { 
			"    ", // convert \t to four spaces
			"def",  // ctrl-u to erase the whole line
			"defghi", // load from history (one command above) and edit it
			"xy"    // use backspace to delete
		});
	}
	
	@Test
	public void lineEditTest(){
		Mocks m = Mocks.createSuite(1, 15);
		ConsoleSession session = new ConsoleSession();
		MockANSIConsole console = new MockANSIConsole(session, m);
		
		String input = "abc"      + // "abc_"
					   "\033[D"   + // LEFT
					   "x"        + // "abx*c*"
					   "\033[H"   + // HOME
					   "12"       + // "12*a*bxc"
					   "\033[3~"  + // DELETE
					   "f"        + // "12f*b*xc"
					   "\177\177" + // BACK DELETE
					   "\033[C"   + // RIGHT
					   "y"        + // "1b*y*xc"
					   "\033[F"   + // END
					   "0"        + // "1byxc0_"
					   "\n";
		
		console.setLineInput(input);
		
		validateReadLn(console, new String[] { 
			"1byxc0"
		});
	}

	private void validateReadLn(MockANSIConsole console, String[] strings) {
		for (String str : strings) {
			String res = console.readln();
			Assert.assertEquals(str, res);
		}
	}
}
