package common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Revision represents a state of a particular commit. 
 * 
 * Revision has access to its Repository. 
 * It contains the following public methods: 
 *  - getRepository(): returns a Repository 
 *  - getCommitID(): returns a commit ID 
 *  - getParents(): returns a set of parents 
 *  - getDiffFiles(parent): returns a list of DiffFiles 
 *    corresponding to a parent 
 *  - isCompilable(): returns a COMPIABLE state 
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

    private final Repository repository;
    private final String commitID;
    private final Map<Revision, List<DiffFile>> parentToDiffFiles;
    private Compilable compilable;
    private /*@Nullable*/ TestResult testResult;

    /**
     * Create a Revision. 
     * COMPILABLE state and TestResult are populated in this constructor.
     * 
     * @throws Exception 
     */
    public Revision(Repository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles) 
    		throws Exception {
    	this.repository = repository;
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	
    	compilable = Compilable.UNKNOWN;
        testResult = null;
        
        boolean checkoutCommitSuccessful = repository.checkoutCommit(commitID);
        
        if (checkoutCommitSuccessful) {
        	populateTestResult();
        } else {
    		throw new Exception("git checkout commit " + commitID + " unsuccessful");
    	}
    }
    
    /**
     * Create a Revision. 
     * COMPILABLE state and TestResult are given.
     */
    public Revision(Repository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles, 
    		Compilable compilable, TestResult testResult) {
    	this.repository = repository;
    	this.commitID = commitID;
    	this.compilable = compilable;
    	this.testResult = testResult;
    	this.parentToDiffFiles = parentToDiffFiles;
    }

    /**
     * Get a Repository.
     * 
     * @return a Repository of this Revision
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * Get a commit ID.
     * 
     * @return a commit ID of this Revision
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * Get a set of parents.
     * 
     * @return a set of parents of this Revision
     */
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * Get a list of DiffFiles corresponding to a parent.
     * 
     * @return a list of DiffFiles corresponding to the parent, 
     *         null if the parent is not a parent of this Revision
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }

    /**
     * Get a COMPILABLE state.
     * 
     * @return a COMPILABLE state of this Revision
     */
    public Compilable isCompilable() {
        return compilable;
    }

    /**
     * Get a TestResult.
     * 
     * @return a TestResult of this Revision
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * Compile, run tests, and parse the test results.
     * 
     * @modifies this
     * @throws Exception 
     */
    private void populateTestResult() throws Exception {
    	BuildStrategy buildStrategy = repository.getBuildStrategy();
    	
    	Pair<Compilable, TestResult> result = buildStrategy.runTestViaShellScript(commitID);
    	compilable = result.getFirst();
		testResult = result.getSecond();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;
        
        boolean boolRepo = repository.equals(revision.repository);
        boolean boolCommitID = commitID.equals(revision.commitID);
        boolean boolParentToDiffFiles = parentToDiffFiles.equals(revision.parentToDiffFiles);
        boolean boolCompilable = compilable == revision.compilable;
        boolean boolTestResult = (testResult == null && revision.testResult == null) 
        						|| (testResult != null && testResult.equals(revision.testResult));
        
        return boolRepo && boolCommitID && boolParentToDiffFiles && boolCompilable && boolTestResult;
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        			+ 17 * parentToDiffFiles.hashCode() + 19 * compilable.hashCode();
        if (testResult != null) {
            code += 23 * testResult.hashCode();
        }

        return code;
    }

    @Override
    public String toString() {
        String result = "commit : " + commitID + "\n";
        result += "compilable : ";
        if (compilable == Compilable.YES) {
            result += "yes\n";
            result += testResult.toString();
        } else if (compilable == Compilable.NO) {
            result += "no\n";
        } else if (compilable == Compilable.UNKNOWN) {
            result += "unknown\n";
        } else {
            result += "no build file\n";
        }
                
        for (Revision parent : parentToDiffFiles.keySet()) {
        	result += "parent : " + parent.commitID + "\n";
        	result += "diff files :\n";
        	
        	List<DiffFile> diffFiles = parentToDiffFiles.get(parent);
        	for (DiffFile diffFile : diffFiles) {
        		result += diffFile + "\n";
        	}
        }

        return result;
    }
}
