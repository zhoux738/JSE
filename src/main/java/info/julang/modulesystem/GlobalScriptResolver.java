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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import info.julang.execution.FileScriptProvider;
import info.julang.execution.StreamScriptProvider;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.external.interfaces.IExtModuleManager;
import info.julang.interpretation.GlobalScriptExecutable;
import info.julang.modulesystem.GlobalScriptRunner.Options;
import info.julang.modulesystem.GlobalScriptRunner.ScriptRoot;
import info.julang.modulesystem.scripts.InternalScriptLoader;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;

/**
 * The interface to resolve a script path and get provider for it.
 * 
 * @author Ming Zhou
 */
interface GlobalScriptResolver {

	/**
	 * Get the full path of the script. This path will be then passed back to {@link #getExecutable(String)} on the same resolver object.
	 * Its main external use is for caching identification.
	 * 
	 * @return null if the path cannot be resolved.
	 */
	String getFullPath();
	
	/**
	 * Get an executable from the given path which was resolved earlier from {@link #getFullPath()}.
	 * 
	 * @param spath
	 * @return The executable.
	 */
	GlobalScriptExecutable getExecutable(String spath);
}

/**
 * Given the runner options, makes decision on what kind of resolver should be used.
 * 
 * @author Ming Zhou
 */
final class GlobalScriptResolverFactory {
	
	private GlobalScriptResolverFactory() {}
	
	static GlobalScriptResolver createResolver(String rpath, Options opts, JThreadManager tm) {
		switch (opts.primaryRoot) {
		case System:
			if (opts.secondaryRoot == null) {
				return new SystemScriptResolver(rpath);
			} else {
				return new CombinedScriptResolver(rpath, opts, tm, true);
			}
		case Script:
		case DefaultModule:
		default:
			if (opts.secondaryRoot == null) {
				return new ExternalScriptResolver(rpath, opts, tm);
			} else {
				return new CombinedScriptResolver(rpath, opts, tm, false);
			}
		}
	}
}

/**
 * Combines both {@link SystemScriptResolver} and {@link ExternalScriptResolver} in a preferred order.
 * 
 * @author Ming Zhou
 */
class CombinedScriptResolver implements GlobalScriptResolver {

	private enum ResolvedTo {
		NOT_RESOLVED,
		FIRST,
		SECOND,
		UNRESOLVABLE
	}
	
	private String rawPath;
	private JThreadManager tm;
	private Options opts;
	private GlobalScriptResolver r1;
	private GlobalScriptResolver r2;
	private boolean systemFirst;
	private ResolvedTo resolved;
	
	CombinedScriptResolver(String rpath, Options opts, JThreadManager tm, boolean systemFirst) {
		rawPath = rpath;
		this.opts = opts;
		this.tm = tm;
		r1 = getResolver(this.systemFirst = systemFirst);
		resolved = ResolvedTo.NOT_RESOLVED;
	}
	
	private GlobalScriptResolver getResolver(boolean sys) {
		if (sys) {
			return new SystemScriptResolver(rawPath);
		} else {
			return new ExternalScriptResolver(rawPath, opts, tm);
		}
	}
	
	@Override
	public String getFullPath() {
		switch (resolved) {
		case NOT_RESOLVED:
			String p = r1.getFullPath();
			if (p == null) {
				if (r2 == null) {
					r2 = getResolver(!this.systemFirst);
				}
				
				p = r2.getFullPath();
				
				if (p == null) {
					resolved = ResolvedTo.UNRESOLVABLE;
				} else {
					resolved = ResolvedTo.SECOND;
				}
			} else {
				resolved = ResolvedTo.FIRST;
			}
			
			return p;
		case FIRST:
			return r1.getFullPath();
		case SECOND:
			return r2.getFullPath();
		case UNRESOLVABLE:
		default:
			return null;
		}
	}

	@Override
	public GlobalScriptExecutable getExecutable(String spath) {
		switch (resolved) {
		case FIRST:
			return r1.getExecutable(spath);
		case SECOND:
			return r2.getExecutable(spath);
		case NOT_RESOLVED:
		case UNRESOLVABLE:
		default:
			throw new JSEError(CombinedScriptResolver.class.getSimpleName() 
				+ " cannot get provider since it is in an illegal state: " + resolved.name() + ".");
		}
	}
}

/**
 * Resolve for the built-in scripts.
 * 
 * @author Ming Zhou
 */
class SystemScriptResolver implements GlobalScriptResolver {

	private String path;
	
	SystemScriptResolver(String rpath) {
		path = InternalScriptLoader.canonicalize(rpath);
	}
	
	@Override
	public String getFullPath() {
		return InternalScriptLoader.hasScript(path) ? path : null;
	}

	@Override
	public GlobalScriptExecutable getExecutable(String spath) {
		InputStream stream = InternalScriptLoader.openStream(spath);
		StreamScriptProvider provider = new StreamScriptProvider(stream, spath);
		try {
			return provider.getExecutable(false);
		} catch (ScriptNotFoundException e) {
			return null;
		}
	}
}

/**
 * Resolve for the any external scripts prodived by the user.
 * 
 * @author Ming Zhou
 */
class ExternalScriptResolver implements GlobalScriptResolver {

	private String rawPath;
	private JThreadManager tm;
	private JThread currMainThread;
	private ScriptRoot root;
	
	ExternalScriptResolver(String rpath, Options opts, JThreadManager tm) {
		rawPath = rpath;
		this.tm = tm;
		this.currMainThread = tm.getCurrentMain();
		root = opts.primaryRoot;
	}
	
	@Override
	public String getFullPath() {
		File orgFile = new File(rawPath);
		if (!orgFile.isAbsolute()) {
			orgFile = resolveRelativePath(tm, currMainThread, root);
			if (orgFile == null) {
				// Path for the current script cannot be ascertained. A relative path cannot be used in this case.
				return null;
			}
		}
		
		if (!orgFile.exists()) {
			// Path for the current script cannot be ascertained. A relative path cannot be used in this case.
			// throw new JSEIOException("Script file cannot be found: " + rawPath);
			return null;
		}
		
		String spath;
		try {
			spath = orgFile.getCanonicalPath();
		} catch (IOException e) {
			throw new JSEIOException(e);
		}
		
		return spath;
	}

	@Override
	public GlobalScriptExecutable getExecutable(String spath) {
		try {
			return FileScriptProvider.create(spath).getExecutable(false);
		} catch (ScriptNotFoundException e) {
			return null;
		}
	}
	
	private File resolveRelativePath(JThreadManager tm, JThread currMainThread, ScriptRoot root) {
		if (root == null) {
			return null;
		}
		
		switch (root) {
		case Script:
			return resolveAgainstCurrentScript(currMainThread);
		case DefaultModule:
			return resolveAgainstDefaultModule(tm);
		case System:
		default:
			throw new JSEError(ExternalScriptResolver.class.getSimpleName() 
				+ " cannot handle root type " + root.name() + ".");
		}
	}

	private File resolveAgainstDefaultModule(JThreadManager tm) {
		String firstPath = GlobalScriptRunner.getScriptPath(tm.getFirstMain());
		File firstFile = new File(firstPath);
		if (firstFile.exists()) { // It's possible the script is not coming from FS.
			String fullPath = 
				firstFile.getParent() + File.separator 
				+ IExtModuleManager.DefaultModuleDirectoryName + File.separator 
				+ rawPath;
			File file = new File(fullPath);
			if (file.exists()) {
				return file;
			}
		}
		
		return null;
	}

	private File resolveAgainstCurrentScript(JThread currMainThread) {
		String currPath = GlobalScriptRunner.getScriptPath(currMainThread);
		File currFile = new File(currPath);
		if (currFile.exists()) { // It's possible the script is not coming from FS.
			String fullPath = currFile.getParent() + File.separator + rawPath;
			File file = new File(fullPath);
			if (file.exists()) {
				return file;
			}
		}
		
		return null;
	}
}
