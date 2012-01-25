package common;

import java.util.ArrayList;
import java.util.List;

public class BugFix {
    private final List<TestResult> nodeFailList;
    private final TestResult nodePass;

    /**
     * @param nodePass
     *            : node that fixes the bug
     */
    public BugFix(TestResult nodePass) {
        this.nodePass = nodePass;
        nodeFailList = new ArrayList<TestResult>();
    }

    public void addNodeFail(TestResult nodeFail) {
        nodeFailList.add(nodeFail);
    }

    public List<TestResult> getNodeFailList() {
        return nodeFailList;
    }

    public TestResult getNodePass() {
        return nodePass;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        BugFix list = (BugFix) other;

        return nodeFailList.equals(list.nodeFailList)
                && nodePass.equals(list.nodePass);
    }

    @Override
    public int hashCode() {
        return 13 * nodeFailList.hashCode() + 17 * nodePass.hashCode();
    }

    @Override
    public String toString() {
        String str = "Node that fixes the bug: " + nodePass + "\n"
                + "Consecutive nodes that have the bug: ";

        for (int i = 0; i < nodeFailList.size() - 1; i++) {
            str += nodeFailList.get(i) + ",\n";
        }

        str += nodeFailList.get(nodeFailList.size() - 1) + "\n";

        return str;
    }
}
