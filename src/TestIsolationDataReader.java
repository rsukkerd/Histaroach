import git.GitRepository;

import java.io.File;

import ant.JodatimeBuildStrateygy;
import ant.VoldemortBuildStrategy;

import plume.Option;
import plume.OptionGroup;
import plume.Options;

import common.BuildStrategy;
import common.HistoryGraph;
import common.MixingTool;
import common.Repository;
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
	 * Project name.
	 */
	@Option(value = "-p Project name (Required)",
	        aliases = { "-projName" })
	public static String projName = null;

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
	        aliases = { "-repoPath" })
	public static String repoPath = null;
	
	/**
	 * Full path to the cloned repository directory.
	 */
	@Option(value = "-c Full path to the cloned repository directory (Required)",
	        aliases = { "-clonedRepoPath" })
	public static String clonedRepoPath = null;

	/**
	 * Build command. Default is 'ant'.
	 */
	@Option(value = "-b build command (Optional)", aliases = { "-buildCommand" })
	public static String buildCommand = "ant";

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
	    
	    if (projName == null || serializedRevisionsDirName == null 
	    		|| repoPath == null || clonedRepoPath == null) {
	    	plumeOptions.print_usage();
	    	return;
	    }
	    
	    File repoDir = new File(repoPath);
	    File clonedRepoDir = new File(clonedRepoPath);
                
        BuildStrategy buildStrategy = null;
        BuildStrategy clonedBuildStrategy = null;
        
        if (projName.equals(TestIsolationDataGenerator.VOLDEMORT)) {
        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
        	clonedBuildStrategy = new VoldemortBuildStrategy(clonedRepoDir, buildCommand);
        } else if (projName.equals(TestIsolationDataGenerator.JODA_TIME)) {
        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
        	clonedBuildStrategy = new JodatimeBuildStrateygy(clonedRepoDir, buildCommand);
        }
        
        assert buildStrategy != null && clonedBuildStrategy != null;
        
        Repository repository = new GitRepository(repoDir, buildStrategy);
        Repository clonedRepository = new GitRepository(clonedRepoDir, clonedBuildStrategy);
	    
	    File serializedRevisionsDir = new File(serializedRevisionsDirName);
	    
	    HistoryGraph historyGraph = Util.reconstructHistoryGraph(serializedRevisionsDir);
	    	    
	    MixingTool mixing = new MixingTool(historyGraph, repository, clonedRepository);
	    mixing.run();
	}
}
