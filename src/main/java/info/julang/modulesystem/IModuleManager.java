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

import java.util.List;

import info.julang.execution.security.EngineLimit;
import info.julang.execution.security.EnginePolicyEnforcer;
import info.julang.execution.threading.JThread;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.hosting.HostedMethodManager;

public interface IModuleManager extends IExtModuleManager {
	
	/**
	 * Get a list of classes info which has the given name as the not fully qualified name.
	 * 
	 * @param name
	 * @return
	 */
	public List<ClassInfo> getClassesByNFQName(String name);
	
	/**
	 * Get the class info by a fully qualified name.
	 * 
	 * @param name
	 * @return null if no class with the specified name is found.
	 */
	public ClassInfo getClassesByFQName(String name);
	
	/**
	 * Get a manager for hosted types.
	 * 
	 * @return
	 */
	public HostedMethodManager getHostedMethodManager();
	
	/**
	 * Get the engine policy enforcer.
	 * 
	 * @return
	 */
	public EnginePolicyEnforcer getEnginePolicyEnforcer();
	
	/**
	 * Set engine limit to the specified value. Engine limit is a subtype of engine policy,
	 * so it can be reset by {@link #resetPlatformAccess()}.
	 * 
	 * @param el
	 * @param value
	 */
	public void setEngineLimit(EngineLimit el, int value);

	/**
	 * Load a module of specified name.
	 * 
	 * @param thread
	 * @param moduleName
	 * @return
	 */
    public ModuleInfo loadModule(JThread thread, String moduleName);
	
}
