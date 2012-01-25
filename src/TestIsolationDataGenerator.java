import java.io.IOException;

import common.DataExtractor;

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
    public static String outputFilename = null;

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
    public static String repositoryDir = null;

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

        DataExtractor.extractData(repositoryDir, outputFilename, startCommitID,
                endCommitID);
    }
}
