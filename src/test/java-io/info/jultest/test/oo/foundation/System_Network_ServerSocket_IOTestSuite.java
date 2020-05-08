package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.buildSimpleEngine;
import static info.jultest.test.Commons.getStringValue;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringArrayValue;
import static info.jultest.test.Commons.validateStringValue;
import static info.jultest.test.Commons.validateIntValueWithinRange;
import info.julang.execution.symboltable.VariableTable;
import info.julang.util.OSTool;
import info.julang.util.OSTool.OSType;
import info.jultest.test.Commons;
import info.jultest.test.EngineComponentSet;
import info.jultest.test.TestSession;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import junit.framework.Assert;

import org.junit.Test;

import edu.emory.mathcs.backport.java.util.Arrays;

public class System_Network_ServerSocket_IOTestSuite {

	private static final String FEATURE = "Foundation/Network";
	
	//------------------ Async API ------------------//

	// - Make consecutive async writes, linked through promise chain
    @Test
    public void asyncWriteBaselineTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        StringBuilder sb = new StringBuilder();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_write_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            char[] buffer = new char[30];
            try (InputStreamReader sr = new InputStreamReader(socket.getInputStream())){
                while(true) {
                    int read = sr.read(buffer, 0, 30);
                    if(read > 0) {
                        sb.append(new String(buffer, 0, read));
                    } else if (read == -1) {
                        break;
                    }
                }
            }
            
            socket.close();
        }
        
        Assert.assertEquals("Hello World! This is Julian.", sb.toString());
    }
    
    // - Make multiple async write, without waiting for each call's result except for the last one
    // - All the data is sent out in the exact call order
    @Test
    public void asyncWriteBaselineTest2() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        StringBuilder sb = new StringBuilder();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_write_2.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            char[] buffer = new char[30];
            try (InputStreamReader sr = new InputStreamReader(socket.getInputStream())){
                while(true) {
                    int read = sr.read(buffer, 0, 30);
                    if(read > 0) {
                        sb.append(new String(buffer, 0, read));
                    } else if (read == -1) {
                        break;
                    }
                }
            }
            
            socket.close();
        }
        
        // Since we are not enforcing the order in the script, only check if all the parts in received.
        String[] strs = new String[] { "Hello ", "World! ", "This is ","Julian, ", "a scripting language."};
        String result = sb.toString();
        for(String str : strs) {
        	int ind = result.indexOf(str);
        	Assert.assertTrue("Missing " + str, ind >= 0);
        	result = result.substring(0, ind) + result.substring(ind + str.length());
        }
        
        Assert.assertEquals("", result);
    }
    
	// - Accept multiple connections
	// - All sockets receive right data, despite running in parallel and multiplexing on a single pooling thread
	// - Shutdown of each socket do no interfere with others
    @Test
    public void acceptAndRunInParallelTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        Result[] results = new Result[3];
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_parallel_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            results[0] = launchSocket(session, 0, port, new String[]{"abc", "xyz"});
            results[1] = launchSocket(session, 1, port, new String[]{"def", "xyz"});
            results[2] = launchSocket(session, 2, port, new String[]{"xyz", "abc"});
        }
        
        VariableTable gvt = ecs.getVariableTable();
        
        String[] strs = new String[results.length];
        int i = 0;
        for(Result r : results){
            strs[i] = r.get();
            i++;
        }
        Arrays.sort(strs);
        
        validateStringArrayValue(gvt, "strs", strs);
    }
    
    private Result launchSocket(
        TestSession session, int seq, int port, final String[] input) 
        throws UnknownHostException, IOException {  
        
        // connect
        final Socket socket = new Socket("127.0.0.1", port);
        socket.setTcpNoDelay(true);
        final Result result = new Result(socket.getLocalPort());
        
        Thread th = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    try (OutputStreamWriter sw = new OutputStreamWriter(socket.getOutputStream())){
                        for(String in : input) {
                            result.add(in);
                            sw.write(in); 
                            sw.flush();
                            try {
                                Thread.sleep(100);
                            } catch (Exception e) {
                                // NO-OP
                            }
                        }
                        
                        sw.write("_fin"); 
                        sw.flush();
                        
                        socket.close();
                    }
                } catch (UnknownHostException e) {
                } catch (SocketException e) {
                } catch (IOException e) {
                }
            }
        });
        
        th.start();
        return result;
    }
	
    private class Result {
        private String string = "";
        private int count;
        private int port;
        public Result(int port) {
            this.port = port;
        }
        public void add(String in) {
            string += in;
            count += in.length();
        }
        public String get(){
            String str = port + ": (" + count + ") " + string;
            return str;
        }
    }
    
    // Bind local, accept then close
    @Test
    public void baselineTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        validateStringValue(gvt, "s0", "BOUND");
        validateStringValue(gvt, "s1", "CONNECTED");
        validateStringValue(gvt, "s2", "BOUND");
        validateStringValue(gvt, "s3", "BOUND");
    }
    
    // Initialize with settings, bind, then retrieve settings
    @Test
    public void baselineTest2() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_2.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        String s1 = getStringValue(gvt, "s1");
        Assert.assertTrue(s1.contains("127.0.0.1"));
        validateIntValueWithinRange(gvt, "p1", "(0, 65535]");
        validateBoolValue(gvt, "b1", false);
        validateBoolValue(gvt, "b2", true);
        validateBoolValue(gvt, "b3", true);
    }
    
	// - Read multiple times until a protocol-based signal. 
	// - Close the connection inside the callback to cause promise settlement.
	// - Make sure the callback thread is a managed IO thread.
    @Test
    public void asyncBaselineTest1a() throws Exception {
    	asyncBaselineTest1(128, "server_socket_async_1a.jul");
    }
    
    // - Same to asyncBaselineTest1a, only with a small buffer. This is a critical change
    //   with regards to how the internal async reader works.
    @Test
    public void asyncBaselineTest1b() throws Exception {
    	asyncBaselineTest1(4, "server_socket_async_1b.jul");
    }
    
    // Although the script is same, we cannot share the same file due to the way TestSession works.
    private void asyncBaselineTest1(int bsize, String fn) throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        ecs.addIntVar("_bsize", bsize);
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, fn)){
            int port = session.getIntUntil("port", 1, 65535);
            // System.out.println(port);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            socket.setTcpNoDelay(true);
            
            try (OutputStreamWriter sw = new OutputStreamWriter(socket.getOutputStream())){
                sw.write("abcde"); sw.flush();
                Thread.sleep(500);
                sw.write("12345");
                sw.write("abcde");
                sw.write("12345");
                sw.write("yxxxx"); // Chars from here may or may not arrive since the server side abort after 20 chars
                sw.write("-----");
            }
            
            socket.close();
        }
        
        VariableTable gvt = ecs.getVariableTable();
        String accStr = getStringValue(gvt, "accStr");
        Assert.assertTrue(accStr.startsWith("abcde12345abcde12345"));
        validateStringValue(gvt, "state", "BOUND");
        String tname = getStringValue(gvt, "threadName");
        Assert.assertTrue(tname.contains("Worker"));
    }
    
    // - Handle TCP close from the client side properly: no errors; promise settlement
    // - Pass -1 to the last call
    @Test
    public void asyncBaselineTest2() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_2.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            Socket socket = new Socket("127.0.0.1", port);
            
            socket.close();
        }
        
        VariableTable gvt = ecs.getVariableTable();
        validateIntValue(gvt, "cnt", -1);
        validateIntValue(gvt, "total", 0);
        validateBoolValue(gvt, "flag", true);
    }
    
    // - Call getStream() multiple times
    // - read() will not shut down the stream
    @Test
    public void asyncBaselineTest3() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_3.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            socket.setTcpNoDelay(true);
            
            try (OutputStreamWriter sw = new OutputStreamWriter(socket.getOutputStream())){
                sw.write("abc"); 
                sw.flush();
                
                session.assertBool("flag", true);
                
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    // NO-OP
                }
                
                sw.write("def"); 
                sw.flush();
                
                socket.close();
            }
        }
        
        VariableTable gvt = ecs.getVariableTable();
        validateStringValue(gvt, "res1", "abc");
        validateStringValue(gvt, "res2", "def");
        validateStringValue(gvt, "stat1", "CONNECTED");
        validateStringValue(gvt, "stat2", "CONNECTED");
        validateStringValue(gvt, "stat3", "BOUND");
    }
    
    // - Handle TCP error properly
    // - Socket will be closed due to IO error
    @Test
    public void asyncBaselineTest4() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_async_4.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            Socket socket = new Socket("127.0.0.1", port);
            
            // This is one way to simulate a TCP error with BSD socket API. By setting linger time to 0 we are
            // opting out of normal TCP termination procedure which starts with a FIN from the initiator. Instead
            // this will immediately send out an RST to the server side, resulting in an error should the application
            // layer leave it unhandled.
            // (TODO: this doesn't seem working on Ubuntu, where the server can close without any error)
            socket.setSoLinger(true, 0);
            socket.close();
        }
        
        VariableTable gvt = ecs.getVariableTable();
        if (OSTool.is(OSType.WINDOWS, OSType.MAC)){
            validateBoolValue(gvt, "flag", true);
        }
        validateStringValue(gvt, "stat2", "BOUND");
    }
    
    // - Full-duplex async operation
    @Test
    public void asyncDuplexTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        StringBuilder sb = new StringBuilder();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_duplex_async_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            String[] msgs = new String[]{
                "Hello", "World", "This", "Is", "Julian"
            };
            char[] cbuf = new char[30];
            try (InputStreamReader sr = new InputStreamReader(socket.getInputStream()); 
                 OutputStreamWriter sw = new OutputStreamWriter(socket.getOutputStream())){
                for(String msg : msgs) {
                    sw.write(msg);
                    sw.flush();
                    int read = sr.read(cbuf, 0, 30);
                    String str = new String(cbuf, 0, read);
                    sb.append(str);
                }
            }
            
            socket.close();
        }
        
        Assert.assertEquals("[Hello][World][This][Is][Julian]", sb.toString());
    }
    
    // Tests for termination
    
    // - closing stream also closes socket
    @Test
    public void disconnectTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "socket_disconn_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        validateStringValue(gvt, "s1", "CONNECTED");
        validateStringValue(gvt, "s2", "BOUND");
        validateStringValue(gvt, "s3", "BOUND");
    }
    
    // - disconnecting socket also closes streams, regardless when those stream are obtained (before/after socket closure)
    @Test
    public void disconnectTest2() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "socket_disconn_2.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        validateStringValue(gvt, "s1", "CONNECTED");
        validateStringValue(gvt, "s2", "BOUND");
        validateStringValue(gvt, "s3", "BOUND");
        validateStringValue(gvt, "s4", "BOUND");
    }
    
    // - double closing has no effect for either socket or stream
    @Test
    public void disconnectTest3() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "socket_disconn_3.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        validateStringValue(gvt, "s1", "CONNECTED");
        validateStringValue(gvt, "s2", "BOUND");
        validateStringValue(gvt, "s3", "BOUND");
    }
    
    // - I/O op after closing
    @Test
    public void disconnectTest4() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "socket_disconn_4.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);
            
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        VariableTable gvt = ecs.getVariableTable();
        validateIntValue(gvt, "rd", -1);
        validateIntValue(gvt, "rd2", -1);
        validateBoolValue(gvt, "caught", true);
    }
}
