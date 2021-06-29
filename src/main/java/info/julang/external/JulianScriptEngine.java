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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import info.julang.external.interfaces.ResetPolicy;
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
 * @see info.julang.jsr223.JulianScriptingEngine JSR-223 compliant Julian script engine
 * @author Ming Zhou
 */
public class JulianScriptEngine {
	
	protected IExtScriptEngine engine;
	
	private EngineInitializationOption option;
	
	private InputStream input;
	private OutputStream output;
	private OutputStream error;
	
	/**
	 * Create a Julian script engine instance.
	 * 
	 * @param throwOnScriptError Throw exception, instead of returning successfully 
	 * (albeit with Julian's stack trace printed out), if the script encounters an error.
	 * @param isInteractiveMode If true, start the engine in interactive mode. 
	 * This mode is reentrant and will preserve defined types.
	 */
	public JulianScriptEngine(boolean throwOnScriptError, boolean isInteractiveMode){
		this(new EngineInitializationOption(true, throwOnScriptError, isInteractiveMode));
	}
	
	protected JulianScriptEngine(EngineInitializationOption option){
		EngineFactory fact = new EngineFactory(option);
		engine = fact.createEngine();
		this.option = option;
	}
	
	//--------------------------- Builder API ----------------------------//
	
	/**
	 * A class that can build an instance of {@link JulianScriptEngine} with customized settings.
	 * 
	 * @author Ming Zhou
	 */
	public static class Builder {
		
		private EngineInitializationOption option;
		
		// Staged settings
		private Set<String> modulePaths;
		private Map<String, String[]> allowPolicies;
		private Map<String, String[]> denyPolicies;
		private Map<String, Integer> limits;
		private InputStream stdin;
		private OutputStream stdout;
		private OutputStream stderr;
		
		private Builder() {
			option = new EngineInitializationOption();
		}
		
		public static Builder create() {
			return new Builder();
		}
		
		public Builder setIn(InputStream in) {
			stdin = in;
			return this;
		}
	
		public Builder setOut(OutputStream out) {
			stdout = out;
			return this;
		}
	
		public Builder setError(OutputStream err) {
			stderr = err;
			return this;
		}
		
		public Builder addModulePath(String mpath) {
			if (modulePaths == null) {
				modulePaths = new HashSet<>();
			}
			
			modulePaths.add(mpath);
			
			return this;
		}
		
		public Builder setLimit(String name, int limit) {
			if (limits == null) {
				limits = new HashMap<>();
			}
			
			limits.put(name, limit);
			
			return this;
		}
		
		public Builder allow(String category, String... operations) {
			if (allowPolicies == null) {
				allowPolicies = new HashMap<>();
			}
			
			allowPolicies.put(category, operations);
			
			return this;
		}
		
		public Builder deny(String category, String... operations) {
			if (denyPolicies == null) {
				denyPolicies = new HashMap<>();
			}
			
			denyPolicies.put(category, operations);
			
			return this;
		}
		
		public Builder setAllowReentry(boolean value) {
			option.allowReentry = value;
			return this;
		}
	
		public Builder setUseExceptionDefaultHandler(boolean value) {
			option.useExceptionDefaultHandler = value;
			return this;
		}
		
		public Builder setInteractiveMode(boolean value) {
			option.interactiveMode = value;
			return this;
		}

		public Builder setClearUserDefinedTypesOnReentry(boolean value) {
			option.clearUserDefinedTypesOnReentry = value;
			return this;
		}

		public Builder setClearUserBindingsOnExit(boolean value) {
			option.clearUserBindingsOnExit = value;
			return this;
		}
		
		public JulianScriptEngine build() {
			JulianScriptEngine instance = new JulianScriptEngine(option);
			
			if (modulePaths != null && modulePaths.size() > 0) {
				for (String mpath : modulePaths) {
					instance.addModulePath(mpath);
				}
			}
			
			if (allowPolicies != null && allowPolicies.size() > 0) {
				for (Entry<String, String[]> entry : allowPolicies.entrySet()) {
					instance.allow(entry.getKey(), entry.getValue());
				}
			}
	
			if (denyPolicies != null && denyPolicies.size() > 0) {
				for (Entry<String, String[]> entry : denyPolicies.entrySet()) {
					instance.allow(entry.getKey(), entry.getValue());
				}
			}
			
			if (limits != null && limits.size() > 0) {
				for (Entry<String, Integer> entry : limits.entrySet()) {
					instance.setLimit(entry.getKey(), entry.getValue());
				}
			}
			
			if (stdin != null) {
				instance.setInput(stdin);
			}
			
			if (stdout != null) {
				instance.setOutput(stdout);
			}
			
			if (stderr != null) {
				instance.setError(stderr);
			}
			
			return instance;
		}
	}
	
	//--------------------------- Environment Settings ----------------------------//
	
	/**
	 * Add a module path. Duplicate paths are disregarded.
	 * 
	 * @param path The path to a directory for module files.
	 */
	public void addModulePath(String path){
		engine.getContext().addModulePath(path);
	}
	
	/**
	 * Reset the engine. Wipe out all the variables and types.
	 * 
	 * @param pol The reset policy.
	 */
	public void reset(ResetPolicy pol){
		engine.reset(pol);
	}
	
	/**
	 * Allow access to the named category/operations.
	 * <p>
	 * If the operations are not given, allow all operations under this category.
	 * If the category is the wildcard (*), allow everything. This is also the default setting.
	 * <p>
	 * @param category The category of policy, as defined by {@link info.julang.execution.security.PACON}.
	 * @param operations O or more operations of policy, as defined by {@link info.julang.execution.security.PACON}.
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
	 * @param category The category of policy, as defined by {@link info.julang.execution.security.PACON}.
	 * @param operations O or more operations of policy, as defined by {@link info.julang.execution.security.PACON}.
	 */
	public void deny(String category, String... operations){
		engine.getContext().addPolicy(false, category, operations);
	}
	
	/**
	 * Set standard input stream. This method can be called multiple times, but the new value 
	 * will only take effect until the next invocation (e.g. {@link JulianScriptEngine#runFile(String)}).
	 * <p>
	 * This is the input source for <code style="color:green">System.Console.readln()</code>.
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
	 * This is the output sink for <code style="color:green">System.Console.println()</code>.
	 * 
	 * @param output The standard output stream to use within the scripts. If null, default to the platform's standard output.
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
	 * @param error The standard error stream to use within the scripts. If null, default to the platform's standard error.
	 */
	public void setError(OutputStream error) {
		this.error = error;
	}
	
	/**
	 * Set resource utilization limit to the engine.
	 * <p>
	 * Supported names:
	 * <pre>
	 * <code>max.threads</code>
	 * <code>max.used.memory.in.byte</code>
	 * </pre>
	 * @param name Name of the limit.
	 * @param value Value of the limit.
	 */
	public void setLimit(String name, int value) {
		this.engine.setLimit(name, value);
	}
	
	//----------------------------- External Bindings -----------------------------//
	
	public void bindChar(String name, char c){
		engine.getContext().addBinding(name, new CharacterBinding(c));
	}
	
	/**
	 * Get a char binding's value.
	 * 
	 * @param name The name of char variable.
	 * @return the char value
	 * @throws ExternalBindingException If the binding cannot be retrieved.
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
	 * @param name The name of boolean variable.
	 * @return the boolean value
	 * @throws ExternalBindingException If the binding cannot be retrieved.
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
	 * @param name The name of float variable.
	 * @return the float value
	 * @throws ExternalBindingException If the binding cannot be retrieved.
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
	 * @param name The name of int variable.
	 * @return the int value
	 * @throws ExternalBindingException If the binding cannot be retrieved.
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
	 * <p>
	 * If an error occurs, returns null.
	 * 
	 * @param name The name of String variable.
	 * @return the String value
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
	 * <p>
	 * If an error occurs, returns null.
	 * 
	 * @param name The name of String variable.
	 * @param throwOnError if true, will throw null when error occurs.
	 * @return the String value
	 * @throws ExternalBindingException if the binding cannot be retrieved.
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
	 * <p>
	 * If an error occurs, returns null.
	 * 
	 * @param name the name of the bound object
	 * @return The bound object.
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
	 * <p>
	 * If an error occurs, returns null.
	 * 
	 * @param name the name of the bound object
	 * @param throwOnError if true, will throw null when error occurs.
	 * @return The bound object.
	 * @throws ExternalBindingException if the binding cannot be retrieved.
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
	 * @param path the script file's path
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
	 * @param path the script file's path
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
	 * @param script the script content
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
				option.shouldUseExceptionDefaultHandler()){
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
