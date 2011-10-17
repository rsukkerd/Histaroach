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
		ParallelFixesFinder finder = new ParallelFixesFinder();
		Map<String, Set<TestResultNode>> map = finder.findParallelFixes();
		
		printParallelFixes(map);
	}
	
	private static void printParallelFixes(Map<String, Set<TestResultNode>> map)
	{
		for (String bug : map.keySet())
		{
			System.out.println("Test : " + bug);
			System.out.println("Commits that fix this bug in parallel:");
			
			for (TestResultNode node : map.get(bug))
			{
				System.out.println("\t" + node);
			}
			
			System.out.println();
		}
	}

}
