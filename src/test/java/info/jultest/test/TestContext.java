package info.jultest.test;

import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to facilitate communication between JVM and Julian engine during tests. Both side can 
 * set values into and retrieve from this context, which is compartmented by a group name.
 * <p>
 * To use this in Julian test script, add the following at the beginning of file:
 * <pre><code>import ModuleSys.TestHelper;
 * </code></pre>
 * 
 * @author Ming Zhou
 */
public class TestContext {

	private static Map<String, Map<String, Object>> map;
	
	//--------------- Called by scripts ---------------//
	
	public static void set(String group, String key, String value){
        //System.out.println(group + ": " + key + " = " + value);
		Map<String, Object> map = getMap(group);
		map.put(key, value);
	}
	
	public static void set(String group, String key, int value){
        //System.out.println(group + ": " + key + " = " + value);
		Map<String, Object> map = getMap(group);
		map.put(key, value);
	}
	
    public static void set(String group, String key, boolean value){
        //System.out.println(group + ": " + key + " = " + value);
        Map<String, Object> map = getMap(group);
        map.put(key, value);
    }
	
    //--------------- Called by test cases ---------------//
    
	public static String getString(String group, String key){
		Map<String, Object> map = getMap(group);
		return (String)map.get(key);
	}
	
	public static int getInt(String group, String key){
		Map<String, Object> map = getMap(group);
		Object obj = map.get(key);
		if (obj != null) {
			return (Integer)obj;
		} else {
			return 0;
		}
	}
	
    public static Boolean getBool(String group, String key) {
        Map<String, Object> map = getMap(group);
        Object obj = map.get(key);
        if (obj != null) {
            return (Boolean)obj;
        } else {
            return null;
        }
    }

    //--------------- Private members ---------------//
    
	private synchronized static Map<String, Object> getMap(String group){
		if (map == null) {
		    //System.out.println("Created map");
			map = new HashMap<String, Map<String, Object>>();
		}
		
		Map<String, Object> m = map.get(group);
		if (m == null) {
			m = new HashMap<String, Object>(); 
			map.put(group, m);
            //System.out.println("Created " + group);
		}
		
		return m;
	}
	
}
