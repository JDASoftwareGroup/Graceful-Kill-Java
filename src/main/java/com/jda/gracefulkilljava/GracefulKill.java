 /**
 *
 * See https://github.com/everit-org/javaagent-shutdown/blob/master/README.md
 * A Java Agent that shuts down after installed. The agent should be installed dynamically with Sun Attach API. Here is an example, how:

 VirtualMachine vm = VirtualMachine.attach(pid);
 vm.loadAgent(pathToShutdownAgentJar, args);
 Three arguments are supported:

 exitcode: The exit code that the agent should use to stop the JVM. The code is used when everything went well; all shutdown hooks ran and there are only daemon threads left. The default value is 0.
 timeout: The agent waits until the timeout, before shuts down the JVM forcibly by calling Runtime.halt(haltcode). The value is specified in milliseconds. If the value is less than or equal to 0, the agent will wait forever for a clean shutdown. The default value is 0.
 haltcode: The code that the agent uses during stopping the JVM forcibly after the timeout. Default value is 1.
 Arguments should be separated by comma. E.g.:

 vm.loadAgent(pathToShutdownAgentJar, "timeout=1000,haltcode=5");
 */
package com.jda.gracefulkilljava;
import java.io.*;
import java.util.HashSet;

import com.sun.tools.attach.*;

 /**
  * A utility to find JVMs based on a partial text commandline search and gracefully kill them.   See the project README.md for more details.
  */
 public class GracefulKill {

    public static void main(String[] args) throws IOException {
        if (args.length < 3){
            System.err.println("Usage:  GracefulKill <path to org.everit.jdk.javaagent.shutdown-1.0.0.jar> <commandline substring> <timeout>");
            System.exit(1);
        }

        String pathToShutdownAgentJar = args[0];
        String commandSubstring = args[1];
        long timeout = Long.parseLong(args[2]);//insures valid input

        //First, find this JVM so that we don't accidentally kill it
        String output = getProcessResults("wmic process where \"commandline like '%GracefulKill%'\" get processid");
        //The set of process IDs for all JVMs with GracefulKill in the commandline.  Presumably that would usually be just one JVM.
        HashSet<String> doNotKill = getPidForGracefulKill(output);

        //The PIDs for the JVMs that the user is searching for.
        output = getProcessResults("wmic process where \"caption like 'java.exe' and commandline like '%"+commandSubstring+"%'\" get processid");
        //int skip=2;
        int skip=1;
        String[] splitResults = output.split("[' ']+");
        boolean killedOne = false;
        for (String pid : splitResults) {
            if (skip>0){
                skip--;
                continue;
            }
            //If we found ourselves, keep going
            if (doNotKill.contains(pid))
                continue;

            killedOne = true;
            try {
                System.out.println("Killing java.exe with pid of: "+pid);
                //Next two lines are as per https://github.com/everit-org/javaagent-shutdown/blob/master/README.md
                VirtualMachine vm = VirtualMachine.attach(pid);
                vm.loadAgent(pathToShutdownAgentJar, "timeout="+timeout);
            }catch(Throwable ex){
                System.err.println("Caught exception for pid "+pid+" "+ex.getClass().getSimpleName()+":"+ex.toString());
            }
        }
        if (!killedOne)
            System.out.println("No java.exe found to kill.");

    }

     /**
      * Parse the output of the WMIC process search for PIDs.
      * @param output Text output of a previous WMIC process search.
      * @return Set of PIDs
      */
    private static HashSet<String> getPidForGracefulKill(String output) {
        HashSet<String> result = new HashSet<>();
        boolean header = true;
        for (String pid : output.split("[' ']+")) {
            if (header){
                header = false;
                continue;
            }
            result.add(pid);
        }
        return result;
    }

     /**
      * Launch a process and return the standard out.
      * @param command Process with arguments to run.
      * @return String with stdout contents from the process.
      * @throws IOException Any issues with executing the command.
      */
    private static String getProcessResults(String command) throws IOException {
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(command);

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        StringBuilder stdout = new StringBuilder();

        // read the output from the command
        String s;
        while ((s = stdInput.readLine()) != null) {
            stdout.append(s);
        }
        return stdout.toString();
    }
}
