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

package info.julang.interpretation.errorhandling;

import info.julang.execution.threading.JThread;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.modulesystem.IModuleManager;
import info.julang.modulesystem.ModuleManager;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassType;

public final class JSExceptionUtility {

	public final static String SystemModule = "System";
	
	public final static String SystemExceptionClass = SystemModule + "." + "Exception";
	
	private final static String throwErrMsg = "Cannot throw an object which is not an instance of System.Exception or its subclass.";
	
	private static JClassType sysExceptType;
	
	/**
	 * Set the source info into an exception.
	 * 
	 * @param jse the exception instance wrapping an actual script exception.
	 * @param ast used for getting the file name.
	 * @param lineNo 1-based. Note {@link ScanLocation} is 0-based.
	 */
	public static void setSourceInfo(JulianScriptException jse, AstInfo<?> ast, int lineNo){
		jse.setLineNumber(lineNo);
		jse.setFileName(ast.getFileName());
	}
	
	/**
	 * Set the source info into an exception. 
	 * 
	 * @param jse the exception instance wrapping an actual script exception.
	 * @param loInfo the source info
	 */
	public static void setSourceInfo(JulianScriptException jse, IHasLocationInfo loInfo){
		jse.setLineNumber(loInfo.getLineNumber());
		jse.setFileName(loInfo.getFileName());
	}
	
	public static void loadSystemModule(JThread thread, IModuleManager mm){
		if(!mm.isLoaded("System")){
			((ModuleManager)mm).loadModule(thread, "System");
		}
		if(!mm.isLoaded("System.Lang")){
			((ModuleManager)mm).loadModule(thread, "System.Lang");
		}
		if(!mm.isLoaded("System.IO")){
			((ModuleManager)mm).loadModule(thread, "System.IO");
		}
	}
	
	/**
	 * Initialize a Julian script exception using the runtime value (which is a script exception instance).
	 * 
	 * @param context
	 * @param jval Must be a <code><font color=="green">System.Exception</font></code>.
	 * @param linfo location info
	 */
	public static JulianScriptException initializeAsScriptException(Context context, JValue jval, IHasLocationInfo linfo) {
		if(jval == null){
			throw new RuntimeCheckException("Trying to throw a null object.", linfo);
		}
		
		jval = jval.deref();
		if(jval.getKind() != JValueKind.OBJECT){
			throw new RuntimeCheckException(throwErrMsg, linfo);
		}
		
		ObjectValue ov = (ObjectValue) jval;
		ICompoundType typ = (ICompoundType) ov.getType();
		if(!typ.isDerivedFrom(getSysExceptType(context), true)){
			throw new RuntimeCheckException(throwErrMsg, linfo);
		}
		
		JulianScriptException jse = new JulianScriptException(context.getTypTable(), ov);
		// Capture JSE (step 1/2):
		// At this point we have file and location info, but we don't know what function is called and
		// what are the parameters. So we set source info and throw again.
		JSExceptionUtility.setSourceInfo(jse, linfo);
		
		return jse;
	}
	
	private static JClassType getSysExceptType(Context context){
		if(sysExceptType == null){
			JSExceptionUtility.loadSystemModule(context.getJThread(), context.getModManager());
			sysExceptType = (JClassType) context.getTypTable().getType(JSExceptionUtility.SystemExceptionClass);
		}
		return sysExceptType;
	}

	public static boolean isFatal(JulianScriptException jse) {
		String name = jse.getExceptionFullName();
		return "System.StackOverflowException".equals(name); // To be extended
	}
	
}
