package voldemort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ParallelFixesFinder 
{
	private static HistoryGraph historyGraph;

	/**
	 * @param historyGraph
     * @return mapping from bug to BugFixPairs that fix the bug in parallel
     */
    public static Map<String, Set<BugFixPair>> findParallelFixes(HistoryGraph historyGraph) 
    {
    	ParallelFixesFinder.historyGraph = historyGraph;
    	
    	Map<String, Set<BugFixPair>> map = new HashMap<String, Set<BugFixPair>>();
    	
    	Iterator<String> bugItr = historyGraph.getBugIterator();
    	
    	while (bugItr.hasNext())
    	{
    		String bug = bugItr.next();
    		List<BugFixPair> pairs = historyGraph.getBugFixPairs(bug);
    		
    		for (int i = 0; i < pairs.size(); i++) 
    		{	
    			for (int j = i + 1; j < pairs.size(); j++) 
    			{	
    				BugFixPair pair_A = pairs.get(i);
    				BugFixPair pair_B = pairs.get(j);
    				
    				TestResultNode node_A = pair_A.getNodePass();
    				TestResultNode node_B = pair_B.getNodePass();
    				    				
    				if (areParallel(node_A, node_B)) 
    				{
    					if (!map.containsKey(bug))
    					{
    						Set<BugFixPair> parallelFixes = new HashSet<BugFixPair>();
    						parallelFixes.add(pair_A);
    						parallelFixes.add(pair_B);
    						
    						map.put(bug, parallelFixes);
    					}
    					else
    					{
    						map.get(bug).add(pair_A);
    						map.get(bug).add(pair_B);
    					}
    				}
    			}
    		}
    	}
    	
    	return map;
    }
    
    /**
     * @return true iff node_A is neither ancestor or descendant of node_B
     */
    private static boolean areParallel(TestResultNode node_A, TestResultNode node_B)
    {
    	return !node_A.equals(node_B) && 
    	!isAncestor(node_A, node_B, new HashSet<TestResultNode>()) && 
    	!isAncestor(node_B, node_A, new HashSet<TestResultNode>());
    }
    
    /**
     * @return true iff node_B is an ancestor of node_A
     */
    private static boolean isAncestor(TestResultNode node_A, TestResultNode node_B, Set<TestResultNode> visited)
    {
    	visited.add(node_A);
    	
    	List<TestResultNode> parents = historyGraph.getParents(node_A);
    	for (TestResultNode parent : parents)
    	{
    		if (parent.equals(node_B))
    		{
    			return true;
    		}
    	}
    	
    	for (TestResultNode parent : parents)
    	{
    		if (!visited.contains(parent))
    		{
	    		if (isAncestor(parent, node_B, visited))
	    		{
	    			return true;
	    		}
    		}
    	}
    	
    	return false;
    }
}
