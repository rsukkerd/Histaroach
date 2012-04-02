package ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.BuildStrategy;
import common.Pair;
import common.TestResult;
import common.Util;
import common.Revision.COMPILABLE;

/**
 * AntBuildStrategy is an implementation of BuildStrategy. 
 * AntBuildStrategy is associated with Ant.
 */
public class AntBuildStrategy implements BuildStrategy, Serializable {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 5365077245485076752L;
	
	private static final String TEST_PATTERN = "\\s*\\[junit\\] Running (\\S+)";
	private static final String FAILED_TEST_PATTERN = "\\s*\\[junit\\] Test (\\S+) FAILED";
	private static final String BUILD_SUCCESSFUL_PATTERN = "BUILD SUCCESSFUL";
	private static final String BUILD_FAILED_PATTERN = "BUILD FAILED";
	
	private static final String RUN_SCRIPT_COMMAND = "./run_test.sh";
	private static final String TEST_OUTPUT = "output/run_test_output";
	private static final String TEST_ERROR = "output/run_test_error";

	private final File directory;
	private final String[] antTestCommand;
	private final String antTestString;
	
	public AntBuildStrategy(File directory, String antCommand, String testCommand) {
		this.directory = directory;
		
		antTestString = antCommand + " " + testCommand;
		antTestCommand = antTestString.split(" ");
	}
	
	@Override
	public Pair<COMPILABLE, TestResult> runTest(String commitID) 
			throws IOException, InterruptedException {
		Process process = Util.runProcess(antTestCommand, directory);

        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);

        COMPILABLE compilable = buildSuccessful(outputStreamContent, errorStreamContent);
        TestResult testResult = null;
        
        if (compilable == COMPILABLE.YES) {
            testResult = getTestResult(commitID, outputStreamContent, errorStreamContent);
        }

        return new Pair<COMPILABLE, TestResult>(compilable, testResult);
	}

	@Override
	public Pair<COMPILABLE, TestResult> runTestViaShellScript(String commitID) throws Exception {
		String workingDir = System.getProperty("user.dir");
    	File dir = new File(workingDir);
    	
    	String outputStream = workingDir + File.separatorChar + TEST_OUTPUT;
    	String errorStream = workingDir + File.separatorChar + TEST_ERROR;
    	
    	String[] command = new String[] { RUN_SCRIPT_COMMAND, directory.getPath(), 
    			antTestString, outputStream, errorStream, };
    	    	
        Process process = Util.runProcess(command, dir);
        
        if (process != null) { // create dependency
	        BufferedReader stdOutputReader = new BufferedReader(
	                new FileReader(new File(TEST_OUTPUT)));
	
	        BufferedReader stdErrorReader = new BufferedReader(
	                new FileReader(new File(TEST_ERROR)));
	
	        List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
	        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
	        
	        COMPILABLE compilable = buildSuccessful(outputStreamContent, errorStreamContent);
	        TestResult testResult = null;
	        
	        if (compilable == COMPILABLE.YES) {
	            testResult = getTestResult(commitID, outputStreamContent, errorStreamContent);
	        }
	        
	        return new Pair<COMPILABLE, TestResult>(compilable, testResult);
        } else {
        	// should not happen; Exception should already be thrown at Util.runProcess
        	assert false;
        	return null;
        }
	}

	@Override
	public COMPILABLE buildSuccessful(List<String> outputStreamContent,
			List<String> errorStreamContent) {
		Pattern buildSuccessfulPattern = Pattern.compile(BUILD_SUCCESSFUL_PATTERN);
	    Pattern buildFailedPattern = Pattern.compile(BUILD_FAILED_PATTERN);
	
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

	@Override
	public TestResult getTestResult(String commitID,
			List<String> outputStreamContent, List<String> errorStreamContent) {
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
    	
    	TestResult testResult = new TestResult(commitID, allTests, failedTests);
    	
		return testResult;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        AntBuildStrategy buildStrategy = (AntBuildStrategy) other;
        
        return directory.equals(buildStrategy.directory) 
        		&& antTestCommand.equals(buildStrategy.antTestCommand);
	}
	
	@Override
	public int hashCode() {
		return 11 * directory.hashCode() + 13 * antTestCommand.hashCode();
	}

}
