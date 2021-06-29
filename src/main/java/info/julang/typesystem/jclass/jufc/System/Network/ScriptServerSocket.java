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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.ServerSocketChannel;

import info.julang.execution.Argument;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.IO.IOInstanceNativeExecutor;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;

/**
 * The native implementation of <code style="color:green">System.Network.Socket</code>.
 * 
 * @author Ming Zhou
 */
public class ScriptServerSocket extends ScriptSocketBase {

    public static final String FQCLASSNAME = "System.Network.ServerSocket";
    
    //----------------- IRegisteredMethodProvider -----------------//
    
    public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

        @Override
        protected void implementProvider(SimpleHostedMethodProvider provider) {
            provider
                .add("ctor", new InitExecutor())
                .add("bind", new BindExecutor())
                .add("accept", new AcceptExecutor())
                .add("close", new CloseExecutor())
                .add("getSetting", new ScriptSocketBase.GetSettingExecutor())
                .add("getState", new GetStateExecutor())
                .add("getLocalHost", new GetLocalHostExecutor())
                .add("getLocalPort", new GetLocalPortExecutor());
        }
        
    };
    
    //----------------- native executors -----------------//
    
    private static class InitExecutor extends CtorNativeExecutor<ScriptServerSocket> {

        @Override
        protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptServerSocket sock, Argument[] args) throws Exception {
            ObjectValue ov = this.getObject(args, 0);
            ScriptSocketConfig conf = new ScriptSocketConfig(ov);
            sock.init(conf);
            setOverwrittenReturnValue(VoidValue.DEFAULT);
        }
        
    }
    
    private static class BindExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {

    	BindExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_connect);
		}
		
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {
            String host = this.getString(args, 0);
            int port = this.getInt(args, 1);
            
            thisVal.bind(host, port);
            
            return VoidValue.DEFAULT;
        }
        
    }
    
    private static class AcceptExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {

    	AcceptExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_listen);
		}
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {
            Socket sock = thisVal.accept();
            
            ObjectValue scSock = ThreadRuntimeHelper.instantiateSystemType(
            	rt, 
            	ScriptSocket.FQCLASSNAME, 
            	new JValue[]{ TempValueFactory.createTempBoolValue(false) });
    		HostedValue hv = (HostedValue)scSock;
            
            ObjectValue scConf = ThreadRuntimeHelper.instantiateSystemType(
            	rt, 
            	ScriptSocketConfig.FQCLASSNAME, 
            	new JValue[0]);
    		ScriptSocketConfig ssc = new ScriptSocketConfig(scConf);
            
    		ScriptSocket ss = new ScriptSocket();
    		ss.initInternal(sock, ssc);
    		hv.setHostedObject(ss);
    		
    		return TempValueFactory.createTempRefValue(hv);
        }
        
    }
    
    private static class CloseExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {     
            thisVal.close();
            
            return VoidValue.DEFAULT;
        }
        
    }
    
    private static class GetStateExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {     
        	SocketState st = thisVal.getState();
            JValue val = TempValueFactory.createTempIntValue(st.ordinal());
            return val;
        }
        
    }
    
    private static class GetLocalHostExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {     
        	String host = thisVal.getLocalHost();
            JValue val = TempValueFactory.createTempStringValue(host);
            return val;
        }
        
    }
    
    private static class GetLocalPortExecutor extends IOInstanceNativeExecutor<ScriptServerSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptServerSocket thisVal, Argument[] args) throws Exception {     
        	int port = thisVal.getLocalPort();
            JValue val = TempValueFactory.createTempIntValue(port);
            return val;
        }
        
    }

    private ServerSocket sock;
    private boolean settingChanged;
    
    public void init(ScriptSocketConfig conf) {
        try {
            sock = ServerSocketChannel.open().socket();
            super.initBase(conf);
        } catch (IOException e) {
            throw new JSEIOException(e.getMessage());
        }
    }

	public String getLocalHost() {
		if (sock.isBound()) {
			return sock.getLocalSocketAddress().toString();
		} else {
			return conf.getLocalAddress();
		}
    }
	
	public int getLocalPort() {
		if (sock.isBound()) {
			return sock.getLocalPort();
		} else {
			return conf.getLocalPort();
		}
    }

	public SocketState getState() {
    	if (sock.isClosed()){
    		if (sock.isBound()){
    			return SocketState.BOUND;
    		} else {
    			return SocketState.CLOSED;
    		}
    	} else if (sock.isBound()) {
    		return SocketState.BOUND;
    	}
    	
    	return SocketState.UNBOUND;
	}

	public void bind(String localAddr, int localPort) {
        try {            
            if (!sock.isBound()) {
                InetSocketAddress addr = new InetSocketAddress(
                    localAddr == null ? InetAddress.getLocalHost().getHostAddress() : localAddr, localPort);
                sock.bind(addr);
            }
        } catch (UnknownHostException e) {
            throw new JSENetworkException(e);
        } catch (SocketException e) {
            throw new JSESocketException(e.getMessage());
        } catch (IOException e) {
            throw new JSEIOException(e.getMessage());
        }
    }
    
    public Socket accept(){
        try {
            // Bind to local first
            if (!settingChanged){
                sock.setReuseAddress(conf.isReuseaddr());
                
                if (!sock.isBound()) {
                    String localAddr = conf.getLocalAddress();
                    int localPort = conf.getLocalPort();
                    bind(localAddr, localPort);
                }
                
                sock.setSoTimeout(conf.getTimeout());  
                
                settingChanged = true;
            }
            
            return sock.accept();
        } catch (UnknownHostException e) {
            throw new JSENetworkException(e);
        } catch (SocketException e) {
            throw new JSESocketException(e.getMessage());
        } catch (IOException e) {
            throw new JSEIOException(e.getMessage());
        }
    }
    
    public void close(){
        try {
            sock.close();
        } catch (SocketException e) {
            throw new JSESocketException(e.getMessage());
        } catch (IOException e) {
            throw new JSEIOException(e.getMessage());
        }
    }
}
