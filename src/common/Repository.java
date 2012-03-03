package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

import voldemort.VoldemortTestResult;

/**
 * Repository represents a git repository. Repository has access to the 
 * directory associated with it. Repository contains methods to check out 
 * a commit into the working directory, build, run tests, and build a graph 
 * structure representing the revision history of the repository. 
 * Repository is immutable.
 */
public class Repository {

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
     * Build a history graph containing revisions from startCommit to 
     * endCommit.
     * 
     * @return a history graph of this repository
     */
    public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) {
    	HistoryGraph hGraph = new HistoryGraph(this);
    	
    	int exitValue = checkoutCommit(startCommitID);
        assert (exitValue == 0);

        Process logProcess = Util.runProcess(LOG_COMMAND, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));

        List<String> lines = Util.getStreamContent(reader);
        
        // parent id -> a list of its children's ids
        Map<String, List<String>> parentIDToChildrenIDs = new HashMap<String, List<String>>();
        // child id -> a list of its parents' ids
        Map<String, List<String>> childIDToParentsIDs = new HashMap<String, List<String>>();
        
        // special case : the start revision has no children
        if (!lines.isEmpty()) {
        	String line = lines.get(0);
        	String[] hashes = line.split(" ");
        	
        	String topID = hashes[0];
        	parentIDToChildrenIDs.put(topID, new ArrayList<String>());
        }
        
        for (String line : lines) {
        	String[] hashes = line.split(" ");

            String childID = hashes[0];
            List<String> parentsIDs = new ArrayList<String>();
            
            for (int i = 1; i < hashes.length; i++) {
            	String parentID = hashes[i];
            	parentsIDs.add(parentID);
            	
            	if (parentIDToChildrenIDs.containsKey(parentID)) {
            		parentIDToChildrenIDs.get(parentID).add(childID);
            	} else {
            		List<String> childrenIDs = new ArrayList<String>();
            		childrenIDs.add(childID);
            		parentIDToChildrenIDs.put(parentID, childrenIDs);
            	}
            }
            
            childIDToParentsIDs.put(childID, parentsIDs);
            
            if (childID.equals(endCommitID)) {
            	break;
            }
        }
        
        // commit id -> number of its parents
        Map<String, Integer> incomingEdgeCounter = new HashMap<String, Integer>();
        
        for (String childID : childIDToParentsIDs.keySet()) {
        	int incomingEdges = 0;
        	List<String> parentsIDs = childIDToParentsIDs.get(childID);
        	for (String parentID : parentsIDs) {
        		if (childIDToParentsIDs.containsKey(parentID)) {
        			incomingEdges++;
        		}
        	}
        	
        	incomingEdgeCounter.put(childID, incomingEdges);
        }
        
        Set<String> noIncomingEdge = new HashSet<String>();
        
        for (String commitID : incomingEdgeCounter.keySet()) {
        	int count = incomingEdgeCounter.get(commitID);
        	if (count == 0) {
        		noIncomingEdge.add(commitID);
        	}
        }
        
        // commit id -> its revision object
        Map<String, Revision> revisions = new HashMap<String, Revision>();
        
        while (!noIncomingEdge.isEmpty()) {
        	Iterator<String> itr = noIncomingEdge.iterator();
        	String commitID = itr.next();
        	noIncomingEdge.remove(commitID);
        	
        	Map<Revision, List<DiffFile>> parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
        	
        	List<String> parentsIDs = childIDToParentsIDs.get(commitID);
        	for (String parentID : parentsIDs) {
        		Revision parent;
        		if (revisions.containsKey(parentID)) {
        			parent = revisions.get(parentID);
        		} else {
        			// dummy revision of parent
        			parent = new Revision(this, parentID, new HashMap<Revision, List<DiffFile>>(), 
        					COMPILABLE.UNKNOWN, null);
        		}
        		
        		List<DiffFile> diffFiles = getDiffFiles(commitID, parentID);
        		
        		parentToDiffFiles.put(parent, diffFiles);
        	}
        	
        	Revision revision = new Revision(this, commitID, parentToDiffFiles);
        	hGraph.addRevision(revision);
        	
        	revisions.put(commitID, revision);
        	
        	List<String> childrenIDs = parentIDToChildrenIDs.get(commitID);
        	
        	for (String childID : childrenIDs) {
        		int edges = incomingEdgeCounter.get(childID);
        		incomingEdgeCounter.put(childID, edges - 1);
        		
        		if (edges - 1 == 0) {
        			noIncomingEdge.add(childID);
        		}
        	}
        }
    	
    	return hGraph;
    }

    /**
     * Checks out a given commit from this repository.
     * 
     * @return exit value of 'git checkout' process
     */
    public int checkoutCommit(String commitID) {
        Process p = Util.runProcess(
                new String[] { "git", "checkout", commitID }, directory);
        return p.exitValue();
    }

    /**
     * Build using a given build command
     * 
     * @return YES if build successful, NO if build failed, 
     * and NO_BUILD_FILE if there is no build file
     */
    public COMPILABLE build(String[] buildCommand) {
        Process process = Util.runProcess(buildCommand, directory);

        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        List<String> outputStreamContent = Util
                .getStreamContent(stdOutputReader);
        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);

        return buildSuccessful(outputStreamContent, errorStreamContent);
    }

    /**
     * run a given test command
     * 
     * @return a pair of compilable flag and test result
     */
    public Pair<COMPILABLE, TestResult> run(String[] testCommand, String commitID) {
        Process process = Util.runProcess(testCommand, directory);

        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        List<String> outputStreamContent = Util
                .getStreamContent(stdOutputReader);
        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);

        COMPILABLE compilable = buildSuccessful(outputStreamContent, errorStreamContent);
        TestResult testResult = null;
        
        if (compilable == COMPILABLE.YES) {
            testResult = new VoldemortTestResult(commitID, outputStreamContent,
                    errorStreamContent);            
        }

        return new Pair<COMPILABLE, TestResult>(compilable, testResult);
    }

    /**
     * Helper method for build(command) and run(command)
     * 
     * @return YES if build successful, NO if build failed, 
     * and NO_BUILD_FILE if there is no build file
     */
    private COMPILABLE buildSuccessful(List<String> outputStreamContent,
            List<String> errorStreamContent) {
        Pattern buildSuccessfulPattern = Pattern.compile("BUILD SUCCESSFUL");
        Pattern buildFailedPattern = Pattern.compile("BUILD FAILED");

        for (String line : outputStreamContent) {
            Matcher buildSuccessfulMatcher = buildSuccessfulPattern
                    .matcher(line);
            if (buildSuccessfulMatcher.find()) {
                return COMPILABLE.YES;
            }
        }

        for (String line : errorStreamContent) {
            Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
            if (buildFailedMatcher.find()) {
                return COMPILABLE.NO;
            }
        }

        return COMPILABLE.NO_BUILD_FILE;
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
