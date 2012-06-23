package histaroach;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.Flip;
import histaroach.model.GitRepository;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.Revision;
import histaroach.util.HistoryGraphXMLReader;
import histaroach.util.Util;
import histaroach.util.XMLReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import plume.Option;
import plume.OptionGroup;
import plume.Options;


/**
 * ExploreTestNondeterminism determines, in a given HistoryGraph, what tests 
 * that flip from pass->fail are nondeterministic.
 */
public class ExploreTestNondeterminism {
	
	/**
     * Print a help message.
     */
    @OptionGroup("General Options")
    @Option(value="-h Print a help message", aliases={"-help"})
    public static boolean help;
    
	/**
	 * Repository directory.
	 */
	@Option(value = "-r <filename> Repository directory")
	public static File repoDir = null;
	
	/**
     * Build command. Default is 'ant'.
     */
    @Option(value = "-b Build command (Optional)")
    public static String buildCommand = "ant";
	
	/**
     * HistoryGraph xml file.
     */
    @Option(value = "-H <filename> HistoryGraph xml file")
    public static File historyGraphXML = null;
    
    /** One line synopsis of usage */
	public static final String usage_string = "ExploreTestNondeterminism [options]";
	
	private static final int REPEAT = 10;
	
	private final IRepository repository;
	private final IBuildStrategy buildStrategy;
	private final Set<Flip> flips;
	// keep track of what tests in what Flips are nondeterministic
	private final Map<Flip, Set<String>> nondeterministicTests;

	public ExploreTestNondeterminism(IRepository repository, 
			HistoryGraph historyGraph) {
		this.repository = repository;
		buildStrategy = repository.getBuildStrategy();
		flips = historyGraph.getAllFlips();
		nondeterministicTests = new HashMap<Flip, Set<String>>();
	}
	
	/**
	 * Explores all tests that flip from pass->fail and determines 
	 * if they are nondeterministic.
	 * 
	 * @throws Exception
	 */
	public void explore() throws Exception {
		for (Flip flip : flips) {
			Revision parent = flip.getParentRevision();
			Revision child = flip.getChildRevision();
			Set<String> toFailTests = flip.getToFailTests();
			if (toFailTests.isEmpty()) {
				continue;
			}
			
			// initially, only nondeterministic tests in parent
			Set<String> nondeterministicTests = 
				getNondeterministicTests(parent, toFailTests, true);
			
			// do not re-explore nondeterministic tests in parent
			Set<String> remainingToFailTests = new HashSet<String>(toFailTests);
			remainingToFailTests.removeAll(nondeterministicTests);
			
			// add nondeterministic tests in child
			nondeterministicTests.addAll(
					getNondeterministicTests(child, remainingToFailTests, false));
			
			if (!nondeterministicTests.isEmpty()) {
				this.nondeterministicTests.put(flip, nondeterministicTests);
			}
		}
	}
	
	/**
	 * From a given set of tests in a given revision, determines 
	 * which tests are nondeterministic.
	 * 
	 * @return a set of nondeterministic tests.
	 * @throws Exception
	 */
	private Set<String> getNondeterministicTests(Revision revision, Set<String> tests, 
			boolean expectedResult) throws Exception {
		Set<String> nondeterministicTests = new HashSet<String>();
		
		boolean checkoutSuccessful = repository.checkoutCommit(revision.getCommitID());
		if (!checkoutSuccessful) {
			throw new Exception("check out commit " + revision.getCommitID()
					+ " unsuccessful");
		}
		
		for (String test : tests) {
			if (isNondeterministic(test, expectedResult)) {
				nondeterministicTests.add(test);
			}
		}
		
		return nondeterministicTests;
	}
	
	/**
	 * Determines if a test is nondeterministic.
	 * 
	 * @return true if a test is nondeterministic.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean isNondeterministic(String test, boolean expectedResult) 
			throws IOException, InterruptedException {
		for (int i = 0; i < REPEAT; i++) {
			// clean up processes from previous run
			Util.killOtherJavaProcesses();
			boolean result = buildStrategy.runSingleTest(test);
			if (result != expectedResult) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Prints the result to standard out.
	 */
	public void printResult() {
		for (Flip flip : flips) {
			Revision parent = flip.getParentRevision();
			Revision child = flip.getChildRevision();
			
			if (nondeterministicTests.containsKey(flip)) {
				System.out.println("Flip " +
						parent.getCommitID() + "-" + child.getCommitID() +
						" contains at least the following nondeterministic test(s):");
				System.out.println(nondeterministicTests.get(flip));
			}
		}
	}
	
	/**
	 * Initial program entrance -- executes test nondeterminism explorer.
	 * 
	 * @param args - command line arguments.
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Options plumeOptions = new Options(usage_string, ExploreTestNondeterminism.class);
	    plumeOptions.parse_or_usage(args);
	    
	    // Display the help screen.
	    if (help) {
	        plumeOptions.print_usage();
	        return;
	    }
	    
	    if (repoDir == null || historyGraphXML == null) {
            plumeOptions.print_usage();
            return;
        }
		
	    IBuildStrategy buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
		IRepository repository = new GitRepository(repoDir, buildStrategy);
		XMLReader<HistoryGraph> reader = new HistoryGraphXMLReader(historyGraphXML);
	    HistoryGraph historyGraph = reader.read();
	    
		ExploreTestNondeterminism explorer = new ExploreTestNondeterminism(
				repository, historyGraph);
		explorer.explore();
		explorer.printResult();
	}

}
