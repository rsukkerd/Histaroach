package common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * TestResult represents test results of a Revision. 
 * 
 * TestResult contains the following public methods: 
 *  - getAllTests(): returns a set of all tests 
 *  - getFailedTests(): returns a set of failed tests 
 *  - getPassedTests(): returns a set of passed tests 
 *  - pass(test): returns whether a test passes
 *  - fail(test): returns whether a test fails.
 * 
 * TestResult is immutable.
 */
public class TestResult implements Serializable {
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 2210640650733708242L;
	
    private final /*@Non-Null*/ Set<String> allTests;
    private final /*@Non-Null*/ Set<String> failedTests;
    private final /*@Non-Null*/ Set<String> passedTests;

    /**
     * Creates a TestResult.
     * 
     * @precondition allTests and failedTests are Non-Null.
     */
    public TestResult(/*@Non-Null*/ Set<String> allTests, 
    		/*@Non-Null*/ Set<String> failedTests) {
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
     * Returns a set of all tests.
     * 
     * @return a set of all tests.
     */
    public Set<String> getAllTests() {
        return allTests;
    }

    /**
     * Returns a set of failed tests.
     * 
     * @return a set of failed tests.
     */
    public Set<String> getFailedTests() {
        return failedTests;
    }
    
    /**
     * Returns a set of passed tests.
     * 
     * @return a set of passed tests.
     */
    public Set<String> getPassedTests() {
        return passedTests;
    }

    /**
     * Returns true if a test passes.
     * 
     * @return true if the test passes.
     */
    public boolean pass(String test) {
        return passedTests.contains(test);
    }

    /**
     * Returns true if a test fails.
     * 
     * @return true if the test fails.
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

        return allTests.equals(result.allTests) && failedTests.equals(result.failedTests);
    }

    @Override
    public int hashCode() {
        int code = 13 * allTests.hashCode() + 17 * failedTests.hashCode();
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
