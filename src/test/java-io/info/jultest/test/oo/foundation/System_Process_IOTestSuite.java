package info.jultest.test.oo.foundation;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import static info.jultest.test.Commons.validateBoolValue;
import static info.jultest.test.Commons.validateIntValue;
import static info.jultest.test.Commons.validateStringValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.jultest.test.Commons;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.memory.value.StringValue;
import junit.framework.Assert;

// Tests in this class run "java -version" as this executable is guaranteed to exist in dev environment.
// Note that due to some historical reason, "java -version" outputs version info to standard error.
public class System_Process_IOTestSuite {

	private static final String FEATURE = "Foundation/Process";

	private Future<StringBuilder> sb;
	private ExecutorService executor;

	@Before
	public void setup() {
		executor = new ThreadPoolExecutor(1, // minimal number of threads
				1, // maximum number of threads
				1, // keep-alive time before excess idle thread (beyond minimal)
					// is recycled
				TimeUnit.MICROSECONDS, // unit of the parameter above
				new ArrayBlockingQueue<Runnable>(1));
		// a queue used to keep submitted tasks when all the threads are
		// occupied.

		sb = executor.submit(new Callable<StringBuilder>() {
			@Override
			public StringBuilder call() throws Exception {
				StringBuilder builder1 = new StringBuilder();

				ProcessBuilder pb = (new ProcessBuilder()).command("java", "-version");
				Process proc = null;
				try {
					proc = pb.start();
					proc.waitFor();
				} catch (IOException | InterruptedException e) {
					builder1.append(e.getMessage());
				}

				fillOutput(proc.getErrorStream(), builder1);

				return builder1;
			}
		});
	}

	@After
	public void teardown() {
		if (executor != null) {
			executor.shutdown();
		}
	}

	// Test setting a Stream to redirect error to.
	//
	// This test is very hard to implement perfectly. The process spawned from
	// Julian engine can terminate any time
	// without regards to the state of redirection thread. So it's possible that
	// the redirection thread, which is
	// an unmanaged daemon, is still fetching and outputting contents after we
	// exited the engine.
	//
	// To mitigate this we (1) wait 0.1 second in the script after everything is
	// done; (2) only check if the output
	// matches the start of expected contents; (3) rerun a couple of times if
	// the redirection thread didn't fetch
	// any contents at all.
	
	/*
	@Test
	public void output0Test() {
		JulianScriptEngine jse = new JulianScriptEngine(true);
		jse.addModulePath(Commons.SRC_REPO_ROOT);
		String path = makeScriptPath(Commons.Groups.OO, FEATURE, "process_x.jul");
		jse.runFile(path);
	}
	*/
	
	@Test
	public void outputTest() throws EngineInvocationError, IOException {
		int attempts = 3;
		while (attempts > 0) {
			VariableTable gvt = new VariableTable(null);
			gvt.enterScope();
			HeapArea heap = new SimpleHeapArea();
			TypeTable tt = new TypeTable(heap);
			SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
			engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);

			// 1) create a temp file
			File temp = File.createTempFile("__jse_test_", ".tmp");
			temp.deleteOnExit();

			try {
				// 2) create a global var "path" and set temp file's full path
				// to it
				tt.initialize(engine.getRuntime());
				String path = temp.getAbsolutePath();
				gvt.addVariable("path", new StringValue(heap, path));

				// 3) in process_2.jul, new up a file using "path", and use its
				// write-stream as the output stream of subprocess.
				engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "process_2.jul"));

				// 4) validate runtime variables
				validateBoolValue(gvt, "alive", false);
				validateIntValue(gvt, "res", 0);

				StringBuilder builder2 = new StringBuilder();
				FileInputStream fis = new FileInputStream(path);
				fillOutput(fis, builder2);

				StringBuilder builder1 = null;
				try {
					builder1 = sb.get();
				} catch (InterruptedException | ExecutionException e) {
					Assert.fail("Encountered an unexpected exception when running the executable directly: "
							+ e.getMessage());
				}

				if (builder1 == null) {
					Assert.fail("Could retrive result from running the executable directly.");
				}

				String expected = builder1.toString();
				String actual = builder2.toString();

				if ("".equals(actual)) {
					attempts--;
					continue;
				} else {
					Assert.assertTrue(expected.startsWith(actual));
					break;
				}

				// We can't do this:
				// Assert.assertEquals(builder1.toString(),
				// builder2.toString());
			} finally {
				// 5) delete the temp file
				temp.delete();
			}
		}

		if (attempts <= 0) {
			Assert.fail("Couldn't get any output from the Julian process after multiple retries.");
		}
	}
	
	@Test
	public void basicTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "process_1.jul"));

		validateBoolValue(gvt, "alive0", false);
		validateBoolValue(gvt, "alive1", true);
		validateBoolValue(gvt, "alive2", false);
		validateIntValue(gvt, "res", 0);
	}

	@Test
	public void readFromPipeTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "process_3.jul"));

		validateIntValue(gvt, "res", 0);
		validateIntValue(gvt, "total", demandResultFromDirectRun().toString().getBytes().length);
	}

	@Test
	public void inputTest() throws EngineInvocationError, IOException {
		runInputTest("process_4.jul");
	}

	@Test
	public void writeToPipeTest() throws EngineInvocationError, IOException {
		runInputTest("process_5.jul");
	}

	@Test
	public void pipingTest() throws EngineInvocationError, IOException {
		runInputTest("process_6.jul");
	}

	@Test
	public void currentTest() throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);
		SimpleScriptEngine engine = makeSimpleEngine(gvt);

		// set an env variable
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "process_current_1.jul"));

		validateStringValue(gvt, "n", "julian");
		validateBoolValue(gvt, "validated", true);
	}

	@Test
	public void envTest() throws EngineInvocationError, IOException {
		String root = new File("").getAbsolutePath();
		String cp = root + "/target/test-classes";

		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);

		// 2) pass along classpath
		tt.initialize(engine.getRuntime());
		gvt.addVariable("cp", new StringValue(heap, cp));

		// 3) in process_4.jul, new up a file using "path", and use its
		// read-stream as the output stream of subprocess.
		engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "process_env_1.jul"));

		// 4) validate the output file
		validateIntValue(gvt, "res", 0);
	}

	private void runInputTest(String script) throws EngineInvocationError, IOException {
		String root = new File("").getAbsolutePath();
		String cp = root + "/target/test-classes";

		VariableTable gvt = new VariableTable(null);
		gvt.enterScope();
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		SimpleScriptEngine engine = makeSimpleEngine(heap, gvt, tt, null);
		engine.getContext().addModulePath(Commons.SRC_REPO_ROOT);

		// 1) create a temp file and output some contents
		File temp = File.createTempFile("__jse_test_", ".tmp");
		temp.deleteOnExit();

		File temp2 = File.createTempFile("__jse_test_", ".tmp");
		temp2.deleteOnExit();

		StringBuilder builder1 = null;
		try {
			builder1 = sb.get();
		} catch (InterruptedException | ExecutionException e) {
			Assert.fail("Encountered an unexpected exception when running the executable directly: " + e.getMessage());
		}

		try (FileWriter fw = new FileWriter(temp)) {
			fw.write(builder1.toString());
			fw.flush();
		}

		try {
			// 2) create a global var "path" and set temp file's full path to it
			tt.initialize(engine.getRuntime());
			String path = temp.getAbsolutePath();
			String path2 = temp2.getAbsolutePath();
			gvt.addVariable("path", new StringValue(heap, path));
			gvt.addVariable("ofile", new StringValue(heap, path2));
			gvt.addVariable("cp", new StringValue(heap, cp));

			// 3) in process_4.jul, new up a file using "path", and use its
			// read-stream as the output stream of subprocess.
			engine.run(getScriptFile(Commons.Groups.OO, FEATURE, script));

			// 4) validate the output file
			StringBuilder builder2 = new StringBuilder();
			FileInputStream fis = new FileInputStream(path2);
			fillOutput(fis, builder2);

			String expected = builder1.toString();
			String actual = builder2.toString();
			Assert.assertEquals(expected, actual);
		} finally {
			// 5) delete the temp files
			temp.delete();
			temp2.delete();
		}
	}

	private StringBuilder demandResultFromDirectRun() {
		StringBuilder builder1 = null;
		try {
			builder1 = sb.get();
		} catch (InterruptedException | ExecutionException e) {
			Assert.fail("Encountered an unexpected exception when running the executable directly: " + e.getMessage());
		}

		if (builder1 == null) {
			Assert.fail("Couldn't retrive result from running the executable directly.");
		}

		return builder1;
	}

	private static void fillOutput(InputStream stream, StringBuilder builder) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.lineSeparator());
			}
		} catch (IOException e) {
			builder.append(e.getMessage());
		}
	}
}
