package voldemort;

import java.util.List;

/**
 * TestResult contains 2 lists of names of the tests:
 * 1. list of all tests
 * 2. list of failed tests
 */
public class TestResult 
{
	private final List<String> allTests;
	private final List<String> failures;
	
	public TestResult(List<String> allTests, List<String> failures) 
	{
		this.allTests = allTests;
		this.failures = failures;
	}
	
	/**
	 * @return list of names of all tests
	 */
	public List<String> getAllTests() 
	{
		return allTests;
	}
	
	/**
	 * @return list of names of failed tests
	 */
	public List<String> getFailures() 
	{
		return failures;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (other == null || !other.getClass().equals(this.getClass())) 
		{
			return false;
		}
		
		TestResult result = (TestResult) other;
		
		return allTests.equals(result.allTests) && failures.equals(result.failures);
	}
	
	@Override
	public int hashCode() 
	{
		return 13 * allTests.hashCode() + 17 * failures.hashCode();
	}
}
