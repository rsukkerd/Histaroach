package common;

import java.util.List;

/**
 * Flip represents a pair of child-parent revisions that contain some tests
 * which one revision passes and the other revision fails.
 */
public class Flip {
	public enum FlipType {
		TO_PASS,
		TO_FAIL
	}

	private final Revision child;
	private final Revision parent;
	// list of tests that flip from fail to pass
	private final List<String> toFixTests;
	// list of tests that flip from pass to fail
	private final List<String> toFailTests;

	public Flip(Revision child, Revision parent, List<String> toFixTests,
			List<String> toFailTests) {
		this.child = child;
		this.parent = parent;
		this.toFixTests = toFixTests;
		this.toFailTests = toFailTests;
	}

	public Revision getChildRevision() {
		return child;
	}

	public Revision getParentRevision() {
		return parent;
	}

	public List<DiffFile> getDiffFiles() {
		return child.getDiffFiles(parent);
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		Flip flip = (Flip) other;

		return child.equals(flip.child) && parent.equals(flip.parent)
				&& toFailTests.equals(flip.toFailTests) && toFixTests.equals(flip.toFixTests);
	}

	@Override
	public int hashCode() {
		return 11 * child.hashCode() + 13 * parent.hashCode() 
				+ 17 * toFailTests.hashCode() + 19 * toFixTests.hashCode();
	}

	@Override
	public String toString() {
		String str = "child commit: " + child.getCommitID() + "\n"
				+ "parent commit: " + parent.getCommitID() + "\n";
		
		str += "TO PASS:\n";
		for (String test : toFixTests) {
			str += test + "\n";
		}
		
		str += "TO FAIL:\n";
		for (String test : toFailTests) {
			str += test + "\n";
		}
		
		return str;
	}
}
