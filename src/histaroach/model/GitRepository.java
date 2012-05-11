package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * GitRepository is for git version control. 
 * 
 * GitRepository is immutable.
 */
public class GitRepository implements IRepository, Serializable {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -3708734056581889395L;

	private static final String[] LOG_COMMAND = { "git", "log", "--pretty=format:%h %p" };

	private final File directory;
	private final IBuildStrategy buildStrategy;
	
	/**
	 * Creates a GitRepository.
	 */
	public GitRepository(File directory, IBuildStrategy buildStrategy) {
		this.directory = directory;
		this.buildStrategy = buildStrategy;
	}
	
	@Override
	public File getDirectory() {
		return directory;
	}

	@Override
	public IBuildStrategy getBuildStrategy() {
		return buildStrategy;
	}

	@Override
	public boolean checkoutCommit(String commitID) throws IOException,
			InterruptedException {
		Process p = Util.runProcess(
                new String[] { "git", "checkout", commitID }, directory);
        return p.exitValue() == 0;
	}

	@Override
	public boolean discardFileChange(String filename) throws IOException,
			InterruptedException {
		File file = new File(directory.getPath() + File.separatorChar + filename);
		
		Process p = Util.runProcess(
	            new String[] { "git", "checkout", filename }, directory);
	    return !file.exists() || p.exitValue() == 0;
	}

	@Override
	public List<DiffFile> getDiffFiles(String baseCommitID,
			String otherCommitID) throws IOException, InterruptedException {
		List<DiffFile> diffFiles = new ArrayList<DiffFile>();

        Process p = Util.runProcess(new String[] { "git", "diff",
                "--name-status", otherCommitID, baseCommitID }, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));

        List<String> lines = Util.getStreamContent(reader);

        for (String line : lines) {
            String[] tokens = line.split("\\s");

            DiffType type;
            if (tokens[0].equals("A")) {
                type = DiffType.ADDED;
            } else if (tokens[0].equals("M")) {
                type = DiffType.MODIFIED;
            } else {
                type = DiffType.DELETED;
            }

            DiffFile diffFile = new DiffFile(type, tokens[1]);

            diffFiles.add(diffFile);
        }

        return diffFiles;
	}

	@Override
	public HistoryGraph buildHistoryGraph(String startCommitID,
			String endCommitID) throws Exception {
		HistoryGraph hGraph = new HistoryGraph();
		
		boolean checkoutCommitSuccessful = checkoutCommit(startCommitID);
		
		if (!checkoutCommitSuccessful) {
			throw new Exception("git checkout commit " + startCommitID + " unsuccessful");
		}
		
        Process logProcess = Util.runProcess(LOG_COMMAND, directory);

        BufferedReader logReader = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));

        List<String> lines = Util.getStreamContent(logReader);
        
        // revision's commit id -> list of its parents' ids
        Map<String, List<String>> commitIDToParentsIDs = getCommitIDToParentsIDs(lines, endCommitID);
        
        // revision's commit id -> count of its parent edges
        Map<String, Integer> parentEdgeCounter = getParentEdgeCounter(commitIDToParentsIDs);
        
        // revision's commit id -> revision object
        Map<String, Revision> revisions = new HashMap<String, Revision>();
        
        while (!parentEdgeCounter.isEmpty()) {
        	// find a commit id node that has no parent edge
        	String commitID = "";
        	
        	for (Map.Entry<String, Integer> entry : parentEdgeCounter.entrySet()) {
        		if (entry.getValue() == 0) {
        			commitID = entry.getKey();
        			break;
        		}
        	}
        	
        	assert !commitID.isEmpty();
        	
        	parentEdgeCounter.remove(commitID);
        	
        	// create a revision
        	Map<Revision, List<DiffFile>> parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
        	List<String> parentsIDs = commitIDToParentsIDs.get(commitID);
        	
        	for (String parentID : parentsIDs) {
        		
        		if (!revisions.containsKey(parentID)) {
        			// parentID is not in the range [startCommitID, endCommitID]
        			// ignore this parentID
        			continue;
        		}
        		
        		Revision parent = revisions.get(parentID);
        		List<DiffFile> diffFiles = getDiffFiles(commitID, parentID);
        		
        		parentToDiffFiles.put(parent, diffFiles);
        	}
        	
        	Revision revision = new Revision(this, commitID, parentToDiffFiles);
        	hGraph.addRevision(revision);
        	
        	// print progress to stdout
        	System.out.println(revision);
        	
        	revisions.put(commitID, revision);
        	
        	// update parentEdgeCounter
        	decrementParentEdgeCounts(commitIDToParentsIDs, commitID, parentEdgeCounter);
        }
		
		return hGraph;
	}
	
	/**
	 * Returns a graph where each node is a commit ID and each edge 
	 * goes from child commit ID to parent commit ID.
	 * 
	 * @return a map from commit ID to the list of its parents' IDs.
	 */
	private Map<String, List<String>> getCommitIDToParentsIDs(List<String> lines, 
			String endCommitID) {
        Map<String, List<String>> commitIDToParentsIDs = new HashMap<String, List<String>>();
        
        for (String line : lines) {
        	String[] hashes = line.split(" ");

            String commitID = hashes[0];
            List<String> parentsIDs = new ArrayList<String>();
            
            for (int i = 1; i < hashes.length; i++) {
            	String parentID = hashes[i];
            	parentsIDs.add(parentID);
            }
            
            commitIDToParentsIDs.put(commitID, parentsIDs);
            
            if (commitID.equals(endCommitID)) {
            	break;
            }
        }
        
        return commitIDToParentsIDs;
	}
	
	/**
	 * Returns a parent edge counter.
	 * 
	 * @return a map from commit ID to the number of its parent edges.
	 */
	private Map<String, Integer> getParentEdgeCounter(Map<String, List<String>> commitIDToParentsIDs) {
		Map<String, Integer> parentEdgeCounter = new HashMap<String, Integer>();
		
		// commit ids in the range [startCommitID, endCommitID]
		Set<String> commitIDs = commitIDToParentsIDs.keySet();
		
		for (String commitID : commitIDs) {
			List<String> parentsIDs = commitIDToParentsIDs.get(commitID);
			int extraParentEdges = 0;
			
			// eliminate parent edges that are not in the range [startCommitID, endCommitID]
			for (String parentID : parentsIDs) {
				if (!commitIDs.contains(parentID)) {
					extraParentEdges++;
				}
			}
			
			int parentEdges = parentsIDs.size() - extraParentEdges;
			parentEdgeCounter.put(commitID, parentEdges);
		}
		
		return parentEdgeCounter;
	}
	
	/**
	 * Decrements parent edge counts of commit ID nodes whose parent 
	 * has just been added to HistoryGraph.
	 * 
	 * @modifies parentEdgeCounter
	 */
	private void decrementParentEdgeCounts(Map<String, List<String>> commitIDToParentsIDs, 
			String removedParentEdge, 
			Map<String, Integer> parentEdgeCounter) {
		
		for (String commitID : commitIDToParentsIDs.keySet()) {
			List<String> parentsIDs = commitIDToParentsIDs.get(commitID);
			
			if (parentsIDs.contains(removedParentEdge)) {
				int parentEdgeCount = parentEdgeCounter.get(commitID);
				parentEdgeCounter.put(commitID, parentEdgeCount - 1);
			}
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        GitRepository repository = (GitRepository) other;
        
        return directory.equals(repository.directory) 
        		&& buildStrategy.equals(repository.buildStrategy);
	}
	
	@Override
	public int hashCode() {
		return 11 * directory.hashCode() + 13 * buildStrategy.hashCode();
	}
	
	@Override
	public String toString() {
		return "repository location: " + directory.getPath().toString();
	}
}
