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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HistoryGraphBuilder 
{
	private static final String[] TEST_CMD = {"ant", "junit"};
		
	/**
	 * @param repoDir : full path to directory of the repository
	 * @return HistoryGraph of the repository
	 */
	public static HistoryGraph buildHistoryGraph(String repoDir) 
	{
		File directory = new File(repoDir);
		
		HistoryGraph historyGraph = new HistoryGraph();
		
		int commitCount = 0;
		
		TestResultNode masterNode = getTestResultNode(directory, "master");
		
		Queue<TestResultNode> q = new LinkedList<TestResultNode>();
    	q.add(masterNode);
    	
    	Set<String> visited = new HashSet<String>();
    	visited.add("master");
    	
    	while (!q.isEmpty()) 
    	{
    		TestResultNode next = q.poll();
    		    		
    		// process 'next'
    		String currCommit = next.getCommit();
    		List<String> parentCommits = getParentCommits(directory, currCommit);
    		
    		List<TestResultNode> parents = new ArrayList<TestResultNode>();
    		    		
    		for (String parentCommit : parentCommits) 
    		{    			
				TestResultNode parent = getTestResultNode(directory, parentCommit);
				parents.add(parent);
				
    			if (!visited.contains(parentCommit)) 
    			{
    				q.add(parent);
    				visited.add(parentCommit);
    			}
    		}
    		// add 'next' to graph
    		historyGraph.addNode(next, parents);
    		
    		commitCount++;
    		printProgress(next, commitCount);
    	}
    	
    	addBugFixesInfo(historyGraph, masterNode);
    			
		return historyGraph;
	}
	
	/**
	 * add information about bug fixes to historyGraph
	 * @modifies historyGraph
	 */
	/*package*/ static void addBugFixesInfo(HistoryGraph historyGraph, TestResultNode masterNode)
	{
		Queue<TestResultNode> queue = new LinkedList<TestResultNode>();
    	queue.add(masterNode);
    	
    	Set<TestResultNode> visitedNodes = new HashSet<TestResultNode>();
    	visitedNodes.add(masterNode);
    	
    	while (!queue.isEmpty())
    	{
    		TestResultNode next = queue.poll();
    		
    		// find all fixed bugs in 'next'
    		Set<String> fixedBugs = new HashSet<String>();
    		
    		List<TestResultNode> parents = historyGraph.getParents(next);
    		for (TestResultNode parent : parents)
    		{
    			if (!visitedNodes.contains(parent))
    			{
    				queue.add(parent);
    				visitedNodes.add(parent);
    			}
    			
    			for (String bug : parent.getTestResult().getFailures())
    			{
    				if (next.pass(bug))
    				{
    					fixedBugs.add(bug);
    				}
    			}
    		}
    		
    		for (String bug : fixedBugs)
    		{
    			// BFS to find all consecutive nodes, start from 'next', that fail 'bug'
    			Queue<TestResultNode> failQueue = new LinkedList<TestResultNode>();
    			BugFix bugFix = new BugFix(next);
    			
    			Set<TestResultNode> subVisitedNodes = new HashSet<TestResultNode>();
    			
    			for (TestResultNode parent : parents)
    			{
    				if (parent.fail(bug))
    				{
    					failQueue.add(parent);
    					bugFix.addNodeFail(parent);
    					subVisitedNodes.add(parent);
    				}
    			}
    			
    			while (!failQueue.isEmpty())
    			{
    				TestResultNode failNode = failQueue.poll();
    				
    				for (TestResultNode p : historyGraph.getParents(failNode))
    				{
    					if (!subVisitedNodes.contains(p) && p.fail(bug))
    					{
    						failQueue.add(p);
    						bugFix.addNodeFail(p);
    						subVisitedNodes.add(p);
    					}
    				}
    			}
    			
    			historyGraph.addBugFix(bug, bugFix);
    		}	
    	}
	}
	
	private static void checkoutCommit(File directory, String commit)
	{
		ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout", commit);
		checkoutBuilder.directory(directory);
		
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
	private static List<String> getParentCommits(File directory, String commit) 
	{	
		checkoutCommit(directory, commit);
		
    	ProcessBuilder logBuilder = new ProcessBuilder("git", "log", "--parents", "-1");
    	logBuilder.directory(directory);
    	
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
	
	/**
	 * @return TestResultNode representing the commit
	 */
	private static TestResultNode getTestResultNode(File directory, String commit)
	{
		TestResult result = getTestResult(directory, commit);
		TestResultNode testResultNode = new TestResultNode(commit, result);
		
		return testResultNode;
	}
	
	/**
	 * @return TestResult of the commit
	 */
	/*package*/ static TestResult getTestResult(File directory, String commit)
	{
		checkoutCommit(directory, commit);
		
		ProcessBuilder runtestBuilder = new ProcessBuilder(TEST_CMD);
		runtestBuilder.directory(directory);
        
		TestResult testResult = null;
		
		try 
		{
			Process runtestProcess = runtestBuilder.start();
			testResult = getTestResultHelper(runtestProcess);
			
			try 
			{
				// make current thread waits until this process terminates
				runtestProcess.waitFor();
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
        
        return testResult;
	}
	
	/**
	 * @return TestResult from output from process
	 */
	private static TestResult getTestResultHelper(Process process)
	{
		BufferedReader stdOutputReader = new BufferedReader(new
				InputStreamReader(process.getInputStream()));
		
		BufferedReader stdErrorReader = new BufferedReader(new
				InputStreamReader(process.getErrorStream()));
		
		String line = new String();
		Set<String> allTests = new HashSet<String>();
		Set<String> failures = new HashSet<String>();
    	
		try 
		{
			while ((line = stdOutputReader.readLine()) != null) 
			{				
				Pattern allTestsPattern = Pattern.compile("\\s*\\[junit\\] Running (\\S+)");
				Matcher allTestsMatcher = allTestsPattern.matcher(line);
				
				if (allTestsMatcher.find()) 
				{
					allTests.add(allTestsMatcher.group(1));
				}
			}
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
		try 
		{
			while ((line = stdErrorReader.readLine()) != null) 
			{	
				Pattern failuresPattern = Pattern.compile("\\s*\\[junit\\] Test (\\S+) FAILED");
				Matcher failuresMatcher = failuresPattern.matcher(line);
				
				if (failuresMatcher.find()) 
				{
					failures.add(failuresMatcher.group(1));
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
	
	private static void printProgress(TestResultNode node, int count)
	{
		System.out.println("(" + count + ") " + node);
	}
}
