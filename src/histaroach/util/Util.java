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
import java.lang.management.ManagementFactory;
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
	public static final int TIMEOUT = 3600;
	public static final String KILL_JAVA_PROCESSES_SH = "./kill_j_a_v_a_processes.sh";
	
	/**
	 * Creates a process that executes command in processDir.
	 * The process will be forcibly terminated after 1 hour.
	 * 
	 * @return the process
	 * @throws IOException 
	 * @throws InterruptedException when the started process had to be killed forcibly
	 */
	public static Process runProcess(String[] command, File processDir) 
			throws IOException, InterruptedException {
		ProcessBuilder pBuilder = new ProcessBuilder(command);
		pBuilder.directory(processDir);
		Process p = null;

		p = pBuilder.start();

		//timer setup
		ProcessKillTimer pkt = new ProcessKillTimer(p, TIMEOUT);
		Thread t = new Thread( pkt ); //timer thread for 1 hour
		t.start();

		p.waitFor(); // make current thread waits until this process terminates

		//cleanup timer
		if ( !pkt.killed ) {
			//the killed flag is false if the process terminated naturally
			//in this case we have to make sure that the timer thread stops waiting and doesn't interfere later on
			t.interrupt();
		} else {
			//the process had to be killed by us, and thus this is a problem
			throw new InterruptedException( "Process had to be killed" );
		}

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
	
	/**
	 * Looks up this java application's PID. 
	 * Specific to Sun's JVM.
	 * 
	 * @return this java application's PID.
	 */
	public static String getOwnPID() {
		String jvmName = ManagementFactory.getRuntimeMXBean().getName();
		return jvmName.split("@")[0];
	}
}

final class ProcessKillTimer implements Runnable {

	private Process proc;
	int timeout;
	boolean killed = false;

	/**
	 * @param p the process to be killed after a timeout
	 * @param timeout in seconds
	 */
	ProcessKillTimer( Process p, int timeout ) {
		this.proc = p;
		this.timeout = timeout;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			synchronized (this) { 
				wait( timeout * 1000L );
			}
		} catch (InterruptedException e) {
			//this exception will be thrown when the main thread calls interrupt(),
			//which can only happen if the process terminated naturally
			return;
		}
		//kill the process and set the flag so we know we killed it
		proc.destroy();
		killed = true;
		
		// kill all other java processes under this user.
		Process p;
		try {
			String[] command = { Util.KILL_JAVA_PROCESSES_SH, Util.getOwnPID() };
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
		} catch (Exception e) {
			System.err.println("Warning: " + Util.KILL_JAVA_PROCESSES_SH 
					+ " has terminated unexpectedly.");
			e.printStackTrace();
		}
	}
}
