package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * HistoryGraph represents a graph structure of a particular repository.
 */
public class HistoryGraph implements Iterable<Revision>, Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = 7286435306324502773L;

    /** mapping : a revision's commit id -> that revision **/
    private final Map<String, Revision> map;

    /** revisions' order according to 'git log' **/
    private final List<Revision> orderedRevisions;

    public HistoryGraph() {
        map = new HashMap<String, Revision>();
        orderedRevisions = new ArrayList<Revision>();
    }

    /**
     * add a revision to this history graph
     */
    public void addRevision(Revision revision) {
        map.put(revision.getCommitID(), revision);
        orderedRevisions.add(revision);
    }

    /**
     * @return true iff revision_A and revision_B are parallel in this history
     *         graph
     */
    public boolean parallel(Revision revision_A, Revision revision_B) {
        return !revision_A.equals(revision_B)
                && !isAncestorOf(revision_A, revision_B)
                && !isAncestorOf(revision_B, revision_A);
    }

    /**
     * @return true iff revision_A is an ancestor of revision_B
     */
    public boolean isAncestorOf(Revision revision_A, Revision revision_B) {
        return isAncestorOf(revision_A, revision_B, new HashSet<Revision>());
    }

    /**
     * Helper method
     * 
     * @return true iff revision_A is an ancestor of revision_B
     */
    private boolean isAncestorOf(Revision revision_A, Revision revision_B,
            Set<Revision> visited) {
        visited.add(revision_B);

        List<Revision> parents = revision_B.getParents();
        for (Revision parent : parents) {
            if (parent.equals(revision_A)) {
                return true;
            }
        }

        for (Revision parent : parents) {
            if (!visited.contains(parent)) {
                if (isAncestorOf(revision_A, parent, visited)) {
                    return true;
                }
            }
        }

        return false;
    }
    
    /**
     * @return a set of all flips in this historyGraph
     */
    public Set<Flip> getAllFlips() {
        Set<Flip> flips = new HashSet<Flip>();

        for (Revision revision : orderedRevisions) {
            TestResult childResult = revision.getTestResult();
            
            if (childResult == null) { continue; }

            Set<String> allTests = childResult.getAllTests();

            List<Revision> parents = revision.getParents();

            for (Revision parent : parents) {
            	if (parent == null) { continue; } // when build partial HistoryGraph
            	
                TestResult parentResult = parent.getTestResult();
                
                if (parentResult == null) { continue; }
                
                List<String> toPassTests = null;
                List<String> toFailTests = null;

                for (String test : allTests) {
                    if (childResult.pass(test) && parentResult.fail(test)) {
                        if (toPassTests == null) {
                        	toPassTests = new ArrayList<String>();
                        }
                        toPassTests.add(test);
                    } else if (childResult.fail(test) && parentResult.pass(test)) {
                        if (toFailTests == null) {
                        	toFailTests = new ArrayList<String>();
                        }
                        toFailTests.add(test);
                    }
                }

                if (toPassTests != null || toFailTests != null) {
                	if (toPassTests == null) {
                		toPassTests = new ArrayList<String>();
                	} else if (toFailTests == null) {
                		toFailTests = new ArrayList<String>();
                	}
                	
	                Flip flip = new Flip(revision, parent, toPassTests, toFailTests);
	                flips.add(flip);
                }
            }
        }

        return flips;
    }
    
    /**
     * @return a ParallelBugFixes instance of a given bug
     */
    public ParallelBugFixes getParallelBugFixes(String bug, List<BugFix> allFixes) {
        Set<BugFix> parallelFixes = new HashSet<BugFix>();

        for (int i = 0; i < allFixes.size() - 1; i++) {
            for (int j = i + 1; j < allFixes.size(); j++) {
                BugFix fix_A = allFixes.get(i);
                BugFix fix_B = allFixes.get(j);

                Revision revision_A = fix_A.getPassedRevision();
                Revision revision_B = fix_B.getPassedRevision();

                if (parallel(revision_A, revision_B)) {
                    parallelFixes.add(fix_A);
                    parallelFixes.add(fix_B);
                }
            }
        }

        return new ParallelBugFixes(bug, parallelFixes);
    }
    
    /**
     * @return a mapping : a test name (bug) -> a list of all its fixes
     */
    public Map<String, List<BugFix>> getAllBugFixes() {
        Map<String, List<BugFix>> res = new HashMap<String, List<BugFix>>();

        for (Revision revision : orderedRevisions) {
        	// find all bug that this revision fixed
            List<String> fixedBugs = new ArrayList<String>();
            List<Revision> parents = revision.getParents();

            for (Revision parent : parents) {
                for (String failedTest : parent.getTestResult()
                        .getFailedTests()) {
                    if (revision.getTestResult().pass(failedTest)) {
                        fixedBugs.add(failedTest);
                    }
                }
            }

            for (String test : fixedBugs) {
                // BFS to find all consecutive revisions, start from this
                // revision, that fail this test
                Queue<Revision> queue = new LinkedList<Revision>();
                BugFix bugFix = new BugFix(test, revision);

                Set<Revision> visited = new HashSet<Revision>();

                for (Revision parent : parents) {
                    if (parent.getTestResult().fail(test)) {
                        queue.add(parent);
                        bugFix.addFailedRevision(parent);
                        visited.add(parent);
                    }
                }

                while (!queue.isEmpty()) {
                    Revision nextRevision = queue.poll();

                    for (Revision parent : nextRevision.getParents()) {
                        if (!visited.contains(parent)
                                && parent.getTestResult().fail(test)) {
                            queue.add(parent);
                            bugFix.addFailedRevision(parent);
                            visited.add(parent);
                        }
                    }
                }

                if (res.containsKey(test)) {
                    res.get(test).add(bugFix);
                } else {
                    List<BugFix> list = new ArrayList<BugFix>();
                    list.add(bugFix);
                    res.put(test, list);
                }
            }
        }

        return res;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        HistoryGraph hGraph = (HistoryGraph) other;

        return map.equals(hGraph.map)
                && orderedRevisions.equals(hGraph.orderedRevisions);
    }

    @Override
    public int hashCode() {
        return 11 * map.hashCode() + 13 * orderedRevisions.hashCode();
    }

    @Override
    public String toString() {
        String str = "";
        for (Revision revision : orderedRevisions) {
            str += revision.toString() + "\n";
        }

        return str;
    }

    @Override
    public Iterator<Revision> iterator() {
        return orderedRevisions.iterator();
    }
}
