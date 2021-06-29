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

package info.julang.typesystem.jclass.jufc.System.IO;

import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.IOThreadHandle;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.NullIOThreadHandle;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.Concurrency.PromiseHandleWrapper;
import info.julang.util.Box;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class JFileStream {
	
	private static final String FullTypeName = "System.IO.FileStream";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("write", new WriteExecutor())
				.add("writeArray", new WriteArrayExecutor())
				.add("read", new ReadExecutor())
				.add("readArray", new ReadArrayExecutor())
				.add("skip", new SkipExecutor())
				.add("flush", new FlushExecutor())
				.add("close", new CloseExecutor())
				.add("writeAsync", new WriteArrayAsyncExecutor())
				.add("readAsync", new ReadArrayAsyncExecutor())
				.add("readAllAsync", new ReadArrayToEOFAsyncExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JFileStream> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JFileStream jfs, Argument[] args) throws Exception {
			String path = getString(args, 0);
			int imode = getEnumAsOrdinal(args, 1);
			FileMode mode = FileMode.values()[imode];
			jfs.init(path, mode);
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	protected static class SkipExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public SkipExecutor() { }

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			int count = getInt(args, 0);
			count = jfs.skip(count);
			return TempValueFactory.createTempIntValue(count);
		}
		
	}
	
	protected static class ReadExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public ReadExecutor() { 
			super(PACON.IO.Name, PACON.IO.Op_read);
		}

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			int i = jfs.read();
			return TempValueFactory.createTempIntValue(i);
		}
		
	}
	
	protected static class ReadArrayExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public ReadArrayExecutor() { 
			super(PACON.IO.Name, PACON.IO.Op_read);
		}

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			int count = getInt(args, 2); // guaranteed to be non-negative in script
			
			int len = array.getLength();
			if (offset >= len){
				// cannot read more
				return TempValueFactory.createTempIntValue(0);
			}
			
			if (offset + count > len) {
				// read what can be accommodated
				count = len - offset;
			}
			
			if (count > 0){
				BasicArrayValue bav = (BasicArrayValue) array;
				byte[] target = (byte[]) bav.getPlatformArrayObject();
				count = jfs.read(target, offset, count);
			}
			
			return TempValueFactory.createTempIntValue(count);
		}
		
	}
	
	protected static class WriteExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public WriteExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			byte b = getByte(args, 0);
			jfs.write(b);
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _write(byte[] buffer, int offset, int count);
	protected static class WriteArrayExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public WriteArrayExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			int count = getInt(args, 2); // guaranteed to be non-negative in script
			
			int len = array.getLength();
			if (offset >= len){
				// no more to write
				return VoidValue.DEFAULT;
			}
			
			if (offset + count > len) {
				// write what is left
				count = len - offset;
			}
			
			if (count > 0){
				BasicArrayValue bav = (BasicArrayValue) array;
				byte[] target = (byte[]) bav.getPlatformArrayObject();
				jfs.write(target, offset, count);
			}
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	protected static class CloseExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public CloseExecutor() { }

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			jfs.close();
			return VoidValue.DEFAULT;
		}
		
	}
	
	protected static class FlushExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public FlushExecutor() { }
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			jfs.flush();
			return VoidValue.DEFAULT;
		}
		
	}
	
	//---------------------------------- AsyncStream ----------------------------------//
	
	// private hosted void _writeAsync(byte[] buffer, int offset, PromiseHandle handle);
	protected static class WriteArrayAsyncExecutor extends IOInstanceNativeExecutor<JFileStream> {
	
		WriteArrayAsyncExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_write);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			ObjectValue handleObj = getObject(args, 2);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			//FileStreamReadAsyncCallbackInvoker cb = new FileStreamReadAsyncCallbackInvoker(rt, callback, handleObj);
			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			jfs.writeAsync(rt, target, offset, handle);
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _readAsync(byte[] buffer, int offset, Function callback, PromiseHandle handle);
	protected static class ReadArrayAsyncExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		ReadArrayAsyncExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			//FuncValue callback = getFunction(args, 2);
			ObjectValue handleObj = getObject(args, 2);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			//FileStreamReadAsyncCallbackInvoker cb = new FileStreamReadAsyncCallbackInvoker(rt, callback, handleObj);
			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			jfs.readAsync(rt, target, offset, null, handle);
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _readAllAsync(byte[] buffer, Function callback, PromiseHandle handle);
	protected static class ReadArrayToEOFAsyncExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		ReadArrayToEOFAsyncExecutor() {
			super(PACON.IO.Name, PACON.IO.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			FuncValue callback = getFunction(args, 1);
			ObjectValue handleObj = getObject(args, 2);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			StreamReadAsyncCallback cb = new StreamReadAsyncCallback(callback, handleObj);
			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			jfs.readAsync(rt, target, 0, cb, handle);
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	//-------------------------- implementation at native end -------------------------//
	//----- code under this line is purely Java and is unaware of Julian runtime ------//
	
	private enum FileMode {
		// If file doesn't exist, create one; if it does, append new contents to the end. File is in WRITE mode.
		Append,
		
		// If file doesn't exist, create one; if it does, truncate the contents. File is in WRITE mode.
		Create,

		// Open an existing file. If it doesn't exist, throw IOException. File is in READ mode.
		Open
	}
	
	protected InputStream fis;
	protected OutputStream fos;
	private String path;
	private boolean closed;
	
	protected InputStream getInputStream(){
		return fis;
	}

	protected OutputStream getOutputStream(){
		return fos;
	}
	
	protected String getStreamType(){
		return "File";
	}
	
	protected int skip(int count) throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		return (int) getInputStream().skip(count);
	}
	
	protected int read() throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		return getInputStream().read();
	}
	
	protected int read(byte[] buffer, int offset, int count) throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		return getInputStream().read(buffer, offset, count);
	}
	
	protected void write(byte data) throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		getOutputStream().write(data);
	}
	
	protected void write(byte[] buffer, int offset, int count) throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		getOutputStream().write(buffer, offset, count);
	}
	
	protected void flush() throws IOException {
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
		getOutputStream().flush();
	}
	
	protected void close() throws IOException {
		try {
			if (getInputStream() != null) {
				getInputStream().close();
			}
			if (getOutputStream() != null) {
				getOutputStream().close();
			} 
		} finally {
			closed = true;
		}
	}
	
	private void init(String path, FileMode mode) {
		try {
			this.path = path;
			switch(mode){
			case Append: 
				{
					boolean created = false;
					File f = new File(path);
					if (!(created = f.exists())){
						created = f.createNewFile();
					}
					
					if (created){
						fos = new FileOutputStream(path, true);
					} else {
						throw new JSEIOException("Failed to obtain a file stream: cannot create file.");
					}
				}
				break;
			case Create:
				{
					boolean created = false;
					File f = new File(path);
					if (!(created = f.exists())){
						created = f.createNewFile();
					}
					
					if (created){
						fos = new FileOutputStream(path, false);
						((FileOutputStream)fos).getChannel().truncate(0);
					} else {
						throw new JSEIOException("Failed to obtain a file stream: cannot create file.");
					}
				}
				break;
			case Open:
				{
					File f = new File(path);
					if (!f.exists()){
						throw new JSEIOException("File '" + path + "' doesn't exist.");
					}
					
					fis = new FileInputStream(path);		
				}
				break;
			default:
				break;
			}
		} catch (FileNotFoundException e) {
			throw new JSEIOException("File '" + path + "' doesn't exist.");
		} catch (IOException e) {
			throw new JSEIOException(e);
		}
	}
	
	/*
	 * An overview of asynchronous API implementation.
	 * 
	 * When calling readAsync() on Julian's FileStream, the user provide a callback, 
	 * which takes the read count and a promise handle. readAsync() returns immediately
	 * with a deferred promise that can be chained into. The callback will be invoked 
	 * after the system reads the data off of the source.
	 * 
	 * Both user's callback, and JuFC bridge (this class), are able to settle the 
	 * promise using the handle. Whoever does first wins.
	 * 
	 * If the user's callback resolves the promise explicitly, the promise continues 
	 * with whatever result is set by the user. Otherwise, the promise continues with 
	 * the total count of read bytes.
	 * 
	 *  ------------- Julian ---------------|------------------ JAVA ------------------
	 *  
	 *  User's code         FileStream                  JFileStream         Platform/OS
	 *  
	 *  [creates]           [creates]
	 *    callback            promise handle
	 *    
	 *  [calls]             [calls]                     [calls]
	 *     |                   |                           |
	 *     |                   +-------------------------->|
	 *     |                   |                           |
	 *     |                   |                           +------------------->|
	 *     |                   |                           |                    |
	 *     |                   |                           |<-----(callback)----+
	 *     |                   |                           |
	 *     |<--------------------------(callback)-----+    | 
	 *     |                   |           ||          \   | 
	 *     |   +---(resolve)-->|          <OR>          +--+
	 *     |  /       ||       |           ||          /   |
	 *     +-+       <OR>      |<-------(reject)------+    |
	 *     |  \       ||       |           ||              |    
	 *     |   +---(reject)--->|          <OR>             |
	 *     |                   |           ||              |
	 *     |                   |<-------(resolve)----------+
	 *     |                   |                           | 
	 */
	
	/**
	 * Read data => invoke callback (if not null) => set handle.
	 * 
	 * @param rt
	 * @param target Read bytes to this buffer
	 * @param offset
	 * @param callback Can be null
	 * @param handle
	 */
	private void readAsync(ThreadRuntime rt, byte[] target, int offset, StreamReadAsyncCallback callback, PromiseHandleWrapper handle) {
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
		
			Path p = Paths.get(path);
			AsynchronousFileChannel achan = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
			ByteBuffer buffer = ByteBuffer.wrap(target);
			Box<Integer> box = new Box<Integer>(0);
			readAsync0(rt, null, buffer, achan, offset, callback, handle, box);
		} catch (IOException e) {
			// Reject
			JSEIOException jseioe = new JSEIOException(e);
			JulianScriptException jse = jseioe.toJSE(rt, Context.createSystemLoadingContext(rt));
			handle.reject(jse.getExceptionValue());
		}
	}
	
	private void writeAsync(
		ThreadRuntime rt, byte[] source, int offset, PromiseHandleWrapper handle) {
		try {
			if (offset >= source.length){
				// Resolve
				handle.resolve(0);
				return;
			}
		
			Path p = Paths.get(path);
			AsynchronousFileChannel achan = AsynchronousFileChannel.open(p, StandardOpenOption.WRITE);
			ByteBuffer buffer = ByteBuffer.wrap(source);
			buffer.position(offset);
			Box<Integer> box = new Box<Integer>(0);
			writeAsync0(rt, null, buffer, achan, handle, box);
		} catch (IOException e) {
			// Reject
			JSEIOException jseioe = new JSEIOException(e);
			JulianScriptException jse = jseioe.toJSE(rt, Context.createSystemLoadingContext(rt));
			handle.reject(jse.getExceptionValue());
		}
	}
	
	/**
	 * Reads from a file channel, invokes callback 0+ times until EOF, upon which settles the promise.
	 * 
	 * @param rt        The runtime for the thread on which this was initiated. The callback is not necessarily run on the 
	 *                  same thread.
	 * @param iothread  A Julian IO thread on which to invoke the callback. If null, this method will fetch one if the 
	 *                  callback is not null.
	 * @param buffer    A buffer to hold the read data.
	 * @param achan     The file channel.
	 * @param position  The initial position on the buffer to read the data into. 
	 * @param callback  The user callback to be invoked after each time the channel was read. If null, the call will finish 
	 *                  after the first read which either filled the buffer or hit EOF. If not null, will call this method 
	 *                  recursively, and upon end of each call the buffer will be filled out, except perhaps the last time 
	 *                  when the EOF was hit first. After each read, the callback will be invoked on the given IO thread.  
	 * @param handle    The promise handle of type <code style="color:green">System.Concurrency.PromiseHandle</code> 
	 *                  that can be used to settle the promise associated with this asynchronous call.
	 * @param totalRead The count of bytes read so far.
	 */
	private void readAsync0(
		final ThreadRuntime rt,
		IOThreadHandle iothread0,
		final ByteBuffer buffer, 
		final AsynchronousFileChannel achan, 
		final long position, 
		final StreamReadAsyncCallback callback, 
		final PromiseHandleWrapper handle,
		final Box<Integer> totalRead) {
		
		final JThread thread = rt.getJThread();
		final boolean repeat = callback != null;
		
		// initialize io-thread
		final IOThreadHandle iothread = 
			iothread0 != null ? 
				iothread0 : // Use the given io-thread
				repeat ? 
					rt.getThreadManager().fetchIOThread(rt, true) : // Create a new one only if this is a repeated call
					NullIOThreadHandle.INSTANCE;
					
		achan.read(
			buffer, position, buffer, 
			new CompletionHandler<Integer, ByteBuffer>() {
				
				@Override
			    public void completed(final Integer read, final ByteBuffer bb) {
					if (repeat) {
						// [Code path for readToEndAsync]
						// Once completed for each read, invoke the callback on Julian's IO thread.
						iothread.post(
							thread.getThreadRuntime(), 
							new Executable(){

								@Override
								public Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) throws EngineInvocationError {
							    	try {
							    		if (checkStreamState()) { 
							    			return Result.Void;
							    		}
		
										bb.flip();
										
										if (read > 0){
											totalRead.set(totalRead.get() + read);
											
									        // Invoke callback
											callback.invokeWithReadCountAndHandle(runtime, read);
										}
		
							    		if (checkStreamState()) {
							    			return Result.Void;
							    		}
										
								        // Call recursively
								        if (read > 0 && repeat){
								        	readAsync0(rt, iothread, buffer, achan, position + read, callback, handle, totalRead);
								        } else {
								        	// Resolve
											handle.resolve(totalRead.get());
											iothread.complete();
								        }
							    	} catch (JulianScriptException jse){
										// Reject
										handle.reject(jse.getExceptionValue());
										iothread.complete();
							    	} catch (Exception ex){
										// Reject
										handle.reject(ex.getMessage()); // TODO - add cause?
										iothread.complete();
							    	}

					    			return Result.Void;
								}
							
							});						
					} else {
						// [Code path for readAsync]
						// There is no callback, just settle the promise
						try {
				    		if (checkStreamState()) { 
				    			return;
				    		}

							bb.flip();
							
							if (read > 0){
								totalRead.set(totalRead.get() + read);
							}

				    		if (checkStreamState()) { 
				    			return;
				    		}
							
				    		// Resolve
							handle.resolve(totalRead.get());
				    	} catch (JulianScriptException jse){
							// Reject
							handle.reject(jse.getExceptionValue());
				    	} catch (Exception ex){
							// Reject
							handle.reject(ex.getMessage()); // TODO - add cause?
				    	}
					}
			    }
	
			    @Override
			    public void failed(Throwable ex, ByteBuffer attachment) {
					// Reject
			    	if (ex instanceof JulianScriptException){
						handle.reject(((JulianScriptException)ex).getExceptionValue());
			    	} else {
						handle.reject(ex.getMessage()); // TODO - add cause?
			    	}
                    iothread.complete();
			    }
			    
			    // Return false if the stream should not be operated on anymore.
			    private boolean checkStreamState(){
					if (JFileStream.this.closed) {
						throw new JSEIOException(getStreamType() + " stream is already closed.");
					}
					
		    		if (thread.checkTermination()){
						// Reject due to termination
						handle.reject("Script engine is terminated.");
                        iothread.complete();
						return true;
		        	}
		    		
		    		return false;
			    }
			}
		);
	}

	/**
	 * Writes into a file channel, and settles the promise at the end.
	 * 
	 * @param rt        The runtime for the thread on which this was initiated. The callback is not necessarily run on the 
	 *                  same thread.
	 * @param iothread  A Julian IO thread on which to invoke the callback. If null, this method will fetch one if the 
	 *                  callback is not null.
	 * @param buffer    A buffer to hold the data to write.
	 * @param achan     The file channel.
	 * @param position  The initial position on the buffer to read the data into.
	 * @param handle    The promise handle of type <code style="color:green">System.Concurrency.PromiseHandle</code> 
	 *                  that can be used to settle the promise associated with this asynchronous call.
	 */
	private void writeAsync0(
		final ThreadRuntime rt,
		IOThreadHandle iothread0,
		final ByteBuffer buffer, 
		final AsynchronousFileChannel achan,
		final PromiseHandleWrapper handle,
		final Box<Integer> totalWritten) throws IOException {
		
		final long offset = achan.size();
		final JThread thread = rt.getJThread();
	
		// initialize io-thread
		final IOThreadHandle iothread = 
			iothread0 != null ? 
				iothread0 : // Use the given io-thread
				NullIOThreadHandle.INSTANCE;
		
		achan.write(
			buffer, offset, buffer, 
			new CompletionHandler<Integer, ByteBuffer>() {
				
				@Override
			    public void completed(final Integer wrote, final ByteBuffer bb) {
					try {
			    		if (checkStreamState()) { 
			    			return;
			    		}

						bb.flip();
						
						if (wrote > 0){
							totalWritten.set(totalWritten.get() + wrote);
						}

			    		if (checkStreamState()) { 
			    			return;
			    		}
						
			    		// Resolve
						handle.resolve(totalWritten.get());
			    	} catch (JulianScriptException jse){
						// Reject
						handle.reject(jse.getExceptionValue());
			    	} catch (Exception ex){
						// Reject
						handle.reject(ex.getMessage()); // TODO - add cause?
			    	}
			    }
	
			    @Override
			    public void failed(Throwable ex, ByteBuffer attachment) {
					// Reject
			    	if (ex instanceof JulianScriptException){
						handle.reject(((JulianScriptException)ex).getExceptionValue());
			    	} else {
						handle.reject(ex.getMessage()); // TODO - add cause?
			    	}
                    iothread.complete();
			    }
			    
			    // Return false if the stream should not be operated on anymore.
			    private boolean checkStreamState(){
					if (JFileStream.this.closed) {
						throw new JSEIOException(getStreamType() + " stream is already closed.");
					}
					
		    		if (thread.checkTermination()){
						// Reject due to termination
						handle.reject("Script engine is terminated.");
                        iothread.complete();
						return true;
		        	}
		    		
		    		return false;
			    }
			}
		);
	}
}