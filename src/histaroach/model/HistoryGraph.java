package histaroach.model;

import histaroach.model.Revision.Compilable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	
	private final Set<Revision> revisions;
	// for Revisions' topological ordering in iterator() and toString()
	private final List<Revision> orderedRevisions;
	
	/**
	 * Creates an empty HistoryGraph.
	 */
    public HistoryGraph() {
        revisions = new HashSet<Revision>();
        orderedRevisions = new ArrayList<Revision>();
    }

    /**
     * Adds a Revision to this HistoryGraph.
     * 
     * @modifies this
     */
    public void addRevision(Revision revision) {
        revisions.add(revision);
        orderedRevisions.add(revision);
    }
    
    /**
	 * @return a set of all Flips in this HistoryGraph.
	 */
	public Set<Flip> getAllFlips() {
	    Set<Flip> flips = new HashSet<Flip>();
	
	    for (Revision revision : revisions) {
	    	
	        if (revision.isCompilable() != Compilable.YES) {
	        	continue;
	        }
	        	
	        Set<Revision> parents = revision.getParents();
	
	        for (Revision parent : parents) {
	            
	            if (parent.isCompilable() != Compilable.YES) {
	            	continue;
	            }
	            
	            Set<String> toPassTests = new HashSet<String>();
	            Set<String> toFailTests = new HashSet<String>();
	
	            findFlippedTests(revision, parent, toPassTests, toFailTests);
	            
	            if (!toPassTests.isEmpty() || !toFailTests.isEmpty()) {
	                Flip flip = new Flip(revision, parent, toPassTests, toFailTests);
	                flips.add(flip);
	            }
	        }
	    }
	
	    return flips;
	}
	
	/**
	 * Find all flipped tests between child and parent Revisions.
	 * 
	 * @modifies toPassTests, toFailTests
	 */
	private void findFlippedTests(Revision child, Revision parent, 
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
