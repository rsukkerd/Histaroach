package voldemort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HistoryGraph 
{
	private final Map<TestResultNode, List<TestResultNode>> nodeToParents;
	private final Map<String, List<BugFixPair>> bugToPairs;
	
	public HistoryGraph() 
	{
		nodeToParents = new HashMap<TestResultNode, List<TestResultNode>>();
		bugToPairs = new HashMap<String, List<BugFixPair>>();
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
	 * @param node : node to get parents
	 * @return list of parents of the node
	 */
	public List<TestResultNode> getParents(TestResultNode node)
	{
		return nodeToParents.get(node);
	}
	
	/* package private */ Iterator<TestResultNode> getNodeIterator()
	{
		return nodeToParents.keySet().iterator();
	}

	/**
	 * add bug fix information
	 * @param bug : test that fails in parent node but passes in child node
	 * @param pair : pair of <parent node, child node>
	 */
	public void addBugFix(String bug, BugFixPair pair)
	{
		if (!bugToPairs.containsKey(bug))
		{
			List<BugFixPair> pairs = new ArrayList<BugFixPair>();
			pairs.add(pair);
			bugToPairs.put(bug, pairs);
		}
		else
		{
			bugToPairs.get(bug).add(pair);
		}
	}

	/**
	 * @return iterator over the bugs that get fixed
	 */
	public Iterator<String> getBugIterator()
	{
		return bugToPairs.keySet().iterator();
	}
	
	/**
	 * @param bug
	 * @return list of <nodeFail, nodePass> pairs of the bug
	 */
	public List<BugFixPair> getBugFixPairs(String bug)
	{
		return bugToPairs.get(bug);
	}
}
