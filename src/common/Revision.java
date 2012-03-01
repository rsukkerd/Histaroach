package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Revision represents a state of a particular commit. Revision has access to
 * its repository, commit ID, a set of its parents and their corresponding diff
 * files, compilable state, and test result.
 */
public class Revision implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -4044614975764741642L;

    public enum COMPILABLE {
        YES, NO, UNKNOWN, NO_BUILD_FILE
    }

    private final Repository repository;
    private final String commitID;
    /**
     * mapping : parent revision -> a list of files that are different
     * between the parent and this revision
     */
    private final Map<Revision, List<DiffFile>> parentToDiffFiles;
    private COMPILABLE compilable;
    private/* @Nullable */TestResult testResult;

    /**
     * Create a revision.
     * Compilable state and test result are known.
     */
    public Revision(Repository repository, String commitID) {
        this.repository = repository;
        this.commitID = commitID;
        parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;

        // Check out the revision.
        int exitValue = repository.checkoutCommit(commitID);
        assert exitValue == 0;

        // Run all the tests on the checked-out revision.
        compileAndRunAllTests();
    }

    /**
     * @return repository of this revision
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @return commit ID of this revision
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * add a parent revision and its corresponding diff files
     */
    public void addParent(Revision parent, List<DiffFile> files) {
        parentToDiffFiles.put(parent, files);
    }

    /**
     * @return set of parents of this revision
     */
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * @return list of diff files corresponding to the given parent
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }

    /**
     * @return compilable flag of this revision
     */
    public COMPILABLE isCompilable() {
        return compilable;
    }

    /**
     * @return test result of this revision
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * compile and run all tests on this revision
     * 
     * @modifies this
     */
    private void compileAndRunAllTests() {
        Pair<COMPILABLE, TestResult> pair = repository.run(repository.antJunit, commitID);
        compilable = pair.getFirst();
        testResult = pair.getSecond();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository)
                && commitID.equals(revision.commitID)
                && parentToDiffFiles.equals(revision.parentToDiffFiles)
                && compilable == revision.compilable
                && ((testResult == null && revision.testResult == null) || testResult
                        .equals(revision.testResult));
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        			+ 17 * parentToDiffFiles.hashCode() + 19 * compilable.hashCode();
        if (testResult != null) {
            code += 23 * testResult.hashCode();
        }

        return code;
    }

    @Override
    public String toString() {
        String result = "commit : " + commitID + "\n";
        result += "compilable : ";
        if (compilable == COMPILABLE.YES) {
            result += "yes\n";
            result += testResult.toString();
        } else if (compilable == COMPILABLE.NO) {
            result += "no\n";
        } else if (compilable == COMPILABLE.UNKNOWN) {
            result += "unknown\n";
        } else {
            result += "no build file\n";
        }
        
        for (Revision parent : parentToDiffFiles.keySet()) {
        	result += "parent : " + parent.getCommitID() + "\n";
        	result += "diff files :\n";
        	List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	
        	for (DiffFile diffFile : diffFiles) {
        		result += diffFile + "\n";
        	}
        }

        return result;
    }
}
