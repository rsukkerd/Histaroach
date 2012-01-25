import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.BugFix;
import common.HistoryGraph;
import common.ParallelFixesFinder;
import common.Repository;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

public class ParallelBugFixesFinder {
    /**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;

    /**
     * Full path to the repository directory.
     */
    @Option(value = "-r Full path to the repository directory.",
            aliases = { "-repodir" })
    public static String repositoryDirName = null;

    /** One line synopsis of usage */
    public static final String usage_string = "ParallelBugFixesFinder [options]";

    /**
     * @param args
     *            [0] : full path of the repository directory
     */
    public static void main(String[] args) {
        Options plumeOptions = new Options(ParallelBugFixesFinder.usage_string);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        Repository repo = new Repository(new File(repositoryDirName));

        HistoryGraph historyGraph = new HistoryGraph(repo);

        Map<String, Set<BugFix>> bugFixMap = ParallelFixesFinder
                .findParallelFixes(historyGraph);

        System.out.println("ALL BUG FIXES");
        printAllFixes(historyGraph);

        System.out.println("PARALLEL BUG FIXES");
        printParallelFixes(bugFixMap);
    }

    public static void printAllFixes(HistoryGraph historyGraph) {
        Iterator<String> itr = historyGraph.getBugIterator();
        while (itr.hasNext()) {
            String bug = itr.next();

            System.out.println("Test : " + bug);
            System.out.println("All commits that fix this bug:");

            List<BugFix> list = historyGraph.getBugFixList(bug);
            for (BugFix fix : list) {
                System.out.println(fix);
            }
        }
    }

    public static void printParallelFixes(Map<String, Set<BugFix>> bugFixMap) {
        for (String bug : bugFixMap.keySet()) {
            System.out.println("Test : " + bug);
            System.out.println("Commits that fix this bug in parallel:");

            Set<BugFix> fixes = bugFixMap.get(bug);
            for (BugFix fix : fixes) {
                System.out.println(fix);
            }
        }
    }
}
