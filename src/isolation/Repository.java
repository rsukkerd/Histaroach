package isolation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import voldemort.TestResultNode;

public class Repository 
{
	private final Map<TestResultNode, List<TestResultNode>> nodeToChildren;
	
	public Repository()
	{
		nodeToChildren = new HashMap<TestResultNode, List<TestResultNode>>();
	}
	
	/**
	 * add new node to this Repository
	 * @param node : node to be added
	 * @param parents : list of parents of the node
	 */
	public void addNode(TestResultNode node, List<TestResultNode> parents)
	{
		for (TestResultNode parent : parents)
		{
			if (!nodeToChildren.containsKey(parent))
			{
				List<TestResultNode> children = new ArrayList<TestResultNode>();
				children.add(node);
				nodeToChildren.put(parent, children);
			}
			else
			{
				nodeToChildren.get(parent).add(node);
			}
		}
	}
	
	/**
	 * @param node : node to get Iterator over its children
	 * @return Iterator over children of the node
	 */
	public Iterator<TestResultNode> getChildrenIterator(TestResultNode node)
	{
		return nodeToChildren.get(node).iterator();
	}
}
