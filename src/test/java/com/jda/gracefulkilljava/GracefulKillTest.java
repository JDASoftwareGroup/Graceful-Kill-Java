package com.jda.gracefulkilljava;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;
import sampleapp.SampleApplicationWithShutdownLogic;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Unit test for simple App.
 */
public class GracefulKillTest
{
    public static final String KILLED_FILE="killed.txt";
    /**
     * Launch a java process and then try to kill it
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        try {
            String userDir = System.getProperty("user.dir");
            Assert.assertNotNull("user.dir", userDir);

            File theUserDir = new File(userDir);
            Assert.assertTrue(theUserDir.exists());
            File targetDir = new File(theUserDir, "target");
            Assert.assertTrue(targetDir.exists());

            ProcessBuilder processBuilder = new ProcessBuilder("for_ut_usage_only.bat");
            processBuilder.directory(theUserDir);
            Process javaProcess = processBuilder.start();
            Thread.sleep(2000);
            ProcessBuilder killProcessBuilder = new ProcessBuilder(Arrays.asList("graceful-kill-java.bat","SampleApplicationWithShutdownLogic"));
            killProcessBuilder.directory(theUserDir);

            File log = new File(targetDir,"gracefulkill_ut.log");
            killProcessBuilder.redirectErrorStream(true);
            killProcessBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(log));

            assertTrue(javaProcess.isAlive());

            Process killProcess = killProcessBuilder.start();
            killProcess.waitFor(60, TimeUnit.SECONDS);
            javaProcess.waitFor(60, TimeUnit.SECONDS);
            int numLines =0;
            File killedFile = new File(targetDir, KILLED_FILE);
            try(BufferedReader reader = new BufferedReader(new FileReader(killedFile))){
                String line = reader.readLine();
                while (line != null){
                    if (!line.isEmpty()) {
                        Assert.assertTrue("Line found is: "+line, SampleApplicationWithShutdownLogic.LINE_CONTENT.equals(line));
                    }
                    numLines++;
                    line = reader.readLine();
                }
            }
            assertEquals(SampleApplicationWithShutdownLogic.NUM_LINES, numLines);

        } catch (IOException | InterruptedException e) {
            Assert.fail(e.getMessage());
        }
    }
}
