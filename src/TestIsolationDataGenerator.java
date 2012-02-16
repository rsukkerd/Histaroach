import java.io.IOException;
import java.util.Iterator;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.Util;

// TODO: add class level comment description of what this class does.
public class TestIsolationDataGenerator {
    // TODO: What is this used for/by? Describe.
	public static final String FILE_PREFIX = "historyGraph";
    public static final String SERIALIZED_EXTENSION = ".ser";
    public static final String HUMAN_READ_EXTENSION = ".log";

    // TODO: for each option, you must specify whether the option is required, or optional in the description text.

    /**
	 * Print the short usage message.
	 */
	@OptionGroup("General Options")
	@Option(value = "-h Print short usage message", aliases = { "-help" })
	public static boolean showHelp = false;
	
	/**
	 * The ant command used for running ant. By default this is just 'ant'.
	 */
	@Option(value = "-a ant command", aliases = { "-antCommand" })
    public static String antCommand = "ant";
	
	/**
     * The commit ID from which to begin the HistoryGraph analysis.
     */
    @Option(value = "-S Starting commit ID (HistoryGraph)", aliases = { "-startHGraphID" })
    public static String startHGraphID = null;

    /**
     * The commit ID from which to begin the TestResult analysis.
     */
    @Option(value = "-s Starting commit ID (TestResult)", aliases = { "-startTResultID" })
    public static String startTResultID = null;

    /**
     * The commit ID where the TestResult analysis should terminate.
     */
    @Option(value = "-e Ending commit ID (TestResult)", aliases = { "-endTResultID" })
    public static String endTResultID = null;
    
    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory", aliases = { "-repoDir" })
    public static String repositoryDirName = null;
    
    /**
     * Full path to the output directory.
     */
    @Option(value = "-o Full path to the output directory", aliases = { "-outputDir" })
    public static String outputDirName = null;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";
    
    /**
     * Initial program entrance -- parses the arguments and runs the data
     * extraction.
     * 
     * @param args : command line arguments.
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Options plumeOptions = new Options(TestIsolationDataGenerator.usage_string, TestIsolationDataGenerator.class);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }
        
        // TODO: What if repositoryDirName is not specified, and is null? Then this crashes with a null pointer exception error.
        // To avoid this and similar issues, check that ALL required options are specified, and show an error message if any
        // are not specified.

        Repository repository = new Repository(repositoryDirName, antCommand);
        HistoryGraph historyGraph = repository.buildHistoryGraph(startHGraphID);
        
        if (startTResultID != null && endTResultID != null) {
        	populateTestResults(historyGraph);
        }
        
        String fileName = "";
        if (startTResultID != null && endTResultID != null) {
        	fileName = "_" + startHGraphID + "_" + endTResultID;
        }
        
        // TODO: so if startTResultID and endTResultID are both null, then filename is the empty string?? Shouldn't there
        // be some sort of default start/end resultID that you can use for the filename? Certainly an empty string
        // filename doesn't make sense.
        
        Util.writeToSerializedFile(outputDirName + FILE_PREFIX + fileName + SERIALIZED_EXTENSION, historyGraph);
        Util.writeToHumanReadableFile(outputDirName + FILE_PREFIX + fileName + HUMAN_READ_EXTENSION, historyGraph);
    }

    /**
     * Construct a TestResult instance for each revision in 
     * a specified range in historyGraph.
     * 
     * @modifies historyGraph
     */
    public static void populateTestResults(HistoryGraph historyGraph) {
    	Iterator<Revision> itr = historyGraph.iterator();
    	Revision revision = null;
    	while (itr.hasNext() && !(revision = itr.next()).getCommitID().equals(startTResultID)) { /* search for start revision */ }
    	
        // TODO: Isn't the condition of this if statement the same as the while condition? Please refactor this so that you
        // do not duplicate this logic. Also, do you need the same logic in the if condition? I'm not sure.
        
    	if (revision != null && revision.getCommitID().equals(startTResultID)) {
    		revision.getTestResult();
    		
    		String filename = outputDirName + revision.getCommitID() + ".ser";
    		Util.writeToSerializedFile(filename, revision);
    		
    		if (revision.getCommitID().equals(endTResultID)) {
    			return;
    		}
    		
	    	while (itr.hasNext()) {
	    		revision = itr.next();
	    		revision.getTestResult();
	    		
	    		filename = outputDirName + revision.getCommitID() + ".ser";
	    		Util.writeToSerializedFile(filename, revision);
	    		
	    		if (revision.getCommitID().equals(endTResultID)) {
	    			return;
	    		}
	    	}
    	}
    }
}
