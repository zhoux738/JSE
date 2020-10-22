package info.jultest.test.clapp;

import org.junit.Test;

import info.julang.clapp.repl.IPreparser;
import info.julang.clapp.repl.IPreparserReplaceable;
import info.julang.clapp.repl.REPLParsingException;
import info.julang.clapp.repl.pparser.BlockPreparser;
import info.julang.clapp.repl.pparser.ContinuationPreparser;
import info.julang.clapp.repl.pparser.RegularPreparser;
import org.junit.Assert;

public class PreparserTests {

	static class ParserBox implements IPreparserReplaceable {

		private IPreparser parser;
		
		public ParserBox(){
			parser = RegularPreparser.INSTANCE;
		}
		
		@Override
		public void setPreparser(IPreparser parser) {
			this.parser = parser;
		}
		
		public IPreparser getPreparser(){
			return parser;
		}
	}
	
	class Validator{
		
		private ParserBox box;
		
		Validator(ParserBox box){
			this.box = box;
		}
		
		/**
		 * Call parse() on the current preparser.
		 * 
		 * @param input
		 * @param obj if it's Class, verify that the new preparser is of this class; if it's string, verify the output against it.
		 * @return the output from parse() call
		 * @throws REPLParsingException
		 */
		String input(String input, Object obj) throws REPLParsingException {
			IPreparser pp = box.getPreparser();
			String output = pp.parse(box, input);
			if (obj instanceof String){
				Assert.assertEquals(obj, output);
			} else if (obj instanceof Class){
				pp = box.getPreparser();
				Assert.assertTrue(pp.getClass() == obj);
			}
			
			return output;
		}
	}
	
	@Test
	public void blockPreparserTest() throws REPLParsingException {
		ParserBox box = new ParserBox();
		Validator v = new Validator(box);
		v.input("class C { ", BlockPreparser.class);
		v.input("  string s; ", null);
		String actual = 
		v.input("}", RegularPreparser.class);
		
		String expected = 
			"class C {" + System.lineSeparator() +
			"  string s;" + System.lineSeparator() +
			"}";
		
		Assert.assertEquals(expected, actual);
	}

	@Test
	public void contPreparserTest() throws REPLParsingException {
		ParserBox box = new ParserBox();
		Validator v = new Validator(box);
		
		// By ending with one or more \, turn to ContinuationPreparser
		v.input("a b c \\\\", ContinuationPreparser.class); 
		
		// When processed by ContinuationPreparser, \ is optional. Without it we still treat it as continuation. The trailing blanks, however, are removed.
		v.input("d e f ", null); 
		
		// If we want to preserve the trailing blanks, protect them with a trailing \
		v.input("d2 e2 f2 \\ ", null);
		
		// If the trailing \ is not separated with the main text with at least one blank char, treat them as part of input
		v.input("g h i\\", null);
		
		// End ContinuationPreparser with a single ;
		String actual = 
		v.input(";", RegularPreparser.INSTANCE);
	
		String expected = 
			"a b c " + System.lineSeparator() +
			"d e f" + System.lineSeparator() +
			"d2 e2 f2 " + System.lineSeparator() +
			"g h i\\" + System.lineSeparator() +
			";";
		
		Assert.assertEquals(expected, actual);
	}

}
