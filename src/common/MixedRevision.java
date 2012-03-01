package common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import common.Revision.COMPILABLE;

/**
 * MixedRevision represents a hypothetical revision. MixedRevision maintains a
 * base revision, this revision is not to be modified. MixedRevision has methods
 * to mix in a file from some other revision to the base revision, compile and
 * run tests on it. These methods determine the compilability and test result of
 * this MixedRevision.
 */
public class MixedRevision {
    private static final File BASE_TMP_DIR = new File("tmpBase");
    private static final File OTHER_TMP_DIR = new File("tmpOther");

    private final Revision baseRevision;
    private final Repository repository;
    private final File repoDir;
    private COMPILABLE compilable;
    private TestResult testResult;

    /**
     * Create a MixedRevision
     * 
     * @param baseRevision
     */
    public MixedRevision(Revision baseRevision) {
        this.baseRevision = baseRevision;
        repository = baseRevision.getRepository();
        repoDir = repository.getDirectory();
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;
    }

    /**
     * Mix in a file from other revision into the base revision.
     */
    public void mixIn(String filename, Revision otherRevision)
            throws IOException {
        int exitValue = repository.checkoutCommit(otherRevision.getCommitID());
        assert (exitValue == 0);

        copyFile(filename, repoDir, OTHER_TMP_DIR);

        exitValue = repository.checkoutCommit(baseRevision.getCommitID());
        assert (exitValue == 0);

        copyFile(filename, repoDir, BASE_TMP_DIR);
        copyFile(filename, OTHER_TMP_DIR, repoDir);
    }

    /**
     * restore a file in the base revision to its original version
     */
    public void restoreBaseRevision(String filename) throws IOException {
        copyFile(filename, BASE_TMP_DIR, repoDir);

        // clear files in base and other temp directories
        deleteFile(filename, BASE_TMP_DIR);
        deleteFile(filename, OTHER_TMP_DIR);
    }

    public COMPILABLE isCompilable() {
        return compilable;
    }

    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * compile the mixed revision
     * 
     * @modifies this
     */
    public void compile() {
        boolean build = repository.build(repository.antBuild);
        boolean buildtest = repository.build(repository.antBuildtest);

        if (build && buildtest) {
            compilable = COMPILABLE.YES;
        } else {
            compilable = COMPILABLE.NO;
        }
    }

    /**
     * compile and run all tests on the mixed revision
     * 
     * @modifies this
     */
    public void compileAndRunAllTests() {
        testResult = repository.run(repository.antJunit,
                baseRevision.getCommitID() + "-mixed");

        if (testResult != null) {
            compilable = COMPILABLE.YES;
        } else {
            compilable = COMPILABLE.NO;
        }
    }

    /**
     * copy a file from source directory to destination directory
     */
    public void copyFile(String filename, File srcDir, File destDir)
            throws IOException {
        File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar
                + filename);
        FileUtils.copyFileToDirectory(srcFile, destDir);
    }

    /**
     * delete a file from directory
     */
    public void deleteFile(String filename, File dir) throws IOException {
        File file = new File(dir.getAbsolutePath() + File.separatorChar
                + filename);
        FileUtils.forceDelete(file);
    }
}
