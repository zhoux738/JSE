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

package info.julang.execution.security;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * The exception to be thrown when the engine detected system limit violation.
 * <p>
 * By default, no system limits are set. The module manager API exposes API to set various {@link EngineLimit}, 
 * which are checked during runtime at the relevant places. If a violation is determined to be imminent, this exception
 * will be thrown, crashing the engine.
 * 
 * @author Ming Zhou
 */
public class RuntimeQuotaException extends JSERuntimeException {
    
	private static final long serialVersionUID = -3377844953197847997L;

	public static final String FullName = "System.UnderprivilegeException";
    
    public RuntimeQuotaException(String msg) {
        super("Imminent system limit violation. " + msg);
    }

    @Override
    public KnownJSException getKnownJSException() {
        return KnownJSException.Underprivilege;
    }
}
