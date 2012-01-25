package common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Repository {
    private final Map<TestResultNode, List<TestResultNode>> nodeToChildren;
    private File repositoryDir;

    public Repository(File repositoryDir) {
        nodeToChildren = new HashMap<TestResultNode, List<TestResultNode>>();
        this.repositoryDir = repositoryDir;
    }

    public File getDirectory() {
        return repositoryDir;
    }

    /**
     * Checks out a particular commit for this repository.
     * 
     * @param directory
     *            : repository directory
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
