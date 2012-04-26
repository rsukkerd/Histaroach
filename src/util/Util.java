package util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;

import common.HistoryGraph;
import common.MixedRevisionTemplate;
import common.MixedRevisionTemplatesGenerator;
import common.Revision;

public class Util {
	
	/**
	 * Single space character
	 */
	public static final String SINGLE_SPACE_CHAR = " ";
	
	/**
	 * Create a process that executes the given command 
	 * in the given directory.
	 * 
	 * @return the process
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
    public static Process runProcess(String[] command, File processDir) throws IOException, InterruptedException {
        ProcessBuilder pBuilder = new ProcessBuilder(command);
        pBuilder.directory(processDir);
        Process p = null;
        
        p = pBuilder.start();
        p.waitFor(); // make current thread waits until this process terminates
        
        return p;
    }
    
    /**
     * Read and cache content from reader.
     * 
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
     * Write an object to a serialized output file.
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
     * Write an object in a human-readable form to an output file.
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
     * Read an object from a serialized input file.
     * 
     * @return an object of type T
     */
    public static <T> T readObject(Class<T> type, String fileName) {
    	T object = null;
    	ObjectInputStream input;
    	
    	try {
			input = new ObjectInputStream(new FileInputStream(fileName));
			object = (T) input.readObject();
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
     * Untar a repoTarFile and put its entries in repoDir.
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
    
    public static class RevisionFileFilter implements FileFilter {
    	private static final int LENGTH = 11;
    	private static final Pattern REVISION_PATTERN = Pattern.compile("[a-f0-9]{7}.ser");
    	
		@Override
		public boolean accept(File pathname) {
			String fileName = pathname.getName();
			Matcher revisionMatcher = REVISION_PATTERN.matcher(fileName);
			
			return fileName.length() == LENGTH && revisionMatcher.find();
		}
    }
    
    /**
     * Reconstruct a HistoryGraph instance from serialized Revision files.
     * 
     * @return a HistoryGraph containing all revisions corresponding 
     *         to serialized revision files in serializedRevisionsDir
     */
    public static HistoryGraph reconstructHistoryGraph(File serializedRevisionsDir) {
    	HistoryGraph hGraph = new HistoryGraph();
    	
    	FileFilter filter = new RevisionFileFilter();
    	File[] revisionFiles = serializedRevisionsDir.listFiles(filter);
    	
    	for (File revisionFile : revisionFiles) {
    		Revision revision = readObject(Revision.class, revisionFile.getPath());
    		hGraph.addRevision(revision);
    	}
    	
    	return hGraph;
    }
}
