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
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostingPlatformException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.JType;

/**
 * The exception to be thrown when the engine or the underlying platform detected access or usage violation.
 * <p>
 * Since Julian runs on top of a managed platform such as JVM, a security enforcement layer might already be enabled
 * underneath. If a violation is detected through such built-in mechanism, JSE will catch the propagated exception 
 * (HostingPlatformException) and wrap it in this class. This way, any security-related issue can be unified in the
 * same representation at the surface of JSE.
 * 
 * @author Ming Zhou
 */
public class UnderprivilegeException extends JSERuntimeException {

    private static final long serialVersionUID = 1857658737626586018L;
    
	public static final String FullName = "System.UnderprivilegeException";
    
    private HostingPlatformException platEx;
    
    public UnderprivilegeException(String category, String operation) {
        this(category, operation, null);
    }
    
    public UnderprivilegeException(String category, String operation, String exMsg) {
        super(composeMessage(category, operation, exMsg));
    }
    
    public UnderprivilegeException(HostingPlatformException ex) {
    	super("Access is denied by the underlying plaform.");
    	platEx = ex;
    }
    
    private static String composeMessage(String category, String operation, String exMsg){
        String msg = "Access is denied by policy " + category + " (" + operation + ").";
        if (exMsg != null && !"".equals(exMsg)) {
            msg += " ";
            msg += exMsg;
        }
        
        return msg;
    }

    @Override
    public KnownJSException getKnownJSException() {
        return KnownJSException.Underprivilege;
    }
    
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		JulianScriptException jse;
		if (platEx != null) {
			// Special: if this is caused by a platform violation, 
			// call the ctor which takes a HostingPlatformException instance as the sole argument.
			ParsedTypeName exTypName = ParsedTypeName.makeFromFullName(UnderprivilegeException.FullName);
			JType exType = context.getTypeResolver().resolveType(exTypName);
			ObjectValue cause = platEx.toJSE(rt, context).getExceptionValue();
			NewObjExecutor noe = new NewObjExecutor(rt);
			ObjectValue ov = noe.newObject(
				context.getHeap(),
				exType,
				new JValue[] { TempValueFactory.createTempRefValue(cause) }
			);

			jse = new JulianScriptException(context.getTypTable(), ov);
		} else {
			jse = super.toJSE(rt, context);
		}
		
		return jse;
	}
}
