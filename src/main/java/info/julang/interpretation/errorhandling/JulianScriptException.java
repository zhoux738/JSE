/*
MIT License

Copyright (c) 2017 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.interpretation.errorhandling;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.JSERuntimeException;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * Julian script exception, or JSE, is the wrapper class on hosting platform and 
 * interop bridge for <font color="green">System.Exception</font> in Julian language.
 * <p/>
 * JSE is different from {@link JSERuntimeException}, which is used throughout the
 * codebase to represent every kind of script exceptions that must be handled later
 * on. JSE is used to carry the script exception up the stack utilizing Java's 
 * exception handling mechanism. 
 * <p/>
 * When a script exception arises in the script, it will be caught and wrapped in
 * a JSE at appropriate timing. The JSE will be re-thrown, and upon the exit of each
 * frame the stack trace will be updated. Finally, the script engine will see the 
 * JSE surfaced up, and handle that in accordance with the settings (outputs stacktrace
 * to console, or returns the exception to engine users for in-proc info exchange).
 * <p/>
 * Since callsite information is not fully available at the time the exception is 
 * created, there are two stages that an exception will go through before it can leave  
 * a stack trace record. First, the enclosing expression/statement in which the 
 * exception is thrown can log its source file name and line number. Second, when the
 * exception is popped up to the function caller, the function name will become
 * available, therefore a new stack trace entry can be created.
 * <p/>
 * When we add a new trace entry, we unset the line number info. This is because we 
 * want to make sure that the line number is set only once between it travels through
 * two frames. We thus use {@link JulianScriptException#UNSET_LINENO} to determine
 * if the line has been set.
 * 
 * @author Ming Zhou
 */
public class JulianScriptException extends RuntimeException {

	private static final long serialVersionUID = 8548608028182393374L;

	public static final int UNSET_LINENO = -1;
	
	private final static String ex_field_stacktrace = "_stacktrace";
	
	private final static String ex_field_stackdepth = "_stackdepth";
	
	private final static String ex_field_message = "_message";
	
	private final static String ex_field_cause = "_cause";
	
	private final static String ex_field_filename = "_filename";
	
	private final static String ex_field_lineno = "_lineno";
	
	private final static String ex_field_rawformat = "_rawformat";
	
	private final static int stack_capacity = 10;
	
	private final static int stack_growthRate = 2;
	
	private ObjectValue exception;
	
	private MemoryArea memory;
	
	/**
	 * Create a new JulianScriptException wrapping a Julian exception type.
	 * 
	 * @param except A Julian script runtime value whose type derives from or is of System.Exception.
	 */
	public JulianScriptException(ITypeTable tt, ObjectValue except) {
		this(except.getType().getName(),
			except.getMemoryArea(), tt, except);
	}
	
	/**
	 * Create a new JulianScriptException wrapping a Julian exception type.
	 * 
	 * @param type The full name of Julian Exception type.
	 * @param memory The memory area from which the exception object is allocated.
	 */
	protected JulianScriptException(
		JClassType type, MemoryArea memory, ITypeTable tt) {
		this(type.getName(),
			memory, 
			tt,
			new ObjectValue(memory, type, false));
	}
	
	/**
	 * Create a new JulianScriptException wrapping a Julian exception type.
	 * 
	 * @param type The full name of Julian Exception type.
	 * @param rt thread runtime
	 * @param context interpretation context
	 * @param stream a stream with contents in form of "<code>new ObjectType(Expr0, Expr1, ..., ExprN);</code>"
	 */
	JulianScriptException(
		JClassType type, ThreadRuntime rt, Context context, List<ArgumentContext> alist, AstInfo<? extends ParserRuleContext> ainfo){
		this(type.getName(),
			context.getHeap(), 
			context.getTypTable(),
			NewObjExecutor.createObject(rt, context, alist, type, ainfo));
	}
	
	private JulianScriptException(String typeName, MemoryArea memory, ITypeTable tt, ObjectValue exception) {
		super("Script exception. Type: " + typeName);
		
		this.memory = memory;
		this.exception = exception;
		
		replaceJStackTrace(tt, exception);
	}

	/**
	 * Add a frame record, with source info.
	 * <p/>
	 * An example of formatted string: calculate(int, int)  (/path/to/file.jul, 117)
	 * 
	 * @param funcName
	 * @param parameters
	 * @param fileName	source file's name
	 * @param lineNo	line number in source file
	 */
	public void addStackTrace(ITypeTable tt, String funcName, String[] parameters, String fileName, int lineNo){
		StringBuilder sb = new StringBuilder();
		sb.append(funcName);
		
		if(parameters != null){
			sb.append("(");
			for(int i=0;i<parameters.length;){
				sb.append(parameters[i]);
				i++;
				if(i<parameters.length){
					sb.append(",");
				}
			}
			sb.append(")");
		}
		
		if(fileName != null){
			sb.append("  (");
			sb.append(fileName);
			
			if(lineNo != -1){
				sb.append(", ");
				sb.append(lineNo);
			}		
			
			sb.append(")");
		}
		
		addStackTraceInternal(tt, exception, sb.toString());
	}
	
	/**
	 * WARNING: this overwrites the actual stack trace depth which is tied with the trace array.
	 * 
	 * @param depth
	 */
	public void setStackTraceDepth(int depth) {
		IntValue sdepthVal = (IntValue) exception.getMemberValue(ex_field_stackdepth);
		IntValue newDepth = TempValueFactory.createTempIntValue(depth);
		newDepth.assignTo(sdepthVal);
	}
	
	/**
	 * Add a raw stack message to the trace.
	 * 
	 * @param tt
	 * @param stackMessage
	 */
	public void addRawStackTrace(ITypeTable tt, String stackMessage){
		addStackTraceInternal(tt, exception, stackMessage);
	}
	
	/**
	 * Get the type of JSE exception, which can be retrieved by {@link #getExceptionValue()}. It's
	 * either of, or derived from, type <code><font color="green">System.Exception</font></code>.
	 */
	public JClassType getExceptionType(){
		return (JClassType) exception.getType();
	}
	
	/**
	 * Get the JSE exception instance. This is the script exception that this class is carrying 
	 * across the platform stack. Most methods exposed by this class is merely retrieving fields
	 * from this instance.
	 */
	public ObjectValue getExceptionValue(){
		return exception;
	}
	
	public String getExceptionFullName(){
		return exception.getType().getName();
	}

	public int getLineNumber(){
		return getLineNumberInternal(exception);
	}
	
	public void setLineNumber(int num){
		setLineNumberInternal(exception, num);
	}
	
	public String getFileName(){
		return getFileNameInternal(exception);
	}
	
	public void setFileName(String fileName){
		setFileNameInternal(exception, fileName);
	}
	
	/**
	 * WARNING: this can overwrite the cause passed through constructor by users.
	 * 
	 * @param inner
	 */
	public void setJSECause(JulianScriptException inner) {
		RefValue rval = (RefValue) exception.getMemberValue(ex_field_cause);
		inner.exception.assignTo(rval);
	}
	
	/**
	 * Mark this exception as raw formatted. The stack trace would be rendered as is.
	 * 
	 * @param isRaw
	 */
	public void setRawFormat(boolean isRaw) {
		BoolValue bv = TempValueFactory.createTempBoolValue(isRaw);
		JValue jv = exception.getMemberValue(ex_field_rawformat);
		bv.assignTo(jv);
	}
	
	public String getExceptionMessage(){
		return getExceptionMessageInternal(exception);
	}
	
	public String[] getStackTraceAsArray(){
		return getStackTraceAsArrayInternal(exception);
	}
	
	/**
	 * Get the inner Julian exception that caused this one.
	 * 
	 * @return an ObjectValue of <code><font color="green">System.Exception</font></code> type. 
	 * null of no internal cause.
	 */
	public ObjectValue getJSECause(){
		ObjectValue ov = RefValue.tryDereference(getCauseRef(exception));
		return ov == RefValue.NULL ? null : ov;
	}
	
	/**
	 * Get a standard exception output that contains exception name, 
	 * error message, stack trace info.
	 * 
	 * @param indent the indentation for each line.
	 * @param endByLineBreak true to add an EOL at the end of message.
	 * @return
	 */
	public String getStandardExceptionOutput(int indent, boolean endByLineBreak){
		StringBuilder sb = new StringBuilder();
		generateStandardExceptionOutputInternal(sb, exception, indent, endByLineBreak, 0);
		return sb.toString();
	}
	
	//--------------------------- Non-public members ---------------------------//
	
	/**
	 * Test only. Add a frame record. No source info.
	 * 
	 * @param funcName
	 */
	protected void addStackTrace(ITypeTable tt, String funcName, String[] parameters){
		addStackTrace(tt, funcName, parameters, null, -1);
	}
	
	// If the cause chain is longer than 4, do not show the causes beyond the 4th
	private void generateStandardExceptionOutputInternal(
		StringBuilder sb, ObjectValue exception, int indent, boolean endByLineBreak, int recursiveCount){
		
		String typName = exception.getType().getName();
		
		// Line 1: error type and message
		if(indent>0){
			addIndent(sb, indent);
		}
		sb.append(typName);
		sb.append(": ");
		String msg = getExceptionMessageInternal(exception);
		sb.append(msg);
		sb.append(System.lineSeparator());
		
		// Line 2 ~ N: stack trace
		String[] stacktrace = getStackTraceAsArrayInternal(exception);
		for(int i = 0; i < stacktrace.length; i++){
			if(indent>0){
				addIndent(sb, indent);
			}
			sb.append("  at ");
			sb.append(stacktrace[i]);
			sb.append(System.lineSeparator());
		}
		
		// Line N + 1 - error source
		boolean formatted = !isRawFormat(exception);
		if (formatted) {
			String fileName = getFileNameInternal(exception);
			int lineNo = getLineNumberInternal(exception);
			if(indent>0){
				addIndent(sb, indent);
			}
			sb.append("  from  (");
			sb.append(fileName);
			sb.append(", ");
			sb.append(lineNo);	
			sb.append(")");
		}
		
		// Cause, if existing
		RefValue rv = getCauseRef(exception);
		if(!rv.isNull()){
			if (recursiveCount < 4) {
				if (formatted) {
					sb.append(System.lineSeparator());
				}
				if(indent>0){
					addIndent(sb, indent);
				}
				sb.append("Caused by:");
				sb.append(System.lineSeparator());
				generateStandardExceptionOutputInternal(
					sb, rv.getReferredValue(), indent, false, recursiveCount + 1);
			} else {
				sb.append("More causes ...");
				sb.append(System.lineSeparator());
			}
		}
		
		if(endByLineBreak){
			sb.append(System.lineSeparator());		
		}
	}
	
	private void addIndent(StringBuilder sb, int indent){
		while(indent > 0){
			sb.append(' ');
			indent--;
		}
	}
	
	private void addStackTraceInternal(ITypeTable tt, ObjectValue exception, String string) {
		ArrayValue aval = getJStackTrace(exception);
		
		IntValue sdepthVal = (IntValue) exception.getMemberValue(ex_field_stackdepth);
		int depth = sdepthVal.getIntValue();
		int length = aval.getLength();
		if(depth >= length){
			// The current depth is already equal to the array length, we need to scale up.
			replaceJStackTrace(tt, exception);
			
			// Re-get array and length since the stack is stretched.
			aval = getJStackTrace(exception);
			length = aval.getLength();
		}
		depth++;
		
		// Add new string to stack
		RefValue sv = (RefValue) aval.getValueAt(depth - 1);
		StringValue stringSv = TempValueFactory.createTempStringValue(string);
		stringSv.assignTo(sv);
		
		IntValue newDepth = TempValueFactory.createTempIntValue(depth);
		newDepth.assignTo(sdepthVal);
		
		// Unset line number.
		setLineNumber(UNSET_LINENO);
	}
	
	private void replaceJStackTrace(ITypeTable tt, ObjectValue exception){
		RefValue rval = getJStackTraceRef(exception);
		int size = 0;
		ArrayValue old = null;
		
		if(rval.getReferredValue() == RefValue.NULL){
			// First time, set stack capacity to initial value
			size = stack_capacity;
		} else {
			// We have a stack already in use, retrieve it for later use
			old = getJStackTrace(exception);
			// and set stack capacity to a new value
			size = old.getLength() * stack_growthRate;
		}
		
		// Create an array value with string as element type
		ArrayValue aval = ArrayValueFactory.createArrayValue(memory, tt, JStringType.getInstance(), size);
		
		// Migrate old data into new array
		if(old != null){
			int depth = getJStackDepth(exception);
			for(int i=0; i<depth; i++){
				RefValue sv = (RefValue) old.getValueAt(i);
				sv.assignTo(aval.getValueAt(i));
			}
		}
		
		// Set array value to the ref-type field in Exception instance 
		aval.assignTo(rval);
	}
	
	/**
	 * Get the ref value for stack trace. The ref value contains an array value.
	 * 
	 * @return
	 */
	private RefValue getJStackTraceRef(ObjectValue exception){
		RefValue rval = (RefValue) exception.getMemberValue(ex_field_stacktrace);
		return rval;
	}
	
	// Get the ref value for cause. The ref value contains a System.Exception value.
	private RefValue getCauseRef(ObjectValue exception){
		RefValue rval = (RefValue) exception.getMemberValue(ex_field_cause);
		return rval;
	}
	
	private String getExceptionMessageInternal(ObjectValue exception){
		StringValue msg = StringValue.dereference(exception.getMemberValue(ex_field_message));
		return msg != null ? msg.getStringValue() : "";
	}
	
	private String[] getStackTraceAsArrayInternal(ObjectValue exception){
		ArrayValue aval = getJStackTrace(exception);
		
		int depth = getJStackDepth(exception);
		String[] result = new String[depth];
		
		for(int i=0;i<depth;i++){
			StringValue sv = StringValue.dereference(aval.getValueAt(i));
			result[i] = sv.getStringValue();
		}
		
		return result;
	}
	
	// Get the array value for stack trace.
	private ArrayValue getJStackTrace(ObjectValue exception){
		RefValue rval = getJStackTraceRef(exception);
		ObjectValue oval = rval.dereference();
		ArrayValue aval = (ArrayValue) oval;
		return aval;
	}
	
	// Get current depth of stack trace.
	private int getJStackDepth(ObjectValue exception){
		IntValue sdepthVal = (IntValue) exception.getMemberValue(ex_field_stackdepth);
		return sdepthVal.getIntValue();
	}
	
	private String getFileNameInternal(ObjectValue exception){
		StringValue msg = StringValue.dereference(exception.getMemberValue(ex_field_filename));
		return msg != null ? msg.getStringValue() : null;
	}
	
	private void setFileNameInternal(ObjectValue exception, String fileName){
		StringValue sv = TempValueFactory.createTempStringValue(fileName != null ? fileName : "<unknown>");
		JValue jv = exception.getMemberValue(ex_field_filename);
		sv.assignTo(jv);
	}
	
	private int getLineNumberInternal(ObjectValue exception){
		IntValue ln = (IntValue)exception.getMemberValue(ex_field_lineno);
		return ln.getIntValue();
	}
	
	private void setLineNumberInternal(ObjectValue exception, int lineNo){
		IntValue iv = TempValueFactory.createTempIntValue(lineNo);
		JValue jv = exception.getMemberValue(ex_field_lineno);
		iv.assignTo(jv);
	}
	
	private boolean isRawFormat(ObjectValue exception) {
		BoolValue bv = (BoolValue) exception.getMemberValue(ex_field_rawformat);
		return bv.getBoolValue();
	}
}
