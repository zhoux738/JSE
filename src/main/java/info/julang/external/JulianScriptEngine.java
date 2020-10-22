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

package info.julang.external;

import java.io.InputStream;
import java.io.OutputStream;

import javax.script.ScriptException;

import info.julang.external.binding.BindingKind;
import info.julang.external.binding.BooleanBinding;
import info.julang.external.binding.CharacterBinding;
import info.julang.external.binding.FloatBinding;
import info.julang.external.binding.IBinding;
import info.julang.external.binding.IntegerBinding;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.binding.StringBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.IExtResult;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtValue;
import info.julang.external.interfaces.IExtValue.IBoolVal;
import info.julang.external.interfaces.IExtValue.IByteVal;
import info.julang.external.interfaces.IExtValue.ICharVal;
import info.julang.external.interfaces.IExtValue.IEnumVal;
import info.julang.external.interfaces.IExtValue.IFloatVal;
import info.julang.external.interfaces.IExtValue.IHostedVal;
import info.julang.external.interfaces.IExtValue.IIntVal;
import info.julang.external.interfaces.IExtValue.IObjectVal;
import info.julang.external.interfaces.IExtValue.IStringVal;

/**
 * The public-facing Julian Script Engine (JSE) API.
 * <p>
 * This is a user-friendly facade which provides all the basic functionalities of the script engine.
 * The engine is isolated, reentrant and provides APIs that are fault-tolerant. In case of script 
 * failure, the error info (stacktrace, etc.) will be printed out to a configurable sink which 
 * defaults to the platfrom's standard error.
 * <p>
 * However, there is a practicality issue with this API. Most Java users would prefer to targeting  
 * the standard Java scripting interface, a.k.a <code>javax.script</code> (JSR-223) package. Julian 
 * does also provide fully compliant JSR-223 API, which is in fact a thin wrapper of this class.
 * <p>
 * When using JSE from the command line, the entrance is {@link info.julang.CmdLineApplication}, 
 * which parses cmdline arguments and provides interactive experiences. But ultimately it still uses
 * this class to execute the script.
 * 
 * @see {@link info.julang.jsr223.JulianScriptingEngine JSR-223 compliant Julian script engine}.
 * @author Ming Zhou
 */
public class JulianScriptEngine {
	
	protected IExtScriptEngine engine;
	
	private EngineInitializationOption option;
	
	private InputStream input;
	private OutputStream output;
	private OutputStream error;
	
	public JulianScriptEngine(boolean throwOnScriptError, boolean isInteractiveMode){
		this(new EngineInitializationOption(true, throwOnScriptError, isInteractiveMode));
	}
	
	protected JulianScriptEngine(EngineInitializationOption option){
		EngineFactory fact = new EngineFactory(option);
		engine = fact.createEngine();
		this.option = option;
	}
	
	//--------------------------- Environment Settings ----------------------------//
	
	/**
	 * Add a module path. Duplicate paths are disregarded.
	 * @param path
	 */
	public void addModulePath(String path){
		engine.getContext().addModulePath(path);
	}
	
	/**
	 * Reset the engine. Wipe out all the variables and types.
	 */
	public void reset(){
		engine.reset();
	}
	
	/**
	 * Allow access to the named category/operations.
	 * <p>
	 * If the operations are not given, allow all operations under this category.
	 * If the category is the wildcard (*), allow everything. This is also the default setting.
	 * <p>
	 * @param category
	 * @param operations
	 */
	public void allow(String category, String... operations){
		engine.getContext().addPolicy(true, category, operations);
	}
	
	/**
	 * Deny access to the named category/operations.
	 * <p>
	 * If the operations are not given, deny all operations under this category.
	 * If the category is the wildcard (*), deny everything.
	 * <p>
	 * @param category
	 * @param operations
	 */
	public void deny(String category, String... operations){
		engine.getContext().addPolicy(false, category, operations);
	}
	
	/**
	 * Set standard input stream. This method can be called multiple times, but the new value 
	 * will only take effect until the next invocation (e.g. {@link JulianScriptEngine#runFile(String)}).
	 * <p>
	 * This is the input source for <code><font color="green">System.Console.readln()</font></code>.
	 * 
	 * @param input The standard input stream to use within the scripts. If null, default to the platform's standard input.
	 */
	public void setInput(InputStream input) {
		this.input = input;
	}
	
	/**
	 * Set standard output stream. This method can be called multiple times, but the new value 
	 * will only take effect until the next invocation (e.g. {@link JulianScriptEngine#runFile(String)}).
	 * <p>
	 * This is the output sink for <code><font color="green">System.Console.println()</font></code>.
	 * 
	 * @param input The standard output stream to use within the scripts. If null, default to the platform's standard output.
	 */
	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	/**
	 * Set standard error stream. This method can be called multiple times, but the new value 
	 * will only take effect until the next invocation (e.g. {@link JulianScriptEngine#runFile(String)}).
	 * <p>
	 * This is the output sink for unhandled exceptions.
	 * 
	 * @param input The standard error stream to use within the scripts. If null, default to the platform's standard error.
	 */
	public void setError(OutputStream error) {
		this.error = error;
	}
	
	//----------------------------- External Bindings -----------------------------//
	
	public void bindChar(String name, char c){
		engine.getContext().addBinding(name, new CharacterBinding(c));
	}
	
	/**
	 * Get a char binding's value.
	 * 
	 * @param name
	 * @return
	 * @throws ExternalBindingException
	 */
	public char getChar(String name) throws ExternalBindingException {
		CharacterBinding binding = getBinding(name, BindingKind.Character);
		return binding.getValue();
	}
	
	public void bindBool(String name, boolean b){
		engine.getContext().addBinding(name, new BooleanBinding(b));
	}
	
	/**
	 * Get a boolean binding's value.
	 * 
	 * @param name
	 * @return
	 * @throws ExternalBindingException
	 */
	public boolean getBool(String name) throws ExternalBindingException {
		BooleanBinding binding = getBinding(name, BindingKind.Boolean);
		return binding.getValue();
	}
	
	public void bindFloat(String name, float f){
		engine.getContext().addBinding(name, new FloatBinding(f));
	}
	
	/**
	 * Get an integer binding's value.
	 * 
	 * @param name
	 * @return
	 */
	public float getFloat(String name) throws ExternalBindingException {
		FloatBinding binding = getBinding(name, BindingKind.Float);
		return binding.getValue();
	}
	
	public void bindInt(String name, int i){
		engine.getContext().addBinding(name, new IntegerBinding(i));
	}
	
	/**
	 * Get an integer binding's value.
	 * 
	 * @param name
	 * @return
	 */
	public int getInt(String name) throws ExternalBindingException {
		IntegerBinding binding = getBinding(name, BindingKind.Integer);
		return binding.getValue();
	}
	
	public void bindString(String name, String s){
		engine.getContext().addBinding(name, new StringBinding(s));
	}
	
	/**
	 * Get a string binding's value.
	 * <p/>
	 * If an error occurs, returns null.
	 * 
	 * @param name
	 * @return
	 */
	public String getString(String name) {
		try {
			return getString(name, false);
		} catch (ExternalBindingException e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Get a string binding's value.
	 * <p/>
	 * If an error occurs, returns null.
	 * 
	 * @param name
	 * @param throwOnError if true, will throw null when error occurs.
	 * @return
	 * @throws ExternalBindingException
	 */
	public String getString(String name, boolean throwOnError) throws ExternalBindingException {
		try {
			StringBinding binding = getBinding(name, BindingKind.String);
			return binding.getValue();	
		} catch (ExternalBindingException e) {
			if(throwOnError) {
				throw e;
			}
		}
		
		return null;	
	}
	
	public void bindObject(String name, Object o){
		engine.getContext().addBinding(name, new ObjectBinding(o));
	}
	
	/**
	 * Get an object binding's value.
	 * <p/>
	 * If an error occurs, returns null.
	 * 
	 * @param name
	 * @return
	 */
	public Object getObject(String name) {
		try {
			return getObject(name, false);
		} catch (ExternalBindingException e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Get an object binding's value.
	 * <p/>
	 * If an error occurs, returns null.
	 * 
	 * @param name
	 * @param throwOnError if true, will throw null when error occurs.
	 * @return
	 * @throws ExternalBindingException
	 */
	public Object getObject(String name, boolean throwOnError) throws ExternalBindingException {
		try {
			ObjectBinding binding = getBinding(name, BindingKind.Object);
			return binding.getValue();	
		} catch (ExternalBindingException e) {
			if(throwOnError) {
				throw e;
			}
		}
		
		return null;	
	}
	
	@SuppressWarnings("unchecked")
	private <T> T getBinding(String name, BindingKind bk) throws ExternalBindingException {
		IBinding binding = engine.getContext().getBinding(name);
		if(binding == null){
			throw ExternalBindingException.create(name, ExternalBindingException.Type.NOT_EXIST);
		}
		
		if(binding.getKind() == bk){
			return (T) binding;
		} else {
			throw ExternalBindingException.create(name, ExternalBindingException.Type.BAD_TYPE);
		}
	}
	
	public void addBinding(String name, IBinding binding){
		engine.getContext().addBinding(name, binding);
	}
	
	//-------------------------------- Invocation: public API ---------------------------------//
	
	/**
	 * Run the Julian script as specified by the path, without any arguments.
	 * 
	 * @param Path the script file's path
	 * @return The result of running; can be null
	 */
	// This method is currently not used anywhere
	public Object runFile(String path) {
		try {
			return runFile(path, null);
		} catch (JSEException e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Run the Julian script as specified by the path.
	 * 
	 * @param Path the script file's path
	 * @param arguments Arguments to pass along to the script
	 * @return The result of running; can be null
	 * @throws JSEException A wrapper exception, the cause of which can be any of 
	 * {@link JSEError}, {@link EngineInvocationError}, {@link ScriptException}, etc.
	 */
	public Object runFile(String path, String[] arguments) throws JSEException {
		return runInternal(path, arguments, true);
	}
	
	/**
	 * Run the string as a Julian script.
	 * 
	 * @param Script the script content
	 * @param arguments Arguments to pass along to the script
	 * @return The result of running; can be null
	 * @throws JSEException A wrapper exception, the cause of which can be any of 
	 * {@link JSEError}, {@link EngineInvocationError}, {@link ScriptException}, etc.
	 */
	public Object runScript(String script, String[] arguments) throws JSEException {
		return runInternal(script, arguments, false);
	}
	
	/**
	 * Stop the current running.
	 * 
	 * Note this method can only be practically useful if the running was launched in a separate thread.
	 * Since methods such as {@link #runFile(String)} will be blocking, a caller won't get chance to 
	 * stop it.
	 * 
	 * @return true if the engine is to be stopped; false if the engine is not running at all.
	 */
	public boolean stopRunning() {
		return engine.abort();
	}
	
	//-------------------------------- Invocation: internals ----------------------------------//
	
	private Object runInternal(String script, String[] args, boolean isFileOrSnippet) throws JSEException {
		try {
			engine.setRedirection(output, error, input);
			
			engine.getContext().setArguments(args);
			if (isFileOrSnippet) {
				engine.runFile(script);
			} else {
				engine.runSnippet(script);
			}
			
			IExtResult result = engine.getResult();
			
			return convertResult(result);
		} catch (JSEError error) {
			// Engine bug
			throw new JSEException("The engine encountered an unexpected exception.", error);
		} catch (EngineInvocationError eir) {
			// Environment error
			throw new JSEException("The engine encountered an exception.", eir);
		} catch (ScriptException se) {
			// User error
			throw new JSEException("The script threw an exception.", se);
		}
	}

	private Object convertResult(IExtResult result) throws ScriptException {
		if(result == null){
			return null;
		}
		
		if(result.isSuccess()){
			IExtValue jval = result.getReturnedValue(true);
			switch(jval.getKind()){
			case BOOLEAN:
				return ((IBoolVal)jval).getBoolValue();
			case BYTE:
				return ((IByteVal)jval).getByteValue();
			case CHAR:
				return ((ICharVal)jval).getCharValue();
			case FLOAT:
				return ((IFloatVal)jval).getFloatValue();
			case INTEGER:
				return ((IIntVal)jval).getIntValue();
			case NONE:
				return null;
			case OBJECT:
				IObjectVal ov = (IObjectVal)jval;
				switch(ov.getBuiltInValueKind()){
				case HOSTED:
					IHostedVal hv = (IHostedVal) ov;
					return hv.getHostedObject();
				case STRING:
					IStringVal sv = (IStringVal) ov;
					return sv.getStringValue();
				case ENUM:
					IEnumVal ev = (IEnumVal) ov;
					return ev.getOrdinal();
				default:
					break;
				}
				// Fall thru
			default:
				return JSE_OBJECT_VALUE;
			}
		} else {
			String error = result.getExceptionOutput();
			String fileName = result.getExceptionFileName();
			int lineNo = result.getExceptionLineNumber();
			if (!option.isInteractiveMode() && // If we are running in interactive mode, do not throw. The exception has been printed out, and the console must continue as usual.
				option.useExceptionDefaultHandler()){
				throw new ScriptException(
					error != null ? System.lineSeparator() + error : "<Error Unavailable>", 
					fileName, 
					lineNo);
			} else {
				return null;
			}
		}
	}
	
	// A temporary solution for now. Will evolve.
	public final static Object JSE_OBJECT_VALUE = new Object() {
		@Override
		public String toString(){
			return "<Unable to convert to Java object>";
		}
	};	
}
