import java.util.List;

import common.Flip;
import common.HistoryGraph;
import common.Repository;
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
    
    /**
     * The ant command used for running ant. By default this is just 'ant'.
     */
    @Option(value = "-a ant command (Optional)", aliases = { "-antCommand" })
    public static String antCommand = "ant";
    
    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataReader [options]";
    
	/**
	 * Initial program entrance -- reconstruct a HistoryGraph from 
	 * serialized revision files from an input directory.
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
        
        Repository repository = new Repository(serializedRevisionsDirName, antCommand);
        HistoryGraph hGraph = Util.reconstructHistoryGraph(repository);
        
        List<Flip> flips = hGraph.getAllFlips();
        for (Flip flip : flips) {
        	System.out.println(flip);
        }
	}
}
