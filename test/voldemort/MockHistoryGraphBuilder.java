package voldemort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MockHistoryGraphBuilder
{
	private static final String TEST_1 = "test1";
	private static final String TEST_2 = "test2";
	
	public static HistoryGraph buildHistoryGraph()
	{
		HistoryGraph historyGraph = new HistoryGraph();
		
		Set<String> allTests = new HashSet<String>();
		allTests.add(TEST_1);
		allTests.add(TEST_2);
		
		List<Set<String>> failureSets = new ArrayList<Set<String>>();
		
		Set<String> failureSet0 = new HashSet<String>();
		failureSet0.add(TEST_1);
		failureSet0.add(TEST_2);
		failureSets.add(failureSet0);
		
		Set<String> failureSet1 = new HashSet<String>();
		failureSet1.add(TEST_1);
		failureSet1.add(TEST_2);
		failureSets.add(failureSet1);

		Set<String> failureSet2 = new HashSet<String>();
		failureSet2.add(TEST_1);
		failureSets.add(failureSet2);
		
		Set<String> failureSet3 = new HashSet<String>();
		failureSet3.add(TEST_1);
		failureSets.add(failureSet3);
		
		Set<String> failureSet4 = new HashSet<String>();
		failureSet4.add(TEST_1);
		failureSet4.add(TEST_2);
		failureSets.add(failureSet4);
		
		Set<String> failureSet5 = new HashSet<String>();
		failureSet5.add(TEST_2);
		failureSets.add(failureSet5);
		
		Set<String> failureSet6 = new HashSet<String>();
		failureSets.add(failureSet6);
		
		Set<String> failureSet7 = new HashSet<String>();
		failureSet7.add(TEST_1);
		failureSets.add(failureSet7);
		
		Set<String> failureSet8 = new HashSet<String>();
		failureSets.add(failureSet8);
		
		List<TestResult> results = new ArrayList<TestResult>();
		for (Set<String> failureSet : failureSets)
		{
			results.add(new TestResult(allTests, failureSet));
		}
		
		List<TestResultNode> nodes = new ArrayList<TestResultNode>();
		for (int i = 0; i < results.size(); i++)
		{
			nodes.add(new TestResultNode("commit" + i, results.get(i)));
		}
		
		List<List<TestResultNode>> parents = new ArrayList<List<TestResultNode>>();
		
		List<TestResultNode> parents0 = new ArrayList<TestResultNode>();
		parents.add(parents0);
		
		List<TestResultNode> parents1 = new ArrayList<TestResultNode>();
		parents1.add(nodes.get(0));
		parents.add(parents1);
		
		List<TestResultNode> parents2 = new ArrayList<TestResultNode>();
		parents2.add(nodes.get(0));
		parents.add(parents2);
		
		List<TestResultNode> parents3 = new ArrayList<TestResultNode>();
		parents3.add(nodes.get(2));
		parents.add(parents3);
		
		List<TestResultNode> parents4 = new ArrayList<TestResultNode>();
		parents4.add(nodes.get(2));
		parents.add(parents4);
		
		List<TestResultNode> parents5 = new ArrayList<TestResultNode>();
		parents5.add(nodes.get(1));
		parents.add(parents5);
		
		List<TestResultNode> parents6 = new ArrayList<TestResultNode>();
		parents6.add(nodes.get(3));
		parents.add(parents6);
		
		List<TestResultNode> parents7 = new ArrayList<TestResultNode>();
		parents7.add(nodes.get(4));
		parents.add(parents7);
		
		List<TestResultNode> parents8 = new ArrayList<TestResultNode>();
		parents8.add(nodes.get(5));
		parents8.add(nodes.get(6));
		parents8.add(nodes.get(7));
		parents.add(parents8);
		
		for (int i = 0; i < nodes.size(); i++)
		{
			historyGraph.addNode(nodes.get(i), parents.get(i));
		}
		
		historyGraph.addBugFix(TEST_2, new BugFixPair(nodes.get(0), nodes.get(2)));
		historyGraph.addBugFix(TEST_1, new BugFixPair(nodes.get(1), nodes.get(5)));
		historyGraph.addBugFix(TEST_1, new BugFixPair(nodes.get(3), nodes.get(6)));
		historyGraph.addBugFix(TEST_2, new BugFixPair(nodes.get(4), nodes.get(7)));
		historyGraph.addBugFix(TEST_2, new BugFixPair(nodes.get(5), nodes.get(8)));
		historyGraph.addBugFix(TEST_1, new BugFixPair(nodes.get(7), nodes.get(8)));
		
		return historyGraph;
	}
}
