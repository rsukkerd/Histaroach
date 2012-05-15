package histaroach.buildstrategy;

import histaroach.model.TestResult;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * MyBuildStrategy is for sample project used in unit testing.
 */
public class MyBuildStrategy extends AntBuildStrategy {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -5588843108793112280L;
	
	private static final String TEST_PATTERN = "\\s*\\[junit\\] Running (\\S+)";
	private static final String FAILED_TEST_PATTERN = "\\s*\\[junit\\] Test (\\S+) FAILED";
	
	private static final String BUILD_TARGET_NAME = "build";
	private static final String TEST_TARGET_NAME = "junit";
	
	public MyBuildStrategy(File directory, String antCommand) {
		super(directory, antCommand, BUILD_TARGET_NAME, TEST_TARGET_NAME);
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
