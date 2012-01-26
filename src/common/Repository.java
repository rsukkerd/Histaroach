package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
	public static final String[] LOG_COMMAND = { "git", "log", "--pretty=format:%h %p" };
	public static final String[] JUNIT_COMMAND = { "ant", "junit" };
	public static final String SINGLE_TEST_COMMAND = "ant junit-test -Dtest.name=";
	
	private final File directory;
	private final String outputFileName;
	
	public Repository(String pathname, String outputFileName) {
		directory = new File(pathname);
		this.outputFileName = outputFileName;
	}
	
	public File getDirectory() {
		return directory;
	}
	
	/**
    * Checks out a particular commit from this repository.
    * @return exit value of 'git checkout' process
    */
    public int checkoutCommit(String commitID) {
        int exitValue = -1;

        ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout", commitID);
        checkoutBuilder.directory(directory);

        try {
            Process checkoutProcess = checkoutBuilder.start();

            try {
                // make current thread waits until this process terminates
                exitValue = checkoutProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return exitValue;
    }
    
    /**
     * @return diff files between childCommit and parentCommit
     */
    public List<String> getDiffFiles(String childID, String parentID) {
        List<String> diffFiles = new ArrayList<String>();

        ProcessBuilder diffBuilder = new ProcessBuilder("git", "diff", "--name-status", childID, parentID);
        diffBuilder.directory(directory);

        try {
            Process diffProcess = diffBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    diffProcess.getInputStream()));

            String line = new String();
            while ((line = reader.readLine()) != null) {
                diffFiles.add(line);
            }

            try {
                // make current thread waits until this process terminates
                diffProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return diffFiles;
    }
    
    public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) throws IOException {
    	HistoryGraph hGraph = new HistoryGraph();
    	
    	FileWriter outFileStream = new FileWriter(outputFileName);
    	BufferedWriter outFileWriter = new BufferedWriter(outFileStream);
    	
    	int exitValue = checkoutCommit(startCommitID);
    	
    	if (exitValue != 0) {
            System.out.println("'git checkout' process returns non-zero exit value");
            // TODO: Do something sensible -- graceful recovery.
        } else {
            ProcessBuilder logBuilder = new ProcessBuilder(LOG_COMMAND);
            logBuilder.directory(directory);

            try {
                Process logProcess = logBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        logProcess.getInputStream()));

                String line = new String();
                while ((line = reader.readLine()) != null) {
                    String[] hashes = line.split(" ");
                    
                    String commitID = hashes[0];
                    Map<String, List<String>> parentIDToDiffFiles = new HashMap<String, List<String>>();

                    if (hashes.length > 1) {
                        for (int i = 1; i < hashes.length; i++) {
                            String parentID = hashes[i];                            
                            List<String> diffFiles =  getDiffFiles(commitID, parentID);
                            
                            parentIDToDiffFiles.put(parentID, diffFiles);
                        }
                    }
                    
                    Revision revision = new Revision(this, commitID, parentIDToDiffFiles);
                    hGraph.addRevision(revision);
                    
                    outFileWriter.write(revision.toString());
                    outFileWriter.write("\n");

                    if (commitID.equals(endCommitID)) {
                        break;
                    }
                }
                
                outFileWriter.close();

                try {
                    // make current thread waits until this process terminates
                    logProcess.waitFor();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    	
    	return hGraph;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Repository repository = (Repository) other;
        return directory.equals(repository.directory);
    }
    
    @Override
    public int hashCode() {
    	return directory.hashCode();
    }
    
    @Override
    public String toString() {
    	return "repository location : " + directory.getAbsolutePath();
    }
}
