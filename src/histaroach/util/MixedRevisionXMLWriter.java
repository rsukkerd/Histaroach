package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.MixedRevision;
import histaroach.model.Revision;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;


/**
 * MixedRevisionXMLWriter writes a list of MixedRevisions to an XML file.
 */
public class MixedRevisionXMLWriter extends XMLWriter {
	
	public static final String MIXED_REVISIONS = "MixedRevisions";
	public static final String MIXED_REVISION = "MixedRevision";
	public static final String REVERTED_FILE_RECORDS = "RevertedFileRecords";
	public static final String REVERTED_FILE_RECORD = "RevertedFileRecord";
	
	private final List<MixedRevision> mixedRevisions;

	public MixedRevisionXMLWriter(File xmlFile, List<MixedRevision> mixedRevisions)
			throws ParserConfigurationException {
		super(xmlFile);
		this.mixedRevisions = mixedRevisions;
	}

	@Override
	public void write() throws TransformerException {
		Element rootElement = doc.createElement(MIXED_REVISIONS);
		
		for (MixedRevision mixedRevision : mixedRevisions) {
			Element mixedRevisionElement = createMixedRevisionElement(mixedRevision);
			rootElement.appendChild(mixedRevisionElement);
		}
		
		doc.appendChild(rootElement);
		
		write(rootElement);
	}

	public Element createMixedRevisionElement(MixedRevision mixedRevision) {
		Element mixedRevisionElement = doc.createElement(MIXED_REVISION);
				
		String baseCommitID = mixedRevision.getBaseRevision().getCommitID();
		Map<Set<DiffFile>, Revision> revertedFileRecords = 
			mixedRevision.getRevertedFileRecords();
		
		Element baseCommitIDElement = createCommitIDElement(baseCommitID);
		Element revertedFileRecordsElement = createRevertedFileRecordsElement(
				revertedFileRecords);
		
		mixedRevisionElement.appendChild(baseCommitIDElement);
		mixedRevisionElement.appendChild(revertedFileRecordsElement);
		
		return mixedRevisionElement;
	}
	
	public Element createRevertedFileRecordsElement(
			Map<Set<DiffFile>, Revision> revertedFileRecords) {
		Element recordsElement = doc.createElement(REVERTED_FILE_RECORDS);
		
		for (Map.Entry<Set<DiffFile>, Revision> record : revertedFileRecords.entrySet()) {
			Set<DiffFile> revertedFile = record.getKey();
			Revision otherRevision = record.getValue();
			
			Element recordElement = createDiffRecordElement(otherRevision, 
					revertedFile, REVERTED_FILE_RECORD);
			recordsElement.appendChild(recordElement);
		}
		
		return recordsElement;
	}
}
