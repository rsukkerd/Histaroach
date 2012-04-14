package common;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Repository represents a project repository. 
 * 
 * Repository has access to its associated directory and BuildStrategy. 
 * It contains the following public methods: 
 *  - getDirectory(): returns a directory 
 *  - getBuildStrategy(): returns a BuildStrategy 
 *  - checkoutCommit(commitID): checks out a commit into the working directory 
 *  - discardFileChange(filename): discards any change made in a file 
 *  - getDiffFiles(baseCommitID, otherCommitID): returns a list of DiffFiles 
 *    between any 2 commits 
 *  - buildHistoryGraph(startCommitID, endCommitID): builds a HistoryGraph 
 *    containing Revisions from startCommit to endCommit.
 */
public interface Repository {

	/**
	 * Get a directory.
	 * 
     * @return a directory of this Repository
     */
	public File getDirectory();
	
	/**
	 * Get a BuildStrategy.
	 * 
	 * @return a BuildStrategy of this Repository
	 */
	public BuildStrategy getBuildStrategy();
	
	/**
     * Check out a commit into the working directory.
     * 
     * @return true if and only if the method successfully 
     *         checked out the commit
     * @throws InterruptedException 
     * @throws IOException 
     */
	public boolean checkoutCommit(String commitID) throws IOException, InterruptedException;
	
	/**
	 * Discard any change made in a file.
	 * 
	 * @return true if and only if the method successfully 
	 *         restored the file if there was any change
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean discardFileChange(String filename) throws IOException, InterruptedException;
	
	/**
	 * Get a list of DiffFiles between any 2 commits.
	 * 
     * @return a list of DiffFiles between baseCommit and otherCommit
     * @throws InterruptedException 
     * @throws IOException 
     */
	public List<DiffFile> getDiffFiles(String baseCommitID,
            String otherCommitID) throws IOException, InterruptedException;

	/**
	 * Build a HistoryGraph containing Revisions from startCommit 
	 * to endCommit.
	 * 
	 * @return a HistoryGraph containing Revisions from startCommit 
	 *         to endCommit
	 * @throws Exception
	 */
	public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) 
			throws Exception;
}
