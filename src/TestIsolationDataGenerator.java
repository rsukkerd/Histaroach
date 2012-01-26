
import java.io.IOException;

import common.HistoryGraph;
import common.Repository;

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
     * Full path to the serialized output file.
     */
    @Option(value = "-o File to use for serialized output.", aliases = { "-serializedOutputFile" })
    public static String serializedOutputFileName = null;
    
    /**
     * Full path to the human-readable output file.
     */
    @Option(value = "-o File to use for human-readable output.", aliases = { "-humanReadOutputFile" })
    public static String humanReadOutputFileName = null;

    /**
     * The commit ID from which to begin the analysis.
     */
    @Option(value = "-s Starting commit id")
    public static String startCommitID = null;

    /**
     * The commit ID where the analysis should terminate.
     */
    @Option(value = "-e Ending commit id")
    public static String endCommitID = null;

    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory.",
            aliases = { "-repodir" })
    public static String repositoryDirName = null;

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
        Options plumeOptions = new Options(
                TestIsolationDataGenerator.usage_string);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        HistoryGraph historyGraph = extractData();
        
        
        
        
    }

    /**
     * TODO: Add comment.
     */
    public static HistoryGraph extractData() throws IOException {
    	Repository repository = new Repository(repositoryDirName, humanReadOutputFileName);
    	HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
    	
    	return historyGraph;
    }

}
