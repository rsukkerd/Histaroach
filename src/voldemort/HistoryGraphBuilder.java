package voldemort;

import java.io.BufferedInputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HistoryGraphBuilder 
{
	private static final File DIR = new File("/homes/iws/rsukkerd/workspace/github/voldemort");
	private static final String[] TEST_CMD = {"ant", "junit"};
	
	public static HistoryGraph buildHistoryGraph() 
	{
		HistoryGraph historyGraph = new HistoryGraph();
		
		TestResultNode masterNode = getTestResultNode("master");
		
		Queue<TestResultNode> q = new LinkedList<TestResultNode>();
    	q.add(masterNode);
    	
    	Set<String> visited = new HashSet<String>();
    	visited.add("master");
    	
    	while (!q.isEmpty()) 
    	{
    		TestResultNode next = q.poll();
    		    		
    		// process next
    		String currCommit = next.getCommit();
    		List<String> parentCommits = getParentCommits(currCommit);
    		
    		List<TestResultNode> parents = new ArrayList<TestResultNode>();
    		    		
    		for (String parentCommit : parentCommits) 
    		{    			
				TestResultNode parent = getTestResultNode(parentCommit);
				parents.add(parent);
				
    			if (!visited.contains(parentCommit)) 
    			{
    				q.add(parent);
    				visited.add(parentCommit);
    			}
    		}
    		// add node to graph
    		historyGraph.addNode(next, parents);
    		
    		// add information about bug fixes
    		for (TestResultNode parent : parents)
    		{
    			for (String test : parent.getTestResult().getFailures())
    			{
    				if (!next.getTestResult().getFailures().contains(test) && 
    						next.getTestResult().getAllTests().contains(test))
    				{
    					historyGraph.addBugFix(test, next);
    				}
    			}
    		}
    	}
		
		return historyGraph;
	}
	
	/**
	 * @return list of parent commits of this commit
	 */
	private static List<String> getParentCommits(String commit) 
	{	
		checkoutCommit(commit);
		
    	ProcessBuilder logBuilder = new ProcessBuilder("git", "log", "--parents", "-1");
    	logBuilder.directory(DIR);
    	
    	List<String> parentCommits = new ArrayList<String>();
    	
    	try 
    	{
			Process logProcess = logBuilder.start();
			
			try 
			{
				// make current thread waits until this process terminates
				logProcess.waitFor();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
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
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
    	
    	return parentCommits;
    }
	
	/**
	 * @return TestResultNode representing the commit
	 */
	private static TestResultNode getTestResultNode(String commit)
	{
		TestResult result = getTestResult(commit);
		TestResultNode testResultNode = new TestResultNode(commit, result);
		
		return testResultNode;
	}
	
	private static void checkoutCommit(String commit)
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
	 * @return TestResult of the commit
	 */
	private static TestResult getTestResult(String commit)
	{
		checkoutCommit(commit);
		
		ProcessBuilder runtestBuilder = new ProcessBuilder(TEST_CMD);
		runtestBuilder.directory(DIR);
        
		TestResult testResult = null;
		
		try 
		{
			Process runtestProcess = runtestBuilder.start();
			
			try 
			{
				// make current thread waits until this process terminates
				runtestProcess.waitFor();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			testResult = getTestResultHelper(runtestProcess);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
        
        return testResult;
	}
	
	/**
	 * get TestResult from output on screen
	 * @return TestResult
	 */
	private static TestResult getTestResultHelper(Process process)
	{
		InputStreamReader tempReader = new InputStreamReader(
				new BufferedInputStream(process.getInputStream()));
		
		BufferedReader reader = new BufferedReader(tempReader);
		
		String line = new String();
		Set<String> allTests = new HashSet<String>();
		Set<String> failures = new HashSet<String>();
    	
		try 
		{
			while ((line = reader.readLine()) != null) 
			{
				Pattern testPattern = Pattern.compile("\\s*[junit] Running (\\S+)");
				Matcher testMatcher = testPattern.matcher(line);
				
				Pattern failPattern = Pattern.compile("\\s*[junit] Test (\\S+) FAILED");
				Matcher failMatcher = failPattern.matcher(line);
				
				if (testMatcher.find()) 
				{
					allTests.add(testMatcher.group(1));
				} 
				else if (failMatcher.find()) 
				{
					failures.add(failMatcher.group(1));
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		TestResult testResult = new TestResult(allTests, failures);
		
		return testResult;
	}
}
