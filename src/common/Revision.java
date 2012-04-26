package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Pair;

/**
 * Revision represents a state of a particular commit. 
 * 
 * Revision contains the following public methods: 
 *  - getCommitID(): returns a commit ID 
 *  - getParents(): returns a set of parents 
 *  - getDiffFiles(parent): returns a list of DiffFiles 
 *    corresponding to a parent 
 *  - isCompilable(): returns a Compilable state 
 *  - getTestResult(): returns a TestResult.
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
    private final Map<Revision, List<DiffFile>> parentToDiffFiles;
    private Compilable compilable;
    private /*@Nullable*/ TestResult testResult;
    
    /* used in equals(other) and hashCode() methods only */
    private final Map<String, List<DiffFile>> parentIDToDiffFiles;

    /**
     * Creates a Revision. 
     * Compilable state and TestResult are populated in this constructor.
     * 
     * @throws Exception 
     */
    public Revision(Repository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles) 
    		throws Exception {
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	
    	parentIDToDiffFiles = new HashMap<String, List<DiffFile>>();
    	
    	for (Revision parent : parentToDiffFiles.keySet()) {
    		String parentID = parent.getCommitID();
    		List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
    		
    		parentIDToDiffFiles.put(parentID, diffFiles);
    	}
        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(commitID);
        
        if (checkoutCommitSuccessful) {
        	BuildStrategy buildStrategy = repository.getBuildStrategy();
        	
        	Pair<Compilable, TestResult> result = buildStrategy.runTestViaShellScript();
        	compilable = result.getFirst();
    		testResult = result.getSecond();
        } else {
    		throw new Exception("git checkout commit " + commitID + " unsuccessful");
    	}
    }
    
    /**
     * Creates a Revision. 
     * Compilable state and TestResult are given.
     */
    public Revision(String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles, 
    		Compilable compilable, TestResult testResult) {
    	this.commitID = commitID;
    	this.compilable = compilable;
    	this.testResult = testResult;
    	this.parentToDiffFiles = parentToDiffFiles;
    	
    	parentIDToDiffFiles = new HashMap<String, List<DiffFile>>();
    	
    	for (Revision parent : parentToDiffFiles.keySet()) {
    		String parentID = parent.getCommitID();
    		List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
    		
    		parentIDToDiffFiles.put(parentID, diffFiles);
    	}
    }

    /**
     * Returns a commit ID.
     * 
     * @return a commit ID of this Revision.
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * Returns a set of parents.
     * 
     * @return a set of parents of this Revision.
     */
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * Returns a list of DiffFiles corresponding to a parent.
     * 
     * @return a list of DiffFiles corresponding to the parent, 
     *         null if the parent is not a parent of this Revision.
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }

    /**
     * Returns a Compilable state.
     * 
     * @return a Compilable state of this Revision.
     */
    public Compilable isCompilable() {
        return compilable;
    }

    /**
     * Returns a TestResult.
     * 
     * @return a TestResult of this Revision.
     */
    public TestResult getTestResult() {
        return testResult;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;
        
        boolean boolCommitID = commitID.equals(revision.commitID);
        boolean boolParentIDToDiffFiles = parentIDToDiffFiles.equals(revision.parentIDToDiffFiles);
        boolean boolCompilable = compilable == revision.compilable;
        boolean boolTestResult = (testResult == null && revision.testResult == null) 
        						|| (testResult != null && testResult.equals(revision.testResult));
        
        return boolCommitID && boolParentIDToDiffFiles && boolCompilable && boolTestResult;
    }

    @Override
    public int hashCode() {
        int code = 13 * commitID.hashCode() + 17 * parentIDToDiffFiles.hashCode() 
        	+ 19 * compilable.hashCode();
        if (testResult != null) {
            code += 23 * testResult.hashCode();
        }

        return code;
    }

    @Override
    public String toString() {
        String result = "Commit: " + commitID + "\n";
        result += "Compilable: " + compilable + "\n";
        
        if (compilable == Compilable.YES) {
            result += testResult.toString();
        }
                
        for (Revision parent : parentToDiffFiles.keySet()) {
        	result += "Parent: " + parent.commitID + "\n";
        	result += "Diff Files:\n";
        	
        	List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	for (DiffFile diffFile : diffFiles) {
        		result += diffFile + "\n";
        	}
        }

        return result;
    }
}
