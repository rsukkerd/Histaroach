package common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public final class HistoryGraphBuilder {

    /**
     * @param repoDir
     *            : full path to directory of the repository
     * @return HistoryGraph of the repository
     */
    public static HistoryGraph buildHistoryGraph(Repository repo) {

        HistoryGraph historyGraph = new HistoryGraph();

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
            historyGraph.addNode(next, parents);

            commitCount++;
            printProgress(next, commitCount);
        }

        addBugFixesInfo(historyGraph, masterNode);

        return historyGraph;
    }

    /**
     * add information about bug fixes to historyGraph
     * 
     * @modifies historyGraph
     */
    public static void addBugFixesInfo(HistoryGraph historyGraph,
            TestResultNode masterNode) {
        Queue<TestResultNode> queue = new LinkedList<TestResultNode>();
        queue.add(masterNode);

        Set<TestResultNode> visitedNodes = new HashSet<TestResultNode>();
        visitedNodes.add(masterNode);

        while (!queue.isEmpty()) {
            TestResultNode next = queue.poll();

            // find all fixed bugs in 'next'
            Set<String> fixedBugs = new HashSet<String>();

            List<TestResultNode> parents = historyGraph.getParents(next);
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

                    for (TestResultNode p : historyGraph.getParents(failNode)) {
                        if (!subVisitedNodes.contains(p) && p.fail(bug)) {
                            failQueue.add(p);
                            bugFix.addNodeFail(p);
                            subVisitedNodes.add(p);
                        }
                    }
                }

                historyGraph.addBugFix(bug, bugFix);
            }
        }
    }

    private static void printProgress(TestResultNode node, int count) {
        System.out.println("(" + count + ") " + node);
    }
}
