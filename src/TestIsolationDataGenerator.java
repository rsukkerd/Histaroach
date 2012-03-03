import java.io.File;
import java.io.IOException;

import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.Util;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

/**
 * TestIsolationDataGenerator builds a HistoryGraph from voldemort repository
 * and writes each Revision in the graph to a serialized file.
 */
public class TestIsolationDataGenerator {
    // Prefix of files to which HistoryGraph instances are written.
    public static final String FILE_PREFIX = "historyGraph";

    // Extension of serialized files.
    public static final String SERIALIZED_EXTENSION = ".ser";

    // Extension of human-readable files.
    public static final String HUMAN_READ_EXTENSION = ".log";

    /**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * The ant command used for running ant. By default this is just 'ant'.
     */
    @Option(value = "-a ant command (Optional)", aliases = { "-antCommand" })
    public static String antCommand = "ant";

    /**
     * The commit ID from which to begin the HistoryGraph analysis.
     */
    @Option(
            value = "-s Starting commit ID for HistoryGraph analysis (Required)",
            aliases = { "-startCommitID" })
    public static String startCommitID = null;
    
    /**
     * The commit ID where the HistoryGraph analysis should terminate.
     */
    @Option(
            value = "-e Ending commit ID for HistoryGraph analysis (Required)",
            aliases = { "-endCommitID" })
    public static String endCommitID = null;

    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory (Required)",
            aliases = { "-repoDir" })
    public static String repositoryDirName = null;

    /**
     * Full path to the output directory. 
     * Must NOT contain '/' at the end of the path.
     */
    @Option(value = "-o Full path to the output directory (Required)",
            aliases = { "-outputDir" })
    public static String outputDirName = null;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";

    /**
     * Initial program entrance -- parses the arguments and runs the data
     * extraction.
     * 
     * @param args
     *            : command line arguments.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Options plumeOptions = new Options(
                TestIsolationDataGenerator.usage_string,
                TestIsolationDataGenerator.class);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        if (startCommitID == null || endCommitID == null || repositoryDirName == null 
        		|| outputDirName == null) {
            plumeOptions.print_usage();
            return;
        }

        Repository repository = new Repository(repositoryDirName, antCommand);
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        
        exportTestResults(historyGraph);

        String fileName = "_" + startCommitID + "_" + endCommitID;

        Util.writeToSerializedFile(outputDirName + File.separatorChar + FILE_PREFIX + fileName
                + SERIALIZED_EXTENSION, historyGraph);
        Util.writeToHumanReadableFile(outputDirName + File.separatorChar + FILE_PREFIX + fileName
                + HUMAN_READ_EXTENSION, historyGraph);
    }

    /**
     * write each revision in the historyGraph to a serialized file
     */
    public static void exportTestResults(HistoryGraph historyGraph) {
    	for (Revision revision : historyGraph) {
    		String filename = outputDirName + File.separatorChar + revision.getCommitID() + SERIALIZED_EXTENSION;
    		Util.writeToSerializedFile(filename, revision);
    	}
    }
}
