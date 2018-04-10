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

package info.julang.typesystem.jclass.jufc.System;

import java.io.InputStream;
import java.io.OutputStream;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.jufc.System.IO.JFileStream;

public class ProcessPipeStream extends JFileStream {
	
	static final String FullTypeName = "System.PipeStream";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("write", new JFileStream.WriteExecutor())
				.add("writeArray", new JFileStream.WriteArrayExecutor())
				.add("read", new JFileStream.ReadExecutor())
				.add("readArray", new JFileStream.ReadArrayExecutor())
				.add("skip", new JFileStream.SkipExecutor())
				.add("flush", new JFileStream.FlushExecutor())
				.add("close", new JFileStream.CloseExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ProcessPipeStream> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ProcessPipeStream jfs, Argument[] args) throws Exception {
			HostedValue hv = getHosted(args, 0);
			int fd = getInt(args, 1);
			jfs.init((JProcess)hv.getHostedObject(), fd);
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	//-------------------------- implementation at native end -------------------------//
	
	private int fd = -1;
	private JProcess proc;
	private Object lock = new Object();
	
	@Override
	protected String getStreamType(){
		return "Process";
	}
	
	// Must keep this for the reflection-based instantiation
	public ProcessPipeStream() {

	}
	
	// Only used for creating streams for current process
	ProcessPipeStream(CurrentProcess proc, int fd) {
		this.proc = proc;
		this.fd = fd;
	}
	
	private void init(JProcess proc, int fd) {
		this.proc = proc;
		this.fd = fd;
	}
	
	@Override
	protected InputStream getInputStream(){
		if (fis == null){
			synchronized(lock){
				if (fis == null){
					fis = proc.getInputStream(fd == 2);
				}
			}
		}
		
		return fis;
	}

	@Override
	protected OutputStream getOutputStream(){
		if (fos == null){
			synchronized(lock){
				if (fos == null){
					fos = proc.getOutputStream(fd == 2);
				}
			}
		}
		
		return fos;
	}
}
