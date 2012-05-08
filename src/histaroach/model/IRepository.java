package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;

import java.io.File;
import java.io.IOException;
import java.util.List;


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
     * @return a list of DiffFiles between baseCommit and otherCommit.
     * @throws InterruptedException 
     * @throws IOException 
     */
	public List<DiffFile> getDiffFiles(String baseCommitID,
            String otherCommitID) throws IOException, InterruptedException;

	/**
	 * Builds a HistoryGraph containing Revisions from startCommit 
	 * to endCommit.
	 * 
	 * @return a HistoryGraph containing Revisions from startCommit 
	 *         to endCommit.
	 * @throws Exception
	 */
	public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) 
			throws Exception;
}
