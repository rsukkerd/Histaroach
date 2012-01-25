package common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import voldemort.VoldemortTestResult;

public class Repository {
    private static final String[] ALL_TESTS_CMD = { "ant", "junit" };
    private final Map<TestResultNode, List<TestResultNode>> nodeToChildren;
    private File repositoryDir;

    public Repository(File repositoryDir) {
        nodeToChildren = new HashMap<TestResultNode, List<TestResultNode>>();
        this.repositoryDir = repositoryDir;
    }

    /**
     * @param directory
     *            : repository directory
     * @param commitID
     *            : commit id
     * @param command
     *            : test command
     * @return TestResult of the commit
     */
    public TestResult getTestResult(String commitID) {
        int exitValue = checkoutCommit(commitID);
        TestResult testResult = null;

        if (exitValue != 0) {
            System.out
                    .println("\'ant junit\' process returns non-zero exit value");
            // TODO: Do something sensible.
            return testResult;
        }

        ProcessBuilder runTestBuilder = new ProcessBuilder(ALL_TESTS_CMD);
        runTestBuilder.directory(this.repositoryDir);

        try {
            Process runTestProcess = runTestBuilder.start();

            BufferedReader stdOutputReader = new BufferedReader(
                    new InputStreamReader(runTestProcess.getInputStream()));

            BufferedReader stdErrorReader = new BufferedReader(
                    new InputStreamReader(runTestProcess.getErrorStream()));

            testResult = new VoldemortTestResult(stdOutputReader,
                    stdErrorReader);

            try {
                // make current thread waits until this process terminates
                runTestProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return testResult;
    }

    /**
     * Checks out a particular commit for this repository.
     * 
     * @param commit
     *            : commit id
     * @return exit value of 'git checkout' process
     */
    public int checkoutCommit(String commit) {
        int exitValue = -1;

        ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout",
                commit);
        checkoutBuilder.directory(repositoryDir);

        try {
            Process checkoutProcess = checkoutBuilder.start();

            try {
                // make current thread waits until this process terminates
                exitValue = checkoutProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return exitValue;
    }

    /**
     * add new node to this Repository
     * 
     * @param node
     *            : node to be added
     * @param parents
     *            : list of parents of the node
     */
    public void addNode(TestResultNode node, List<TestResultNode> parents) {
        for (TestResultNode parent : parents) {
            if (!nodeToChildren.containsKey(parent)) {
                List<TestResultNode> children = new ArrayList<TestResultNode>();
                children.add(node);
                nodeToChildren.put(parent, children);
            } else {
                nodeToChildren.get(parent).add(node);
            }
        }
    }

    /**
     * @param node
     *            : node to get Iterator over its children
     * @return Iterator over children of the node
     */
    public Iterator<TestResultNode> getChildrenIterator(TestResultNode node) {
        return nodeToChildren.get(node).iterator();
    }
}
