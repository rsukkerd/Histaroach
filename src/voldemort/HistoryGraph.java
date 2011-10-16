package voldemort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryGraph 
{
	private Map<TestResultNode, List<TestResultNode>> nodeToParentsMap;
	private Map<TestResultNode, List<TestResultNode>> nodeToChildrenMap;
	
	public HistoryGraph() 
	{
		nodeToParentsMap = new HashMap<TestResultNode, List<TestResultNode>>();
		nodeToChildrenMap = new HashMap<TestResultNode, List<TestResultNode>>();
	}
	
	/**
	 * add new node to this HistoryGraph
	 */
	public void addNode(TestResultNode node, List<TestResultNode> parents) 
	{
		nodeToParentsMap.put(node, parents);
		nodeToChildrenMap.put(node, new ArrayList<TestResultNode>());
		
		for (TestResultNode parent : parents) 
		{
			if (!nodeToChildrenMap.containsKey(parent))
			{
				List<TestResultNode> children = new ArrayList<TestResultNode>();
				children.add(node);
				nodeToChildrenMap.put(parent, children);
			} else 
			{
				nodeToChildrenMap.get(parent).add(node);
			}
		}
	}
	
	public List<TestResultNode> getParents(TestResultNode node)
	{
		return nodeToParentsMap.get(node);
	}
}
