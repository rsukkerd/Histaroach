package util;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import common.DiffFile;
import common.HistoryGraph;
import common.Revision;
import common.TestResult;

public class HistoryGraphXMLWriter {
	
	public static final String HISTORY_GRAPH = "HistoryGraph";
	public static final String REVISION = "Revision";
	public static final String COMMIT_ID = "commitID";
	public static final String COMPILABLE = "Compilable";
	public static final String TEST_RESULT = "TestResult";
	public static final String TESTS = "Tests";
	public static final String FAILED_TESTS = "FailedTests";
	public static final String TEST_NAME = "testName";
	public static final String PARENTS = "Parents";
	public static final String PARENT = "Parent";
	public static final String DIFF_FILES = "DiffFiles";
	public static final String DIFF_FILE = "DiffFile";
	public static final String FILE_NAME = "fileName";
	public static final String DIFF_TYPE = "DiffType";
	
	private final HistoryGraph hGraph;
	private final File hGraphXML;
	private final Document doc;
	
	public HistoryGraphXMLWriter(HistoryGraph hGraph, File hGraphXML) 
			throws ParserConfigurationException {
		this.hGraph = hGraph;
		this.hGraphXML = hGraphXML;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		doc = builder.newDocument();
	}
	
	public void write() throws ParserConfigurationException, 
			TransformerException {
		Element rootElement = doc.createElement(HISTORY_GRAPH);
		
		for (Revision revision : hGraph) {
			Element revisionElement = createRevisionElement(revision);
			rootElement.appendChild(revisionElement);
		}
		
		doc.appendChild(rootElement);
		
		/* write the content into xml file */
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		
		// set indentation
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(hGraphXML);
		transformer.transform(source, result);
	}
	
	private Element createRevisionElement(Revision revision) {
		Element revisionElement = doc.createElement(REVISION);
		
		Element commitIDElement = createCommitIDElement(revision);
		Element compilableElement = createCompilableElement(revision);
		Element testResultElement = createTestResultElement(revision);
		Element parentsElement = createParentsElement(revision);
		
		revisionElement.appendChild(commitIDElement);
		revisionElement.appendChild(compilableElement);
		revisionElement.appendChild(testResultElement);
		revisionElement.appendChild(parentsElement);
		
		return revisionElement;
	}
	
	private Element createCommitIDElement(Revision revision) {
		Element commitIDElement = doc.createElement(COMMIT_ID);
		
		String commitID = revision.getCommitID();
		addText(commitIDElement, commitID);
		
		return commitIDElement;
	}
	
	private Element createCompilableElement(Revision revision) {
		Element compilableElement = doc.createElement(COMPILABLE);
		
		String compilableStr = revision.isCompilable().toString();
		addText(compilableElement, compilableStr);
		
		return compilableElement;
	}
	
	private Element createTestResultElement(Revision revision) {
		Element testResultElement = doc.createElement(TEST_RESULT);
		
		TestResult testResult = revision.getTestResult();
		
		if (testResult != null) {
			Set<String> allTests = testResult.getAllTests();
			Set<String> failedTests = testResult.getFailedTests();
			
			Element allTestsElement = doc.createElement(TESTS);
			Element failedTestsElement = doc.createElement(FAILED_TESTS);
			
			addTestNameElements(allTestsElement, allTests);
			addTestNameElements(failedTestsElement, failedTests);
			
			testResultElement.appendChild(allTestsElement);
			testResultElement.appendChild(failedTestsElement);
		}
		
		return testResultElement;
	}
	
	private Element createParentsElement(Revision revision) {
		Element parentsElement = doc.createElement(PARENTS);
		
		Set<Revision> parents = revision.getParents();
		
		for (Revision parent : parents) {
			List<DiffFile> diffFiles = revision.getDiffFiles(parent);
			
			Element parentElement = createParentElement(parent, diffFiles);
			parentsElement.appendChild(parentElement);
		}
		
		return parentsElement;
	}
	
	private Element createParentElement(Revision parent, List<DiffFile> diffFiles) {		
		Element parentElement = doc.createElement(PARENT);
		
		Element commitIDElement = createCommitIDElement(parent);
		Element diffFilesElement = createDiffFilesElement(diffFiles);
		
		parentElement.appendChild(commitIDElement);
		parentElement.appendChild(diffFilesElement);
		
		return parentElement;
	}
	
	private Element createDiffFilesElement(List<DiffFile> diffFiles) {
		Element diffFilesElement = doc.createElement(DIFF_FILES);
		
		for (DiffFile diffFile : diffFiles) {
			Element diffFileElement = createDiffFileElement(diffFile);
			diffFilesElement.appendChild(diffFileElement);
		}
		
		return diffFilesElement;
	}
	
	private Element createDiffFileElement(DiffFile diffFile) {
		Element diffFileElement = doc.createElement(DIFF_FILE);
		
		String fileName = diffFile.getFileName();
		String diffTypeStr = diffFile.getDiffType().toString();
		
		Element fileNameElement = doc.createElement(FILE_NAME);
		addText(fileNameElement, fileName);
		
		Element diffTypeElement = doc.createElement(DIFF_TYPE);
		addText(diffTypeElement, diffTypeStr);
		
		diffFileElement.appendChild(fileNameElement);
		diffFileElement.appendChild(diffTypeElement);
		
		return diffFileElement;
	}
	
	private void addTestNameElements(Element testsElement, Set<String> tests) {
		for (String testName : tests) {
			Element testNameElement = doc.createElement(TEST_NAME);
			addText(testNameElement, testName);
			
			testsElement.appendChild(testNameElement);
		}
	}

	private void addText(Element element, String data) {
		Text text = doc.createTextNode(data);
		element.appendChild(text);
	}
}
