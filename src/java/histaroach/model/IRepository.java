package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;

import java.io.File;
import java.io.IOException;
import java.util.Set;


/**
 * Repository represents a project repository. 
 * 
 * Repository is associated with a particular directory 
 * and a BuildStrategy.
 */
public interface IRepository {
	
	public File getDirectory();
	
	public IBuildStrategy getBuildStrategy();
	
	/**
     * Checks out a commit into the working directory.
     * 
     * @return true if the method successfully checked out the commit.
     * @throws InterruptedException 
     * @throws IOException 
     */
	public boolean checkoutCommit(String commitID) throws IOException, InterruptedException;
	
	/**
	 * Discards any change made in a file.
	 * 
	 * @return true if the method successfully restored the file 
	 *         if there was any change.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean discardFileChange(String filename) throws IOException, InterruptedException;
	
	/**
     * @return a set of DiffFiles between referenceCommit and otherCommit, 
     *         where the DiffType is from the point of view of referenceCommit.
     * @throws InterruptedException 
     * @throws IOException 
     */
	public Set<DiffFile> getDiffFiles(String referenceCommitID,
            String otherCommitID) throws IOException, InterruptedException;

	/**
	 * Builds a HistoryGraph containing Revisions from startCommit 
	 * to endCommit.
	 * 
	 * @requires startCommitID and endCommitID are each at least 7-character long.
	 * @return a HistoryGraph containing Revisions from startCommit 
	 *         to endCommit.
	 * @throws Exception
	 */
	public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) 
			throws Exception;
}
