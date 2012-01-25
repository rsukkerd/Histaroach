package common;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * TestResult contains 2 lists of names of the tests: list of all tests and list
 * of failed tests
 */
public class TestResult {
    private final Set<String> allTests;
    private final Set<String> failures;
    private final String commit;

    public static TestResult fromRepo(Repository repo, String commit) {
        return repo.getTestResult(commit);
    }

    public TestResult(String commit) {
        this.allTests = new LinkedHashSet<String>();
        this.failures = new LinkedHashSet<String>();
        this.commit = commit;
    }

    public TestResult(String commit, Set<String> allTests, Set<String> failures) {
        this.commit = commit;
        this.allTests = allTests;
        this.failures = failures;
    }

    /**************************************************/

    /**
     * @return true iff this node is neither ancestor or descendant of node_B
     */
    public boolean isParallelWith(HistoryGraph historyGraph, TestResult node_B) {
        return !this.equals(node_B) && !this.isAncestorOf(historyGraph, node_B)
                && !this.isAncestorOf(historyGraph, node_B);
    }

    /**
     * @return true iff this node is an ancestor of node_B
     */
    public boolean isAncestorOf(HistoryGraph historyGraph, TestResult node_B) {
        return isAncestorOf(historyGraph, this, node_B,
                new HashSet<TestResult>());

    }

    /**
     * @return true iff node_A is an ancestor of node_B
     */
    static private boolean isAncestorOf(HistoryGraph historyGraph,
            TestResult node_A, TestResult node_B, Set<TestResult> visited) {
        visited.add(node_A);

        List<TestResult> parents = historyGraph.getParents(node_A);
        for (TestResult parent : parents) {
            if (parent.equals(node_B)) {
                return true;
            }
        }

        for (TestResult parent : parents) {
            if (!visited.contains(parent)) {
                if (isAncestorOf(historyGraph, parent, node_B, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    /*************************************************/

    /**
     * @return commit string
     */
    public String getCommit() {
        return commit;
    }

    public void addFailedTest(String test) {
        failures.add(test);
    }

    public void addTest(String test) {
        allTests.add(test);
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
    public Set<String> getFailures() {
        return failures;
    }

    /**
     * @return true iff this node passes the test
     */
    public boolean pass(String test) {
        return !this.getFailures().contains(test)
                && this.getAllTests().contains(test);
    }

    /**
     * @return true iff this node fails the test
     */
    public boolean fail(String test) {
        return this.getFailures().contains(test);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        TestResult result = (TestResult) other;

        return commit.equals(result.commit) && allTests.equals(result.allTests)
                && failures.equals(result.failures);
    }

    @Override
    public int hashCode() {
        int code;
        code = 11 * commit.hashCode();
        if (allTests != null) {
            code += (13 * allTests.hashCode());
        }

        if (failures != null) {
            code += (17 * failures.hashCode());
        }
        return code;
    }

    @Override
    public String toString() {
        String result = "commit : " + commit + "\n";
        result += "Tests: \n";
        for (String test : allTests) {
            result += test + "\n";
        }

        result += "Failures: \n";
        for (String fail : failures) {
            result += fail + "\n";
        }

        return result;
    }
}
