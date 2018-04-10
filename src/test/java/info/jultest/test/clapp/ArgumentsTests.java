package info.jultest.test.clapp;

import info.julang.clapp.CLEnvironment;
import info.julang.clapp.CLParser;
import info.julang.clapp.CLParsingException;

import org.junit.Assert;
import org.junit.Test;

public class ArgumentsTests {
	
	@Test
	public void parserHelpTest1() throws CLParsingException {
		CLEnvironment env = parse("-mp f:/a/b -h filea.jul");
		Assert.assertTrue(env.shouldTerminate());
	}
	
	@Test
	public void parserTest1() throws CLParsingException {
		CLEnvironment env = parse("-mp f:/a/b filea.jul");
		validateModulePaths(env, new String[]{"f:/a/b"});
		validateScriptFile(env, "filea.jul");
	}
	
	@Test
	public void parserTest2() throws CLParsingException {
		CLEnvironment env = parse("-mp f:/a/b --module-path f:/c/d filea.jul");
		validateModulePaths(env, new String[]{"f:/a/b", "f:/c/d"});
		validateScriptFile(env, "filea.jul");
	}
	
	@Test
	public void parserTest3() throws CLParsingException {
		CLEnvironment env = parse("-mp:f:/a/b\t--module-path:f:/c/d\tfilea.jul");
		validateModulePaths(env, new String[]{"f:/a/b", "f:/c/d"});
		validateScriptFile(env, "filea.jul");
	}
	
	@Test
	public void parserTest4() throws CLParsingException {
		CLEnvironment env = parse("-mp:f:/a/b,f:/c/d -f filea.jul fileb.jul");
		validateModulePaths(env, new String[]{"f:/a/b", "f:/c/d"});
		validateScriptFile(env, "filea.jul");
	}
	
	@Test(expected=CLParsingException.class)
	public void parserFailTest2() throws CLParsingException {
		parse("--mp f:/a/b filea.jul");
	}
	
	private void validateModulePaths(CLEnvironment env, String[] expectedModulePaths){
		String[] strs = env.getModulePaths();
		Assert.assertNotNull(strs);
		Assert.assertArrayEquals(expectedModulePaths, strs);
	}
	
	private void validateScriptFile(CLEnvironment env, String scriptFile){
		String str = env.getScriptFile();
		Assert.assertNotNull(str);
		Assert.assertEquals(scriptFile, str);
	}

	private CLEnvironment parse(String cmdLine) throws CLParsingException {
		String[] args = cmdLine.split("\\s");
		CLParser parser = new CLParser(args);
		return parser.parse();
	}

}
