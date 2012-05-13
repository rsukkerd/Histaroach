package histaroach.util;

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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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

	/**
	 * Copies a file from source directory to destination directory.
	 * 
	 * @modifies file system
	 * @throws IOException
	 */
	public static void copyFile(String filename, File srcDir, File destDir) throws IOException {
	    File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar 
	            + filename);
	    File destFile = new File(destDir.getAbsolutePath() + File.separatorChar 
	    		+ filename);
	    FileUtils.copyFile(srcFile, destFile);
	}

	/**
	 * Deletes a file from directory.
	 * 
	 * @modifies file system
	 * @throws IOException 
	 */
	public static void deleteFile(String filename, File dir) throws IOException {
	    File file = new File(dir.getAbsolutePath() + File.separatorChar
	            + filename);
		FileUtils.forceDelete(file);
	}
}
