package info.jultest.test.oo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

import info.jultest.test.TestExceptionHandler;
import info.julang.execution.IScriptEngine;
import info.julang.interpretation.errorhandling.JulianScriptException;

/**
 * A mixin class providing validation support for Julian exceptions.
 * 
 * @author Ming Zhou
 */
public class ExceptionTestsBase {

	private static String ptnStr = "(^[^\\)]*\\))\\s*\\((.*),\\s*(\\d+)\\)$";
	private static Pattern ptn = Pattern.compile(ptnStr);
	
	private static final int FuncSig = 0;
	private static final int FilePath = 1;
	private static final int FileLineNo = 2;
	
	protected TestExceptionHandler installExceptionHandler(IScriptEngine engine){
		TestExceptionHandler teh = new TestExceptionHandler();
		engine.setExceptionHandler(teh);
		return teh;
	}
	
	/**
	 * Validate that the exception that faulted the main thread meet certain criteria. The caller may opt out of any of those 
	 * checks by providing argument with special values.
	 * <p>
	 * This method doesn't inspect or verify the cause of the exception.
	 * 
	 * @param teh the exception handler returned by {@link #installExceptionHandler(IScriptEngine)}.
	 * @param typName expected full type name. null if not to be verified.
	 * @param msg expected exception message. null if not to be verified.
	 * @param stacktrace expected exception stacktrace. null if not to be verified. File path is not checked for now.
	 * Each entry should start from the function signature, then a file path, and end with a line number. Example:
	 * <code>fun(MyModule.MyStream)  (/.../my_script.jul, 93)</code>
	 * @param fileName expected source file name where the exception is last thrown from. 
	 * null if not to be verified. This check passes as long as the argument is a suffix of the full path.
	 * @param lineNo expected source file line number where the exception is last thrown from. -1 if not to be verified.
	 */
	protected void validateException(
		TestExceptionHandler teh,
		String typName,
		String msg,
		String[] stacktrace,
		String fileName,
		int lineNo){
		if(typName!=null){
			assertEquals(typName, teh.getTypeName());
		}
		
		if(msg!=null){
			assertEquals(msg, teh.getMessage());
		}
		
		if(stacktrace!=null){
			String[] st = teh.getStacktrace();
			assertEquals(stacktrace.length, st.length);
			for(int i=0; i<st.length; i++){
				String[] expected = matchTrace(stacktrace[i]);
				String[] actual = matchTrace(st[i]);
				if(expected == null || expected.length != 3){
					Assert.fail("The expected stack trace doesn't comply with the required format. This is a unit test defect.");
				}
				assertEquals(expected.length, actual.length);
				
				assertEquals(expected[FuncSig], actual[FuncSig]);
				//assertEquals(expected[FilePath], actual[FilePath]);
				assertEquals(expected[FileLineNo], actual[FileLineNo]);
			}
		}
		
		if(fileName!=null){
			assertTrue(teh.getLastFileName().endsWith(fileName));
		}
		
		if(lineNo>=0){
			assertEquals(lineNo, teh.getLastLineNumber());
		}
	}
	
	protected void validateCause(
		TestExceptionHandler originTeh,
		String typName,
		String msg,
		String[] stacktrace,
		String fileName,
		int lineNo){
		JulianScriptException jse = originTeh.getCause();
		assertNotNull(jse);
		TestExceptionHandler teh = new TestExceptionHandler();
		teh.onException(jse);
		validateException(teh, typName, msg, stacktrace, fileName, lineNo);
	}
	
	/**
	 * Assert that an exception of specified type has been thrown, regardless of other info.
	 * 
	 * @param teh
	 * @param exceptionName
	 */
	protected void assertException(TestExceptionHandler teh, String exceptionName){
		validateException(
			teh, 
			exceptionName,
			null,
			null,
			null,
			-1);
	}
	
	/**
	 * Assert that an exception of specified type has been thrown, with specified cause, regardless of other info.
	 * 
	 * @param teh
	 * @param exceptionName skip checking this if null
	 * @param causeName
	 */
	protected void assertCause(TestExceptionHandler teh, String exceptionName, String causeName){
		if (exceptionName != null){
			validateException(
				teh, 
				exceptionName,
				null,
				null,
				null,
				-1);
		}
		
		validateCause(
			teh, 
			causeName,
			null,
			null,
			null,
			-1);
	}
	
	/**
	 * 
	 * @param trace "funC(Integer)  (/script/exception_1.jul, 12)";
	 * @return A String array with 3 elements: <br/>
	 * 	[0]: funC(Integer) <br/>
	 *  [1]: /script/exception_1.jul <br/>
	 *  [2]: 12
	 */
	private String[] matchTrace(String trace){
		Matcher m = ptn.matcher(trace);
			
		String[] result = null;
		
		if (m.find()) {
			result = new String[3];
			result[FuncSig] = m.group(1);
			result[FilePath] = m.group(2);
			result[FileLineNo] = m.group(3);
		}
		
		return result;
	}
	
}
