package info.jultest.test.security;

import static info.jultest.test.Commons.makeSimpleEngine;

import java.io.File;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import info.julang.execution.security.PACON;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.jultest.test.Commons;
import info.jultest.test.ExceptionTestsBase;
import info.jultest.test.TestExceptionHandler;

public class PlatformAccessTestSuite extends ExceptionTestsBase {

    private Random rnd;
    
    @Before
    public void setup() {
        rnd = new Random();
    }
    
    private String name() {
        return "__nonexist__" + rnd.nextInt(Integer.MAX_VALUE);
    }
    
    @Test
    public void denyAllTest() throws EngineInvocationError {
        SimpleScriptEngine engine = makeSimpleEngine(new VariableTable(null));
        engine.getContext().addPolicy(false, "*", null);
        TestExceptionHandler teh = installExceptionHandler(engine);
        
        engine.runSnippet(
            "import System.IO;" + "\r\n" +
            "File f = new File(\"" + name() + "\"); " + "\r\n" +
            "f.create();"
        );
        
        validateException(
            teh,
            "System.UnderprivilegeException",
            "Access is denied by policy System.IO (write).",
            null,
            null,
            3);
    }
    
    @Test
    public void denyMappingTest() throws EngineInvocationError {
        SimpleScriptEngine engine = makeSimpleEngine(new VariableTable(null));
        engine.getContext().addPolicy(false, "System.Interop", new String[] { "*" });
        TestExceptionHandler teh = installExceptionHandler(engine);
        
        engine.runSnippet(
            "[Mapped(className=\"a.b.C\")]" + "\r\n" +
            "class MyClass { }"             + "\r\n" +
            "MyClass mc = new MyClass();"
        );
        
        assertCause(
            teh,
            "System.ClassLoadingException",
            "System.UnderprivilegeException");
    }
    
    @Test
    public void allowSpecificTest1() throws EngineInvocationError {
    	VariableTable gvt = new VariableTable(null);
        SimpleScriptEngine engine = makeSimpleEngine(gvt);
        boolean allowAll = false;
        engine.getContext().addPolicy(allowAll, "*", null); // deny all
        engine.getContext().addPolicy(true, PACON.Environment.Name, null); // allow System.Environment
        runAndValidate(gvt, engine, allowAll);
    }
    
    @Test
    public void allowSpecificTest2() throws EngineInvocationError {
    	VariableTable gvt = new VariableTable(null);
        SimpleScriptEngine engine = makeSimpleEngine(gvt);
        boolean allowAll = true;
        engine.getContext().addPolicy(allowAll, "*", null); // allow all
        engine.getContext().addPolicy(false, PACON.IO.Name, null); // deny IO operations
        runAndValidate(gvt, engine, allowAll);
    }
    
    @Test
    public void allowSpecificTest3() throws EngineInvocationError {
    	VariableTable gvt = new VariableTable(null);
        SimpleScriptEngine engine = makeSimpleEngine(gvt);
        boolean allowAll = false;
        engine.getContext().addPolicy(allowAll, "*", null); // deny all
        engine.getContext().addPolicy(true, PACON.IO.Name, null); // allow IO operations
        engine.getContext().addPolicy(false, PACON.IO.Name, new String[] { PACON.IO.Op_write }); // except "write"
        runAndValidate(gvt, engine, allowAll);
    }

	private void runAndValidate(
		VariableTable gvt, SimpleScriptEngine engine, boolean allowAll) throws EngineInvocationError {
		String n = null;

        try {
            engine.runSnippet(
                "import System.IO;"                             + "\r\n" +
                "bool caught = false;"                          + "\r\n" +
                "try {"                                         + "\r\n" +
                "  File f = new File(\"" + (n = name()) + "\");" + "\r\n" +
                "  f.create();"                                 + "\r\n" +
                "} catch (UnderprivilegeException ex) {"        + "\r\n" +
                "  caught = true;"                              + "\r\n" +
                "}"                                             + "\r\n" +
                (allowAll ? ( // Additional test if the default is ALLOW
                "System.Reflection.Script s ="                  + "\r\n" +
                "  Environment.getScript();"                    + "\r\n" +
                "bool succ = s != null;"                        + "\r\n"
                ) : "")
            );

            Commons.validateBoolValue(gvt, "caught", true);
            if (allowAll) { // Additional validation if the default is ALLOW
                Commons.validateBoolValue(gvt, "succ", true);
            }
        } finally {
        	if (n != null) {
            	File f = new File(n);	
            	if (f.exists()) {
            		f.delete();
            	}
        	}
        }
	}
}
