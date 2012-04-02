package common;

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
 * HistoryGraph represents a graph structure of a particular Repository. 
 * 
 * HistoryGraph has access to the Repository it represents. 
 * 
 * HistoryGraph maintain a set of Revision's. It has methods to add 
 * a Revision and to iterate over its set of Revision's. 
 * 
 * HistoryGraph has a method to get a set of Flip's that are present 
 * in its list of Revision's.
 */
public class HistoryGraph implements Iterable<Revision> {
	
    private final Repository repository;
	private final Set<Revision> revisions;
	
	/**
	 * Create an empty HistoryGraph.
	 */
    public HistoryGraph(Repository repository) {
    	this.repository = repository;
        revisions = new HashSet<Revision>();
    }

    /**
     * Add a Revision to this HistoryGraph.
     */
    public void addRevision(Revision revision) {
        revisions.add(revision);
    }
    
    /**
     * @return a Repository this HistoryGraph represents
     */
    public Repository getRepository() {
    	return repository;
    }

    /**
     * @return true if and only if revision_A and revision_B 
     *         are parallel in this HistoryGraph
     */
    public boolean parallel(Revision revision_A, Revision revision_B) {
        return !revision_A.equals(revision_B)
                && !isAncestorOf(revision_A, revision_B)
                && !isAncestorOf(revision_B, revision_A);
    }

    /**
     * @return true if and only if revision_A is an ancestor of revision_B
     */
    public boolean isAncestorOf(Revision revision_A, Revision revision_B) {
        return isAncestorOf(revision_A, revision_B, new HashSet<Revision>());
    }

    /**
     * Helper method
     * 
     * @return true if and only if revision_A is an ancestor of revision_B
     */
    private boolean isAncestorOf(Revision revision_A, Revision revision_B,
            Set<Revision> visited) {
        visited.add(revision_B);

        Set<Revision> parents = revision_B.getParents();
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
     * @return a set of all Flip's in this HistoryGraph
     */
    public Set<Flip> getAllFlips() {
        Set<Flip> flips = new HashSet<Flip>();

        for (Revision revision : revisions) {
            TestResult childResult = revision.getTestResult();
            
            if (childResult == null) { continue; }

            Set<String> allTests = childResult.getAllTests();

            Set<Revision> parents = revision.getParents();

            for (/*@NonNull*/Revision parent : parents) {            	
                TestResult parentResult = parent.getTestResult();
                
                if (parentResult == null) { continue; }
                
                Set<String> toPassTests = null;
                Set<String> toFailTests = null;

                for (String test : allTests) {
                    if (childResult.pass(test) && parentResult.fail(test)) {
                        if (toPassTests == null) {
                        	toPassTests = new HashSet<String>();
                        }
                        toPassTests.add(test);
                    } else if (childResult.fail(test) && parentResult.pass(test)) {
                        if (toFailTests == null) {
                        	toFailTests = new HashSet<String>();
                        }
                        toFailTests.add(test);
                    }
                }

                if (toPassTests != null || toFailTests != null) {
                	if (toPassTests == null) {
                		toPassTests = new HashSet<String>();
                	} else if (toFailTests == null) {
                		toFailTests = new HashSet<String>();
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

        for (Revision revision : revisions) {
        	// find all bug that this revision fixed
            List<String> fixedBugs = new ArrayList<String>();
            Set<Revision> parents = revision.getParents();

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

        return repository.equals(hGraph.repository) 
        		&& revisions.equals(hGraph.revisions);
    }

    @Override
    public int hashCode() {
        return 11 * repository.hashCode() + 13 * revisions.hashCode();
    }

    @Override
    public String toString() {
        String str = repository.toString() + "\n";
        for (Revision revision : revisions) {
            str += revision.toString() + "\n";
        }

        return str;
    }

    @Override
    public Iterator<Revision> iterator() {
        return revisions.iterator();
    }
}
