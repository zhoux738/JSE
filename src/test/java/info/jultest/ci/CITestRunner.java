package info.jultest.ci;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import info.julang.clapp.CLEnvironment;
import info.julang.clapp.CLExecutor;
import info.julang.clapp.ExecutionArguments;
import info.julang.clapp.IExecutionHelper;
import info.julang.clapp.NamedBinding;
import info.julang.external.exceptions.JSEException;

/**
 * A standalone application that runs Julian tests using standard Java Scripting Engine API.
 * <p/>
 * This application uses the same cmdline execution utilities by JSE interpreter.
 * 
 * @author Ming Zhou
 */
public class CITestRunner {

	private static class Helper implements IExecutionHelper {

		@Override
		public Object getEngineInstance(CLEnvironment env) {
			ScriptEngineManager scm = new ScriptEngineManager();
			ScriptEngine se = scm.getEngineByName("julian"); // We intentionally do not use constant.
			return se;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void addModulePaths(Object engine, ExecutionArguments args, String[] paths) {
			@SuppressWarnings("rawtypes")
			List list = new ArrayList(); 
			for(String path : paths){
				list.add(path);
			}
			
			ScriptContext context = new SimpleScriptContext();
			context.setAttribute(
				"JSE_MODULE_PATHS", // = JulianScriptingEngine.MODULE_PATHS (We intentionally do not use constant)
				list, 
				ScriptContext.ENGINE_SCOPE);
		
			args.setContext(context);
		}

		@Override
		public Object invokeEngine(Object engine, ExecutionArguments args) throws JSEException {
			FileReader reader = null;
			try {
				reader = new FileReader(args.getScriptPath()); // CI Tests always use script file
			} catch (FileNotFoundException e) {
				throw new JSEException("Script file not found", e);
			}
			
			ScriptEngine se = (ScriptEngine)engine;
			try {
				return se.eval(reader, args.getContext());
			} catch (ScriptException e) {
				throw new JSEException("Script file faulted", e);
			}
		}

		@Override
		public void addBindings(Object engine, NamedBinding[] namedBindings) {
			// CI Tests do not support binding yet
		}
		
	}
	
	public static void main(String[] args) {
		Helper helper = new Helper();
		CLExecutor exec = new CLExecutor();
		exec.execute(helper, args);
	}
}
