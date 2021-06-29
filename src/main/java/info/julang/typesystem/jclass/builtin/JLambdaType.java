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

package info.julang.typesystem.jclass.builtin;

import info.julang.execution.Executable;
import info.julang.interpretation.errorhandling.IHasLocationInfoEx;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ExecutableType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.JReturn;
import info.julang.util.Crypto;
import info.julang.util.OSTool;

/**
 * The lambda type is for lambda defined in script.
 * 
 * @author Ming Zhou
 */
public class JLambdaType extends JFunctionType implements ExecutableType {

	public static final String Name = "<Lambda>";
	
	private IHasLocationInfoEx locInfo;
	private String uniqueName;
	
	public JLambdaType(IHasLocationInfoEx locInfo, JParameter[] params, Executable executable) {
		super(Name, params, null, executable);
		this.ret = JReturn.UntypedReturn;
		this.locInfo = locInfo;
	}
	
	@Override
	public JLambdaType bindParams(JType[] paramTypesToRemove) {
		JParameter[] params = removeParams(paramTypesToRemove, false);
		return new JLambdaType(locInfo, params, this.getExecutable());
	}

	@Override
	public FunctionKind getFunctionKind(){
		return FunctionKind.LAMBDA;
	}
	
	@Override
	public String getFullFunctionName(boolean includeParams) {
		if (!includeParams) {
			return super.getName();
		}
		
		if (uniqueName == null) {
			synchronized (JLambdaType.class) {
				if (uniqueName == null) {
					uniqueName = createName();
				}
			}
		}
		
		return uniqueName;
	}
	
	/**
	 * The type name of a lambda is its full function name (simplified location info + full location info hash)
	 */
	@Override
	public String getName() {
		return getFullFunctionName(true);
	}
	
	/**
	 * Create a name in <code>format: filename:linenumber:sha256{filepath}</code>
	 */
	private String createName() {
		String fpath = OSTool.canonicalizePath(locInfo.getFileName());
		String fname = OSTool.getFileSimpleName(fpath, true);
		
		int ln = locInfo.getLineNumber();
		int cn = locInfo.getColumnNumber();
		String lncn = ":" + ln + ":" + cn + ":";
		String hash = Crypto.sha256(fpath + lncn, 8);
		
		String combined = fname + lncn + hash;
		
		return combined;
	}
}
