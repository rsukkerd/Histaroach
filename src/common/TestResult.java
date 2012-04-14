package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * TestResult represents test results of a particular Revision. 
 * 
 * TestResult contains the following public methods: 
 *  - getCommitID(): returns a commit ID of its Revision 
 *  - getAllTests(): returns a set of all tests 
 *  - getFailedTests(): returns a set of failed tests 
 *  - getPassedTests(): returns a set of passed tests 
 *  - pass(test): returns whether its Revision passes a test 
 *  - fail(test): returns whether its Revision fails a test. 
 * 
 * TestResult is immutable.
 */
public class TestResult implements Serializable {
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 2210640650733708242L;
	
	private final String commitID;
    private final /*@Non-Null*/ Set<String> allTests;
    private final /*@Non-Null*/ Set<String> failedTests;
    private final /*@Non-Null*/ Set<String> passedTests;

    /**
     * Create a TestResult.
     * 
     * @precondition allTests and failedTests are Non-Null
     */
    public TestResult(String commitID, /*@Non-Null*/ Set<String> allTests, 
    		/*@Non-Null*/ Set<String> failedTests) {
        this.commitID = commitID;
        this.allTests = allTests;
        this.failedTests = failedTests;
        
        passedTests = new HashSet<String>();
        for (String test : allTests) {
        	if (!failedTests.contains(test)) {
        		passedTests.add(test);
        	}
        }
    }
    
    /**
     * Get a commit ID of its Revision.
     * 
     * @return a commit ID of this TestResult's Revision
     */
    public String getCommitID() {
        return commitID;
    }
    
    /**
     * Get a set of all tests.
     * 
     * @return a set of all tests
     */
    public Set<String> getAllTests() {
        return allTests;
    }

    /**
     * Get a set of failed tests.
     * 
     * @return a set of failed tests
     */
    public Set<String> getFailedTests() {
        return failedTests;
    }
    
    /**
     * Get a set of passed tests.
     * 
     * @return a set of passed tests
     */
    public Set<String> getPassedTests() {
        return passedTests;
    }

    /**
     * Does its Revision pass a test?
     * 
     * @return true if and only if this TestResult's Revision passes the test
     */
    public boolean pass(String test) {
        return passedTests.contains(test);
    }

    /**
     * Does its Revision fail a test?
     * 
     * @return true if and only if this TestResult's Revision fails the test
     */
    public boolean fail(String test) {
        return failedTests.contains(test);
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
