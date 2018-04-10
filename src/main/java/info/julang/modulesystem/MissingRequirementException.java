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

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * The required module cannot be found from configured repository.
 * 
 * @author Ming Zhou
 */
public class MissingRequirementException extends JSERuntimeException {

	private static final long serialVersionUID = 8329336145335866336L;

	public MissingRequirementException(String moduleName, ModuleLocationInfo mli) {
		super(makeMsg(moduleName, mli));
	}
	
	private static String makeMsg(String moduleName, ModuleLocationInfo mli) {
		StringBuilder sb = new StringBuilder();
		sb.append("Module ");
		sb.append(moduleName);
		sb.append(" cannot be found from configured repository.");
		
		if (mli != null){
			List<String> paths = mli.getSearchedModulePaths();
			if (paths != null){
				sb.append(" The following paths were searched:");
				for (String path : paths){
					sb.append("\n");
					sb.append("  ");
					sb.append(path);
				}
			}
		}
		
		return sb.toString();
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.MissingRequirement;
	}
	
}
