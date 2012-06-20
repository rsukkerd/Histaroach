package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.IntermediateRevision;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;


/**
 * IntermediateRevisionXMLWriter writes a list of IntermediateRevisions to an XML file.
 */
public class IntermediateRevisionXMLWriter extends XMLWriter {
	
	public static final String INTERMEDIATE_REVISIONS = "IntermediateRevisions";
	public static final String INTERMEDIATE_REVISION = "IntermediateRevision";
	
	private final List<IntermediateRevision> mixedRevisions;

	public IntermediateRevisionXMLWriter(File xmlFile, List<IntermediateRevision> mixedRevisions)
			throws ParserConfigurationException {
		super(xmlFile);
		this.mixedRevisions = mixedRevisions;
	}

	@Override
	public void buildDocument() throws TransformerException {
		Element rootElement = doc.createElement(INTERMEDIATE_REVISIONS);
		
		for (IntermediateRevision mixedRevision : mixedRevisions) {
			Element mixedRevisionElement = createIntermediateRevisionElement(mixedRevision);
			rootElement.appendChild(mixedRevisionElement);
		}
		
		doc.appendChild(rootElement);
		
		write();
	}

	public Element createIntermediateRevisionElement(IntermediateRevision mixedRevision) {
		Element mixedRevisionElement = doc.createElement(INTERMEDIATE_REVISION);
				
		String baseCommitID = mixedRevision.getBaseRevision().getCommitID();
		String successorCommitID = mixedRevision.getSuccessorRevision().getCommitID();
		Set<DiffFile> delta = mixedRevision.getDelta();
		
		Element baseCommitIDElement = createCommitIDElement(baseCommitID);
		Element successorCommitIDElement = createCommitIDElement(successorCommitID);
		Element deltaElement = createDiffFilesElement(delta);
		
		mixedRevisionElement.appendChild(baseCommitIDElement);
		mixedRevisionElement.appendChild(successorCommitIDElement);
		mixedRevisionElement.appendChild(deltaElement);
				
		return mixedRevisionElement;
	}
}
