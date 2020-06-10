package info.jultest.test;

import org.junit.Assert;

import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.DefaultExceptionHandler;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.typesystem.jclass.JClassType;

public class AssertableExceptionHandler extends DefaultExceptionHandler {

	public AssertableExceptionHandler() {
		super(GlobalSetting.DumpException);
	}
	
	@Override
	public void onException(JulianScriptException jse) {
		super.onException(jse);
		
		JClassType typ = jse.getExceptionType();
		String msg = jse.getExceptionMessage();

		Assert.fail(
			System.lineSeparator() +
			"(Julian Exception) " + System.lineSeparator() + 
			"File: " + jse.getFileName() + System.lineSeparator() + 
			"Line: " + jse.getLineNumber() + System.lineSeparator() + 
			typ.getName() + ": " + msg);
	}
}
