package common;

import java.util.ArrayList;
import java.util.List;

/**
 * BugFix contains
 * 1. a test that got fixed
 * 2. a revision that fixed the test
 * 3. a list of consecutive revisions that failed the test prior to the fix
 */
public class BugFix {
	private final String testName;
    private final Revision passedRevision;
    private final List<Revision> failedRevisions;

	/**
     * @param testName : test name
     * @param passedRevision : revision that fixes the bug
     */
    public BugFix(String testName, Revision passedRevision) {
    	this.testName = testName;
        this.passedRevision = passedRevision;
        failedRevisions = new ArrayList<Revision>();
    }

    public void addFailedRevision(Revision failedRevision) {
        failedRevisions.add(failedRevision);
    }
    
    public String getTestName() {
    	return testName;
    }

    public Revision getPassedRevision() {
	    return passedRevision;
	}

	public List<Revision> getFailedRevisions() {
        return failedRevisions;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        BugFix fix = (BugFix) other;

        return failedRevisions.equals(fix.failedRevisions)
                && passedRevision.equals(fix.passedRevision);
    }

    @Override
    public int hashCode() {
        return 11 * testName.hashCode() + 13 * passedRevision.hashCode() + 17 * failedRevisions.hashCode();
    }

    @Override
    public String toString() {
    	String str = "Test : " + testName + "\n";
        str += "Fixed at : " + passedRevision.getCommitID() + "\n";
        str += "Failed at : \n";

        for (Revision rev : failedRevisions) {
            str += rev.getCommitID() + "\n";
        }
        
        return str;
    }
}
