package histaroach;

import histaroach.algorithm.MixedRevisionAnalysis;
import histaroach.algorithm.MixedRevisionGenerator;
import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.JodatimeBuildStrateygy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.Flip;
import histaroach.model.GitRepository;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.MixedRevision;
import histaroach.util.HistoryGraphXMLReader;
import histaroach.util.MixedRevisionXMLReader;
import histaroach.util.MixedRevisionXMLWriter;
import histaroach.util.XMLReader;
import histaroach.util.XMLWriter;

import java.io.File;
import java.util.List;
import java.util.Set;

import plume.Option;
import plume.OptionGroup;
import plume.Options;


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
     * MixedRevision output file.
     */
    @Option(value = "-m MixedRevision output file", 
    		aliases = { "-mixedRevisionOutput" })
    public static String mixedRevisionOutput = null;
    
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
	    
	    if (projName == null || repoPath == null || clonedRepoPath == null || 
	    		hGraphXML == null) {
	    	plumeOptions.print_usage();
	    	return;
	    }
	    
    	File repoDir = new File(repoPath);
	    File clonedRepoDir = new File(clonedRepoPath);
	    
	    IBuildStrategy buildStrategy = null;
        IBuildStrategy clonedBuildStrategy = null;
        
        if (projName.equals(TestIsolationDataGenerator.VOLDEMORT)) {
        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
        	clonedBuildStrategy = new VoldemortBuildStrategy(clonedRepoDir, buildCommand);
        } else if (projName.equals(TestIsolationDataGenerator.JODA_TIME)) {
        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
        	clonedBuildStrategy = new JodatimeBuildStrateygy(clonedRepoDir, buildCommand);
        }
        
        assert buildStrategy != null && clonedBuildStrategy != null;
        
        IRepository repository = new GitRepository(repoDir, buildStrategy);
        IRepository clonedRepository = new GitRepository(clonedRepoDir, clonedBuildStrategy);
        
        XMLReader<HistoryGraph> hGraphreader = new HistoryGraphXMLReader(new File(hGraphXML));
	    HistoryGraph historyGraph = hGraphreader.read();
	    
	    File xmlFile = new File("output/mixedRevisions.xml");
        
        if (numMixedRevisions > 0) {
        	XMLReader<List<MixedRevision>> mRevisionReader = new MixedRevisionXMLReader(
        			xmlFile, repository, clonedRepository, historyGraph);
        	List<MixedRevision> mixedRevisions = mRevisionReader.read();
        	
        	MixedRevisionAnalysis analysis = new MixedRevisionAnalysis(mixedRevisions);
        	analysis.runTestOnMixedRevisions(startIndex, numMixedRevisions, 
        			mixedRevisionOutput);
        } else {
        	Set<Flip> flips = historyGraph.getAllFlips();
        	
        	MixedRevisionGenerator generator = new MixedRevisionGenerator(repository, 
        			clonedRepository);
        	List<MixedRevision> mixedRevisions = generator.generateMixedRevisionsFromFlips(
        			flips);
        	
        	XMLWriter writer = new MixedRevisionXMLWriter(xmlFile, mixedRevisions);
        	writer.buildDocument();
        }
	}
}
