This project is separate from the main project. 

1. Compile/Install from cmdline
Run "mvn clean install" to compile and install the plugin to local repository.

2. Compile in Eclipse
(1) Add source link to two source folders from the main project: main/java and generated-sources.
(2) Install "m2e connector for build-helper-maven-plugin" to fix the error on pom.xml.

3. Compile the main project
(1) One can only compile the main project from cmdline after 1 has been run so that a plugin of name "juleng.maven.plugin" can be located.
(2) If there are any changes to JuFC, must compile and re-install the plugin before building the main project.

4. Test the plug-in
cd mvnplugin && mvn clean install && cd ..
mvn generate-resources -Djuleng.codegen.disable=true -Djuleng.docgen.enable=true -Djuleng.docgen.clean=false -Djuleng.docgen.format=html -Djuleng.docgen.pattern=*
              
To debug, use mvndebug and attach port 8000 from IDE