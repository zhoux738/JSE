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

import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;

class ScriptSocketConfig {

    public static final String FQCLASSNAME = "System.Network.SocketConfig";
	
    private ObjectValue ov;
    
    ScriptSocketConfig(ObjectValue ov){
        this.ov = ov;
    }
    
    boolean isReuseaddr(){
        return getBoolValue("_reuseaddr");
    }
    
    boolean isKeepalive(){
        return getBoolValue("_keepalive");
    }
    
    boolean isOobinline(){
        return getBoolValue("_oobinline");
    }
    
    boolean isNagle(){
        return getBoolValue("_nagle");
    }
    
    int getLinger(){
        return getIntValue("_linger");
    }
    
    int getTimeout(){
        return getIntValue("_timeout");
    }
    
    int getLocalPort(){
        return getIntValue("_localport");
    }
    
    String getLocalAddress(){
    	JValue jv = ov.getMemberValue("_localaddr").deref();
        return jv == RefValue.NULL ? null : ((StringValue)jv).getStringValue();
    }
    
    private boolean getBoolValue(String fieldName){
        BoolValue bv = (BoolValue)ov.getMemberValue(fieldName);
        return bv.getBoolValue();
    }
    
    private int getIntValue(String fieldName){
        IntValue bv = (IntValue)ov.getMemberValue(fieldName);
        return bv.getIntValue();
    }
}
