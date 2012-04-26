package ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.Pair;
import util.Util;

import common.BuildStrategy;
import common.TestResult;
import common.Revision.Compilable;

/**
 * AntBuildStrategy is an implementation of BuildStrategy Interface. 
 * 
 * For ant build tool. 
 * 
 * AntBuildStrategy is an abstract class; it contains the following 
 * abstract method: 
 *  - getTestResult(outputStreamContent, errorStreamContent): 
 *    parses test results from output and error streams 
 *    and returns a TestResult. 
 * 
 * AntBuildStrategy is immutable.
 */
public abstract class AntBuildStrategy implements BuildStrategy, Serializable {
	
	/**
	 * Ant's build file.
	 */
	public static final String BUILD_XML = "build.xml";
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 5365077245485076752L;
	
	private static final String BUILD_SUCCESSFUL_PATTERN = "BUILD SUCCESSFUL";
	private static final String BUILD_FAILED_PATTERN = "BUILD FAILED";
	
	private static final String RUN_SCRIPT_COMMAND = "./run_test.sh";
	private static final String TEST_OUTPUT = "output/run_test_stdout";
	private static final String TEST_ERROR = "output/run_test_stderr";
	
	private final File directory;
	private final String antTestCmdStr;
	private final String[] antTestCmdArr;
	
	/**
	 * Creates an AntBuildStrategy.
	 */
	protected AntBuildStrategy(File directory, String antCommand, String testCommand) {
		this.directory = directory;
		antTestCmdStr = antCommand + Util.SINGLE_SPACE_CHAR + testCommand;
		antTestCmdArr = antTestCmdStr.split(Util.SINGLE_SPACE_CHAR);
	}
	
	@Override
	public Pair<Compilable, TestResult> runTest() throws Exception {
		Process process = Util.runProcess(antTestCmdArr, directory);

        BufferedReader stdOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        BufferedReader stdErrorReader = new BufferedReader(
                new InputStreamReader(process.getErrorStream()));

        List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);

        Compilable compilable = buildSuccessful(outputStreamContent, errorStreamContent);
        TestResult testResult = null;
        
        if (compilable == Compilable.YES) {
            testResult = getTestResult(outputStreamContent, errorStreamContent);
        }

        return new Pair<Compilable, TestResult>(compilable, testResult);
	}

	@Override
	public Pair<Compilable, TestResult> runTestViaShellScript() throws Exception {
		String workingDir = System.getProperty("user.dir");
    	File dir = new File(workingDir);
    	
    	String outputStream = workingDir + File.separatorChar + TEST_OUTPUT;
    	String errorStream = workingDir + File.separatorChar + TEST_ERROR;
    	
    	String[] command = new String[] { RUN_SCRIPT_COMMAND, directory.getPath(), 
    			antTestCmdStr, outputStream, errorStream, };
    	    	
        Util.runProcess(command, dir);
        
        BufferedReader stdOutputReader = new BufferedReader(
                new FileReader(new File(TEST_OUTPUT)));

        BufferedReader stdErrorReader = new BufferedReader(
                new FileReader(new File(TEST_ERROR)));

        List<String> outputStreamContent = Util.getStreamContent(stdOutputReader);
        List<String> errorStreamContent = Util.getStreamContent(stdErrorReader);
        
        Compilable compilable = buildSuccessful(outputStreamContent, errorStreamContent);
        TestResult testResult = null;
        
        if (compilable == Compilable.YES) {
            testResult = getTestResult(outputStreamContent, errorStreamContent);
        }
        
        return new Pair<Compilable, TestResult>(compilable, testResult);
	}
	
	/**
	 * Parses test results from output and error streams 
	 * and returns a TestResult.
	 * 
	 * @return a TestResult.
	 */
	protected abstract TestResult getTestResult(List<String> outputStreamContent, 
			List<String> errorStreamContent);

	/**
     * @return YES if build successful, NO if build failed, 
     *         and NO_BUILD_FILE if there is no build file.
     */
	private Compilable buildSuccessful(List<String> outputStreamContent,
			List<String> errorStreamContent) {
		Pattern buildSuccessfulPattern = Pattern.compile(BUILD_SUCCESSFUL_PATTERN);
	    Pattern buildFailedPattern = Pattern.compile(BUILD_FAILED_PATTERN);
	
	    for (String line : outputStreamContent) {
	        Matcher buildSuccessfulMatcher = buildSuccessfulPattern.matcher(line);
	        if (buildSuccessfulMatcher.find()) {
	            return Compilable.YES;
	        }
	    }
	
	    for (String line : errorStreamContent) {
	        Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
	        if (buildFailedMatcher.find()) {
	            return Compilable.NO;
	        }
	    }
	
	    return Compilable.NO_BUILD_FILE;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        AntBuildStrategy buildStrategy = (AntBuildStrategy) other;
        
        return directory.equals(buildStrategy.directory) 
        		&& antTestCmdStr.equals(buildStrategy.antTestCmdStr);
	}
	
	@Override
	public int hashCode() {
		return 11 * directory.hashCode() + 13 * antTestCmdStr.hashCode();
	}

}
