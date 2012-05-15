package histaroach.buildstrategy;

import histaroach.model.Revision.Compilable;
import histaroach.model.TestResult;
import histaroach.util.Pair;
import histaroach.util.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private final String buildCommand;
	private final String testCommand;
	
	/**
	 * Creates an AntBuildStrategy.
	 */
	protected AntBuildStrategy(File directory, String antCommand, 
			String buildTargetName, String testTargetName) {
		this.directory = directory;
		this.buildCommand = antCommand + Util.SINGLE_SPACE_CHAR + buildTargetName;
		this.testCommand = antCommand + Util.SINGLE_SPACE_CHAR + testTargetName;
	}
	
	@Override
	public Compilable build() throws IOException, InterruptedException {
		Pair<List<String>, List<String>> result = run(buildCommand);
		List<String> outputStreamContent = result.getFirst();
		List<String> errorStreamContent = result.getSecond();
		
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
	public TestResult runTest() throws IOException, InterruptedException {
		Pair<List<String>, List<String>> result = run(testCommand);
		List<String> outputStreamContent = result.getFirst();
		List<String> errorStreamContent = result.getSecond();
		
		return getTestResult(outputStreamContent, errorStreamContent);
	}
	
	/**
	 * Runs the command.
	 * 
	 * @return outputStreamContent and errorStreamContent of the command.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private Pair<List<String>, List<String>> run(String command) 
			throws IOException, InterruptedException {
    	File workingDir = new File(System.getProperty("user.dir"));
    	    	
    	String outputStream = workingDir.getPath() + File.separatorChar + RUN_TEST_STDOUT;
    	String errorStream = workingDir.getPath() + File.separatorChar + RUN_TEST_STDERR;
    	
    	String[] runScriptCommand = new String[] { RUN_TEST_SH_CMD, directory.getPath(), 
    			command, outputStream, errorStream, };
    	    	
        Util.runProcess(runScriptCommand, workingDir);
        
        FileInputStream stdOutStream = new FileInputStream(new File(RUN_TEST_STDOUT));
        FileInputStream stdErrStream = new FileInputStream(new File(RUN_TEST_STDERR));
        
        List<String> outputStreamContent = Util.getInputStreamContent(stdOutStream);
        List<String> errorStreamContent = Util.getInputStreamContent(stdErrStream);
        
        return new Pair<List<String>, List<String>>(outputStreamContent, 
        		errorStreamContent);
	}
	
	/**
	 * Parses test results from output and error streams 
	 * and returns a TestResult.
	 * 
	 * @return a TestResult.
	 */
	protected abstract TestResult getTestResult(List<String> outputStreamContent, 
			List<String> errorStreamContent);

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        AntBuildStrategy buildStrategy = (AntBuildStrategy) other;
        
        return directory.equals(buildStrategy.directory) 
        		&& buildCommand.equals(buildStrategy.buildCommand) 
        		&& testCommand.equals(buildStrategy.testCommand);
	}
	
	@Override
	public int hashCode() {
		return 11 * directory.hashCode() + 13 * buildCommand.hashCode() 
			+ 17 * testCommand.hashCode();
	}

}
