package common;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import util.Pair;

import common.DiffFile.DiffType;
import common.Revision.Compilable;

/**
 * MixedRevision represents a hypothetical Revision. 
 * It contains a baseRevision, a set of reverted 
 * files and Revisions they are reverted to. 
 * 
 * MixedRevision contains the following public methods: 
 *  - revertFiles(diffFiles, otherRevision): reverts 
 *    diffFiles in baseRevision to their former states 
 *    in otherRevision 
 *  - runTest(): compiles this MixedRevision, runs tests, 
 *    and parses the test results 
 *  - export(): exports this MixedRevision 
 *  - restoreBaseRevision(diffFile): restores a diffFile 
 *    in baseRevision to its original state 
 *  - restoreBaseRevision(): restores all diffFiles 
 *    in baseRevision to their original states 
 *  - getBaseRevision(): returns baseRevision 
 *  - isCompilable(): returns a Compilable state 
 *  - getTestResult(): returns a TestResult 
 *  - getRevertedFiles(): returns all reverted files 
 *    and Revisions they are reverted to.
 */
public class MixedRevision {
    private final Revision baseRevision;
    private final Repository repository;
    private final Repository clonedRepository;
    private final File repoDir;
    private final File clonedRepoDir;
    
    private Compilable compilable;
    private TestResult testResult;
    private Map<DiffFile, Revision> revertedFiles;

    /**
     * Create a MixedRevision.
     * 
     * @requires baseRevision is compilable.
     * @throws Exception 
     */
    public MixedRevision(Revision baseRevision, Repository repository, Repository clonedRepository) 
    		throws Exception {
        this.baseRevision = baseRevision;
        this.repository = repository;
        this.clonedRepository = clonedRepository;
        
        repoDir = repository.getDirectory();
        clonedRepoDir = clonedRepository.getDirectory();
        
        compilable = Compilable.UNKNOWN;
        testResult = null;
        revertedFiles = new HashMap<DiffFile, Revision>();
        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(baseRevision.getCommitID());
        if (!checkoutCommitSuccessful) {
        	throw new Exception("check out base commit " + baseRevision.getCommitID() + " unsuccessful");
        }
    }
    
    /**
     * Reverts diffFiles in baseRevision to their former states 
     * in otherRevision.
     * 
     * @requires otherRevision is compilable.
     * @modifies this, file system
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
	 * Compiles this MixedRevision, runs tests, and 
	 * parses the test results.
	 * 
	 * @modifies this
	 * @throws Exception
	 */
	public void runTest() throws Exception {
		BuildStrategy buildStrategy = repository.getBuildStrategy();
		// Pair<COMPILABLE, TestResult> pair = buildStrategy.runTest();
		Pair<Compilable, TestResult> pair = buildStrategy.runTestViaShellScript();
	    compilable = pair.getFirst();
	    testResult = pair.getSecond();
	}

	/**
     * Exports this MixedRevision.
     * 
     * @return a deep copy of the current states of this MixedRevision.
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
    		copy.testResult = new TestResult(allTests, failedTests);
    	}
    	
    	copy.revertedFiles = new HashMap<DiffFile, Revision>();
    	for (DiffFile diffFile : revertedFiles.keySet()) {
    		Revision otherRevision = revertedFiles.get(diffFile);
    		copy.revertedFiles.put(diffFile, otherRevision);
    	}
    	
    	return copy;
    }
    
    /**
     * Restores a diffFile in baseRevision to its original state.
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
    	compilable = Compilable.UNKNOWN;
    	testResult = null;
    }
    
    /**
     * Restores all diffFiles in baseRevision to their original states.
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
     * Returns baseRevision.
     * 
     * @return baseRevision of this MixedRevision
     */
    public Revision getBaseRevision() {
    	return baseRevision;
    }

    /**
     * Returns a Compilable state.
     * 
     * @return a current Compilable state of this MixedRevision.
     */
    public Compilable isCompilable() {
        return compilable;
    }

    /**
     * Returns a TestResult.
     * 
     * @return a current TestResult of this MixedRevision.
     */
    public TestResult getTestResult() {
        return testResult;
    }
    
    /**
     * Returns all reverted files and Revisions they are reverted to.
     * 
     * @return all reverted files and Revisions they are reverted to.
     */
    public Map<DiffFile, Revision> getRevertedFiles() {
    	return revertedFiles;
    }

    /**
     * Copies a file from source directory to destination directory.
     * 
     * @modifies file system
     * @throws IOException
     */
    private void copyFile(String filename, File srcDir, File destDir) throws IOException {
        File srcFile = new File(srcDir.getAbsolutePath() + File.separatorChar 
                + filename);
        File destFile = new File(destDir.getAbsolutePath() + File.separatorChar 
        		+ filename);
        FileUtils.copyFile(srcFile, destFile);
    }

    /**
     * Deletes a file from directory.
     * 
     * @modifies file system
     * @throws IOException 
     */
    private void deleteFile(String filename, File dir) throws IOException {
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
        String str = "Base: " + baseRevision.getCommitID() + "\n";
        if (!revertedFiles.isEmpty()) {
	        str += "Reverted Files:\n";
	        
	    	for (DiffFile diffFile : revertedFiles.keySet()) {
	    		str += diffFile.toString() + "\n";
	    		
	    		Revision otherRevision = revertedFiles.get(diffFile);
	    		str += "To: " + otherRevision.getCommitID() + "\n";
	    	}
        }
        
        str += "Compilable: " + compilable + "\n";
        
        if (compilable == Compilable.YES) {
            str += testResult.toString();
        }
        
        return str;
    }
}
