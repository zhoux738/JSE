package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.IntValue;
import info.julang.modulesystem.ModuleManager;
import info.julang.util.Box;
import info.jultest.test.Commons;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Assert;

import org.junit.Test;

public class System_Network_Socket_IOTestSuite {

	private static final String FEATURE = "Foundation/Network";
	
	// Bind local then close
	@Test
	public void baselineTest1() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "socket_1.jul"));
		
		validateStringValue(gvt, "st0", "BOUND");
		validateStringValue(gvt, "st1", "BOUND");
	}
	
	// Connect to a server socket, send some messages and disconnect normally.
	@Test
	public void baselineTest2() throws Exception {
		final ServerSocket ss = new ServerSocket(0);
		final int port = ss.getLocalPort();
		final StringBuilder sb = new StringBuilder();
		final Box<Exception> box = new Box<Exception>(null);
		
		Thread t = new Thread(new SocketComm(ss, box){
			protected void useSocket(Socket s) throws IOException {
				int len = 30;
	            char[] cbuf = new char[len];
	            try (InputStreamReader sr = new InputStreamReader(s.getInputStream()); 
	                // OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream())
	                ){
	            	int read = 0;	            	
	            	while ((read = sr.read(cbuf, 0, len)) != -1) {
	                    String str = new String(cbuf, 0, read);
	                    sb.append(str);
	            	}
	            }
			}
		});
		t.start();
		
		invokeScript("socket_2.jul", port, box, t);
		
		Assert.assertEquals("Hello World. This is Julian![DONE]", sb.toString());
	}

	// Connect to a server socket, receive some messages and disconnect normally.
	@Test
	public void baselineTest3() throws Exception {
		final ServerSocket ss = new ServerSocket(0);
		final int port = ss.getLocalPort();
		final Box<Exception> box = new Box<Exception>(null);
		
		Thread t = new Thread(new SocketComm(ss, box){
			protected void useSocket(Socket s) throws IOException {
	            String[] strings = new String[]{
	            	"Hello ", "World. ", "This is Julian!"
	            };
	            try (//InputStreamReader sr = new InputStreamReader(s.getInputStream()); 
	                 OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream())
	                ){            	
	            	for (String str : strings) {
	            		char[] cbuf = str.toCharArray();
	            		sw.write(cbuf, 0, cbuf.length);
	            	}
	            }
			}
		});
		t.start();
		
		VariableTable gvt = invokeScript("socket_3.jul", port, box, t);
		validateStringValue(gvt, "accStr", "Hello World. This is Julian![DONE]");
	}
	
	// Connect to a server socket, send/receive some messages and disconnect normally.
	// (Starts receiving first)
	@Test
	public void duplexTest1() throws Exception {
		final ServerSocket ss = new ServerSocket(0);
		final int port = ss.getLocalPort();
		final Box<Exception> box = new Box<Exception>(null);
		final StringBuilder sb = new StringBuilder();
		
		Thread t = new Thread(new SocketComm(ss, box){
			protected void useSocket(Socket s) throws IOException {
				int len = 30;
	            char[] cbuf = new char[len];
	            try (InputStreamReader sr = new InputStreamReader(s.getInputStream()); 
	                 OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream())
	                ){
	            	int read = 0;
	            	while ((read = sr.read(cbuf, 0, len)) != -1) {
	            		sw.write(cbuf, 0, read);
	            		sb.append(new String(cbuf, 0, read));
	            		if (sb.toString().contains("Julian!")){
	            			break;
	            		}
	            	}
	            }
			}
		});
		t.start();
		
		VariableTable gvt = invokeScript("socket_duplex_1.jul", port, box, t);
		validateStringValue(gvt, "accStr", "HelloWorldThisisJulian!");
	}
	
	// Connect to a server socket, send/receive some messages and disconnect normally.
	// (Sends first)
	@Test
	public void duplexTest2() throws Exception {
		final ServerSocket ss = new ServerSocket(0);
		final int port = ss.getLocalPort();
		final Box<Exception> box = new Box<Exception>(null);
		final StringBuilder sb = new StringBuilder();
		
		Thread t = new Thread(new SocketComm(ss, box){
			protected void useSocket(Socket s) throws IOException {
				int len = 30;
	            char[] cbuf = new char[len];
	            try (InputStreamReader sr = new InputStreamReader(s.getInputStream()); 
	                 OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream())
	                ){
	            	int read = 0;
	            	while ((read = sr.read(cbuf, 0, len)) != -1) {
	            		sw.write(cbuf, 0, read);
	            		sb.append(new String(cbuf, 0, read));
	            		if (sb.toString().contains("Julian!")){
	            			break;
	            		}
	            	}
	            }
			}
		});
		t.start();
		
		VariableTable gvt = invokeScript("socket_duplex_2.jul", port, box, t);
		validateStringValue(gvt, "accStr", "HelloWorldThisisJulian!");
	}
	
	// Negotiate termination at application layer. To ensure the delivery of each message, always flush().
	@Test
	public void duplexTest3() throws Exception {
		final ServerSocket ss = new ServerSocket(0);
		final int port = ss.getLocalPort();
		final Box<Exception> box = new Box<Exception>(null);
		final StringBuilder sb = new StringBuilder();
		
		Thread t = new Thread(new SocketComm(ss, box){
			protected void useSocket(Socket s) throws IOException {
				int len = 30;
	            char[] cbuf = new char[len];
	            try (InputStreamReader sr = new InputStreamReader(s.getInputStream()); 
	                 OutputStreamWriter sw = new OutputStreamWriter(s.getOutputStream())
	                ){
	            	int read = 0;
	            	while ((read = sr.read(cbuf, 0, len)) != -1) {
	            		String str = new String(cbuf, 0, read);
	            		sb.append(str);
	            		if (sb.lastIndexOf("[DONE]") >= 0) {
	            			break;
	            		}
	            		sw.write(cbuf, 0, read);
	            		sw.flush();
	            	}
	            }
			}
		});
		t.start();
		
		VariableTable gvt = invokeScript("socket_duplex_3.jul", port, box, t);
		validateStringValue(gvt, "accStr", "HelloWorldThisisJulian!");
	}

	private abstract class SocketComm implements Runnable {
		private ServerSocket ss;
		private Box<Exception> box;
		private SocketComm(ServerSocket ss, Box<Exception> exBox){
			this.ss = ss;
			this.box = exBox;
		}
		
		@Override
		public void run() {
			Socket s = null;
			try {
				s = ss.accept();
				useSocket(s);
			} catch (IOException e) {
				box.set(e);
			} finally {
				if (s != null) {
					try {
						s.close();
					} catch (IOException e) {
						box.set(e);
					}
				}
				
				try {
					ss.close();
				} catch (IOException e) {
					box.set(e);
				}
			}
		}

		protected abstract void useSocket(Socket s) throws IOException;
	}
	
	private VariableTable invokeScript(
		final String scriptName, 
		final int port,
		final Box<Exception> exceptionBox, 
		Thread serverThread) throws Exception {
		final VariableTable gvt = new VariableTable(null);
		Thread t2 = new Thread(new Runnable(){
			@Override
			public void run() {
				gvt.enterScope();
				HeapArea heap = new SimpleHeapArea();
				TypeTable tt = new TypeTable(heap);
				SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
				engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
				
				tt.initialize(engine.getRuntime());
				gvt.addVariable("port", new IntValue(heap, port));
				
				try {
					engine.run(getScriptFile(Commons.Groups.OO, FEATURE, scriptName));
				} catch (EngineInvocationError e) {
					exceptionBox.set(e);
				}			
			}
		});
		t2.start();
		
		boolean done = false;
		while(!done){
			try {
				t2.join();
				serverThread.join();
				done = true;
			} catch (InterruptedException e) {
			}
		}
		
		Exception ex = exceptionBox.get();
		if (ex != null) {
			throw ex;
		}
		
		return gvt;
	}
}
