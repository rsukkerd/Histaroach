package common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Revision represents a state of a particular revision.
 * 
 * Revision has access to its repository, a commit ID, 
 * a list of its parents and their corresponding diff files.
 * 
 * Revision knows its compilable state and its test result. 
 * These information can be set by other classes.
 */
public class Revision implements Serializable {
    /**
     * serial version ID
     */
    private static final long serialVersionUID = -4044614975764741642L;

    public enum COMPILABLE {
        YES, NO, UNKNOWN
    }
    
    private final Repository repository;
    private final String commitID;
    /**
     * index mapping : a parent revision -> a list of files that are different
     * between the parent and this revision
     **/
    private final List<Revision> parents;
    private final List<List<DiffFile>> diffFiles;
    private COMPILABLE compilable;
    private/* @Nullable */TestResult testResult;

    /**
     * Create a revision 
     * Initially, compilable flag is unknown and test result is null
     */
    public Revision(Repository repository, String commitID) {
        this.repository = repository;
        this.commitID = commitID;
        this.parents = new ArrayList<Revision>();
        this.diffFiles = new ArrayList<List<DiffFile>>();
        compilable = COMPILABLE.UNKNOWN;
        testResult = null;
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
     * add a parent revision and its corresponding diff files
     */
    public void addParent(Revision parent, List<DiffFile> files) {
    	parents.add(parent);
    	diffFiles.add(files);
    }
    
    /**
     * @return list of parents of this revision
     */
    public List<Revision> getParents() {
    	return parents;
    }

    /**
     * @return list of diff files corresponding to the given parent
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
    	int i = parents.indexOf(parent);
    	assert i >= 0;
        return diffFiles.get(i);
    }
    
    /**
     * set this revision's compilable flag
     */
    public void setCompilableFlag(COMPILABLE compilable) {
    	this.compilable = compilable;
    }
    
    /**
     * set this revisions's test result
     */
    public void setTestResult(/*@Nullable*/TestResult testResult) {
    	this.testResult = testResult;
    }
    
    /**
     * combine the given test result into this revision's test result
     * @requires result != null
     */
    public void addTestResult(/*@NonNull*/TestResult result) {
    	if (this.testResult == null) {
    		this.testResult = result;
    	} else {
    		for (String test : result.getAllTests()) {
    			this.testResult.addTest(test);
    		}
    		for (String failedTest : result.getFailedTests()) {
    			this.testResult.addFailedTest(failedTest);
    		}
    	}
    }
    
    public COMPILABLE isCompilable() {
    	return compilable;
    }
    
    public TestResult getTestResult() {
    	return testResult;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository)
                && commitID.equals(revision.commitID)
                && parents.equals(revision.parents)
                && diffFiles.equals(revision.diffFiles)
                && compilable == revision.compilable
                && ((testResult == null && revision.testResult == null) || testResult
                        .equals(revision.testResult));
    }

    @Override
    public int hashCode() {
        int code = 11 * repository.hashCode() + 13 * commitID.hashCode() 
        		+ 17 * parents.hashCode() + 19 * diffFiles.hashCode() + 23 * compilable.hashCode();
        if (testResult != null) {
            code += 29 * testResult.hashCode();
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
        } else {
            result += "unknown\n";
        }
        
        for (int i = 0; i < parents.size(); i++) {
        	result += "parent : " + parents.get(i).getCommitID() + "\n";
        	result += "diff files :\n";
        	List<DiffFile> files = diffFiles.get(i);
        	
        	for (DiffFile file : files) {
                result += file + "\n";
            }
        }

        return result;
    }
}
