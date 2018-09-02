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

import info.julang.execution.threading.IOThreadHandle;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadRuntime;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A centralized facility that drives asynchronous operations for all sockets.
 * <p>
 * The session is backed by a polling thread and several IO-continuation threads. Multiple sockets can be registered 
 * into this session with read, write or both operations. The IO operation will be performed asynchronously by a JVM's
 * worker thread. Upon completion of each operation, a callback defined by the user, if available, will
 * be invoked in the IO thread managed by Julian.
 * <p>
 * <pre>
 * [Thread 0]                       [Thread 0]                [Thread 1]            [Thread 1]     [Thread 2]
 * (Julian worker thread)                                     (JVM worker thread)                  (Julian IO Thread)  
 * ======================           =====================     ==================    =========      ================
 * System.Network.Socket            System.Network.Socket     AsyncSocketSession       JVM         Julian IO Thread
 * (Julian)          [N]            (Java)            [N]            /|\     [1]
 *                                                                    |
 * #1 Socket.readToEndAsync() +-+                          +----------+
 *                              |                          |
 * #2 Socket.readToEndAsync() +-+-----> register socket ---+   +---> select --+---> read into ---> invoke callback
 *                              |       for READ              /|\             |     buffer         
 * #N Socket.readToEndAsync() +-+                              |              |
 *                                                             +--- (loop) ---+                                </pre>
 * 
 * As shown above, there will be three threads involved in any single asynchronous operations. A Julian worker thread 
 * [0], or main thread, kicks off async operation. The polling thread [1] inside this class will keep posting new 
 * works and dispatches them to one of several Julian's IO threads. So the total thread usage by this class will be 
 * 1 + K, where K is the size of IO thread pool, being configured with a very small number.
 * <p>
 * In contrast to read operation, it may not appear clear why async write is also needed. The write operation is 
 * initiated by our side and ideally should just go through. The reality, however, is the possible congestion on the 
 * network, causing adaptor to reject sending arbitrary size of data due to a full sending buffer. To make the process
 * of data sending asynchronous, first register a selection key the first time writeAsync() is called. Then every time
 * the script sends some data on that socket we queue a write operation item under the key. In the meantime, the 
 * poller thread continuously polls each socket's sending queue and tries to send pending data out, strictly in the
 * queued order. If all the data are sent, the key will get cancelled until the next time a new sending request comes 
 * in. 
 * 
 * @author Ming Zhou
 */
public class AsyncSocketSession {

	private Selector readSelector;
	private Selector writeSelector;
	private Thread poller;
	private Map<Socket, SocketKey> wsocks;
	private final JThreadManager manager;
	private final ThreadRuntime orgRt;
	
	public AsyncSocketSession(ThreadRuntime rt){
	    wsocks = new HashMap<Socket, SocketKey>();
		manager = rt.getThreadManager();
		orgRt = rt;
		poller = (new Thread(new Runnable(){

			@Override
			public void run() {
		        while(manager.isRunning()){
		            try{
		            	readFromSockets();
		            	writeToSockets();
		            } catch(IOException e){
		                // TODO
		                // 1. Move handling to each single socket
		                // 2. Should cancel the key and reject the promise 
		                e.printStackTrace();
		            }

		            try {
		                Thread.sleep(10);
		            } catch (InterruptedException e) {
		                // NO-OP
		            }
		        }
			}
			
		}));
		
		poller.start();
	}
	
	/**
	 * Returns null if the channel is closed.
	 */
	public SocketKey registerSocketForRead(
		Socket sock, byte[] buffer, IAsyncChannelAware aware, IAsyncSocketCallback callback, int offset, boolean forSync){
	    try {
			SocketChannel socketChannel = sock.getChannel();
			if (aware != null) {
				aware.setAsyncChannel(new AsyncChannel(socketChannel));
			}
			socketChannel.configureBlocking(false);
			Selector selector = getReadSelector();
			selector.selectNow(); // Performing a selection right now to clean up cancelled keys. 
			SelectionKey key = socketChannel.register(selector, SelectionKey.OP_READ);
			ReadAsyncSocket asock = new ReadAsyncSocket(socketChannel, buffer, callback, offset, forSync);
			key.attach(asock);
			return new SocketKey(key, asock, null);
		} catch (ClosedChannelException e) {
			return null;
		} catch (IOException e) {
			throw new JSESocketException("Couldn't perform asynchronous socket operation.", e);
		}
	}
	
	public SocketKey registerSocketForWrite(
        Socket sock, byte[] buffer, int offset, int length, IAsyncChannelAware aware, IAsyncSocketCallback callback){
        try {
            Selector selector = getWriteSelector();
            selector.selectNow(); // Performing a selection right now to clean up cancelled keys.
            SocketChannel socketChannel = sock.getChannel();
            if (aware != null) {
                aware.setAsyncChannel(new AsyncChannel(socketChannel));
            }
            
            synchronized(this){
                SocketKey sk = wsocks.get(sock);
                SelectionKey key = null;
                WriteAsyncSocket asock = null;
                if (sk == null){
                    socketChannel.configureBlocking(false);
                    key = socketChannel.register(selector, SelectionKey.OP_WRITE);
                    asock = new WriteAsyncSocket(sock, socketChannel);
                    key.attach(asock);
                    sk = new SocketKey(key, asock, sock);
                    wsocks.put(sock, sk);
                } else { // If the socket has been registered with a WRITE op, reuse it.
                    asock = (WriteAsyncSocket)sk.asock;
                }
                
                asock.addWriteOperation(buffer, offset, length, callback);
                return sk;
            }
        } catch (IOException e) {
            throw new JSESocketException("Couldn't perform asynchronous socket operation.", e);
        }
    }

    private synchronized Selector getReadSelector() throws IOException {
        if (readSelector == null) {
            readSelector = Selector.open();
        }
        return readSelector;
    }
    
    private synchronized Selector getWriteSelector() throws IOException {
        if (writeSelector == null) {
            writeSelector = Selector.open();
        }
        return writeSelector;
    }
	
	private void readFromSockets() throws IOException {
	    getReadSelector().selectNow();
        Set<SelectionKey> selectedKeys = readSelector.selectedKeys();
        for(SelectionKey key : selectedKeys){
            Object obj = key.attachment();
            if (obj != null){
                readFromSocket(key, (ReadAsyncSocket)obj);
            }    
        }
        
        // selectedKeys() returns the ref to internal set, not a shallow copy. So we must clean the set afterwards.
        selectedKeys.clear();
    }

	private void readFromSocket(SelectionKey key, ReadAsyncSocket asock) throws IOException {
		int read = asock.read();
		
        if(read == READ_RESULT_EOF){
            cancel(key, true);
        } else if(read == READ_RESULT_SYNC){
        	// If this is a sync read, immediately turn off the selection key to prevent any  
        	// further async read which would overwrite what we have received in the buffer.
            cancel(key, false);
        }
	}
	
    private void writeToSockets() throws IOException {
        getWriteSelector().selectNow();
        Set<SelectionKey> selectedKeys = writeSelector.selectedKeys();
        for(SelectionKey key : selectedKeys){
            Object obj = key.attachment();
            if (obj != null){
                writeToSocket(key, (WriteAsyncSocket)obj);
            }    
        }
        
        // selectedKeys() returns the ref to internal set, not a shallow copy. So we must clean the set afterwards.
        selectedKeys.clear();
    }
    
    private void writeToSocket(SelectionKey key, WriteAsyncSocket asock) throws IOException {
        if (asock.write()) {
            synchronized(this){
                if (asock.wops.isEmpty()) {
                    wsocks.remove(asock.socket);
                    cancel(key, false);
                }
            }
        }
    }
	
	private static void cancel(SelectionKey key, boolean closeChannel){
        try {
            key.attach(null);
            key.cancel();
            if (closeChannel){
                key.channel().close();
            }
        } catch (IOException e) {
            // thrown from close()
        }
	}
	
	class SocketKey {

        private Socket sock;
	    private SelectionKey key;
	    private IAsyncSocket asock;
	    
	    SocketKey(SelectionKey key, IAsyncSocket asock, Socket sock){
	        this.key = key;
	        this.asock = asock;
	        this.sock = sock;
	    }
	    
	    void cancel(boolean closeChannel){
	        AsyncSocketSession.cancel(key, closeChannel);
	        if (sock != null) {
	            synchronized(AsyncSocketSession.this){
	                AsyncSocketSession.this.wsocks.remove(key);
	            }
	        }
	    }
	    
	    void enable(){
	        asock.enable();
	    }
	}
	
	interface IAsyncSocket {
	    void enable();
	}
	
	class ReadAsyncSocket implements IAsyncSocket {

		private SocketChannel socketChannel;
		private IAsyncSocketCallback callback;

		private int offset;
        private boolean enabled;
		private byte[] scriptBuffer; // this overlaps with byte[] in script.
		private final int capacity = 8192;
		private byte[] src = new byte[capacity];
		private ByteBuffer srcBuffer = ByteBuffer.wrap(src);
		private boolean forSync;
		
		public ReadAsyncSocket(SocketChannel socketChannel, byte[] buffer, IAsyncSocketCallback callback, int offset, boolean forSync) {
			this.socketChannel = socketChannel;
			this.callback = callback;
			this.scriptBuffer = buffer;
			this.offset = offset;
			this.forSync = forSync;
		}
		
		public void enable() {
		    enabled = true;
        }

		// Potential improvement in future:
		// 1) use a byte array pool to handle massive sockets. OR 
		// 2) copy directly to the script buffer
        int read() {
		    if (!enabled) {
		        return READ_RESULT_NONE;
		    }
		    
			// Must wait until the previous callback has released the lock
			// on the target buffer. Otherwise we would be overwriting the 
			// buffer which is still being used by a previous callback.
			callback.beforeRead();
			
			srcBuffer.clear();
			
			// Read a number of bytes no more than (1) the script buffer can 
			// accommodate, and (2) the internal buffer can accommodate
			int maxRead = scriptBuffer.length - offset;
			if (maxRead > capacity) {
				maxRead = capacity;
			}
			
			srcBuffer.limit(maxRead);
			try {
    			int read = socketChannel.read(srcBuffer);
    			if (read > 0) {
    				// EQUIVALENT TO: System.arraycopy(src, 0, scriptBuffer, offset, maxRead);
    				srcBuffer.flip();
    				srcBuffer.get(scriptBuffer, offset, read);
    			}
                
                // Invoke Julian callback on an arbitrary IO thread.
    			IOThreadHandle iothread = AsyncSocketSession.this.manager.fetchIOThread(AsyncSocketSession.this.orgRt, true);
                callback.onRead(iothread, scriptBuffer, read);
                
                return forSync ? 
                	(read == READ_RESULT_EOF ? READ_RESULT_EOF : READ_RESULT_SYNC)
                	: read;
			} catch (IOException ioex) {
			    callback.onError(ioex);
			    
			    return READ_RESULT_NONE;
			}
		}
        
	}
	
	private static int READ_RESULT_NONE = 0;
	private static int READ_RESULT_EOF = -1;
	private static int READ_RESULT_SYNC = -2;
	
	class WriteAsyncSocket implements IAsyncSocket {

        private SocketChannel socketChannel;
        private Socket socket;

        private Queue<WriteOperation> wops = new LinkedBlockingQueue<WriteOperation>();
        
        public WriteAsyncSocket(Socket sock, SocketChannel socketChannel) {
            this.socketChannel = socketChannel;
            this.socket = sock;
        }
        
        public void enable() {
            // NO-OP
        }
        
        void addWriteOperation(byte[] scriptBuffer, int offset, int length, IAsyncSocketCallback callback){
            WriteOperation wo = new WriteOperation(scriptBuffer, offset, length, callback);
            wops.offer(wo);
        }
        
        /**
         * Called when the underlying socket is ready to write.
         * @return true if there are no more pending data to write.
         */
        boolean write() {
            WriteOperation wo = wops.peek();
            if (wo != null) {
                try {
                    if (wo.write(socketChannel)) {
                        // If all the pending bytes in this op are written, the op is done and can be removed.
                        wops.remove();
                    }

                    wo.settle(null);
                } catch (IOException ioex) {
                    wo.settle(ioex);
                }
            }
            
            return wops.isEmpty();
        }
	}
    
    private class WriteOperation {
       
        private ByteBuffer srcBuffer;
        private int rem;
        private int length;
        private IAsyncSocketCallback callback;
        
        WriteOperation(
            byte[] scriptBuffer, int offset, int length, IAsyncSocketCallback callback){
            this.callback = callback;
            byte[] src = new byte[length];
            System.arraycopy(scriptBuffer, offset, src, 0, length);
            srcBuffer = ByteBuffer.wrap(src);
            this.length = rem = length;
        }

        // Return true if the entire buffer is written
        boolean write(SocketChannel sc) throws IOException {
            int written = sc.write(srcBuffer);
            rem -= written;
            return rem <= 0;
        }
        
        void settle(IOException ioex){
            if (ioex == null) {
                callback.afterWrite(length);
            } else {
                callback.onError(ioex);
            }
        }
    }
}