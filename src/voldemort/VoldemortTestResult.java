package voldemort;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestResult;

public class VoldemortTestResult extends TestResult {
	
    /**
	 * serial version ID
	 */
	private static final long serialVersionUID = 5271469198946606556L;

	/**
     * parse junit test results from standard output and standard error streams
     * @return TestResult instance of a given commit
     */
    public VoldemortTestResult(String commitID, BufferedReader stdOutputReader, BufferedReader stdErrorReader) {
        super(commitID);
        
    	Pattern testPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
    	Pattern failedTestPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
    	
        String line = new String();
        try {
            while ((line = stdOutputReader.readLine()) != null) {
                Matcher testMatcher = testPattern.matcher(line);
                if (testMatcher.find()) {
                    this.addTest(testMatcher.group(1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        
        try {
            while ((line = stdErrorReader.readLine()) != null) {
            	Matcher failedTestMatcher = failedTestPattern.matcher(line);
                if (failedTestMatcher.find()) {
                    this.addFailedTest(failedTestMatcher.group(1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
