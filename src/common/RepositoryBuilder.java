package common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class RepositoryBuilder {
    private static final String SINGLE_TEST_CMD = "ant junit-test -Dtest.name=";
    private static final String PROCESSING_COMMIT = "Processing commit";
    private static final String PROCESSING_PARENTS = "Processing parents of commit";

    /**
     * @param path
     *            : full path to directory of the repository
     * @param commit
     *            : starting commit
     * @return Repository
     */
    public static Repository buildRepository(String path, String commit) {
        File directory = new File(path);
        Repository repo = new Repository(directory);

        int commitCount = 0;

        TestResult startNode = repo.getTestResult(commit);

        Queue<TestResult> q = new LinkedList<TestResult>();
        q.add(startNode);

        Set<String> visited = new HashSet<String>();
        visited.add(commit);

        while (!q.isEmpty()) {
            TestResult next = q.poll();
            printInProgress(next.getCommit(), PROCESSING_PARENTS);

            // process 'next'
            String currCommit = next.getCommit();
            List<String> parentCommits = repo.getParentCommits(currCommit);

            List<TestResult> parents = new ArrayList<TestResult>();

            for (String parentCommit : parentCommits) {
                TestResult parent = repo.getTestResult(parentCommit);
                parents.add(parent);

                if (!visited.contains(parentCommit)) {
                    q.add(parent);
                    visited.add(parentCommit);
                }

                // check for sudden fail and find diff
                checkSuddenFail(repo, next, parent);
            }
            // add 'next' to repository
            repo.addNode(next, parents);

            commitCount++;
            printCommitCompleted(next, commitCount);
        }

        return repo;
    }

    private static void printInProgress(String commit, String message) {
        System.out.println(message + " " + commit);
    }

    private static void printCommitCompleted(TestResult node, int count) {
        System.out.println("Completed this commit and its parents");
        System.out.println("(" + count + ") " + node);
    }

    /**
     * check if a test suddenly fails ie. the test passes in 'parent' but fails
     * in 'node' find diff files between node and parent
     */
    public static void checkSuddenFail(Repository repo, TestResult node,
            TestResult parent) {
        Map<Difference, List<String>> diffCache = new HashMap<Difference, List<String>>();

        for (String test : node.getAllTests()) {
            if (node.fail(test) && parent.pass(test)) {
                Difference dummyDiff = new Difference(node, parent, null);
                List<String> changedFiles = null;

                if (diffCache.containsKey(dummyDiff)) {
                    changedFiles = diffCache.get(dummyDiff);
                } else {
                    changedFiles = repo.getChangedFiles(node.getCommit(),
                            parent.getCommit());
                    diffCache.put(dummyDiff, changedFiles);
                }

                Difference diff = new Difference(node, parent, changedFiles);

                System.out.println("Failed Test: " + test);
                System.out.println(diff);
            }
        }
    }

    public static boolean passSingleTest(Repository repo, String commit,
            String testName) {
        String command = SINGLE_TEST_CMD + testName;
        TestResult result = repo.getTestResult(commit, command.split(" "));

        return result.getFailures().isEmpty();
    }
}
