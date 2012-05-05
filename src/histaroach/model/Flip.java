package histaroach.model;

import java.util.List;
import java.util.Set;


/**
 * Flip represents a pair of child-parent Revisions that contain 
 * some tests which one Revision passes and the other fails. 
 * 
 * Flip has access to its child parent Revisions. 
 * It contains the following public methods: 
 *  - getChildRevision(): returns a child Revision 
 *  - getParentRevision(): returns a parent Revision 
 *  - getDiffFiles(): returns a list of DiffFiles between 
 *    child and parent 
 *  - getToPassTests(): returns a set of tests that fail in 
 *    parent but pass in child 
 *  - getToFailTests(): returns a set of tests that pass in 
 *    parent but fail in child 
 * 
 * Flip is comparable. A Flip that has fewer DiffFiles is less 
 * than a Flip that has more DiffFiles.
 * 
 * Flip is immutable.
 */
public class Flip implements Comparable<Flip> {
	public enum FlipType {
		TO_PASS,
		TO_FAIL
	}

	private final Revision child;
	private final Revision parent;
	// set of tests that flip from fail to pass
	private final /*@Non-Null*/ Set<String> toPassTests;
	// set of tests that flip from pass to fail
	private final /*@Non-Null*/ Set<String> toFailTests;
	
	/**
	 * Creates a Flip.
	 * 
	 * @precondition toPassTests and toFailTests are Non-Null.
	 */
	public Flip(Revision child, Revision parent, /*@Non-Null*/ Set<String> toPassTests,
			/*@Non-Null*/ Set<String> toFailTests) {
		this.child = child;
		this.parent = parent;
		this.toPassTests = toPassTests;
		this.toFailTests = toFailTests;
	}
	
	/**
	 * Returns a child Revision.
	 * 
	 * @return a child Revision of this Flip.
	 */
	public Revision getChildRevision() {
		return child;
	}
	
	/**
	 * Returns a parent Revision.
	 * 
	 * @return a parent Revision of this Flip.
	 */
	public Revision getParentRevision() {
		return parent;
	}
	
	/**
	 * Returns a list of DiffFiles between child and parent.
	 * 
	 * @return a list of DiffFiles between child and parent of this Flip.
	 */
	public List<DiffFile> getDiffFiles() {
		return child.getDiffFiles(parent);
	}
	
	/**
	 * Returns a set of tests that fail in parent but pass in child.
	 * 
	 * @return a set of tests that fail in parent but pass in child.
	 */
	public Set<String> getToPassTests() {
		return toPassTests;
	}
	
	/**
	 * Returns a set of tests that pass in parent but fail in child.
	 * 
	 * @return a set of tests that pass in parent but fail in child.
	 */
	public Set<String> getToFailTests() {
		return toFailTests;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		Flip flip = (Flip) other;

		return child.equals(flip.child) && parent.equals(flip.parent)
				&& toFailTests.equals(flip.toFailTests) && toPassTests.equals(flip.toPassTests);
	}

	@Override
	public int hashCode() {
		return 11 * child.hashCode() + 13 * parent.hashCode() 
				+ 17 * toFailTests.hashCode() + 19 * toPassTests.hashCode();
	}

	@Override
	public String toString() {
		String str = "child commit: " + child.getCommitID() + "\n"
				+ "parent commit: " + parent.getCommitID() + "\n";
		
		str += "diff files: \n";
		List<DiffFile> diffFiles = getDiffFiles();
		for (DiffFile diffFile : diffFiles) {
			str += diffFile.toString() + "\n";
		}
		
		if (!toPassTests.isEmpty()) {
			str += "TO PASS:\n";
			for (String test : toPassTests) {
				str += test + "\n";
			}
		}
		
		if (!toFailTests.isEmpty()) {
			str += "TO FAIL:\n";
			for (String test : toFailTests) {
				str += test + "\n";
			}
		}
		
		return str;
	}

	@Override
	public int compareTo(Flip other) {
		List<DiffFile> diffFiles = getDiffFiles();
		List<DiffFile> otherDiffFiles = other.getDiffFiles();
		
		return ((Integer) diffFiles.size()).compareTo(otherDiffFiles.size());
	}
}
