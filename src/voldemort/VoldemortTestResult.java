package voldemort;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import common.TestResult;

public class VoldemortTestResult extends TestResult {
    /**
     * @return TestResult from output from process
     */
    public VoldemortTestResult(String commitID, BufferedReader stdOutputReader,
            BufferedReader stdErrorReader) {
        super(commitID);

        String line = new String();

        try {
            while ((line = stdOutputReader.readLine()) != null) {
            	Pattern buildSuccessfulPattern = Pattern.compile("BUILD SUCCESSFUL");
            	Matcher buildSuccessfulMatcher = buildSuccessfulPattern.matcher(line);
            	
            	if (buildSuccessfulMatcher.find()) {
            		this.setCompiledFlag(true);
            	}
            	
                Pattern testPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
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
            	Pattern buildFailedPattern = Pattern.compile("BUILD FAILED");
            	Matcher buildFailedMatcher = buildFailedPattern.matcher(line);
            	
            	if (buildFailedMatcher.find()) {
            		this.setCompiledFlag(false);
            		break;
            	}
            	
                Pattern failedTestPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
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
