package histaroach;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.JodatimeBuildStrateygy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.GitRepository;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.util.HistoryGraphXMLWriter;
import histaroach.util.XMLWriter;

import java.io.File;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import plume.Option;
import plume.OptionGroup;
import plume.Options;


/**
 * TestIsolationDataGenerator builds a HistoryGraph from voldemort repository
 * and writes the HistoryGraph output to an xml file.
 */
public class TestIsolationDataGenerator {
	
	public static final String OUTPUT_PATH = "output";
	
    // Prefix of files to which HistoryGraph instances are written.
    public static final String HGRAPH_FILE_PREFIX = "historyGraph";
    
    // Extension of xml files.
    public static final String XML_EXTENSION = ".xml";
    
    public static final String VOLDEMORT = "voldemort";
    public static final String JODA_TIME = "joda-time";
    
    /**
     * Print the short usage message.
     */
    @OptionGroup("General Options")
    @Option(value = "-h Print short usage message", aliases = { "-help" })
    public static boolean showHelp = false;
    
    /**
	 * Project name.
	 */
	@Option(value = "-p Project name (Required)",
	        aliases = { "-projName" })
	public static String projName = null;
    
    /**
	 * Full path to the repository directory.
	 */
	@Option(value = "-r Full path to the repository directory (Required)",
	        aliases = { "-repoPath" })
	public static String repoPath = null;
    
	/**
     * Build command. Default is 'ant'.
     */
    @Option(value = "-b build command (Optional)", 
    		aliases = { "-buildCommand" })
    public static String buildCommand = "ant";
    
    /**
     * The commit ID from which to begin the HistoryGraph analysis.
     */
    @Option(value = "-s Starting commit ID for HistoryGraph analysis (Required)",
            aliases = { "-startCommitID" })
    public static String startCommitID = null;
    
    /**
     * The commit ID where the HistoryGraph analysis should terminate.
     */
    @Option(value = "-e Ending commit ID for HistoryGraph analysis (Required)",
            aliases = { "-endCommitID" })
    public static String endCommitID = null;

    /** One line synopsis of usage */
    public static final String usage_string = "TestIsolationDataGenerator [options]";

    /**
     * Initial program entrance -- parses the arguments and runs the data
     * extraction.
     * 
     * @param args
     *            : command line arguments.
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        Options plumeOptions = new Options(
                TestIsolationDataGenerator.usage_string,
                TestIsolationDataGenerator.class);
        plumeOptions.parse_or_usage(args);

        // Display the help screen.
        if (showHelp) {
            plumeOptions.print_usage();
            return;
        }

        if (projName == null || repoPath == null 
        		|| startCommitID == null || endCommitID == null) {
            plumeOptions.print_usage();
            return;
        }
        
        File repoDir = new File(repoPath);
        
        IBuildStrategy buildStrategy = null;
        
        if (projName.equals(VOLDEMORT)) {
        	buildStrategy = new VoldemortBuildStrategy(repoDir, buildCommand);
        } else if (projName.equals(JODA_TIME)) {
        	buildStrategy = new JodatimeBuildStrateygy(repoDir, buildCommand);
        }
        
        assert buildStrategy != null;
        
        IRepository repository = new GitRepository(repoDir, buildStrategy);
        
        HistoryGraph historyGraph = repository.buildHistoryGraph(startCommitID, endCommitID);
        
        saveHistoryGraph(historyGraph);
    }

    /**
     * Write a HistoryGraph to an xml file.
     * 
     * @throws ParserConfigurationException 
     * @throws TransformerException 
     */
    public static void saveHistoryGraph(HistoryGraph historyGraph) throws ParserConfigurationException, 
    		TransformerException {
    	
    	String fileName = HGRAPH_FILE_PREFIX + "_" + startCommitID + "_" + endCommitID 
    			+ XML_EXTENSION;
    	File xmlFile = new File(OUTPUT_PATH + File.separatorChar + fileName);
    	
    	XMLWriter writer = new HistoryGraphXMLWriter(xmlFile, historyGraph);
    	writer.write();
    }
}
