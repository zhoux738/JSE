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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import info.julang.execution.Argument;
import info.julang.execution.security.PACON;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadAbortedException;
import info.julang.execution.threading.SystemInitiatedThreadRuntime;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.JIllegalStateException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.BasicArrayValue;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IArrayValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.PresetBasicArrayValueFactory;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.JClassTypeUtil;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.typesystem.jclass.jufc.SystemTypeUtility;
import info.julang.typesystem.jclass.jufc.System.Collection.JMap;
import info.julang.typesystem.jclass.jufc.System.IO.IOInstanceNativeExecutor;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;
import info.julang.util.Pair;

/**
 * The platform end for <code><font color="green">System.Process</font></code>.
 * <p>
 * The most important feature of <code><font color="green">System.Process</font></code> is the support for IO 
 * redirection. The process has three standard IO channels: Input, Output and Error. When starting a new 
 * process, the caller defines whether using inherited IO from the parent process, which in this case is always 
 * the JVM on which Julian engine is running. If <code>inheritedIO = true</code>, the process simply directs IO 
 * to that of the parent process. If not, the IO would be connected to OS pipes.  
 * <p>
 * Whatever value <code>inheritedIO</code> is, the caller may always overwrite a specific channel with an 
 * instance of <code><font color="green">System.IO.Stream</font></code>. Then the subprocess will use that 
 * stream to input or output. In particular, it calls <code>read()</code> or <code>write()</code> method on that 
 * stream object. (TODO) One exception is that, if the stream is exactly of type <code><font color="green">
 * System.IO.FileStream</font></code>, the underlying file will be used directly, bypassing method call. This is 
 * a performance optimization.
 * <p>
 * If a channel is not using <code>inheritedIO</code>, and it is not specified with a stream object, the IO is 
 * directed to an OS pipe. Only in such case, one may call <code>getReadStream()</code> or <code>getErrorStream()
 * </code> to read the output/error from the subprocess, or call <code>getWriteStream()</code> to send input to 
 * it. Otherwise these methods return null.
 * 
 * @author Ming Zhou
 */
public class JProcess {

	private static final String FullTypeName = "System.Process";
	private static final String ProcessState_FullTypeName = "System.ProcessState";
	private static final String WriteMethodName = "write";
	private static final String FlushMethodName = "flush";
	private static final String ReadMethodName = "read";
	
	//------------ IRegisteredMethodProvider -------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("wait", new WaitExecutor())
				.add("waitFor", new WaitForExecutor())
				.add("start", new StartExecutor())
				.add("kill", new KillExecutor())
				.add("getExitCode", new GetExitCodeExecutor())
				.add("getCurrent", new GetCurrentExecutor())
				.add("getEnvArg", new GetEnvArgExecutor());
		}
		
	};

	//----------------- native executors -----------------//
	
	private static class GetCurrentExecutor extends StaticNativeExecutor<JProcess> {

		private HostedValue hv;
		
		GetCurrentExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			if (hv == null) {
				synchronized(JProcess.class){
					if (hv == null) {
						createCurrentProcessObject(rt);
					}
				}
			}
			
			return hv;
		}
		
		private void createCurrentProcessObject(ThreadRuntime rt){
			JType typ = SystemTypeUtility.ensureTypeBeLoaded(rt, JProcess.FullTypeName);

			CurrentProcess cp = new CurrentProcess();
			hv = new HostedValue(rt.getHeap(), typ);
			
			// Set fields on Julian object
			
			// private ProcessState state
			typ = SystemTypeUtility.ensureTypeBeLoaded(rt, ProcessState_FullTypeName);
			JEnumType etype = (JEnumType) typ;
			EnumValue ev = new EnumValue(rt.getStackMemory().currentFrame(), etype, 1, "IN_PROGRESS");
			ev.assignTo(hv.getMemberValue("state"));
			
			// This needs more investigation. For now just make something up. JMX doesn't provide a 
			// solution. Runtime bean doesn't contain cmdline args to main() method, and property
			// "sun.java.command" is only available on Sun/Oracle JVM. The process name is also nowhere
			// to find.
			/*
			String jmxName = ManagementFactory.getRuntimeMXBean().getName();
			// The JMX name has a possible format of {PID}@{HOSTNAME}. This is not guaranteed, but worth trying.
			String[] parts = jmxName.split("@");
			if (parts.length == 2){
				int pid = Integer.parseInt(parts[0]);
				// TODO - get process by PID. No immediate solution with JDK classes. One awkward approach is:
				try {
				    String line;
				    Process p = Runtime.getRuntime().exec(isLinux? "ps -e" : (System.getenv("windir") + "\\system32\\"+"tasklist.exe"));
				    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
				    while ((line = input.readLine()) != null) {
				        ... ... // Parse data here
				    }
				    input.close();
				} catch (Exception e) {
				    .. ... 
				}
			*/

			// private string name
			StringValue name = TempValueFactory.createTempStringValue("julian");
			name.assignTo(hv.getMemberValue("name"));

			// private string args
			String oneArg = System.getProperty("sun.java.command");
			if (oneArg != null) {
				String[] rargs = oneArg.split("\\s+");
				int len = rargs.length;
				ArrayValue av = ArrayValueFactory.createArrayValue(rt.getHeap(), rt.getTypeTable(), JStringType.getInstance(), len);
				for (int i = 0 ; i < len; i++){
					StringValue sv = TempValueFactory.createTempStringValue(rargs[i]);
					sv.assignTo(av.getValueAt(i));
				}

				av.assignTo(hv.getMemberValue("args"));	
			}
			
			// private PipeStream wirteStream;
			// private PipeStream readStream;
			// private PipeStream errorStream;
			typ = SystemTypeUtility.ensureTypeBeLoaded(rt, ProcessPipeStream.FullTypeName);
			setPipeStream(rt, typ, cp, "readStream", 0);
			setPipeStream(rt, typ, cp, "wirteStream", 1);
			setPipeStream(rt, typ, cp, "errorStream", 2);
			
			hv.setHostedObject(cp);
		}
		
		private void setPipeStream(ThreadRuntime rt, JType et, CurrentProcess cp, String fieldName, int fd){
			HostedValue h = new HostedValue(rt.getHeap(), et);
			ProcessPipeStream stream = new ProcessPipeStream(cp, fd);
			h.setHostedObject(stream);
			h.assignTo(hv.getMemberValue(fieldName));	
		}
	}
	
	private static class InitExecutor extends CtorNativeExecutor<JProcess> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JProcess proc, Argument[] args) throws Exception {
			/*
			private hosted void _init(
				string name, string args, Map envArgs, 
				string workingDir, 
				bool inheritedIO, 
				Stream stdin, Stream stdout, Stream stderr); // ProcessConfig
			*/
			
			String name = getString(args, 0);
			IArrayValue execArgs = (IArrayValue)getObject(args, 1);
			int len = execArgs.getLength();
			List<String> list = new ArrayList<String>(len);
			for(int i = 0; i <len; i++){
				StringValue sv = (StringValue)execArgs.getValueAt(i).deref();
				list.add(sv.getStringValue());
			}
			
			// Map envArgs
			ObjectValue val = getObject(args, 2);
			Map<String, String> map = null;
			if (val != null) {
				map = new HashMap<String, String>();
				HostedValue ov = (HostedValue)val;
				JMap jmap = (JMap) ov.getHostedObject();

				Pair<JValue, JValue>[] all = jmap.getAll();
				for(Pair<JValue, JValue> entry : all){
					StringValue k = StringValue.dereference(entry.getFirst(), true);
					StringValue v = StringValue.dereference(entry.getSecond(), true);
					if(k != null && v != null){
						map.put(k.getStringValue(), v.getStringValue());
					}
				}
			}
			
			// string workingDir
			String workingDir = getString(args, 3);
			
			// bool inheritedIO
			boolean inheritedIO = getBool(args, 4);
			
			// Stream stdin, Stream stdout, Stream stderr
			ObjectValue stdinObj = getObject(args, 5);
			ObjectValue stdoutObj = getObject(args, 6);
			ObjectValue stderrObj = getObject(args, 7);
			
			proc.init(name, list, map, workingDir, inheritedIO, stdinObj, stdoutObj, stderrObj);
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	private static class WaitExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		WaitExecutor() {
			super(PACON.Process.Name, PACON.Process.Op_wait);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			int ec = proc.wait(rt);
			return TempValueFactory.createTempIntValue(ec);
		}
		
	}
	
	private static class WaitForExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		WaitForExecutor() {
			super(PACON.Process.Name, PACON.Process.Op_wait);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			int duration = getInt(args, 0);
			boolean res = proc.waitFor(rt, duration);
			return TempValueFactory.createTempBoolValue(res);
		}
		
	}
	
	private static class StartExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		StartExecutor() {
			super(PACON.Process.Name, PACON.Process.Op_control);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			proc.start(rt);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class KillExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		KillExecutor() {
			super(PACON.Process.Name, PACON.Process.Op_control);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			proc.kill();
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class GetExitCodeExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		GetExitCodeExecutor() {
			super(PACON.Process.Name, PACON.Process.Op_control);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			int ec = proc.exitcode();
			return TempValueFactory.createTempIntValue(ec);
		}
		
	}
	
	private static class GetEnvArgExecutor extends IOInstanceNativeExecutor<JProcess> {
		
		GetEnvArgExecutor() {
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, JProcess proc, Argument[] args) throws Exception {
			String name = getString(args, 0);
			String res = proc.getEnvArg(rt, name);
			return res == null ? RefValue.makeNullRefValue(
				rt.getStackMemory().currentFrame(), JStringType.getInstance()) : 
				TempValueFactory.createTempStringValue(res);
		}
		
	}
	
	//----------- implementation at native end -----------//
	
	protected static final int UNDEFINED_EXITCODE = Integer.MIN_VALUE + 32767;
	private static final int MAX = 8192;
	
	private ProcessBuilder pb;
	private Process proc;
	
	private ObjectValue stdinObj;
	private ObjectValue stdoutObj;
	private ObjectValue stderrObj;
	
	private void init(
		String exec, List<String> args, Map<String, String> map, String workingDir, boolean inheritedIO, 
		ObjectValue stdinObj, ObjectValue stdoutObj, ObjectValue stderrObj){
		
		int aSize = args.size();
		String[] cmdline = new String[aSize + 1];
		cmdline[0] = exec;
		int i = 1;
		for(String a : args){
			cmdline[i] = a;
			i++;
		}
		
		pb = (new ProcessBuilder()).command(cmdline);
		if (workingDir != null){
			pb.directory(new File(workingDir));
		}
		
		// Redirect to the corresponding stream inherited from the parent process (JVM) only if 
		// 1) inheriting IO by default AND 2) the stream is not specified otherwise. 
		pb.redirectInput (inheritedIO && stdinObj  == null ? Redirect.INHERIT : Redirect.PIPE);	
		pb.redirectOutput(inheritedIO && stdoutObj == null ? Redirect.INHERIT : Redirect.PIPE);	
		pb.redirectError (inheritedIO && stderrObj == null ? Redirect.INHERIT : Redirect.PIPE);	
		
		// Redirect input, output and error
		this.stdinObj  = stdinObj;
		this.stdoutObj = stdoutObj;
		this.stderrObj = stderrObj;
	
		// Environment vars
		Map<String, String> env = pb.environment();
		if (map != null){
			for(Entry<String, String> entry : map.entrySet()){
				env.put(entry.getKey(), entry.getValue());
			}
		}
	}
	
	//----------- Protected methods to be overridden by special processes -----------//

	protected void start(ThreadRuntime rt){
		if(proc == null){
			synchronized(pb){
				if(proc == null){
					try {
						proc = pb.start();
						
						// Start redirection threads for each redirected stream. These threads will keep 
						// polling from the corresponding streams exposed by the subprocess and redirect 
						// to the Julian stream by calling appropriate API. They will terminate when the
						// subprocess exits.
						
						if (stdoutObj != null){
							InputStream input = proc.getInputStream();
							createOutputRedirectionThread(rt, input, stdoutObj);
						}
						
						if (stderrObj != null){
							InputStream input = proc.getErrorStream();
							createOutputRedirectionThread(rt, input, stderrObj);
						}

						if (stdinObj != null){
							OutputStream output = proc.getOutputStream();
							createInputRedirectionThread(rt, output, stdinObj);
						}
					} catch (IOException e) {
						throw new JSEIOException(e);
					}
				} else {
					throw new JIllegalStateException("The process has been started.");
				}
			}
		}
	}

	protected void kill(){
		if(proc == null){
			throw new JIllegalStateException("The process has not started.");
		}
		
		if(proc.isAlive()){
			proc.destroy();
		}
	}
	
	protected int wait(ThreadRuntime rt){
		if(proc == null){
			throw new JIllegalStateException("The process has not started.");
		}
		
		JThread thread = rt.getJThread();
		
		while(true){
			try {
				int i = proc.waitFor();
				return i;
			} catch (InterruptedException e) {
				if (thread.checkTermination()){
					throw new JThreadAbortedException(thread);
				}
			}
		}
	}
	
	protected boolean waitFor(ThreadRuntime rt, int duration){
		if(proc == null){
			throw new JIllegalStateException("The process has not started.");
		}
		
		JThread thread = rt.getJThread();
		
		while(true){
			try {
				return proc.waitFor(duration, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				if (thread.checkTermination()){
					throw new JThreadAbortedException(thread);
				}
			}
		}
	}
	
	protected int exitcode(){
		if(proc != null && proc.isAlive()){
			return proc.exitValue();
		} else {
			return UNDEFINED_EXITCODE;
		}
	}
	
	// Always return null for subprocess. This only works for current process.
	protected String getEnvArg(ThreadRuntime rt, String name) {
		return null;
	}

	//--------------- Stream redirection utilities ---------------//
	
	private JMethodType findMethod(ObjectValue streamObj, String name, JType[] ptypes){
		JMethodType jmtyp = JClassTypeUtil.findMethodWithStrictSignatureMapping(
			streamObj, 
			name, 
			ptypes,
			false);
		
		if (jmtyp == null){
			// This should be a bug of scripting engine. The ProcessContext is strongly typed.
			throw new IllegalArgumentsException(
				name, 
				"No method with name \"" + name + 
				"\" of required signature is on the stream object.");
		}
		
		return jmtyp;
	}
	
	// Read from native input stream and redirect by calling Julian output stream's API 
	private void createOutputRedirectionThread(ThreadRuntime rt, final InputStream input, final ObjectValue streamObj){
		final ITypeTable tt = rt.getTypeTable();
		final MemoryArea mem = rt.getStackMemory().currentFrame();
		final JMethodType writeMethod = findMethod(
			streamObj,
			WriteMethodName, 
			new JType[]{
				rt.getTypeTable().getArrayType(ByteType.getInstance()),
				IntType.getInstance(),
				IntType.getInstance()});
		final JMethodType flushMethod = findMethod(
			streamObj, 
			FlushMethodName, 
			new JType[0]);
		final FuncCallExecutor fc = createSystemExecutor(rt);
		
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				// Use buffer to store contents read from the output stream, then wrap the buffer in a specialized 
				// byte array value that can be accessed by Julian runtime. Call stream.write() with this array. 
				byte[] buffer = new byte[MAX];
				BasicArrayValue barray = PresetBasicArrayValueFactory.fromByteArray(mem, tt, buffer);
				int read = 0, total8ks = 0; long total = 0;
				try {
					// If the subprocess terminated, the pipe may still contain some output. So we
					// keep draining it until we see the end. There is no need to check liveness of
					// the subprocess.
					while (true){						
						// Read
						read = input.read(buffer, 0, MAX);
						
						// Redirect
						if (read > 0) {
							//System.out.write(buffer, 0, read);
							
							JValue[] values = new JValue[]{
								barray,
								TempValueFactory.createTempIntValue(0),
								TempValueFactory.createTempIntValue(read),
							};
							fc.invokeMethodInternal(writeMethod, WriteMethodName, values, streamObj);
							
							// Flush every 8K bytes
							total += read;
							int newTotal8ks = (int)(total >> 13); // right-shift by 13 is same to divide by 8192
							if (newTotal8ks > total8ks){
								total8ks = newTotal8ks;
								fc.invokeMethodInternal(flushMethod, FlushMethodName, new JValue[0], streamObj);
							}
						} else if (read < 0){
							break;
						}
					}
				} catch (IOException e) {
					// Ignore
				} finally {
					try{
						// Flush at the end (best efforts)
						fc.invokeMethodInternal(flushMethod, FlushMethodName, new JValue[0], streamObj);
					} catch (Exception e) {
						// Ignore
					}
				}
			}
			
		});
		
		t.setDaemon(false);
		t.start();
	}

	// Read from Julian input stream's API and redirect by calling native output stream
	private void createInputRedirectionThread(ThreadRuntime rt, final OutputStream output, final ObjectValue streamObj) {
		final ITypeTable tt = rt.getTypeTable();
		final MemoryArea mem = rt.getStackMemory().currentFrame();
		final JMethodType readMethod = findMethod(
			streamObj,
			ReadMethodName, 
			new JType[]{
				rt.getTypeTable().getArrayType(ByteType.getInstance()),
				IntType.getInstance(),
				IntType.getInstance()});
		final FuncCallExecutor fc = createSystemExecutor(rt);
		
		Thread t = new Thread(new Runnable(){
			@Override
			public void run() {
				// Use buffer to store contents read from the output stream, then wrap the buffer in a specialized 
				// byte array value that can be accessed by Julian runtime. Call stream.write() with this array. 
				byte[] buffer = new byte[MAX];
				BasicArrayValue barray = PresetBasicArrayValueFactory.fromByteArray(mem, tt, buffer);
				int read = 0;
				try {
					// If the subprocess terminated, the pipe may still contain some output. So we
					// keep draining it until we see the end. There is no need to check liveness of
					// the subprocess.
					while (true){						
						// Read up to MAX
						JValue[] values = new JValue[]{
							barray,
							TempValueFactory.createTempIntValue(0),
							TempValueFactory.createTempIntValue(MAX),
						};
						
						JValue val = fc.invokeMethodInternal(readMethod, ReadMethodName, values, streamObj);
						read = ((IntValue) val).getIntValue();
						
						// Redirect
						if (read > 0) {
							//System.out.write(buffer, 0, read);
							output.write(buffer, 0 , read);
							output.flush();
						} else if (read < 0){
							output.close(); // Signal the end of stream. The recipient process would hang on read() if we don't close the stream.
							break;
						}
					}
				} catch (IOException e) {
					// Ignore
				}
			}
			
		});
		
		t.setDaemon(false);
		t.start();
	}
	
	private static FuncCallExecutor createSystemExecutor(ThreadRuntime rt){
		Context cntx = Context.createSystemLoadingContext(rt);
		rt = new SystemInitiatedThreadRuntime(cntx);
		return new FuncCallExecutor(rt);
	}
	
	//--------------- Used by ProcessPipeStream ---------------//
	
	// When representing a spawned process, the output stream is actually the input stream to 
	// that process, and the input stream is either stdout or stdin from that process.
	
	OutputStream getOutputStream(boolean isError){
		return proc.getOutputStream();
	}
	
	InputStream getInputStream(boolean isError){
		return isError ? proc.getErrorStream() : proc.getInputStream();
	}
}

/**
 * A specialized class only for representing the current process, which is the JVM instance. Most operations
 * are effectively forbidden, with prominent exception of getting environment variables.
 * 
 * @author Ming Zhou
 */
class CurrentProcess extends JProcess {

	CurrentProcess(){
		
	}
	
	protected void start(ThreadRuntime rt){
		throw new JIllegalStateException("The process has been started.");
	}
	
	protected void kill(){
		throw new JIllegalStateException("The current process cannot be killed.");
	}
	
	protected int wait(ThreadRuntime rt){
		throw new JIllegalStateException("The current process cannot be waited on.");
	}
	
	protected boolean waitFor(ThreadRuntime rt, int duration){
		throw new JIllegalStateException("The current process cannot be waited on.");
	}
	
	protected int exitcode(){
		return UNDEFINED_EXITCODE;
	}
	
	protected String getEnvArg(ThreadRuntime rt, String name) {
		return System.getenv(name);
	}
	
	// When representing the current process, the output stream is either stdout or stdin,
	// and the input stream is stdin.
	
	@Override
	OutputStream getOutputStream(boolean isError){
		return isError ? System.err : System.out;
	}

	@Override
	InputStream getInputStream(boolean isError){
		return System.in;
	}
}