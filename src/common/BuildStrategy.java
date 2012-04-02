package common;

import java.io.IOException;
import java.util.List;

import common.Revision.COMPILABLE;

/**
 * BuildStrategy determines how to compile a project, 
 * run unit tests, and parse the test results. 
 * 
 * BuildStrategy is immutable.
 */
public interface BuildStrategy {
	
	/**
     * Compile and run all unit tests.
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws InterruptedException 
     * @throws IOException 
     */
	public Pair<COMPILABLE, TestResult> runTest(String commitID) 
			throws IOException, InterruptedException;
	
	/**
     * Compile and run all unit tests on this MixedRevision. 
     * (Alternative of runTest() method.)
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws Exception
     */
	public Pair<COMPILABLE, TestResult> runTestViaShellScript(String commitID) 
			throws Exception;
	
	/**
     * @return YES if build successful, NO if build failed, 
     *         and NO_BUILD_FILE if there is no build file
     */
	public COMPILABLE buildSuccessful(List<String> outputStreamContent,
            List<String> errorStreamContent);
	
	/**
	 * @return a TestResult of the commit
	 */
	public TestResult getTestResult(String commitID, List<String> outputStreamContent, 
			List<String> errorStreamContent);
}
