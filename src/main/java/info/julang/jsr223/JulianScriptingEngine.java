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

package info.julang.jsr223;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import info.julang.execution.security.IEnginePolicy;
import info.julang.external.EngineInitializationOption;
import info.julang.external.JulianScriptEngine;
import info.julang.external.binding.IBinding;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.IExtEngineContext;
import info.julang.util.Pair;

/**
 * Julian scripting engine implementation as per JSR-223 (https://www.jcp.org/en/jsr/detail?id=223).
 * <p>
 * To add module paths, call {@link ScriptContext#setAttribute(String, Object, int)} with key = {@link #MODULE_PATHS}.
 * The value must be a String, List or an String array (<code>String[]</code>). If it is a list, only String elements in it
 * will be added as module path.
 * <p>
 * Only {@link ScriptContext#ENGINE_SCOPE} is honored by this engine.
 * <p>
 * The user may use the following attributes to control the engine's behavior:
 * <table style="text-align:left" summary="attributes recognized by JSR-223-compliant Julian scripting engine">
 *   <tr>
 *     <th>Key</th>
 *     <th>Type</th>
 *     <th>Meaning</th>
 *     <th>When not set</th>
 *   </tr>
 *   <tr>
 *     <td>"{@link #MODULE_PATHS JSE_MODULE_PATHS}"</td>
 *     <td>String, String[] or List&lt;String&gt;</td>
 *     <td>The paths to use as module roots.</td>
 *     <td>No external module roots will be used.</td>
 *   </tr>
 *   <tr>
 *     <td>"{@link #ALLOW_POLICIES JSE_ALLOW_POLICIES}"</td>
 *     <td>String, String[] or List&lt;String&gt;</td>
 *     <td>Allow platform access policies. Each string should be in the form of "CATEGORY/OPERATION(,OPERATION)*".</td>
 *     <td>If neither this nor JSE_DENY_POLICIES is set, all policies are allowed.</td>
 *   </tr>
 *   <tr>
 *     <td>"{@link #DENY_POLICIES JSE_DENY_POLICIES}"</td>
 *     <td>String, String[] or List&lt;String&gt;</td>
 *     <td>Deny platform access policies. Each string should be in the form of "CATEGORY/OPERATION(,OPERATION)*".</td>
 *     <td>If neither this nor JSE_ALLOW_POLICIES is set, all policies are allowed.</td>
 *   </tr>
 *   <tr>
 *     <td>"{@link #ALLOW_OBJECT_BINDING JSE_ALLOW_OBJECT_BINDING}"</td>
 *     <td>boolean</td>
 *     <td>Set to allow or deny arbitrary object binding. If not allowed, only a few select types can be bound.</td>
 *     <td>Allow arbitrary object binding.</td>
 *   </tr>
 * </table>
 * This engine runs in REPL mode, therefore the state from the previous runs will be retained.
 * <p>
 * 
 * @author Ming Zhou
 */
public class JulianScriptingEngine extends AbstractScriptEngine {

	/** Use this as the key to set module paths object (of type List/Array) to ScriptContext. */
	public static final String MODULE_PATHS = "JSE_MODULE_PATHS";
	
	/** 
	 * Use this as the key to set a variable of type List&lt;String&gt; or String[] to 
	 * ScriptContext that contains the policies to be explicitly allowed. 
	 * Each element should be in the form of <code>CATEGORY/OPERATION(,OPERATION)*</code>, 
	 * such as "System.Environment/read", or "System.IO/read,write".
	 * <p>
	 * By default, all policies are allowed.
	 */
	public static final String ALLOW_POLICIES = "JSE_ALLOW_POLICIES";
	
	/** 
	 * Use this as the key to set a variable of type List&lt;String&gt; or String[] to 
	 * ScriptContext that contains the policies to be explicitly denied. 
	 * Each element should be in the form of <code>CATEGORY/OPERATION(,OPERATION)*</code>,
	 * such as "System.Environment/read", or "System.IO/read,write".
	 * <p>
	 * By default, all policies are allowed.
	 */
	public static final String DENY_POLICIES = "JSE_DENY_POLICIES";
	

	/** 
	 * If set to true, allow arbitrary object binding. Otherwise, only certain primitive 
	 * types and String can be bound. Beware of the overhead - if enabled, this will cause 
	 * the engine to inspect the bound types and map them to internal representation.
	 * <p>
	 * By default, all policies are allowed.
	 */
	public static final String ALLOW_OBJECT_BINDING = "JSE_ALLOW_OBJECT_BINDING";
	
	private JulianScriptEngineInternal jse;
	
	public JulianScriptingEngine() {
		jse = new JulianScriptEngineInternal();
	}
	
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		processContext(context);
		
		try {
			Object result = null;
			try {
				result = jse.runScript(script, new String[0]);
			} finally {
				// Whatever happened, let's update the bindings first
				updateBindings(context);
			}
			
			return result;
		} catch (JSEException e) {
			Throwable cause = e.getCause();
			if (cause instanceof ScriptException) {
				throw (ScriptException)cause;
			} else {
				throw new ScriptException(e);
			}
		}
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		int size = 8192;
	    int read = 0;
	    char[] arr = new char[size];
	    StringBuilder buffer = new StringBuilder();
	    try {
			while ((read = reader.read(arr, 0, size)) != -1) {
			    buffer.append(arr, 0, read);
			}
		} catch (IOException e) {
			throw new ScriptException(e);
		}
	    
	    String script = buffer.toString();	
		return eval(script, context);
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	private JulianScriptingEngineFactory factory;
	
	void setEngineFactory(JulianScriptingEngineFactory factory) {
		this.factory = factory;
	}
	
	private List<Pair<String, String[]>> processPolicies(Object obj){
		List<Pair<String, String[]>> lst = null;
		if(obj instanceof List){
			lst = new ArrayList<Pair<String, String[]>>();
			@SuppressWarnings("rawtypes")
			List polList = (List)obj;
			for(Object ele : polList){
				if(ele instanceof String){
					String polStr = (String) ele;
					String[] sections = polStr.split("/");
					if (sections.length == 2) {
						if (IEnginePolicy.WILDCARD.equals(sections[0].trim())) {
							lst.add(new Pair<String, String[]>(IEnginePolicy.WILDCARD, null));
						} else {
							lst.add(new Pair<String, String[]>(sections[0], sections[1].split(",")));
						}
					} else if (sections.length == 1) {
						if (IEnginePolicy.WILDCARD.equals(sections[0].trim())) {
							lst.add(new Pair<String, String[]>(IEnginePolicy.WILDCARD, null));
						} else {
							lst.add(new Pair<String, String[]>(sections[0], new String[] { IEnginePolicy.WILDCARD }));
						}
					}			
				}
			}
		} else if (obj instanceof String[]){
			List<String> slist = new ArrayList<String>();
			for (String s : (String[])obj) {
				slist.add(s);
			}
			
			lst = processPolicies(slist);
		} else if (obj instanceof String){
			List<String> slist = new ArrayList<String>();
			slist.add((String)obj);
			lst = processPolicies(slist);
		}

		return lst.size() > 0 ? lst : null;
	}
	
	private void processContext(ScriptContext context){
		if(context == null){
			return;
		}
		
		// Set module paths
		Object obj1 = context.getAttribute(MODULE_PATHS);
		if(obj1 != null){
			if(obj1 instanceof List){
				@SuppressWarnings("rawtypes")
				List modList = (List)obj1;
				for(Object ele : modList){
					if(ele instanceof String){
						String modPath = (String) ele;
						jse.addModulePath(modPath);				
					}
				}
			} else if (obj1 instanceof String[]){
				String[] arr = (String[]) obj1;
				for(Object ele : arr){
					String modPath = (String) ele;
					jse.addModulePath(modPath);
				}
			} else if (obj1 instanceof String) {
				String modPath = (String) obj1;
				jse.addModulePath(modPath);
			}
		}
		
		// Set policies
		Object obj2 = context.getAttribute(ALLOW_POLICIES);
		if (obj2 != null) {
			List<Pair<String, String[]>> pairs = processPolicies(obj2);
			if (pairs != null) {
				for (Pair<String, String[]> pair : pairs) {
					jse.allow(pair.getFirst(), pair.getSecond());
				}
			}
		}
		obj2 = context.getAttribute(DENY_POLICIES);
		if(obj2 != null){
			List<Pair<String, String[]>> pairs = processPolicies(obj2);
			if (pairs != null) {
				for (Pair<String, String[]> pair : pairs) {
					jse.deny(pair.getFirst(), pair.getSecond());
				}
			}
		}
		
		boolean requiresObjBinding = false;
		boolean allowObjBinding = true;
		
		// Convert bindings (ignore GLOBAL_SCOPE)
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		Set<Entry<String, Object>> set = bindings.entrySet();
		for(Entry<String, Object> entry : set){
			String key = entry.getKey();
			Object ov = entry.getValue();
			if(ov != null){
				if(ov instanceof String){
					String value = (String) ov;
					jse.bindString(key, value);
				} else if (ov instanceof Integer){
					int value = (int) ov;
					jse.bindInt(key, value);
				} else if (ov instanceof Boolean){
					boolean value = (boolean) ov;
					jse.bindBool(key, value);
				} else if (ov instanceof Character){
					char value = (char) ov;
					jse.bindChar(key, value);
				} else if (ov instanceof Float){
					float value = (float) ov;
					jse.bindFloat(key, value);
				} else {
					// Allow arbitrary object binding?
					if (!requiresObjBinding) {
						requiresObjBinding = true;
						Object obj3 = context.getAttribute(ALLOW_OBJECT_BINDING);
						if(obj3 != null && obj3 instanceof Boolean){
							allowObjBinding = ((Boolean)obj3).booleanValue();
						}
					}
					
					if (allowObjBinding) {
						jse.bindObject(key, ov);
					}
				}
			}
		}
	}
	
	private void updateBindings(ScriptContext context) {
		IExtEngineContext engineContext = jse.getEngineContext();
		
		Bindings bindings = context.getBindings(ScriptContext.ENGINE_SCOPE);
		Set<Entry<String, Object>> set = bindings.entrySet();
		for(Entry<String, Object> entry : set){
			String key = entry.getKey();
			IBinding binding = engineContext.getBinding(key);
			if(binding != null){
				Object obj = binding.toExternal();
				entry.setValue(obj);
			}
		}
	}
	
	private static class JulianScriptEngineInternal extends JulianScriptEngine {
		
		private JulianScriptEngineInternal(){
			super(new EngineInitializationOption(true, true, false));
		}
		
		private IExtEngineContext getEngineContext(){
			return (IExtEngineContext)engine.getContext();
		}
		
	}

}
