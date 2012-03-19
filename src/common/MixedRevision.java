package common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import voldemort.VoldemortTestParsingStrategy;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

/**
 * MixedRevision represents a hypothetical revision. MixedRevision maintains 
 * a base revision. MixedRevision has methods to revert a subset of files in 
 * the base revision to their former states in other revision, compile and 
 * run tests on the partially reverted base revision. These methods determine 
 * the compilability and test result of this MixedRevision.
 */
public class MixedRevision {    
    private static final String ANT_COMMAND = "ant";
    private static final TestParsingStrategy STRATEGY = new VoldemortTestParsingStrategy();

    private final Revision baseRevision;
    private final Repository repository;
    private final Repository clonedRepository;
    private final File repoDir;
    private final File clonedRepoDir;
    
    private COMPILABLE compilable;
    private TestResult testResult;
    private Map<DiffFile, Revision> mixedInFiles;
    private List<DiffFile> mixedOutFiles;

    /**
     * Create a MixedRevision
     */
    public MixedRevision(Revision baseRevision, String repoPath, String clonedRepoPath) {
        this.baseRevision = baseRevision;
        
        repository = new Repository(repoPath, ANT_COMMAND, STRATEGY);        
        repoDir = repository.getDirectory();
        
        clonedRepository = new Repository(clonedRepoPath, ANT_COMMAND, STRATEGY);
        clonedRepoDir = clonedRepository.getDirectory();
        
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;
        mixedInFiles = new HashMap<DiffFile, Revision>();
        mixedOutFiles = new ArrayList<DiffFile>();
        
        int exitValue = repository.checkoutCommit(baseRevision.getCommitID());
        assert exitValue == 0;
    }
    
    /**
     * Revert files in the base revision to their former states 
     * in other revision
     * 
     * @modifies this, files in the base revision.
     * @throws IOException
     */
    public void revertFiles(List<DiffFile> diffFiles, Revision otherRevision) throws IOException {
    	int exitValue = clonedRepository.checkoutCommit(otherRevision.getCommitID());
    	assert exitValue == 0;
    	
    	for (DiffFile diffFile : diffFiles) {
    		String filename = diffFile.getFileName();
    		DiffType type = diffFile.getDiffType();
    		
    		if (type == DiffType.MODIFIED || type == DiffType.DELETED) {
    			copyFile(filename, clonedRepoDir, repoDir);
    			mixedInFiles.put(diffFile, otherRevision);
    		} else {
    			deleteFile(filename, repoDir);
    			mixedOutFiles.add(diffFile);
    		}
    	}
    }

    /**
     * Export this MixedRevision
     * 
     * @return a deep copy of the current state of this MixedRevision
     */
    public MixedRevision export() {
    	MixedRevision copy = new MixedRevision(baseRevision, repoDir.getPath(), clonedRepoDir.getPath());
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
     * Restore a file in the base revision to its original state
     * 
     * @modifies this, file system
     * @throws IOException
     */
    public void restoreBaseRevision(DiffFile diffFile) throws IOException {
    	String filename = diffFile.getFileName();
    	DiffType type = diffFile.getDiffType();
    	
    	if (type == DiffType.MODIFIED || type == DiffType.ADDED) {
    		Process restoreProcess = Util.runProcess(
    				new String[] { "git", "checkout", filename }, repoDir);
    		int exitValue = restoreProcess.exitValue();
    		assert exitValue == 0;
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
     * Restore all files in the base revision to their original states
     * 
     * @modifies this, file system
     * @throws IOException
     */
    public void restoreBaseRevision() throws IOException {
    	List<DiffFile> mixedFiles = new ArrayList<DiffFile>();
    	
    	for (DiffFile diffFile : mixedInFiles.keySet()) {
    		mixedFiles.add(diffFile);
    	}
    	
    	for (DiffFile diffFile : mixedOutFiles) {
    		mixedFiles.add(diffFile);
    	}
    	
    	for (DiffFile diffFile : mixedFiles) {
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
     * Get all new files in this MixedRevision
     * 
     * @return all new files and their corresponding source revisions
     */
    public Map<DiffFile, Revision> getNewFiles() {
    	return mixedInFiles;
    }
    
    /**
     * Get all files that are removed from this MixedRevision
     * 
     * @return all removed files
     */
    public List<DiffFile> getRemovedFiles() {
    	return mixedOutFiles;
    }

    /**
     * Compile this MixedRevision
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
     * Compile and run all tests on this MixedRevision
     * 
     * @modifies this
     */
    public void compileAndRunAllTests() {
        Pair<COMPILABLE, TestResult> pair = repository.run(repository.antJunit, baseRevision.getCommitID());
        compilable = pair.getFirst();
        testResult = pair.getSecond();
    }

    /**
     * Copy a file from source directory to destination directory 
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
     * Delete a file from directory
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
				&& mixedInFiles.equals(mr.mixedInFiles) && mixedOutFiles.equals(mr.mixedOutFiles);
	}

	@Override
	public int hashCode() {
		int hashCode = 11 * baseRevision.hashCode() + 13 * compilable.hashCode() 
						+ 17 * mixedInFiles.hashCode() + 19 * mixedOutFiles.hashCode();
		if (testResult != null) {
			hashCode += 23 * testResult.hashCode();
		}
		
		return hashCode;
	}
	
	@Override
    public String toString() {
        String str = "base : " + baseRevision.getCommitID() + "\n";
        str += "compilable : ";
        
        if (compilable == COMPILABLE.YES) {
            str += "yes\n";
            str += testResult.toString();
        } else if (compilable == COMPILABLE.NO) {
            str += "no\n";
        } else if (compilable == COMPILABLE.UNKNOWN) {
            str += "unknown\n";
        } else {
            str += "no build file\n";
        }
        
        if (!mixedInFiles.isEmpty()) {
        	str += "new files :\n";
        	for (DiffFile diffFile : mixedInFiles.keySet()) {
        		str += diffFile.toString() + " | ";
        		Revision srcRev = mixedInFiles.get(diffFile);
        		str += srcRev.getCommitID() + "\n";
        	}
        }
        
        if (!mixedOutFiles.isEmpty()) {
        	str += "removed files :\n";
        	for (DiffFile diffFile : mixedOutFiles) {
        		str += diffFile.toString() + "\n";
        	}
        }

        return str;
    }
}
