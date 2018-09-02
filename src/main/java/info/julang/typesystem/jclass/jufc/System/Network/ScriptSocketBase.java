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
package info.julang.typesystem.jclass.jufc.System.Network;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.jufc.System.IO.IOInstanceNativeExecutor;

public class ScriptSocketBase {

    // Sync with System.Network.TCPOption
    private enum TCPOption {
    	/* SO_REUSEADDR. A boolean value default to false. */
    	REUSEADDR,
     	
     	/* SO_KEEPALIVE. A boolean value default to false. */
     	KEEPALIVE,
     	
     	/* SO_OOBINLINE. A boolean value default to false. */
     	OOBINLINE,
     	
     	/* Nagle's algorithm. This is the negation of TCP_NODELAY. A boolean value default to true. */
     	NAGLE,
     	
     	/* SO_LINGER. An integer value default to OS setting. */
     	LINGER,
     	
     	/* SO_TIMEOUT. An integer value default to OS setting. */
     	TIMEOUT, 	
    }
    
    protected static class GetSettingExecutor extends IOInstanceNativeExecutor<ScriptSocketBase> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocketBase thisVal, Argument[] args) throws Exception {    
        	String val = this.getString(args, 0);
        	TCPOption opt = TCPOption.valueOf(val);
            JValue res = thisVal.getSetting(opt);
            return res;
        }
        
    }

    protected ScriptSocketConfig conf;

    protected void initBase(ScriptSocketConfig conf) {
		this.conf = conf;
	}
    
	public JValue getSetting(TCPOption opt) {
		switch(opt){
		case REUSEADDR:
			return TempValueFactory.createTempBoolValue(conf.isReuseaddr());
		case KEEPALIVE:
			return TempValueFactory.createTempBoolValue(conf.isKeepalive());
		case OOBINLINE:
			return TempValueFactory.createTempBoolValue(conf.isOobinline());
		case NAGLE:
			return TempValueFactory.createTempBoolValue(conf.isNagle());
		case LINGER:
			return TempValueFactory.createTempIntValue(conf.getLinger());
		case TIMEOUT:
			return TempValueFactory.createTempIntValue(conf.getTimeout());
		default:
			throw new JSEError("Unrecognized TCP setting: " + opt);
		}
	}
}
