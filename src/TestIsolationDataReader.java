import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;

import common.Flip;
import common.HistoryGraph;
import common.Revision;
import common.Util;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

public class TestIsolationDataReader {

	/**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * Full path to the directory containing serialized revision files
     */
    @Option(value = "-z Directory containing serialized revision files.", aliases = { "-serializedRevisionsDir" })
    public static String serializedRevisionsDirName = null;
    
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
        
        HistoryGraph hGraph = Util.reconstructHistoryGraph(serializedRevisionsDirName);
        // verifyHistoryGraph(hGraph);
        
        Set<Flip> flips = hGraph.getAllFlips();
        for (Flip flip : flips) {
        	System.out.println(flip);
        }
	}

	public static void verifyHistoryGraph(HistoryGraph historyGraph) {
		for (Revision revision : historyGraph) {
			List<Revision> parents = revision.getParents();
			for (Revision parent : parents) {
				assertNotNull("null parent at " + revision.getCommitID(), parent);
			}
		}
	}
}
