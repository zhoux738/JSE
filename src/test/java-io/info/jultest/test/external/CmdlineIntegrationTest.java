package info.jultest.test.external;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import info.julang.CmdLineApplication;
import info.julang.clapp.CLParsingException;
import info.jultest.test.Commons;

public class CmdlineIntegrationTest {
	
	/*
	 java -jar JSE.jar 
	 [-h|--help+/-] [-i|--interactive+/-] 
	 [-f|--file <value>] 
	 [-mp|--module-path <value>]* 
	 [-q|--quiet+/-] 
	 [-s|--snippet <value>] 
	 [-v|--variable <value>]* 
	 [script-file] 
	 [argument]*
options:
    -h  (--help)                 Show help message.
    -i  (--interactive)          Launch interactive console.
    -f  (--file)                 The script file to run. This will overwrite the free argument (script-file) at the end.
    -mp (--module-path)          Add one or more module paths.
    -q  (--quiet)                Do not print the result.
    -s  (--snippet)              The script snippet to run. This will overwrite any script file arguments.
    -v  (--variable)             Add one or more script vairables, in the format of name[:type][=value]. Types are: bool, char, int, string.
	 */
	@Test
	public void cmdAppTest1() throws CLParsingException {
		int result = CmdLineApplication.runMain(new String[] { 
			"-f", getPath("app_1.jul"),
			"-mp", Commons.SRC_REPO_ROOT,
			"-q",
			"-v", "sevenInt:int=7",
			"-v", "trueBool:bool=true",
			"-v", "zChar:char=z",
			"-v", "helloStr:string=hello",
			});
		Assert.assertEquals(12, result);
	}
	
	@Test
	public void cmdAppTest2() throws CLParsingException {
		int result = CmdLineApplication.runMain(new String[] {
			"-q",
			"-s", "return sevenInt * (trueBool ? 5 : 3) * ((int)zChar - (int)'z' + 1) * (helloStr == \"hello\" ? 2 : 0);",
			      //      7        *             5      *             1               *             2
			"-v", "sevenInt:int=7",
			"-v", "trueBool:bool=true",
			"-v", "zChar:char=z",
			"-v", "helloStr:string=hello",
			});
		Assert.assertEquals(7 * 5 * 1 * 2, result);
	}
	
	@Test
	public void cmdAppTest3() throws CLParsingException, IOException {
		PrintStream sysOut = System.out;
		try {
			try(ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(baos, true)){
				System.setOut(ps);
				int result = 0;
				
				// Script file not found: /.../src/test/julian/ExternalAPI/app_not_exist.jul
				result = CmdLineApplication.runMain(new String[] { 
					"-f", getPath("app_not_exist.jul")
				});
				Assert.assertNotEquals(0, result);

				// Script file not specified.
				result = CmdLineApplication.runMain(new String[] { });
				Assert.assertNotEquals(0, result);

				// The script file must be named *.jul.
				result = CmdLineApplication.runMain(new String[] { 
					"-f", getPath("app_not_exist")
				});
				Assert.assertNotEquals(0, result);
				
				String output = baos.toString();
				
				
				Assert.assertTrue(output.contains("not found"));
				Assert.assertTrue(output.contains("not specified"));
				Assert.assertTrue(output.contains("must be named *.jul"));
			}
		} finally {
			System.setOut(sysOut);
		}
	}
	
	private static String getPath(String fileName) {
		return Commons.SRC_REPO_ROOT + "ExternalAPI/" + fileName;
	}
	
}
