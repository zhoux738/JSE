package info.jultest.test;

import info.julang.dev.GlobalSetting;
import info.julang.execution.simple.DefaultExceptionHandler;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.jclass.JClassType;

public class TestExceptionHandler extends DefaultExceptionHandler {

	public TestExceptionHandler() {
		super(GlobalSetting.DumpException);
	}

	private String typName;
	
	private String msg;
	
	private String[] stacktrace;
	
	private String fileName;
	
	private int lineNo;
	
	private JulianScriptException cause;
	
	private JulianScriptException jse;
	
	@Override
	public void onException(JulianScriptException jse) {
		super.onException(jse);
		
		JClassType typ = jse.getExceptionType();
		typName = typ.getName();
		msg = jse.getExceptionMessage();
		stacktrace = jse.getStackTraceAsArray();
		fileName = jse.getFileName();
		lineNo = jse.getLineNumber();
		this.jse = jse;
		
		ObjectValue inner = jse.getJSECause();
		if(inner != null){
			cause = new JulianScriptException(Commons.DummyTypeTable, inner);
		}
	}
	
	public String getTypeName() {
		return typName;
	}

	public String getMessage() {
		return msg;
	}

	public String[] getStacktrace() {
		return stacktrace;
	}

	public String getLastFileName() {
		return fileName;
	}

	public int getLastLineNumber() {
		return lineNo;
	}
	
	public JulianScriptException getCause() {
		return cause;
	}
	
	public String getStandardExceptionOutput() {
		return jse.getStandardExceptionOutput(0, true);
	}
}
