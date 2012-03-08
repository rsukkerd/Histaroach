package common;

import java.io.Serializable;
import java.util.Set;

/**
 * TestResult represents junit test results of a particular revision.
 * TestResult has a commit id and contains 2 sets of tests: 
 * a set of all tests and a set of failed tests. 
 * TestResult is immutable.
 */
public class TestResult implements Serializable {
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 2210640650733708242L;
	
	private final String commitID;
    private /*@Non-Null*/ Set<String> allTests;
    private /*@Non-Null*/ Set<String> failedTests;

    /**
     * Create a TestResult instance
     * precondition : allTests and failedTests are Non-Null
     */
    public TestResult(String commitID, /*@Non-Null*/ Set<String> allTests, /*@Non-Null*/ Set<String> failedTests) {
        this.commitID = commitID;
        this.allTests = allTests;
        this.failedTests = failedTests;
    }
    
    /**
     * @return commit id of the revision associated with this TestResult
     */
    public String getCommitID() {
        return commitID;
    }
    
    /**
     * @return a set of all tests
     */
    public Set<String> getAllTests() {
        return allTests;
    }

    /**
     * @return a set of failed tests
     */
    public Set<String> getFailedTests() {
        return failedTests;
    }

    /**
     * @return true iff this revision passes the test
     */
    public boolean pass(String test) {
        return !this.getFailedTests().contains(test) && this.getAllTests().contains(test);
    }

    /**
     * @return true iff this revision fails the test
     */
    public boolean fail(String test) {
        return this.getFailedTests().contains(test);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        TestResult result = (TestResult) other;

        return commitID.equals(result.commitID) 
        	&& allTests.equals(result.allTests) && failedTests.equals(result.failedTests);
    }

    @Override
    public int hashCode() {
        int code = 11 * commitID.hashCode() + 13 * allTests.hashCode() 
        		+ 17 * failedTests.hashCode();
        return code;
    }

    @Override
    public String toString() {
		String result = "Tests: \n";
        for (String test : allTests) {
            result += test + "\n";
        }

        result += "Failed Tests: \n";
        for (String fail : failedTests) {
            result += fail + "\n";
        }

        return result;
    }
}
