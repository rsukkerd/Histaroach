package voldemort;

public class BugFixPair 
{
	private final TestResultNode nodeFail;
	private final TestResultNode nodePass;
	
	public BugFixPair(TestResultNode nodeFail, TestResultNode nodePass)
	{
		this.nodeFail = nodeFail;
		this.nodePass = nodePass;
	}
	
	public TestResultNode getNodeFail()
	{
		return nodeFail;
	}
	
	public TestResultNode getNodePass()
	{
		return nodePass;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (other == null || !other.getClass().equals(this.getClass())) 
		{
			return false;
		}
		
		BugFixPair pair = (BugFixPair) other;
		
		return nodeFail.equals(pair.nodeFail) && nodePass.equals(pair.nodePass);
	}
	
	@Override
	public int hashCode() 
	{
		return 13 * nodeFail.hashCode() + 17 * nodePass.hashCode();
	}
}
