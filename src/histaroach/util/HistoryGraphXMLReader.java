package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.HistoryGraph;
import histaroach.model.Revision;
import histaroach.model.TestResult;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;


/**
 * HistoryGraphXMLReader reads an XML file representing a HistoryGraph 
 * and reconstructs the HistoryGraph instance.
 */
public class HistoryGraphXMLReader {
	
	private final File hGraphXML;
	private final Map<String, Revision> revisions;
	
	public HistoryGraphXMLReader(File hGraphXML) {
		this.hGraphXML = hGraphXML;
		revisions = new HashMap<String, Revision>();
	}
	
	/**
	 * Reconstructs the HistoryGraph instance from the XML file.
	 * 
	 * @return the HistoryGraph instance represented by the XML file.
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	public HistoryGraph reconstructHistoryGraph() throws ParserConfigurationException, 
			SAXException, IOException {
		HistoryGraph hGraph = new HistoryGraph();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(hGraphXML);
		doc.getDocumentElement().normalize();
		
		Element root = doc.getDocumentElement(); // <HistoryGraph>
		
		List<Element> revisionElements = traverseContainedElements(root);
		
		for (Element revisionElement : revisionElements) { // <Revision>
			Revision revision = parseRevisionElement(revisionElement);
			hGraph.addRevision(revision);
			
			revisions.put(revision.getCommitID(), revision);
		}
		
		return hGraph;
	}
	
	private Revision parseRevisionElement(Element revisionElement) {
		Iterator<Element> iter = traverseContainedElements(revisionElement).iterator();
		
		Element commitIDElement = iter.next();		// <commitID>
		Element compilableElement = iter.next();	// <Compilable>
		Element testResultElement = iter.next();	// <TestResult>
		Element parentsElement = iter.next();		// <Parents>
		
		String commitID = getString(commitIDElement);
		Compilable compilable = parseCompilableElement(compilableElement);
		TestResult testResult = null;
		
		if (compilable == Compilable.YES) {
			testResult = parseTestResultElement(testResultElement);
		}
		
		Map<Revision, List<DiffFile>> parentToDiffFiles = parseParentsElement(parentsElement);
		
		Revision revision = new Revision(commitID, parentToDiffFiles, compilable, testResult);
		
		return revision;
	}
	
	private Compilable parseCompilableElement(Element compilableElement) {
		String compilableStr = getString(compilableElement);
		
		if (compilableStr.equals(Compilable.YES.toString())) {
			return Compilable.YES;
		} else if (compilableStr.equals(Compilable.NO.toString())) {
			return Compilable.NO;
		} else if (compilableStr.equals(Compilable.UNKNOWN.toString())) {
			return Compilable.UNKNOWN;
		}
		
		return Compilable.NO_BUILD_FILE;
	}
	
	private TestResult parseTestResultElement(Element testResultElement) {
		Iterator<Element> iter = traverseContainedElements(testResultElement).iterator();
		
		Element allTestsElement = iter.next();		// <Tests>
		Element failedTestsElement = iter.next();	// <FailedTests>
		
		Set<String> allTests = parseTestsElement(allTestsElement);
		Set<String> failedTests = parseTestsElement(failedTestsElement);
		
		TestResult testResult = new TestResult(allTests, failedTests);
		
		return testResult;
	}
	
	private Set<String> parseTestsElement(Element testsElement) {
		Set<String> tests = new HashSet<String>();
		
		List<Element> testNameElements = traverseContainedElements(testsElement);
		
		for (Element testNameElement : testNameElements) { // <testName>
			String testName = getString(testNameElement);
			tests.add(testName);
		}
		
		return tests;
	}
	
	private Map<Revision, List<DiffFile>> parseParentsElement(Element parentsElement) {
		Map<Revision, List<DiffFile>> parentToDiffFiles = new HashMap<Revision, List<DiffFile>>();
		
		List<Element> parentElements = traverseContainedElements(parentsElement);
		
		for (Element parentElement : parentElements) { // <Parent>
			Pair<Revision, List<DiffFile>> pair = parseParentElement(parentElement);
			Revision parent = pair.getFirst();
			List<DiffFile> diffFiles = pair.getSecond();
			
			parentToDiffFiles.put(parent, diffFiles);
		}
		
		return parentToDiffFiles;
	}
	
	private Pair<Revision, List<DiffFile>> parseParentElement(Element parentElement) {
		Iterator<Element> iter = traverseContainedElements(parentElement).iterator();
		
		Element commitIDElement = iter.next();	// <commitID>
		Element diffFilesElement = iter.next();	// <DiffFiles>
		
		String parentCommitID = getString(commitIDElement);
		List<DiffFile> diffFiles = parseDiffFilesElement(diffFilesElement);
		
		// because of topological ordering of Revisions in HistoryGraph
		assert revisions.containsKey(parentCommitID);
		
		Revision parent = revisions.get(parentCommitID);
		
		Pair<Revision, List<DiffFile>> pair = new Pair<Revision, List<DiffFile>>(parent, diffFiles);
		
		return pair;
	}
	
	private List<DiffFile> parseDiffFilesElement(Element diffFilesElement) {
		List<DiffFile> diffFiles = new ArrayList<DiffFile>();
		
		List<Element> diffFileElements = traverseContainedElements(diffFilesElement);
		
		for (Element diffFileElement : diffFileElements) { // <DiffFile>
			DiffFile diffFile = parseDiffFileElement(diffFileElement);
			diffFiles.add(diffFile);
		}
		
		return diffFiles;
	}
	
	private DiffFile parseDiffFileElement(Element diffFileElement) {
		Iterator<Element> iter = traverseContainedElements(diffFileElement).iterator();
		
		Element fileNameElement = iter.next();	// <fileName>
		Element diffTypeElement = iter.next();	// <DiffType>
		
		String fileName = getString(fileNameElement);
		DiffType diffType = parseDiffTypeElement(diffTypeElement);
		
		DiffFile diffFile = new DiffFile(diffType, fileName);
		
		return diffFile;
	}
	
	private DiffType parseDiffTypeElement(Element diffTypeElement) {
		String diffTypeStr = getString(diffTypeElement);
		
		if (diffTypeStr.equals(DiffType.ADDED.toString())) {
			return DiffType.ADDED;
		} else if (diffTypeStr.equals(DiffType.MODIFIED.toString())) {
			return DiffType.MODIFIED;
		}
		
		return DiffType.DELETED;
	}
	
	/**
	 * @return a list of elements contained in the containerElement.
	 */
	private List<Element> traverseContainedElements(Element containerElement) {
		List<Element> containedElements = new ArrayList<Element>();
		
		NodeList nodes = containerElement.getChildNodes();
		
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element) node;
				containedElements.add(element);
			}
		}
		
		return containedElements;
	}
	
	private String getString(Element element) {
		Text text = (Text) element.getFirstChild();
		String str = text.getData().trim();
		
		return str;
	}
}
