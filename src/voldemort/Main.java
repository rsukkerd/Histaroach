package voldemort;

import java.util.Map;
import java.util.Set;

public class Main 
{
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		ParallelFixesFinder finder = new ParallelFixesFinder(args[0]);
		Map<String, Set<BugFixPair>> map = finder.findParallelFixes();
		
		printParallelFixes(map);
	}
	
	private static void printParallelFixes(Map<String, Set<BugFixPair>> map)
	{
		for (String bug : map.keySet())
		{
			System.out.println("Test : " + bug);
			System.out.println("Commits that fix this bug in parallel:");
			
			for (BugFixPair pair : map.get(bug))
			{
				TestResultNode nodeFail = pair.getNodeFail();
				TestResultNode nodePass = pair.getNodePass();
				
				System.out.println("\t" + "node fixed : " + nodePass.toString());
				System.out.println("\t" + "node failed : " + nodeFail.toString());
			}
			
			System.out.println();
		}
	}

}
