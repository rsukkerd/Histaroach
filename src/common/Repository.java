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
import java.util.Scanner;

import voldemort.VoldemortTestResult;

public class Repository {
    private static final String[] ALL_TESTS_CMD = { "ant", "junit" };
    private final Map<TestResult, List<TestResult>> nodeToChildren;
    private File repositoryDir;

    public Repository(File repositoryDir) {
        nodeToChildren = new HashMap<TestResult, List<TestResult>>();
        this.repositoryDir = repositoryDir;
    }

    /**
     * @return diff files between childCommit and parentCommit
     */
    public List<String> getChangedFiles(String childCommitID,
            String parentCommitID) {
        List<String> files = new ArrayList<String>();

        ProcessBuilder diffBuilder = new ProcessBuilder("git", "diff",
                "--name-status", childCommitID, parentCommitID);
        diffBuilder.directory(repositoryDir);

        try {
            Process diffProcess = diffBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    diffProcess.getInputStream()));

            String line = new String();
            while ((line = reader.readLine()) != null) {
                files.add(line);
            }

            try {
                // make current thread waits until this process terminates
                diffProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        return files;
    }

    /**
     * @param commitID
     *            : commit id
     * @return TestResult of the commit
     */
    public TestResult getTestResult(String commitID) {
        return getTestResult(commitID, ALL_TESTS_CMD);
    }

    /**
     * @param commitID
     *            : commit id
     * @param command
     *            : test command
     * @return TestResult of the commit
     */
    public TestResult getTestResult(String commitID, String[] commands) {
        int exitValue = checkoutCommit(commitID);
        TestResult testResult = null;

        if (exitValue != 0) {
            System.out
                    .println("\'ant junit\' process returns non-zero exit value");
            // TODO: Do something sensible.
            return testResult;
        }

        ProcessBuilder runTestBuilder = new ProcessBuilder(commands);
        runTestBuilder.directory(this.repositoryDir);

        try {
            Process runTestProcess = runTestBuilder.start();

            BufferedReader stdOutputReader = new BufferedReader(
                    new InputStreamReader(runTestProcess.getInputStream()));

            BufferedReader stdErrorReader = new BufferedReader(
                    new InputStreamReader(runTestProcess.getErrorStream()));

            testResult = new VoldemortTestResult(commitID, stdOutputReader,
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
     * @param commitID
     *            : commit id
     * @return exit value of 'git checkout' process
     */
    public int checkoutCommit(String commitID) {
        int exitValue = -1;

        ProcessBuilder checkoutBuilder = new ProcessBuilder("git", "checkout",
                commitID);
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
    public void addNode(TestResult node, List<TestResult> parents) {
        for (TestResult parent : parents) {
            if (!nodeToChildren.containsKey(parent)) {
                List<TestResult> children = new ArrayList<TestResult>();
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
    public Iterator<TestResult> getChildrenIterator(TestResult node) {
        return nodeToChildren.get(node).iterator();
    }

    /**
     * @return list of parent commits (Strings) of this commit
     */
    public List<String> getParentCommits(String commitID) {
        checkoutCommit(commitID);

        ProcessBuilder logBuilder = new ProcessBuilder("git", "log",
                "--parents", "-1");
        logBuilder.directory(repositoryDir);

        List<String> parentCommits = new ArrayList<String>();

        try {
            Process logProcess = logBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    logProcess.getInputStream()));

            String parentsLine = reader.readLine();

            Scanner scanner = new Scanner(parentsLine);
            scanner.next(); // "commit"
            scanner.next(); // this commit
            while (scanner.hasNext()) // parent commits
            {
                parentCommits.add(scanner.next());
            }

            try {
                // make current thread waits until this process terminates
                logProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return parentCommits;
    }
}
