import java.io.File;

import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.TestParsingStrategy;
import common.Util;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import voldemort.VoldemortTestParsingStrategy;

/**
 * TestIsolationDataGenerator builds a HistoryGraph from voldemort repository
 * and writes each Revision in the graph to a serialized file.
 */
public class TestIsolationDataGenerator {
    // Prefix of files to which HistoryGraph instances are written.
    public static final String HGRAPH_FILE_PREFIX = "historyGraph";

    // Extension of serialized files.
    public static final String SERIALIZED_EXTENSION = ".ser";

    // Extension of human-readable files.
    public static final String HUMAN_READ_EXTENSION = ".log";
    
    public static final String VOLDEMORT_PROJECT_NAME = "voldemort";
    
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
    @Option(value = "-s Starting commit ID for HistoryGraph analysis (Required)",
            aliases = { "-startCommitID" })
    public static String startCommitID = null;
    
    /**
     * The commit ID where the HistoryGraph analysis should terminate.
     */
    @Option(value = "-e Ending commit ID for HistoryGraph analysis (Required)",
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
    
    /**
     * Project name
     */
    @Option(value = "-p Project name (Required)",
    		aliases = { "-projectName" })
    public static String projectName = null;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";

    /**
     * Initial program entrance -- parses the arguments and runs the data
     * extraction.
     * 
     * @param args
     *            : command line arguments.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
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
        		|| outputDirName == null || projectName == null) {
            plumeOptions.print_usage();
            return;
        }
        
        TestParsingStrategy strategy = null;
        if (projectName.equals(VOLDEMORT_PROJECT_NAME)) {
        	strategy = new VoldemortTestParsingStrategy();
        }
        
        assert strategy != null;

        Repository repository = new Repository(repositoryDirName, antCommand, strategy);
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        
        exportTestResults(historyGraph);

        String fileName = "_" + startCommitID + "_" + endCommitID;
        
        Util.writeToHumanReadableFile(outputDirName + File.separatorChar + HGRAPH_FILE_PREFIX + fileName
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
