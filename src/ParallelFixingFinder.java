import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.BugFix;
import common.HistoryGraph;
import common.ParallelFixing;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import voldemort.ParallelRepository;

public class ParallelFixingFinder {
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
    public static final String usage_string = "ParallelFixingFinder [options]";

    public static void main(String[] args) throws IOException {
        Options plumeOptions = new Options(ParallelFixingFinder.usage_string);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        Set<ParallelFixing> allParallelFixing = findParallelFixing();
    }
    
    public static Set<ParallelFixing> findParallelFixing() throws IOException {
    	Set<ParallelFixing> allParallelFixing = new HashSet<ParallelFixing>();
        
        ParallelRepository repository = new ParallelRepository(repositoryDirName, humanReadOutputFileName);
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        Map<String, List<BugFix>> allBugFixes = repository.getAllBugFixes(historyGraph);
        
        for (String bug : allBugFixes.keySet()) {
        	List<BugFix> allFixes = allBugFixes.get(bug);
        	ParallelFixing parallelFixing = repository.getParallelFixing(historyGraph, bug, allFixes);
        	
        	allParallelFixing.add(parallelFixing);
        }
        
        return allParallelFixing;
    }
}
