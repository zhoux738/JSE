package info.jultest.test.security;

import static info.jultest.test.Commons.makeSimpleEngine;

import java.io.File;
import java.io.FilePermission;
import java.security.AccessControlException;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.AssertHelper;
import info.jultest.test.TestExceptionHandler;
import info.jultest.test.oo.ExceptionTestsBase;

// This test verifies that a Security Exception thrown by the Java's access controller will
// be translated to JSE's System.UnderprivilegeException.
public class JavaSecurityManagerTestSuite extends ExceptionTestsBase {

    private Random rnd;
    private static final String exheader = "Failed to pass the platform access control: ";
    
    @Before
    public void setup() {
        rnd = new Random();
    }
    
    private String name() {
        return "__nonexist__" + rnd.nextInt(Integer.MAX_VALUE);
    }
    
    @Test
    public void denyByPlatformTest() throws EngineInvocationError {
    	// Make sure we start clean
    	SecurityManager sm = System.getSecurityManager();
    	Assert.assertTrue(sm == null);
    	
    	final String name = name();
    	File f = null;
    	try {
    		// Install a JVM SecMgr that allows everything except 
    		// for IO operation against the specified file name.
        	System.setSecurityManager(sm = new SecurityManager() {
        		@Override
        		public void checkPermission(java.security.Permission perm) {
        			if (perm instanceof FilePermission) {
        				if (perm.toString().contains(name)) {
            				throw new AccessControlException(
                				exheader + perm.toString(), perm); 
        				}
        			}
        		}
        	});
        	
        	// Validate that our setup is working
        	Exception e1 = null;
        	f = new File(name);
        	try {
				f.createNewFile();
			} catch (Exception e) {
				e1 = e;
			}
        	Assert.assertTrue(e1 instanceof AccessControlException);
        	Assert.assertTrue(e1.getMessage().contains(exheader));
        	
        	// Run JSE script to create the new file.
            SimpleScriptEngine engine = makeSimpleEngine(new VariableTable(null));
            engine.getContext().addPolicy(true, "*", null); // allow everything
            TestExceptionHandler teh = installExceptionHandler(engine);
            
            engine.runSnippet(
                "import System.IO;" + "\r\n" +
                "File f = new File(\"" + name + "\"); " + "\r\n" +
                "f.create();"
            );
            
            /**/
            validateException(
                teh,
                "System.UnderprivilegeException",
                "Access is denied by the underlying plaform.",
                null,
                null,
                -1); // In fact, ctor File() checks IO permission so we will fail at line 2. 
                     // Since JSE doesn't guarantee where the underlying check happens, do not assert on the line number.
    		//*/
            
            String msg = teh.getCause().getExceptionMessage();
            // System.HostingPlatformException: 
            // An exception is thrown from a method implemented by the hosting language. 
            // Method: System.IO.File.File, 
            // Exception: 
            // Failed to pass the platform access control: 
            // ("java.io.FilePermission" "/Users/patrick/Development/source/jse/__nonexist__1114111340" "read")
            AssertHelper.validateStringOccurencesUnordered(msg, exheader, "java.io.FilePermission", name);
    	} finally {
    		System.setSecurityManager(null);
    		sm = System.getSecurityManager();
        	Assert.assertTrue(sm == null);
    		
    		if (f != null && f.exists()) {
    			System.out.println("File was created.");
    			f.delete();
    		}
    	}
    }
}
