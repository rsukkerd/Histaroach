package histaroach.buildstrategy;

import java.io.IOException;

import histaroach.model.TestResult;
import histaroach.model.Revision.Compilable;


/**
 * BuildStrategy represents a way to compile a project, 
 * run tests, and parse the test results.
 */
public interface IBuildStrategy {
	
	/**
	 * Compiles source files and test files of the project.
	 * 
	 * @return a Compilable state.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public Compilable build() throws IOException, InterruptedException;
	
	/**
     * Runs tests and parses the test results.
     * 
     * @return a TestResult.
	 * @throws InterruptedException 
	 * @throws IOException 
     */
	public TestResult runTest() throws IOException, InterruptedException;

}
