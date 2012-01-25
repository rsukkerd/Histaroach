package common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class DataExtractor {
    private static final String[] LOG_COMMAND = { "git", "log",
            "--pretty=format:%h %p" };

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

                        List<String> diffFiles = repo.getChangedFiles(commit,
                                parent);
                        for (String file : diffFiles) {
                            outFileWriter.write(file + "\n");
                        }
                    }
                }

                TestResult testResult = repo.getTestResult(commit);
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

}
