package isolation;

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

import voldemort.TestResult;
import voldemort.TestResultNode;

public final class RepositoryBuilder 
{
	private static final String[] ALL_TESTS_CMD = {"ant", "junit"};
	private static final String SINGLE_TEST_CMD = "ant junit-test -Dtest.name=";
	
	/**
	 * @param path : full path to directory of the repository
	 * @param commit : starting commit
	 * @return Repository
	 */
	public static Repository buildRepository(String path, String commit)
	{
		File directory = new File(path);
		Repository repository = new Repository();
		
		int commitCount = 0;
		
		TestResultNode startNode = getTestResultNode(directory, commit);
		
		Queue<TestResultNode> q = new LinkedList<TestResultNode>();
    	q.add(startNode);
    	
    	Set<String> visited = new HashSet<String>();
    	visited.add(commit);
    	
    	while (!q.isEmpty()) 
    	{
    		TestResultNode next = q.poll();
    		printInProgress(next);
    		    		
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
    			
    			// check for 1st fail and find diff
    			checkFailure(directory, next, parent);
    		}
    		// add 'next' to repository
    		repository.addNode(next, parents);
    		
    		commitCount++;
    		printCommitCompleted(next, commitCount);
    	}
		
		return repository;
	}
	
	/**
	 * checkout commit from the repository
	 * @param directory : directory of the repository
	 * @param commit : commit id
	 */
	public static void checkoutCommit(File directory, String commit)
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
	 * @param directory : directory of the repository
	 * @param commit : commit id
	 * @return list of parent commits (String's) of the commit
	 */
	public static List<String> getParentCommits(File directory, String commit) 
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
	 * @param directory : directory of the repository
	 * @param commit : commit id
	 * @return TestResultNode representing the commit
	 */
	public static TestResultNode getTestResultNode(File directory, String commit)
	{
		TestResult result = getTestResult(directory, commit, ALL_TESTS_CMD);
		TestResultNode testResultNode = new TestResultNode(commit, result);
		
		return testResultNode;
	}
	
	/**
	 * @param directory : directory of the repository
	 * @param commit : commit id
	 * @param command : test command
	 * @return TestResult of the commit
	 */
	public static TestResult getTestResult(File directory, String commit, String[] command)
	{
		checkoutCommit(directory, commit);
		
		ProcessBuilder runTestBuilder = new ProcessBuilder(command);
		runTestBuilder.directory(directory);
        
		TestResult testResult = null;
		
		try 
		{
			Process runTestProcess = runTestBuilder.start();
			testResult = getTestResultHelper(runTestProcess);
			
			try 
			{
				// make current thread waits until this process terminates
				runTestProcess.waitFor();
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
	 * @param process : 'ant junit' process
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
	
	private static void printInProgress(TestResultNode node)
	{
		System.out.println("Processing commit: " + node);
	}
	
	private static void printCommitCompleted(TestResultNode node, int count)
	{
		System.out.println("(" + count + ") " + node);
	}
	
	private static void checkFailure(File directory, TestResultNode node, TestResultNode parent)
	{		
		for (String test : node.getTestResult().getAllTests())
		{
			if (node.fail(test) && parent.pass(test))
			{
				List<String> changedFiles = getChangedFiles(directory, node.getCommit(), parent.getCommit());
				Difference diff = new Difference(node, parent, changedFiles);
				
				System.out.println("Failed Test: " + test);
				System.out.println(diff);
			}
		}
	}
	
	/**
	 * @return diff files between childCommit and parentCommit
	 */
	public static List<String> getChangedFiles(File directory, String childCommit, String parentCommit)
	{
		List<String> files = new ArrayList<String>();
		
		ProcessBuilder diffBuilder = new ProcessBuilder("git", "diff", "--name-only", childCommit, parentCommit);
		diffBuilder.directory(directory);
		
	    try 
	    {
			Process diffProcess = diffBuilder.start();
			
			BufferedReader reader = new BufferedReader(new
					InputStreamReader(diffProcess.getInputStream()));
			
			String line = new String();
			while ((line = reader.readLine()) != null)
			{ 
				files.add(line);
			}
			
	    	try 
	    	{
	    		// make current thread waits until this process terminates
				diffProcess.waitFor();
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
	    
	    return files;
	}
	
	public static boolean passSingleTest(File directory, String commit, String testName)
	{
		String command = SINGLE_TEST_CMD + testName;
		TestResult result = getTestResult(directory, commit, command.split(" "));
		
		return result.getFailures().isEmpty();
	}
}
