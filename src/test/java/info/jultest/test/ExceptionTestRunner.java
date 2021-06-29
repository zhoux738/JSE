package info.jultest.test;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import info.julang.JSERuntimeException;
import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.DefaultExceptionHandler;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;

import org.junit.Assert;

/**
 * Use this class when the test case needs to throw a Julian exception yet 
 * the test class is not supposed to derive from {@link ExceptionTestsBase}.
 * 
 * @author Ming Zhou
 */
public class ExceptionTestRunner extends ExceptionTestsBase {
	
	private TestExceptionHandler teh;
	
	private SimpleScriptEngine engine;
	
	private String group;
	
	private String feature;
	
	private String[] args;
	
	public ExceptionTestRunner(String group, String feature) {
		this(group, feature, false);
	}
	
	public ExceptionTestRunner(String group, String feature, boolean reentry){
		VariableTable gvt = new VariableTable(null);
		engine = makeSimpleEngine(gvt, reentry);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		teh = installExceptionHandler(engine);
		this.group = group;
		this.feature = feature;
	}
	
	/**
	 * Get the engine instance.
	 */
	public SimpleScriptEngine getEngine() {
		return engine;
	}
	
	public void setArguments(String... args) {
		this.args = args;
	}
	
	private void execute(String fileName) throws EngineInvocationError {
		if (this.args != null) {
			engine.getContext().setArguments(args);
		}
		
		engine.run(getScriptFile(group, feature, fileName));
	}
	
	/**
	 * For dev only.
	 * 
	 * @param fileName
	 * @throws EngineInvocationError
	 */
	public void executeOnly(
		String fileName) 
		throws EngineInvocationError {
		
		engine.setExceptionHandler(new DefaultExceptionHandler(engine.getRuntime().getStandardIO(), true));
		execute(fileName);
	}
	
	/**
	 * Execute a script and validate the exception thrown from it.
	 * <p/>
	 * Example:<pre><code>executeAndValidate(
     *  "script1.jul", 
     *  "System.DivByZeroException",
     *  "Cannot divide by zero.",
     *  new String[]{
     *    "funC(Integer)  (/.../exception_2.jul, 12)",
     *    "funB(Integer)  (/.../exception_2.jul, 8)",
     *    "funA(Integer)  (/.../exception_2.jul, 4)"
     *  },
     *  null,
     *  15);</code></pre>
	 * @param fileName
	 * @param exceptionName FQ name
	 * @param exceptionMessage
	 * @param exceptionStackTrace message format in <code>function-name(arg-list)  (file-name, line-no)</code>
	 * @param checkFileName check the name of last file in the stack trace.
	 * @param lineNo the last line number in the stack trace.
	 * @throws EngineInvocationError
	 */
	public void executeAndValidate(
		String fileName,
		String exceptionName,
		String exceptionMessage,
		String[] exceptionStackTrace,
		boolean checkFileName,
		int lineNo) 
		throws EngineInvocationError {

		execute(fileName);
		
		validateException(
			teh, 
			exceptionName,
			exceptionMessage,
			exceptionStackTrace,
			checkFileName ? fileName : null,
			lineNo);
	}
	
	/**
	 * Check exception name only. <br>
	 * Simple but less accurate.
	 */
	public void executeAndExpect(
		String fileName,
		JSERuntimeException expected) 
		throws EngineInvocationError {
		executeAndExpect(
			fileName, 
			expected.getKnownJSException(), 
			expected.getClass().getName(),
			null);
	}
	
	/**
	 * Check the name of exception and direct cause. Also check texts that appear in the stack trace. <br>
	 * Simple but less accurate.
	 */
	public void executeAndExpect(
		String fileName,
		KnownJSException exception,
		KnownJSException directCause,
		String... texts) 
		throws EngineInvocationError {
		
		execute(fileName);
		
		if (exception != null) {
			Assert.assertEquals(exception.getFullName(), teh.getTypeName());
		}
		if (directCause != null) {
			Assert.assertEquals(directCause.getFullName(), teh.getCause().getExceptionFullName());
		}
		
		String strace = teh.getStandardExceptionOutput();
		AssertHelper.validateStringOccurences(strace, texts);
	}
	
	/**
	 * Check the name of exception and direct cause, as well as the line number for each.
	 */
	public void executeAndExpect(
		String fileName,
		String exceptionName,
		int exceptionLine,
		String causeName,
		int causeLine) 
		throws EngineInvocationError {
		
		try {
			execute(fileName);
			
			validateException(
				teh, 
				exceptionName,
				null,
				null,
				null,
				exceptionLine);
			
			if (causeName != null){
				validateCause(
					teh,
					causeName,
					null,
					null,
					null,
					causeLine);				
			}
		
			return;
		} catch (JulianScriptException jse) {
			// 2) If the exception is thrown as is, check the Java exception.
			assertEquals(exceptionName, jse.getExceptionType().getName());
			assertEquals(causeName, jse.getJSECause().getType().getName());
		} 
		
		fail("Expected an exception of type \"" + exceptionName + "\", but ended successfully .");
	}
	
	private void executeAndExpect(
		String fileName,
		KnownJSException kjs,
		String platformExceptionName,
		KnownJSException cause) 
		throws EngineInvocationError {
		
		try {
			execute(fileName);	
			
			// 1) If the exception is converted to JSE, check the engine stack.
			if(!GlobalSetting.skipCatch(kjs)){
				validateException(
					teh, 
					kjs.getFullName(),
					null,
					null,
					null,
					-1);
				
				if (cause != null){
					validateCause(
						teh,
						cause.getFullName(),
						null,
						null,
						null,
						-1);				
				}
				
				return;
			}
		} catch (JSERuntimeException jse) {
			// 2) If the exception is thrown as is, check the Java exception.
			if(GlobalSetting.skipCatch(kjs)){
				assertEquals(kjs, jse.getKnownJSException());
				return;
			}
		} catch (Throwable t){
			if (platformExceptionName != null) {
				fail("Expected an exception of type \"" + 
					platformExceptionName + "\", but saw " + 
					t.getClass().getName() + ":\n  " + t.getMessage());
			} else {
				fail("Expected an exception of Julian type \"" + 
					kjs.getFullName() + "\", but saw native exception " + 
					t.getClass().getName() + ":\n  " + t.getMessage());
			}
			
			return;
		}

		if (platformExceptionName != null) {
			fail("Expected an exception of type \"" + 
				platformExceptionName + "\", but ended successfully .");
		} else {
			fail("Expected an exception of Julian type \"" + 
				kjs.getFullName() + "\", but ended successfully .");
		}
	}
}
