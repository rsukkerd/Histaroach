package histaroach.buildstrategy;

import histaroach.model.DiffFile;
import histaroach.model.Revision.Compilable;
import histaroach.model.TestResult;

import java.io.IOException;


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
     * Runs all tests and parses the test results.
     * 
     * @return a TestResult.
	 * @throws InterruptedException 
	 * @throws IOException 
     */
	public TestResult runTest() throws IOException, InterruptedException;
	
	/**
	 * Runs a single test.
	 * 
	 * @return true if the Revision passes the test, 
	 *         false if the Revision fails the test.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public boolean runSingleTest(String testname) throws IOException, InterruptedException;
	
	/**
	 * Determines if a file is a test file.
	 * 
	 * @return true if diffFile is a test file.
	 */
	public boolean isTestFile(DiffFile diffFile);

}
