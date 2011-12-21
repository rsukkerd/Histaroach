package voldemort;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main 
{
	/**
	 * @param args[0] : full path of the repository directory
	 */
	public static void main(String[] args) 
	{
		HistoryGraph historyGraph = HistoryGraphBuilder.buildHistoryGraph(args[0]);
		Map<String, Set<BugFix>> bugFixMap = ParallelFixesFinder.findParallelFixes(historyGraph);
		
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
			
			List<BugFix> lists = historyGraph.getBugFixList(bug);
			for (BugFix list : lists)
			{
				System.out.println(list);
			}
		}
	}
	
	public static void printParallelFixes(Map<String, Set<BugFix>> bugFixMap)
	{
		for (String bug : bugFixMap.keySet())
		{
			System.out.println("Test : " + bug);
			System.out.println("Commits that fix this bug in parallel:");
			
			Set<BugFix> lists = bugFixMap.get(bug);
			for (BugFix list : lists)
			{
				System.out.println(list);
			}
		}
	}
}
