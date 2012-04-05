import git.GitRepository;

import java.io.File;

import ant.JodatimeBuildStrateygy;
import ant.VoldemortBuildStrategy;

import common.BuildStrategy;
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
    public static final String HGRAPH_FILE_PREFIX = "historyGraph";

    // Extension of serialized files.
    public static final String SERIALIZED_EXTENSION = ".ser";

    // Extension of human-readable files.
    public static final String HUMAN_READ_EXTENSION = ".log";
    
    public static final String VOLDEMORT = "voldemort";
    public static final String JODA_TIME = "joda-time";
    
    /**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;
    
    /**
	 * Project name.
	 */
	@Option(value = "-p Project name (Required)",
	        aliases = { "-projName" })
	public static String projName = null;
    
    /**
	 * Full path to the repository directory.
	 */
	@Option(value = "-r Full path to the repository directory (Required)",
	        aliases = { "-repoPath" })
	public static String repoPath = null;
    
	/**
     * Build command. Default is 'ant'.
     */
    @Option(value = "-b build command (Optional)", 
    		aliases = { "-buildCommand" })
    public static String buildCommand = "ant";
    
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
     * Full path to the output directory. 
     * Must NOT contain '/' at the end of the path.
     */
    @Option(value = "-o Full path to the output directory (Required)",
            aliases = { "-outputPath" })
    public static String outputPath = null;
    
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

        if (projName == null || repoPath == null 
        		|| startCommitID == null || endCommitID == null 
        		|| outputPath == null) {
            plumeOptions.print_usage();
            return;
        }
        
        File repoDir = new File(repoPath);
        
        BuildStrategy buildStrategy = null;
        
        if (projName.equals(VOLDEMORT)) {
        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
        } else if (projName.equals(JODA_TIME)) {
        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
        }
        
        assert buildStrategy != null;
        
        Repository repository = new GitRepository(repoDir, buildStrategy);
        
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        
        exportTestResults(historyGraph);

        String fileName = "_" + startCommitID + "_" + endCommitID;
        
        Util.writeToHumanReadableFile(outputPath + File.separatorChar + HGRAPH_FILE_PREFIX + fileName
                + HUMAN_READ_EXTENSION, historyGraph);
    }

    /**
     * Write each Revision in the HistoryGraph to a serialized file.
     */
    public static void exportTestResults(HistoryGraph historyGraph) {
    	for (Revision revision : historyGraph) {
    		String filename = outputPath + File.separatorChar + revision.getCommitID() + SERIALIZED_EXTENSION;
    		Util.writeToSerializedFile(filename, revision);
    	}
    }
}
