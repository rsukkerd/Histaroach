package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Revision represents a state of a particular commit. Revision has access to
 * its repository, commit ID, a list of its parents and their corresponding diff
 * files, compilable state, and test result. Revision contains methods to check
 * out itself from the repository, compile and run tests. These methods {
 * compile(), and compileAndRunAllTests() } modify the state of the revision.
 * Revision also contains methods that compile and run tests but do not modify
 * its state. These methods are build(command) and run(command).
 */
public class Revision implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -4044614975764741642L;

    public enum COMPILABLE {
        YES, NO, UNKNOWN
    }

    private final Repository repository;
    private final String commitID;
    /**
     * index mapping : a parent revision -> a list of files that are different
     * between the parent and this revision
     **/
    private final List<Revision> parents;
    private final List<List<DiffFile>> diffFiles;
    private COMPILABLE compilable;
    private/* @Nullable */TestResult testResult;

    /**
     * Create a revision Initially, compilable flag is unknown and test result
     * is null
     */
    public Revision(Repository repository, String commitID) {
        this.repository = repository;
        this.commitID = commitID;
        this.parents = new ArrayList<Revision>();
        this.diffFiles = new ArrayList<List<DiffFile>>();
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
        parents.add(parent);
        diffFiles.add(files);
    }

    /**
     * @return list of parents of this revision
     */
    public List<Revision> getParents() {
        return parents;
    }

    /**
     * @return list of diff files corresponding to the given parent
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        int i = parents.indexOf(parent);
        assert i >= 0;
        return diffFiles.get(i);
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
     * compile this revision
     * 
     * @modifies this
     */
    // private void compile() {
    // if (repository.build(repository.antBuild)
    // && repository.build(repository.antBuildtest)) {
    // compilable = COMPILABLE.YES;
    // } else {
    // compilable = COMPILABLE.NO;
    // }
    // }

    /**
     * compile and run all tests on this revision
     * 
     * @modifies this
     */
    private void compileAndRunAllTests() {
        testResult = repository.run(repository.antJunit, commitID);

        if (testResult != null) {
            compilable = COMPILABLE.YES;
        } else {
            compilable = COMPILABLE.NO;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository)
                && commitID.equals(revision.commitID)
                && parents.equals(revision.parents)
                && diffFiles.equals(revision.diffFiles)
                && compilable == revision.compilable
                && ((testResult == null && revision.testResult == null) || testResult
                        .equals(revision.testResult));
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() + 17
                * parents.hashCode() + 19 * diffFiles.hashCode() + 23
                * compilable.hashCode();
        if (testResult != null) {
            code += 29 * testResult.hashCode();
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
        } else {
            result += "unknown\n";
        }

        for (int i = 0; i < parents.size(); i++) {
            result += "parent : " + parents.get(i).getCommitID() + "\n";
            result += "diff files :\n";
            List<DiffFile> files = diffFiles.get(i);

            for (DiffFile file : files) {
                result += file + "\n";
            }
        }

        return result;
    }
}
