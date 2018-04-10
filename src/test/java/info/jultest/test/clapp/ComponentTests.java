package info.jultest.test.clapp;

import org.junit.Test;

import info.julang.clapp.repl.os.LineBuffer;
import info.julang.clapp.update.WrapperScriptUpdater;
import junit.framework.Assert;

public class ComponentTests {
	
	@Test
	public void scriptUpdateTest() {
		class TestWrapperScriptUpdater extends WrapperScriptUpdater {

			public TestWrapperScriptUpdater() {
				super();
				this.addVariable("Var1", "XYZ");
				this.addVariable("Var2", "ZYX");
				this.addVariable("Var3", "Var3");
			}
			
			void validateProcess(String input, String expected){
				String s2 = process(input);
				Assert.assertEquals(expected, s2);
			}
		};
		
		TestWrapperScriptUpdater updater = new TestWrapperScriptUpdater();
		updater.validateProcess("abc def ", "abc def ");
		updater.validateProcess("abc @def@ gh", "abc @def@ gh");
		updater.validateProcess("abc @Var1@ gh", "abc XYZ gh");
		updater.validateProcess("abc @vaR1@ gh", "abc XYZ gh");
		updater.validateProcess("abc @vaR1@@var2@ gh", "abc XYZZYX gh");
		updater.validateProcess("abc @va R1@var2@ gh", "abc @va R1ZYX gh");
		updater.validateProcess("abc @va R1", "abc @va R1");
		updater.validateProcess("abc @@", "abc @@");
		updater.validateProcess("@echo off", "@echo off");
		updater.validateProcess("abc @Var3@ gh", "abc Var3 gh");
	}

	@Test
	public void lineBufferBasicTest(){
		Mocks m = Mocks.createSuite(1, 5);
		Terminal term = m.getTerminal();
		LineBuffer buffer = new MockLineBuffer(0, term, m.getController(), m.getPrinter());
		
		buffer.printChar('a');
		term.validateScreen(new String[] { "a_" });
		buffer.printChar('b');
		term.validateScreen(new String[] { "ab_" });
		buffer.printChar('c');
		term.validateScreen(new String[] { "abc_" });
		buffer.deletePrev();
		term.validateScreen(new String[] { "ab_" });
		buffer.printChar('d');
		term.validateScreen(new String[] { "abd_" });
		buffer.moveCursor(-1);
		term.validateScreen(new String[] { "ab*d*" });
		buffer.moveCursor(-1);
		term.validateScreen(new String[] { "a*b*d" });
		buffer.printChar('e');
		term.validateScreen(new String[] { "ae*b*d" });
		buffer.deleteCurr();
		term.validateScreen(new String[] { "ae*d*" });
		buffer.deleteCurr();
		term.validateScreen(new String[] { "ae_" });
		buffer.deleteCurr();
		term.validateScreen(new String[] { "ae_" });
	}
	
	@Test
	public void lineBufferResetTest(){
		Mocks m = Mocks.createSuite(1, 5);
		Terminal term = m.getTerminal();
		LineBuffer buffer = new MockLineBuffer(0, term, m.getController(), m.getPrinter());
		
		buffer.replace("abcd");
		term.validateScreen(new String[] { "abcd_" });
		buffer.deletePrev();
		term.validateScreen(new String[] { "abc_" });
		buffer.replace("");
		term.validateScreen(new String[] { "_" });
		buffer.deletePrev();
		term.validateScreen(new String[] { "_" });
		buffer.deleteCurr();
		term.validateScreen(new String[] { "_" });
	}
}
