package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Repository represents a git repository.
 * Repository contains a reference to the directory associated with this Repository.
 */
public class Repository {
    public static final String[] LOG_COMMAND = { "git", "log", "--pretty=format:%h %p" };
    public static final String[] JUNIT_COMMAND = { "ant", "junit" };
    public static final String SINGLE_TEST_COMMAND = "ant junit-test -Dtest.name=";

    private final File directory;

    /**
     * create a repository instance
     * 
     * @param pathname : full path to the repository directory
     */
    public Repository(String pathname) {
        directory = new File(pathname);
    }

    /**
     * @return directory of this repository
     */
    public File getDirectory() {
        return directory;
    }

    /**
     * Checks out a particular commit from this repository.
     * 
     * @param commitID
     * @return exit value of 'git checkout' process
     */
    public int checkoutCommit(String commitID) {
        Process p = Util.runProcess(new String[] { "git", "checkout", commitID }, directory);
        return p.exitValue();
    }

    /**
     * @param childCommitID
     * @param parentCommitID
     * @return a list of diff files between childCommit and parentCommit
     * @throws IOException
     */
    public List<String> getDiffFiles(String childCommitID, String parentCommitID) throws IOException {
        List<String> diffFiles = new ArrayList<String>();

        Process p = Util.runProcess(new String[] { "git", "diff", "--name-status", childCommitID, parentCommitID }, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            diffFiles.add(line);
        }
        
        return diffFiles;
    }

    /**
     * build a history graph instance containing revisions from startCommit to endCommit
     * 
     * @param startCommitID
     * @param endCommitID
     * @return a history graph of this repository
     * @throws IOException
     */
    public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) throws IOException {
        HistoryGraph hGraph = new HistoryGraph();

        int exitValue = checkoutCommit(startCommitID);
        assert (exitValue == 0);

        Process logProcess = Util.runProcess(LOG_COMMAND, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            String[] hashes = line.split(" ");

            String commitID = hashes[0];
            Map<String, List<String>> parentIDToDiffFiles = getParentIDToDiffFiles(commitID, hashes);
            
            Revision revision = new Revision(this, commitID, parentIDToDiffFiles);
            hGraph.addRevision(revision);
            
            /* print progress to standard output */
            System.out.println(revision);

            if (commitID.equals(endCommitID)) {
                break;
            }
        }

        return hGraph;
    }
    
    /**
     * @return a mapping : a parentCommitID -> a list of diff files
     * @throws IOException
     */
    private Map<String, List<String>> getParentIDToDiffFiles(String commitID, String[] hashes) throws IOException {
    	Map<String, List<String>> parentIDToDiffFiles = new HashMap<String, List<String>>();

        if (hashes.length > 1) {
            for (int i = 1; i < hashes.length; i++) {
                String parentID = hashes[i];
                List<String> diffFiles = getDiffFiles(commitID, parentID);

                parentIDToDiffFiles.put(parentID, diffFiles);
            }
        }
        
        return parentIDToDiffFiles;
    }
    
    /**
	 * @return a mapping : a test name (bug) -> a list of all its fixes
	 */
	public Map<String, List<BugFix>> getAllBugFixes(HistoryGraph historyGraph) {
		Map<String, List<BugFix>> map = new HashMap<String, List<BugFix>>();
		Iterator<Revision> itr = historyGraph.getRevisionIterator();
		
		while (itr.hasNext()) {
			Revision revision = itr.next();
			List<String> fixedBugs = new ArrayList<String>(); // find all bugs that this revision fixed
			Set<Revision> parents = historyGraph.getParents(revision);
			
			for (Revision parent : parents) {
				for (String failedTest : parent.getTestResult().getFailedTests()) {
					if (revision.getTestResult().pass(failedTest)) {
						fixedBugs.add(failedTest);
					}
				}
			}
			
			for (String test : fixedBugs) {
				// BFS to find all consecutive revisions, start from this revision, that fail this test
				Queue<Revision> queue = new LinkedList<Revision>();
                BugFix bugFix = new BugFix(test, revision);

                Set<Revision> visited = new HashSet<Revision>();

                for (Revision parent : parents) {
                    if (parent.getTestResult().fail(test)) {
                        queue.add(parent);
                        bugFix.addFailedRevision(parent);
                        visited.add(parent);
                    }
                }

                while (!queue.isEmpty()) {
                    Revision nextRevision = queue.poll();

                    for (Revision parent : historyGraph.getParents(nextRevision)) {
                        if (!visited.contains(parent) && parent.getTestResult().fail(test)) {
                            queue.add(parent);
                            bugFix.addFailedRevision(parent);
                            visited.add(parent);
                        }
                    }
                }
                
                if (map.containsKey(test)) {
                	map.get(test).add(bugFix);
                } else {
                	List<BugFix> list = new ArrayList<BugFix>();
                	list.add(bugFix);
                	map.put(test, list);
                }
			}
		}
		
		return map;
	}
	
	/**
	 * @return a ParallelBugFixes instance of a given bug
	 */
	public ParallelBugFixes getParallelBugFixes(HistoryGraph historyGraph, String bug, List<BugFix> allFixes) {
		Set<BugFix> parallelFixes = new HashSet<BugFix>();
		
		for (int i = 0; i < allFixes.size() - 1; i++) {
			for (int j = i + 1; j < allFixes.size(); j++) {
				BugFix fix_A = allFixes.get(i);
                BugFix fix_B = allFixes.get(j);

                Revision revision_A = fix_A.getPassedRevision();
                Revision revision_B = fix_B.getPassedRevision();

                if (historyGraph.parallel(revision_A, revision_B)) {
                	parallelFixes.add(fix_A);
                	parallelFixes.add(fix_B);
                }
            }
        }
		
		return new ParallelBugFixes(bug, parallelFixes);
	}

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Repository repository = (Repository) other;
        return directory.equals(repository.directory);
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }

    @Override
    public String toString() {
        return "repository location : " + directory.getAbsolutePath();
    }
}
