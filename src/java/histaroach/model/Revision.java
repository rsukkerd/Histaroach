package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;


/**
 * Revision represents a particular commit in some Repository. 
 * 
 * Revision can be responsible for populating its Compilable 
 * state and TestResult at construction time. 
 * In this case, the caller to Revision's constructor must 
 * pass in a Repository. This Repository will be called from 
 * the constructor to compile the project, run tests and 
 * parse the test results. 
 * 
 * Otherwise, Revision's Compilable state and TestResult must 
 * be given by the caller at construction time. 
 * 
 * Revision is immutable.
 */
public class Revision implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -4044614975764741642L;

    public enum Compilable {
        YES, NO, UNKNOWN, NO_BUILD_FILE
    }

    private final String commitID;
    private final Map<Revision, Set<DiffFile>> parentToDiffFiles;
    private final Compilable compilable;
    private final boolean testAborted;
	private final /*@Nullable*/ TestResult testResult;
	
    /**
     * Creates a Revision, whose Compilable state and 
     * TestResult are populated in this constructor. 
     * 
     * The caller to this constructor must pass in 
     * a Repository.
     * 
     * @throws Exception 
     */
    public Revision(IRepository repository, String commitID, 
    		Map<Revision, Set<DiffFile>> parentToDiffFiles) throws Exception {
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(commitID);
        
        if (!checkoutCommitSuccessful) {
        	throw new Exception("git checkout commit " + commitID + " unsuccessful");
        }
        
    	IBuildStrategy buildStrategy = repository.getBuildStrategy();
    	
    	compilable = buildStrategy.build();
	    
	    if (compilable == Compilable.YES) {
	    	TestResult testResult;
	    	boolean testAborted;
	    	
	    	try {
	    		testResult = buildStrategy.runTest();
	    		testAborted = false;
	    	} catch (InterruptedException e) {
	    		// the process was killed forcibly
	    		testResult = null;
	    		testAborted = true;
	    	}
	    	
	    	this.testResult = testResult;
	    	this.testAborted = testAborted;
	    	
	    } else {
	    	testResult = null;
	    	testAborted = false;
	    }
    }
    
    /**
     * Creates a Revision, whose Compilable state and 
     * TestResult are given.
     */
    public Revision(String commitID, Map<Revision, Set<DiffFile>> parentToDiffFiles, 
    		Compilable compilable, boolean testAborted, TestResult testResult) {
    	this.commitID = commitID;
    	this.compilable = compilable;
    	this.testAborted = testAborted;
    	this.testResult = testResult;
    	this.parentToDiffFiles = parentToDiffFiles;
    }
    
    public String getCommitID() {
        return commitID;
    }
    
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * @return a set of DiffFiles corresponding to the parent, 
     *         null if the parent is not a parent of this Revision.
     */
    public Set<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }
    
    public Compilable isCompilable() {
        return compilable;
    }

    /**
	 * @return true if the process that runs tests has been aborted. 
	 *         If true, getTestResult() returns null.
	 */
	public boolean hasTestAborted() {
		return testAborted;
	}

	/**
     * @return a TestResult; null if this Revision is not compilable.
     */
    public TestResult getTestResult() {
        return testResult;
    }
    
    @Override
    public boolean equals(Object object) {
        if (object == null || !object.getClass().equals(this.getClass())) {
            return false;
        }

        Revision other = (Revision) object;
        
        boolean boolCommitID = commitID.equals(other.commitID);
        boolean boolCompilable = compilable == other.compilable;
        boolean boolTestResult = (testResult == null && other.testResult == null) 
        						|| (testResult != null && testResult.equals(other.testResult));
        boolean boolTestAborted = testAborted == other.testAborted;
        
        // check equality of parents' IDs and DiffFiles
        Set<Revision> parents = parentToDiffFiles.keySet();
        Set<Revision> otherParents = other.parentToDiffFiles.keySet();
                
        for (Revision parent : parents) {
        	String parentID = parent.commitID;
        	Set<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	
        	boolean foundMatch = false;
        	
        	for (Revision otherParent : otherParents) {
        		String otherParentID = otherParent.commitID;
        		Set<DiffFile> otherDiffFiles = other.parentToDiffFiles.get(otherParent);
        		
        		if (parentID.equals(otherParentID) && diffFiles.equals(otherDiffFiles)) {
        			foundMatch = true;
        			break;
        		}
        	}
        	
        	if (!foundMatch) {
        		return false;
        	}
        }
        
        return boolCommitID && boolCompilable && boolTestResult && boolTestAborted;
    }

    @Override
    public int hashCode() {
        int code = 11 * commitID.hashCode() + 13 * compilable.hashCode();
        
        if (testResult != null) {
            code += 17 * testResult.hashCode();
        }
        
        if (testAborted) {
        	code += 29;
        }
        
        for (Revision parent : parentToDiffFiles.keySet()) {
        	String parentID = parent.commitID;
        	Set<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	
        	code += 19 * parentID.hashCode() + 23 * diffFiles.hashCode();
        }

        return code;
    }

    @Override
    public String toString() {
        String result = "Commit: " + commitID + "\n";
        result += "Compilable: " + compilable + "\n";
        
        if (compilable == Compilable.YES) {
        	
        	if (testAborted) {
        		result += "Test Aborted\n";
        	} else {
        		result += testResult.toString();
        	}
        }
                        
        for (Revision parent : parentToDiffFiles.keySet()) {
        	result += "Parent: " + parent.commitID + "\n";
        	result += "Diff Files:\n";
        	
        	Set<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	for (DiffFile diffFile : diffFiles) {
        		result += diffFile + "\n";
        	}
        }

        return result;
    }
}
