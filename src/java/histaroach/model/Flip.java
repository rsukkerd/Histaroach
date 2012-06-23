package histaroach.model;

import java.util.Set;


/**
 * Flip represents a pair of parent-child Revisions that have 
 * at least one test which one Revision passes and the other fails. 
 * 
 * There are 3 types of Flip:
 *  1) TO_PASS: contains only tests that flip from fail->pass
 *  2) TO_FAIL: contains only tests that flip from pass->fail, and
 *  3) BOTH: contains both tests that flip from fail->pass and from pass->fail.
 * 
 * Flip is comparable. A Flip that has fewer DiffFiles is less 
 * than a Flip that has more DiffFiles.
 * 
 * Flip is immutable.
 */
public class Flip implements Comparable<Flip> {
	public enum FlipType {
		/**
		 * The Flip contains only tests that flip from fail->pass.
		 */
		TO_PASS,
		
		/**
		 * The Flip contains only tests that flip from pass->fail.
		 */
		TO_FAIL,
		
		/**
		 * The Flip contains both tests that flip from fail->pass 
		 * and from pass->fail.
		 */
		BOTH
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
	public Flip(Revision parent, Revision child, Set<String> toPassTests,
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
	
	public FlipType getFlipType() {
		if (toFailTests.isEmpty()) {
			return FlipType.TO_PASS;
		}
		
		if (toPassTests.isEmpty()) {
			return FlipType.TO_FAIL;
		}
		
		return FlipType.BOTH;
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
		String str = "parent commit: " + parent.getCommitID() + "\n"
				+ "child commit: " + child.getCommitID() + "\n";
		
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
