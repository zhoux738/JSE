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

package info.julang.modulesystem;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.security.CheckResultKind;
import info.julang.execution.security.EngineLimit;
import info.julang.execution.security.EngineLimitPolicy;
import info.julang.execution.security.EnginePolicyEnforcer;
import info.julang.execution.security.IEnginePolicy;
import info.julang.execution.security.PlatformAccessPolicy;
import info.julang.execution.security.StatefulEngineLimitPolicy;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.SymbolDuplicatedDefinitionException;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThread.MonitorInterruptCondition;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodManager;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.ModuleInfo.DuplicateClassInfoException;
import info.julang.modulesystem.ModuleInfo.MutableModuleInfo;
import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.prescanning.CollectScriptInfoStatement;
import info.julang.modulesystem.prescanning.IRawScriptInfo;
import info.julang.modulesystem.prescanning.IllegalModuleFileException;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.jclass.jufc.SystemRawScriptInfoLoader;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;
import info.julang.typesystem.loading.InternalTypeResolver;
import info.julang.util.OneOrMoreList;
import info.julang.util.Pair;

/**
 * The module manager is a globally shared runtime object for module management. Its main duties are:
 * <ul>
 * <li>Loading a specified module and all of required modules (those the interested one depends on).</li>
 * <li>Maintaining a cache of all the modules loaded.</li>
 * </ul>
 * <p>
 * Generally, when the engine encounters a module import declaration (such as "<code><b>import</b> A.B.C;</code>") 
 * it will load the module, as well as all the requirements, using this class. The ModuleManager maintains an 
 * internal cache that will track all the loaded modules, so that it won't load a given module more than once.
 * <p>
 * A key property of this class is that for any loaded modules, it is guaranteed that all of its required modules
 * are also loaded. This is called <b>Loading Completeness Principle</b> (LCP). 
 * <p>
 * In addition to managing regular modules, this class also carries a few related responsibilities:
 * <ul>
 * <li>Through {@link HostedMethodManager}, managing mapped platform API.</li>
 * <li>Through {@link EnginePolicyEnforcer}, safeguarding access to the platform resources.</li>
 * </ul>
 * This class is expected to be called during pre-interpretation stage where all the script files are pre-scanned 
 * for collecting type information. In future it may be also called during dynamic scripting.
 * <p>
 * @author Ming Zhou
 */
public class ModuleManager implements IModuleManager {

	// Concurrency control
	private Object lock = new Object();
	private Thread owningThread;
	
	// Module management
	private Map<String, ModuleInfo> cache;
	private ModuleLocator locator;
	/** Keyed by simple name (<b>NOT</b> fully qualified). */
	private Map<String, OneOrMoreList<ClassInfo>> allClasses; 
	private Set<String> allUserDefinedClasses;
	
	// Platform API mapping
	private HostedMethodManager hmm;
	private ClassLoader extLoader;
	
	// Policy enforcement
	private EnginePolicyEnforcer policyEnforcer;
	private Map<String, Pair<String[], String[]>> policies;
	private CheckResultKind checkKind = CheckResultKind.UNDEFINED;
	
	/**
	 * [CFOW]
	 */
	public ModuleManager(ClassLoader extLoader){
		cache = new HashMap<String, ModuleInfo>();
		locator = new ModuleLocator();
		allClasses = new HashMap<String, OneOrMoreList<ClassInfo>>();
		this.extLoader = extLoader;
	}
	
	public ModuleManager(){
		this(ModuleManager.class.getClassLoader());
	}
	
	/**
	 * Clear all user-defined modules. JuFC modules will be preserved.
	 */
	public synchronized void clearAllUserDefinedModules() {
		Set<String> mNames = new HashSet<>(cache != null ? cache.size() : 0);
		Set<String> cNames = new HashSet<>(allClasses != null ? allClasses.size() : 0);
		
		for (String mName : cache.keySet()) {
			if (isSystemModule(mName)) {
				continue;
			} else {
				mNames.add(mName);
			}
		}

		if (allUserDefinedClasses != null) {
			for (String cName : allClasses.keySet()) {
				if (allUserDefinedClasses.contains(cName)) {
					cNames.add(cName);
				}
			}
		}
		
		if (cache != null) {
			for (String mName : mNames) {
				cache.remove(mName);
			}
		}
		
		if (allClasses != null) {
			for (String cName : cNames) {
				OneOrMoreList<ClassInfo> clist = allClasses.get(cName);
				if (clist == null || clist.size() == 0) {
					// Abnormal case. But can be safely ignored here.
					continue;
				}
				
				if (clist.hasOnlyOne()) {
					// This single class must be the user-defined one. Remove the whole entry
					allClasses.remove(cName);
					continue;
				}
				
				// More complicated case - a simple name is used by more than one classes, and it's possible
				// some of them are JuFC.
				
				OneOrMoreList<ClassInfo> sysClasses = null;
				for (ClassInfo ci : clist) {
					if (TypeTable.isSystemType(ci.getFQName())) {
						if (sysClasses == null) {
							sysClasses = new OneOrMoreList<ClassInfo>(ci);
						} else {
							sysClasses.add(ci);
						}
					}
				}
				
				if (sysClasses != null) {
					// Replace with a pure JuFC list
					allClasses.put(cName, sysClasses);
				} else {
					// All existing classes are user-defined. They can all go.
					allClasses.remove(cName);
				}
			}
		}
		
		// We don't know any user-defined types anymore.
		if (allUserDefinedClasses != null) {
			allUserDefinedClasses.clear();
		}
		
		// Hosted mappings can all go since JuFC doesn't use it.
		if (hmm != null) {
			hmm.clearAllMappedTypes();
		}
	}
	
	private static boolean isSystemModule(String modName) {
		return modName != null && ("System".equals(modName) || modName.startsWith("System."));
	}
	
	/**
	 * Get a list of classes info which has the given name as the not fully qualified name.
	 * 
	 * @param name
	 * @return
	 */
	public List<ClassInfo> getClassesByNFQName(String name){
		OneOrMoreList<ClassInfo> list = allClasses.get(name);
		if(list != null){
			return list.getList();
		} else {
			return new ArrayList<ClassInfo>();
		}
	}
	
	/**
	 * Get the class info by a fully qualified name.
	 * 
	 * @param name
	 * @return null if no class with the specified name is found.
	 */
	public ClassInfo getClassesByFQName(String name){
		FQName fqname = new FQName(name);
		OneOrMoreList<ClassInfo> list = allClasses.get(fqname.getSimpleName());
		if(list != null){
			for(ClassInfo ci : list){
				if(ci.getFQName().equals(name)){
					return ci;
				}
			}
		}
		
		return null;
	}
	
	public void addModulePath(String path) {
		locator.addModulePath(path);
	}
	
	public void clearExecutionData() {
		locator.clearModulePath();
		args = null;
		gsEvalCache = null;
	}
	
	public boolean isLoaded(String moduleName){
		synchronized(lock){
			return cache.containsKey(moduleName);
		}
	}
	
	/**
	 * Load a module of specified name. 
	 * <p>
	 * This method will first locate the script files belonging to the specified module, then
	 * parse those scripts to collect information about the contained types. The method will
	 * also apply the same procedure to any other required modules encountered along the way.
	 * By the time this method returns it is guaranteed that the entire module closure is 
	 * loaded (LCP).
	 * <p>
	 * This method is thread safe. Calling it from two competing threads won't result in the 
	 * module being loaded twice.
	 * <p>
	 * 
	 * @param moduleName
	 * @return
	 */
	public ModuleInfo loadModule(JThread thread, String moduleName){
		ModuleInfo info = null;
		synchronized(lock){
			// Return now if this module is already loaded
			info = cache.get(moduleName);
			if(info!=null){
				return info;
			}
			
			// Acquire the lock in preparation for loading
			while(!secureLock()){
				thread.safeWait(lock, condition);
			}
			
			// Check cache again. Return if this module is already loaded
			info = cache.get(moduleName);
			if(info!=null){
				releaseLock();
				return info;
			}
		}
		
		try {
			info = loadModule0(moduleName, true);
		} catch (FileNotFoundException f) {
			releaseLock();
			synchronized(lock){
				lock.notifyAll();
			}
			
			JSEIOException ioe = new JSEIOException("Couldn't find script file. (" + f.getMessage() + ")");
			throw ioe;
		} catch (Exception ex) {
			releaseLock();
			synchronized(lock){
				lock.notifyAll();
			}
			
			throw ex;
		}
		
		synchronized(lock){
			cache.put(moduleName, info);
			releaseLock();
			lock.notifyAll();
		}
		
		return info;
	}
	
	/**
	 * Load a script as module. The module name is specified by <code>mode</code> argument.
	 * <p>
	 * This method is used for a couple of different purposes. Most commonly, when the engine starts, it begins with an 
	 * entrance script, which needs to be loaded as a default module named {@link ModuleInfo#DEFAULT_MODULE_NAME}.
	 * <p>
	 * The default module is different from other modules in its mutability. Through include statement or other means more
	 * types may be discovered and added to the default module. By specifying <code>ScriptModuleLoadingMode == 
	 * AccumulativeGlobalScript</code> this can be achieved. However, if a type to be added conflicts with the existing 
	 * type, an exception will be thrown.
	 * <p>
	 * In interactive mode, which is mostly associated with REPL, every time a new code snippet is processed it will be
	 * loaded as a default module as well. This module will replace the existing one, thus replacing all types contained
	 * therein. This is achieved by specifying <code>ScriptModuleLoadingMode == SubstitutiveGlobalScript</code>. Values 
	 * which have been created off of the old types will remain functioning, although their integrity might be compromised.
	 * <p>
	 * The last usage relates to implicit type binding. When the platform objects are added to the engine's external bindings,
	 * the engine automatically creates mapped JSE types corresponding to these object's native types. It does so by 
	 * synthesizing a Julian script containing appropriate type definitions with required platform mapping annotations.
	 * This script bears the module name {@link ModuleInfo#IMPLICIT_MODULE_NAME}, and will be loaded by this method as well,
	 * with <code>ScriptModuleLoadingMode == ImplicitModuleScript</code>.
	 * 
	 * @param rt Thread runtime
	 * @param ainfo AST info
	 * @param mode The loading mode. This mostly affects how the types discovered in the new module are 
	 * treated with regards to the types in the existing module.
	 * @return The module info, which has already been added to the module cache.
	 */
	public ModuleInfo loadScriptAsModule(
		ThreadRuntime rt, LazyAstInfo ainfo, ScriptModuleLoadingMode mode) {
		synchronized(lock){
			while(!secureLock()){
				rt.getJThread().safeWait(lock, condition);
			}
		}
		
		String synModuleName = mode.getModuleName();
		
		RawScriptInfo info = new RawScriptInfo(null, false);// Don't know the module name yet.
		info.reset(ainfo.getFileName(), ainfo);
		RawScriptInfo.Option option = info.getOption();
		option.setPresetModuleName(synModuleName);
		option.setAllowNameInconsistency(true);
		try {
			CollectScriptInfoStatement csis = new CollectScriptInfoStatement(true, true); // Load AST now
			csis.prescan(info);
		} catch (IllegalModuleFileException mex) {
			throw new IllegalModuleFileException(
				info, ainfo.getFileName(), 1, "A loose script file must not declare a module name.");
		}
		
		// If the file can be pre-scanned successfully, we should expect the module to be equal to what is given, if it's given.
		String gModName = info.getModuleName();
		if (synModuleName != null
			&& gModName != null
			&& !synModuleName.equals(gModName)) {
			throw new IllegalModuleFileException(
				info, ainfo.getFileName(), 1, "A loose script file must not declare a module name.");
		}
		
		// Load each required module
		Map<String, ModuleInfo> deps = new HashMap<String, ModuleInfo>();
		for(RequirementInfo req : info.getRequirements()){
			String moduleName = req.getName();
			ModuleInfo dep = cache.get(moduleName);
			if(dep == null){
				try {
					dep = loadModule0(moduleName, true);
				} catch (FileNotFoundException f) {
					JSEIOException ioe = new JSEIOException("Couldn't find script file. (" + f.getMessage() + ")");
					releaseLock();
					throw ioe;
				} catch (Exception ex) {
					releaseLock();
					throw ex;
				}
			}
			cache.put(moduleName, dep);
			
			deps.put(req.getName(), dep);
		}
		
		// The order of these steps are delicate. Since there is only one global module
		// at any time, we must first obtain a handle to the current one, then add the
		// new one, which at the end is to be replaced by a merged set of the two. We 
		// also need perform various operations between these steps.
		MutableModuleInfo mmi = makeModuleInfo(info);
		ModuleInfo prev = cache.get(gModName);
		
		boolean accumulative = mode == ScriptModuleLoadingMode.AccumulativeGlobalScript;
		if (accumulative) {
			try {
				mmi.addFrom(prev);
			} catch (DuplicateClassInfoException e) {
				throw new SymbolDuplicatedDefinitionException(e.newClass, mmi);
			}
		}
		
		Map<String, MutableModuleInfo> tempCache = new HashMap<String, MutableModuleInfo>();
		tempCache.put(gModName, mmi);
		populateDependencies(tempCache, accumulative);
		
		try {
			if (mode.shouldLoadImmediately()) {
				// Get new types
				ModuleInfo curr = cache.get(gModName);
				List<ClassInfo> cis = curr.getClasses();

				// If existing is not null, merge this with the new one
				if (prev != null){
					curr.mergeFrom(prev);
				}

				// If any new types are defined, fully load them into runtime
				if (cis != null && !cis.isEmpty()){
					// As of 0.2.0, we do not support class overwriting. Simply load what we could until the first failure.
					InternalTypeResolver resolver = rt.getTypeResolver();
					ITypeTable tt = rt.getTypeTable();
					
					// (1) check if any type exists, abort if so
					Set<ClassInfo> existing = null;
					for(ClassInfo ci : cis){
						JType t = tt.getType(ci.getFQName());
						if (t != null) {
							if (existing == null){
								existing = new HashSet<ClassInfo>();
							}
							existing.add(ci);
						}
					}
					
					if (existing != null) {
						System.err.println("The following types are already defined. None of the types are loaded.");
						for (ClassInfo ci : existing){
							System.err.println("  " + ci.getFQName());
						}
					}
					
					// (2) load each new type until the first failure
					Context cntx = Context.createSystemLoadingContext(rt);
					Set<ClassInfo> loaded = new HashSet<ClassInfo>();
					boolean succ = true;
					for(ClassInfo ci : cis){
						try {
							resolver.resolveType(cntx, ParsedTypeName.makeFromFullName(ci.getFQName()), true);
						} catch (UnknownTypeException ex) {
							System.err.print("Couldn't load type " + ci.getFQName());
							succ = false;
							break;
						}
						
						loaded.add(ci);
					}
					
					// (3) typed loaded will remain in runtime, the type that failed to load
					//     and all the following types are ignored.
					if (!succ){
						System.err.println(loaded.size() > 0 ? ". Only the following types are successfully loaded:" : ".");
						for (ClassInfo ci : loaded){
							System.err.println("  " + ci.getFQName());
						}
					}
				}
			}		
		} finally {
			synchronized(lock){
				releaseLock();
				lock.notifyAll();
			}
		}
		
		return mmi;
	}
	
	private ModuleInfo loadModule0(String moduleName, boolean loadRequirements) throws FileNotFoundException {
		// A cache that contains all the new module info.
		Map<String, MutableModuleInfo> tempCache = new HashMap<String, MutableModuleInfo>();
		
		// A set that tracks modules to be loaded, initially containing only the starting module.
		Set<String> modsToLoad = new HashSet<String>();
		modsToLoad.add(moduleName);
		
		ModuleInfo result = null;
		boolean firstTime = true;
		while(!modsToLoad.isEmpty()){
			Set<String> newMods = new HashSet<String>();
			List<String> toRemove = new ArrayList<>();
			for(String mod : modsToLoad){
				// For a module that has not been loaded.
				
				// Get all script files for this module,
				ModuleLocationInfo mli = locator.findModuleFiles(mod);
				
				if(!mli.isFound()){
					throw new MissingRequirementException(mod, mli);
				}
				
				MutableModuleInfo mi = makeModuleInfo(mod, mli);
				
				if(firstTime){
					result = mi;
					firstTime = false;
				}
				
				// Memorize all of the required modules,
				if(loadRequirements){
					for(String req : mi.getRequiredModuleNames()){
						if(!isLoaded(req) && !tempCache.containsKey(req)){
							// Found a new module
							newMods.add(req);
						}
					}		
				}

				// Put it into temporary cache.
				tempCache.put(mod, mi);
				toRemove.add(mod);
			}
			
			// Update modules to load: remove the loaded, add the newly found
			modsToLoad.removeAll(toRemove);
			modsToLoad.addAll(newMods);
		}
		
		populateDependencies(tempCache, false);
		
		return result;
	}

	/**
	 * Create a {@link ModuleInfo} instance based on the information collected from a set of scripts files, including
	 * requirements, script provider and declared classes.
	 */
	private static MutableModuleInfo makeModuleInfo(
		String modName, ModuleLocationInfo mli) throws FileNotFoundException {
		List<String> scriptPaths = mli.getScriptPaths();
		boolean isEmbedded = mli.isEmbedded();
		
		ModuleInfo.Builder builder = new ModuleInfo.Builder(modName);
		//RawScriptInfo info = new RawScriptInfo(modName, isEmbedded);
		
		for(String path : scriptPaths){
			boolean isFromCustomizedModulePath = mli.isFromCustomizedModulePath(path);
			RawScriptInfo info = loadModuleInfo(modName, path, isEmbedded, false, !isFromCustomizedModulePath);
			
			// Add script info
			builder.addScript(info);
		}
		
		return builder.build();
	}
	
	private static RawScriptInfo loadModuleInfo(
		String modName, String path, boolean isEmbedded, boolean analyticalLoad, boolean allowImplicitModuleName) 
		throws FileNotFoundException {
		
		RawScriptInfo info = null;
		if (isEmbedded) {
			info = SystemRawScriptInfoLoader.INSTANCE.getRawScriptInfo(path);
		} else {
			info = new RawScriptInfo(modName, isEmbedded);
			info.initialize(path);
			
			if (analyticalLoad) {
				RawScriptInfo.Option opt = info.getOption();
				opt.setAllowNameInconsistency(true);
				opt.setAllowSystemModule(true); // So that we can use this for developing Julian itself.
				opt.setPresetModuleName(ModuleInfo.DEFAULT_MODULE_NAME);
			}
			
			CollectScriptInfoStatement csis = new CollectScriptInfoStatement(analyticalLoad, allowImplicitModuleName);
			csis.prescan(info);
		}
		
		return info;
	}
	
	/**
	 * Try to load script info from the specified path. This will cause some generation of AST for each type.
	 * <p>
	 * This method is completely stateless. It won't cause any change to the global environment and 
	 * it doesn't use any contextual object either. Its main use is for external tools such as IDE.
	 * 
	 * @param modName The module's name. The file at the specified path is expected to start with 
	 * "<code>import {modName};</code>", otherwise this method will throw.
	 * @param path The path to the script to load.
	 * @return The {@link RawScriptInfo} object describing the script file at the specified path.
	 * @throws FileNotFoundException
	 */
	public static RawScriptInfo loadScriptInfoFromPath(String modName, String path) 
		throws FileNotFoundException {
		return loadModuleInfo(modName, path, false, true, true);
	}

	/**
	 * Populate module dependency information, then add the new modules into the global cache.
	 * 
	 * @param tempCache
	 * @param accumulative
	 */
	private void populateDependencies(Map<String, MutableModuleInfo> tempCache, boolean accumulative) {
		Collection<MutableModuleInfo> newModules = tempCache.values();
		for(MutableModuleInfo mmi : newModules){
			List<ModuleInfo> reqs = new ArrayList<ModuleInfo>();
			for(String reqName : mmi.getRequiredModuleNames()){
				// First try to find from newly found modules.
				ModuleInfo mod = tempCache.get(reqName);
				if(mod == null){
					// If failed, find it from already defined modules.
					mod = cache.get(reqName);
				}
				if(mod == null){
					throw new MissingRequirementException(reqName, null);
				}
				reqs.add(mod);
			}
			
			// Populate requirements
			if (accumulative) {
				Set<ModuleInfo> miSet = new HashSet<ModuleInfo>();
				List<ModuleInfo> miList = mmi.getRequirements();
				miSet.addAll(miList);
				miSet.addAll(reqs); // Skip the duplicate
				List<ModuleInfo> miListNew = new ArrayList<>();
				miListNew.addAll(miSet);
				mmi.replaceRequirements(miListNew);
			} else {
				for (ModuleInfo req : reqs) {
					mmi.addRequiredModule(req);
				}
			}
			
			// Add classes to global cache
			boolean isUserModule = !isSystemModule(mmi.getName());
			
			if (accumulative) {
				// The following logic is only efficient if there is only one new module, which is
				// exactly the case where accumulative is true, while the module is loaded from 
				// the loose script.
				Map<ClassInfo, Set<ClassInfo>> cinfoMap = new HashMap<>();
				
				// Gather classes from existing and new module info
				for(ClassInfo ci : mmi.getClasses()){
					Set<ClassInfo> ciSet = cinfoMap.get(ci);
					if (ciSet == null) {
						ciSet = new HashSet<>();
						OneOrMoreList<ClassInfo> list = allClasses.get(ci.getName());
						if (list != null) {
							ciSet.addAll(list.getList());
						}
						cinfoMap.put(ci, ciSet);
					}
					
					ciSet.add(ci);
				}
				
				// Replace classes stored in global cache
				for (Entry<ClassInfo, Set<ClassInfo>> entry : cinfoMap.entrySet()) {
					String name = entry.getKey().getName();
					OneOrMoreList<ClassInfo> value = new OneOrMoreList<>(entry.getValue());
					allClasses.put(name, value);
					
					if (isUserModule) {
						if (allUserDefinedClasses == null) {
							allUserDefinedClasses = new HashSet<>();
						}
						
						allUserDefinedClasses.add(name);
					}
				}
			} else {
				for(ClassInfo ci : mmi.getClasses()){
					String name = ci.getName();
					
					OneOrMoreList<ClassInfo> list = allClasses.get(name);
					if(list == null){
						allClasses.put(name, new OneOrMoreList<ClassInfo>(ci));
					} else {
						list.add(ci);
					}
					
					if (isUserModule) {
						if (allUserDefinedClasses == null) {
							allUserDefinedClasses = new HashSet<>();
						}
						
						allUserDefinedClasses.add(name);
					}
				}
			}
		}
		
		// Add all the new modules into the cache in a single move (LCP)
		// Note this will overwrite the previous existing global script module
		cache.putAll(tempCache);
	}
	
	private MutableModuleInfo makeModuleInfo(IRawScriptInfo scriptInfo) {
		ModuleInfo.Builder builder = new ModuleInfo.Builder(scriptInfo.getModuleName());

		// Add script info
		builder.addScript(scriptInfo);
		
		return builder.build();
	}
	
	//----------------- Concurrency Management -----------------//
	
	private boolean secureLock(){
		if(owningThread == null){
			owningThread = Thread.currentThread();
			return true;
		} else if(owningThread.equals(Thread.currentThread())){
			return true;
		}
		
		// Another thread is loading the module.
		return false;
	}
	
	private void releaseLock(){
		owningThread = null;
	}
	
	private MonitorInterruptCondition condition = new MonitorInterruptCondition(){

		@Override
		public boolean shouldInterrupt() {
			return owningThread == null;
		}
		
	};
	
	//----------------- Hosted Method Manager -----------------//
	
	@Override
	public synchronized HostedMethodManager getHostedMethodManager(){
		if(hmm == null){
			hmm = new HostedMethodManager(extLoader);
		}
		
		return hmm;
	}

	//----------------- Loose Scripts Management -----------------//
	
	private Argument[] args;
	private Map<String, Result> gsEvalCache; // Global script evaluation cache. Keyed by full canonical path of the executed script.
	
	public void initArguments(Argument[] arguments) {
		this.args = arguments;
	}
	
	public Argument[] getArguments() {
		return this.args == null ? new Argument[0] : this.args;
	}
	
	Result tryGetResult(String fullScriptPath) {
		if (gsEvalCache == null) {
			return null;
		}
		
		return gsEvalCache.get(fullScriptPath);
	}
	
	void replaceResult(String fullScriptPath, Result res) {
		if (gsEvalCache == null) {
			synchronized(ModuleManager.class) {
				if (gsEvalCache == null) {
					gsEvalCache = new HashMap<>();
				}
			}
		}
		
		if (res == null) {
			gsEvalCache.remove(fullScriptPath);
		} else {
			gsEvalCache.put(fullScriptPath, res);
		}
	}
	
	/**
	 * Get the current script info for the default module.
	 * 
	 * @return null if default module is still not fully loaded.
	 */
	public ScriptInfo getScriptInfoForDefaultModule() {
		ModuleInfo mi = cache.get(ModuleInfo.DEFAULT_MODULE_NAME);
		return mi != null ? mi.getFirstScript() : null;
	}
	
	/**
	 * Reset the script info for the default module to a previous one, retaining new requirements and module info though.
	 * 
	 * @param psi A previous script info object.
	 */
	public void setScriptInfoForDefaultModule(ScriptInfo psi) {
		ModuleInfo mi = cache.get(ModuleInfo.DEFAULT_MODULE_NAME);
		if (mi != null) {
			// Note that module info and script info are cross-referencing each other,
			// but we can only update them one by one. First create a new script info
			// using the existing module info, then update the script info in that module.
			ScriptInfo csi = getScriptInfoForDefaultModule();
			mi.resetScriptInfo(csi, psi);
		}
	}
	
	//----------------- Engine Policy Enforcer -----------------//
	
	public EnginePolicyEnforcer getEnginePolicyEnforcer() {
		if (policyEnforcer == null) {
			synchronized(ModuleManager.class) {
				if (policyEnforcer == null) {
					initializePolicyEnforcer();
				}
			}
		}
		
		return policyEnforcer;
	}

	private void initializePolicyEnforcer() {
		policyEnforcer = new EnginePolicyEnforcer();
		if (checkKind != CheckResultKind.UNDEFINED) {
			policyEnforcer.setDefault(checkKind == CheckResultKind.ALLOW);
		}
		
		if (policies != null && policies.size() > 0) {
			for (Entry<String, Pair<String[], String[]>> kvp : policies.entrySet()) {
				policyEnforcer.addPolicy(
					new PlatformAccessPolicy(kvp.getKey(), kvp.getValue().getFirst(), kvp.getValue().getSecond()));
			}
		}
	}
	
	@Override
	public void setEngineLimit(EngineLimit el, int value) {
		EnginePolicyEnforcer enforcer = getEnginePolicyEnforcer();
		enforcer.addPolicy(
			el.isStateful()	
			? new StatefulEngineLimitPolicy(el, value)
			: new EngineLimitPolicy(el, value));
	}
	
	@Override
	public void resetPlatformAccess() {
		policyEnforcer = null;
		checkKind = CheckResultKind.UNDEFINED;
		policies = null;
	}

	@Override
	public void setPlatformAccess(boolean allowOrDeny, String category, String... operations) {
		if (policyEnforcer != null) {
			// For now, can only be called before the engine has started enforcing policies.
			// After we introduce Julian API for policy config this may be changed.
			return;
		}
		
		if (policies == null) {
			policies = new HashMap<String, Pair<String[], String[]>>();
		}
		
		if (IEnginePolicy.WILDCARD.equals(category)) {
			// Sweeping setting. Void all the previous settings.
			checkKind = allowOrDeny ? CheckResultKind.ALLOW : CheckResultKind.DENY;
			policies.clear();
			return;
		}
		
		String categoryLower = category.toLowerCase();
		String[] operationsLower;
		if (operations != null && operations.length > 0) {
			int len = operations.length;
			operationsLower = new String[len];
			for (int i = 0; i < len; i++) {
				operationsLower[i] = operations[i].toLowerCase().trim();
			}
		} else {
			operationsLower = new String[] { IEnginePolicy.WILDCARD };
		}
		
		setPlatformAccess0(allowOrDeny, categoryLower, operationsLower);
	}
	
	// operations: min size == 1
	private void setPlatformAccess0(boolean allowOrDeny, String category, String[] operations) {
		Pair<String[], String[]> pair = policies.get(category);
		if (pair == null) {
			pair = new Pair<String[], String[]>(
				allowOrDeny ? operations : null,
				allowOrDeny ? null : operations
			);
			policies.put(category, pair);
		} else {
			String[] these = allowOrDeny ? pair.getFirst() : pair.getSecond();
			String[] those = allowOrDeny ? pair.getSecond() : pair.getFirst();
			if (these != null) {
				these = mergeOps(these, operations);
			} else {
				these = operations;
			}

			if (those != null) {
				those = excludeOps(those, these);
			}
			policies.put(category, new Pair<String[], String[]>(these, those));
		}
	}
	
	private static String[] excludeOps(String[] those, String[] these) {
		Set<String> set = new HashSet<String>();
		for (String op : those) {
			set.add(op);
		}
		
		boolean excludeAll = false;
		for (String op : these) {
			if (IEnginePolicy.WILDCARD.equals(op)) {
				excludeAll = true;
				break;
			}
			
			if (set.contains(op)) {
				set.remove(op);
			}
		}
		
		if (excludeAll) {
			return null;
		} else {
			String[] results = new String[set.size()];
			return set.toArray(results);
		}
	}

	private static String[] mergeOps(String[] these, String[] operations) {
		Set<String> set = new HashSet<String>();
		for (String op : these) {
			set.add(op);
		}

		boolean includeAll = false;
		for (String op : operations) {
			if (IEnginePolicy.WILDCARD.equals(op)) {
				includeAll = true;
				break;
			}
			
			set.add(op);
		}
		
		if (includeAll) {
			return new String[] { IEnginePolicy.WILDCARD };
		} else {
			String[] results = new String[set.size()];
			return set.toArray(results);
		}
	}
}
