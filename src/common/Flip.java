package common;

import java.util.List;
import java.util.Map;

/**
 * Flip represents a pair of child-parent revisions that contain some tests
 * which one revision passes and the other revision fails.
 */
public class Flip {
    public enum FlipType {
        TO_FAIL,
        TO_FIX
    }

    private final Revision child;
    private final Revision parent;
    // TODO: What is this mapping to what? Write a comment to explain.
    private final Map<String, FlipType> testToFlipType;

    public Flip(Revision child, Revision parent,
            Map<String, FlipType> testToFlipType) {
        this.child = child;
        this.parent = parent;
        this.testToFlipType = testToFlipType;
    }

    public Revision getChildRevision() {
        return child;
    }

    public Revision getParentRevision() {
        return parent;
    }

    public List<DiffFile> getDiffFiles() {
        return child.getDiffFiles(parent.getCommitID());
    }
}
