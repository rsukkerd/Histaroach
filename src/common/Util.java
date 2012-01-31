package common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
    
    /**
     * write an object in a human-readable form to output file
     */
    public static void writeToHumanReadableFile(String fileName, Object object) {
        BufferedWriter outFileWriter;
        
		try {
			outFileWriter = new BufferedWriter(new FileWriter(fileName));
			outFileWriter.write(object.toString());
	        outFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
