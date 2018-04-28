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

package info.julang.hosting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import info.julang.hosting.attributes.MappedHostedAttribute;
import info.julang.hosting.mapped.IllegalTypeMappingException;
import info.julang.hosting.mapped.inspect.MappedTypeInfo;
import info.julang.hosting.mapped.inspect.PlatformTypeMapper;
import info.julang.memory.value.AttrValue;
import info.julang.modulesystem.naming.FQName;
import info.julang.typesystem.PlatformType;
import info.julang.typesystem.jclass.annotation.IllegalAttributeUsageException;
import info.julang.typesystem.jclass.jufc.System.DateTime;
import info.julang.typesystem.jclass.jufc.System.JConsole;
import info.julang.typesystem.jclass.jufc.System.JProcess;
import info.julang.typesystem.jclass.jufc.System.ProcessPipeStream;
import info.julang.typesystem.jclass.jufc.System.Collection.JList;
import info.julang.typesystem.jclass.jufc.System.Collection.JMap;
import info.julang.typesystem.jclass.jufc.System.Collection.JQueue;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptLock;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread;
import info.julang.typesystem.jclass.jufc.System.IO.JDirectory;
import info.julang.typesystem.jclass.jufc.System.IO.JFile;
import info.julang.typesystem.jclass.jufc.System.IO.JFileStream;
import info.julang.typesystem.jclass.jufc.System.Util.JMath;
import info.julang.util.Pair;

/**
 * A manager of all the registered hosted methods.
 * 
 * @author Ming Zhou
 */
public class HostedMethodManager {
	
	private ClassLoader extLoader;
	
	public HostedMethodManager(ClassLoader extLoader){
		factories = new ConcurrentHashMap<String, HostedMethodProviderFactory>();
		for(HostedMethodProviderFactory factory : BuiltInProviderFactories){
			register(factory.getApiSet(), factory);
		}
		this.extLoader = extLoader;
	}
	
	//-------------------- Bridged type management -------------------//
	
	private Map<String, HostedMethodProviderFactory> factories;
	
	// All the built-in hosted method providers.
	// TODO - make this part auto-generated from juleng
	private static HostedMethodProviderFactory[] BuiltInProviderFactories = 
		new HostedMethodProviderFactory[]{
			JFile.Factory,
			JDirectory.Factory,
			JConsole.Factory,
			JList.Factory,
			JMap.Factory,
			JQueue.Factory,
			ScriptThread.Factory,
			ScriptLock.Factory,
			DateTime.Factory,
			JFileStream.Factory,
			ProcessPipeStream.Factory,
			JProcess.Factory,
			JMath.Factory
		};
	
	public void register(String apiset, HostedMethodProviderFactory factory){
		factories.put(apiset, factory);
	}
	
	public IHostedMethodProvider find(String apiset){
		// getProvider() will initialize the provider.
		return factories.get(apiset).getProvider();
	}

	//-------------------- Mapped type management --------------------//

	// <Platform full name ~ script engine type full name / mapped type info>
	private Map<String, Pair<FQName, MappedTypeInfo>> mapped; 
	private Map<String, PlatformType> platformTypes; 
	private PlatformTypeMapper mapper;
	
	/**
	 * Load a platform type into the system, and register it with the given script type name.
	 * <p>
	 * This class uses the external class loader, which was passed in when the engine was created. This 
	 * way the mapped classes are living in the same world as other classes outside the script engine.
	 * 
	 * @param fqName script type name
	 * @param isClass if true, the type is a class, which must be mapping to a class; if false, an interface.
	 * @param hattr annotation containing the platform type to be loaded
	 * @return a {@link MappedTypeInfo} object that contains the detailed information on the platform class.
	 * @throws IllegalAttributeUsageException if the same platform type has been mapped before. 
	 */
	public synchronized MappedTypeInfo mapPlatformType(FQName fqName, boolean isClass, MappedHostedAttribute hattr, AttrValue av) throws IllegalTypeMappingException {
		if (mapper == null) {
			mapper = new PlatformTypeMapper();
			mapped = new HashMap<String, Pair<FQName, MappedTypeInfo>>();
		}
		
		String pcName = hattr.getClassName();
		checkName(pcName);
		Pair<FQName, MappedTypeInfo> existing = mapped.get(pcName);
		if (existing != null) { 
			// If fqName == existing, we should allow. But it should never happen as it's a bug by itself 
			// and will cause issue somewhere else in the system. So don't bother checking equality.
			throw new IllegalAttributeUsageException(
				"The platform type " + pcName + " is being mapped to more than one script type: " + existing.getFirst() + ", " + fqName);
		}
		
		MappedTypeInfo mti = mapper.mapType(extLoader, hattr.getClassName(), av, isClass);
		
		mapped.put(pcName, new Pair<FQName, MappedTypeInfo>(fqName, mti));
		return mti;
	}

	public Class<?> preloadPlatformClass(String pcname) {
		try {
			checkName(pcname);
			return extLoader.loadClass(pcname);
		} catch (Exception e) {
			return null;
		}
	}
	
	public synchronized PlatformType getPlatformType(String name) {
		if (platformTypes == null) {
			platformTypes = new HashMap<String, PlatformType>();
		}

		PlatformType pt = platformTypes.get(name);
		if (pt == null) {
			pt = new PlatformType(this, name);
			platformTypes.put(name, pt);
		}
		
		return pt;
	}
	
	/**
	 * Get the script type mapped from a platform full name.
	 *  
	 * @param platformTypeFullName
	 * @return null if the given platform type has never been mapped.
	 */
	public FQName getMappedTypeName(String platformTypeFullName){
		if (mapped != null) {
			Pair<FQName, MappedTypeInfo> pair = mapped.get(platformTypeFullName);
			if (pair != null){
				return pair.getFirst();
			}
		}
		
		return null;
	}
	
	/**
	 * Get the platform class mapped from a platform full name.
	 *  
	 * @param platformTypeFullName
	 * @return null if the given platform type has never been mapped.
	 */
	public Class<?> getMappedPlatformClass(String platformTypeFullName){
		if (mapped != null) {
			Pair<FQName, MappedTypeInfo> pair = mapped.get(platformTypeFullName);
			if (pair != null){
				return pair.getSecond().getPlatformClass();
			}
		}
		
		return null;
	}

	/**
	 * Remove the platform class mapped from a platform full name.
	 * 
	 * @param platformTypeFullName
	 */
	public synchronized void removeMappedPlatformClass(String platformTypeFullName) {
		if (mapped != null) {
			mapped.remove(platformTypeFullName);
		}
	}
	
	private void checkName(String pcName) throws IllegalTypeMappingException {
		if (pcName.startsWith("info.julang.")) {
			throw new IllegalTypeMappingException(pcName, "Mapping to Julian internals is disallowed.");
		}
	}
	
}
