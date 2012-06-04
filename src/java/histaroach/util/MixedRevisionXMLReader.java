package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.MixedRevision;
import histaroach.model.Revision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * MixedRevisionXMLReader reads an XML file representing a list of MixedRevisions 
 * and reconstructs the list of MixedRevision instances.
 */
public class MixedRevisionXMLReader extends XMLReader<List<MixedRevision>> {

	private final IRepository repository;
	private final IRepository clonedRepository;
	private final HistoryGraph historyGraph;
	
	public MixedRevisionXMLReader(File xmlFile, IRepository repository, 
			IRepository clonedRepository, HistoryGraph historyGraph)
			throws ParserConfigurationException, SAXException, IOException {
		super(xmlFile);
		this.repository = repository;
		this.clonedRepository = clonedRepository;
		this.historyGraph = historyGraph;
	}

	/**
	 * Reconstructs the list of MixedRevision instances from the XML file.
	 * 
	 * @return the list of MixedRevisions.
	 */
	@Override
	public List<MixedRevision> read() {
		List<MixedRevision> mixedRevisions = new ArrayList<MixedRevision>();
		
		List<Element> mixedRevisionElements = traverseContainedElements(rootElement);
		
		for (Element mixedRevisionElement : mixedRevisionElements) {
			MixedRevision mixedRevision = parseMixedRevisionElement(mixedRevisionElement);
			mixedRevisions.add(mixedRevision);
		}
		
		return mixedRevisions;
	}
	
	public MixedRevision parseMixedRevisionElement(Element mixedRevisionElement) {
		Iterator<Element> iter = traverseContainedElements(mixedRevisionElement).iterator();
		
		Element baseCommitIDElement = iter.next();
		Element revertedFileRecordsElement = iter.next();
				
		String baseCommitID = getString(baseCommitIDElement);
		Revision baseRevision = historyGraph.lookUpRevision(baseCommitID);
		
		Map<Revision, Set<DiffFile>> revertedFileRecords = parseDiffRecordsElement(
				revertedFileRecordsElement);
		
		MixedRevision mixedRevision = new MixedRevision(baseRevision, repository, 
				clonedRepository);
		
		for (Map.Entry<Revision, Set<DiffFile>> record : revertedFileRecords.entrySet()) {
			Revision otherRevision = record.getKey();
			Set<DiffFile> diffFiles = record.getValue();
			
			mixedRevision.setRevertedFiles(diffFiles, otherRevision);
		}
		
		return mixedRevision;
	}

	@Override
	public Pair<Revision, Set<DiffFile>> parseDiffRecordElement(
			Element diffRecordElement) { // <RevertedFileRecord>
		Iterator<Element> iter = traverseContainedElements(diffRecordElement).iterator();
		
		Element commitIDElement = iter.next();	// <commitID>
		Element diffFilesElement = iter.next();	// <DiffFiles>
		
		String otherCommitID = getString(commitIDElement);
		Set<DiffFile> diffFiles = parseDiffFilesElement(diffFilesElement);
		
		Revision otherRevision = historyGraph.lookUpRevision(otherCommitID);
		assert otherRevision != null;
		
		Pair<Revision, Set<DiffFile>> pair = new Pair<Revision, Set<DiffFile>>(
				otherRevision, diffFiles);
		
		return pair;
	}
}
