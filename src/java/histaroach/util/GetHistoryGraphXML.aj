package histaroach.util;

import java.io.File;

import histaroach.DataCollector;
import histaroach.model.HistoryGraph;
import histaroach.model.Revision;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public aspect GetHistoryGraphXML {
	pointcut addRevision():
		execution(void HistoryGraph.addRevision(Revision));
	
	after() returning: addRevision() {
		String filename = DataCollector.DATA_PATH + File.separatorChar 
						+ DataCollector.HISTORYGRAPH_PREFIX 
						+ DataCollector.XML_EXTENSION;
		File xmlFile = new File(filename);
		HistoryGraph hGraph = (HistoryGraph) thisJoinPoint.getThis();
		XMLWriter writer;
		try {
			writer = new HistoryGraphXMLWriter(xmlFile, hGraph);
			writer.buildDocument();
		} catch (ParserConfigurationException e) {
			System.err.println("Error: ParserConfigurationException thrown when " + 
					"attempted to instantiate HistoryGraphXMLWriter.");
			e.printStackTrace();
		} catch (TransformerException e) {
			System.err.println("Error: TransformerException thrown during " + 
					"XMLWriter.buildDocument() execution.");
			e.printStackTrace();
		}
	}
}
