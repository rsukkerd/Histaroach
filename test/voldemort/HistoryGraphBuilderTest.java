package voldemort;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class HistoryGraphBuilderTest 
{
	private static final String DIR = "/home/rsukkerd/workspace/sample";
	
	private static final Map<TestResultNode, List<TestResultNode>> MAP = 
		new HashMap<TestResultNode, List<TestResultNode>>();
	
	private static final TestResultNode NODE_0 = 
		new TestResultNode("e9b3ccd52ec7bc4ca2e4a32be6fd96d0e255b755", null);
	private static final TestResultNode NODE_1 = 
		new TestResultNode("2bcd099db334588179c5d75b3a0b79b0ca88bd70", null);
	private static final TestResultNode NODE_2 = 
		new TestResultNode("8ee45d292987a81a9b6e65469cde48a9f7d669a6", null);
	private static final TestResultNode NODE_3 = 
		new TestResultNode("156340de9ccb83cd0d637f8640bf495d11a656fa", null);
	private static final TestResultNode NODE_4 = 
		new TestResultNode("2bf13d7c8c073bf5b5a07d16b650d04ea5a920bd", null);
	private static final TestResultNode NODE_5 = 
		new TestResultNode("d52fce6c257b4569ec71d55769c9d3c97ad3ac0a", null);
	private static final TestResultNode NODE_6 = 
		new TestResultNode("master", null);
	
	private static final List<TestResultNode> PARENTS_0 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_1 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_2 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_3 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_4 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_5 = new ArrayList<TestResultNode>();
	private static final List<TestResultNode> PARENTS_6 = new ArrayList<TestResultNode>();
	
	static 
	{
		PARENTS_1.add(NODE_0);
		PARENTS_2.add(NODE_1);
		PARENTS_3.add(NODE_2);
		PARENTS_4.add(NODE_2);
		PARENTS_5.add(NODE_4);
		PARENTS_6.add(NODE_5);
		PARENTS_6.add(NODE_3);
		
		MAP.put(NODE_0, PARENTS_0);
		MAP.put(NODE_1, PARENTS_1);
		MAP.put(NODE_2, PARENTS_2);
		MAP.put(NODE_3, PARENTS_3);
		MAP.put(NODE_4, PARENTS_4);
		MAP.put(NODE_5, PARENTS_5);
		MAP.put(NODE_6, PARENTS_6);
	}
	
	@Test
	public void testBuildHistoryGraph()
	{
		HistoryGraph graph = HistoryGraphBuilder.buildHistoryGraph(DIR);
		
		Iterator<TestResultNode> itr = graph.getNodeIterator();
		while (itr.hasNext())
		{
			TestResultNode node = itr.next();
			List<TestResultNode> parents = graph.getParents(node);

			assertTrue(MAP.containsKey(node));
			assertEquals(MAP.get(node), parents);
		}
	}
}
