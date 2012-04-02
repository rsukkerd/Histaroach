package common;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Repository represents a software repository. 
 * 
 * Repository has access to its associated directory and build strategy. 
 * It contains methods to check out a commit into the working directory, 
 * get files that are different between any 2 commits, and build a graph 
 * structure representing the revision history of the repository. 
 * 
 * Repository is immutable.
 */
public interface Repository {

	/**
     * @return a directory of this Repository
     */
	public File getDirectory();
	
	/**
	 * @return a BuildStrategy of this Repository
	 */
	public BuildStrategy getBuildStrategy();
	
	/**
     * Checks out a given commit from this Repository
     * 
     * @return an exit value of the checkout process
     * @throws InterruptedException 
     * @throws IOException 
     */
	public int checkoutCommit(String commitID) throws IOException, InterruptedException;
	
	/**
     * @return a list of DiffFile's between childCommit and parentCommit
     * @throws InterruptedException 
     * @throws IOException 
     */
	public List<DiffFile> getDiffFiles(String childCommitID,
            String parentCommitID) throws IOException, InterruptedException;

	/**
	 * Build a HistoryGraph containing revisions from startCommit 
	 * to endCommit
	 * 
	 * @return a HistoryGraph of this Repository
	 * @throws Exception
	 */
	public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) 
			throws Exception;
}
