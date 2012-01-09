package isolation;

import java.util.List;

import voldemort.TestResultNode;

public class Difference 
{
	private final TestResultNode child;
	private final TestResultNode parent;
	private final List<String> changedFiles;
	
	public Difference(TestResultNode child, TestResultNode parent, List<String> changedFiles)
	{
		this.child = child;
		this.parent = parent;
		this.changedFiles = changedFiles;
	}
	
	public TestResultNode getChild()
	{
		return child;
	}

	public TestResultNode getParent()
	{
		return parent;
	}
	
	public List<String> getChangedFiles()
	{
		return changedFiles;
	}
	
	@Override
	public boolean equals(Object other) 
	{
		if (other == null || !other.getClass().equals(this.getClass())) 
		{
			return false;
		}
		
		Difference diff = (Difference) other;
		
		return child.equals(diff.child) && parent.equals(diff.parent);
	}
	
	@Override
	public int hashCode()
	{
		return 13 * child.hashCode() + 17 * parent.hashCode();
	}

	@Override
	public String toString()
	{
		String result = "Child commit: " + child + "\n" + "Parent commit: " + parent + "\n";
		result += "Diff files:\n";
		for (String file : changedFiles)
		{
			result += file + "\n";
		}
		
		return result;
	}
}
