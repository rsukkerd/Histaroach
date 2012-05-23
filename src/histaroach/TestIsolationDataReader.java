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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import plume.Option;
import plume.OptionGroup;
import plume.Options;


public class TestIsolationDataReader {

	public static final String MREVISION_FILE_PREFIX = "mixedRevision";
	public static final String TXT_EXTENSION = ".txt";
	
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
	 * Repository directory.
	 */
	@Option(value = "-r Repository directory",
	        aliases = { "-repoDir" })
	public static File repoDir = null;
	
	/**
	 * Cloned repository directory.
	 */
	@Option(value = "-c Cloned repository directory",
	        aliases = { "-clonedRepoDir" })
	public static File clonedRepoDir = null;
	
	/**
     * HistoryGraph xml file.
     */
    @Option(value = "-H HistoryGraph xml file", 
    		aliases = { "-hGraphXML" })
    public static File hGraphXML = null;
    
    /**
     * MixedRevision xml file.
     */
    @Option(value = "-M MixedRevision xml file", 
    		aliases = { "-mRevisionXML" })
    public static File mRevisionXML = null;
    
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
	    
	    if (projName == null || repoDir == null || clonedRepoDir == null || 
	    		hGraphXML == null) {
	    	plumeOptions.print_usage();
	    	return;
	    }
	    	    
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
        
        XMLReader<HistoryGraph> reader = new HistoryGraphXMLReader(hGraphXML);
	    HistoryGraph historyGraph = reader.read();

	    if (mRevisionXML != null) {
        	runTestOnMixedRevisions(historyGraph, repository, clonedRepository);
        } else {
        	generateMixedRevisions(historyGraph, repository, clonedRepository);
        }
	}
	
	/**
	 * Generates a list of MixedRevisions from all flips in historyGraph, 
	 * and writes them to an xml file.
	 * 
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void generateMixedRevisions(HistoryGraph historyGraph, 
			IRepository repository, IRepository clonedRepository) 
			throws ParserConfigurationException, TransformerException {
		Set<Flip> flips = historyGraph.getAllFlips();
    	
    	MixedRevisionGenerator generator = new MixedRevisionGenerator(repository, 
    			clonedRepository);
    	List<MixedRevision> mixedRevisions = generator.generateMixedRevisionsFromFlips(
    			flips);
    	
    	String filename = hGraphXML.getName().replaceFirst(
    			TestIsolationDataGenerator.HGRAPH_FILE_PREFIX, MREVISION_FILE_PREFIX);
    	File xmlFile = new File(TestIsolationDataGenerator.OUTPUT_PATH + 
    			File.separatorChar + filename);
    	
    	XMLWriter writer = new MixedRevisionXMLWriter(xmlFile, mixedRevisions);
    	writer.buildDocument();
	}
	
	/**
	 * For a specified range in mixedRevisions, creates actual mixed revisions 
	 * on the file system, runs tests on them and records the results to 
	 * an output file.
	 * 
	 * @throws Exception
	 */
	public static void runTestOnMixedRevisions(HistoryGraph historyGraph, 
			IRepository repository, IRepository clonedRepository) 
			throws Exception {
		XMLReader<List<MixedRevision>> reader = new MixedRevisionXMLReader(
    			mRevisionXML, repository, clonedRepository, historyGraph);
    	List<MixedRevision> mixedRevisions = reader.read();
    	MixedRevisionAnalysis analysis = new MixedRevisionAnalysis(mixedRevisions);
    	
    	String xmlFilename = mRevisionXML.getName();
    	String filename;
    	
    	if (numMixedRevisions > 0) {
    		filename = xmlFilename.substring(0, xmlFilename.indexOf(
        			TestIsolationDataGenerator.XML_EXTENSION)) + 
        			"_" + startIndex + "_" + (startIndex + numMixedRevisions) + 
        			TXT_EXTENSION;
    	} else {
    		filename = xmlFilename.substring(0, xmlFilename.indexOf(
        			TestIsolationDataGenerator.XML_EXTENSION)) + 
        			TXT_EXTENSION;
    	}
    	
    	File txtFile = new File(TestIsolationDataGenerator.OUTPUT_PATH + 
    			File.separatorChar + filename);
    	
    	if (numMixedRevisions > 0) {
	    	analysis.runTestOnMixedRevisions(startIndex, numMixedRevisions, 
	    			txtFile);
    	} else {
    		analysis.runTestOnMixedRevisions(txtFile);
    	}
	}
}
