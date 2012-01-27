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
    private final Repository repository;
    private final String commitID;
    private boolean compilable;
    /** mapping : a parent commit id -> a list of files that are different between the parent and this revision **/
	private final Map<String, List<String>> diffFiles;
	private final /*@Nullable*/ TestResult testResult;
	
    public Revision(Repository repository, String commitID, Map<String, List<String>> parentIDToDiffFiles) {
        this.repository = repository;
        this.commitID = commitID;
        diffFiles = parentIDToDiffFiles;
        testResult = generateTestResult();
    }

    public String getCommitID() {
        return commitID;
    }

    public Set<String> getParentIDs() {
        return diffFiles.keySet();
    }
    
    public boolean isCompilable() {
    	return compilable;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * run all junit tests on this revision to generate TestResult
     * 
     * @return TestResult of this revision
     */
    public TestResult generateTestResult() {
        int exitValue = repository.checkoutCommit(commitID);
        assert (exitValue == 0);

        Process junitProcess = Util.runProcess(Repository.JUNIT_COMMAND,
                repository.getDirectory());
        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(junitProcess.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(junitProcess.getErrorStream()));
        
        TestResult testResult = null;
        compilable = !buildFailed(stdErrorReader);
        
        if (compilable) {
        	testResult = new VoldemortTestResult(commitID, stdOutputReader, stdErrorReader);
        }

        return testResult;
    }
    
    /**
     * @return true iff build failed
     */
    public boolean buildFailed(BufferedReader stdErrorReader) {
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

    public List<String> getDiffFiles(String parentID) {
        return diffFiles.get(parentID);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository)
                && commitID.equals(revision.commitID)
                && testResult.equals(revision.testResult)
                && diffFiles.equals(revision.diffFiles);
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        			+ 17 * diffFiles.hashCode();
        if (testResult != null) {
        	code += 19 * testResult.hashCode();
        }
        
        return code;
    }

    @Override
    public String toString() {
        String result = "commit : " + commitID + "\n";
        result += "compilable : ";
        if (compilable) {
        	result += "yes\n";
        	result += testResult.toString();
        } else {
        	result += "no\n";
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
