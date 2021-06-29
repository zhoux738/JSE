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

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtMemoryArea;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.external.interfaces.IExtTypeTable;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.util.Pair;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * A factory to produce Julian Engine and its components using a customized class loader.
 * <p>
 * This class is the bridge between the two worlds. The user class calls the factory to create
 * components and engine. The returned values from these methods can be safely accessed by the
 * caller. The underlying classes of these values, however, are created in the internal class
 * loader. In particular, they are created via specially designed and regimented constructors,
 * which are commented with <b>[CFOW]</b>, a.k.a. "Called From Other World".
 * <p>
 * The classes in this package are the only classes (along with some utility classes) under 
 * package prefix "info.julang" that can be directly referenced from the caller's
 * world. In other words, EngineFactory itself can be safely loaded from the default class 
 * loader, or whatever class loader that loads the user's class. In contrast, any attempt to
 * cast the engine, components, and any of their derivatives that created by this factory to a 
 * more concrete form, such as {@link info.julang.execution.simple.SimpleScriptEngine 
 * SimpleScriptEngine}, would fail due to the two classes not being loaded by the same class loader.
 * 
 * @author Ming Zhou
 */
public class EngineFactory {

	private ClassLoader loader;
	private EngineInitializationOption option;
	
	protected static final String INITIAL_PATH = "info/julang/external/EngineFactory.class";
	protected static final int INITIAL_PATH_LEN = INITIAL_PATH.length();

	/**
	 * Create an engine factory with specified configuration.
	 * 
	 * @param option The option used to created the factory. 
	 */
	public EngineFactory(EngineInitializationOption option){
		this.option = option;
		
		ClassLoader appLoader = EngineFactory.class.getClassLoader();
		URL url = getEngineBinariesPath();
		loader = new EngineComponentClassLoader(new URL[]{url}, appLoader);
	}
	
	/**
	 * Create an engine factory with default configuration (no re-entrance; use default exception handler)
	 */
	public EngineFactory(){
		this(new EngineInitializationOption());
	}
	
	/**
	 * Create a {@link info.julang.execution.simple.SimpleScriptEngine simple engine}
	 * along with its runtime.
	 * 
	 * @return A pair of engine and runtime.
	 */
	@SuppressWarnings("unchecked")
	public EngineParamPair createEngineAndRuntime() {
		try {
			Class<IExtEngineRuntime> rtClass = (Class<IExtEngineRuntime>) loader.loadClass(
				"info.julang.execution.simple.SimpleEngineRuntime");
			Constructor<IExtEngineRuntime> ctor = rtClass.getConstructor(
				IExtMemoryArea.class, 
				IExtVariableTable.class, 
				IExtTypeTable.class, 
				IExtModuleManager.class);
			IExtMemoryArea mem = createHeapMemory();
			IExtEngineRuntime rt = ctor.newInstance(
				mem, createGlobalVariableTable(), createTypeTable(mem), createModuleManager());
			
			Class<IExtScriptEngine> engineClass = (Class<IExtScriptEngine>) loader.loadClass(
				"info.julang.execution.simple.SimpleScriptEngine");
			Constructor<IExtScriptEngine> ctor2 = engineClass.getConstructor(
				IExtEngineRuntime.class,
				EngineInitializationOption.class);
			IExtScriptEngine engine = ctor2.newInstance(
				rt, 
				option);
			return new EngineParamPair(engine, rt);
		} catch (ClassNotFoundException e) {
			throw new JSEError("Cannot load engine class", e);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			throw new JSEError("Cannot instantiate engine class", e);
		} catch (InvocationTargetException e) {
			throw new JSEError("Failed to instantiate engine class", e);
		}
	}
	
	/**
	 * Create a {@link info.julang.execution.simple.SimpleScriptEngine simple engine}.
	 * 
	 * @return An engine instance.
	 */
	public IExtScriptEngine createEngine() {
		return createEngineAndRuntime().getFirst();
	}

	/**
	 * Create a {@link info.julang.modulesystem.ModuleManager module manager}.
	 * 
	 * @return in form of {@link IExtModuleManager}, but in fact a {@link info.julang
	 * .modulesystem.ModuleManager ModuleManager}. But the caller should not attempt to cast outside 
	 * the script engine since the class is loaded from an internal class loader.
	 */
	public IExtModuleManager createModuleManager() {
		return createComponent(
			"info.julang.modulesystem.ModuleManager", 
			"module manager",
			new CtorParamPair[]{
				// Pass the class loader of this class, which resides outside the engine world. The engine users
				// can therefore share the same classes across engine boundary (using mapped class inside engine)
				new CtorParamPair(ClassLoader.class, EngineFactory.class.getClassLoader())
			});
	}

	/**
	 * Create a {@link info.julang.execution.symboltable.TypeTable type table}.
	 * 
	 * @param mem The memory area to be used by this type table.
	 * @return an object that implements {@link IExtTypeTable}.
	 */
	// IMPLEMENTATION NOTES:
	// The returned object is in fact an info.julang.execution.symboltable.TypeTable. But the caller should not
	// attempt to cast outside the script engine since the class is loaded from an internal class loader.
	public IExtTypeTable createTypeTable(IExtMemoryArea mem) {
		return createComponent(
			"info.julang.execution.symboltable.TypeTable", 
			"type table", 
			new CtorParamPair[]{
				new CtorParamPair(IExtMemoryArea.class, mem)
			});
	}

	/**
	 * Create a {@link info.julang.execution.symboltable.VariableTable variable table}.
	 * 
	 * @return an object that implements {@link IExtVariableTable}.
	 */
	// IMPLEMENTATION NOTES:
	// The returned object is in fact an info.julang.execution.symboltable.VariableTable. But the caller should not
	// attempt to cast outside the script engine since the class is loaded from an internal class loader.
	public IExtVariableTable createGlobalVariableTable() {
		return createComponent(
			"info.julang.execution.symboltable.VariableTable", 
			"variable table", 
			new CtorParamPair[]{
				new CtorParamPair(IExtVariableTable.class, null)
			});
	}

	/**
	 * Create a {@link info.julang.memory.simple.SimpleHeapArea heap memory area}.
	 * 
	 * @return an object that implements {@link IExtMemoryArea}.
	 */
	// IMPLEMENTATION NOTES:
	// The returned object is in fact an info.julang.memory.simple.SimpleHeapArea. But the caller should not
	// attempt to cast outside the script engine since the class is loaded from an internal class loader.
	public IExtMemoryArea createHeapMemory() {
		return createComponent(
			"info.julang.memory.simple.SimpleHeapArea", 
			"memory");
	}
	
	
	/**
	 * Get the class path for engine binaries.
	 * <p>
	 * By default, this returns the path of JSE's jar.
	 * 
	 * @return A URL that can be used to initialize an {@link EngineComponentClassLoader}.
	 */
	protected URL getEngineBinariesPath() {
		return EngineFactory.class.getProtectionDomain().getCodeSource().getLocation();
	}
	
	private <T> T createComponent(String fullClassName, String shortName, CtorParamPair... params){
		try {
			@SuppressWarnings("unchecked")
			Class<T> clazz = (Class<T>)loader.loadClass(fullClassName);
			T result;
			if (params == null || params.length == 0){
				result = clazz.getDeclaredConstructor().newInstance();
			} else {
				try {
					int len = params.length;
					Class<?>[] pars = new Class<?>[len];
					Object[] args = new Object[len];
					for(int i = 0; i < len; i++) {
						CtorParamPair p = params[i];
						pars[i] = p.getFirst();
						args[i] = p.getSecond();
					}
					
					Constructor<T> ctor = clazz.getConstructor(pars);
					result = ctor.newInstance(args);
				} catch (NoSuchMethodException | SecurityException | IllegalArgumentException e) {
					throw new JSEError("Cannot instantiate " + shortName + " class", e);
				} catch (InvocationTargetException e) {
					throw new JSEError("Failed to instantiate " + shortName + " class", e);
				}
			}
			
			return result;
		} catch (ClassNotFoundException e) {
			throw new JSEError("Cannot load " + shortName + " class", e);
		} catch (InstantiationException | 
				IllegalArgumentException | 
				IllegalAccessException | 
				NoSuchMethodException | 
				SecurityException | 
				InvocationTargetException e) {
			throw new JSEError("Cannot instantiate " + shortName + " class", e);
		}
	}
	
	private static class CtorParamPair extends Pair<Class<?>, Object> {
		public CtorParamPair(Class<?> t, Object u) {
			super(t, u);
		}
	}
	
	public static class EngineParamPair extends Pair<IExtScriptEngine, IExtEngineRuntime> {
		public EngineParamPair(IExtScriptEngine t, IExtEngineRuntime u) {
			super(t, u);
		}
	}
	
}
