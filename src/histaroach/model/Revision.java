package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.util.Pair;

import java.io.Serializable;
import java.util.List;
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
    private final Map<Revision, List<DiffFile>> parentToDiffFiles;
    private Compilable compilable;
    private /*@Nullable*/ TestResult testResult;

    /**
     * Creates a Revision, whose Compilable state and 
     * TestResult are populated in this constructor. 
     * 
     * The caller to this constructor must pass in 
     * a Repository.
     * 
     * @throws Exception 
     */
    public Revision(IRepository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles) 
    		throws Exception {
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(commitID);
        
        if (!checkoutCommitSuccessful) {
        	throw new Exception("git checkout commit " + commitID + " unsuccessful");
        }
        
    	IBuildStrategy buildStrategy = repository.getBuildStrategy();
    	
    	Pair<Compilable, TestResult> result = buildStrategy.runTestViaShellScript();
    	compilable = result.getFirst();
		testResult = result.getSecond();
    }
    
    /**
     * Creates a Revision, whose Compilable state and 
     * TestResult are given.
     */
    public Revision(String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles, 
    		Compilable compilable, TestResult testResult) {
    	this.commitID = commitID;
    	this.compilable = compilable;
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
     * Returns a list of DiffFiles corresponding to a parent.
     * 
     * @return a list of DiffFiles corresponding to the parent, 
     *         null if the parent is not a parent of this Revision.
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }
    
    public Compilable isCompilable() {
        return compilable;
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
        
        // check equality of parents' IDs and DiffFiles
        Set<Revision> parents = parentToDiffFiles.keySet();
        Set<Revision> otherParents = other.parentToDiffFiles.keySet();
                
        for (Revision parent : parents) {
        	String parentID = parent.commitID;
        	List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	
        	boolean foundMatch = false;
        	
        	for (Revision otherParent : otherParents) {
        		String otherParentID = otherParent.commitID;
        		List<DiffFile> otherDiffFiles = other.parentToDiffFiles.get(otherParent);
        		
        		if (parentID.equals(otherParentID) && diffFiles.equals(otherDiffFiles)) {
        			foundMatch = true;
        			break;
        		}
        	}
        	
        	if (!foundMatch) {
        		return false;
        	}
        }
        
        return boolCommitID && boolCompilable && boolTestResult;
    }

    @Override
    public int hashCode() {
        int code = 11 * commitID.hashCode() + 13 * compilable.hashCode();
        
        if (testResult != null) {
            code += 17 * testResult.hashCode();
        }
        
        for (Revision parent : parentToDiffFiles.keySet()) {
        	String parentID = parent.commitID;
        	List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	
        	code += 19 * parentID.hashCode() + 23 * diffFiles.hashCode();
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
