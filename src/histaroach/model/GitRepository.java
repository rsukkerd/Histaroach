package histaroach.model;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

	private static final int HASH_LENGTH = 7;
	private static final String[] LOG_COMMAND = 
		{ "git", "log", "--pretty=format:%h %p", "--date-order" };

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
		// discard changes in working directory before check out commit
		Util.runProcess(
				new String[] { "git", "checkout", "--", "./" }, directory);
		
		Process checkoutProcess = Util.runProcess(
                new String[] { "git", "checkout", commitID }, directory);
        return checkoutProcess.exitValue() == 0;
	}

	@Override
	public boolean discardFileChange(String filename) throws IOException,
			InterruptedException {
		File file = new File(directory.getPath() + File.separatorChar + filename);
		
		Process checkoutProcess = Util.runProcess(
	            new String[] { "git", "checkout", filename }, directory);
	    return !file.exists() || checkoutProcess.exitValue() == 0;
	}

	@Override
	public Set<DiffFile> getDiffFiles(String baseCommitID,
			String otherCommitID) throws IOException, InterruptedException {
		Set<DiffFile> diffFiles = new HashSet<DiffFile>();

        Process diffProcess = Util.runProcess(new String[] { "git", "diff",
                "--name-status", otherCommitID, baseCommitID }, directory);
        
        List<String> lines = Util.getInputStreamContent(diffProcess.getInputStream());

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
	public HistoryGraph buildHistoryGraph(String startCommitID, String endCommitID) 
			throws Exception {
		String shortStartCommitID = startCommitID.substring(0, HASH_LENGTH);
		String shortEndCommitID = endCommitID.substring(0, HASH_LENGTH);
		
		HistoryGraph hGraph = new HistoryGraph();
		
		// check out startCommit, which will be the new HEAD
		boolean checkoutCommitSuccessful = checkoutCommit(shortStartCommitID);
		
		if (!checkoutCommitSuccessful) {
			throw new Exception("git checkout commit " + shortStartCommitID + " unsuccessful");
		}
		
		// "git log" shows HEAD's history
        Process logProcess = Util.runProcess(LOG_COMMAND, directory);
        
        /*
         * each line is formatted as either: 
         * {commit id} {parent1's commit id} ... {parentN's commit id} 
         * or 
         * {commit id} if a commit has no parent.
         */
        List<String> lines = Util.getInputStreamContent(logProcess.getInputStream());
        
        /*
         * The commitIDToParentsIDs map maps a revision's commit id to a list of 
         * its parents' commit ids. 
         * 
         * The commitIDToParentsIDs map is a graph representation of HEAD's revision history. 
         * Each node in this graph is a commit id string. Each edge in this graph goes 
         * from a child to a parent. 
         * 
         * This graph will not be modified.
         */
        Map<String, List<String>> commitIDToParentsIDs = getCommitIDToParentsIDs(lines, shortEndCommitID);
        
        /*
         * The parentEdgeCounter map maps a revision's commit id to a count 
         * of its parent edges. 
         * 
         * To construct a Revision object, all of its parents must already exist. 
         * 
         * The parentEdgeCounter map is a counter of the remaining parent edges 
         * of each node in the commitIDToParentsIDs graph. A parent edge represents 
         * a parent that has not been constructed. If a node has one or more parent- 
         * edges, a Revision corresponding to that node cannot be constructed yet. 
         * 
         * This counter will be modified at each while-loop iteration in this method. 
         * At each iteration, an entry in the counter that has 0 count will be removed, 
         * and a Revision corresponding to the node in that entry will be constructed. 
         * 
         * Once a Revision is constructed, the decrementParentEdgeCounts function 
         * searches over all remaining entries in the counter and decrements a count 
         * of each node whose parent has just been constructed.
         */
        Map<String, Integer> parentEdgeCounter = getParentEdgeCounter(commitIDToParentsIDs);
        
        /*
         * The revisions map maps a revision's commit id to a corresponding 
         * Revision object. 
         * 
         * This map is used, when constructing a Revision object, for getting 
         * a parent Revision from a parent commit id string.
         */
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
        	
        	// create a Revision object
        	Map<Revision, Set<DiffFile>> parentToDiffFiles = new HashMap<Revision, Set<DiffFile>>();
        	List<String> parentsIDs = commitIDToParentsIDs.get(commitID);
        	
        	for (String parentID : parentsIDs) {
        		
        		if (!revisions.containsKey(parentID)) {
        			// parentID is not in the range [startCommitID, endCommitID]
        			// ignore this parentID
        			continue;
        		}
        		
        		Revision parent = revisions.get(parentID);
        		Set<DiffFile> diffFiles = getDiffFiles(commitID, parentID);
        		
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
