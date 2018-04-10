// Dependencies
File = Java.type("java.io.File");
BufferedReader = Java.type("java.io.BufferedReader");
BufferedWriter = Java.type("java.io.BufferedWriter");
FileOutputStream = Java.type("java.io.FileOutputStream");
FileReader = Java.type("java.io.FileReader");
OutputStreamWriter = Java.type("java.io.OutputStreamWriter");

out = java.lang.System.out;
pom = new File("pom.xml");
// out.println("pom.xml = " + pom.getAbsolutePath());

var reader = null;
var writer = null;
var input = pom.getAbsolutePath();
var tmpPom = "pom-" + java.lang.System.currentTimeMillis() + ".xml.tmp";
var output = new File(input).getParentFile().getAbsolutePath() + "/" + tmpPom;
// out.println("input = " + input);
// out.println("output = " + output);

try {            
    reader = new BufferedReader(new FileReader(input));
    writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output)));

    var oline;
    var shouldPrint = true;
    while ((oline = reader.readLine()) != null) {
        var line = oline.trim();
		var toCheck = line.startsWith("<!--") && line.endsWith("-->") && line.length() > 7;
        if (toCheck){
            line = line.toLowerCase();
            if (shouldPrint && line.contains("juleng:") && line.contains("start")){
                shouldPrint = false;
            }
        }
        
        if (shouldPrint) {
            writer.write(oline);
            writer.write(java.lang.System.lineSeparator());
        }

        if (toCheck && !shouldPrint && line.contains("juleng:") && line.contains("end")){
            shouldPrint = true;
        } 
    }
    
    writer.flush();

    out.println("Created temp pom file: " + output);
    JSE.setProperty("btpom", tmpPom);
} finally {
    try {
        if (reader != null) {
            reader.close();
        }
        if (writer != null) {
            writer.close();
        }
    } catch (e) {
        // Ignore
    }
}