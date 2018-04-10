## Notes to GitHub users

This is a full clone of the original repository hosted somewhere else. The development work will continue on the original repo and synchronization will occur periodically. Comments and suggestions are welcome, but pull requests are not accepted here. If you are interested in contributing to Julian, please send me a mail (zhoux738@umn.edu) and we can discuss any necessary changes to engineering process.  

## Overview

The implementation of Julian scripting language.

Julian is an interpreted, multi-paradigm, multi-threaded language. Its grammar resembles that of both C# and Java. In fact, as the main goal of design, most C# and Java code can be copy-pasted to Julian without significant syntax changes. This design aims at providing platform programmers with a means to quickly externalize business logic, without the need of even learning a new language. If we look at the major scripting languages available on JVM and CLR today, they have to be learned: JRE is shipped with JavaScript engine, which is nothing like Java but the appearance. Other popular languages, such as Scala, Groovy, Python, Ruby, etc., are all expected to be learned and mastered to achieve anything with necessary complexity.

At the down side to this design, certain compile-language features, such as visibility check, make less or no sense. To help programmers to transition, these features are intentionally preserved, but in general they are not encouraged to be used. On the other hand, once the programmers get used to the scripting environment, they would desire to write less boiler plate code as is common in the managed languages. Julian also plans to provide such convenience in the near future.

Download the latest release from [here](http://julang.info/). The binaries can also be updated from Julian console application. The language can evolve fast!

Main language features include

 * 5 primitive types and a special string type
 * commonly expected control structures and arithmetic operators
 * nested lexical scope
 * standard exception handling
 * first-class function and lambda
 * quick and unstructured scripting
 * module system and standard built-in modules
 * OO: class definition, inheritance, polymorphism
 * dynamic typing
 * meta-programming by code annotation
 * multi-threading and inter-thread synchronization

The standard library shipped within the binary release covers

 * Data containers
 * File system
 * OS process control
 * Asynchronous programming
 * Networking (not available yet)
 * Data processing (not available yet)

## Get Started

1. Requirements:
<br/> (1) JDK 1.7+; 
<br/> (2) Apache Maven 3.0+;
<br/> (3, Optional but strongly recommended) Apache Ant 1.9+; 
<br/> (4, Optional but strongly recommended) Eclipse with Maven integration
<br/> (5, Optional) Docker container 17+ (used only for JSE-223 compliance tests)
2. Clone this project to `<ROOT>`, then start a command line.
3. If Ant is installed, run 'ant bootstrap' from `<ROOT>`. Otherwise, run 'mvn install' from `<ROOT>/mvnplugin` to install *juleng*, a source generation Maven plugin that the main Maven project depends on. If new changes are made to this plugin itself, you must re-install. This can be done by calling 'ant build' if Ant is installed.
4. To build, simply run 'ant' from `<ROOT>` if Ant is installed. Otherwise, run `mvn clean verify` from `<ROOT>`. Note juleng will be invoked during generate-sources phase and may have updated the source code under `<ROOT>/src`. Do not forget to add the changes to the commit.
5. To develop using Eclipse, import the Maven project from `<ROOT>`. To see the test scripts in the Package Explorer, add source folder link pointing to `<ROOT>/test/julian`, then remove it from the build path but keep the link. If making changes to juleng, also import the Maven project from `<ROOT>/mvnplugin`. You need then add two source folders to make it compile - the Java source folder from main project and the generated source folder containing ANTLR parsers from main project's target directory at `<ROOT>/target`. Update the Maven project afterwards.
6. To run integration test, run 'ant test' from `<ROOT>` if Ant is installed. Otherwise, first build the project, then go to `<ROOT>/ci` and call `ant`, which will build Docker image and run a container with scripting engine deployed under standard JRE extension directory.

## Library Dependencies

This project uses [ANTLR](http://www.antlr.org/) in both compile-time and run-time, and [Apache Velocity](http://velocity.apache.org/) in compile-time. All dependencies are managed through [Apache Maven](https://maven.apache.org/).

