package histaroach;

import histaroach.model.HistoryGraph;
import histaroach.model.Revision;
import histaroach.model.TestResult;
import histaroach.util.HistoryGraphXMLReader;
import histaroach.util.XMLReader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ExploreTestNondeterminism {
	
	private final List<HistoryGraph> historyGraphs;
	// keep track of what tests in what Revision are nondeterministic
	private final Map<Revision, Set<String>> nondeterministicTests;

	public ExploreTestNondeterminism(List<HistoryGraph> historyGraphs) {
		this.historyGraphs = historyGraphs;
		nondeterministicTests = new HashMap<Revision, Set<String>>();
	}
	
	public void explore() throws Exception {
		HistoryGraph hGraphA = historyGraphs.get(0);
		
		for (Revision revisionA : hGraphA) {
			TestResult resultA = revisionA.getTestResult();
			
			for (int i = 1; i < historyGraphs.size(); i++) {
				HistoryGraph hGraphB = historyGraphs.get(i);
				Revision revisionB = hGraphB.lookUpRevision(revisionA.getCommitID());
				
				if (revisionA.isCompilable() != revisionB.isCompilable()) {
		    		throw new Exception("difference in compilability");
		    	}
				
				if (revisionA.hasTestAborted() || revisionB.hasTestAborted()) {
					System.err.println("Test aborted at commit " + revisionA.getCommitID());
		    		continue;
		    	}
				
				TestResult resultB = revisionB.getTestResult();
				Set<String> tests;
				try {
					tests = resultA.getNondeterministicTests(resultB);
					nondeterministicTests.put(revisionA, tests);
				} catch (Exception e) {
					System.err.println("A set of tests of the same Revision " +
							"differs between the 2 runs.");
					System.err.println("At Revision " + revisionA.getCommitID());
					System.err.println("diff tests are " + resultA.diff(resultB));
				}
			}
		}
	}
	
	public void printNondeterministicTests() {
		System.out.println("Nondeterministic tests are:");
		
		for (Revision revision : nondeterministicTests.keySet()) {
			Set<String> tests = nondeterministicTests.get(revision);
			System.out.println("in Revision " + revision.getCommitID() + 
					": " + tests);
		}
	}
	
	/**
	 * @param args - directory containing HistoryGraph xml files
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		File dir = new File(args[0]);
		File[] files = dir.listFiles();
		List<HistoryGraph> points = new ArrayList<HistoryGraph>();
		
		for (File file : files) {
			if (file.getName().startsWith(DataCollector.HISTORYGRAPH_PREFIX)) {
				XMLReader<HistoryGraph> reader = new HistoryGraphXMLReader(file);
			    HistoryGraph hGraph = reader.read();
			    points.add(hGraph);
			}
		}
		
		ExploreTestNondeterminism explorer = new ExploreTestNondeterminism(points);
		explorer.explore();
		explorer.printNondeterministicTests();
	}

}
