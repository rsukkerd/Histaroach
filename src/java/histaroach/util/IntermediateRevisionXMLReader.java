package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.IntermediateRevision;
import histaroach.model.Revision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;


/**
 * IntermediateRevisionXMLReader reads an XML file representing a list of 
 * IntermediateRevisions and reconstructs the IntermediateRevision instances.
 */
public class IntermediateRevisionXMLReader extends XMLReader<List<IntermediateRevision>> {

	private final IRepository repository;
	private final IRepository clonedRepository;
	private final HistoryGraph historyGraph;
	
	public IntermediateRevisionXMLReader(File xmlFile, IRepository repository, 
			IRepository clonedRepository, HistoryGraph historyGraph)
			throws ParserConfigurationException, SAXException, IOException {
		super(xmlFile);
		this.repository = repository;
		this.clonedRepository = clonedRepository;
		this.historyGraph = historyGraph;
	}

	/**
	 * Reconstructs the list of IntermediateRevision instances from the XML file.
	 * 
	 * @return the list of IntermediateRevisions.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	@Override
	public List<IntermediateRevision> read() throws IOException, InterruptedException {
		List<IntermediateRevision> intermediateRevisions = new ArrayList<IntermediateRevision>();
		
		List<Element> intermediateRevisionElements = traverseContainedElements(rootElement);
		
		for (Element intermediateRevisionElement : intermediateRevisionElements) {
			IntermediateRevision intermediateRevision = parseIntermediateRevisionElement(
					intermediateRevisionElement);
			intermediateRevisions.add(intermediateRevision);
		}
		
		return intermediateRevisions;
	}
	
	public IntermediateRevision parseIntermediateRevisionElement(Element intermediateRevisionElement) 
			throws IOException, InterruptedException { // <IntermediateRevision>
		Iterator<Element> iter = traverseContainedElements(intermediateRevisionElement).iterator();
		
		Element baseCommitIDElement = iter.next();
		Element successorCommitIDElement = iter.next();
		Element deltaElement = iter.next();
				
		String baseCommitID = getString(baseCommitIDElement);
		String successorCommitID = getString(successorCommitIDElement);
		Revision base = historyGraph.lookUpRevision(baseCommitID);
		Revision successor = historyGraph.lookUpRevision(successorCommitID);
		
		Set<DiffFile> delta = parseDiffFilesElement(deltaElement);
		
		IntermediateRevision intermediateRevision = new IntermediateRevision(base, successor, 
				repository, clonedRepository);
		intermediateRevision.setDelta(delta);
		
		return intermediateRevision;
	}
}
