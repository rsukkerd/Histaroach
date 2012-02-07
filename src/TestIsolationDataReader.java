import java.util.Set;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import common.HistoryGraph;
import common.HistoryGraphReader;
import common.Revision;

import static org.junit.Assert.assertNotNull;

public class TestIsolationDataReader {

	/**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * Full path to the serialized input file.
     */
    @Option(value = "-z File to use as serialized input.", aliases = { "-serializedInputFile" })
    public static String serializedInputFileName = null;
    
    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataReader [options]";
    
	/**
	 * Initial program entrance -- read a serialized HistoryGraph 
	 * from an input file
	 * 
	 * @param args : command line arguments.
	 */
	public static void main(String[] args) {
		Options plumeOptions = new Options(TestIsolationDataReader.usage_string, TestIsolationDataReader.class);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }
        
        HistoryGraphReader hGraphReader = new HistoryGraphReader(serializedInputFileName);
        HistoryGraph reconstructedHGraph = hGraphReader.readHistoryGraph();
        
        verifyHistoryGraph(reconstructedHGraph);
	}

	public static void verifyHistoryGraph(HistoryGraph historyGraph) {
		for (Revision revision : historyGraph) {
			Set<Revision> parents = historyGraph.getParents(revision);
			for (Revision parent : parents) {
				assertNotNull("null parent at " + revision.getCommitID(), parent);
			}
		}
	}
}
