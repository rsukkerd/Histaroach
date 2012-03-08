import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import voldemort.VoldemortTestParsingStrategy;

import common.BugFix;
import common.HistoryGraph;
import common.ParallelBugFixes;
import common.Repository;
import common.TestParsingStrategy;

public class ParallelBugFixesFinder {
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
    
    /**
     * Project name
     */
    @Option(value = "-p Project name (Required)",
    		aliases = { "-projectName" })
    public static String projectName = null;

    /** One line synopsis of usage */
    public static final String usage_string = "ParallelBugFixesFinder [options]";

    public static void main(String[] args) throws IOException {
        Options plumeOptions = new Options(ParallelBugFixesFinder.usage_string);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        if (startCommitID == null || endCommitID == null || repositoryDirName == null 
        		|| projectName == null) {
            plumeOptions.print_usage();
            return;
        }
        
        Set<ParallelBugFixes> allParallelBugFixes = findAllParallelBugFixes();
    }
    
    /**
     * @return a set of all parallel bug fixes
     * @throws IOException
     */
    public static Set<ParallelBugFixes> findAllParallelBugFixes() throws IOException {
    	TestParsingStrategy strategy = null;
        if (projectName.equals(TestIsolationDataGenerator.VOLDEMORT_PROJECT_NAME)) {
        	strategy = new VoldemortTestParsingStrategy();
        }
        
        assert strategy != null;
        
    	Set<ParallelBugFixes> allParallelFixing = new HashSet<ParallelBugFixes>();
        
        Repository repository = new Repository(repositoryDirName, antCommand, strategy);
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        Map<String, List<BugFix>> allBugFixes = historyGraph.getAllBugFixes();
        
        for (String bug : allBugFixes.keySet()) {
        	List<BugFix> allFixes = allBugFixes.get(bug);
        	ParallelBugFixes parallelFixing = historyGraph.getParallelBugFixes(bug, allFixes);
        	
        	allParallelFixing.add(parallelFixing);
        }
        
        return allParallelFixing;
    }
}
