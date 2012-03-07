package voldemort;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestResult;

public class VoldemortTestResult extends TestResult {
	
    /**
	 * serial version ID
	 */
	private static final long serialVersionUID = 5271469198946606556L;

	/**
     * Create a VoldemortTestResult instance. 
     * 
     * Parse junit test results from a cached content of 
     * standard output and error stream.
     */
    public VoldemortTestResult(String commitID, List<String> outputStreamContent, List<String> errorStreamContent) {
        super(commitID);
        
    	Pattern testPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
    	Pattern failedTestPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
    	
    	for (String line : outputStreamContent) {
    		Matcher testMatcher = testPattern.matcher(line);
            if (testMatcher.find()) {
                this.addTest(testMatcher.group(1));
            }
    	}
    	
    	for (String line : errorStreamContent) {
    		Matcher failedTestMatcher = failedTestPattern.matcher(line);
            if (failedTestMatcher.find()) {
                this.addFailedTest(failedTestMatcher.group(1));
            }
    	}
    }
    
    /**
     * Create a VoldemortTestResult instance. 
     * 
     * Use junit test results from given sets of all tests and failed tests.
     */
    public VoldemortTestResult(String commitID, Set<String> allTests, Set<String> failedTests) {
    	super(commitID, allTests, failedTests);
    }
}
