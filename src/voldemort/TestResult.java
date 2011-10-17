package voldemort;

import java.util.Set;

/**
 * TestResult contains 2 lists of names of the tests: list of all tests 
 * and list of failed tests
 */
public class TestResult 
{
	private final Set<String> allTests;
	private final Set<String> failures;
	
	public TestResult(Set<String> allTests, Set<String> failures) 
	{
		this.allTests = allTests;
		this.failures = failures;
	}
	
	/**
	 * @return list of names of all tests
	 */
	public Set<String> getAllTests() 
	{
		return allTests;
	}
	
	/**
	 * @return list of names of failed tests
	 */
	public Set<String> getFailures() 
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
