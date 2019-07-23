package sampleapp;

import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SampleApplicationWithShutdownLogic{
    public static final String LINE_CONTENT="1234567890";
    public static final int NUM_LINES=100;
    public static void main(String[] args) throws InterruptedException {
        Runtime.getRuntime().addShutdownHook(new TestShutdownHook());
        while (true){
            Thread.sleep(1000);
            System.out.println("Still alive!");
        }
    }
    static class TestShutdownHook extends Thread {
        @Override
        public void run()
        {
            String userDir = System.getProperty("user.dir");
            Assert.assertNotNull("user.dir", userDir);

            File theUserDir = new File(userDir);
            Assert.assertTrue(theUserDir.exists());
            File targetDir = new File(theUserDir, "target");
            Assert.assertTrue(targetDir.exists());

            File f = new File(targetDir, "killed.txt");
            try {
                Assert.assertTrue("creating file failed, attempting "+f.getAbsolutePath(),f.createNewFile());
                try (FileWriter fileWriter = new FileWriter(f, false)) {
                    for (int i = 0; i < NUM_LINES; i++) {
                        fileWriter.write(LINE_CONTENT);
                        fileWriter.write("\n");
                        Thread.sleep(100);
                    }
                } //auto close FileWriter
            } catch (IOException | InterruptedException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}