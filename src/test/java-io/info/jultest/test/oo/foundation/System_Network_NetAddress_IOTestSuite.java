package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateNonEmptyStringValue;
import static info.jultest.test.Commons.validateNullValue;
import static info.jultest.test.Commons.validateStringValue;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.modulesystem.ModuleManager;
import info.jultest.test.Commons;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Permission;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class System_Network_NetAddress_IOTestSuite {

	private static final String FEATURE = "Foundation/Network";
	
	// To configure JVM's DNS caching period, must set a security manager first.
	@Before
	public void setUp(){
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission perm) {
                
            }

            @Override
            public void checkPermission(Permission perm, Object context) {
                
            }
        });
	    
	    java.security.Security.setProperty("networkaddress.cache.ttl", "0");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "0");
	}
	
    @After
    public void tearDown(){
        java.security.Security.setProperty("networkaddress.cache.ttl", "-1");
        java.security.Security.setProperty("networkaddress.cache.negative.ttl", "-1");
        System.setSecurityManager(null);
    }
	
	// 1) from address
	// 2) resolve a real DNS name
	// 3) resolve a fake DNS name
	@Test
	public void baselineTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		ModuleManager manager = new ModuleManager();
		SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
		
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
		
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "address_1.jul"));
		
	    validateStringValue(gvt, "a1", "10.20.30.40");
        validateNonEmptyStringValue(gvt, "a2");
        validateNullValue(gvt, "a3");
	}
	
    @Test
    public void getLocalTest() throws EngineInvocationError, UnknownHostException {
        VariableTable gvt = new VariableTable(null);
        ModuleManager manager = new ModuleManager();
        SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
        
        engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
        
        engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "address_2.jul"));
        
        String local = InetAddress.getLocalHost().getHostAddress();
        
        validateStringValue(gvt, "a1", local);
    }
    
    @Test
    public void checkLoopBackTest() throws EngineInvocationError, UnknownHostException {
        VariableTable gvt = new VariableTable(null);
        ModuleManager manager = new ModuleManager();
        SimpleScriptEngine engine = makeSimpleEngine(gvt, manager, false);
        
        engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);
        
        engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "address_3.jul"));
        
        validateBoolValue(gvt, "b0", true);
        validateBoolValue(gvt, "b1", true);
    }
}
