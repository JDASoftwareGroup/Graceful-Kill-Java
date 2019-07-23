# Graceful Kill Java

Graceful-kill-java allows you to gracefully kill java processes, which 'taskkill pid' and 'wmic ... call terminate' cannot do.

**TL;DR** Use the command `graceful-kill-java.bat [command line partial text]` to kill all java processes whose command line contains the partial text.

The means to gracefully kill a process is very important for JVM shutdown activities such as writing a final log file entry, a
code coverage profile or a java profiling snapshot.  And yet, on Windows there is no way to request a graceful kill of a
java process via the command line.  (Linux users can smugly just execute 'kill -9 PID' and get on with their day.)  Taskkill,
wmic, and other command line tools will impatiently (even immediately) kill, and even java's own java.lang.Process.destroy()
API makes no promises.  (Yes, it's unbelievable.  Feel free to go research and prove me wrong, as long as you return to tell me
how to do it.)

To address this shortcoming, a trick has been invented where a running JVM is asked to load a jar as a java agent, much like a java
profiler might do via dynamic request when you first attach.   Once loaded, the agent jar's first request to the JVM will then be "would
you mind gracefully killing yourself please?".   This does work consistently, and happily this politely psychotic agent jar
is already available via open source at https://github.com/everit-org/javaagent-shutdown (thank you!).   However, it's
incomplete as a solution.  Users still have to write additional java code to talk to the running JVM and request that the
agent jar be loaded, and this also assumes that identifying which JVM to kill is easily done.

So, this project, then, is that remaining java code to round out the solution.   It includes a mechanism to scan the list
of running JVMs on a Windows machine, select them by a partial phrase search of their commandline arguments, and ask each
one to gracefully die.   

## Getting Started
AdoptOpenJDK 8 can be installed from https://adoptopenjdk.net/.  Make sure JAVA_HOME are set and the bin directories for 
each are included in your PATH.  

Execute the following to run the unit test case and create the packaged jars:

`mvnw clean install`

Now you are ready to use graceful-kill-java.bat to gracefully kill the JVMs of your choice.


## Running the tests

The one unit test provided is executed when you do `mvn clean install` as seen above.  However it can also be run specifically via

`mvnw test`

This will launch a JVM which infinitely loops until shutdown.  On shutdown it will write one killed.txt file with 100 lines
each 10 characters long.   A second JVM will launch, find the test JVM, and request a graceful kill.   Finally,
the unit test will make sure that the entire killed.txt file was correctly written on JVM kill.  


## Deployment

This tool can be used in a live system by copying the following artifacts to a common folder on the target Windows system:
* graceful-kill-java.bat
* target\gracefulkilljava-1.0.jar
* target\org.everit.jdk.javaagent.shutdown-1.0.0.jar

E.g. you might create `x:\path\to\mygracefulkill` and put all these files in this folder (no target directory required).
Then make sure JAVA_HOME is set to the JDK location and the JDK's bin directory is included in your PATH.  Note that this 
must be JDK, not JRE, due to a dependency on %JAVA_HOME%\lib\tools.jar.

(FYI: tools.jar is included in OpenJDK and is not something that will run you afoul of any licensing concerns.)

### graceful-kill-java.bat
#### Usage:  
> graceful-kill-java.bat [command line partial text]

#### Example:  

Suppose we launched a program via:

>  java -Xms1G -Xmx2G -Dmyprop=superfun SampleSuperFunApplication

Then we could kill this JVM via the case-insensitive search: 

>   graceful-kill-java.bat samplesuperfun 


## Built With

* [OpenJDK](https://adoptopenjdk.net/) - JDK


## Authors

* **Andrew Laird** - *Initial work* - andrew.laird[at]jda.com

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
