package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * MixedRevision represents a hypothetical Revision. 
 * It is a Revision that has some of its files reverted 
 * to their former states in some Revisions in the past. 
 * 
 * MixedRevision keeps track of what Revisions their 
 * reverted files are reverted to. 
 * 
 * The public methods must be called in this order:  
 *  1. setRevertedFiles(diffFiles, otherRevision) and 
 *     checkoutBaseRevision() 
 *     (Note: setRevertedFiles can be called multiple times) 
 *  2. revertFiles() 
 *  3. runTest() 
 *  4. makeCopy() or other get methods 
 *  5. restoreBaseRevision(). 
 *  
 * To run tests on a different file mixture of the same baseRevision, 
 * repeat steps 1-5 without calling checkoutBaseRevision().
 */
public class MixedRevision {
    private final Revision baseRevision;
    private final IRepository repository;
    private final IRepository clonedRepository;
    private final File repoDir;
    private final File clonedRepoDir;
    
    private Compilable compilable;
    private TestResult testResult;
    // map: a set of reverted files -> a Revision they are reverted to
    private Map<Set<DiffFile>, Revision> revertedFileRecords;
    
    /**
     * Create an empty MixedRevision.
     * 
     * @requires baseRevision is compilable.
     */
    public MixedRevision(Revision baseRevision, IRepository repository, 
    		IRepository clonedRepository) {
        this.baseRevision = baseRevision;
        this.repository = repository;
        this.clonedRepository = clonedRepository;
        
        repoDir = repository.getDirectory();
        clonedRepoDir = clonedRepository.getDirectory();
        
        compilable = Compilable.UNKNOWN;
        testResult = null;
        revertedFileRecords = new HashMap<Set<DiffFile>, Revision>();        
    }
    
    /**
	 * Specifies diffFiles in baseRevision to be reverted 
	 * to their former states in otherRevision.
	 * 
	 * @requires otherRevision is compilable.
	 * @modifies this
     */
    public void setRevertedFiles(Set<DiffFile> diffFiles, Revision otherRevision) {
		revertedFileRecords.put(diffFiles, otherRevision);
    }
    
    /**
	 * Checks out the baseRevision into the working directory.
	 * 
	 * @throws Exception
	 */
	public void checkoutBaseRevision() throws Exception {
		boolean checkoutCommitSuccessful = repository.checkoutCommit(baseRevision.getCommitID());
		
	    if (!checkoutCommitSuccessful) {
	    	throw new Exception("check out base commit " + baseRevision.getCommitID() + " unsuccessful");
	    }
	}

	/**
	 * Reverts all diffFiles from all calls to 
	 * setRevertedFiles(diffFiles, otherRevision).
	 * 
	 * @modifies file system
	 * @throws Exception 
	 */
	public void revertFiles() throws Exception {
		
		for (Map.Entry<Set<DiffFile>, Revision> entry : revertedFileRecords.entrySet()) {
			Set<DiffFile> diffFiles = entry.getKey();
			Revision otherRevision = entry.getValue();
			
			boolean checkoutCommitSuccessful = clonedRepository.checkoutCommit(
					otherRevision.getCommitID());
	    	
	    	if (!checkoutCommitSuccessful) {
	    		throw new Exception("check out other commit " 
	    				+ otherRevision.getCommitID() + " unsuccessful");
	    	}
	    	
	    	for (DiffFile diffFile : diffFiles) {
	    		String filename = diffFile.getFileName();
	    		DiffType type = diffFile.getDiffType();
	    		
	    		if (type == DiffType.MODIFIED || type == DiffType.DELETED) {
	    			Util.copyFile(filename, clonedRepoDir, repoDir);
	    		} else {
	    			Util.deleteFile(filename, repoDir);
	    		}        		
	    	}
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
		IBuildStrategy buildStrategy = repository.getBuildStrategy();
		
	    compilable = buildStrategy.build();
	    
	    if (compilable == Compilable.YES) {
	    	testResult = buildStrategy.runTest();
	    } else {
	    	testResult = null;
	    }
	}

	/**
     * Makes a copy of this MixedRevision.
     * 
     * @return a deep copy of this MixedRevision.
     * @throws Exception 
     */
    public MixedRevision makeCopy() throws Exception {
    	MixedRevision copy = new MixedRevision(baseRevision, repository, clonedRepository);
    	copy.compilable = compilable;
    	
    	if (testResult == null) {
    		copy.testResult = null;
    	} else {
    		Set<String> allTests = testResult.getAllTests();
    		Set<String> failedTests = testResult.getFailedTests();
    		copy.testResult = new TestResult(allTests, failedTests);
    	}
    	
    	copy.revertedFileRecords = new HashMap<Set<DiffFile>, Revision>();
    	
    	for (Map.Entry<Set<DiffFile>, Revision> entry : revertedFileRecords.entrySet()) {
    		copy.revertedFileRecords.put(entry.getKey(), entry.getValue());
    	}
    	
    	return copy;
    }
    
    /**
     * Restores all reverted diffFiles in baseRevision to 
     * their original states. Clears Compilable and 
     * TestResult data.
     * 
     * @modifies this, file system
     * @throws Exception 
     */
    public void restoreBaseRevision() throws Exception {
    	
    	for (Set<DiffFile> revertedFiles : revertedFileRecords.keySet()) {
    		
    		for (DiffFile revertedFile : revertedFiles) {
    			String filename = revertedFile.getFileName();
    	    	DiffType type = revertedFile.getDiffType();
    	    	
    	    	if (type == DiffType.MODIFIED || type == DiffType.ADDED) {
    	    		boolean restoreFileSuccessful = repository.discardFileChange(filename);
    	    		
    	    		if (!restoreFileSuccessful) {
    	    			throw new Exception("restore file unsuccessful");
    	    		}
    	    	} else {
    	    		Util.deleteFile(filename, repoDir);
    	    	}
    		}
    	}
    	
    	revertedFileRecords = new HashMap<Set<DiffFile>, Revision>();
    	compilable = Compilable.UNKNOWN;
    	testResult = null;
    }
    
    public Revision getBaseRevision() {
    	return baseRevision;
    }
    
    public Compilable isCompilable() {
        return compilable;
    }
    
    public TestResult getTestResult() {
        return testResult;
    }
    
    public Map<Set<DiffFile>, Revision> getRevertedFileRecords() {
    	return revertedFileRecords;
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
				&& revertedFileRecords.equals(mr.revertedFileRecords);
	}

	@Override
	public int hashCode() {
		int hashCode = 11 * baseRevision.hashCode() + 13 * compilable.hashCode() 
						+ 17 * revertedFileRecords.hashCode();
		
		if (testResult != null) {
			hashCode += 19 * testResult.hashCode();
		}
		
		return hashCode;
	}
	
	@Override
    public String toString() {
        String str = "Base: " + baseRevision.getCommitID() + "\n";
        
        for (Set<DiffFile> diffFiles : revertedFileRecords.keySet()) {
        	Revision otherRevision = revertedFileRecords.get(diffFiles);
        	str += "Files reverted to " + otherRevision.getCommitID() + ":\n";
        	
        	for (DiffFile diffFile : diffFiles) {
        		str += diffFile.toString() + "\n";
        	}
        }
        
        str += "Compilable: " + compilable + "\n";
        
        if (compilable == Compilable.YES) {
            str += testResult.toString();
        }
        
        return str;
    }
}
