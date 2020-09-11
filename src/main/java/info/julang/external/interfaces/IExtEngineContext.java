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

import info.julang.external.interop.IBinding;

public interface IExtEngineContext {

	/**
	 * Get a binding from the context. 
	 * @param name
	 * @return
	 */
	IBinding getBinding(String name);
	
	/**
	 * Add a new binding to this context.
	 * @param binding
	 */
	void addBinding(String name, IBinding binding);
	
	/**
	 * Set value to an environment variable.
	 * <p/>
	 * Will overwrite an existing variable with same name.
	 * @param name
	 * @param value
	 */
	void setEnviromentVariable(String name, String value);
	
	/**
	 * Get value of an environment variable.
	 * @param name
	 * @return
	 */
	String getEnviromentVariable(String name);
	
	/**
	 * Set the arguments.
	 * <p/>
	 * Will overwrite existing arguments whenever called.
	 * @param args
	 */
	void setArguments(String[] args);
	
	/**
	 * Get the arguments.
	 * @return
	 */
	String[] getArguments();
	
	/**
	 * Add path to modules.
	 * <p/>
	 * Duplicate paths will be ignored.
	 * @param path
	 */
	void addModulePath(String path);
	
	/**
	 * Add an engine policy.
	 * 
	 * @param allowOrDeny True to allow, false deny
	 * @param category the name of policy category
	 * @param operations the operations to allow or deny under this category
	 */
	void addPolicy(boolean allowOrDeny, String category, String[] operations);
}
