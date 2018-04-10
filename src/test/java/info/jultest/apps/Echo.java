package info.jultest.apps;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

// Read from stdin, and output to stdout or a file.
// usage (Windows): 
//   java -classpath "<ROOT>/jse/target/test-classes" info.jultest.apps.Echo -outputfile F:\somefile CON<E:\inputfile
public class Echo {

	public static void main(String[] args) {
		boolean disabled = false;
		boolean toReadFile = false;
		String filePath = null; 
		if(args.length > 0){
			for(String arg : args){
				if (toReadFile){
					filePath = arg;
					toReadFile = false;
				} else if ("-disable".equals(arg)){
					disabled = true;
					break;
				} else if ("-outputfile".equals(arg)){
					toReadFile = true;
				}
			}
		}
		
		if (disabled) {
			System.out.println("Reading from input is disabled");
		} else {
			try {
				try (
					BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
					Writer sw = filePath != null ? new FileWriter(filePath) : new OutputStreamWriter(System.out)){
					String line = null;
					while((line = in.readLine()) != null) { // Note readLine() doesn't return line separator.
						sw.write(line);
						sw.write(System.lineSeparator());
					}
					
					sw.flush();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
