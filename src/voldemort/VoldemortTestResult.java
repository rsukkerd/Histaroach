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
                Pattern allTestsPattern = Pattern
                        .compile("\\s*\\[junit\\] Running (\\S+)");
                Matcher allTestsMatcher = allTestsPattern.matcher(line);

                if (allTestsMatcher.find()) {
                    this.addTest(allTestsMatcher.group(1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            while ((line = stdErrorReader.readLine()) != null) {
                Pattern failuresPattern = Pattern
                        .compile("\\s*\\[junit\\] Test (\\S+) FAILED");
                Matcher failuresMatcher = failuresPattern.matcher(line);

                if (failuresMatcher.find()) {
                    this.addFailedTest(failuresMatcher.group(1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
}
