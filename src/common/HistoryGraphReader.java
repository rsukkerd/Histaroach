package common;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * HistoryGraphReader reads in and reconstructs an instance of
 * HistoryGraph from a serialized file.
 */
public class HistoryGraphReader {
	private final String inputFileName;
	
	/**
	 * create a HistoryGraphReader instance
	 * 
	 * @param inputFileName : a HistoryGraph's serialized file
	 */
	public HistoryGraphReader(String inputFileName) {
		this.inputFileName = inputFileName;
	}
	
	/**
     * read historyGraph from a serialized file
     * 
     * @return historyGraph
     */
    public HistoryGraph readHistoryGraph() {
    	HistoryGraph hGraph = null;
    	ObjectInputStream input;
    	
    	try {
			input = new ObjectInputStream(new FileInputStream(inputFileName));
			hGraph = (HistoryGraph) input.readObject();
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return hGraph;
    }
}
