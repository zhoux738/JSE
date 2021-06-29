package info.jultest.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import info.julang.execution.StandardIO;
import info.julang.execution.simple.SimpleScriptEngine;

/**
 * Usage:
 * <pre><code>
 * TestIO io = new TestIO(engine);
 * engine.run(getScriptFile(Commons.Groups.OO, FEATURE, "test.jul")); // inside the script, print stuff enclosed by []
 * String[] entries = io.locateEntriesInOutput();
 * </code></pre>
 * @author Ming Zhou
 */
public class TestIO extends StandardIO {
	
	public TestIO(SimpleScriptEngine engine, String input) {
		super(input != null ? new ByteArrayInputStream(input.getBytes()) : null,
			new ByteArrayOutputStream(1024),
			new ByteArrayOutputStream(1024));

		engine.getRuntime().setStandardIO(this);
	}
	
	public TestIO(SimpleScriptEngine engine) {
		this(engine, null);
	}
	
	public String getOutputString() {
		getOut().flush();
		return new String(((ByteArrayOutputStream)out).toByteArray());
	}
	
	public String getErrorString() {
		getError().flush();
		return new String(((ByteArrayOutputStream)err).toByteArray());
	}

	/**
	 * Find all strings inside [] pair, in the order of appearance.
	 */
	public String[] locateEntriesInOutput() {
		return locateEntries(true, '[');
	}

	/**
	 * Find all strings enclosed by the specified char pair, in the order of appearance.
	 */
	public String[] locateEntries(boolean outOrErr, char leftQuote) {
		char rightQuote;
		switch(leftQuote) {
		case '[': rightQuote = ']'; break;
		default: throw new IllegalArgumentException("Left quote '" + leftQuote + "' is not recognized.");
		}
		
		List<String> strs = new ArrayList<>();
		String out = outOrErr? getOutputString() : getErrorString();
		int offset = 0, max = out.length() - 1;
		while (offset <= max) {
			int s = out.indexOf(leftQuote, offset);
			if (s < 0) {
				break;
			}
			int e = out.indexOf(rightQuote, s);
			if (e < 0) {
				break;
			}
			String entry = out.substring(s + 1, e - 1);
			strs.add(entry);
			offset = e;
		}
		
		return strs.toArray(new String[strs.size()]);
	}
}
