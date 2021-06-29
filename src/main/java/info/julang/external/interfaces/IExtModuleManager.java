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

package info.julang.external.interfaces;

public interface IExtModuleManager {
	
	/**
	 * The name of directory that contains Julian modules: "jse_modules". 
	 * This directory is located alongside the invoked script and is referred to as the default module path.
	 * <p>
	 * When invoking <code>main.jul</code> from the following example, the co-located directory 
	 * <code>jse_modules</code> will be added to the module paths. As a result, without configuring 
	 * the engine's module paths explicitly, the user is immediately able to 
	 * <code>import</code> <code style="color: green">Module1</code><code>;</code> at the beginning
	 * of <code>main.jul</code>.
	 * <pre>
	 *   (file system layout - Windows example)
	 *      
	 *      D:\
	 *       +-- myfiles\
	 *            |
	 *            +-- main.jul
	 *            |
	 *            +-- jse_modules\
	 *                 |
	 *                 +-- Module1\
	 *                 +-- Module2\
	 *                 |    +-- SubmodA
	 *                 +-- Module3\
	 *                 |    +-- SubmodX
	 *                 |         +-- SubmodY
	 *                 ...
	 *                 
	 *   main.jul:
	 *      
	 *      import Module1;
	 *      import Module2.SubmodA;
	 *      import Module3.SubmodX.SubmodY as ThirdMod;
	 * </pre>
	 */
	public static final String DefaultModuleDirectoryName = "jse_modules";

	/**
	 * Add a path for module probing.
	 * 
	 * @param path The path to module directory.
	 */
	void addModulePath(String path);
	
	/**
	 * Clear data prepared for or produced during the previous execution session.
	 */
	void clearExecutionData();
	
	/**
	 * Set platform access policy.
	 * <p>
	 * This method can be called multiple times before the engine starts. Each call will add a new rule
	 * on top of the previous rules, potentially overriding some or all of them. 
	 * <p>
	 * By default, no policy is set, i.e. all access is allowed. To deny everything, 
	 * call <pre><code> setPlatformAccess(false, "*");</code></pre>
	 * Then, one may selectively allow certain categories
	 * by calling, for example, <pre><code> setPlatformAccess(true, "System.IO", "read", "write");</code></pre>
	 * Calling again with <pre><code> setPlatformAccess(false, "System.IO", "write");</code></pre> will override 
	 * the previous permission given to <code>"System.IO/read"</code> operation, 
	 * but will keep <code>"System.IO/write"</code> as allowed. 
	 * <p>
	 * Note that the param <code>operations</code> is
	 * variadic, so calling without specifying any operation is allowed and has the same effect as specifying all
	 * the operations. The following statements are equivalent: <pre><code> setPlatformAccess(true, "System.IO");
	 * setPlatformAccess(true, "System.IO", "*");
	 * setPlatformAccess(true, "System.IO", "read", "write", "list", "stat"); // All operations under "System.IO" as of 0.1.30</code></pre>
	 * While most calls are only to tweak the previous settings, the sweeping call with <code>"*"</code> as the category
	 * will reset the configuration. Therefore, after the following sequence of calls,<pre><code> setPlatformAccess(true, "System.IO");
	 * setPlatformAccess(true, "System.Socket", "read", "bind");
	 * setPlatformAccess(false, "*"); // RESET</code></pre>
	 * All of the access permissions set up so far will be cleared and the engine will deny everything.
	 * <p>
	 * @param allowOrDeny true to allow; false to deny
	 * @param category the platform access category, as defined in {@link info.julang.execution.security.PACON}.
	 * @param operations the name of the operations defined in the category.
	 */
	void setPlatformAccess(boolean allowOrDeny, String category, String... operations);
	
	/**
	 * Clear all platform access settings.
	 */
	void resetPlatformAccess();
	
	/**
	 * Check if the module is loaded.
	 * 
	 * @param moduleName The module's name.
	 * @return True if the module has been loaded.
	 */
	boolean isLoaded(String moduleName);
}
