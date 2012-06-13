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
import histaroach.util.HistoryGraphXMLWriter;
import histaroach.util.MixedRevisionXMLReader;
import histaroach.util.MixedRevisionXMLWriter;
import histaroach.util.Util;
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

/**
 * DataCollector extracts information (HistoryGraph and MixedRevision) 
 * from the project subjects of study.
 */
public class DataCollector {
	
	public static final String DATA_PATH = "data";
	
	// Prefix of files to which HistoryGraph instances are written.
    public static final String HISTORYGRAPH_PREFIX = "historyGraph";
    
    // Prefix of files to which MixedRevision instances are written.
    public static final String MIXEDREVISION_PREFIX = "mixedRevision";
    
    public static final String XML_EXTENSION = ".xml";
    public static final String TXT_EXTENSION = ".txt";
    
    // project subjects of study
    public static final String VOLDEMORT = "voldemort";
    public static final String JODA_TIME = "joda-time";
    
    /**
     * Print a help message.
     */
    @OptionGroup("General Options")
    @Option(value="-h Print a help message", aliases={"-help"})
    public static boolean help;
    
    /**
     * Collect HistoryGraph data.
     */
    @OptionGroup("Mode Options")
    @Option(value="Collect HistoryGraph data")
    public static boolean historyGraphMode;
    
    /**
     * Create MixedRevision templates.
     */
    @Option(value="Create MixedRevision templates")
    public static boolean mixedRevisionTemplateMode;
    
    /**
     * Collect MixedRevision test results.
     */
    @Option(value="Collect MixedRevision test results")
    public static boolean mixedRevisionTestResultMode;
    
    /**
	 * Project name.
	 */
    @OptionGroup("Common Options")
	@Option(value = "-p Project name")
	public static String projectName = null;
	
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
     * The commit ID where HistoryGraph analysis begins.
     */
    @OptionGroup("HistoryGraph Options")
    @Option(value = "-s Starting commit ID for HistoryGraph analysis")
    public static String startCommitID = null;
    
    /**
     * The commit ID where HistoryGraph analysis ends.
     */
    @Option(value = "-e Ending commit ID for HistoryGraph analysis")
    public static String endCommitID = null;
    
    /**
	 * Cloned repository directory.
	 */
    @OptionGroup("MixedRevision Options")
	@Option(value = "-c <filename> Cloned repository directory")
	public static File clonedRepoDir = null;
    
    /**
     * HistoryGraph xml file.
     */
    @Option(value = "-H <filename> HistoryGraph xml file")
    public static File historyGraphXML = null;
    
    /**
     * MixedRevision xml file.
     */
    @OptionGroup("MixedRevision (test results) Options")
    @Option(value = "-M <filename> MixedRevision xml file")
    public static File mixedRevisionXML = null;
    
    /**
     * The index of MixedRevision to begin analysis.
     */
    @Option(value = "-i Index of MixedRevision to begin analysis (Optional)")
    public static int startIndex = 0;
    
    /**
     * The number of MixedRevisions to analyze.
     */
    @Option(value = "-n Number of MixedRevisions to analyze (Optional)")
    public static int numMixedRevisions = 0;

	/** One line synopsis of usage */
	public static final String usage_string = "DataCollector [mode option] [common options]"
		+ " [HistoryGraph/MixedRevision options]";
    

	/**
     * Initial program entrance -- parses the arguments and runs the specific 
     * type of data extraction.
     * 
     * @param args - command line arguments.
     * @throws Exception 
     */
	public static void main(String[] args) throws Exception {
		Options plumeOptions = new Options(usage_string, DataCollector.class);
	    plumeOptions.parse_or_usage(args);
	    
	    // Display the help screen.
	    if (help) {
	        plumeOptions.print_usage();
	        return;
	    }
	    
	    if (projectName == null || repoDir == null) {
            plumeOptions.print_usage();
            return;
        }
	    
	    if (historyGraphMode) {
	    	if (startCommitID == null || endCommitID == null) {
	            plumeOptions.print_usage();
	            return;
	        }
	        	        
	        IBuildStrategy buildStrategy;
	        
	        if (projectName.equals(VOLDEMORT)) {
	        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
	        } else if (projectName.equals(JODA_TIME)) {
	        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
	        } else {
	        	plumeOptions.print_usage("projectName must be either " 
	        			+ VOLDEMORT + " or " + JODA_TIME);
	            return;
	        }
	        
	        IRepository repository = new GitRepository(repoDir, buildStrategy);
	        
	        String timeStamp = Util.getCurrentTimeStamp();
	        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
	        
	        saveHistoryGraph(historyGraph, timeStamp);
	    	
	    } else if (mixedRevisionTemplateMode || mixedRevisionTestResultMode) {
	    	if (clonedRepoDir == null || historyGraphXML == null) {
		    	plumeOptions.print_usage();
		    	return;
		    }
	    	
	    	IBuildStrategy buildStrategy;
	        IBuildStrategy clonedBuildStrategy;
	        
	        if (projectName.equals(VOLDEMORT)) {
	        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
	        	clonedBuildStrategy = new VoldemortBuildStrategy(clonedRepoDir, buildCommand);
	        } else if (projectName.equals(JODA_TIME)) {
	        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
	        	clonedBuildStrategy = new JodatimeBuildStrateygy(clonedRepoDir, buildCommand);
	        } else {
	        	plumeOptions.print_usage("projectName must be either " 
	        			+ VOLDEMORT + " or " + JODA_TIME);
	            return;
	        }
	        
	        IRepository repository = new GitRepository(repoDir, buildStrategy);
	        IRepository clonedRepository = new GitRepository(clonedRepoDir, clonedBuildStrategy);
	        
	        XMLReader<HistoryGraph> reader = new HistoryGraphXMLReader(historyGraphXML);
		    HistoryGraph historyGraph = reader.read();
	    	
		    if (mixedRevisionTemplateMode) {
		    	generateMixedRevisions(historyGraph, repository, clonedRepository);
	        } else {
	        	if (mixedRevisionXML == null) {
			    	plumeOptions.print_usage();
			    	return;
			    }
	        	
	        	runTestOnMixedRevisions(historyGraph, repository, clonedRepository);
	        }
	    } else {
	    	plumeOptions.print_usage();
	    }
	}

	/**
     * Writes historyGraph to an xml file.
     * 
     * @throws ParserConfigurationException 
     * @throws TransformerException 
     */
    public static void saveHistoryGraph(HistoryGraph historyGraph, String timeStamp) 
    		throws ParserConfigurationException, TransformerException {
    	String fileName = HISTORYGRAPH_PREFIX + "_" + startCommitID + "_" + endCommitID 
    			+ "_" + timeStamp + XML_EXTENSION;
    	File xmlFile = new File(DATA_PATH + File.separatorChar + fileName);
    	
    	XMLWriter writer = new HistoryGraphXMLWriter(xmlFile, historyGraph);
    	writer.buildDocument();
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
    	
    	String filename = historyGraphXML.getName().replaceFirst(
    			HISTORYGRAPH_PREFIX, MIXEDREVISION_PREFIX);
    	File xmlFile = new File(DATA_PATH + File.separatorChar + filename);
    	
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
    			mixedRevisionXML, repository, clonedRepository, historyGraph);
    	List<MixedRevision> mixedRevisions = reader.read();
    	MixedRevisionAnalysis analysis = new MixedRevisionAnalysis(mixedRevisions);
    	
    	String xmlFilename = mixedRevisionXML.getName();
    	String filename;
    	
    	if (numMixedRevisions > 0) {
    		filename = xmlFilename.substring(0, xmlFilename.indexOf(XML_EXTENSION)) 
    				+ "_" + startIndex + "_" + (startIndex + numMixedRevisions) 
    				+ TXT_EXTENSION;
    	} else {
    		filename = xmlFilename.substring(0, xmlFilename.indexOf(XML_EXTENSION)) 
    				+ TXT_EXTENSION;
    	}
    	
    	File txtFile = new File(DATA_PATH + File.separatorChar + filename);
    	
    	if (numMixedRevisions > 0) {
	    	analysis.runTestOnMixedRevisions(startIndex, numMixedRevisions, 
	    			txtFile);
    	} else {
    		analysis.runTestOnMixedRevisions(txtFile);
    	}
	}
}
