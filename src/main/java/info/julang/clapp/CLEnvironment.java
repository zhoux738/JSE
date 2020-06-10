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

package info.julang.clapp;

import java.util.ArrayList;
import java.util.List;

import info.julang.external.interop.IBinding;

public class CLEnvironment {

	public NamedBinding[] getBindings() {
		return bnds == null ? new NamedBinding[0] : bnds.toArray(new NamedBinding[0]);
	}
	
	void addBinding(String name, IBinding binding) {
		if(bnds==null){
			bnds = new ArrayList<NamedBinding>();
		}
		bnds.add(new NamedBinding(name, binding));
	}
	
	public String[] getModulePaths() {
		return modulePaths == null ? new String[0] : modulePaths.toArray(new String[0]);
	}

	void addModulePath(String modulePath) {
		if(modulePaths==null){
			modulePaths = new ArrayList<String>();
		}
		modulePaths.add(modulePath);
	}

	public String getScriptFile() {
		return scriptFile;
	}

	void setScriptFile(String scriptFile) {
		this.scriptFile = scriptFile;
	}

	public String getScript() {
		return script;
	}

	void setScript(String script) {
		this.script = script;
	}
	
	public boolean shouldTerminate() {
		return this.exitCode != CONTINUE_EXEC;
	}
	
	public int getExitCode() {
		return this.exitCode;
	}
	
	void setTerminate(int exitCode) {
		this.exitCode = exitCode;
	}
	
	public boolean isQuietMode() {
		return this.quiet;
	}
	
	void setQuietMode(boolean quite) {
		this.quiet = quite;
	}
	
	public boolean isInteractiveMode() {
		return this.interactive;
	}
	
	void setInteractiveMode(boolean value) {
		this.interactive = value;
	}

	public boolean supportsANSIEscaping() {
		return ansiEscaping;
	}
	
	void setSupportForANSIEscaping(boolean value) {
		this.ansiEscaping = value;
	}
	
	public String[] getArguments() {
		return this.arguments == null ? new String[0] : this.arguments;
	}

	void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	private ArrayList<NamedBinding> bnds;
	
	private List<String> modulePaths;
	
	private String[] arguments;
	
	private String scriptFile;
	
	private String script;

	private int exitCode = CONTINUE_EXEC;
	
	private boolean quiet;

	private boolean interactive;
	
	private boolean ansiEscaping;
	
	private static final int CONTINUE_EXEC = -1;
	
}
