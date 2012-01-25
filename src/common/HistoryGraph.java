package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class HistoryGraph {
    private final Map<TestResultNode, List<TestResultNode>> nodeToParents;
    private final Map<String, List<BugFix>> bugToFixList;

    public HistoryGraph() {
        nodeToParents = new HashMap<TestResultNode, List<TestResultNode>>();
        bugToFixList = new HashMap<String, List<BugFix>>();
    }

    public HistoryGraph(Repository repo) {
        this();
        int commitCount = 0;

        TestResultNode masterNode = new TestResultNode(repo, "master");

        Queue<TestResultNode> q = new LinkedList<TestResultNode>();
        q.add(masterNode);

        Set<String> visited = new HashSet<String>();
        visited.add("master");

        while (!q.isEmpty()) {
            TestResultNode next = q.poll();

            // process 'next'
            String currCommit = next.getCommit();
            List<String> parentCommits = repo.getParentCommits(currCommit);

            List<TestResultNode> parents = new ArrayList<TestResultNode>();

            for (String parentCommit : parentCommits) {
                TestResultNode parent = new TestResultNode(repo, parentCommit);
                parents.add(parent);

                if (!visited.contains(parentCommit)) {
                    q.add(parent);
                    visited.add(parentCommit);
                }
            }
            // add 'next' to graph
            this.addNode(next, parents);

            commitCount++;
            printProgress(next, commitCount);
        }

        this.addBugFixesInfo(masterNode);
    }

    /**
     * add information about bug fixes to historyGraph
     * 
     * @modifies historyGraph
     */
    public void addBugFixesInfo(TestResultNode masterNode) {
        Queue<TestResultNode> queue = new LinkedList<TestResultNode>();
        queue.add(masterNode);

        Set<TestResultNode> visitedNodes = new HashSet<TestResultNode>();
        visitedNodes.add(masterNode);

        while (!queue.isEmpty()) {
            TestResultNode next = queue.poll();

            // find all fixed bugs in 'next'
            Set<String> fixedBugs = new HashSet<String>();

            List<TestResultNode> parents = getParents(next);
            for (TestResultNode parent : parents) {
                if (!visitedNodes.contains(parent)) {
                    queue.add(parent);
                    visitedNodes.add(parent);
                }

                for (String bug : parent.getTestResult().getFailures()) {
                    if (next.pass(bug)) {
                        fixedBugs.add(bug);
                    }
                }
            }

            for (String bug : fixedBugs) {
                // BFS to find all consecutive nodes, start from 'next', that
                // fail 'bug'
                Queue<TestResultNode> failQueue = new LinkedList<TestResultNode>();
                BugFix bugFix = new BugFix(next);

                Set<TestResultNode> subVisitedNodes = new HashSet<TestResultNode>();

                for (TestResultNode parent : parents) {
                    if (parent.fail(bug)) {
                        failQueue.add(parent);
                        bugFix.addNodeFail(parent);
                        subVisitedNodes.add(parent);
                    }
                }

                while (!failQueue.isEmpty()) {
                    TestResultNode failNode = failQueue.poll();

                    for (TestResultNode p : getParents(failNode)) {
                        if (!subVisitedNodes.contains(p) && p.fail(bug)) {
                            failQueue.add(p);
                            bugFix.addNodeFail(p);
                            subVisitedNodes.add(p);
                        }
                    }
                }

                addBugFix(bug, bugFix);
            }
        }
    }

    /**
     * @param hGraph
     * @return mapping from bug to BugFixes that fix the bug in parallel
     */
    public Map<String, Set<BugFix>> findParallelFixes() {
        Map<String, Set<BugFix>> map = new HashMap<String, Set<BugFix>>();

        Iterator<String> bugItr = getBugIterator();

        while (bugItr.hasNext()) {
            String bug = bugItr.next();
            List<BugFix> bugFixes = getBugFixList(bug);

            for (int i = 0; i < bugFixes.size() - 1; i++) {
                for (int j = i + 1; j < bugFixes.size(); j++) {
                    BugFix fix_A = bugFixes.get(i);
                    BugFix fix_B = bugFixes.get(j);

                    TestResultNode node_A = fix_A.getNodePass();
                    TestResultNode node_B = fix_B.getNodePass();

                    if (node_A.isParallelWith(this, node_B)) {
                        if (!map.containsKey(bug)) {
                            Set<BugFix> parallelFixes = new HashSet<BugFix>();
                            parallelFixes.add(fix_A);
                            parallelFixes.add(fix_B);

                            map.put(bug, parallelFixes);
                        } else {
                            map.get(bug).add(fix_A);
                            map.get(bug).add(fix_B);
                        }
                    }
                }
            }
        }

        return map;
    }

    private static void printProgress(TestResultNode node, int count) {
        System.out.println("(" + count + ") " + node);
    }

    /**
     * add new node to this HistoryGraph
     * 
     * @param node
     *            : node to be added
     * @param parents
     *            : list of parents of the node
     */
    public void addNode(TestResultNode node, List<TestResultNode> parents) {
        nodeToParents.put(node, parents);
    }

    /**
     * @param node
     *            : node to get parents
     * @return list of parents of the node
     */
    public List<TestResultNode> getParents(TestResultNode node) {
        return nodeToParents.get(node);
    }

    /**
     * @return iterator over all nodes
     */
    public Iterator<TestResultNode> getNodeIterator() {
        return nodeToParents.keySet().iterator();
    }

    /**
     * add bug fix information
     * 
     * @param bug
     *            : test that fails in parent node but passes in child node
     * @param fix
     *            : a node that fixes the bug and a list of consecutive nodes
     *            that have the bug
     */
    public void addBugFix(String bug, BugFix fix) {
        if (!bugToFixList.containsKey(bug)) {
            List<BugFix> list = new ArrayList<BugFix>();
            list.add(fix);
            bugToFixList.put(bug, list);
        } else {
            bugToFixList.get(bug).add(fix);
        }
    }

    /**
     * @return iterator over the bugs that get fixed
     */
    public Iterator<String> getBugIterator() {
        return bugToFixList.keySet().iterator();
    }

    /**
     * @param bug
     * @return list of BugFixes of the bug
     */
    public List<BugFix> getBugFixList(String bug) {
        return bugToFixList.get(bug);
    }
}
