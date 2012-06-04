package histaroach.model;

import java.util.Set;


/**
 * Flip represents a pair of child-parent Revisions that have 
 * at least one test which one Revision passes and the other fails. 
 * 
 * Flip is comparable. A Flip that has fewer DiffFiles is less 
 * than a Flip that has more DiffFiles.
 * 
 * Flip is immutable.
 */
public class Flip implements Comparable<Flip> {
	public enum FlipType {
		// indicates that a test flipped from failing in the parent to passing in the child.
		TO_PASS,
		
		// indicates that a test flipped from passing in the parent to failing in the child.
		TO_FAIL
	}

	private final Revision child;
	private final Revision parent;
	// set of tests that flip from fail to pass
	private final Set<String> toPassTests;
	// set of tests that flip from pass to fail
	private final Set<String> toFailTests;
	
	/**
	 * Creates a Flip.
	 * 
	 * @requires !toPassTests.isEmpty() || !toFailTests.isEmpty()
	 */
	public Flip(Revision child, Revision parent, Set<String> toPassTests,
			Set<String> toFailTests) {
		this.child = child;
		this.parent = parent;
		this.toPassTests = toPassTests;
		this.toFailTests = toFailTests;
	}
	
	public Revision getChildRevision() {
		return child;
	}
	
	public Revision getParentRevision() {
		return parent;
	}
	
	/**
	 * @return a set of DiffFiles between the child and the parent.
	 */
	public Set<DiffFile> getDiffFiles() {
		return child.getDiffFiles(parent);
	}
	
	/**
	 * @return a set of tests flipped from failing in the parent 
	 *         to passing in the child.
	 */
	public Set<String> getToPassTests() {
		return toPassTests;
	}
	
	/**
	 * @return a set of tests flipped from passing in the parent 
	 *         to failing in the child.
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
		Set<DiffFile> diffFiles = getDiffFiles();
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
		Set<DiffFile> diffFiles = getDiffFiles();
		Set<DiffFile> otherDiffFiles = other.getDiffFiles();
		
		return ((Integer) diffFiles.size()).compareTo(otherDiffFiles.size());
	}
}
