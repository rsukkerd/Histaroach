package common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Revision represents a state of a particular commit. Revision has access to
 * its repository, commit ID, a set of its parents and their corresponding diff
 * files, compilable state, and test result. 
 * Revision is immutable.
 */
public class Revision implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -4044614975764741642L;

    public enum COMPILABLE {
        YES, NO, UNKNOWN, NO_BUILD_FILE
    }

    private final Repository repository;
    private final String commitID;
    /**
     * mapping : parent revision -> a list of files that are different
     * between the parent and this revision
     */
    private final Map<Revision, List<DiffFile>> parentToDiffFiles;
    private COMPILABLE compilable;
    private/* @Nullable */TestResult testResult;

    /**
     * Create a revision.
     * Compilable state and test result are populated in this constructor.
     * 
     * @throws Exception 
     */
    public Revision(Repository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles) 
    		throws Exception {
    	this.repository = repository;
    	this.commitID = commitID;
    	this.parentToDiffFiles = parentToDiffFiles;
    	
    	compilable = COMPILABLE.UNKNOWN;
        testResult = null;
        
        populateTestResult();
    }
    
    /**
     * Create a revision.
     * Compilable state and test result are given.
     */
    public Revision(Repository repository, String commitID, Map<Revision, List<DiffFile>> parentToDiffFiles, 
    		COMPILABLE compilable, TestResult testResult) {
    	this.repository = repository;
    	this.commitID = commitID;
    	this.compilable = compilable;
    	this.testResult = testResult;
    	this.parentToDiffFiles = parentToDiffFiles;
    }

    /**
     * @return repository of this revision
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @return commit ID of this revision
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * @return set of parents of this revision
     */
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * @return list of diff files corresponding to the given parent, 
     * null if parent is not a parent of this revision
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }

    /**
     * @return compilable flag of this revision
     */
    public COMPILABLE isCompilable() {
        return compilable;
    }

    /**
     * @return test result of this revision
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * check out this revision, compile and run all tests
     * 
     * @modifies this
     * @throws Exception 
     */
    private void populateTestResult() throws Exception {
    	int exitValue = repository.checkoutCommit(commitID);
        
    	if (exitValue == 0) {
	        Pair<COMPILABLE, TestResult> pair = repository.run(repository.antJunit, commitID);
	        compilable = pair.getFirst();
	        testResult = pair.getSecond();
    	} else {
    		throw new Exception("git checkout commit unsuccessful");
    	}
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
        if (compilable == COMPILABLE.YES) {
            result += "yes\n";
            result += testResult.toString();
        } else if (compilable == COMPILABLE.NO) {
            result += "no\n";
        } else if (compilable == COMPILABLE.UNKNOWN) {
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
