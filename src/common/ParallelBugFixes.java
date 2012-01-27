package common;

import java.util.Set;

/**
 * ParallelBugFixes contains
 * 1. test name : a bug that got fixed
 * 2. parallel bug fixes : a set of bug fixes that occur in parallel
 */
public class ParallelBugFixes {
    private final String testName;
    private final Set<BugFix> bugFixes;

    public ParallelBugFixes(String testName, Set<BugFix> parallelFixes) {
        this.testName = testName;
        this.bugFixes = parallelFixes;
    }
    
    public String getTestName() {
    	return testName;
    }
    
    public Set<BugFix> getBugFixes() {
    	return bugFixes;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        ParallelBugFixes parallelBugFixes = (ParallelBugFixes) other;

        return testName.equals(parallelBugFixes.testName) && bugFixes.equals(parallelBugFixes.bugFixes);
    }
	
	@Override
	public int hashCode() {
		int code = 11 * testName.hashCode() + 13 * bugFixes.hashCode();
		return code;
	}
	
	@Override
	public String toString() {
		String str = "Test : " + testName + "\n";
		str += "Revisions that fix this bug in parallel :\n";
		
		for (BugFix fix : bugFixes) {
			String commitID = fix.getPassedRevision().getCommitID();
			str += commitID + "\n";
		}

        return str;
	}
}
