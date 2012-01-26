package common;

import java.io.File;

public class Util {

    public static Process runProcess(String[] commands, File processDir) {
        ProcessBuilder pBuilder = new ProcessBuilder(commands);
        pBuilder.directory(processDir);
        Process p = null;
        try {
            p = pBuilder.start();

            try {
                // make current thread waits until this process terminates
                p.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return p;
    }

}
