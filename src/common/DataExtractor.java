package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import voldemort.VoldemortTestResult;

public class DataExtractor {
    private static final String[] LOG_COMMAND = { "git", "log",
            "--pretty=format:%h %p" };
    private static final String[] ALL_TESTS_CMD = { "ant", "junit" };
    private static final String SINGLE_TEST_CMD = "ant junit-test -Dtest.name=";

    public static void extractData(String repositoryDirStr, String outputFile,
            String startCommit, String endCommit) throws IOException {
        File repositoryDir = new File(repositoryDirStr);
        FileWriter outFileStream = new FileWriter(outputFile);
        BufferedWriter outFileWriter = new BufferedWriter(outFileStream);

        Repository repo = new Repository(repositoryDir);
        int exitValue = repo.checkoutCommit(startCommit);

        if (exitValue != 0) {
            System.out
                    .println("\'git checkout\' process returns non-zero exit value");
            // TODO: Do something sensible -- graceful recovery.
            return;
        }

        ProcessBuilder logBuilder = new ProcessBuilder(LOG_COMMAND);
        logBuilder.directory(repositoryDir);

        try {
            Process logProcess = logBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    logProcess.getInputStream()));

            String line = new String();
            while ((line = reader.readLine()) != null) {
                String[] hashes = line.split(" ");
                String commit = hashes[0];
                outFileWriter.write("COMMIT " + commit + "\n");

                if (hashes.length > 1) {
                    for (int i = 1; i < hashes.length; i++) {
                        String parent = hashes[i];
                        outFileWriter.write("PARENT " + parent + "\n");
                        outFileWriter.write("DIFF FILES:\n");

                        List<String> diffFiles = getChangedFiles(repositoryDir,
                                commit, parent);
                        for (String file : diffFiles) {
                            outFileWriter.write(file + "\n");
                        }
                    }
                }

                TestResult testResult = getTestResult(repo, commit,
                        ALL_TESTS_CMD);
                if (testResult != null) {
                    outFileWriter.write(testResult.toString());
                }
                outFileWriter.write("\n");

                if (commit.equals(endCommit)) {
                    break;
                }
            }
            outFileWriter.close();

            try {
                // make current thread waits until this process terminates
                logProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return diff files between childCommit and parentCommit
     */
    public static List<String> getChangedFiles(File directory,
            String childCommit, String parentCommit) {
        List<String> files = new ArrayList<String>();

        ProcessBuilder diffBuilder = new ProcessBuilder("git", "diff",
                "--name-status", childCommit, parentCommit);
        diffBuilder.directory(directory);

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
     * @param directory
     *            : repository directory
     * @param commitID
     *            : commit id
     * @param command
     *            : test command
     * @return TestResult of the commit
     */
    public static TestResult getTestResult(Repository repo, String commitID,
            String[] command) {
        int exitValue = repo.checkoutCommit(commitID);
        TestResult testResult = null;

        if (exitValue != 0) {
            System.out
                    .println("\'ant junit\' process returns non-zero exit value");
            // TODO: Do something sensible.
            return testResult;
        }

        ProcessBuilder runTestBuilder = new ProcessBuilder(command);
        runTestBuilder.directory(repo.getDirectory());

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
}
