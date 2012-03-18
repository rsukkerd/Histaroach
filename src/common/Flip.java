package common;

import java.util.List;
import java.util.Set;

/**
 * Flip represents a pair of child-parent revisions that contain 
 * some tests which one revision passes and the other revision fails. 
 * Flip class implements Comparable interface: a flip that has fewer 
 * diff files is less a flip that has more diff files.
 */
public class Flip implements Comparable<Flip> {
	public enum FlipType {
		TO_PASS,
		TO_FAIL
	}

	private final Revision child;
	private final Revision parent;
	private final List<DiffFile> diffFiles;
	// set of tests that flip from fail to pass
	private final Set<String> toPassTests;
	// set of tests that flip from pass to fail
	private final Set<String> toFailTests;

	public Flip(Revision child, Revision parent, Set<String> toPassTests,
			Set<String> toFailTests) {
		this.child = child;
		this.parent = parent;
		diffFiles = child.getDiffFiles(parent);
		this.toPassTests = toPassTests;
		this.toFailTests = toFailTests;
	}

	public Revision getChildRevision() {
		return child;
	}

	public Revision getParentRevision() {
		return parent;
	}

	public List<DiffFile> getDiffFiles() {
		return diffFiles;
	}
	
	public Set<String> getToPassTests() {
		return toPassTests;
	}
	
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
		return diffFiles.size() - other.diffFiles.size();
	}
}
