package common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

/**
 * MixedRevision represents a hypothetical Revision. 
 * 
 * MixedRevision maintains a base revision. It has methods to revert 
 * a subset of files in the base revision to their former states in 
 * other revision, compile and run all unit tests on the partially-
 * reverted base revision. These methods determine the COMPILABLE state 
 * and TestResult of this MixedRevision.
 */
public class MixedRevision {
    private final Revision baseRevision;
    private final Repository repository;
    private final Repository clonedRepository;
    private final File repoDir;
    private final File clonedRepoDir;
    
    private COMPILABLE compilable;
    private TestResult testResult;
    private Map<DiffFile, Revision> revertedFiles;

    /**
     * Create a MixedRevision.
     * 
     * @throws Exception 
     */
    public MixedRevision(Revision baseRevision, Repository repository, Repository clonedRepository) 
    		throws Exception {
        this.baseRevision = baseRevision;
        this.repository = repository;
        this.clonedRepository = clonedRepository;
        
        repoDir = repository.getDirectory();
        clonedRepoDir = clonedRepository.getDirectory();
        
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;
        revertedFiles = new HashMap<DiffFile, Revision>();
        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(baseRevision.getCommitID());
        if (!checkoutCommitSuccessful) {
        	throw new Exception("check out base commit " + baseRevision.getCommitID() + " unsuccessful");
        }
    }
    
    /**
     * Revert files in the base revision to their former states 
     * in other revision.
     * 
     * @modifies this, files in the base revision
     * @throws Exception 
     */
    public void revertFiles(Set<DiffFile> diffFiles, Revision otherRevision) throws Exception {
    	boolean checkoutCommitSuccessful = clonedRepository.checkoutCommit(otherRevision.getCommitID());
    	
    	if (checkoutCommitSuccessful) {
	    	for (DiffFile diffFile : diffFiles) {
	    		String filename = diffFile.getFileName();
	    		DiffType type = diffFile.getDiffType();
	    		
	    		if (type == DiffType.MODIFIED || type == DiffType.DELETED) {
	    			copyFile(filename, clonedRepoDir, repoDir);
	    		} else {
	    			deleteFile(filename, repoDir);
	    		}
	    		
	    		revertedFiles.put(diffFile, otherRevision);
	    	}
    	} else {
    		throw new Exception("check out other commit " + otherRevision.getCommitID() + " unsuccessful");
    	}
    }

    /**
     * Export this MixedRevision.
     * 
     * @return a deep copy of the current state of this MixedRevision
     * @throws Exception 
     */
    public MixedRevision export() throws Exception {
    	MixedRevision copy = new MixedRevision(baseRevision, repository, clonedRepository);
    	copy.compilable = compilable;
    	
    	if (testResult == null) {
    		copy.testResult = null;
    	} else {
    		Set<String> allTests = testResult.getAllTests();
    		Set<String> failedTests = testResult.getFailedTests();
    		copy.testResult = new TestResult(baseRevision.getCommitID(), allTests, failedTests);
    	}
    	
    	copy.revertedFiles = new HashMap<DiffFile, Revision>();
    	for (DiffFile diffFile : revertedFiles.keySet()) {
    		Revision otherRevision = revertedFiles.get(diffFile);
    		copy.revertedFiles.put(diffFile, otherRevision);
    	}
    	
    	return copy;
    }
    
    /**
     * Restore a file in the base revision to its original state.
     * 
     * @modifies this, file system
     * @throws Exception 
     */
    public void restoreBaseRevision(DiffFile diffFile) throws Exception {
    	String filename = diffFile.getFileName();
    	DiffType type = diffFile.getDiffType();
    	
    	if (type == DiffType.MODIFIED || type == DiffType.ADDED) {
    		boolean restoreFileSuccessful = repository.discardFileChange(filename);
    		
    		if (!restoreFileSuccessful) {
    			throw new Exception("restore file unsuccessful");
    		}
    	} else {
    		deleteFile(filename, repoDir);
    	}
    	
    	revertedFiles.remove(diffFile);
    	compilable = COMPILABLE.UNKNOWN;
    	testResult = null;
    }
    
    /**
     * Restore all files in the base revision to their original states.
     * 
     * @modifies this, file system
     * @throws Exception 
     */
    public void restoreBaseRevision() throws Exception {
    	Set<DiffFile> revertedDiffFiles = new HashSet<DiffFile>();
    	
    	for (DiffFile diffFile : revertedFiles.keySet()) {
    		revertedDiffFiles.add(diffFile);
    	}
    	
    	for (DiffFile diffFile : revertedDiffFiles) {
    		restoreBaseRevision(diffFile);
    	}
    }
    
    /**
     * @return base revision of this MixedRevision
     */
    public Revision getBaseRevision() {
    	return baseRevision;
    }

    /**
     * @return a current COMPILABLE state of this MixedRevision
     */
    public COMPILABLE isCompilable() {
        return compilable;
    }

    /**
     * @return a current TestResult of this MixedRevision
     */
    public TestResult getTestResult() {
        return testResult;
    }
    
    /**
     * @return all reverted files and their source Revisions
     */
    public Map<DiffFile, Revision> getRevertedFiles() {
    	return revertedFiles;
    }

    /**
     * Compile and run all unit tests on this MixedRevision.
     * 
     * @modifies this
     * @throws Exception
     */
    public void runTest() throws Exception {
    	BuildStrategy buildStrategy = repository.getBuildStrategy();
    	// Pair<COMPILABLE, TestResult> pair = buildStrategy.runTest(baseRevision.getCommitID());
    	Pair<COMPILABLE, TestResult> pair = buildStrategy.runTestViaShellScript(baseRevision.getCommitID());
        compilable = pair.getFirst();
        testResult = pair.getSecond();
    }

    /**
     * Copy a file from source directory to destination directory.
     * 
     * @modifies file system
     * @throws IOException
     */
    public void copyFile(String filename, File srcDir, File destDir) throws IOException {
        File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar 
                + filename);
        File destFile = new File(destDir.getAbsolutePath() + File.separatorChar 
        		+ filename);
        FileUtils.copyFile(srcFile, destFile);
    }

    /**
     * Delete a file from directory.
     * 
     * @modifies file system
     * @throws IOException 
     */
    public void deleteFile(String filename, File dir) throws IOException {
        File file = new File(dir.getAbsolutePath() + File.separatorChar
                + filename);
		FileUtils.forceDelete(file);
    }
    
    @Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		MixedRevision mr = (MixedRevision) other;

		return baseRevision.equals(mr.baseRevision) && compilable == mr.compilable 
				&& ((testResult == null && mr.testResult == null) || 
						(testResult != null && testResult.equals(mr.testResult)))
				&& revertedFiles.equals(mr.revertedFiles);
	}

	@Override
	public int hashCode() {
		int hashCode = 11 * baseRevision.hashCode() + 13 * compilable.hashCode() 
						+ 17 * revertedFiles.hashCode();
		if (testResult != null) {
			hashCode += 19 * testResult.hashCode();
		}
		
		return hashCode;
	}
	
	@Override
    public String toString() {
        String str = "base : " + baseRevision.getCommitID() + "\n";
        if (!revertedFiles.isEmpty()) {
	        str += "reverted files :\n";
	        
	    	for (DiffFile diffFile : revertedFiles.keySet()) {
	    		str += diffFile.toString() + "\n";
	    		
	    		Revision otherRevision = revertedFiles.get(diffFile);
	    		str += "reference revision : " + otherRevision.getCommitID() + "\n";
	    	}
        }
        
        str += "compilable : ";
        if (compilable == COMPILABLE.YES) {
        	assert testResult != null;
            str += "yes\n";
            str += testResult.toString();
        } else if (compilable == COMPILABLE.NO) {
            str += "no\n";
        } else if (compilable == COMPILABLE.UNKNOWN) {
            str += "unknown\n";
        } else {
            str += "no build file\n";
        }
        
        return str;
    }
}
