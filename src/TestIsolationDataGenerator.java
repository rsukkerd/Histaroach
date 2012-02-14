import java.io.IOException;
import java.util.Iterator;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.Util;

public class TestIsolationDataGenerator {
	public static final String FILE_PREFIX = "historyGraph";
    public static final String SERIALIZED_EXTENSION = ".ser";
    public static final String HUMAN_READ_EXTENSION = ".log";

    /**
	 * Print the short usage message.
	 */
	@OptionGroup("General Options")
	@Option(value = "-h Print short usage message", aliases = { "-help" })
	public static boolean showHelp = false;
	
	/**
	 * run ant command
	 */
	@Option(value = "-a ant command", aliases = { "-antCommand" })
    public static String antCommand = null;
	
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

        HistoryGraph historyGraph = extractData();
        
        if (startTResultID != null && endTResultID != null) {
        	populateTestResults(historyGraph);
        }
        
        String fileName = "";
        if (startTResultID != null && endTResultID != null) {
        	fileName = "_" + startHGraphID + "_" + endTResultID;
        }
        
        Util.writeToSerializedFile(outputDirName + FILE_PREFIX + fileName + SERIALIZED_EXTENSION, historyGraph);
        Util.writeToHumanReadableFile(outputDirName + FILE_PREFIX + fileName + HUMAN_READ_EXTENSION, historyGraph);
    }

    /**
     * create a HistoryGraph instance from the given repository
     * @return HistoryGraph
     */
    public static HistoryGraph extractData() throws IOException {
    	Repository repository = new Repository(repositoryDirName, antCommand);
    	HistoryGraph historyGraph = repository.buildHistoryGraph(startHGraphID);
    	
    	return historyGraph;
    }
    
    /**
     * construct a TestResult instance for each revision in 
     * a specified range in historyGraph
     * 
     * @modifies historyGraph
     */
    public static void populateTestResults(HistoryGraph historyGraph) {
    	Iterator<Revision> itr = historyGraph.iterator();
    	Revision revision = null;
    	while (itr.hasNext() && !(revision = itr.next()).getCommitID().equals(startTResultID)) { /* search for start revision */ }
    	
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
