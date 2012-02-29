package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.DiffFile.DiffType;

/**
 * Repository represents a git repository.
 * 
 * Repository has access to the directory associated with it.
 * 
 * Repository contains methods to check out a commit into 
 * the working directory, and to build a graph structure 
 * representing the revision history of the repository.
 */
public class Repository implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -2999033773371301088L;

    public static final String[] LOG_COMMAND = { "git", "log",
            "--pretty=format:%h %p" };
    public static final String BUILD_COMMAND = "build";
    public static final String BUILDTEST_COMMAND = "buildtest";
    public static final String JUNIT_COMMAND = "junit";
    public static final String JUNIT_TEST_COMMAND = "junit-test -Dtest.name=";

    private final File directory;
    
    protected final String[] antBuild;
    protected final String[] antBuildtest;
    protected final String[] antJunit;

	/**
     * Create a repository
     * 
     * @param pathname : full path to the repository directory
     * @param antCommand : ant command
     */
    public Repository(String pathname, String antCommand) {
        directory = new File(pathname);
        
        String[] ant = antCommand.split(" ");
        antJunit = new String[ant.length + 1];
        antBuild = new String[ant.length + 1];
        antBuildtest = new String[ant.length + 1];

        for (int i = 0; i < ant.length; i++) {
        	antJunit[i] = ant[i];
        	antBuild[i] = ant[i];
        	antBuildtest[i] = ant[i];
        }

        antJunit[antJunit.length - 1] = JUNIT_COMMAND;
        antBuild[antBuild.length - 1] = BUILD_COMMAND;
        antBuildtest[antBuildtest.length - 1] = BUILDTEST_COMMAND;
    }

    /**
     * @return directory of this repository
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Build a HistoryGraph instance containing revisions from startCommit 
     * to the root commit.
     * Initially, each revision in the HistoryGraph has
     * compilable = UNKNOWN and TestResult = null
     * 
     * @return a history graph of this repository
     */
    public HistoryGraph buildHistoryGraph(String startCommitID) {
        HistoryGraph hGraph = new HistoryGraph(this);

        int exitValue = checkoutCommit(startCommitID);
        assert (exitValue == 0);

        Process logProcess = Util.runProcess(LOG_COMMAND, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));

        List<String> lines = Util.getStreamContent(reader);

        // mapping : parent ID -> list of its child revisions
        // used for referencing child to its parents later
        Map<String, List<Revision>> parentIDToChildRevisions = new HashMap<String, List<Revision>>();
        
        for (String line : lines) {
            String[] hashes = line.split(" ");

            String commitID = hashes[0];

            Revision revision = new Revision(this, commitID);
            hGraph.addRevision(revision);
            
            for (int i = 1; i < hashes.length; i++) {
            	if (parentIDToChildRevisions.containsKey(hashes[i])) {
            		parentIDToChildRevisions.get(hashes[i]).add(revision);
            	} else {
            		List<Revision> childRevisions = new ArrayList<Revision>();
            		childRevisions.add(revision);
            		parentIDToChildRevisions.put(hashes[i], childRevisions);
            	}
            }

            /* print progress to standard output */
            System.out.println(revision);
        }
        
        // referencing each child to its parents
        for (Revision revision : hGraph) {
        	String commitID = revision.getCommitID();
        	
        	if (parentIDToChildRevisions.containsKey(commitID)) {
        		List<Revision> childRevisions = parentIDToChildRevisions.get(commitID);
        		
        		for (Revision childRevision : childRevisions) {
        			String childID = childRevision.getCommitID();
        			List<DiffFile> files = getDiffFiles(childID, commitID);
        			
        			childRevision.addParent(revision, files);
        		}
        	}
        }

        return hGraph;
    }

    /**
	 * Checks out a given commit from this repository.
	 * @return exit value of 'git checkout' process
	 */
	public int checkoutCommit(String commitID) {
	    Process p = Util.runProcess(
	            new String[] { "git", "checkout", commitID }, directory);
	    return p.exitValue();
	}

	/**
	 * Helper method for buildHistoryGraph
	 * @return a list of diff files between childCommit and parentCommit
	 */
	private List<DiffFile> getDiffFiles(String childCommitID,
	        String parentCommitID) {
	    List<DiffFile> diffFiles = new ArrayList<DiffFile>();
	
	    Process p = Util.runProcess(new String[] { "git", "diff",
	            "--name-status", childCommitID, parentCommitID }, directory);
	
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
	            p.getInputStream()));
	
	    List<String> lines = Util.getStreamContent(reader);
	
	    for (String line : lines) {
	        String[] tokens = line.split("\\s");
	
	        DiffType type;
	        if (tokens[0].equals("A")) {
	            type = DiffType.ADDED;
	        } else if (tokens[0].equals("M")) {
	            type = DiffType.MODIFIED;
	        } else {
	            type = DiffType.DELETED;
	        }
	
	        DiffFile diffFile = new DiffFile(type, tokens[1]);
	
	        diffFiles.add(diffFile);
	    }
	
	    return diffFiles;
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
