package voldemort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HistoryGraph 
{
	private final Map<TestResultNode, List<TestResultNode>> nodeToParents;
	private final Map<String, List<BugFix>> bugToFixList;
	
	public HistoryGraph() 
	{
		nodeToParents = new HashMap<TestResultNode, List<TestResultNode>>();
		bugToFixList = new HashMap<String, List<BugFix>>();
	}
	
	/**
	 * add new node to this HistoryGraph
	 * @param node : node to be added
	 * @param parents : list of parents of the node
	 */
	public void addNode(TestResultNode node, List<TestResultNode> parents) 
	{
		nodeToParents.put(node, parents);
	}
	
	/**
	 * @param node : node to get parents
	 * @return list of parents of the node
	 */
	public List<TestResultNode> getParents(TestResultNode node)
	{
		return nodeToParents.get(node);
	}
	
	/**
	 * @return iterator over all nodes
	 */
	/* package private */ Iterator<TestResultNode> getNodeIterator()
	{
		return nodeToParents.keySet().iterator();
	}

	/**
	 * add bug fix information
	 * @param bug : test that fails in parent node but passes in child node
	 * @param fix : a node that fixes the bug and a list of 
	 * 				consecutive nodes that have the bug
	 */
	public void addBugFix(String bug, BugFix fix)
	{
		if (!bugToFixList.containsKey(bug))
		{
			List<BugFix> list = new ArrayList<BugFix>();
			list.add(fix);
			bugToFixList.put(bug, list);
		}
		else
		{
			bugToFixList.get(bug).add(fix);
		}
	}

	/**
	 * @return iterator over the bugs that get fixed
	 */
	public Iterator<String> getBugIterator()
	{
		return bugToFixList.keySet().iterator();
	}
	
	/**
	 * @param bug
	 * @return list of BugFixes of the bug
	 */
	public List<BugFix> getBugFixList(String bug)
	{
		return bugToFixList.get(bug);
	}
}
