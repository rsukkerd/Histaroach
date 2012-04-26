package common;

import util.Pair;
import common.Revision.Compilable;

/**
 * BuildStrategy represents a way to compile a project, 
 * run tests, and parse the test results. 
 * 
 * BuildStrateggy contains the following public methods: 
 *  - runTest(): compiles the project, runs tests, and 
 *    parses the test results 
 *  - runTestViaShellScript(): an alternative method 
 *    of runTest.
 */
public interface BuildStrategy {
	
	/**
     * Compiles the project, runs tests, and 
     * parses the test results.
     * 
     * @return a pair of Compilable state and TestResult.
     * @throws Exception
     */
	public Pair<Compilable, TestResult> runTest() throws Exception;
	
	/**
     * Compiles the project, runs tests, and 
     * parses the test results. 
     * 
     * This method is an alternative of runTest().
     * 
     * @return a pair of Compilable state and TestResult.
     * @throws Exception
     */
	public Pair<Compilable, TestResult> runTestViaShellScript() throws Exception;

}
