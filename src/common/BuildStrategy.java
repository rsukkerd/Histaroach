package common;

import common.Revision.COMPILABLE;

/**
 * BuildStrategy is an interface that determines 
 * how to compile a project, run all unit tests, 
 * and parse the test results.
 */
public interface BuildStrategy {
	
	/**
     * Compile and run all unit tests.
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws Exception
     */
	public Pair<COMPILABLE, TestResult> runTest(String commitID) 
			throws Exception;
	
	/**
     * Compile and run all unit tests. 
     * Alternative of runTest(commitID) method.
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws Exception
     */
	public Pair<COMPILABLE, TestResult> runTestViaShellScript(String commitID) 
			throws Exception;

}
