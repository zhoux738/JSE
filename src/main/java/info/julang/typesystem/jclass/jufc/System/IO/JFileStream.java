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

import info.julang.execution.Argument;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.interop.FunctionCaller;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.Concurrency.PromiseHandleWrapper;
import info.julang.util.Box;

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
		
		public ReadExecutor() { }

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			int i = jfs.read();
			return TempValueFactory.createTempIntValue(i);
		}
		
	}
	
	protected static class ReadArrayExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public ReadArrayExecutor() { }

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
		
		public WriteExecutor() { }

		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			byte b = getByte(args, 0);
			jfs.write(b);
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _write(byte[] buffer, int offset, int count);
	protected static class WriteArrayExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		public WriteArrayExecutor() { }

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
	
	// private hosted void _readAsync(byte[] buffer, int offset, int count, Function callback, PromiseHandle handle);
	protected static class ReadArrayAsyncExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			int offset = getInt(args, 1);
			FuncValue callback = getFunction(args, 2);
			ObjectValue handleObj = getObject(args, 3);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			FileStreamReadAsyncCallbackInvoker cb = new FileStreamReadAsyncCallbackInvoker(rt, callback, handleObj);
			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			jfs.readAsync(rt, target, offset, false, cb, handle);
			
			return VoidValue.DEFAULT;
		}
		
	}
	
	// private hosted void _readAllAsync(byte[] buffer, Function callback, PromiseHandle handle);
	protected static class ReadArrayToEOFAsyncExecutor extends IOInstanceNativeExecutor<JFileStream> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JFileStream jfs, Argument[] args) throws Exception {
			ArrayValue array = getArray(args, 0);
			FuncValue callback = getFunction(args, 1);
			ObjectValue handleObj = getObject(args, 2);
			
			BasicArrayValue bav = (BasicArrayValue) array;
			byte[] target = (byte[]) bav.getPlatformArrayObject();

			FileStreamReadAsyncCallbackInvoker cb = new FileStreamReadAsyncCallbackInvoker(rt, callback, handleObj);
			PromiseHandleWrapper handle = new PromiseHandleWrapper(rt, handleObj);
			jfs.readAsync(rt, target, 0, true, cb, handle);
			
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
		if (closed) {
			throw new JSEIOException(getStreamType() + " stream is already closed.");
		}
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
	
	// read data => invoke callback => set handle
	private void readAsync(ThreadRuntime rt, byte[] target, int offset, boolean repeat, FileStreamReadAsyncCallbackInvoker callback, PromiseHandleWrapper handle) {
		try {
			if (offset >= target.length){
				// Invoke callback
				callback.invokeWithReadCountAndHandle(0);
				// Resolve
				handle.resolve(0);
				
				return;
			}
		
			Path p = Paths.get(path);
			AsynchronousFileChannel achan = AsynchronousFileChannel.open(p, StandardOpenOption.READ);
			ByteBuffer buffer = ByteBuffer.wrap(target);
			Box<Integer> box = new Box<Integer>(0);
			readAsync(rt.getJThread(), buffer, achan, offset, repeat, callback, handle, box);
		} catch (IOException e) {
			// Reject
			JSEIOException jseioe = new JSEIOException(e);
			JulianScriptException jse = jseioe.toJSE(rt, Context.createSystemLoadingContext(rt));
			handle.reject(jse.getExceptionValue());
		}
	}
	
	/**
	 * Reads from a file channel, and invokes callback once or multile times until EOF.
	 * 
	 * @param thread    the thread on which this was initiated. The callback is not necessarily run on the same thread.
	 * @param buffer    a buffer to hold the read data.
	 * @param achan     the file channel
	 * @param position  the initial position on the buffer to read the data into
	 * @param repeat    whether to repeat the reading process. If false, read only once, either filling buffer or hitting EOF; if 
	 *                  true, read as many times as it takes to hit EOF, always filling the buffer except perhaps the last time. 
	 * @param callback  the user callback to be invoked after each time the channel was read 
	 * @param handle    the promise handle of type <code><font color="green">System.Concurrency.PromiseHandle</font></code> that 
	 *                  can be used to settle the promise associated with this asynchronous call.
	 * @param totalRead the count of bytes read so far
	 */
	private void readAsync(
		final JThread thread,
		final ByteBuffer buffer, 
		final AsynchronousFileChannel achan, 
		final long position, 
		final boolean repeat,
		final FileStreamReadAsyncCallbackInvoker callback, 
		final PromiseHandleWrapper handle,
		final Box<Integer> totalRead) {
		
		achan.read(
			buffer, position, buffer, 
			new CompletionHandler<Integer, ByteBuffer>() {
				
				@Override
			    public void completed(Integer read, ByteBuffer bb) {
			    	try {
			    		if (checkStreamState()) { 
			    			return;
			    		}

						bb.flip();
						
						if (read > 0){
							totalRead.set(totalRead.get() + read);
							
					        // Invoke callback
							callback.invokeWithReadCountAndHandle(read);
						}

			    		if (checkStreamState()) { 
			    			return;
			    		}
						
				        // Call recursively
				        if (read > 0 && repeat){
				        	readAsync(thread, buffer, achan, position + read, repeat, callback, handle, totalRead);
				        } else {
				        	// Resolve
							handle.resolve(totalRead.get());
				        }
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
			    }
			    
			    // Return false if the stream should not be operated on anymore.
			    private boolean checkStreamState(){
					if (JFileStream.this.closed) {
						throw new JSEIOException(getStreamType() + " stream is already closed.");
					}
					
		    		if (thread.checkTermination()){
						// Reject due to termination
						handle.reject("Script engine is terminated.");
						return true;
		        	}
		    		
		    		return false;
			    }
			}
		);
	}
}

/**
 * Dynamically invokes user's callback with (int read, ProcessHandle handle).
 * 
 * @author Ming Zhou
 */
class FileStreamReadAsyncCallbackInvoker extends FunctionCaller {

	private ObjectValue handleObj;
	
	public FileStreamReadAsyncCallbackInvoker(ThreadRuntime rt, FuncValue fv, ObjectValue handleObj) {
		super(rt, fv, true);
		this.handleObj = handleObj;
	}
	
	public void invokeWithReadCountAndHandle(int read){
		this.call(new JValue[] {
			TempValueFactory.createTempIntValue(read),
			TempValueFactory.createTempRefValue(handleObj)
		}, true);
	}
	
}