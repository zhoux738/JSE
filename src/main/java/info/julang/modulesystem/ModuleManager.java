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

import info.julang.execution.security.CheckResultKind;
import info.julang.execution.security.EnginePolicyEnforcer;
import info.julang.execution.security.IEnginePolicy;
import info.julang.execution.security.PlatformAccessPolicy;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThread.MonitorInterruptCondition;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodManager;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
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
	private Map<String, OneOrMoreList<ClassInfo>> allClasses;
	
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
	
	public void clearModulePath() {
		locator.clearModulePath();
	}
	
	public boolean isLoaded(String moduleName){
		synchronized(lock){
			return cache.containsKey(moduleName);
		}
	}
	
	/**
	 * Load a module of specified name. 
	 * <p/>
	 * This method will first locate the script files belonging to the specified module, then
	 * parse those scripts to collect information about the contained types. The method will
	 * also apply the same procedure to any other required modules encountered along the way.
	 * By the time this method returns it is guaranteed that the entire module closure is 
	 * loaded (LCP).
	 * <p/>
	 * This method is thread safe. Calling it from two competing threads won't result in the 
	 * module being loaded twice.
	 * <p/>
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
	 * Load a script as module.
	 * 
	 * @param rt Thread runtime
	 * @param ainfo AST info
	 * @param synModuleName a mandated module name for this script. Must be a const value from {@link ModuleInfo}, 
	 * such as {@link ModuleInfo#DEFAULT_MODULE_NAME DEFAULT_MODULE_NAME}.
	 * @param loadTypesNow if true, force loading all new types into the runtime immediately after module loading.
	 * @return
	 */
	public ModuleInfo loadScriptAsModule(
		ThreadRuntime rt, LazyAstInfo ainfo, String synModuleName, boolean loadTypesNow) {
		synchronized(lock){
			while(!secureLock()){
				rt.getJThread().safeWait(lock, condition);
			}
		}
		
		RawScriptInfo info = new RawScriptInfo(null, false);// Don't know the module name yet.
		info.reset(ainfo.getFileName(), ainfo);
		RawScriptInfo.Option option = info.getOption();
		option.setPresetModuleName(synModuleName);
		option.setAllowNameInconsistency(true);
		CollectScriptInfoStatement csis = new CollectScriptInfoStatement(true); // Load AST now
		csis.prescan(info);
		
		// If the file can be pre-scanned successfully, we should expect the module to be equal to what is given, if it's given.
		String gModName = info.getModuleName();
		if (synModuleName != null
			&& gModName != null
			&& !synModuleName.equals(gModName)) {
			throw new IllegalModuleFileException(
				info, ainfo.getFileName(), 1, "A loose script file must not declare a module name explicitly.");
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
		Map<String, MutableModuleInfo> tempCache = new HashMap<String, MutableModuleInfo>();
		tempCache.put(gModName, mmi);
		populateDependencies(tempCache);
		
		// Add all the new modules into the cache in a single move (LCP)
		// Note this will overwrite the previous existing global script module
		cache.putAll(tempCache);
		
		try {
			if (loadTypesNow) {
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
				
				MutableModuleInfo mi = makeModuleInfo(mod, mli.getScriptPaths(), mli.isEmbedded());
				
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
		
		populateDependencies(tempCache);
		
		// Add all the new modules into the cache in a single move (LCP)
		cache.putAll(tempCache);
		
		return result;
	}

	/**
	 * Create a {@link ModuleInfo} instance based on the information collected from a set of scripts files, including
	 * requirements, script provider and declared classes.
	 * 
	 * @param modName
	 * @param scriptPaths
	 * @param isEmbedded
	 * @return
	 */
	private static MutableModuleInfo makeModuleInfo(
		String modName, List<String> scriptPaths, boolean isEmbedded) 
		throws FileNotFoundException {
		ModuleInfo.Builder builder = new ModuleInfo.Builder(modName);
		//RawScriptInfo info = new RawScriptInfo(modName, isEmbedded);
		
		for(String path : scriptPaths){
			RawScriptInfo info = loadModuleInfo(modName, path, isEmbedded, false);
			
			// Add script info
			builder.addScript(info);
		}
		
		return builder.build();
	}
	
	private static RawScriptInfo loadModuleInfo(
		String modName, String path, boolean isEmbedded, boolean analyticalLoad) 
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
			
			CollectScriptInfoStatement csis = new CollectScriptInfoStatement(analyticalLoad);
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
		return loadModuleInfo(modName, path, false, true);
	}

	/**
	 * Populate module dependency information.
	 * @param tempCache
	 */
	private void populateDependencies(Map<String, MutableModuleInfo> tempCache) {
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
			for(ModuleInfo req : reqs){
				mmi.addRequiredModule(req);
			}
			
			// Add classes to global cache
			for(ClassInfo ci : mmi.getClasses()){
				String name = ci.getName();
				OneOrMoreList<ClassInfo> list = allClasses.get(name);
				if(list == null){
					allClasses.put(name, new OneOrMoreList<ClassInfo>(ci));
				} else {
					list.add(ci);
				}
			}
		}
		
		// once we have populated all the modules, move them to global cache
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
