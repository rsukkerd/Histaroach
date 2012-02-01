package voldemort;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestResult;

public class VoldemortTestResult extends TestResult {
	
    /**
	 * serial version ID
	 */
	private static final long serialVersionUID = 5271469198946606556L;

	/**
     * parse junit test results from a cached content of 
     * standard output and standard error streams
     * 
     * @return TestResult instance of a given commit
     */
    public VoldemortTestResult(String commitID, List<String> outputErrorStreamContent) {
        super(commitID);
        
    	Pattern testPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
    	Pattern failedTestPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
    	
    	for (String line : outputErrorStreamContent) {
    		Matcher testMatcher = testPattern.matcher(line);
    		Matcher failedTestMatcher = failedTestPattern.matcher(line);
    		
            if (testMatcher.find()) {
                this.addTest(testMatcher.group(1));
            }
            if (failedTestMatcher.find()) {
                this.addFailedTest(failedTestMatcher.group(1));
            }
    	}
    }
}
