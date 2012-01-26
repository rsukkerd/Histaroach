package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Repository {
    public static final String[] LOG_COMMAND = { "git", "log",
            "--pretty=format:%h %p" };
    public static final String[] JUNIT_COMMAND = { "ant", "junit" };
    public static final String SINGLE_TEST_COMMAND = "ant junit-test -Dtest.name=";

    private final File directory;
    private final String outputFileName;

    public Repository(String pathname, String outputFileName) {
        directory = new File(pathname);
        this.outputFileName = outputFileName;
    }

    public File getDirectory() {
        return directory;
    }

    /**
     * Checks out a particular commit from this repository.
     * 
     * @return exit value of 'git checkout' process
     */
    public int checkoutCommit(String commitID) {
        Process p = Util.runProcess(
                new String[] { "git", "checkout", commitID }, directory);
        return p.exitValue();
    }

    /**
     * @return diff files between childCommit and parentCommit
     * @throws IOException
     */
    public List<String> getDiffFiles(String childID, String parentID)
            throws IOException {
        List<String> diffFiles = new ArrayList<String>();

        Process p = Util.runProcess(new String[] { "git", "diff",
                "--name-status", childID, parentID }, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            diffFiles.add(line);
        }

        return diffFiles;
    }

    public HistoryGraph buildHistoryGraph(String startCommitID,
            String endCommitID) throws IOException {
        HistoryGraph hGraph = new HistoryGraph();

        FileWriter outFileStream = new FileWriter(outputFileName);
        BufferedWriter outFileWriter = new BufferedWriter(outFileStream);

        int exitValue = checkoutCommit(startCommitID);
        assert (exitValue == 0);

        Process logProcess = Util.runProcess(LOG_COMMAND, directory);

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                logProcess.getInputStream()));

        String line = new String();
        while ((line = reader.readLine()) != null) {
            String[] hashes = line.split(" ");

            String commitID = hashes[0];
            Map<String, List<String>> parentIDToDiffFiles = new HashMap<String, List<String>>();

            if (hashes.length > 1) {
                for (int i = 1; i < hashes.length; i++) {
                    String parentID = hashes[i];
                    List<String> diffFiles = getDiffFiles(commitID, parentID);

                    parentIDToDiffFiles.put(parentID, diffFiles);
                }
            }

            Revision revision = new Revision(this, commitID,
                    parentIDToDiffFiles);
            hGraph.addRevision(revision);

            outFileWriter.write(revision.toString());
            outFileWriter.write("\n");

            if (commitID.equals(endCommitID)) {
                break;
            }
        }

        outFileWriter.close();

        return hGraph;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !other.getClass().equals(this.getClass())) {
            return false;
        }

        Repository repository = (Repository) other;
        return directory.equals(repository.directory);
    }

    @Override
    public int hashCode() {
        return directory.hashCode();
    }

    @Override
    public String toString() {
        return "repository location : " + directory.getAbsolutePath();
    }
}
