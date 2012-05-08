package histaroach.util;

import histaroach.model.MixedRevisionTemplate;
import histaroach.model.MixedRevisionTemplatesGenerator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;


public class Util {
	
	/**
	 * Single space character
	 */
	public static final String SINGLE_SPACE_CHAR = " ";
	
	/**
	 * Creates a process that executes command in processDir.
	 * 
	 * @return the process
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
    public static Process runProcess(String[] command, File processDir) 
    		throws IOException, InterruptedException {
        ProcessBuilder pBuilder = new ProcessBuilder(command);
        pBuilder.directory(processDir);
        Process p = null;
        
        p = pBuilder.start();
        p.waitFor(); // make current thread waits until this process terminates
        
        return p;
    }
    
    /**
     * Reads and caches content from inputStream.
     * 
     * @return a list of lines from inputStream.
     * @throws IOException
     */
    public static List<String> getInputStreamContent(InputStream inputStream) 
    		throws IOException {
    	BufferedReader bufferedReader = new BufferedReader(
    			new InputStreamReader(inputStream));
    	
    	List<String> lines = new ArrayList<String>();
    	String line;
    	
    	while ((line = bufferedReader.readLine()) != null) {
			lines.add(line);
		}
    	
    	return lines;
    }
    
    /**
     * Writes an object to a serialized output file.
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
    
    public static List<MixedRevisionTemplate> readMixedRevisionTemplates() {
    	List<MixedRevisionTemplate> object = null;
    	ObjectInputStream input;
    	
    	try {
			input = new ObjectInputStream(new FileInputStream(
					MixedRevisionTemplatesGenerator.MIXED_REVISION_TEMPLATES_FILE));
			object = (List<MixedRevisionTemplate>) input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return object;
    }
    
    /**
     * Untars a repoTarFile and put its entries in repoDir.
     * 
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void untar(String repoTarFile, String repoDir) 
    		throws FileNotFoundException, IOException {
    	File dest = new File(repoDir);
    	dest.mkdir();
    	
    	TarInputStream tis = new TarInputStream(new BufferedInputStream(
    			new FileInputStream(repoTarFile)));
    	
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
