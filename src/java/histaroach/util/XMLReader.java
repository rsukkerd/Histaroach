package histaroach.util;

import histaroach.model.DiffFile;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision;
import histaroach.model.Revision.Compilable;
import histaroach.model.TestResult;

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

public abstract class XMLReader<T> {
	
	protected final Element rootElement;

	protected XMLReader(File xmlFile) throws ParserConfigurationException, 
			SAXException, IOException {		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(xmlFile);
		doc.getDocumentElement().normalize();
		
		rootElement = doc.getDocumentElement();
	}
	
	/**
	 * Reconstructs an instance of type T from the XML file.
	 * 
	 * @return an instance of type T.
	 */
	public abstract T read();
	
	public Compilable parseCompilableElement(Element compilableElement) { // <Compilable>
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
	
	public boolean parseTestAbortedElement(Element testAbortedElement) {
		String testAbortedStr = getString(testAbortedElement);
		
		return testAbortedStr.equals(Boolean.TRUE.toString());
	}
	
	public TestResult parseTestResultElement(Element testResultElement) { // <TestResult>
		Iterator<Element> iter = traverseContainedElements(testResultElement).iterator();
		
		Element allTestsElement = iter.next();		// <Tests>
		Element failedTestsElement = iter.next();	// <FailedTests>
		
		Set<String> allTests = parseTestsElement(allTestsElement);
		Set<String> failedTests = parseTestsElement(failedTestsElement);
		
		TestResult testResult = new TestResult(allTests, failedTests);
		
		return testResult;
	}
	
	public Set<String> parseTestsElement(Element testsElement) { // <Tests> or <FailedTests>
		Set<String> tests = new HashSet<String>();
		
		List<Element> testNameElements = traverseContainedElements(testsElement);
		
		for (Element testNameElement : testNameElements) { // <testName>
			String testName = getString(testNameElement);
			tests.add(testName);
		}
		
		return tests;
	}
	
	public Map<Revision, Set<DiffFile>> parseDiffRecordsElement(
			Element diffRecordsElement) { // <Parents> or <RevertedFileRecords>
		Map<Revision, Set<DiffFile>> diffRecords = new HashMap<Revision, Set<DiffFile>>();
		
		List<Element> diffRecordElements = traverseContainedElements(diffRecordsElement);
		
		for (Element diffRecordElement : diffRecordElements) {
			Pair<Revision, Set<DiffFile>> pair = parseDiffRecordElement(diffRecordElement);
			Revision otherRevision = pair.getFirst();
			Set<DiffFile> diffFiles = pair.getSecond();
			
			diffRecords.put(otherRevision, diffFiles);
		}
		
		return diffRecords;
	}
	
	public abstract Pair<Revision, Set<DiffFile>> parseDiffRecordElement(
			Element diffRecordElement); // <Parent> or <RevertedFileRecord>
	
	public Set<DiffFile> parseDiffFilesElement(Element diffFilesElement) { // <DiffFiles>
		Set<DiffFile> diffFiles = new HashSet<DiffFile>();
		
		List<Element> diffFileElements = traverseContainedElements(diffFilesElement);
		
		for (Element diffFileElement : diffFileElements) { // <DiffFile>
			DiffFile diffFile = parseDiffFileElement(diffFileElement);
			diffFiles.add(diffFile);
		}
		
		return diffFiles;
	}
	
	public DiffFile parseDiffFileElement(Element diffFileElement) { // <DiffFile>
		Iterator<Element> iter = traverseContainedElements(diffFileElement).iterator();
		
		Element fileNameElement = iter.next();	// <fileName>
		Element diffTypeElement = iter.next();	// <DiffType>
		
		String fileName = getString(fileNameElement);
		DiffType diffType = parseDiffTypeElement(diffTypeElement);
		
		DiffFile diffFile = new DiffFile(diffType, fileName);
		
		return diffFile;
	}
	
	public DiffType parseDiffTypeElement(Element diffTypeElement) { // <DiffType>
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
	protected List<Element> traverseContainedElements(Element containerElement) {
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
	
	/**
	 * @return a String contained in the element.
	 */
	protected String getString(Element element) {
		Text text = (Text) element.getFirstChild();
		String str = text.getData().trim();
		
		return str;
	}
}
