package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.IOException;
import java.util.Set;


/**
 * IntermediateRevision represents an intermediate state between 2 Revisions: 
 * a base Revision and a successor Revision. An IntermediateRevision is 
 * a base plus a subset of changes between base and successor (delta). 
 * 
 * The public methods must be called in this order:  
 *  1. checkoutBaseSuccessorRevisions() and setDelta(delta) 
 *     (Note: these 2 methods can be called in either order) 
 *  2. applyDelta() 
 *  3. runTest() 
 *  4. makeCopy() or other getter methods 
 *  5. restoreBaseRevision(). 
 *  
 * To get a different IntermediateRevision of the same base-successor (ie. 
 * differs in delta), repeat steps 1-5 without calling checkoutBaseSuccessorRevisions().
 */
public class IntermediateRevision {
	private final IRepository repository;
    private final IRepository clonedRepository;
    
    private final Revision base;
    private final Revision successor;
	private final Set<DiffFile> totalDelta;
	
    private Set<DiffFile> delta;
	private Compilable compilable;
    private boolean testAborted;
	private TestResult testResult;
	
    /**
     * Create an empty IntermediateRevision.
     * 
     * @requires base and successor Revisions are compilable.
     * @throws InterruptedException 
     * @throws IOException 
     */
    public IntermediateRevision(Revision base, Revision successor, 
    		IRepository repository, IRepository clonedRepository) 
    		throws IOException, InterruptedException {
    	this.base = base;
    	this.successor = successor;
    	
        this.repository = repository;
        this.clonedRepository = clonedRepository;
        
        if (successor.isChildOf(base)) {
        	totalDelta = successor.getDiffFiles(base);
        } else {
        	totalDelta = repository.getDiffFiles(base.getCommitID(), 
        			successor.getCommitID());
        }
        
        resetFields();
    }
    
    /**
	 * Checks out base and successor Revisions into the working directories.
	 * 
	 * @throws Exception
	 */
	public void checkoutBaseSuccessorRevisions() throws Exception {
		checkoutRevision(base, repository);
		checkoutRevision(successor, clonedRepository);
	}
	
	/**
	 * Specifies a set of changes to be applied to base Revision.
	 * 
	 * @modifies this
     */
    public void setDelta(Set<DiffFile> delta) {
    	this.delta = delta;
    }
    
    /**
	 * Applies the set of changes to base Revision.
	 * 
	 * @modifies file system
	 * @throws Exception
	 */
	public void applyDelta() throws Exception {
		boolean checkoutCommitSuccessful = clonedRepository.checkoutCommit(
				successor.getCommitID());
    	
    	if (!checkoutCommitSuccessful) {
    		throw new Exception("check out commit " + successor.getCommitID() + 
    				" from " + clonedRepository.getDirectory() + " unsuccessful");
    	}
    	
    	for (DiffFile diffFile : delta) {
    		String filename = diffFile.getFileName();
    		DiffType type = diffFile.getDiffType();
    		
    		if (type == DiffType.ADDED || type == DiffType.MODIFIED) {
    			Util.copyFile(filename, clonedRepository.getDirectory(), 
    					repository.getDirectory());
    		} else {
    			Util.deleteFile(filename, repository.getDirectory());
    		}        		
    	}
	}

	/**
	 * Compiles this IntermediateRevision, runs tests, and 
	 * parses the test results.
	 * 
	 * @modifies this
	 * @throws Exception
	 */
	public void runTest() throws Exception {
		IBuildStrategy buildStrategy = repository.getBuildStrategy();
		
	    compilable = buildStrategy.build();
	    
	    if (compilable == Compilable.YES) {
	    	
	    	try {
	    		testResult = buildStrategy.runTest();
	    		testAborted = false;
	    	} catch (InterruptedException e) {
	    		testResult = null;
	    		testAborted = true;
	    	}
	    	
	    } else {
	    	testResult = null;
	    	testAborted = false;
	    }
	}

	/**
     * Makes a copy of this IntermediateRevision.
     * 
     * @return a deep copy of this MixedRevision.
     * @throws Exception 
     */
    public IntermediateRevision makeCopy() throws Exception {
    	IntermediateRevision copy = new IntermediateRevision(base, successor, 
    			repository, clonedRepository);
    	copy.delta = delta;
    	copy.compilable = compilable;
    	copy.testAborted = testAborted;
    	
    	if (testResult == null) {
    		copy.testResult = null;
    	} else {
    		Set<String> allTests = testResult.getAllTests();
    		Set<String> failedTests = testResult.getFailedTests();
    		copy.testResult = new TestResult(allTests, failedTests);
    	}
    	
    	return copy;
    }
    
    /**
     * Restores base Revision to its original state ie. 
     * clears delta, Compilable and TestResult data.
     * 
     * @modifies this, file system
     * @throws Exception 
     */
    public void restoreBaseRevision() throws Exception {    		
		for (DiffFile diffFile : delta) {
			String filename = diffFile.getFileName();
	    	DiffType type = diffFile.getDiffType();
	    	
	    	if (type == DiffType.DELETED || type == DiffType.MODIFIED) {
	    		boolean restoreFileSuccessful = repository.discardFileChange(filename);
	    		
	    		if (!restoreFileSuccessful) {
	    			throw new Exception("restore file " + filename + " in " + 
	    					repository.getDirectory() + " unsuccessful");
	    		}
	    	} else {
	    		Util.deleteFile(filename, repository.getDirectory());
	    	}
		}
    	
    	resetFields();
    }
    
    public Revision getBaseRevision() {
    	return base;
    }
    
    public Revision getSuccessorRevision() {
    	return successor;
    }
    
    public Set<DiffFile> getTotalDelta() {
    	return totalDelta;
    }
    
    public Set<DiffFile> getDelta() {
    	return delta;
    }
    
    public Compilable isCompilable() {
        return compilable;
    }
    
    public boolean hasTestAborted() {
		return testAborted;
	}

	public TestResult getTestResult() {
        return testResult;
    }
    
    private void checkoutRevision(Revision revision, IRepository repository) throws Exception {
		boolean checkoutSuccessful = repository.checkoutCommit(revision.getCommitID());
		
	    if (!checkoutSuccessful) {
	    	throw new Exception("check out commit " + revision.getCommitID() + 
	    			" from " + repository.getDirectory() + " unsuccessful");
	    }
	}

	private void resetFields() {
    	delta = null;
    	compilable = Compilable.UNKNOWN;
    	testAborted = false;
        testResult = null;
    }
    
    @Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		IntermediateRevision mr = (IntermediateRevision) other;

		return base.equals(mr.base) && successor.equals(mr.successor)
				&& delta.equals(mr.delta)
				&& compilable == mr.compilable && testAborted == mr.testAborted
				&& ((testResult == null && mr.testResult == null) ||
						(testResult != null && testResult.equals(mr.testResult)));
	}

	@Override
	public int hashCode() {
		int hashCode = 11 * base.hashCode() + 13 * successor.hashCode()
						+ 17 * delta.hashCode() + 19 * compilable.hashCode();
		
		if (testAborted) {
			hashCode += 23;
		}
		
		if (testResult != null) {
			hashCode += 29 * testResult.hashCode();
		}
		
		return hashCode;
	}
	
	@Override
    public String toString() {
        String str = "Base: " + base.getCommitID() + "\n";
        str += "Successor: " + successor.getCommitID() + "\n";
        str += "Delta:\n";
        
        for (DiffFile diffFile : delta) {
    		str += diffFile.toString() + "\n";
    	}
        
        str += "Compilable: " + compilable + "\n";
        
        if (compilable == Compilable.YES) {

        	if (testAborted) {
        		str += "Test Aborted\n";
        	} else {
        		str += testResult.toString();
        	}
        }
        
        return str;
    }
}
