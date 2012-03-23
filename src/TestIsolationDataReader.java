import java.io.File;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import voldemort.VoldemortTestParsingStrategy;

import common.HistoryGraph;
import common.MixingTool;
import common.Repository;
import common.TestParsingStrategy;
import common.Util;

public class TestIsolationDataReader {

	/**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", 
    		aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * Full path to the directory containing serialized revision files.
     */
    @Option(value = "-z Directory containing serialized revision files (Required)", 
    		aliases = { "-serializedRevisionsDir" })
    public static String serializedRevisionsDirName = null;
    
    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory (Required)",
            aliases = { "-repoDir" })
    public static String repoDirName = null;
    
    /**
     * Full path to the cloned repository directory.
     */
    @Option(value = "-c Full path to the cloned repository directory (Required)",
            aliases = { "-clonedRepoDir" })
    public static String clonedRepoDirName = null;
    
    /**
     * The ant command used for running ant. By default this is just 'ant'.
     */
    @Option(value = "-a ant command (Optional)", 
    		aliases = { "-antCommand" })
    public static String antCommand = "ant";
    
    /**
     * Project name
     */
    @Option(value = "-p Project name (Required)",
    		aliases = { "-projectName" })
    public static String projectName = null;
    
    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataReader [options]";
    
	/**
	 * Initial program entrance -- reconstruct a HistoryGraph from 
	 * serialized revision files from an input directory.
	 * 
	 * @param args : command line arguments.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Options plumeOptions = new Options(TestIsolationDataReader.usage_string, TestIsolationDataReader.class);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }
        
        if (serializedRevisionsDirName == null || projectName == null 
        		|| repoDirName == null || clonedRepoDirName == null) {
        	plumeOptions.print_usage();
        	return;
        }
        
        TestParsingStrategy strategy = null;
        if (projectName.equals(TestIsolationDataGenerator.VOLDEMORT_PROJECT_NAME)) {
        	strategy = new VoldemortTestParsingStrategy();
        }
        
        assert strategy != null;
        
        Repository repository = new Repository(repoDirName, antCommand, strategy);
        File serializedRevisionsDir = new File(serializedRevisionsDirName);
        
        HistoryGraph historyGraph = Util.reconstructHistoryGraph(serializedRevisionsDir, repository);
        
        Repository clonedRepository = new Repository(clonedRepoDirName, antCommand, strategy);
        
        MixingTool mixing = new MixingTool(historyGraph, clonedRepository);
        // mixing.run();
        mixing.runOneFlipOneCombination();
	}
}
