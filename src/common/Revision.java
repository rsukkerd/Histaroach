package common;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ant.AntBuildStrategy;

/**
 * Revision represents a state of a particular commit. 
 * 
 * Revision has access to its Repository, commit ID, 
 * a set of its parents and their corresponding DiffFile's, 
 * COMPILABLE state, and TestResult. 
 * 
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
    	
    	compilable = COMPILABLE.UNKNOWN;
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
    		COMPILABLE compilable, TestResult testResult) {
    	this.repository = repository;
    	this.commitID = commitID;
    	this.compilable = compilable;
    	this.testResult = testResult;
    	this.parentToDiffFiles = parentToDiffFiles;
    }

    /**
     * @return a Repository of this Revision
     */
    public Repository getRepository() {
        return repository;
    }

    /**
     * @return a commit ID of this Revision
     */
    public String getCommitID() {
        return commitID;
    }

    /**
     * @return a set of parents of this Revision
     */
    public Set<Revision> getParents() {
        return parentToDiffFiles.keySet();
    }

    /**
     * @return a list of DiffFile's corresponding to the given parent, 
     *         null if parent is not a parent of this Revision
     */
    public List<DiffFile> getDiffFiles(Revision parent) {
        return parentToDiffFiles.get(parent);
    }

    /**
     * @return a COMPILABLE state of this Revision
     */
    public COMPILABLE isCompilable() {
        return compilable;
    }

    /**
     * @return a TestResult of this Revision
     */
    public TestResult getTestResult() {
        return testResult;
    }

    /**
     * Compile and run all unit tests.
     * 
     * @modifies this
     * @throws Exception 
     */
    private void populateTestResult() throws Exception {
    	Pair<COMPILABLE, TestResult> pair = null;
    	BuildStrategy buildStrategy = repository.getBuildStrategy();
    		
		if (buildStrategy.ensureNoHaltOnFailure()) {
			// pair = buildStrategy.runTest(commitID);
			pair = buildStrategy.runTestViaShellScript(commitID);
			
			if (pair != null) { // force dependency
				if (!repository.discardFileChange(AntBuildStrategy.BUILD_XML)) {
					throw new Exception("discard change in build.xml unsuccessful");
				}
				
				compilable = pair.getFirst();
				testResult = pair.getSecond();
			}
		} else {
			throw new Exception("ensure haltonfailure=no unsuccessful");
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
