package isolation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import voldemort.TestResult;

public class DataExtractor 
{
	private static final String[] LOG_COMMAND = {"git", "log", "--pretty=format:%h %p"};
	private static final String[] ALL_TESTS_CMD = {"ant", "junit"};
	private static final String SINGLE_TEST_CMD = "ant junit-test -Dtest.name=";
	
	public static void extractData(String repository, String outputFile, String startCommit, String endCommit) throws IOException
	{
		File directory = new File(repository);
		FileWriter fileStream = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(fileStream);
		
		checkoutCommit(directory, startCommit);
		
		ProcessBuilder logBuilder = new ProcessBuilder(LOG_COMMAND);
		logBuilder.directory(directory);
		
		try 
    	{
			Process logProcess = logBuilder.start();
			
			BufferedReader reader = new BufferedReader(new
					InputStreamReader(logProcess.getInputStream()));
			
			String line = new String();
			while ((line = reader.readLine()) != null)
			{
				String[] hashes = line.split(" ");
				String commit = hashes[0];
				out.write("COMMIT " + commit + "\n");
				
				if (hashes.length > 1)
				{
					for (int i = 1; i < hashes.length; i++)
					{
						String parent = hashes[i];
						out.write("PARENT " + parent + "\n");
						out.write("DIFF FILES:\n");
						
						List<String> diffFiles = getChangedFiles(directory, commit, parent);
						for (String file : diffFiles)
						{
							out.write(file + "\n");
						}
					}
				}
				
				TestResult testResult = getTestResult(directory, commit, ALL_TESTS_CMD);
				out.write(testResult.toString());
				out.write("\n");
				
				if (commit.equals(endCommit))
				{
					break;
				}
			}
			out.close();
			
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
	}
	
	/**
	 * checkout commit from the repository
	 * @param directory : repository directory
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
	 * @return diff files between childCommit and parentCommit
	 */
	public static List<String> getChangedFiles(File directory, String childCommit, String parentCommit)
	{
		List<String> files = new ArrayList<String>();
		
		ProcessBuilder diffBuilder = new ProcessBuilder("git", "diff", "--name-status", childCommit, parentCommit);
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
	
	/**
	 * @param directory : repository directory
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
}
