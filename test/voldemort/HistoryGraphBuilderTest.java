package voldemort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class HistoryGraphBuilderTest 
{
	private static final String DIR_1 = "/home/rsukkerd/workspace/project499/test/voldemort/sample/sample1";
	private static final String DIR_2 = "/home/rsukkerd/workspace/project499/test/voldemort/sample/sample2";
	private static final String DIR_3 = "/home/rsukkerd/workspace/project499/test/voldemort/sample/sample3";
	private static final String DIR_4 = "/home/rsukkerd/workspace/project499/test/voldemort/sample/sample4";
	private static final String DIR_5 = "/home/rsukkerd/workspace/project499/test/voldemort/sample/sample5";
	
	private static final TestResultNode NODE_1_MAP_1 = 
		new TestResultNode("master", null);
	
	private static final TestResultNode NODE_1_MAP_2 = 
		new TestResultNode("88cb3de667c0de7c18710d011a2357b0fa81f5b9", null);
	private static final TestResultNode NODE_2_MAP_2 = 
		new TestResultNode("master", null);
	
	private static final TestResultNode NODE_1_MAP_3 = 
		new TestResultNode("4cb3b283449c3f180c2d5e7de5a7a1c4ec6f220b", null);
	private static final TestResultNode NODE_2_MAP_3 = 
		new TestResultNode("d3885c6b2054d27e7c7f011b72151db54dfa7f2e", null);
	private static final TestResultNode NODE_3_MAP_3 = 
		new TestResultNode("master", null);
	
	private static final TestResultNode NODE_1_MAP_4 = 
		new TestResultNode("ce23b5c98d6d6379998b466b1f75cb8cd11a305e", null);
	private static final TestResultNode NODE_2_MAP_4 = 
		new TestResultNode("cf3709a5b0a02e2a4db8188908210f1e021bc749", null);
	private static final TestResultNode NODE_3_MAP_4 = 
		new TestResultNode("6a33c575386da5056f4c9fef4fbd2ceb903a33da", null);
	private static final TestResultNode NODE_4_MAP_4 = 
		new TestResultNode("master", null);
	
	private static final TestResultNode NODE_1_MAP_5 = 
		new TestResultNode("05475b452fe2c26f42515ed3cba4fa169bef8700", null);
	private static final TestResultNode NODE_2_MAP_5 = 
		new TestResultNode("4c7f7bd25c0738087f4839624576749161224930", null);
	private static final TestResultNode NODE_3_MAP_5 = 
		new TestResultNode("3bfa3dabf0b67b740522a650a9f18adc521fb588", null);
	private static final TestResultNode NODE_4_MAP_5 = 
		new TestResultNode("f4f5b80555533b32e411784b1ced71a18e12f5cd", null);
	private static final TestResultNode NODE_5_MAP_5 = 
		new TestResultNode("29e5218f558665a8380eb2ebc8c99bb289171472", null);
	private static final TestResultNode NODE_6_MAP_5 = 
		new TestResultNode("master", null);
	
	private static final List<TestResultNode> PARENTS_1_MAP_1 = new ArrayList<TestResultNode>();
	
	private static final List<TestResultNode> PARENTS_1_MAP_2 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_2_MAP_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_MAP_2.add(NODE_1_MAP_2);
	}
	
	private static final List<TestResultNode> PARENTS_1_MAP_3 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_2_MAP_3 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_3_MAP_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_MAP_3.add(NODE_1_MAP_3);
		PARENTS_3_MAP_3.add(NODE_2_MAP_3);
	}
	
	private static final List<TestResultNode> PARENTS_1_MAP_4 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_2_MAP_4 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_3_MAP_4 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_4_MAP_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_MAP_4.add(NODE_1_MAP_4);
		PARENTS_3_MAP_4.add(NODE_1_MAP_4);
		PARENTS_4_MAP_4.add(NODE_3_MAP_4);
		PARENTS_4_MAP_4.add(NODE_2_MAP_4);
	}
	
	private static final List<TestResultNode> PARENTS_1_MAP_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_2_MAP_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_3_MAP_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_4_MAP_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_5_MAP_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_6_MAP_5 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_MAP_5.add(NODE_1_MAP_5);
		PARENTS_3_MAP_5.add(NODE_1_MAP_5);
		PARENTS_4_MAP_5.add(NODE_3_MAP_5);
		PARENTS_4_MAP_5.add(NODE_2_MAP_5);
		PARENTS_5_MAP_5.add(NODE_1_MAP_5);
		PARENTS_6_MAP_5.add(NODE_5_MAP_5);
		PARENTS_6_MAP_5.add(NODE_4_MAP_5);
	}
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP_NODE_PARENTS_1 = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	static
	{
		MAP_NODE_PARENTS_1.put(NODE_1_MAP_1, PARENTS_1_MAP_1);
	}
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP_NODE_PARENTS_2 = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	static
	{
		MAP_NODE_PARENTS_2.put(NODE_1_MAP_2, PARENTS_1_MAP_2);
		MAP_NODE_PARENTS_2.put(NODE_2_MAP_2, PARENTS_2_MAP_2);
	}
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP_NODE_PARENTS_3 = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	static
	{
		MAP_NODE_PARENTS_3.put(NODE_1_MAP_3, PARENTS_1_MAP_3);
		MAP_NODE_PARENTS_3.put(NODE_2_MAP_3, PARENTS_2_MAP_3);
		MAP_NODE_PARENTS_3.put(NODE_3_MAP_3, PARENTS_3_MAP_3);
	}
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP_NODE_PARENTS_4 = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	static
	{
		MAP_NODE_PARENTS_4.put(NODE_1_MAP_4, PARENTS_1_MAP_4);
		MAP_NODE_PARENTS_4.put(NODE_2_MAP_4, PARENTS_2_MAP_4);
		MAP_NODE_PARENTS_4.put(NODE_3_MAP_4, PARENTS_3_MAP_4);
		MAP_NODE_PARENTS_4.put(NODE_4_MAP_4, PARENTS_4_MAP_4);
	}
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP_NODE_PARENTS_5 = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	static
	{
		MAP_NODE_PARENTS_5.put(NODE_1_MAP_5, PARENTS_1_MAP_5);
		MAP_NODE_PARENTS_5.put(NODE_2_MAP_5, PARENTS_2_MAP_5);
		MAP_NODE_PARENTS_5.put(NODE_3_MAP_5, PARENTS_3_MAP_5);
		MAP_NODE_PARENTS_5.put(NODE_4_MAP_5, PARENTS_4_MAP_5);
		MAP_NODE_PARENTS_5.put(NODE_5_MAP_5, PARENTS_5_MAP_5);
		MAP_NODE_PARENTS_5.put(NODE_6_MAP_5, PARENTS_6_MAP_5);
	}
	
	private static final String TEST_1 = "test1";
	private static final String TEST_2 = "test2";
	private static final String TEST_3 = "test3";
	private static final String TEST_4 = "test4";
	private static final String TEST_5 = "test5";
	private static final String TEST_6 = "test6";
	private static final String TEST_7 = "test7";
	private static final String TEST_8 = "test8";
	private static final String TEST_9 = "test9";
	private static final String TEST_10 = "test10";
	private static final String TEST_11 = "test11";
	private static final String TEST_12 = "test12";
	
	/* all tests in graph1 */
	private static final Set<String> ALL_TESTS_1 = new HashSet<String>();
	static
	{
		ALL_TESTS_1.add(TEST_1);
		ALL_TESTS_1.add(TEST_2);
		ALL_TESTS_1.add(TEST_3);
		ALL_TESTS_1.add(TEST_4);
	}
	
	/* failures of node1 in graph1 */
	private static final Set<String> FAILURES_1_1 = new HashSet<String>();
	static
	{
		FAILURES_1_1.add(TEST_2);
		FAILURES_1_1.add(TEST_4);
	}
	/* failures of node2 in graph1 */
	private static final Set<String> FAILURES_2_1 = new HashSet<String>();
	static
	{
		FAILURES_2_1.add(TEST_2);
		FAILURES_2_1.add(TEST_3);
	}
	
	/* node1 in graph1 */
	private static final TestResultNode NODE_1_1 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_1, FAILURES_1_1));
	/* node2 in graph1 */
	private static final TestResultNode NODE_2_1 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_1, FAILURES_2_1));
	
	/* parents of node1 in graph1 */
	private static final List<TestResultNode> PARENTS_1_1 = new ArrayList<TestResultNode>();
	/* parents of node2 in graph1 */
	private static final List<TestResultNode> PARENTS_2_1 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_1.add(NODE_1_1);
	}
	
	/* all tests in graph2 */
	private static final Set<String> ALL_TESTS_2 = new HashSet<String>();
	static
	{
		ALL_TESTS_2.add(TEST_1);
		ALL_TESTS_2.add(TEST_2);
		ALL_TESTS_2.add(TEST_3);
		ALL_TESTS_2.add(TEST_4);
		ALL_TESTS_2.add(TEST_5);
	}
	
	/* failures of node1 in graph2 */
	private static final Set<String> FAILURES_1_2 = new HashSet<String>();
	static
	{
		FAILURES_1_2.add(TEST_2);
		FAILURES_1_2.add(TEST_3);
		FAILURES_1_2.add(TEST_4);
	}
	/* failures of node2 in graph2 */
	private static final Set<String> FAILURES_2_2 = new HashSet<String>();
	static
	{
		FAILURES_2_2.add(TEST_1);
		FAILURES_2_2.add(TEST_2);
		FAILURES_2_2.add(TEST_5);
	}
	/* failures of node3 in graph2 */
	private static final Set<String> FAILURES_3_2 = new HashSet<String>();
	static
	{
		FAILURES_3_2.add(TEST_4);
		FAILURES_3_2.add(TEST_5);
	}
	
	/* node1 in graph2 */
	private static final TestResultNode NODE_1_2 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_2, FAILURES_1_2));
	/* node2 in graph2 */
	private static final TestResultNode NODE_2_2 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_2, FAILURES_2_2));
	/* node3 in graph2 */
	private static final TestResultNode NODE_3_2 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_2, FAILURES_3_2));
	
	/* parents of node1 in graph2 */
	private static final List<TestResultNode> PARENTS_1_2 = new ArrayList<TestResultNode>();
	/* parents of node2 in graph2 */
	private static final List<TestResultNode> PARENTS_2_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_2.add(NODE_1_2);
	}
	/* parents of node3 in graph2 */
	private static final List<TestResultNode> PARENTS_3_2 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_2.add(NODE_2_2);
	}
	
	/* all tests in graph3 */
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
		ALL_TESTS_3.add(TEST_9);
		ALL_TESTS_3.add(TEST_10);
		ALL_TESTS_3.add(TEST_11);
		ALL_TESTS_3.add(TEST_12);
	}
	
	/* failures of node1 in graph3 */
	private static final Set<String> FAILURES_1_3 = new HashSet<String>();
	static
	{
		FAILURES_1_3.add(TEST_3);
		FAILURES_1_3.add(TEST_4);
		FAILURES_1_3.add(TEST_5);
		FAILURES_1_3.add(TEST_6);
		FAILURES_1_3.add(TEST_7);
		FAILURES_1_3.add(TEST_12);
	}
	/* failures of node2 in graph3 */
	private static final Set<String> FAILURES_2_3 = new HashSet<String>();
	static
	{
		FAILURES_2_3.add(TEST_2);
		FAILURES_2_3.add(TEST_3);
		FAILURES_2_3.add(TEST_4);
		FAILURES_2_3.add(TEST_5);
		FAILURES_2_3.add(TEST_11);
		FAILURES_2_3.add(TEST_12);
	}
	/* failures of node3 in graph3 */
	private static final Set<String> FAILURES_3_3 = new HashSet<String>();
	static
	{
		FAILURES_3_3.add(TEST_1);
		FAILURES_3_3.add(TEST_2);
		FAILURES_3_3.add(TEST_3);
		FAILURES_3_3.add(TEST_10);
		FAILURES_3_3.add(TEST_11);
		FAILURES_3_3.add(TEST_12);
	}
	/* failures of node4 in graph3 */
	private static final Set<String> FAILURES_4_3 = new HashSet<String>();
	static
	{
		FAILURES_4_3.add(TEST_4);
		FAILURES_4_3.add(TEST_7);
		FAILURES_4_3.add(TEST_9);
		FAILURES_4_3.add(TEST_10);
		FAILURES_4_3.add(TEST_11);
		FAILURES_4_3.add(TEST_12);
	}
	
	/* node1 in graph3 */
	private static final TestResultNode NODE_1_3 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_3, FAILURES_1_3));
	/* node2 in graph3 */
	private static final TestResultNode NODE_2_3 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_3, FAILURES_2_3));
	/* node3 in graph3 */
	private static final TestResultNode NODE_3_3 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_3, FAILURES_3_3));
	/* node4 in graph3 */
	private static final TestResultNode NODE_4_3 = new TestResultNode("4", 
			new TestResult(ALL_TESTS_3, FAILURES_4_3));
	
	/* parents of node1 in graph3 */
	private static final List<TestResultNode> PARENTS_1_3 = new ArrayList<TestResultNode>();
	/* parents of node2 in graph3 */
	private static final List<TestResultNode> PARENTS_2_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_3.add(NODE_1_3);
	}
	/* parents of node3 in graph3 */
	private static final List<TestResultNode> PARENTS_3_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_3.add(NODE_1_3);
	}
	/* parents of node4 in graph3 */
	private static final List<TestResultNode> PARENTS_4_3 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_4_3.add(NODE_3_3);
		PARENTS_4_3.add(NODE_2_3);
	}
	
	/* all tests in graph4 */
	private static final Set<String> ALL_TESTS_4 = new HashSet<String>();
	static
	{
		ALL_TESTS_4.add(TEST_1);
		ALL_TESTS_4.add(TEST_2);
		ALL_TESTS_4.add(TEST_3);
		ALL_TESTS_4.add(TEST_4);
		ALL_TESTS_4.add(TEST_5);
		ALL_TESTS_4.add(TEST_6);
		ALL_TESTS_4.add(TEST_7);
		ALL_TESTS_4.add(TEST_8);
		ALL_TESTS_4.add(TEST_9);
		ALL_TESTS_4.add(TEST_10);
	}
	
	/* failures of node1 in graph4 */
	private static final Set<String> FAILURES_1_4 = new HashSet<String>();
	static
	{
		FAILURES_1_4.add(TEST_5);
		FAILURES_1_4.add(TEST_6);
		FAILURES_1_4.add(TEST_7);
		FAILURES_1_4.add(TEST_8);
		FAILURES_1_4.add(TEST_9);
		FAILURES_1_4.add(TEST_10);
	}
	
	/* failures of node2 in graph4 */
	private static final Set<String> FAILURES_2_4 = new HashSet<String>();
	static
	{
		FAILURES_2_4.add(TEST_4);
		FAILURES_2_4.add(TEST_5);
		FAILURES_2_4.add(TEST_6);
		FAILURES_2_4.add(TEST_8);
		FAILURES_2_4.add(TEST_9);
	}
	
	/* failures of node3 in graph4 */
	private static final Set<String> FAILURES_3_4 = new HashSet<String>();
	static
	{
		FAILURES_3_4.add(TEST_3);
		FAILURES_3_4.add(TEST_4);
		FAILURES_3_4.add(TEST_5);
		FAILURES_3_4.add(TEST_7);
		FAILURES_3_4.add(TEST_8);
		FAILURES_3_4.add(TEST_10);
	}
	
	/* failures of node4 in graph4 */
	private static final Set<String> FAILURES_4_4 = new HashSet<String>();
	static
	{
		FAILURES_4_4.add(TEST_1);
		FAILURES_4_4.add(TEST_2);
		FAILURES_4_4.add(TEST_3);
		FAILURES_4_4.add(TEST_4);
		FAILURES_4_4.add(TEST_5);
		FAILURES_4_4.add(TEST_6);
		FAILURES_4_4.add(TEST_7);
		FAILURES_4_4.add(TEST_9);
		FAILURES_4_4.add(TEST_10);
	}
	
	/* failures of node5 in graph4 */
	private static final Set<String> FAILURES_5_4 = new HashSet<String>();
	static
	{
		FAILURES_5_4.add(TEST_1);
		FAILURES_5_4.add(TEST_2);
		FAILURES_5_4.add(TEST_3);
		FAILURES_5_4.add(TEST_4);
		FAILURES_5_4.add(TEST_6);
		FAILURES_5_4.add(TEST_7);
		FAILURES_5_4.add(TEST_8);
		FAILURES_5_4.add(TEST_10);
	}
	
	/* failures of node6 in graph4 */
	private static final Set<String> FAILURES_6_4 = new HashSet<String>();
	static
	{
		FAILURES_6_4.add(TEST_2);
		FAILURES_6_4.add(TEST_3);
		FAILURES_6_4.add(TEST_4);
		FAILURES_6_4.add(TEST_5);
		FAILURES_6_4.add(TEST_6);
		FAILURES_6_4.add(TEST_7);
		FAILURES_6_4.add(TEST_9);
	}
	
	/* failures of node7 in graph4 */
	private static final Set<String> FAILURES_7_4 = new HashSet<String>();
	
	/* node1 in graph4 */
	private static final TestResultNode NODE_1_4 = new TestResultNode("1", 
			new TestResult(ALL_TESTS_4, FAILURES_1_4));
	/* node2 in graph4 */
	private static final TestResultNode NODE_2_4 = new TestResultNode("2", 
			new TestResult(ALL_TESTS_4, FAILURES_2_4));
	/* node3 in graph4 */
	private static final TestResultNode NODE_3_4 = new TestResultNode("3", 
			new TestResult(ALL_TESTS_4, FAILURES_3_4));
	/* node4 in graph4 */
	private static final TestResultNode NODE_4_4 = new TestResultNode("4", 
			new TestResult(ALL_TESTS_4, FAILURES_4_4));
	/* node5 in graph4 */
	private static final TestResultNode NODE_5_4 = new TestResultNode("5", 
			new TestResult(ALL_TESTS_4, FAILURES_5_4));
	/* node6 in graph4 */
	private static final TestResultNode NODE_6_4 = new TestResultNode("6", 
			new TestResult(ALL_TESTS_4, FAILURES_6_4));
	/* node7 in graph4 */
	private static final TestResultNode NODE_7_4 = new TestResultNode("7", 
			new TestResult(ALL_TESTS_4, FAILURES_7_4));
	
	/* parents of node1 in graph4 */
	private static final List<TestResultNode> PARENTS_1_4 = new ArrayList<TestResultNode>();
	/* parents of node2 in graph4 */
	private static final List<TestResultNode> PARENTS_2_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_2_4.add(NODE_1_4);
	}
	/* parents of node3 in graph4 */
	private static final List<TestResultNode> PARENTS_3_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_3_4.add(NODE_2_4);
	}
	/* parents of node4 in graph4 */
	private static final List<TestResultNode> PARENTS_4_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_4_4.add(NODE_2_4);
	}
	/* parents of node5 in graph4 */
	private static final List<TestResultNode> PARENTS_5_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_5_4.add(NODE_4_4);
		PARENTS_5_4.add(NODE_3_4);
	}
	/* parents of node6 in graph4 */
	private static final List<TestResultNode> PARENTS_6_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_6_4.add(NODE_1_4);
	}
	/* parents of node7 in graph4 */
	private static final List<TestResultNode> PARENTS_7_4 = new ArrayList<TestResultNode>();
	static
	{
		PARENTS_7_4.add(NODE_6_4);
		PARENTS_7_4.add(NODE_5_4);
	}
	
	private static final Map<String, List<BugFix>> EXPECTED_BUG_FIXES_1 = 
		new HashMap<String, List<BugFix>>();
	static
	{
		List<BugFix> fixList_4 = new ArrayList<BugFix>(); // of test4
		BugFix fix_1_4 = new BugFix(NODE_2_1); // 1st fix of test4
		fix_1_4.addNodeFail(NODE_1_1);
		fixList_4.add(fix_1_4);
		
		EXPECTED_BUG_FIXES_1.put(TEST_4, fixList_4);
	}
	
	private static final Map<String, List<BugFix>> EXPECTED_BUG_FIXES_2 = 
		new HashMap<String, List<BugFix>>();
	static
	{
		List<BugFix> fixList_1 = new ArrayList<BugFix>(); // of test1
		BugFix fix_1_1 = new BugFix(NODE_3_2); // 1st fix of test1
		fix_1_1.addNodeFail(NODE_2_2);
		fixList_1.add(fix_1_1);
		
		List<BugFix> fixList_2 = new ArrayList<BugFix>(); // of test2
		BugFix fix_1_2 = new BugFix(NODE_3_2); // 1st fix of test2
		fix_1_2.addNodeFail(NODE_2_2);
		fix_1_2.addNodeFail(NODE_1_2);
		fixList_2.add(fix_1_2);
		
		List<BugFix> fixList_3 = new ArrayList<BugFix>(); // of test3
		BugFix fix_1_3 = new BugFix(NODE_2_2); // 1st fix of test3
		fix_1_3.addNodeFail(NODE_1_2);
		fixList_3.add(fix_1_3);
		
		List<BugFix> fixList_4 = new ArrayList<BugFix>(); // of test4
		BugFix fix_1_4 = new BugFix(NODE_2_2); // 1st fix of test4
		fix_1_4.addNodeFail(NODE_1_2);
		fixList_4.add(fix_1_4);
		
		EXPECTED_BUG_FIXES_2.put(TEST_1, fixList_1);
		EXPECTED_BUG_FIXES_2.put(TEST_2, fixList_2);
		EXPECTED_BUG_FIXES_2.put(TEST_3, fixList_3);
		EXPECTED_BUG_FIXES_2.put(TEST_4, fixList_4);
	}
	
	private static final Map<String, List<BugFix>> EXPECTED_BUG_FIXES_3 = 
		new HashMap<String, List<BugFix>>();
	static
	{
		List<BugFix> fixList_1 = new ArrayList<BugFix>(); // of test1
		BugFix fix_1_1 = new BugFix(NODE_4_3); // 1st fix of test1
		fix_1_1.addNodeFail(NODE_3_3);
		fixList_1.add(fix_1_1);
		
		List<BugFix> fixList_2 = new ArrayList<BugFix>(); // of test2
		BugFix fix_1_2 = new BugFix(NODE_4_3); // 1st fix of test2
		fix_1_2.addNodeFail(NODE_3_3);
		fix_1_2.addNodeFail(NODE_2_3);
		fixList_2.add(fix_1_2);
		
		List<BugFix> fixList_3 = new ArrayList<BugFix>(); // of test3
		BugFix fix_1_3 = new BugFix(NODE_4_3); // 1st fix of test3
		fix_1_3.addNodeFail(NODE_3_3);
		fix_1_3.addNodeFail(NODE_2_3);
		fix_1_3.addNodeFail(NODE_1_3);
		fixList_3.add(fix_1_3);
		
		List<BugFix> fixList_4 = new ArrayList<BugFix>(); // of test4
		BugFix fix_1_4 = new BugFix(NODE_3_3); // 1st fix of test4
		fix_1_4.addNodeFail(NODE_1_3);
		fixList_4.add(fix_1_4);
		
		List<BugFix> fixList_5 = new ArrayList<BugFix>(); // of test5
		BugFix fix_1_5 = new BugFix(NODE_4_3); // 1st fix of test5
		fix_1_5.addNodeFail(NODE_2_3);
		fix_1_5.addNodeFail(NODE_1_3);
		BugFix fix_2_5 = new BugFix(NODE_3_3); // 2nd fix of test5
		fix_2_5.addNodeFail(NODE_1_3);
		fixList_5.add(fix_1_5);
		fixList_5.add(fix_2_5);
		
		List<BugFix> fixList_6 = new ArrayList<BugFix>(); // of test6
		BugFix fix_1_6 = new BugFix(NODE_3_3); // 1st fix of test6
		fix_1_6.addNodeFail(NODE_1_3);
		BugFix fix_2_6 = new BugFix(NODE_2_3); // 2nd fix of test6
		fix_2_6.addNodeFail(NODE_1_3);
		fixList_6.add(fix_1_6);
		fixList_6.add(fix_2_6);
		
		List<BugFix> fixList_7 = new ArrayList<BugFix>(); // of test7
		BugFix fix_1_7 = new BugFix(NODE_3_3); // 1st fix of test7
		fix_1_7.addNodeFail(NODE_1_3);
		BugFix fix_2_7 = new BugFix(NODE_2_3); // 2nd fix of test7
		fix_2_7.addNodeFail(NODE_1_3);
		fixList_7.add(fix_1_7);
		fixList_7.add(fix_2_7);
		
		EXPECTED_BUG_FIXES_3.put(TEST_1, fixList_1);
		EXPECTED_BUG_FIXES_3.put(TEST_2, fixList_2);
		EXPECTED_BUG_FIXES_3.put(TEST_3, fixList_3);
		EXPECTED_BUG_FIXES_3.put(TEST_4, fixList_4);
		EXPECTED_BUG_FIXES_3.put(TEST_5, fixList_5);
		EXPECTED_BUG_FIXES_3.put(TEST_6, fixList_6);
		EXPECTED_BUG_FIXES_3.put(TEST_7, fixList_7);
	}
	
	private static final Map<String, List<BugFix>> EXPECTED_BUG_FIXES_4 = 
		new HashMap<String, List<BugFix>>();
	static
	{
		List<BugFix> fixList_1 = new ArrayList<BugFix>(); // of test1
		BugFix fix_1_1 = new BugFix(NODE_7_4); // 1st fix of test1
		fix_1_1.addNodeFail(NODE_5_4);
		fix_1_1.addNodeFail(NODE_4_4);
		fixList_1.add(fix_1_1);
		
		List<BugFix> fixList_2 = new ArrayList<BugFix>(); // of test2
		BugFix fix_1_2 = new BugFix(NODE_7_4); // 1st fix of test2
		fix_1_2.addNodeFail(NODE_6_4);
		fix_1_2.addNodeFail(NODE_5_4);
		fix_1_2.addNodeFail(NODE_4_4);
		fixList_2.add(fix_1_2);
		
		List<BugFix> fixList_3 = new ArrayList<BugFix>(); // of test3
		BugFix fix_1_3 = new BugFix(NODE_7_4); // 1st fix of test3
		fix_1_3.addNodeFail(NODE_6_4);
		fix_1_3.addNodeFail(NODE_5_4);
		fix_1_3.addNodeFail(NODE_4_4);
		fix_1_3.addNodeFail(NODE_3_4);
		fixList_3.add(fix_1_3);
		
		List<BugFix> fixList_4 = new ArrayList<BugFix>(); // of test4
		BugFix fix_1_4 = new BugFix(NODE_7_4); // 1st fix of test4
		fix_1_4.addNodeFail(NODE_6_4);
		fix_1_4.addNodeFail(NODE_5_4);
		fix_1_4.addNodeFail(NODE_4_4);
		fix_1_4.addNodeFail(NODE_3_4);
		fix_1_4.addNodeFail(NODE_2_4);
		fixList_4.add(fix_1_4);
		
		List<BugFix> fixList_5 = new ArrayList<BugFix>(); // of test5
		BugFix fix_1_5 = new BugFix(NODE_7_4); // 1st fix of test5
		fix_1_5.addNodeFail(NODE_6_4);
		fix_1_5.addNodeFail(NODE_1_4);
		BugFix fix_2_5 = new BugFix(NODE_5_4); // 2nd fix of test5
		fix_2_5.addNodeFail(NODE_4_4);
		fix_2_5.addNodeFail(NODE_3_4);
		fix_2_5.addNodeFail(NODE_2_4);
		fix_2_5.addNodeFail(NODE_1_4);
		fixList_5.add(fix_1_5);
		fixList_5.add(fix_2_5);
		
		List<BugFix> fixList_6 = new ArrayList<BugFix>(); // of test6
		BugFix fix_1_6 = new BugFix(NODE_7_4); // 1st fix of test6
		fix_1_6.addNodeFail(NODE_6_4);
		fix_1_6.addNodeFail(NODE_5_4);
		fix_1_6.addNodeFail(NODE_1_4);
		fix_1_6.addNodeFail(NODE_4_4);
		fix_1_6.addNodeFail(NODE_2_4);
		BugFix fix_2_6 = new BugFix(NODE_3_4); // 2nd fix of test6
		fix_2_6.addNodeFail(NODE_2_4);
		fix_2_6.addNodeFail(NODE_1_4);
		fixList_6.add(fix_1_6);
		fixList_6.add(fix_2_6);
		
		List<BugFix> fixList_7 = new ArrayList<BugFix>(); // of test7
		BugFix fix_1_7 = new BugFix(NODE_7_4); // 1st fix of test7
		fix_1_7.addNodeFail(NODE_6_4);
		fix_1_7.addNodeFail(NODE_5_4);
		fix_1_7.addNodeFail(NODE_1_4);
		fix_1_7.addNodeFail(NODE_4_4);
		fix_1_7.addNodeFail(NODE_3_4);
		BugFix fix_2_7 = new BugFix(NODE_2_4); // 2nd fix of test7
		fix_2_7.addNodeFail(NODE_1_4);
		fixList_7.add(fix_1_7);
		fixList_7.add(fix_2_7);
		
		List<BugFix> fixList_8 = new ArrayList<BugFix>(); // of test8
		BugFix fix_1_8 = new BugFix(NODE_7_4); // 1st fix of test8
		fix_1_8.addNodeFail(NODE_5_4);
		fix_1_8.addNodeFail(NODE_3_4);
		fix_1_8.addNodeFail(NODE_2_4);
		fix_1_8.addNodeFail(NODE_1_4);
		BugFix fix_2_8 = new BugFix(NODE_6_4); // 2nd fix of test8
		fix_2_8.addNodeFail(NODE_1_4);
		BugFix fix_3_8 = new BugFix(NODE_4_4); // 3rd fix of test8
		fix_3_8.addNodeFail(NODE_2_4);
		fix_3_8.addNodeFail(NODE_1_4);
		fixList_8.add(fix_1_8);
		fixList_8.add(fix_2_8);
		fixList_8.add(fix_3_8);
		
		List<BugFix> fixList_9 = new ArrayList<BugFix>(); // of test9
		BugFix fix_1_9 = new BugFix(NODE_7_4); // 1st fix of test9
		fix_1_9.addNodeFail(NODE_6_4);
		fix_1_9.addNodeFail(NODE_1_4);
		BugFix fix_2_9 = new BugFix(NODE_5_4); // 2nd fix of test9
		fix_2_9.addNodeFail(NODE_4_4);
		fix_2_9.addNodeFail(NODE_2_4);
		fix_2_9.addNodeFail(NODE_1_4);
		BugFix fix_3_9 = new BugFix(NODE_3_4); // 3rd fix of test9
		fix_3_9.addNodeFail(NODE_2_4);
		fix_3_9.addNodeFail(NODE_1_4);
		fixList_9.add(fix_1_9);
		fixList_9.add(fix_2_9);
		fixList_9.add(fix_3_9);
		
		List<BugFix> fixList_10 = new ArrayList<BugFix>(); // of test10
		BugFix fix_1_10 = new BugFix(NODE_7_4); // 1st fix of test10
		fix_1_10.addNodeFail(NODE_5_4);
		fix_1_10.addNodeFail(NODE_4_4);
		fix_1_10.addNodeFail(NODE_3_4);
		BugFix fix_2_10 = new BugFix(NODE_6_4); // 2nd fix of test10
		fix_2_10.addNodeFail(NODE_1_4);
		BugFix fix_3_10 = new BugFix(NODE_2_4); // 3rd fix of test10
		fix_3_10.addNodeFail(NODE_1_4);
		fixList_10.add(fix_1_10);
		fixList_10.add(fix_2_10);
		fixList_10.add(fix_3_10);
		
		EXPECTED_BUG_FIXES_4.put(TEST_1, fixList_1);
		EXPECTED_BUG_FIXES_4.put(TEST_2, fixList_2);
		EXPECTED_BUG_FIXES_4.put(TEST_3, fixList_3);
		EXPECTED_BUG_FIXES_4.put(TEST_4, fixList_4);
		EXPECTED_BUG_FIXES_4.put(TEST_5, fixList_5);
		EXPECTED_BUG_FIXES_4.put(TEST_6, fixList_6);
		EXPECTED_BUG_FIXES_4.put(TEST_7, fixList_7);
		EXPECTED_BUG_FIXES_4.put(TEST_8, fixList_8);
		EXPECTED_BUG_FIXES_4.put(TEST_9, fixList_9);
		EXPECTED_BUG_FIXES_4.put(TEST_10, fixList_10);
	}
	
	@Test
	public void testBuildHistoryGraph1()
	{
		checkHistoryGraph(DIR_1, MAP_NODE_PARENTS_1);
	}
	
	@Test
	public void testBuildHistoryGraph2()
	{
		checkHistoryGraph(DIR_2, MAP_NODE_PARENTS_2);
	}
	
	@Test
	public void testBuildHistoryGraph3()
	{
		checkHistoryGraph(DIR_3, MAP_NODE_PARENTS_3);
	}
	
	@Test
	public void testBuildHistoryGraph4()
	{
		checkHistoryGraph(DIR_4, MAP_NODE_PARENTS_4);
	}
	
	@Test
	public void testBuildHistoryGraph5()
	{
		checkHistoryGraph(DIR_5, MAP_NODE_PARENTS_5);
	}
	
	private void checkHistoryGraph(String dir, Map<TestResultNode, List<TestResultNode>> map)
	{
		HistoryGraph graph = HistoryGraphBuilder.buildHistoryGraph(dir);
		
		Iterator<TestResultNode> itr = graph.getNodeIterator();
		while (itr.hasNext())
		{
			TestResultNode node = itr.next();
			List<TestResultNode> parents = graph.getParents(node);

			assertTrue(map.containsKey(node));
			assertEquals(map.get(node), parents);
		}
	}
	
	@Test
	public void testAddBugFixesInfo1()
	{	
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_1, PARENTS_1_1);
		graph.addNode(NODE_2_1, PARENTS_2_1);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_2_1);
		checkBugFixes(EXPECTED_BUG_FIXES_1, graph);
	}
	
	@Test
	public void testAddBugFixesInfo2()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_2, PARENTS_1_2);
		graph.addNode(NODE_2_2, PARENTS_2_2);
		graph.addNode(NODE_3_2, PARENTS_3_2);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_3_2);
		checkBugFixes(EXPECTED_BUG_FIXES_2, graph);
	}
	
	@Test
	public void testAddBugFixesInfo3()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_3, PARENTS_1_3);
		graph.addNode(NODE_2_3, PARENTS_2_3);
		graph.addNode(NODE_3_3, PARENTS_3_3);
		graph.addNode(NODE_4_3, PARENTS_4_3);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_4_3);
		checkBugFixes(EXPECTED_BUG_FIXES_3, graph);
	}
	
	@Test
	public void testAddBugFixesInfo4()
	{
		HistoryGraph graph = new HistoryGraph();
		graph.addNode(NODE_1_4, PARENTS_1_4);
		graph.addNode(NODE_2_4, PARENTS_2_4);
		graph.addNode(NODE_3_4, PARENTS_3_4);
		graph.addNode(NODE_4_4, PARENTS_4_4);
		graph.addNode(NODE_5_4, PARENTS_5_4);
		graph.addNode(NODE_6_4, PARENTS_6_4);
		graph.addNode(NODE_7_4, PARENTS_7_4);
		
		HistoryGraphBuilder.addBugFixesInfo(graph, NODE_7_4);
		checkBugFixes(EXPECTED_BUG_FIXES_4, graph);
	}
	
	private void checkBugFixes(Map<String, List<BugFix>> expected, HistoryGraph graph)
	{
		Iterator<String> itr = graph.getBugIterator();
		int bugCount = 0;
		while (itr.hasNext())
		{
			String bug = itr.next();
			bugCount++;
			List<BugFix> bugFixes = graph.getBugFixList(bug);

			assertTrue(expected.containsKey(bug));
			assertEquals(expected.get(bug), bugFixes);
		}
		
		assertTrue(bugCount == expected.size());
	}
}
