package tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import common.BugFix;
import common.HistoryGraph;
import common.HistoryGraphBuilder;
import common.ParallelFixesFinder;
import common.TestResult;
import common.TestResultNode;

public class ParallelFixesFinderTest
{
	private static final String TEST_1 = "test1";
	private static final String TEST_2 = "test2";
	private static final String TEST_3 = "test3";
	private static final String TEST_4 = "test4";
	private static final String TEST_5 = "test5";
	private static final String TEST_6 = "test6";
	private static final String TEST_7 = "test7";
	private static final String TEST_8 = "test8";
	
	// all tests in graph1
	private static final Set<String> ALL_TESTS_1 = new HashSet<String>();
	static
	{
		ALL_TESTS_1.add(TEST_1);
		ALL_TESTS_1.add(TEST_2);
	}
	// failures of node1 in graph1
	private static final Set<String> FAILURES_1_1 = new HashSet<String>();
	static
	{
		FAILURES_1_1.add(TEST_1);
		FAILURES_1_1.add(TEST_2);
	}
	// failures of node2 in graph1
	private static final Set<String> FAILURES_2_1 = new HashSet<String>();
	static
	{
		FAILURES_2_1.add(TEST_2);
	}
	// failures of node3 in graph1
	private static final Set<String> FAILURES_3_1 = new HashSet<String>();
	static
	{
		FAILURES_3_1.add(TEST_1);
	}
	// failures of node4 in graph1
	private static final Set<String> FAILURES_4_1 = new HashSet<String>();
	
	// node1 in graph1
	private static final TestResultNode NODE_1_1 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_1, FAILURES_1_1));
	// node2 in graph1
	private static final TestResultNode NODE_2_1 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_1, FAILURES_2_1));
	// node3 in graph1
	private static final TestResultNode NODE_3_1 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_1, FAILURES_3_1));
	// node4 in graph1
	private static final TestResultNode NODE_4_1 = new TestResultNode("4", 
			new TestResult(ALL_TESTS_1, FAILURES_4_1));
	
	// parents of node1 in graph1
	private static final List<TestResultNode> PARENTS_1_1 = new ArrayList<TestResultNode>();
	// parents of node2 in graph1
	private static final List<TestResultNode> PARENTS_2_1 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_1.add(NODE_1_1);
	}
	// parents of node3 in graph1
	private static final List<TestResultNode> PARENTS_3_1 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_1.add(NODE_2_1);
	}
	// parents of node4 in graph1
	private static final List<TestResultNode> PARENTS_4_1 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_4_1.add(NODE_3_1);
	}
	
	// all tests in graph2
	private static final Set<String> ALL_TESTS_2 = new HashSet<String>();
	static
	{
		ALL_TESTS_2.add(TEST_1);
		ALL_TESTS_2.add(TEST_2);
	}
	// failures of node1 in graph2
	private static final Set<String> FAILURES_1_2 = new HashSet<String>();
	static
	{
		FAILURES_1_2.add(TEST_1);
		FAILURES_1_2.add(TEST_2);
	}
	// failures of node2 in graph2
	private static final Set<String> FAILURES_2_2 = new HashSet<String>();
	static
	{
		FAILURES_2_2.add(TEST_2);
	}
	// failures of node3 in graph2
	private static final Set<String> FAILURES_3_2 = new HashSet<String>();
	// failures of node4 in graph2
	private static final Set<String> FAILURES_4_2 = new HashSet<String>();
	
	// node1 in graph2
	private static final TestResultNode NODE_1_2 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_2, FAILURES_1_2));
	// node2 in graph2
	private static final TestResultNode NODE_2_2 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_2, FAILURES_2_2));
	// node3 in graph2
	private static final TestResultNode NODE_3_2 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_2, FAILURES_3_2));
	// node4 in graph2
	private static final TestResultNode NODE_4_2 = new TestResultNode("4", 
			new TestResult(ALL_TESTS_2, FAILURES_4_2));
	
	// parents of node1 in graph2
	private static final List<TestResultNode> PARENTS_1_2 = new ArrayList<TestResultNode>();
	// parents of node2 in graph2
	private static final List<TestResultNode> PARENTS_2_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_2.add(NODE_1_2);
	}
	// parents of node3 in graph2
	private static final List<TestResultNode> PARENTS_3_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_2.add(NODE_1_2);
	}
	// parents of node4 in graph2
	private static final List<TestResultNode> PARENTS_4_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_4_2.add(NODE_3_2);
		PARENTS_4_2.add(NODE_2_2);
	}
	
	// all tests in graph3
	private static final Set<String> ALL_TESTS_3 = new HashSet<String>();
	static
	{
		ALL_TESTS_3.add(TEST_1);
		ALL_TESTS_3.add(TEST_2);
		ALL_TESTS_3.add(TEST_3);
		ALL_TESTS_3.add(TEST_4);
		ALL_TESTS_3.add(TEST_5);
		ALL_TESTS_3.add(TEST_6);
		ALL_TESTS_3.add(TEST_7);
		ALL_TESTS_3.add(TEST_8);
	}
	// failures of node1 in graph3
	private static final Set<String> FAILURES_1_3 = new HashSet<String>();
	static
	{
		FAILURES_1_3.add(TEST_1);
		FAILURES_1_3.add(TEST_2);
		FAILURES_1_3.add(TEST_3);
		FAILURES_1_3.add(TEST_4);
		FAILURES_1_3.add(TEST_5);
		FAILURES_1_3.add(TEST_6);
		FAILURES_1_3.add(TEST_7);
		FAILURES_1_3.add(TEST_8);
	}
	// failures of node2 in graph3
	private static final Set<String> FAILURES_2_3 = new HashSet<String>();
	static
	{
		FAILURES_2_3.add(TEST_1);
		FAILURES_2_3.add(TEST_2);
		FAILURES_2_3.add(TEST_4);
		FAILURES_2_3.add(TEST_5);
		FAILURES_2_3.add(TEST_6);
	}
	// failures of node3 in graph3
	private static final Set<String> FAILURES_3_3 = new HashSet<String>();
	static
	{
		FAILURES_3_3.add(TEST_1);
		FAILURES_3_3.add(TEST_3);
		FAILURES_3_3.add(TEST_5);
		FAILURES_3_3.add(TEST_6);
		FAILURES_3_3.add(TEST_7);
		FAILURES_3_3.add(TEST_8);
	}
	// failures of node4 in graph3
	private static final Set<String> FAILURES_4_3 = new HashSet<String>();
	static
	{
		FAILURES_4_3.add(TEST_1);
		FAILURES_4_3.add(TEST_3);
		FAILURES_4_3.add(TEST_5);
		FAILURES_4_3.add(TEST_7);
		FAILURES_4_3.add(TEST_8);
	}
	// failures of node5 in graph3
	private static final Set<String> FAILURES_5_3 = new HashSet<String>();
	static
	{
		FAILURES_5_3.add(TEST_2);
		FAILURES_5_3.add(TEST_3);
		FAILURES_5_3.add(TEST_4);
		FAILURES_5_3.add(TEST_5);
		FAILURES_5_3.add(TEST_6);
		FAILURES_5_3.add(TEST_7);
	}
	// failures of node6 in graph3
	private static final Set<String> FAILURES_6_3 = new HashSet<String>();
	static
	{
		FAILURES_6_3.add(TEST_2);
		FAILURES_6_3.add(TEST_6);
		FAILURES_6_3.add(TEST_7);
	}
	// failures of node7 in graph3
	private static final Set<String> FAILURES_7_3 = new HashSet<String>();
	static
	{
		FAILURES_7_3.add(TEST_1);
		FAILURES_7_3.add(TEST_2);
		FAILURES_7_3.add(TEST_3);
		FAILURES_7_3.add(TEST_4);
		FAILURES_7_3.add(TEST_8);
	}
	
	// node1 in graph3
	private static final TestResultNode NODE_1_3 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_3, FAILURES_1_3));
	// node2 in graph3
	private static final TestResultNode NODE_2_3 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_3, FAILURES_2_3));
	// node3 in graph3
	private static final TestResultNode NODE_3_3 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_3, FAILURES_3_3));
	// node4 in graph3
	private static final TestResultNode NODE_4_3 = new TestResultNode("4", 
			new TestResult(ALL_TESTS_3, FAILURES_4_3));
	// node5 in graph3
	private static final TestResultNode NODE_5_3 = new TestResultNode("5", 
			new TestResult(ALL_TESTS_3, FAILURES_5_3));
	// node6 in graph3
	private static final TestResultNode NODE_6_3 = new TestResultNode("6", 
			new TestResult(ALL_TESTS_3, FAILURES_6_3));
	// node7 in graph3
	private static final TestResultNode NODE_7_3 = new TestResultNode("7", 
			new TestResult(ALL_TESTS_3, FAILURES_7_3));
	
	// parents of node1 in graph3
	private static final List<TestResultNode> PARENTS_1_3 = new ArrayList<TestResultNode>();
	// parents of node2 in graph3
	private static final List<TestResultNode> PARENTS_2_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_3.add(NODE_1_3);
	}
	// parents of node3 in graph3
	private static final List<TestResultNode> PARENTS_3_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_3.add(NODE_2_3);
	}
	// parents of node4 in graph3
	private static final List<TestResultNode> PARENTS_4_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_4_3.add(NODE_2_3);
	}
	// parents of node5 in graph3
	private static final List<TestResultNode> PARENTS_5_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_5_3.add(NODE_4_3);
		PARENTS_5_3.add(NODE_3_3);
	}
	// parents of node6 in graph3
	private static final List<TestResultNode> PARENTS_6_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_6_3.add(NODE_1_3);
	}
	// parents of node7 in graph3
	private static final List<TestResultNode> PARENTS_7_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_7_3.add(NODE_6_3);
		PARENTS_7_3.add(NODE_5_3);
	}
	
	// parallel fixes in graph1
	private static final Map<String, Set<BugFix>> EXPECTED_PARALLEL_FIXES_1 = 
		new HashMap<String, Set<BugFix>>();
	
	// parallel fixes in graph2
	private static final Map<String, Set<BugFix>> EXPECTED_PARALLEL_FIXES_2 = 
		new HashMap<String, Set<BugFix>>();
	static
	{
		BugFix fix_1_1 = new BugFix(NODE_3_2); // 1st fix of test1
		fix_1_1.addNodeFail(NODE_1_2);
		BugFix fix_2_1 = new BugFix(NODE_2_2); // 2nd fix of test1
		fix_2_1.addNodeFail(NODE_1_2);
		
		Set<BugFix> parallelNodes_1 = new HashSet<BugFix>(); // parallel nodes that fix test1
		parallelNodes_1.add(fix_1_1);
		parallelNodes_1.add(fix_2_1);
		
		EXPECTED_PARALLEL_FIXES_2.put(TEST_1, parallelNodes_1);
	}
	
	// parallel fixes in graph3
	private static final Map<String, Set<BugFix>> EXPECTED_PARALLEL_FIXES_3 = 
		new HashMap<String, Set<BugFix>>();
	static
	{
		BugFix fix_1_1 = new BugFix(NODE_6_3); // 1st fix of test1
		fix_1_1.addNodeFail(NODE_1_3);
		BugFix fix_2_1 = new BugFix(NODE_5_3); // 2nd fix of test1
		fix_2_1.addNodeFail(NODE_4_3);
		fix_2_1.addNodeFail(NODE_3_3);
		fix_2_1.addNodeFail(NODE_2_3);
		fix_2_1.addNodeFail(NODE_1_3);
		
		BugFix fix_1_2 = new BugFix(NODE_4_3); // 1st fix of test2
		fix_1_2.addNodeFail(NODE_2_3);
		fix_1_2.addNodeFail(NODE_1_3);
		BugFix fix_2_2 = new BugFix(NODE_3_3); // 2nd fix of test2
		fix_2_2.addNodeFail(NODE_2_3);
		fix_2_2.addNodeFail(NODE_1_3);
		
		BugFix fix_1_3 = new BugFix(NODE_6_3); // 1st fix of test3
		fix_1_3.addNodeFail(NODE_1_3);
		BugFix fix_2_3 = new BugFix(NODE_2_3); // 2nd fix of test3
		fix_2_3.addNodeFail(NODE_1_3);
		
		BugFix fix_1_4 = new BugFix(NODE_6_3); // 1st fix of test4
		fix_1_4.addNodeFail(NODE_1_3);
		BugFix fix_2_4 = new BugFix(NODE_4_3); // 2nd fix of test4
		fix_2_4.addNodeFail(NODE_2_3);
		fix_2_4.addNodeFail(NODE_1_3);
		BugFix fix_3_4 = new BugFix(NODE_3_3); // 3rd fix of test4
		fix_3_4.addNodeFail(NODE_2_3);
		fix_3_4.addNodeFail(NODE_1_3);
		
		BugFix fix_1_8 = new BugFix(NODE_6_3); // 1st fix of test8
		fix_1_8.addNodeFail(NODE_1_3);
		BugFix fix_2_8 = new BugFix(NODE_5_3); // 2nd fix of test8
		fix_2_8.addNodeFail(NODE_4_3);
		fix_2_8.addNodeFail(NODE_3_3);
		BugFix fix_3_8 = new BugFix(NODE_2_3); // 3rd fix of test8
		fix_3_8.addNodeFail(NODE_1_3);
		
		Set<BugFix> parallelNodes_1 = new HashSet<BugFix>(); // parallel nodes that fix test1
		parallelNodes_1.add(fix_1_1);
		parallelNodes_1.add(fix_2_1);
		
		Set<BugFix> parallelNodes_2 = new HashSet<BugFix>(); // parallel nodes that fix test2
		parallelNodes_2.add(fix_1_2);
		parallelNodes_2.add(fix_2_2);
		
		Set<BugFix> parallelNodes_3 = new HashSet<BugFix>(); // parallel nodes that fix test3
		parallelNodes_3.add(fix_1_3);
		parallelNodes_3.add(fix_2_3);
		
		Set<BugFix> parallelNodes_4 = new HashSet<BugFix>(); // parallel nodes that fix test4
		parallelNodes_4.add(fix_1_4);
		parallelNodes_4.add(fix_2_4);
		parallelNodes_4.add(fix_3_4);
		
		Set<BugFix> parallelNodes_8 = new HashSet<BugFix>(); // parallel nodes that fix test8
		parallelNodes_8.add(fix_1_8);
		parallelNodes_8.add(fix_2_8);
		parallelNodes_8.add(fix_3_8);
		
		EXPECTED_PARALLEL_FIXES_3.put(TEST_1, parallelNodes_1);
		EXPECTED_PARALLEL_FIXES_3.put(TEST_2, parallelNodes_2);
		EXPECTED_PARALLEL_FIXES_3.put(TEST_3, parallelNodes_3);
		EXPECTED_PARALLEL_FIXES_3.put(TEST_4, parallelNodes_4);
		EXPECTED_PARALLEL_FIXES_3.put(TEST_8, parallelNodes_8);
	}
	
	@Test
	public void testFindParallelFixes1()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_1, PARENTS_1_1);
		graph.addNode(NODE_2_1, PARENTS_2_1);
		graph.addNode(NODE_3_1, PARENTS_3_1);
		graph.addNode(NODE_4_1, PARENTS_4_1);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_4_1);
		
		checkParallelFixes(EXPECTED_PARALLEL_FIXES_1, graph);
	}
	
	@Test
	public void testFindParallelFixes2()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_2, PARENTS_1_2);
		graph.addNode(NODE_2_2, PARENTS_2_2);
		graph.addNode(NODE_3_2, PARENTS_3_2);
		graph.addNode(NODE_4_2, PARENTS_4_2);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_4_2);
		
		checkParallelFixes(EXPECTED_PARALLEL_FIXES_2, graph);
	}
	
	@Test
	public void testFindParallelFixes3()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_3, PARENTS_1_3);
		graph.addNode(NODE_2_3, PARENTS_2_3);
		graph.addNode(NODE_3_3, PARENTS_3_3);
		graph.addNode(NODE_4_3, PARENTS_4_3);
		graph.addNode(NODE_5_3, PARENTS_5_3);
		graph.addNode(NODE_6_3, PARENTS_6_3);
		graph.addNode(NODE_7_3, PARENTS_7_3);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_7_3);
		
		checkParallelFixes(EXPECTED_PARALLEL_FIXES_3, graph);
	}
	
	private void checkParallelFixes(Map<String, Set<BugFix>> expected, HistoryGraph graph)
	{
		Map<String, Set<BugFix>> actual = ParallelFixesFinder.findParallelFixes(graph);
		assertEquals(expected, actual);
	}
}
