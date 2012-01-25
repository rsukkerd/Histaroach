import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import common.Repository;
import common.TestResult;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

public class TestIsolationDataGenerator {

    /**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * Full path to the output file.
     */
    @Option(value = "-o File to use for output.", aliases = { "-outputfile" })
    public static String outputFileName = null;

    /**
     * The commit ID from which to begin the analysis.
     */
    @Option(value = "-s Starting commit id")
    public static String startCommitID = null;

    /**
     * The commit ID where the analysis should terminate.
     */
    @Option(value = "-e End commit id")
    public static String endCommitID = null;

    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory.",
            aliases = { "-repodir" })
    public static String repositoryDirName = null;

    private static Options plumeOptions;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";

    /**
     * Initial program entrance -- parses the arguments and runs the data
     * extraction.
     * 
     * @param args
     *            command line arguments.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        plumeOptions = new Options(TestIsolationDataGenerator.usage_string);
        @SuppressWarnings("unused")
        String[] cmdLineArgs = plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        extractData();
    }

    private static final String[] LOG_COMMAND = { "git", "log",
            "--pretty=format:%h %p" };

    private static final String SINGLE_TEST_CMD = "ant junit-test -Dtest.name=";

    /**
     * TODO: Add comment.
     */
    public static void extractData() throws IOException {
        File repositoryDir = new File(repositoryDirName);
        FileWriter outFileStream = new FileWriter(outputFileName);
        BufferedWriter outFileWriter = new BufferedWriter(outFileStream);

        Repository repo = new Repository(repositoryDir);
        int exitValue = repo.checkoutCommit(startCommitID);

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

                if (commit.equals(endCommitID)) {
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
