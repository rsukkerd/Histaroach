package common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

import voldemort.VoldemortTestResult;

public class Revision {
	private final Repository repository;
	private final String commitID;
	private final TestResult testResult;
	private final Map<String, List<String>> diffFiles;
	
	public Revision(Repository repository, String commitID, Map<String, List<String>> parentIDToDiffFiles) {
		this.repository = repository;
		this.commitID = commitID;
		testResult = generateTestResult();
		diffFiles = parentIDToDiffFiles;
	}
	
	public String getCommitID() {
		return commitID;
	}
	
	public Set<String> getParentIDs() {
		return diffFiles.keySet();
	}
	
	public TestResult getTestResult() {
		return testResult;
	}
	
	/**
	 * run all junit tests on this revision to generate TestResult
     * @return TestResult of this revision
     */
    public TestResult generateTestResult() {
        int exitValue = repository.checkoutCommit(commitID);
        TestResult testResult = null;

        if (exitValue != 0) {
        	System.out.println("'git checkout' process returns non-zero exit value");
            // TODO: Do something sensible -- graceful recovery.
            return testResult;
        }

        ProcessBuilder junitBuilder = new ProcessBuilder(Repository.JUNIT_COMMAND);
        junitBuilder.directory(repository.getDirectory());

        try {
            Process junitProcess = junitBuilder.start();

            BufferedReader stdOutputReader = new BufferedReader(
                    new InputStreamReader(junitProcess.getInputStream()));

            BufferedReader stdErrorReader = new BufferedReader(
                    new InputStreamReader(junitProcess.getErrorStream()));

            testResult = new VoldemortTestResult(commitID, stdOutputReader,
                    stdErrorReader);

            try {
                // make current thread waits until this process terminates
                junitProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return testResult;
    }
	
	public List<String> getDiffFiles(String parentID) {
		return diffFiles.get(parentID);
	}
	
	@Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Revision revision = (Revision) other;

        return repository.equals(revision.repository) && commitID.equals(revision.commitID) 
        		&& testResult.equals(revision.testResult) 
        		&& diffFiles.equals(revision.diffFiles);
    }
	
	@Override
	public int hashCode() {
		int code = 11 * repository.hashCode() + 13 * commitID.hashCode() + 17 * testResult.hashCode()
					+ 19 * diffFiles.hashCode();
		return code;
	}
	
	@Override
	public String toString() {
		String result = "commit : " + commitID + "\n";
		result += testResult.toString();
        
        for (String parentID : diffFiles.keySet()) {
        	result += "parent : " + parentID + "\n";
        	result += "diff files :\n";
        	List<String> files = getDiffFiles(parentID);
        	
        	for (String file : files) {
        		result += file + "\n";
        	}
        }

        return result;
	}
}
