package histaroach.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * HistoryGraph represents a graph of Revision history. 
 * 
 * HistoryGraph contains the following public methods: 
 *  - addRevision(revision): adds a Revision 
 *  - getAllFlips(): returns a set of all Flips occurred. 
 *    
 * HistoryGraph is iterable. The order of Revisions returned by 
 * an iterator is from ancestor to descendant.
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
     * Returns a set of all Flips occurred.
     * 
	 * @return a set of all Flips in this HistoryGraph.
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
