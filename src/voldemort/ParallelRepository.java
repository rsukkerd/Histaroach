package voldemort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import common.BugFix;
import common.HistoryGraph;
import common.ParallelFixing;
import common.Repository;
import common.Revision;

public class ParallelRepository extends Repository {

	public ParallelRepository(String pathname, String outputFileName) {
		super(pathname, outputFileName);
	}
	
	/**
	 * @return a mapping from test name (bug) -> list of all BugFix'es of the test
	 */
	public Map<String, List<BugFix>> getAllBugFixes(HistoryGraph historyGraph) {
		Map<String, List<BugFix>> map = new HashMap<String, List<BugFix>>();
		Iterator<Revision> itr = historyGraph.getRevisionIterator();
		
		while (itr.hasNext()) {
			Revision revision = itr.next();
			List<String> fixedBugs = new ArrayList<String>(); // find all bugs that this revision fixed
			Set<Revision> parents = historyGraph.getParents(revision);
			
			for (Revision parent : parents) {
				for (String failedTest : parent.getTestResult().getFailedTests()) {
					if (revision.getTestResult().pass(failedTest)) {
						fixedBugs.add(failedTest);
					}
				}
			}
			
			for (String test : fixedBugs) {
				// BFS to find all consecutive revisions, start from this revision, that fail this test
				Queue<Revision> queue = new LinkedList<Revision>();
                BugFix bugFix = new BugFix(test, revision);

                Set<Revision> visited = new HashSet<Revision>();

                for (Revision parent : parents) {
                    if (parent.getTestResult().fail(test)) {
                        queue.add(parent);
                        bugFix.addFailedRevision(parent);
                        visited.add(parent);
                    }
                }

                while (!queue.isEmpty()) {
                    Revision nextRevision = queue.poll();

                    for (Revision parent : historyGraph.getParents(nextRevision)) {
                        if (!visited.contains(parent) && parent.getTestResult().fail(test)) {
                            queue.add(parent);
                            bugFix.addFailedRevision(parent);
                            visited.add(parent);
                        }
                    }
                }
                
                if (map.containsKey(test)) {
                	map.get(test).add(bugFix);
                } else {
                	List<BugFix> list = new ArrayList<BugFix>();
                	list.add(bugFix);
                	map.put(test, list);
                }
			}
		}
		
		return map;
	}
	
	public ParallelFixing getParallelFixing(HistoryGraph historyGraph, String bug, List<BugFix> allFixes) {
		Set<BugFix> parallelFixes = new HashSet<BugFix>();
		
		for (int i = 0; i < allFixes.size() - 1; i++) {
			for (int j = i + 1; j < allFixes.size(); j++) {
				BugFix fix_A = allFixes.get(i);
                BugFix fix_B = allFixes.get(j);

                Revision revision_A = fix_A.getPassedRevision();
                Revision revision_B = fix_B.getPassedRevision();

                if (historyGraph.parallel(revision_A, revision_B)) {
                	parallelFixes.add(fix_A);
                	parallelFixes.add(fix_B);
                }
            }
        }
		
		return new ParallelFixing(bug, parallelFixes);
	}
}
