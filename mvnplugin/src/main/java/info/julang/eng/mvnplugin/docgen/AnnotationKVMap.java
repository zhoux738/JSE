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

package info.julang.eng.mvnplugin.docgen;

import info.julang.util.Box;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.ArrayInitializerExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.utils.Pair;

/**
 * A key-value map with all the attributes parsed out of {@link JulianDoc} annotation directly from the source code.
 * 
 * @author Ming Zhou
 */
public class AnnotationKVMap {

	private static enum PropertyType {
		Boolean,
		String,
		StringArray,
		StringPairArray
	}
	
	static class Keys {
		final static String SUMMARY = "summary";
		final static String STATIC = "isStatic";
		final static String ALIAS = "alias";
		final static String NAME = "name";
		final static String RETURNS = "returns";
		final static String PARAMS = "params";
		final static String EXCEPTIONS = "exceptions";
		final static String REFERENCES = "references";
	}
	
	private static Map<String, PropertyType> ProTypes;
	
	static {
		ProTypes = new HashMap<String, PropertyType>();
		ProTypes.put(Keys.STATIC, PropertyType.Boolean);
		ProTypes.put(Keys.SUMMARY, PropertyType.String);
		ProTypes.put(Keys.ALIAS, PropertyType.String);
		ProTypes.put(Keys.NAME, PropertyType.String);
		ProTypes.put(Keys.RETURNS, PropertyType.String);
		ProTypes.put(Keys.PARAMS, PropertyType.StringArray);
		ProTypes.put(Keys.REFERENCES, PropertyType.StringArray);
		ProTypes.put(Keys.EXCEPTIONS, PropertyType.StringPairArray);
	}
	
	private Map<String, Object> kvmap;
	
	AnnotationKVMap(AnnotationExpr ae){
		kvmap = new HashMap<String, Object>();
		init(ae);
	}
	
	/**
	 * Call this with a constant from {@link AnnotationKVMap#Keys}. Returns false if not found.
	 */
	boolean is(String key){
		if (ProTypes.get(key) != PropertyType.Boolean) {
			throw new IllegalArgumentException(
				"The field in JulianDoc annotation with name " + key + " is not a " + PropertyType.Boolean);
		}
		
		Boolean b = (Boolean)kvmap.get(key);
		return b == null ? false : b.booleanValue();
	}
	
	/**
	 * Call this with a constant from {@link AnnotationKVMap#Keys}. Returns empty string if not found.
	 */
	String getString(String key){
		if (ProTypes.get(key) != PropertyType.String) {
			throw new IllegalArgumentException(
				"The field in JulianDoc annotation with name " + key + " is not a " + PropertyType.String);
		}
		
		String str = (String)kvmap.get(key);
		return str == null ? "" : str;
	}
	
	/**
	 * Call this with a constant from {@link AnnotationKVMap#Keys}. Returns empty array if not found.
	 */
	String[] getStringArray(String key){
		if (ProTypes.get(key) != PropertyType.StringArray) {
			throw new IllegalArgumentException(
				"The field in JulianDoc annotation with name " + key + " is not a " + PropertyType.StringArray);
		}
		
		String[] arr = (String[])kvmap.get(key);
		return arr == null ? new String[0] : arr;
	}
	
	/**
	 * Call this with a constant from {@link AnnotationKVMap#Keys}. Returns empty array if not found.
	 */
	@SuppressWarnings("unchecked")
	Pair<String, String>[] getStringPairArray(String key){
		if (ProTypes.get(key) != PropertyType.StringPairArray) {
			throw new IllegalArgumentException(
				"The field in JulianDoc annotation with name " + key + " is not a " + PropertyType.StringPairArray);
		}
		
		Pair<String, String>[] arr = (Pair<String, String>[])kvmap.get(key);
		return arr == null ? (Pair<String, String>[])Array.newInstance(Pair.class, 0) : arr;
	}

	private void init(AnnotationExpr anno) {
    	if (anno instanceof NormalAnnotationExpr){
    		NormalAnnotationExpr nae = (NormalAnnotationExpr)anno;
    		NodeList<MemberValuePair> pairs = nae.getPairs();
    		for (MemberValuePair pair : pairs) {
    			String key = pair.getName().toString();
    			Expression expr = pair.getValue();
    			
    			PropertyType ptype = ProTypes.get(key);
    			switch(ptype){
				case Boolean:
					Box<Boolean> bool = new Box<Boolean>(false);
            		expr.accept(new ExtractingBooleanLiteralVisitor(), bool);
            		kvmap.put(key, bool.get());
					break;
    			case String:
    				StringBuilder sb = new StringBuilder();
            		expr.accept(new AccumulatingStringLiteralVisitor(), sb);
            		String value = sb.toString();
            		kvmap.put(key, value);
            		break;
    			case StringArray:
    				List<String> slist = new ArrayList<String>();
            		expr.accept(new AccumulatingStringListVisitor(), slist);
					String[] sarray = new String[slist.size()];
            		slist.toArray(sarray);
            		kvmap.put(key, sarray);
            		break;
    			case StringPairArray:
    				List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
            		expr.accept(new AccumulatingStringPairListVisitor(), list);
            		@SuppressWarnings("unchecked")
					Pair<String, String>[] array = (Pair<String, String>[])Array.newInstance(Pair.class, list.size());
            		list.toArray(array);
            		kvmap.put(key, array);
            		break;
				default:
					break;
    			}
    		}
    	}
	}
	
	// true OR false
	private static class ExtractingBooleanLiteralVisitor extends VoidVisitorAdapter<Box<Boolean>> {
		
	    @Override
	    public void visit(BooleanLiteralExpr bin, Box<Boolean> box) {
	    	boolean val = bin.getValue();
	    	box.set(val);
	    }
	}
	
	// "x" + "y"
	private static class AccumulatingStringLiteralVisitor extends VoidVisitorAdapter<StringBuilder> {
		
	    @Override
	    public void visit(BinaryExpr bin, StringBuilder builder) {
	    	Operator op = bin.getOperator();
	    	if (op == Operator.PLUS) {
		        super.visit(bin, builder);
	    	}
	    }
	    
	    @Override
	    public void visit(StringLiteralExpr bin, StringBuilder builder) {
	    	String str = bin.asString();
	    	builder.append(str);
	    }
	}
	
	// {"x:"y, "x:" + "y", "x:y"}
	private static class AccumulatingStringPairListVisitor extends VoidVisitorAdapter<List<Pair<String, String>>> {
		
		private StringBuilder sb;
		
	    @Override
	    public void visit(ArrayInitializerExpr init, List<Pair<String, String>> list) {
	    	NodeList<Expression> nodes = init.getValues();
	    	for(Expression expr : nodes) {
	    		sb = new StringBuilder();
	    		expr.accept(this, list);
		    	String str = sb.toString();
		    	int ind = str.indexOf(":");
		    	String k = str.substring(0, ind);
		    	String v = str.substring(ind+1).trim();
		    	list.add(new Pair<String, String>(k, v));
		    	sb = null;
	    	}
	    }
	    
	    @Override
	    public void visit(BinaryExpr bin, List<Pair<String, String>> list) {
	    	Operator op = bin.getOperator();
	    	if (op == Operator.PLUS) {
		        super.visit(bin, list);
	    	}
	    }
	    
	    @Override
	    public void visit(StringLiteralExpr bin, List<Pair<String, String>> list) {
	    	String str = bin.asString();
	    	if (sb != null) {
		    	sb.append(str);
	    	}
	    }
	}
	
	// {"", "" + "", ""}
	private static class AccumulatingStringListVisitor extends VoidVisitorAdapter<List<String>> {
	
		private StringBuilder sb;
		
	    @Override
	    public void visit(ArrayInitializerExpr init, List<String> list) {
	    	NodeList<Expression> nodes = init.getValues();
	    	for(Expression expr : nodes) {
	    		sb = new StringBuilder();
	    		expr.accept(this, list);
	    		String str = sb.toString();
		    	list.add(str);
		    	sb = null;
	    	}
	    }
	    
	    @Override
	    public void visit(BinaryExpr bin, List<String> list) {
	    	Operator op = bin.getOperator();
	    	if (op == Operator.PLUS) {
		        super.visit(bin, list);
	    	}
	    }
	    
	    @Override
	    public void visit(StringLiteralExpr bin, List<String> list) {
	    	String str = bin.asString();
	    	if (sb != null) {
		    	sb.append(str);
	    	}
	    }
	}
}
