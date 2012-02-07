package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

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
            System.exit(1);
        }
        return p;
    }
    
    /**
     * read and cache content from reader
     * @return a list of lines obtained by reader.readline()
     * @throws IOException
     */
    public static List<String> getStreamContent(BufferedReader reader) {
    	List<String> lines = new ArrayList<String>();
    	
    	String line = new String();
    	try {
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
    	
    	return lines;
    }
    
    /**
     * write an object to serialized output file
     */
    public static void writeToSerializedFile(String fileName, Object object) {
    	ObjectOutputStream output;
    	
    	try {
			output = new ObjectOutputStream(new FileOutputStream(fileName));
			output.writeObject(object);
			output.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
