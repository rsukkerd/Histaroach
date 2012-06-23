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
	
	private static final String RUN_COMMAND_SH = "./run_command.sh";
	private static final String RUN_COMMAND_STDOUT = "output/run_command_stdout";
	private static final String RUN_COMMAND_STDERR = "output/run_command_stderr";
	
	private final File directory;
	private final String buildCommand;
	private final String testCommand;
	
	/**
	 * Creates an AntBuildStrategy.
	 * 
	 * @param antCommand - command to run ant.
	 * @param buildTargetName - name of the target(s) to compile source files 
	 *        and test files. If there are multiple targets, each target is 
	 *        separated by a single space.
	 * @param testTargetName - name of the target to run test.
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
		return runTest(testCommand);
	}
	
	/**
	 * Runs arbitrary test(s) using testCommand.
	 * 
	 * @return a TestResult.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected TestResult runTest(String testCommand) throws IOException, 
			InterruptedException {
		Pair<List<String>, List<String>> result = run(testCommand);
		List<String> outputStreamContent = result.getFirst();
		List<String> errorStreamContent = result.getSecond();
		
		return getTestResult(outputStreamContent, errorStreamContent);
	}
	
	/**
	 * Parses test result(s) from output and error streams.
	 * 
	 * @return a TestResult.
	 */
	protected abstract TestResult getTestResult(List<String> outputStreamContent, 
			List<String> errorStreamContent);

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
    	    	
    	String outputStream = workingDir.getPath() + File.separatorChar + RUN_COMMAND_STDOUT;
    	String errorStream = workingDir.getPath() + File.separatorChar + RUN_COMMAND_STDERR;
    	
    	String[] runScriptCommand = new String[] { RUN_COMMAND_SH, directory.getPath(), 
    			command, outputStream, errorStream, };
    	    	
        Util.runProcess(runScriptCommand, workingDir);
        
        FileInputStream stdOutStream = new FileInputStream(new File(RUN_COMMAND_STDOUT));
        FileInputStream stdErrStream = new FileInputStream(new File(RUN_COMMAND_STDERR));
        
        List<String> outputStreamContent = Util.getInputStreamContent(stdOutStream);
        List<String> errorStreamContent = Util.getInputStreamContent(stdErrStream);
        
        return new Pair<List<String>, List<String>>(outputStreamContent, 
        		errorStreamContent);
	}
	
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
