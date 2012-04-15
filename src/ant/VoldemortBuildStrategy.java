package ant;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestResult;

/**
 * VoldemortBuildStrategy is a concrete subclass of AntBuildStrategy. 
 * 
 * For voldemort project.
 */
public class VoldemortBuildStrategy extends AntBuildStrategy {

	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -4473334473853444036L;
	
	private static final String TEST_PATTERN = "\\s*\\[junit\\] Running (\\S+)";
	private static final String FAILED_TEST_PATTERN = "\\s*\\[junit\\] Test (\\S+) FAILED";
	private static final String TEST_COMMAND = "junit";
	
	/**
	 * Creates a VoldemortBuildStrategy.
	 */
	public VoldemortBuildStrategy(File directory, String antCommand) {
		super(directory, antCommand, TEST_COMMAND);
	}

	@Override
	protected TestResult getTestResult(List<String> outputStreamContent, 
			List<String> errorStreamContent) {
		Set<String> allTests = new HashSet<String>();
		Set<String> failedTests = new HashSet<String>();
		
		Pattern testPattern = Pattern.compile(TEST_PATTERN);
		Pattern failedTestPattern = Pattern.compile(FAILED_TEST_PATTERN);
		
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
		
		TestResult testResult = new TestResult(allTests, failedTests);
		
		return testResult;
	}

}
