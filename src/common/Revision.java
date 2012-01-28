package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import voldemort.VoldemortTestResult;

/**
 * Revision contains
 * 1. a reference to its repository
 * 2. commit id
 * 3. a set of its parents' commit ids
 * 4. diff files between this revision and each of its parent
 * 5. compilable flag
 * 6. test result
 */
public class Revision {
	public enum COMPILABLE {
		YES,
		NO,
		UNKNOWN
	}
	
    private final Repository repository;
    private final String commitID;
    /** mapping : a parent commit id -> a list of files that are different between the parent and this revision **/
	private final Map<String, List<String>> diffFiles;
	private COMPILABLE compilable;
	private /*@Nullable*/ TestResult testResult;
	
	/**
	 * create a revision
	 * initially, compilable flag is unknown and test result is null
	 */
    public Revision(Repository repository, String commitID, Map<String, List<String>> parentIDToDiffFiles) {
        this.repository = repository;
        this.commitID = commitID;
        diffFiles = parentIDToDiffFiles;
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;
    }

    public String getCommitID() {
        return commitID;
    }

    public Set<String> getParentIDs() {
        return diffFiles.keySet();
    }
    
    public List<String> getDiffFiles(String parentID) {
	    return diffFiles.get(parentID);
	}

	public COMPILABLE isCompilable() {
    	if (compilable == COMPILABLE.UNKNOWN) {
    		compileAndRunTests();
    	}
    	
    	return compilable;
    }

    public TestResult getTestResult() {
    	if (compilable == COMPILABLE.UNKNOWN) {
    		compileAndRunTests();
    	}
    	
        return testResult;
    }

    /**
     * compile and run all junit tests on this revision to generate TestResult
     * @modifies : set compilable flag to YES/NO,
     *             generate test result if compilable
     */
    private void compileAndRunTests() {
        int exitValue = repository.checkoutCommit(commitID);
        assert (exitValue == 0);

        Process junitProcess = Util.runProcess(Repository.JUNIT_COMMAND,
                repository.getDirectory());
        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(junitProcess.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(junitProcess.getErrorStream()));
        
        if (buildFailed(stdErrorReader)) {
        	compilable = COMPILABLE.NO;
        } else {
        	compilable = COMPILABLE.YES;
        }
        
        if (compilable == COMPILABLE.YES) {
        	testResult = new VoldemortTestResult(commitID, stdOutputReader, stdErrorReader);
        }
    }
    
    /**
     * @return true iff build failed
     */
    private boolean buildFailed(BufferedReader stdErrorReader) {
        Pattern buildFailedPattern = Pattern.compile("BUILD FAILED");
        String line = new String();
        
        try {
            while ((line = stdErrorReader.readLine()) != null) {
            	Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
                if (buildFailedMatcher.find()) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository) && commitID.equals(revision.commitID)
                && diffFiles.equals(revision.diffFiles) && compilable == revision.compilable
                && ((testResult == null && revision.testResult == null) || 
                		testResult.equals(revision.testResult));
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        			+ 17 * diffFiles.hashCode() + 19 * compilable.hashCode();
        if (testResult != null) {
        	code += 23 * testResult.hashCode();
        }
        
        return code;
    }

    @Override
    public String toString() {
        String result = "commit : " + commitID + "\n";
        result += "compilable : ";
        if (compilable == COMPILABLE.YES) {
        	result += "yes\n";
        	result += testResult.toString();
        } else if (compilable == COMPILABLE.NO) {
        	result += "no\n";
        } else {
        	result += "unknown\n";
        }

        for (String parentID : diffFiles.keySet()) {
            result += "parent : " + parentID + "\n";
            result += "diff files :\n";
            List<String> files = getDiffFiles(parentID);

            for (String file : files) {
                result += file + "\n";
            }
        }
        
        return result;
    }
}
