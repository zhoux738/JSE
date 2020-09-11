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
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.IO.IOInstanceNativeExecutor;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * The native implementation of <font color="green">System.Network.Socket</font>.
 * 
 * @author Ming Zhou
 */
public class ScriptSocket extends ScriptSocketBase {

    public static final String FQCLASSNAME = "System.Network.Socket";
    
    //----------------- IRegisteredMethodProvider -----------------//
    
    public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

        @Override
        protected void implementProvider(SimpleHostedMethodProvider provider) {
            provider
                .add("ctor", new InitExecutor())
                .add("bind", new BindExecutor())
                .add("connect", new ConnectExecutor())
                .add("getLocalHost", new GetLocalHostExecutor())
                .add("getLocalPort", new GetLocalPortExecutor())
                .add("getSetting", new ScriptSocketBase.GetSettingExecutor())
                .add("getRemoteHost", new GetRemoteHostExecutor())
                .add("getRemotePort", new GetRemotePortExecutor())
                .add("close", new CloseExecutor())
                .add("getStream", new GetStreamExecutor())
                .add("getState", new GetStateExecutor());
        }
        
    };
    
    //----------------- native executors -----------------//
    
    private static class InitExecutor extends CtorNativeExecutor<ScriptSocket> {

        @Override
        protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptSocket sock, Argument[] args) throws Exception {
            ObjectValue ov = this.getObject(args, 0);
            ScriptSocketConfig conf = new ScriptSocketConfig(ov);
            sock.init(conf);
            setOverwrittenReturnValue(VoidValue.DEFAULT);
        }
        
    }
    
    private static class BindExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

    	BindExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_connect);
		}
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {
            String host = this.getString(args, 0);
            int port = this.getInt(args, 1);
            
            thisVal.bind(host, port);
            
            return VoidValue.DEFAULT;
        }
        
    }
    
    private static class ConnectExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

    	ConnectExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_connect);
		}
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {
            String host = this.getString(args, 0);
            int port = this.getInt(args, 1);
            
            thisVal.connect(host, port);
            
            return VoidValue.DEFAULT;
        }
        
    }
    
    private static class CloseExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
            thisVal.close();
            
            return VoidValue.DEFAULT;
        }
        
    }
   
    private static class GetLocalHostExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
            String host = thisVal.getLocalHost();
            JValue val = host == null ? RefValue.NULL : TempValueFactory.createTempStringValue(host);
            return val;
        }
        
    }
    
    private static class GetLocalPortExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
            int port = thisVal.getLocalPort();
            JValue val = TempValueFactory.createTempIntValue(port);
            return val;
        }
        
    }
    
    private static class GetRemoteHostExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
            String host = thisVal.getRemoteHost();
            JValue val = host == null ? RefValue.NULL : TempValueFactory.createTempStringValue(host);
            return val;
        }
        
    }
    
    private static class GetRemotePortExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
            int port = thisVal.getRemotePort();
            JValue val = TempValueFactory.createTempIntValue(port);
            return val;
        }
        
    }
    
    private static class GetStateExecutor extends IOInstanceNativeExecutor<ScriptSocket> {

        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {     
        	SocketState st = thisVal.getState();
            JValue val = TempValueFactory.createTempIntValue(st.ordinal());
            return val;
        }
        
    }
    
    private static class GetStreamExecutor extends IOInstanceNativeExecutor<ScriptSocket> {
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocket thisVal, Argument[] args) throws Exception {
        	BoolValue bv = (BoolValue)this.getValue(args, 0);
        	ObjectValue ov = thisVal.getStream(rt, bv);
            return ov;
        }
        
    }

    private Socket sock;

    /**
     * Initialize with specified internal objects. Used by engine internals.
     * 
     * @param sock
     * @param conf
     */
    void initInternal(Socket sock, ScriptSocketConfig conf) {
		this.sock = sock;
		super.initBase(conf);
	}

	public String getLocalHost() {
		if (sock.isBound()) {
			return sock.getLocalAddress().toString();
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

	public String getRemoteHost() {
	    InetAddress sa = sock.getInetAddress();
        return sa == null ? null : sa.toString();
    }
	
    public int getRemotePort() {
        return sock.getPort();
    }

    public void init(ScriptSocketConfig conf) throws IOException {
        sock = SocketChannel.open().socket();
        this.conf = conf;
    }

    public SocketState getState() {
    	if (sock.isClosed()){
    		if (sock.isBound()){
    			return SocketState.BOUND;
    		} else {
    			return SocketState.CLOSED;
    		}
    	} else if (sock.isConnected()) {
    		return SocketState.CONNECTED;
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
    
    public void connect(String remoteHost, int remotePort){
        try {
            // Bind to local first
            sock.setReuseAddress(conf.isReuseaddr());
            
            InetAddress remoteAddr = InetAddress.getByName(remoteHost);
            if (!sock.isBound()) {
                String localAddr = conf.getLocalAddress();
                int localPort = conf.getLocalPort();
                if (localAddr != null || localPort > 0) {
                	// Bind only if any value is explicitly set.
                    bind(localAddr, localPort);
                }
            }
            
            sock.setTcpNoDelay(!conf.isNagle());
            sock.setKeepAlive(conf.isKeepalive());
            sock.setOOBInline(conf.isOobinline());
            int lin = conf.getLinger();
            sock.setSoLinger(lin >= 0, lin >= 0 ? lin : 0);
            sock.setSoTimeout(conf.getTimeout());
            
            InetSocketAddress addr = new InetSocketAddress(remoteAddr, remotePort);
            
            sock.connect(addr);
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
        } finally {
        	if (listeners != null) {
        		for (ISocketEventListener l : listeners){
        			try {
						l.onClose();
					} catch (Exception e) {
						// NO-OP
					}
        		}
        	}
        }
    }
	
    public ObjectValue getStream(ThreadRuntime rt, BoolValue writable) {
		HostedValue ov = (HostedValue)ThreadRuntimeHelper.instantiateSystemType(
			rt, ScriptSocketStream.FullTypeName, new JValue[]{ writable });
		ScriptSocketStream sss = (ScriptSocketStream)ov.getHostedObject();
		sss.setSocket(this, ov);
		return ov;
	}
    
    //------- For interaction with ScriptSocketStream -------//
    
    Socket getSocket(){
        return sock;
    }
    
    private List<ISocketEventListener> listeners;
    
    synchronized void addListener(ISocketEventListener listener){
        if (listeners == null) {
            listeners = new ArrayList<ISocketEventListener>();
        }
        
        listeners.add(listener);
    }
}
