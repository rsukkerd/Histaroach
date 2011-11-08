package voldemort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class ParallelFixesFinderTest 
{
	private static final String TEST_1 = "test1";
	private static final String TEST_2 = "test2";
	
	private static final Set<String> ALL_TESTS = new HashSet<String>();
	static
	{
		ALL_TESTS.add(TEST_1);
		ALL_TESTS.add(TEST_2);
	}
	
	private static final Set<String> FAILURES_0 = new HashSet<String>();
	static
	{
		FAILURES_0.add(TEST_1);
		FAILURES_0.add(TEST_2);
	}
	
	private static final Set<String> FAILURES_1 = new HashSet<String>();
	static
	{
		FAILURES_1.add(TEST_1);
		FAILURES_1.add(TEST_2);
	}
	
	private static final Set<String> FAILURES_2 = new HashSet<String>();
	static
	{
		FAILURES_2.add(TEST_1);
	}
	
	private static final Set<String> FAILURES_3 = new HashSet<String>();
	static
	{
		FAILURES_3.add(TEST_1);
	}
	
	private static final Set<String> FAILURES_4 = new HashSet<String>();
	static
	{
		FAILURES_4.add(TEST_1);
		FAILURES_4.add(TEST_2);
	}
	
	private static final Set<String> FAILURES_5 = new HashSet<String>();
	static
	{
		FAILURES_5.add(TEST_2);
	}
	
	private static final Set<String> FAILURES_6 = new HashSet<String>();
	
	private static final Set<String> FAILURES_7 = new HashSet<String>();
	static
	{
		FAILURES_7.add(TEST_1);
	}
	
	private static final Set<String> FAILURES_8 = new HashSet<String>();
	
	private static final TestResult TEST_RESULT_0 = new TestResult(ALL_TESTS, FAILURES_0);
	private static final TestResult TEST_RESULT_1 = new TestResult(ALL_TESTS, FAILURES_1);
	private static final TestResult TEST_RESULT_2 = new TestResult(ALL_TESTS, FAILURES_2);
	private static final TestResult TEST_RESULT_3 = new TestResult(ALL_TESTS, FAILURES_3);
	private static final TestResult TEST_RESULT_4 = new TestResult(ALL_TESTS, FAILURES_4);
	private static final TestResult TEST_RESULT_5 = new TestResult(ALL_TESTS, FAILURES_5);
	private static final TestResult TEST_RESULT_6 = new TestResult(ALL_TESTS, FAILURES_6);
	private static final TestResult TEST_RESULT_7 = new TestResult(ALL_TESTS, FAILURES_7);
	private static final TestResult TEST_RESULT_8 = new TestResult(ALL_TESTS, FAILURES_8);
	
	private static final TestResultNode NODE_0 = new TestResultNode("commit0", TEST_RESULT_0);
	private static final TestResultNode NODE_1 = new TestResultNode("commit1", TEST_RESULT_1);
	private static final TestResultNode NODE_2 = new TestResultNode("commit2", TEST_RESULT_2);
	private static final TestResultNode NODE_3 = new TestResultNode("commit3", TEST_RESULT_3);
	private static final TestResultNode NODE_4 = new TestResultNode("commit4", TEST_RESULT_4);
	private static final TestResultNode NODE_5 = new TestResultNode("commit5", TEST_RESULT_5);
	private static final TestResultNode NODE_6 = new TestResultNode("commit6", TEST_RESULT_6);
	private static final TestResultNode NODE_7 = new TestResultNode("commit7", TEST_RESULT_7);
	private static final TestResultNode NODE_8 = new TestResultNode("commit8", TEST_RESULT_8);
	
	private static final Map<String, Set<BugFixPair>> PARALLEL_EXPECTED = new HashMap<String, Set<BugFixPair>>();
	static
	{
		BugFixPair pair1 = new BugFixPair(NODE_1, NODE_5);
		BugFixPair pair2 = new BugFixPair(NODE_3, NODE_6);
		
		Set<BugFixPair> parallelFixes = new HashSet<BugFixPair>();
		parallelFixes.add(pair1);
		parallelFixes.add(pair2);
		
		PARALLEL_EXPECTED.put(TEST_1, parallelFixes);
	}
	
	private static final Map<String, Set<BugFixPair>> ALL_EXPECTED = new HashMap<String, Set<BugFixPair>>();
	static
	{
		BugFixPair pair1_1 = new BugFixPair(NODE_1, NODE_5);
		BugFixPair pair1_2 = new BugFixPair(NODE_3, NODE_6);
		BugFixPair pair1_3 = new BugFixPair(NODE_7, NODE_8);
		
		BugFixPair pair2_1 = new BugFixPair(NODE_0, NODE_2);
		BugFixPair pair2_2 = new BugFixPair(NODE_4, NODE_7);
		BugFixPair pair2_3 = new BugFixPair(NODE_5, NODE_8);
		
		Set<BugFixPair> allFixes1 = new HashSet<BugFixPair>();
		allFixes1.add(pair1_1);
		allFixes1.add(pair1_2);
		allFixes1.add(pair1_3);
		
		Set<BugFixPair> allFixes2 = new HashSet<BugFixPair>();
		allFixes2.add(pair2_1);
		allFixes2.add(pair2_2);
		allFixes2.add(pair2_3);
		
		ALL_EXPECTED.put(TEST_1, allFixes1);
		ALL_EXPECTED.put(TEST_2, allFixes2);
	}
	
	@Test
	public void testFindParallelFixes()
	{
		HistoryGraph historyGraph = MockHistoryGraphBuilder.buildHistoryGraph();
		Map<String, Set<BugFixPair>> actual = ParallelFixesFinder.findParallelFixes(historyGraph);
		
		assertEquals(PARALLEL_EXPECTED, actual);
	}
	
	@Test
	public void testFindAllFixes()
	{
		HistoryGraph historyGraph = MockHistoryGraphBuilder.buildHistoryGraph();
		Iterator<String> itr = historyGraph.getBugIterator();
		
		int bugCount = 0;
		while (itr.hasNext())
		{
			String bug = itr.next();
			bugCount++;
			assertTrue(ALL_EXPECTED.containsKey(bug));
			
			List<BugFixPair> actualPairs = historyGraph.getBugFixPairs(bug);
			Set<BugFixPair> expectedPairs = ALL_EXPECTED.get(bug);
			assertTrue(actualPairs.size() == expectedPairs.size());

			for (BugFixPair pair : actualPairs)
			{
				assertTrue(expectedPairs.contains(pair));
			}
		}
		
		assertTrue(bugCount == ALL_EXPECTED.size());
	}
}
