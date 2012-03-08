package voldemort;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestParsingStrategy;
import common.TestResult;

/**
 * VoldemortTestParsingStrategy is an implementation of TestParsingStrategy 
 * interface.
 */
public class VoldemortTestParsingStrategy implements TestParsingStrategy {

	/**
	 * serial version id
	 */
	private static final long serialVersionUID = 6182988468639916299L;

	@Override
	public TestResult getTestResult(String commitID, List<String> outputStreamContent, List<String> errorStreamContent) {
		Set<String> allTests = new HashSet<String>();
		Set<String> failedTests = new HashSet<String>();
		
		Pattern testPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
    	Pattern failedTestPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
    	
    	for (String line : outputStreamContent) {
    		Matcher testMatcher = testPattern.matcher(line);
            if (testMatcher.find()) {
                allTests.add(testMatcher.group(1));
            }
    	}
    	
    	for (String line : errorStreamContent) {
    		Matcher failedTestMatcher = failedTestPattern.matcher(line);
            if (failedTestMatcher.find()) {
                failedTests.add(failedTestMatcher.group(1));
            }
    	}
    	
    	TestResult testResult = new TestResult(commitID, allTests, failedTests);
    	
		return testResult;
	}

}
