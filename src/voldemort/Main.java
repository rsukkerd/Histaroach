package voldemort;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main 
{
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		HistoryGraph historyGraph = HistoryGraphBuilder.buildHistoryGraph(args[0]);
		Map<String, Set<BugFixPair>> bugFixMap = ParallelFixesFinder.findParallelFixes(historyGraph);
		
		System.out.println("ALL BUG FIXES");
		printAllFixes(historyGraph);
		
		System.out.println("PARALLEL BUG FIXES");
		printParallelFixes(bugFixMap);
	}
	
	public static void printAllFixes(HistoryGraph historyGraph)
	{
		Iterator<String> itr = historyGraph.getBugIterator();
		while (itr.hasNext())
		{
			String bug = itr.next();
			
			System.out.println("Test : " + bug);
			System.out.println("All commits that fix this bug:");
			
			List<BugFixPair> pairs = historyGraph.getBugFixPairs(bug);
			printBugFixPairs(pairs);
		}
	}
	
	public static void printParallelFixes(Map<String, Set<BugFixPair>> bugFixMap)
	{
		for (String bug : bugFixMap.keySet())
		{
			System.out.println("Test : " + bug);
			System.out.println("Commits that fix this bug in parallel:");
			
			Set<BugFixPair> setPairs = bugFixMap.get(bug);
			List<BugFixPair> listPairs = new ArrayList<BugFixPair>(setPairs);
			printBugFixPairs(listPairs);
		}
	}
	
	private static void printBugFixPairs(List<BugFixPair> pairs)
	{
		for (BugFixPair pair : pairs)
		{
			TestResultNode nodeFail = pair.getNodeFail();
			TestResultNode nodePass = pair.getNodePass();
			
			System.out.println("\t" + "node fixed : " + nodePass.toString());
			System.out.println("\t" + "node failed : " + nodeFail.toString());
		}
		
		System.out.println();
	}

}
