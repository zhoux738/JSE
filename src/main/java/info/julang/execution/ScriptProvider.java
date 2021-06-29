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

package info.julang.execution;

import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.interpretation.InterpretedExecutable;


/**
 * The script provider is an abstract layer that decouples script reading and script execution.
 * <p>
 * In general, a script engine runs against {@link Executable}, which hides the implementation detail
 * of how a piece of script is interpreted. ScriptProvider is the interface that can provide such an Executable.
 * 
 * @author Ming Zhou
 */
public interface ScriptProvider {

	/**
	 * Get the executable that can be invoked by a script engine.
	 * <p>
	 * @param allowReentry this executable should be re-enterable. 
	 * @return null of not executable is found.
	 * @throws ScriptNotFoundException if the given script file cannot be found.
	 */
	InterpretedExecutable getExecutable(boolean allowReentry) throws ScriptNotFoundException;
	
	/**
	 * Get the default module path. It will be added to the engine's module path list 
	 * along with other manually set modules paths.
	 * 
	 * @return null if no default module path is configured for this provider.
	 */
	String getDefaultModulePath();	
}
