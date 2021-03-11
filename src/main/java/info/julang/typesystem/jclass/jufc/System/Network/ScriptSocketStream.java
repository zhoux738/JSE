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
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.IOThreadHandle;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.Concurrency.PromiseHandleWrapper;
import info.julang.typesystem.jclass.jufc.System.IO.IOInstanceNativeExecutor;
import info.julang.typesystem.jclass.jufc.System.IO.JFileStream;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;
import info.julang.typesystem.jclass.jufc.System.IO.StreamReadAsyncCallback;
import info.julang.typesystem.jclass.jufc.System.Network.AsyncSocketSession.SocketKey;
import info.julang.util.Box;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ScriptSocketStream extends JFileStream implements ISocketEventListener {
	
	static final String FullTypeName = "System.Network.SocketStream";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("close", new JFileStream.CloseExecutor())
                .add("writeAsync", new WriteArrayAsyncExecutor())
                .add("readAsync", new ReadArrayAsyncExecutor())
                .add("readAllAsync", new ReadArrayToEOFAsyncExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptSocketStream> {

		@Override
		protected void initialize(
			ThreadRuntime rt, HostedValue hvalue, ScriptSocketStream jfs, Argument[] args) 
			throws Exception {
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	//---------------------------------- AsyncStream ----------------------------------//
	
	// private hosted void _readAsync(byte[] buffer, int offset, int count, Function callback, PromiseHandle handle);
	// Promise readAsync(byte[] buffer, int offset);
	protected static class ReadArrayAsyncExecutor extends IOInstanceNativeExecutor<ScriptSocketStream> {
		
		ReadArrayAsyncExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_read);
		}
    	
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptSocketStream sss, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			ObjectValue handleObj = getObject(args, 2);
			boolean forSync = getBool(args, 3);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			sss.readAsync(rt, target, offset, null, handle, forSync);
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _readAllAsync(byte[] buffer, PromiseHandle handle);
    // Promise readToEndAsync(byte[] buffer, Function callback);
    protected static class ReadArrayToEOFAsyncExecutor extends IOInstanceNativeExecutor<ScriptSocketStream> {
        
    	ReadArrayToEOFAsyncExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_read);
		}
		
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocketStream sss, Argument[] args) throws Exception {
            ArrayValue array = getArray(args, 0);
            FuncValue callback = getFunction(args, 1);
            ObjectValue handleObj = getObject(args, 2);
            
            BasicArrayValue bav = (BasicArrayValue) array;
            byte[] target = (byte[]) bav.getPlatformArrayObject();
            
            StreamReadAsyncCallback cb = new StreamReadAsyncCallback(callback, handleObj);
            PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
            sss.readAsync(rt, target, 0, cb, handle, false);
            
            return VoidValue.DEFAULT;
        }
        
    }
	
    //private hosted void _writeAsync(byte[] buffer, int offset, int length, PromiseHandle handle);
    //Promise writeAsync(byte[] buffer, int offset, int length){
    protected static class WriteArrayAsyncExecutor extends IOInstanceNativeExecutor<ScriptSocketStream> {
        
    	WriteArrayAsyncExecutor() {
			super(PACON.Socket.Name, PACON.Socket.Op_write);
		}
    	
        @Override
        protected JValue apply(ThreadRuntime rt, ScriptSocketStream sss, Argument[] args) throws Exception {
            ArrayValue array = getArray(args, 0);
            int offset = getInt(args, 1);
            int length = getInt(args, 2);
            ObjectValue handleObj = getObject(args, 3);
            
            BasicArrayValue bav = (BasicArrayValue) array;
            byte[] target = (byte[]) bav.getPlatformArrayObject();
            PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
            sss.writeAsync(rt, target, offset, length, handle);
            
            return VoidValue.DEFAULT;
        }
        
    }
	//-------------------------- implementation at native end -------------------------//
	
	private Socket sock;
	private HostedValue scriptObj;
	//private Object lock = new Object();
    private Set<PromiseSettler> settlers;
			
	// Must keep this for the reflection-based instantiation. See setSocket(ScriptSocket) for initialization.
	public ScriptSocketStream() {
	    
	}
    
	// This is the effective initializer
    public void setSocket(ScriptSocket ssock, HostedValue ov) {
        settlers = new HashSet<PromiseSettler>();
        this.sock = ssock.getSocket();
        ssock.addListener(this);
        this.scriptObj = ov;
    }
    
    private boolean isWritable(){
    	BoolValue bv = (BoolValue)scriptObj.getMemberValue("isWritable");
    	return bv.getBoolValue();
    }
    
    @Override
    protected String getStreamType(){
        return "Socket";
    }
    
	@Override
	protected InputStream getInputStream(){
		try {
			return !isWritable() && !sock.isClosed() ? sock.getInputStream() : null;
		} catch (IOException e) {
			throw new JSEIOException(e);
		}
	}

	@Override
	protected OutputStream getOutputStream(){
		try {
			return isWritable() && !sock.isClosed() ? sock.getOutputStream() : null;
		} catch (IOException e) {
			throw new JSEIOException(e);
		}
	}

	/**
     * Write data => set handle.
     * 
     * @param rt
     * @param source Write bytes from this buffer ... 
     * @param offset from this position ... 
     * @param length for this length
     * @param handle The promise associated with this operation.
     */
    private void writeAsync(
        final ThreadRuntime rt, byte[] source, int offset, int length, PromiseHandleWrapper handle) {
        try {
            if (offset >= source.length){
                // Resolve
                handle.resolve(0);
                return;
            }

            AsyncSocketSession ass = rt.getThreadManager().getAsyncSocketSession(rt);
            ScriptStreamCallback ssc = new ScriptStreamCallback(rt, null, handle);
            SocketKey key = ass.registerSocketForWrite(sock, source, offset, length, ssc, ssc);
            registerPromiseHandle(handle, key, ssc);
            // key.enable();
        } catch (Exception e) {
            // Reject
            JSEIOException jseioe = new JSEIOException(e);
            JulianScriptException jse = jseioe.toJSE(rt, Context.createSystemLoadingContext(rt));
            handle.reject(jse.getExceptionValue());
        }
    }
    
	/**
	 * Read data => invoke callback (if not null) => set handle.
	 * 
	 * @param rt
	 * @param target Read bytes to this buffer
	 * @param offset
	 * @param callback Can be null
	 * @param handle The promise associated with this operation.
	 * @param forSync If true, this method is called in a synchronous fashion. 
	 * This option is used to enable sync API on top of async infrastructure.
	 */
	private void readAsync(
		final ThreadRuntime rt, 
		byte[] target, 
		int offset, 
		StreamReadAsyncCallback callback, 
		PromiseHandleWrapper handle, 
		boolean forSync) {
		try {
			if (offset >= target.length){
				// Invoke callback
				if (callback != null){
					callback.invokeWithReadCountAndHandle(rt, 0);
				}
				
				// Resolve
				handle.resolve(0);
				return;
			}

			AsyncSocketSession ass = rt.getThreadManager().getAsyncSocketSession(rt);
					
//			(AsyncSocketSession)rt.putLocal(
//			    IThreadLocalStorage.KEY_ASYNC_SOCKET_SESSION, 
//			    new IThreadLocalObjectFactory(){
//                    @Override
//                    public Object create() {
//                        return new AsyncSocketSession(rt);
//                    }
//    			});
			
			ScriptStreamCallback ssc = new ScriptStreamCallback(rt, callback, handle);
			SocketKey key = ass.registerSocketForRead(sock, target, ssc, ssc, offset, forSync);
			if (key == null) {
				// Resolve
				handle.resolve(-1);
				return;
			}
			registerPromiseHandle(handle, key, ssc);
			key.enable();
		} catch (Exception e) {
			// Reject
			JSEIOException jseioe = new JSEIOException(e);
			JulianScriptException jse = jseioe.toJSE(rt, Context.createSystemLoadingContext(rt));
			handle.reject(jse.getExceptionValue());
		}
	}
	
	//-------------------- IAsyncSocketCallback --------------------//

	class ScriptStreamCallback implements IAsyncSocketCallback, IAsyncChannelAware {
		
		private Box<Integer> totalRead;
		private ThreadRuntime ert;
		private StreamReadAsyncCallback callback;
		private PromiseHandleWrapper handle;
		private IAsyncChannel channel;
		private boolean repeat;
		private Object lock;
		private boolean readReadiness;
        private boolean shouldAbort;
        private SocketKey key;
		private ScriptSocketStream sss;
		
        /**
         * Create a new callback, which will be invoked at designated timings: pre-read, on-read, post-write, on-error.
         * 
         * @param rt
         * @param callback only used for repeated-read
         * @param handle
         */
		private ScriptStreamCallback(
			ThreadRuntime rt, StreamReadAsyncCallback callback, PromiseHandleWrapper handle) {
			totalRead = new Box<Integer>(0);
			ert = rt;
			this.callback = callback;
			this.handle = handle;
			this.repeat = callback != null;
			lock = new Object();
			readReadiness = true;

			sss = ScriptSocketStream.this;
		}
		
		Box<Integer> getCounter(){
		    return totalRead;
		}

	    // Return false if the stream should not be operated on anymore.
	    private boolean checkStreamState(ThreadRuntime runtime, int read){
            if (shouldAbort){
                return true;
            }
            
            if (read == -1){
                // -1 is the signal for normal closure. Pass this check so that the user handler has a chance 
                // to deal with the disconnection. We will settle the promise after the callback.
                return false;
            }
            
			if (channel.isClosed()) {
				throw new JSEIOException(getStreamType() + " stream is already closed.");
			}
			
    		if (runtime.getJThread().checkTermination()){
				// Reject due to termination
				handle.reject("Script engine is terminated.");
				return true;
        	}
    		
    		return false;
	    }
	    
		@Override
		public void onRead(final IOThreadHandle iothread, byte[] buffer, final int read) {
			iothread.post(
				ert, 
				new Executable(){

					@Override
					public Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) throws EngineInvocationError {
				    	try {
				    		// System.out.println("Read " + read);
				    		
				    		if (checkStreamState(runtime, read)) { 
				    			return Result.Void;
				    		}
							
							if (read > 0){
								totalRead.set(totalRead.get() + read);
							}
							
                            // Invoke callback
                            if (callback != null) {
                                callback.invokeWithReadCountAndHandle(runtime, read);
                            }

				    		if (checkStreamState(runtime, read)) {
				    			return Result.Void;
				    		}

				        	// Resolve if not called further
					        if (!repeat){
								handle.resolve(totalRead.get());
								complete(false);
					        } else if (read == -1){
                                handle.resolve(totalRead.get());
                                complete(true);
                            }
				    	} catch (JulianScriptException jse){
							// Reject
							handle.reject(jse.getExceptionValue());
                            complete(false);
				    	} catch (Exception ex){ // TODO - add cause?
							// Reject
							handle.reject(ex.getMessage()); 
                            complete(false);
				    	} finally {
				    		// Allow the next callback
				    	    synchronized(lock){
				    	        readReadiness = true;
				    	        lock.notifyAll();
				    	    }
				    	}

		    			return Result.Void;
					}
					
					private void complete(boolean closeChannel){
                        key.cancel(closeChannel);
                        iothread.complete();
                        ScriptStreamCallback.this.sss.unregisterPromiseHandle(handle, key);
					}
				});	
		}

		@Override
		public void beforeRead() {
            synchronized(lock){
                if (shouldAbort){
                    return;
                }
                
                if (readReadiness) {
                    // If ready, lock down the path and return. The following calls will block in the else branch.
                    readReadiness = false;
                } else {
                    // If not ready, wait until further notice to be sent from onRead
                    while (true) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            if (!ert.getThreadManager().isRunning()){
                                handle.reject("Script engine is terminated.");
                                break;
                            }
                        }
                        
                        if (shouldAbort){
                            break;
                        }
                        
                        if (readReadiness) {
                            readReadiness = false;
                            break;
                        }
                    }                     
                }
		    }
		}
		
		@Override
		public void afterWrite(int written){
            handle.resolve(written);
            ScriptStreamCallback.this.sss.unregisterPromiseHandle(handle, key);
		}

        @Override
        public void onError(Exception ex) {
            JSEIOException ex2 = new JSEIOException(ex);
            JulianScriptException jse = ex2.toJSE(ert, Context.createSystemLoadingContext(ert));
            handle.reject(jse.getExceptionValue());
            ScriptStreamCallback.this.sss.unregisterPromiseHandle(handle, key);
            shutdownStream();
        }   

		@Override
		public void setAsyncChannel(IAsyncChannel channel) {
			this.channel = channel;
		}
		
		void shutdownStream(){
            shouldAbort = true;
            synchronized(lock){
                lock.notifyAll();
            }
		}

        void setKey(SocketKey key) {
            this.key = key;
        }
	}

    //-------------------- Closing socket/stream to settle promises --------------------//
    
    // called when stream is closed
    @Override
    protected void close() throws IOException {
        settlePromise();
        super.close();
    }
    
    // called when socket is closed (ISocketEventListener)
    @Override
    public void onClose() {
        settlePromise();
    }
    
    // Make sure all the access and modification to settles are synchronized.
    private synchronized void settlePromise(){
        for (PromiseSettler settler : settlers){
            settler.settle();
        }
    }
    
    private synchronized void registerPromiseHandle(PromiseHandleWrapper handle, SocketKey key, ScriptStreamCallback ssc){
        ssc.setKey(key);
        settlers.add(new PromiseSettler(handle, key, ssc));
    }
    
    private synchronized void unregisterPromiseHandle(PromiseHandleWrapper handle, SocketKey key){
        settlers.remove(new PromiseSettler(handle, key, null));
    }
    
    private class PromiseSettler {
        
        PromiseSettler(PromiseHandleWrapper handle, SocketKey key, ScriptStreamCallback ssc) {
            this.handle = handle;
            this.key = key;
            this.ssc = ssc;
        }
        
        private PromiseHandleWrapper handle;
        private SocketKey key;
        private ScriptStreamCallback ssc;

        void settle() {
        	try {
                key.cancel(true);
                handle.resolve(ssc.getCounter().get());
        	} catch (Exception e) {
                handle.reject("Couldn't resolve promise triggered by socket/stream closure: " + e.getMessage());
        	} finally {
        		ssc.shutdownStream();
        	}
        }
        
        //--------------------- For HashSet ---------------------//
        
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((handle == null) ? 0 : handle.hashCode());
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            PromiseSettler other = (PromiseSettler) obj;
            if (handle == null) {
                if (other.handle != null)
                    return false;
            } else if (!handle.equals(other.handle))
                return false;
            if (key == null) {
                if (other.key != null)
                    return false;
            } else if (!key.equals(other.key))
                return false;
            return true;
        }
    }
}
