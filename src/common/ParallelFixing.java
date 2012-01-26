package common;

import java.util.Set;

public class ParallelFixing {
    private final String testName;
    private final Set<BugFix> parallelBugFixes;

    public ParallelFixing(String testName, Set<BugFix> parallelFixes) {
        this.testName = testName;
        this.parallelBugFixes = parallelFixes;
    }
    
    public String getTestName() {
    	return testName;
    }
    
    public Set<BugFix> getParallelFixes() {
    	return parallelBugFixes;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        ParallelFixing parallelFixing = (ParallelFixing) other;

        return testName.equals(parallelFixing.testName) && parallelBugFixes.equals(parallelFixing.parallelBugFixes);
    }
	
	@Override
	public int hashCode() {
		int code = 11 * testName.hashCode() + 13 * parallelBugFixes.hashCode();
		return code;
	}
	
	@Override
	public String toString() {
		String str = "Test : " + testName + "\n";
		str += "Revisions that fix this bug in parallel :\n";
		
		for (BugFix fix : parallelBugFixes) {
			String commitID = fix.getPassedRevision().getCommitID();
			str += commitID + "\n";
		}

        return str;
	}
}
