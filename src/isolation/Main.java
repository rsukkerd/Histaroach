package isolation;

import java.io.IOException;

public class Main {

	/**
	 * @param args[0] : full path to the repository directory
	 * @param args[1] : full path to the output file
	 * @param args[2] : starting commit id
	 * @param args[3] : ending commit id
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException 
	{
		DataExtractor.extractData(args[0], args[1], args[2], args[3]);
	}

}
