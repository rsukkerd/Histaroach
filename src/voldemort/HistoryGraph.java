package voldemort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HistoryGraph 
{
	private final Map<TestResultNode, List<TestResultNode>> nodeToParents;
	private final Map<String, List<TestResultNode>> bugToNodes;
	
	public HistoryGraph() 
	{
		nodeToParents = new HashMap<TestResultNode, List<TestResultNode>>();
		bugToNodes = new HashMap<String, List<TestResultNode>>();
	}
	
	/**
	 * add new node to this HistoryGraph
	 * @param node : node to add
	 * @param parents : list of parents of the node
	 */
	public void addNode(TestResultNode node, List<TestResultNode> parents) 
	{
		nodeToParents.put(node, parents);
	}
	
	/**
	 * @param node
	 * @return list of parents of node
	 */
	public List<TestResultNode> getParents(TestResultNode node)
	{
		return nodeToParents.get(node);
	}
	
	/**
	 * add bug fix information
	 * @param bug : test that fails in parent node but passes in child node
	 * @param node : child node that fixes the bug
	 */
	public void addBugFix(String bug, TestResultNode node)
	{
		if (!bugToNodes.containsKey(bug))
		{
			List<TestResultNode> nodes = new ArrayList<TestResultNode>();
			nodes.add(node);
			bugToNodes.put(bug, nodes);
		}
		else
		{
			bugToNodes.get(bug).add(node);
		}
	}
	
	/**
	 * @return iterator over the bugs that get fixed
	 */
	public Iterator<String> getBugIterator()
	{
		return bugToNodes.keySet().iterator();
	}
	
	/**
	 * @param bug
	 * @return list of nodes that fix the bug
	 */
	public List<TestResultNode> getNodesThatFixBug(String bug)
	{
		return bugToNodes.get(bug);
	}
}
