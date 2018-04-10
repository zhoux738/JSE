package info.jultest.apps;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

// Check environment vars.
// usage (Linux): 
//   export a=b
//   java -classpath "<ROOT>/jse/target/test-classes" info.jultest.apps.DumpEnv a=b
public class DumpEnv {

	public static void main(String[] args) {
		Map<String, String> map = new HashMap<String, String>();
		for(String arg : args){
			String[] pair = arg.split("=");
			map.put(pair[0], pair[1]);
		}
		
		Map<String, String> env = System.getenv();
		for(Entry<String, String> entry : map.entrySet()){
			boolean found = false;
			try {
				found = entry.getValue().equals(env.get(entry.getKey()));
			} catch(Exception e){
				// Ignore
			}
			
			if (!found){
				System.exit(100);
				return;
			}
		}

		System.exit(0);
	}

}
