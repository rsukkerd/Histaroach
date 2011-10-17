package voldemort;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParallelFixesFinder 
{
	private final HistoryGraph historyGraph;
	
	public ParallelFixesFinder()
	{
		historyGraph = HistoryGraphBuilder.buildHistoryGraph();
	}
	
	/**
     * @return mapping from bug to TestResultNodes that fix it in parallel
     */
    public Map<String, Set<TestResultNode>> findParallelFixes() 
    {
    	Map<String, Set<TestResultNode>> map = new HashMap<String, Set<TestResultNode>>();
    	
    	Iterator<String> bugItr = historyGraph.getBugIterator();
    	
    	while (bugItr.hasNext())
    	{
    		String bug = bugItr.next();
    		List<TestResultNode> nodes = historyGraph.getNodesThatFixBug(bug);
    		
    		for (int i = 0; i < nodes.size(); i++) 
    		{	
    			for (int j = i + 1; j < nodes.size(); j++) 
    			{	
    				TestResultNode node_A = nodes.get(i);
    				TestResultNode node_B = nodes.get(j);
    				    				
    				if (areParallel(node_A, node_B)) 
    				{
    					if (!map.containsKey(bug))
    					{
    						Set<TestResultNode> parallelFixes = new HashSet<TestResultNode>();
    						parallelFixes.add(node_A);
    						parallelFixes.add(node_B);
    						
    						map.put(bug, parallelFixes);
    					}
    					else
    					{
    						map.get(bug).add(node_A);
    						map.get(bug).add(node_B);
    					}
    					
    					System.out.println(node_A + " and " + node_B + " are parallel");
    				} 
    				else 
    				{
    					System.out.println(node_A + " and " + node_B + " are not parallel");
    				}
    			}
    		}
    	}
    	
    	return map;
    }
    
    /**
     * @return true iff node_A is neither ancestor or descendant of node_B
     */
    private boolean areParallel(TestResultNode node_A, TestResultNode node_B)
    {
    	return !isAncestor(node_A, node_B, new HashSet<TestResultNode>()) && 
    		!isAncestor(node_B, node_A, new HashSet<TestResultNode>());
    }
    
    /**
     * @return true iff node_B is an ancestor of node_A
     */
    private boolean isAncestor(TestResultNode node_A, TestResultNode node_B, Set<TestResultNode> visited)
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
