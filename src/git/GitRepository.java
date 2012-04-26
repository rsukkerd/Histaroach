package git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import util.Util;

import common.BuildStrategy;
import common.DiffFile;
import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.DiffFile.DiffType;
import common.Revision.Compilable;

/**
 * GitRepository is an implementation of Repository Interface. 
 * 
 * For git version control. 
 * 
 * GitRepository is immutable.
 */
public class GitRepository implements Repository, Serializable {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -3708734056581889395L;

	private static final String[] LOG_COMMAND = { "git", "log", "--pretty=format:%h %p" };

	private final File directory;
	private final BuildStrategy buildStrategy;
	
	/**
	 * Creates a GitRepository.
	 */
	public GitRepository(File directory, BuildStrategy buildStrategy) {
		this.directory = directory;
		this.buildStrategy = buildStrategy;
	}
	
	@Override
	public File getDirectory() {
		return directory;
	}

	@Override
	public BuildStrategy getBuildStrategy() {
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
		
		if (checkoutCommitSuccessful) {
	        Process logProcess = Util.runProcess(LOG_COMMAND, directory);
	
	        BufferedReader reader = new BufferedReader(new InputStreamReader(
	                logProcess.getInputStream()));
	
	        List<String> lines = Util.getStreamContent(reader);
	        
	        // parent id -> a list of its children's ids
	        Map<String, List<String>> parentIDToChildrenIDs = new HashMap<String, List<String>>();
	        // child id -> a list of its parents' ids
	        Map<String, List<String>> childIDToParentsIDs = new HashMap<String, List<String>>();
	        
	        // special case : the start revision has no children
	        if (!lines.isEmpty()) {
	        	String line = lines.get(0);
	        	String[] hashes = line.split(" ");
	        	
	        	String topID = hashes[0];
	        	parentIDToChildrenIDs.put(topID, new ArrayList<String>());
	        }
	        
	        for (String line : lines) {
	        	String[] hashes = line.split(" ");
	
	            String childID = hashes[0];
	            List<String> parentsIDs = new ArrayList<String>();
	            
	            for (int i = 1; i < hashes.length; i++) {
	            	String parentID = hashes[i];
	            	parentsIDs.add(parentID);
	            	
	            	if (parentIDToChildrenIDs.containsKey(parentID)) {
	            		parentIDToChildrenIDs.get(parentID).add(childID);
	            	} else {
	            		List<String> childrenIDs = new ArrayList<String>();
	            		childrenIDs.add(childID);
	            		parentIDToChildrenIDs.put(parentID, childrenIDs);
	            	}
	            }
	            
	            childIDToParentsIDs.put(childID, parentsIDs);
	            
	            if (childID.equals(endCommitID)) {
	            	break;
	            }
	        }
	        
	        // commit id -> number of its parents
	        Map<String, Integer> incomingEdgeCounter = new HashMap<String, Integer>();
	        
	        for (String childID : childIDToParentsIDs.keySet()) {
	        	int incomingEdges = 0;
	        	List<String> parentsIDs = childIDToParentsIDs.get(childID);
	        	for (String parentID : parentsIDs) {
	        		if (childIDToParentsIDs.containsKey(parentID)) {
	        			incomingEdges++;
	        		}
	        	}
	        	
	        	incomingEdgeCounter.put(childID, incomingEdges);
	        }
	        
	        Set<String> noIncomingEdge = new HashSet<String>();
	        
	        for (String commitID : incomingEdgeCounter.keySet()) {
	        	int count = incomingEdgeCounter.get(commitID);
	        	if (count == 0) {
	        		noIncomingEdge.add(commitID);
	        	}
	        }
	        
	        // commit id -> its revision object
	        Map<String, Revision> revisions = new HashMap<String, Revision>();
	        
	        while (!noIncomingEdge.isEmpty()) {
	        	Iterator<String> itr = noIncomingEdge.iterator();
	        	String commitID = itr.next();
	        	noIncomingEdge.remove(commitID);
	        	
	        	Map<Revision, List<DiffFile>> parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
	        	
	        	List<String> parentsIDs = childIDToParentsIDs.get(commitID);
	        	for (String parentID : parentsIDs) {
	        		Revision parent;
	        		if (revisions.containsKey(parentID)) {
	        			parent = revisions.get(parentID);
	        		} else {
	        			// dummy revision of parent
	        			parent = new Revision(parentID, new HashMap<Revision, List<DiffFile>>(), 
	        					Compilable.UNKNOWN, null);
	        		}
	        		
	        		List<DiffFile> diffFiles = getDiffFiles(commitID, parentID);
	        		
	        		parentToDiffFiles.put(parent, diffFiles);
	        	}
	        	
	        	Revision revision = new Revision(this, commitID, parentToDiffFiles);
	        	hGraph.addRevision(revision);
	        	
	        	/* print progress to standard out */
	        	System.out.println(revision);
	        	
	        	revisions.put(commitID, revision);
	        	
	        	List<String> childrenIDs = parentIDToChildrenIDs.get(commitID);
	        	
	        	for (String childID : childrenIDs) {
	        		int edges = incomingEdgeCounter.get(childID);
	        		incomingEdgeCounter.put(childID, edges - 1);
	        		
	        		if (edges - 1 == 0) {
	        			noIncomingEdge.add(childID);
	        		}
	        	}
	        }
		} else {
			throw new Exception("git checkout commit " + startCommitID + " unsuccessful");
		}
		
		return hGraph;
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
