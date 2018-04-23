package info.jultest.test.interpret.app;

import static info.jultest.test.Commons.getScriptFile;
import static info.jultest.test.Commons.makeSimpleEngine;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.IVariableTableTraverser;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.jultest.test.Commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

/*
 * In these tests, the expected result are encoded in the name of variables.
 * 
 * An anatomy of name "r0_p3_9":
 *   r0_  this is the part to differentiate between different variables with same value, since variables must have different names.
 *   p    the sign. p for positive, n for negative.
 *   3_9  the value. '_' should be replaced by '.' to indicate decimal part. it's illegal to have more than one '_'s.
 * Therefore for variable with name "r0_p3_9", the test will validate that it has value = +3.9
 * 
 * Special values that can be used in place of "p3_9" are:
 *   zero 0
 *   nan  Not a number (must be a string var in the script)
 *   pinf Positive infinity (must be a string var in the script)
 *   ninf Negative infinity (must be a string var in the script)
 */
public class MathComputeTests {
	
	private static final String FEATURE = "App/Compute";
	
	@Test
	public void mathAbsTest() throws EngineInvocationError {
		runTests("abs.jul", 7);
	}
	
	@Test
	public void mathFloorTest() throws EngineInvocationError {
		runTests("floor.jul", 9);
	}
	
	@Test
	public void mathCeilTest() throws EngineInvocationError {
		runTests("ceil.jul", 9);
	}
	
	@Test
	public void mathRoundTest() throws EngineInvocationError {
		runTests("round.jul", 9);
	}
	
	@Test
	public void mathMaxTest() throws EngineInvocationError {
		runTests("max.jul", 15);
	}
	
	@Test
	public void mathMinTest() throws EngineInvocationError {
		runTests("min.jul", 15);
	}
	
	@Test
	public void mathSignTest() throws EngineInvocationError {
		runTests("sign.jul", 8);
	}
	
	@Test
	public void mathLogTest() throws EngineInvocationError {
		runTests("log.jul", 5);
	}
	
	@Test
	public void mathTrigonoTest() throws EngineInvocationError {
		runTests("trigono.jul", 9);
	}
	
	@Test
	public void mathPowerTest() throws EngineInvocationError {
		runTests("power.jul", 9);
	}
	
	private void runTests(String scriptName, int varCount) throws EngineInvocationError {
		VariableTable gvt = new VariableTable(null);		
		SimpleScriptEngine engine = makeSimpleEngine(gvt);
		
		engine.run(getScriptFile(Commons.Groups.IMPERATIVE, FEATURE, scriptName));
		
		validateValueGroup(gvt, varCount);
	}
	
	private void validateValueGroup(VariableTable gvt, int total){
		// Find all the variables to validate
		final List<String> names = new ArrayList<String>();
		gvt.traverse(new IVariableTableTraverser(){

			@Override
			public boolean processScope(int level, Map<String, JValue> scope) {
				for(String n : scope.keySet()){
					char c = n.charAt(0);
					if (c == 'r' || c == 'p' || c == 'n'){
						names.add(n);
					} else if ("zero".equals(n)){
						names.add(n);
					}
				}
				
				return false;
			}
			
		}, true);
		
		Assert.assertEquals(total, names.size());
		
		for(String n : names) {
			String name = n;
			// Cut "rN_"
			if (n.startsWith("r")){
				char c1 = n.charAt(1);
				if ('0' <= c1 && c1 <= '9' && n.charAt(2) == '_') {
					n = n.substring(3);
				}
			}
			
			// Translate [n|p]NNN_NNN to a number
			double d = 0;
			if (n.startsWith("n")){
				if (n.equals("ninf")) {
					d = Double.NEGATIVE_INFINITY;
				} else {
					d = -convert(n.substring(1));
				}
			} else if (n.startsWith("p")){
				if (n.equals("pinf")) {
					d = Double.POSITIVE_INFINITY;
				} else {
					d = convert(n.substring(1));
				}
			} else if (n.equals("zero")) {
				d = 0.0;
			} else if (n.equals("nan")) {
				d = Double.NaN;
			} else {
				d = failOnNaming(n);
			}
			
			validate(gvt, name, d);
		}
	}
	
	private double convert(String name) {
		int index = name.indexOf('_');
		if (index == -1) {
			return Integer.parseInt(name);
		} else if (name.indexOf('_', index + 1) == -1){
			return Double.parseDouble(name.replace("_", "."));
		} else {
			return failOnNaming(name);
		}
	}
	
	private double failOnNaming(String name) {
		String error = "Variable " + name + " is not compliant with naming convention in Math tests.";
		Assert.fail(error);
		throw new RuntimeException(error);
	}
	
	private void validate(VariableTable vt, String vname, double base){
		JValue v = (JValue)vt.getValue(vname);
		Assert.assertNotNull(v);
		v = v.deref();
		switch(v.getKind()){
		case BYTE:
			byte b = ((ByteValue)v).getByteValue();
			Assert.assertEquals(vname, (byte)base, b);
			break;
		case FLOAT:
			float f = ((FloatValue)v).getFloatValue();
			double lower = base - 0.000001;
			Assert.assertTrue("Value lower then expected:" + vname, f >= lower);
			double higher = base + 0.000001;
			Assert.assertTrue("Value higher then expected: " + vname, f <= higher);
			break;
		case INTEGER:
			int i = ((IntValue)v).getIntValue();
			Assert.assertEquals(vname, (int)base, i);
			break;
		case OBJECT:
			String str = StringValue.dereference(v, true).getStringValue();
			boolean res = base == Double.NEGATIVE_INFINITY && str.equals(FloatValue.NEG_INF_STR);
			res |= base == Double.POSITIVE_INFINITY && str.equals(FloatValue.POS_INF_STR);
			res |= base == Double.NaN && str.equals(FloatValue.NAN_STR);
			Assert.assertTrue("The variable is a string, but it doesn't represent positive/negative infinity or NaN.", res);
			break;
		default:
			Assert.fail("Variable " + vname + " is not a number, but a(n) " + v.getKind() + ".");
		}
	}
}
