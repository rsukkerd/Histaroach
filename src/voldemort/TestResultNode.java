package voldemort;

/**
 * TestResultNode contains a commit string and a TestResult
 */
public class TestResultNode 
{
	private final String commit;
	private final TestResult result;
	
	public TestResultNode(String commit, TestResult result) 
	{
		this.commit = commit;
		this.result = result;
	}
	
	/**
	 * @return commit string
	 */
	public String getCommit() 
	{
		return commit;
	}
	
	/**
	 * @return TestResult
	 */
	public TestResult getTestResult() 
	{
		return result;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (other == null || !other.getClass().equals(this.getClass())) 
		{
			return false;
		}
		
		TestResultNode node = (TestResultNode) other;
		
		return commit.equals(node.commit);
	}
	
	@Override
	public int hashCode() 
	{
		return commit.hashCode();
	}
	
	@Override
	public String toString()
	{
		return commit;
	}
}
