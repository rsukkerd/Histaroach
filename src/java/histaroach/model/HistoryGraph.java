package histaroach.model;

import histaroach.model.Flip.FlipType;
import histaroach.model.Revision.Compilable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * HistoryGraph represents a (partial or full) Revision history 
 * of some Repository. 
 * 
 * The order of Revisions added to a HistoryGraph must be 
 * a topological order from ancestor to descendant. 
 * 
 * HistoryGraph is iterable. The order of Revisions returned by 
 * its iterator is from ancestor to descendant.
 */
public class HistoryGraph implements Iterable<Revision> {
	
	// map: Revision's commit id -> Revision object
	private final Map<String, Revision> revisions;
	// for Revisions' topological ordering in iterator() and toString()
	private final List<Revision> orderedRevisions;
	private Set<Flip> allFlips;
	
	/**
	 * Creates an empty HistoryGraph.
	 */
    public HistoryGraph() {
        revisions = new HashMap<String, Revision>();
        orderedRevisions = new ArrayList<Revision>();
        allFlips = null;
    }

    /**
     * Adds a Revision to this HistoryGraph.
     * 
     * @modifies this
     */
    public void addRevision(Revision revision) {
        revisions.put(revision.getCommitID(), revision);
        orderedRevisions.add(revision);
    }
    
    /**
     * Looks up a Revision by its commitID.
     * 
     * @return a Revision of commitID.
     */
    public Revision lookUpRevision(String commitID) {
    	return revisions.get(commitID);
    }
    
    /**
     * Finds Flips of all types.
     * 
     * @modifies this
	 * @return a set of Flips of all types.
	 */
	public Set<Flip> getAllFlips() {
		if (allFlips != null) {
			return allFlips;
		}
		
	    allFlips = new HashSet<Flip>();
	
	    for (Revision revision : orderedRevisions) {
	    	
	        if (revision.isCompilable() != Compilable.YES ||
	        		revision.hasTestAborted()) {
	        	continue;
	        }
	        	
	        Set<Revision> parents = revision.getParents();
	
	        for (Revision parent : parents) {
	            
	            if (parent.isCompilable() != Compilable.YES ||
	            		parent.hasTestAborted()) {
	            	continue;
	            }
	            
	            Set<String> toPassTests = new HashSet<String>();
	            Set<String> toFailTests = new HashSet<String>();
	
	            findFlippedTests(parent, revision, toPassTests, toFailTests);
	            
	            if (!toPassTests.isEmpty() || !toFailTests.isEmpty()) {
	                Flip flip = new Flip(parent, revision, toPassTests, toFailTests);
	                allFlips.add(flip);
	            }
	        }
	    }
	
	    return allFlips;
	}
	
	/**
	 * Finds only Flips of types TO_FAIL and BOTH.
	 * 
	 * @modifies this
	 * @return a set of Flips of types TO_FAIL and BOTH.
	 */
	public Set<Flip> getToFailFlips() {
		if (allFlips == null) {
			getAllFlips();
		}
		
		Set<Flip> toFailFlips = new HashSet<Flip>();
		
		for (Flip flip : allFlips) {
			if (flip.getFlipType() == FlipType.TO_FAIL
					|| flip.getFlipType() == FlipType.BOTH) {
				toFailFlips.add(flip);
			}
		}
		
		return toFailFlips;
	}
	
	/**
	 * Find all flipped tests between parent and child Revisions.
	 * 
	 * @modifies toPassTests, toFailTests
	 */
	private void findFlippedTests(Revision parent, Revision child, 
			Set<String> toPassTests, Set<String> toFailTests) {
		
		TestResult childResult = child.getTestResult();
		TestResult parentResult = parent.getTestResult();
		
		Set<String> allTests = childResult.getAllTests();
		
        for (String test : allTests) {
            if (childResult.pass(test) && parentResult.fail(test)) {
                toPassTests.add(test);
            } else if (childResult.fail(test) && parentResult.pass(test)) {
                toFailTests.add(test);
            }
        }
	}

	@Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        HistoryGraph hGraph = (HistoryGraph) other;

        return revisions.equals(hGraph.revisions);
    }

    @Override
    public int hashCode() {
        return revisions.hashCode();
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
