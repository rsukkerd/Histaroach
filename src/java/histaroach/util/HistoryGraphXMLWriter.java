package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.HistoryGraph;
import histaroach.model.Revision;
import histaroach.model.TestResult;
import histaroach.model.Revision.Compilable;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;


/**
 * HistoryGraphXMLWriter writes a HistoryGraph to an XML file.
 */
public class HistoryGraphXMLWriter extends XMLWriter {
	
	public static final String HISTORY_GRAPH = "HistoryGraph";
	public static final String REVISION = "Revision";
	public static final String PARENTS = "Parents";
	public static final String PARENT = "Parent";
	
	private final HistoryGraph historyGraph;

	public HistoryGraphXMLWriter(File xmlFile, HistoryGraph historyGraph)
			throws ParserConfigurationException {
		super(xmlFile);
		this.historyGraph = historyGraph;
	}

	@Override
	public void buildDocument() throws TransformerException {
		Element rootElement = doc.createElement(HISTORY_GRAPH);
		
		for (Revision revision : historyGraph) {
			Element revisionElement = createRevisionElement(revision);
			rootElement.appendChild(revisionElement);
		}
		
		doc.appendChild(rootElement);
		
		write();
	}
	
	public Element createRevisionElement(Revision revision) {
		String commitID = revision.getCommitID();
		Compilable compilable = revision.isCompilable();
		boolean testAborted = revision.hasTestAborted();
		TestResult testResult = revision.getTestResult();
		
		Element revisionElement = doc.createElement(REVISION);
		
		Element commitIDElement = createCommitIDElement(commitID);
		Element compilableElement = createCompilableElement(compilable);
		Element testAbortedElement = createTestAbortedElement(testAborted);
		Element testResultElement = createTestResultElement(testResult);
		Element parentsElement = createParentsElement(revision);
		
		revisionElement.appendChild(commitIDElement);
		revisionElement.appendChild(compilableElement);
		revisionElement.appendChild(testAbortedElement);
		revisionElement.appendChild(testResultElement);
		revisionElement.appendChild(parentsElement);
		
		return revisionElement;
	}
	
	public Element createParentsElement(Revision revision) {
		Element parentsElement = doc.createElement(PARENTS);
		
		Set<Revision> parents = revision.getParents();
		
		for (Revision parent : parents) {
			Set<DiffFile> diffFiles = revision.getDiffFiles(parent);
			
			Element parentElement = createDiffRecordElement(parent, diffFiles, 
					PARENT);
			parentsElement.appendChild(parentElement);
		}
		
		return parentsElement;
	}

}
