package histaroach.buildstrategy;

import histaroach.model.Revision.Compilable;
import histaroach.model.TestResult;
import histaroach.util.Pair;
import histaroach.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * AntBuildStrategy is for ant build tool. 
 * 
 * AntBuildStrategy is immutable.
 */
public abstract class AntBuildStrategy implements IBuildStrategy, Serializable {
	
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
	
	private static final String RUN_TEST_SH_CMD = "./run_test.sh";
	private static final String RUN_TEST_STDOUT = "output/run_test_stdout";
	private static final String RUN_TEST_STDERR = "output/run_test_stderr";
	
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
		Process runTestProcess = Util.runProcess(antTestCmdArr, directory);
        
        List<String> outputStreamContent = Util.getInputStreamContent(
        		runTestProcess.getInputStream());
        List<String> errorStreamContent = Util.getInputStreamContent(
        		runTestProcess.getErrorStream());

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
    	
    	String outputStream = workingDir + File.separatorChar + RUN_TEST_STDOUT;
    	String errorStream = workingDir + File.separatorChar + RUN_TEST_STDERR;
    	
    	String[] command = new String[] { RUN_TEST_SH_CMD, directory.getPath(), 
    			antTestCmdStr, outputStream, errorStream, };
    	    	
        Util.runProcess(command, dir);
        
        FileInputStream stdOutStream = new FileInputStream(new File(RUN_TEST_STDOUT));
        FileInputStream stdErrStream = new FileInputStream(new File(RUN_TEST_STDERR));
        
        List<String> outputStreamContent = Util.getInputStreamContent(stdOutStream);
        List<String> errorStreamContent = Util.getInputStreamContent(stdErrStream);
        
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
