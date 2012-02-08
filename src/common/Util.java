package common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

//import org.apache.tools.tar.TarEntry;
//import org.apache.tools.tar.TarInputStream;

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
    
    /**
     * untar a repoTarFile and put its entries in repoDir
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void untar(String repoTarFile, String repoDir) throws FileNotFoundException, IOException {
    	File dest = new File(repoDir);
    	dest.mkdir();
    	
    	TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(repoTarFile)));
		TarEntry entry;
		while((entry = tis.getNextEntry()) != null) {
			File destPath = new File(dest.toString() + File.separatorChar + entry.getName());
			
			if (entry.isDirectory()) {
				destPath.mkdirs();
			} else {
				int count;
				byte data[] = new byte[2048];
				
				FileOutputStream fos = new FileOutputStream(destPath);
				BufferedOutputStream bos = new BufferedOutputStream(fos);

				while ((count = tis.read(data)) != -1) {
					bos.write(data, 0, count);
				}
				
				bos.flush();
				bos.close();
			}
		}
		
		tis.close();
	}
}
