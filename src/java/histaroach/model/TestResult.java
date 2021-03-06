package histaroach.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;


/**
 * TestResult represents results of all tests run on a Revision. 
 * 
 * TestResult is immutable.
 */
public class TestResult implements Serializable {
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 2210640650733708242L;
	
	private static final String TRUE = "1";
	private static final String FALSE = "0";
	private static final String NEITHER = "-1";
	
    private final Set<String> allTests;
    private final Set<String> failedTests;

    /**
     * Creates a TestResult.
     * 
     * @requires failedTests is a subset of allTests.
     */
    public TestResult(Set<String> allTests, Set<String> failedTests) {
        this.allTests = allTests;
        this.failedTests = failedTests;
    }
    
    public Set<String> getAllTests() {
        return allTests;
    }
    
    public Set<String> getFailedTests() {
        return failedTests;
    }
    
    public boolean pass(String test) {
        return allTests.contains(test) && !failedTests.contains(test);
    }
    
    public boolean fail(String test) {
        return failedTests.contains(test);
    }
    
    /**
     * Encode the result of test as a String.
     * 
     * @return a String encoded the result of test: 
     *         1 means pass, 0 means fail, and -1 means no result.
     */
    public String encodeAsString(String test) {
    	if (!allTests.contains(test)) {
    		return NEITHER;
    	}
    	
    	return failedTests.contains(test) ? FALSE : TRUE;
    }
    
    /**
     * Finds difference between this set of tests and other set of tests.
     * 
     * @return a set of different tests between this and other.
     */
    public Set<String> diff(TestResult other) {
    	Set<String> diff = new HashSet<String>();
    	
    	for (String test : this.allTests) {
    		if (!other.allTests.contains(test)) {
    			diff.add(test);
    		}
    	}
    	
    	for (String test : other.allTests) {
    		if (!this.allTests.contains(test)) {
    			diff.add(test);
    		}
    	}
    	
    	return diff;
    }
    
    /**
     * Finds nondeterministic tests across the 2 TestResults that 
     * belong to the same Revision.
     * 
     * @return a set of nondeterministic tests across the 2 TestResults.
     * @throws Exception if other has a different set of tests.
     */
    public Set<String> getNondeterministicTests(TestResult other) 
    		throws Exception {
    	if (!this.allTests.equals(other.allTests)) {
    		throw new Exception("other has a different set of tests");
    	}
    	
    	Set<String> nondeterministic = new HashSet<String>();
    	
    	for (String test : this.failedTests) {
    		if (!other.failedTests.contains(test)) {
    			nondeterministic.add(test);
    		}
    	}
    	
    	for (String test : other.failedTests) {
    		if (!this.failedTests.contains(test)) {
    			nondeterministic.add(test);
    		}
    	}
    	
    	return nondeterministic;
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
