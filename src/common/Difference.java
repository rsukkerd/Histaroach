package common;

import java.util.List;

public class Difference {
    private final TestResult child;
    private final TestResult parent;
    private final List<String> changedFiles;

    public Difference(TestResult child, TestResult parent,
            List<String> changedFiles) {
        this.child = child;
        this.parent = parent;
        this.changedFiles = changedFiles;
    }

    public TestResult getChild() {
        return child;
    }

    public TestResult getParent() {
        return parent;
    }

    public List<String> getChangedFiles() {
        return changedFiles;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Difference diff = (Difference) other;

        return child.equals(diff.child) && parent.equals(diff.parent);
    }

    @Override
    public int hashCode() {
        return 13 * child.hashCode() + 17 * parent.hashCode();
    }

    @Override
    public String toString() {
        String result = "Child commit: " + child.getCommit() + "\n"
                + "Parent commit: " + parent.getCommit() + "\n";
        result += "Diff files:\n";
        for (String file : changedFiles) {
            result += file + "\n";
        }

        return result;
    }
}
