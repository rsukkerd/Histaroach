package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.util.Pair;

import java.io.Serializable;
import java.util.HashMap;
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
    
    /* used in equals(other) and hashCode() methods only */
    private final Map<String, List<DiffFile>> parentIDToDiffFiles;

    /**
     * Creates a Revision. 
     * Compilable state and TestResult are populated in this constructor.
     * 
     * @throws Exception 
     */
    public Revision(IRepository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles) 
    		throws Exception {
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	
    	parentIDToDiffFiles = new HashMap<String, List<DiffFile>>();
    	deriveParentIDToDiffFiles();
        
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
    	deriveParentIDToDiffFiles();
    }
    
    /**
     * Derives parentIDToDiffFiles from parentToDiffFiles.
     */
    private void deriveParentIDToDiffFiles() {
    	
    	for (Revision parent : parentToDiffFiles.keySet()) {
    		String parentID = parent.getCommitID();
    		List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
    		
    		parentIDToDiffFiles.put(parentID, diffFiles);
    	}
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
