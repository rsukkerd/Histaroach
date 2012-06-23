package histaroach.buildstrategy;

import histaroach.model.DiffFile;
import histaroach.model.TestResult;
import histaroach.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * VoldemortBuildStrategy is for voldemort project.
 */
public class VoldemortBuildStrategy extends AntBuildStrategy {

	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -4473334473853444036L;
	
	private static final String TEST_PATTERN = "\\s*\\[junit\\] Running (\\S+)";
	private static final String FAILED_TEST_PATTERN = "\\s*\\[junit\\] Test (\\S+) FAILED";
	
	private static final String BUILD_TARGET_NAME = "build buildtest";
	private static final String TEST_TARGET_NAME = "junit";
	private static final String SINGLE_TEST_TARGET_NAME = "junit-test -Dtest.name=";
	
	private static final String TEST_FILE_SUFFIX = "Test.java";
	
	private final String antCommand;
	
	/**
	 * Creates a VoldemortBuildStrategy.
	 */
	public VoldemortBuildStrategy(File directory, String antCommand) {
		super(directory, antCommand, BUILD_TARGET_NAME, TEST_TARGET_NAME);
		this.antCommand = antCommand;
	}

	@Override
	public boolean runSingleTest(String testname) throws IOException, 
			InterruptedException {
		TestResult result = runTest(antCommand + Util.SINGLE_SPACE_CHAR + 
				SINGLE_TEST_TARGET_NAME + testname);
		return result.pass(testname);
	}

	@Override
	public boolean isTestFile(DiffFile diffFile) {
		return diffFile.getFileName().endsWith(TEST_FILE_SUFFIX);
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
