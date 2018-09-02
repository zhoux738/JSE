package info.jultest.test;

import static info.jultest.test.Commons.getScriptFile;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.simple.SimpleScriptEngine;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.StringValue;

public class EngineComponentSet {

	private SimpleScriptEngine engine;
	private TypeTable tt;
	private VariableTable gvt;
	private MemoryArea mem;
	
	public EngineComponentSet(
		SimpleScriptEngine engine, MemoryArea mem, TypeTable tt, VariableTable gvt) {
		this.engine = engine;
		this.mem = mem;
		this.tt = tt;
		this.gvt = gvt;
	}

	public SimpleScriptEngine getEngine() {
		return engine;
	}

	public TypeTable getTypeTable() {
		return tt;
	}

	public VariableTable getVariableTable() {
		return gvt;
	}
	
	public EngineComponentSet addStringVar(String name, String value){
	    gvt.addVariable(name, new StringValue(mem, value));
	    return this;
	}
	
	public EngineComponentSet addIntVar(String name, int value){
	    gvt.addVariable(name, new IntValue(mem, value));
	    return this;
	}

	public void run(String group, String feature, String scriptFile) throws EngineInvocationError {
		this.engine.run(getScriptFile(group, feature, scriptFile));
	}
	
	public TestSession runInThread(String group, String feature, String scriptFile){
		final FileScriptProvider prov = getScriptFile(group, feature, scriptFile);
		this.gvt.enterScope();
		String key = prov.getFilePathName(true);
		EngineComponentSet.this.addStringVar("_tskey", key);
		
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					EngineComponentSet.this.engine.run(prov);
				} catch (EngineInvocationError e) {
					e.printStackTrace();
				}
			}
			
		});
		t.start();
		
		return new TestSession(t, key);
	}

}
