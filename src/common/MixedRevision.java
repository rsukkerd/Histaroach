package common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

/**
 * MixedRevision represents a hypothetical revision. MixedRevision maintains 
 * a base revision, this revision is not to be modified. MixedRevision has 
 * methods to mix in a file from other revision to the base revision, 
 * mix out a file, compile and run tests on it. These methods determine the 
 * compilability and test result of this MixedRevision.
 */
public class MixedRevision {
    private static final File BASE_TMP_DIR = new File("tmpBase");
    private static final File OTHER_TMP_DIR = new File("tmpOther");

    private final Revision baseRevision;
    private final Repository repository;
    private final File repoDir;
    
    private COMPILABLE compilable;
    private TestResult testResult;
    private Map<DiffFile, Revision> mixedInFiles;
    private List<DiffFile> mixedOutFiles;

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
        mixedInFiles = new HashMap<DiffFile, Revision>();
        mixedOutFiles = new ArrayList<DiffFile>();
        
        int exitValue = repository.checkoutCommit(baseRevision.getCommitID());
        assert (exitValue == 0);
    }

    /**
     * Mix in a file from other revision into the base revision. 
     * 
     * @requires diffFile.type == MODIFIED or DELETED
     * @modifies this, file system
     * @throws IOException
     */
    public void mixIn(DiffFile diffFile, Revision otherRevision)
            throws IOException {
    	String filename = diffFile.getFileName();
    	
        int exitValue = repository.checkoutCommit(otherRevision.getCommitID());
        assert (exitValue == 0);

        copyFile(filename, repoDir, OTHER_TMP_DIR);

        exitValue = repository.checkoutCommit(baseRevision.getCommitID());
        assert (exitValue == 0);

        if (diffFile.getDiffType() == DiffType.MODIFIED) {
        	// store original file in base temp directory
        	copyFile(filename, repoDir, BASE_TMP_DIR);
        }
        
        copyFile(filename, OTHER_TMP_DIR, repoDir);
        deleteFile(filename, OTHER_TMP_DIR);
        
        mixedInFiles.put(diffFile, otherRevision);
    }
    
    /**
     * Mix out a file from the base revision
     * 
     * @requires diffFile.type == ADDED
     * @modifies this, file system
     * @throws IOException 
     */
    public void mixOut(DiffFile diffFile) throws IOException {
    	String filename = diffFile.getFileName();
    	
    	// store original file in base temp directory
    	copyFile(filename, repoDir, BASE_TMP_DIR);
    	
    	deleteFile(filename, repoDir);
    	
    	mixedOutFiles.add(diffFile);
    }
    
    /**
     * @return a deep copy of the current state of this MixedRevision
     */
    public MixedRevision export() {
    	MixedRevision copy = new MixedRevision(baseRevision);
    	copy.compilable = compilable;
    	if (testResult == null) {
    		copy.testResult = null;
    	} else {
    		Set<String> allTests = testResult.getAllTests();
    		Set<String> failedTests = testResult.getFailedTests();
    		copy.testResult = new TestResult(baseRevision.getCommitID(), allTests, failedTests);
    	}
    	copy.mixedInFiles = new HashMap<DiffFile, Revision>();
    	for (DiffFile file : mixedInFiles.keySet()) {
    		Revision fromRev = mixedInFiles.get(file);
    		copy.mixedInFiles.put(file, fromRev);
    	}
    	copy.mixedOutFiles = new ArrayList<DiffFile>();
    	for (DiffFile file : mixedOutFiles) {
    		copy.mixedOutFiles.add(file);
    	}
    	
    	return copy;
    }
    
    /**
     * restore a file in the base revision to its original state
     * 
     * @modifies this, file system
     * @throws IOException
     */
    public void restoreBaseRevision(DiffFile diffFile) throws IOException {
    	String filename = diffFile.getFileName();
    	DiffType type = diffFile.getDiffType();
    	
    	if (type == DiffType.MODIFIED || type == DiffType.ADDED) {
    		// restore original file from base temp directory
    		copyFile(filename, BASE_TMP_DIR, repoDir);
    		deleteFile(filename, BASE_TMP_DIR);
    	} else {
    		deleteFile(filename, repoDir);
    	}
    	
    	if (type == DiffType.MODIFIED || type == DiffType.DELETED) {
    		mixedInFiles.remove(diffFile);
    	} else {
    		mixedOutFiles.remove(diffFile);
    	}
    }
    
    /**
     * restore all files in the base revision to their original states
     * 
     * @modifies this, file system
     * @throws IOException
     */
    public void restoreBaseRevsision() throws IOException {
    	for (DiffFile diffFile : mixedInFiles.keySet()) {
    		restoreBaseRevision(diffFile);
    	}
    	
    	for (DiffFile diffFile : mixedOutFiles) {
    		restoreBaseRevision(diffFile);
    	}
    }

    /**
     * @return compilability of the current state of this MixedRevision
     */
    public COMPILABLE isCompilable() {
        return compilable;
    }

    /**
     * @return test result of the current state of this MixedRevision
     */
    public TestResult getTestResult() {
        return testResult;
    }
    
    /**
     * @return all mixed in files and their corresponding revisions
     */
    public Map<DiffFile, Revision> getMixedInFiles() {
    	return mixedInFiles;
    }
    
    /**
     * @return all mixed out files
     */
    public List<DiffFile> getMixedOutFiles() {
    	return mixedOutFiles;
    }

    /**
     * compile the mixed revision
     * 
     * @modifies this
     */
    public void compile() {
    	COMPILABLE antBuild = repository.build(repository.antBuild);
    	
    	if (antBuild == COMPILABLE.YES) {
    		COMPILABLE antBuildtest = repository.build(repository.antBuildtest);
    		
    		if (antBuildtest == COMPILABLE.YES) {
    			compilable = COMPILABLE.YES;
    		} else {
    			compilable = COMPILABLE.NO;
    		}
    	} else {
    		// no build file, or not compilable
    		compilable = antBuild;
    	}
    }

    /**
     * compile and run all tests on the mixed revision
     * 
     * @modifies this
     */
    public void compileAndRunAllTests() {
        Pair<COMPILABLE, TestResult> pair = repository.run(repository.antJunit, baseRevision.getCommitID());
        compilable = pair.getFirst();
        testResult = pair.getSecond();
    }

    /**
     * copy a file from source directory to destination directory
     * 
     * @modifies file system
     */
    public void copyFile(String filename, File srcDir, File destDir)
            throws IOException {
        File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar
                + filename);
        FileUtils.copyFileToDirectory(srcFile, destDir);
    }

    /**
     * delete a file from directory
     * 
     * @modifies file system
     */
    public void deleteFile(String filename, File dir) throws IOException {
        File file = new File(dir.getAbsolutePath() + File.separatorChar
                + filename);
        FileUtils.forceDelete(file);
    }
}
