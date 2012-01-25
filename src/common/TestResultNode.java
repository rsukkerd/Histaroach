package common;

/**
 * TestResultNode contains a commit string and a TestResult
 */
public class TestResultNode {
    private final String commit;
    private final TestResult result;

    public TestResultNode(Repository repo, String commit) {
        this.result = repo.getTestResult(commit);
        this.commit = commit;
    }

    public TestResultNode(String commit, TestResult result) {
        this.result = result;
        this.commit = commit;
    }

    /**
     * @return commit string
     */
    public String getCommit() {
        return commit;
    }

    /**
     * @return TestResult
     */
    public TestResult getTestResult() {
        return result;
    }

    /**
     * @return true iff this node passes the test
     */
    public boolean pass(String test) {
        return !result.getFailures().contains(test)
                && result.getAllTests().contains(test);
    }

    /**
     * @return true iff this node fails the test
     */
    public boolean fail(String test) {
        return result.getFailures().contains(test);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        TestResultNode node = (TestResultNode) other;

        return commit.equals(node.commit);
    }

    @Override
    public int hashCode() {
        return commit.hashCode();
    }

    @Override
    public String toString() {
        return "commit : " + commit + "\n" + result.toString();
    }
}
