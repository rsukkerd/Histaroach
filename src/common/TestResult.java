package common;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * TestResult has a commit id and contains 2 sets of tests: 
 * a set of all tests and a set of failed tests.
 */
public class TestResult {
	private final String commitID;
	private boolean compilable;
    private final Set<String> allTests;
    private final Set<String> failedTests;

    public TestResult(String commit) {
    	this.commitID = commit;
    	this.compilable = false;
        this.allTests = new LinkedHashSet<String>();
        this.failedTests = new LinkedHashSet<String>();
    }

    public TestResult(String commit, boolean compiled, Set<String> allTests, Set<String> failedTests) {
        this.commitID = commit;
        this.compilable = compiled;
        this.allTests = allTests;
        this.failedTests = failedTests;
    }

    /**
     * @return commit string
     */
    public String getCommitID() {
        return commitID;
    }
    
    public void setCompiledFlag(boolean compiled) {
    	this.compilable = compiled;
    }
    
    public boolean compiled() {
    	return compilable;
    }

    public void addTest(String test) {
	    allTests.add(test);
	}

	public void addFailedTest(String test) {
        failedTests.add(test);
    }

    /**
     * @return list of names of all tests
     */
    public Set<String> getAllTests() {
        return allTests;
    }

    /**
     * @return list of names of failed tests
     */
    public Set<String> getFailedTests() {
        return failedTests;
    }

    /**
     * @return true iff this node passes the test
     */
    public boolean pass(String test) {
        return !this.getFailedTests().contains(test)
                && this.getAllTests().contains(test);
    }

    /**
     * @return true iff this node fails the test
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

        return commitID.equals(result.commitID) && compilable == result.compilable 
        		&&allTests.equals(result.allTests) && failedTests.equals(result.failedTests);
    }

    @Override
    public int hashCode() {
        int code = 11 * commitID.hashCode();
        if (allTests != null) {
            code += (13 * allTests.hashCode());
        }

        if (failedTests != null) {
            code += (17 * failedTests.hashCode());
        }
        
        if (compilable) return 7 + code; return code;
    }

    @Override
    public String toString() {
        String result = "compiled? : ";
        if (compilable) {
        	result += "yes\n";
        	result += "Tests: \n";
            for (String test : allTests) {
                result += test + "\n";
            }

            result += "Failed Tests: \n";
            for (String fail : failedTests) {
                result += fail + "\n";
            }
        } else {
        	result += "no\n";
        }

        return result;
    }
}
