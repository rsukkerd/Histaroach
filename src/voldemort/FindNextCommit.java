package voldemort;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

public class FindNextCommit 
{
	private static final File DIR = new File("/homes/iws/rsukkerd/workspace/github/voldemort");
	private static final String TARGET = "8c07e32d40adc1f98d9a8edc6e05f1f00719a04f";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		int count = 0;
		String headCommit = "master";
		
		Queue<String> queue = new LinkedList<String>();
		queue.add(headCommit);
		
		Set<String> visited = new HashSet<String>();
		visited.add(headCommit);
		
		while (!queue.isEmpty()) 
		{
			String currCommit = queue.poll();
			
			count++;
			System.out.println(count + " : " + currCommit);
			
			if (currCommit.equals(TARGET))
			{
				System.out.println("The next commit is " + queue.poll());
				break;
			}
			
			List<String> parentCommits = getParentCommits(currCommit);
			
			for (String parentCommit : parentCommits)
			{
				if (!visited.contains(parentCommit))
				{
					queue.add(parentCommit);
					visited.add(parentCommit);
				}
			}
		}
	}
	
	public static void checkoutCommit(String commit)
	{
		ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout", commit);
		checkoutBuilder.directory(DIR);
		
	    try 
	    {
			Process checkoutProcess = checkoutBuilder.start();
			
	    	try 
	    	{
	    		// make current thread waits until this process terminates
				checkoutProcess.waitFor();
			} 
	    	catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		} 
	    catch (IOException e) 
	    {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * @return list of parent commits (Strings) of this commit
	 */
	public static List<String> getParentCommits(String commit) 
	{	
		checkoutCommit(commit);
		
    	ProcessBuilder logBuilder = new ProcessBuilder("git", "log", "--parents", "-1");
    	logBuilder.directory(DIR);
    	
    	List<String> parentCommits = new ArrayList<String>();
    	
    	try 
    	{
			Process logProcess = logBuilder.start();
			
			BufferedReader reader = new BufferedReader(new
					InputStreamReader(logProcess.getInputStream()));
			
			String parentsLine = reader.readLine();
			
			Scanner scanner = new Scanner(parentsLine);
			scanner.next(); // "commit"
			scanner.next(); // this commit
			while (scanner.hasNext()) // parent commits
			{ 
				parentCommits.add(scanner.next());
			}	
			
			try 
			{
				// make current thread waits until this process terminates
				logProcess.waitFor();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
    	    	
    	return parentCommits;
    }

}
