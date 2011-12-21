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
     * @return mapping from bug to BugFixes that fix the bug in parallel
     */
    public static Map<String, Set<BugFix>> findParallelFixes(HistoryGraph historyGraph) 
    {
    	ParallelFixesFinder.historyGraph = historyGraph;
    	
    	Map<String, Set<BugFix>> map = new HashMap<String, Set<BugFix>>();
    	
    	Iterator<String> bugItr = historyGraph.getBugIterator();
    	
    	while (bugItr.hasNext())
    	{
    		String bug = bugItr.next();
    		List<BugFix> bugFixes = historyGraph.getBugFixList(bug);
    		
    		for (int i = 0; i < bugFixes.size() - 1; i++) 
    		{	
    			for (int j = i + 1; j < bugFixes.size(); j++) 
    			{	
    				BugFix fix_A = bugFixes.get(i);
    				BugFix fix_B = bugFixes.get(j);
    				
    				TestResultNode node_A = fix_A.getNodePass();
    				TestResultNode node_B = fix_B.getNodePass();
    				    				
    				if (areParallel(node_A, node_B)) 
    				{
    					if (!map.containsKey(bug))
    					{
    						Set<BugFix> parallelFixes = new HashSet<BugFix>();
    						parallelFixes.add(fix_A);
    						parallelFixes.add(fix_B);
    						
    						map.put(bug, parallelFixes);
    					}
    					else
    					{
    						map.get(bug).add(fix_A);
    						map.get(bug).add(fix_B);
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
