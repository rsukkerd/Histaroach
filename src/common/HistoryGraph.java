package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Flip.FlipType;

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
     * @return a set of parent revisions of a given child revision
     */
    public Set<Revision> getParents(Revision child) {

        // TODO: This is somewhat round-about. Why can't a Revision know about
        // the actual parent Revisions, instead of the string IDs?

        Set<String> parentIDs = child.getParentIDs();
        Set<Revision> parents = new HashSet<Revision>();

        for (String parentID : parentIDs) {
            Revision parent = map.get(parentID);
            parents.add(parent);
        }

        return parents;
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

        Set<Revision> parents = getParents(revision_B);
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

            // TODO: A revision should know its parents. You shouldn't need
            // to use the history graph for this.

            Set<Revision> parents = getParents(revision);

            for (Revision parent : parents) {
                TestResult parentResult = parent.getTestResult();
                
                if (parentResult == null) { continue; }

                Map<String, FlipType> testToFlipType = null;

                for (String test : allTests) {
                    if (childResult.pass(test) && parentResult.fail(test)) {
                    	if (testToFlipType == null) {
                    		testToFlipType = new HashMap<String, Flip.FlipType>();
                    	}
                        testToFlipType.put(test, FlipType.TO_FIX);
                    } else if (childResult.fail(test) && parentResult.pass(test)) {
                    	if (testToFlipType == null) {
                    		testToFlipType = new HashMap<String, Flip.FlipType>();
                    	}
                        testToFlipType.put(test, FlipType.TO_FAIL);
                    }
                }

                if (testToFlipType != null) {
	                Flip flip = new Flip(revision, parent, testToFlipType);
	                flips.add(flip);
                }
            }
        }

        return flips;
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
