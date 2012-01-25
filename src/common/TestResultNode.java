package common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TestResultNode contains a commit string and a TestResult
 */
public class TestResultNode {
    private final String commit;
    private final TestResult result;

    /**
     * @return true iff node_A is neither ancestor or descendant of node_B
     */
    static boolean areParallel(HistoryGraph historyGraph,
            TestResultNode node_A, TestResultNode node_B) {
        return !node_A.equals(node_B)
                && !TestResultNode.isAncestor(historyGraph, node_A, node_B,
                        new HashSet<TestResultNode>())
                && !TestResultNode.isAncestor(historyGraph, node_B, node_A,
                        new HashSet<TestResultNode>());
    }

    /**
     * @return true iff node_B is an ancestor of node_A
     */
    static boolean isAncestor(HistoryGraph historyGraph, TestResultNode node_A,
            TestResultNode node_B, Set<TestResultNode> visited) {
        visited.add(node_A);

        List<TestResultNode> parents = historyGraph.getParents(node_A);
        for (TestResultNode parent : parents) {
            if (parent.equals(node_B)) {
                return true;
            }
        }

        for (TestResultNode parent : parents) {
            if (!visited.contains(parent)) {
                if (isAncestor(historyGraph, parent, node_B, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

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
