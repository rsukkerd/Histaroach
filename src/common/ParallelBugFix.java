package common;

import java.util.Set;

public class ParallelBugFix {
    String testName;
    Set<BugFix> parallelFixes;

    public ParallelBugFix(String testName, Set<BugFix> parallelFixes) {
        this.testName = testName;
        this.parallelFixes = parallelFixes;
    }
}
