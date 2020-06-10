package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.buildSimpleEngine;
import static info.jultest.test.Commons.validateStringValue;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

import org.junit.Test;

import info.julang.execution.symboltable.VariableTable;
import info.jultest.test.Commons;
import info.jultest.test.EngineComponentSet;
import info.jultest.test.TestSession;
import junit.framework.Assert;

public class System_Network_ServerSocket_Sync_IOTestSuite {

	private static final String FEATURE = "Foundation/NetworkSync";
	
	// [P1]
    // 1. sync API
	//   write without buffer (large inputs)
	//   multiple reads
	//   mixed RW
	//   parallel for R and W
    // 2. file write async
    // [P2]
	// 1. stream close; reopen
    // 2. doc
    // [P3]
    // 1. Tests for error cases
    // ncat 127.0.0.1 49999
	
	//------------------ Async API ------------------//

	// - Make consecutive sync writes
    @Test
    public void syncWriteBaselineTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        StringBuilder sb = new StringBuilder();
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_sync_write_1.jul")){
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
    
    @Test
    public void syncReadBaselineTest1() throws Exception {
        EngineComponentSet ecs = buildSimpleEngine();
        String[] strings = new String[]{
        	"Hello ", "World. ", "This is Julian!"
        };
        StringBuilder sb = new StringBuilder();
        for(String str : strings) {
        	sb.append(str);
        }
        
        try (TestSession session = ecs.runInThread(Commons.Groups.OO, FEATURE, "server_socket_sync_read_1.jul")){
            int port = session.getIntUntil("port", 1, 65535);
            
            // connect
            Socket socket = new Socket("127.0.0.1", port);

            try (OutputStreamWriter sw = new OutputStreamWriter(socket.getOutputStream())){
                for (int i = 0; i < strings.length; i++){
                    char[] buffer = strings[i].toCharArray();
                    sw.write(buffer, 0, buffer.length);
                    sw.flush();
                }
            }
            
            socket.close();
        }
        
        VariableTable gvt = ecs.getVariableTable();
		validateStringValue(gvt, "accStr", sb.toString());
    }
}
