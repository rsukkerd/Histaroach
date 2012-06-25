package histaroach;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.GitRepository;
import histaroach.model.IRepository;
import histaroach.util.Util;

import java.io.File;

import plume.Option;
import plume.OptionGroup;
import plume.Options;


/**
 * ExploreTestNondeterminism determines whether a test of some Revision 
 * is deterministic.
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
	 * Version (ie. commit ID).
	 */
	@Option(value = "-v Version (ie. commit ID)")
	public static String version = null;
	
	/**
     * Build command. Default is 'ant'.
     */
    @Option(value = "-b Build command (Optional)")
    public static String buildCommand = "ant";
	
	/**
     * Test name.
     */
    @Option(value = "-t Test name")
    public static String testName = null;
    
    /** One line synopsis of usage */
	public static final String usage_string = "ExploreTestNondeterminism [options]";
	
	private static final int REPEAT = 10;
	
	private final IRepository repository;
	private final IBuildStrategy buildStrategy;

	public ExploreTestNondeterminism(IRepository repository) {
		this.repository = repository;
		buildStrategy = repository.getBuildStrategy();
	}
	
	/**
	 * Determines if a version of a test is deterministic. 
	 * Prints the result to standard out.
	 * 
	 * @throws Exception
	 */
	public void explore(String test, String version) throws Exception {
		boolean checkoutSuccessful = repository.checkoutCommit(version);
		if (!checkoutSuccessful) {
			throw new Exception("check out commit " + version + " unsuccessful");
		}
		
		boolean initResult = buildStrategy.runSingleTest(test);
			
		for (int i = 1; i < REPEAT; i++) {
			// clean up processes from previous run
			Util.killOtherJavaProcesses();
			boolean result = buildStrategy.runSingleTest(test);
			if (result != initResult) {
				System.out.println("Test " + test + " is nondeterministic.");
			}
		}
		
		System.out.println("Test " + test + " is deterministic.");
		System.out.println("Revision " + version + 
				(initResult ? " passes " : " fails ") + test);
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
	    
	    if (repoDir == null || version == null || testName == null) {
            plumeOptions.print_usage();
            return;
        }
		
	    IBuildStrategy buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
		IRepository repository = new GitRepository(repoDir, buildStrategy);
	    
		ExploreTestNondeterminism explorer = new ExploreTestNondeterminism(repository);
		explorer.explore(testName, version);
	}

}
