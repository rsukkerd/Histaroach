package common;

import common.Revision.Compilable;

/**
 * BuildStrategy represents a way to compile a project, 
 * run tests, and parse the test results. 
 * 
 * BuildStrateggy contains the following public methods: 
 *  - runTest(commitID): compiles, runs tests, and 
 *    parses the test results of a commit 
 *  - runTestViaShellScript(commitID): an alternative 
 *    method of runTest.
 */
public interface BuildStrategy {
	
	/**
     * Compile, run tests, and parse the test results of a commit.
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws Exception
     */
	public Pair<Compilable, TestResult> runTest(String commitID) 
			throws Exception;
	
	/**
     * Compile, run tests, and parse the test results of a commit.
     * 
     * Alternative of runTest.
     * 
     * @return a pair of COMPILABLE state and TestResult of the commit
     * @throws Exception
     */
	public Pair<Compilable, TestResult> runTestViaShellScript(String commitID) 
			throws Exception;

}
