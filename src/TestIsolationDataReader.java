import git.GitRepository;

import java.io.File;
import java.util.List;

import plume.Option;
import plume.OptionGroup;
import plume.Options;
import util.HistoryGraphXMLReader;
import util.Util;
import ant.JodatimeBuildStrateygy;
import ant.VoldemortBuildStrategy;

import common.BuildStrategy;
import common.HistoryGraph;
import common.MixedRevisionTemplate;
import common.MixedRevisionTemplatesGenerator;
import common.MixedRevisionsGenerator;
import common.Repository;

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
	@Option(value = "-p Project name",
	        aliases = { "-projName" })
	public static String projName = null;

	/**
     * HistoryGraph xml file.
     */
    @Option(value = "-x HistoryGraph xml file", 
    		aliases = { "-hGraphXML" })
    public static String hGraphXML = null;
    
    /**
     * Start index of MixedRevision to begin analysis.
     */
    @Option(value = "-s Start index of MixedRevision to begin analysis", 
    		aliases = { "-startIndex" })
    public static int startIndex = 0;
    
    /**
     * Number of MixedRevisions to analyze.
     */
    @Option(value = "-n Number of MixedRevisions to analyze", 
    		aliases = { "-numMixedRevisions" })
    public static int numMixedRevisions = 0;
    
    /**
	 * Full path to the repository directory.
	 */
	@Option(value = "-r Full path to the repository directory",
	        aliases = { "-repoPath" })
	public static String repoPath = null;
	
	/**
	 * Full path to the cloned repository directory.
	 */
	@Option(value = "-c Full path to the cloned repository directory",
	        aliases = { "-clonedRepoPath" })
	public static String clonedRepoPath = null;

	/**
	 * Build command. Default is 'ant'.
	 */
	@Option(value = "-b build command (Optional)", 
			aliases = { "-buildCommand" })
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
	    
	    if (numMixedRevisions > 0) {
	    	
	    	if (projName == null || repoPath == null || clonedRepoPath == null) {
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
		    
	    	List<MixedRevisionTemplate> mixedRevisionTemplates = Util.readMixedRevisionTemplates();
	    	
	    	MixedRevisionsGenerator generator = 
	    		new MixedRevisionsGenerator(mixedRevisionTemplates, repository, clonedRepository);
	    	
	    	generator.constructSimpleMixedRevisions(startIndex, numMixedRevisions);
	    } else {
	    	
	    	if (hGraphXML == null) {
		    	plumeOptions.print_usage();
		    	return;
		    }
	    	
	    	HistoryGraphXMLReader reader = new HistoryGraphXMLReader(new File(hGraphXML));
		    HistoryGraph historyGraph = reader.reconstructHistoryGraph();
		    
		    MixedRevisionTemplatesGenerator templatesGenerator = 
		    	new MixedRevisionTemplatesGenerator(historyGraph);
		    
		    templatesGenerator.generateMixedRevisionTemplates();
		    templatesGenerator.writeOutMixedRevisionTemplates();
	    }
	}
}
