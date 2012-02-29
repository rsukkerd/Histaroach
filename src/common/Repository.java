package common;

import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import voldemort.VoldemortTestResult;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

/**
 * Repository represents a git repository.
 * 
 * Repository has access to the directory associated with it,
 * and the revision that is currently checked out.
 * 
 * Repository contains methods to check out a revision, compile 
 * it and run test(s) on that revision. These methods { compile(), 
 * compileAndRunAllTests(), and compileAndRunTest(test) } modify 
 * the state of the revision.
 * 
 * Repository also contains public methods that compile and run 
 * test(s) on the current revision but do not modify the revision. 
 * These methods are build(command) and run(command).
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
    private final String[] antJunit;
    private final String[] antBuild;
    private final String[] antBuildtest;
    
    private Revision currRevision;

    /**
     * create a repository instance
     * 
     * @param pathname
     *            : full path to the repository directory
     * @param antCommand
     *            : ant command
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
     * Helper method for buildHistoryGraph
	 * Checks out a particular commit from this repository.
	 * 
	 * @return exit value of 'git checkout' process
	 */
	private int checkoutCommit(String commitID) {
	    Process p = Util.runProcess(
	            new String[] { "git", "checkout", commitID }, directory);
	    return p.exitValue();
	}

	/**
	 * Helper method for buildHistoryGraph
	 * 
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

	/**
	 * check out the revision from this repository
	 * @return exit value of the check out process
	 */
	public int checkoutRevision(Revision revision) {
		currRevision = revision;
		return checkoutCommit(currRevision.getCommitID());
	}

	/**
	 * compile the current revision
	 * @modifies compilable flag of the current revision
	 */
	public void compile() {
		if (build(antBuild) && build(antBuildtest)) {
			currRevision.setCompilableFlag(COMPILABLE.YES);
		} else {
			currRevision.setCompilableFlag(COMPILABLE.NO);
		}
	}

	/**
	 * compile and run all tests on the current revision
	 * @modifies compilable flag and test result of the current revision
	 */
	public void compileAndRunAllTests() {
		/*@Nullable*/TestResult testResult = run(antJunit);
		currRevision.setTestResult(testResult);
		
		if (testResult != null) {
			currRevision.setCompilableFlag(COMPILABLE.YES);
		} else {
			currRevision.setCompilableFlag(COMPILABLE.NO);
		}
	}

	/**
	 * compile and run a specific test on the current revision
	 * @modifies test result and compilable flag of the current revision
	 */
	public void compildAndRunTest(String testName) {
		String[] antJunitTest = (JUNIT_TEST_COMMAND + testName).split(" ");
		TestResult result = run(antJunitTest);
		
		if (result != null) {
			currRevision.addTestResult(result);
			currRevision.setCompilableFlag(COMPILABLE.YES);
		} else {
			currRevision.setCompilableFlag(COMPILABLE.NO);
		}
	}

	/**
	 * build the current revision using a given command
	 * @return true if build successful, false if build failed
	 */
	public boolean build(String[] command) {
	    Process process = Util.runProcess(command, directory);
	
	    BufferedReader stdOutputReader = new BufferedReader(
	            new InputStreamReader(process.getInputStream()));
	
	    BufferedReader stdErrorReader = new BufferedReader(
	            new InputStreamReader(process.getErrorStream()));
	
	    List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
	    List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
	    
	    return buildSuccessful(outputStreamContent, errorStreamContent);
	}

	/**
	 * run a given test command on the current revision
	 * @return a TestResult of the test command
	 */
	public TestResult run(String[] testCommand) {
	    Process process = Util.runProcess(testCommand, directory);
	
	    BufferedReader stdOutputReader = new BufferedReader(
	            new InputStreamReader(process.getInputStream()));
	
	    BufferedReader stdErrorReader = new BufferedReader(
	            new InputStreamReader(process.getErrorStream()));
	
	    List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
	    List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
	    
	    if (buildSuccessful(outputStreamContent, errorStreamContent)) {
	    	return new VoldemortTestResult(currRevision.getCommitID(), outputStreamContent, errorStreamContent);
	    }
	    
	    return null;
	}

	/**
	 * Helper method for build(command) and run(command)
	 * @return true if build successful, false if build failed
	 */
	private boolean buildSuccessful(List<String> outputStreamContent, List<String> errorStreamContent) {
		Pattern buildSuccessfulPattern = Pattern.compile("BUILD SUCCESSFUL");
	    Pattern buildFailedPattern = Pattern.compile("BUILD FAILED");
	    
	    for (String line : outputStreamContent) {
	        Matcher buildSuccessfulMatcher = buildSuccessfulPattern.matcher(line);
	        if (buildSuccessfulMatcher.find()) {
	            return true;
	        }
	    }
	    
	    for (String line : errorStreamContent) {
	        Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
	        if (buildFailedMatcher.find()) {
	            return false;
	        }
	    }
	
	    fail("Neither BUILD SUCCESSFUL nor BUILD FAILED found");
	    return false;
	}

	public void copyFile(String filename, File srcDir, File destDir) throws IOException {
		File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar + filename);
		FileUtils.copyFileToDirectory(srcFile, destDir);
	}

	public void deleteFile(String filename, File dir) throws IOException {
		File file = new File(dir.getAbsolutePath() + File.separatorChar + filename);
		FileUtils.forceDelete(file);
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
