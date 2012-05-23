package histaroach.algorithm;

import histaroach.model.DiffFile;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.MixedRevision;
import histaroach.model.Revision;
import histaroach.model.Revision.Compilable;
import histaroach.model.TestResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * MixedRevisionAnalysis performs file manipulation to create actual 
 * mixed revisions on the file system, runs tests on them and records 
 * the results and all data necessary for Histaroach analysis.
 */
public class MixedRevisionAnalysis {
		
	private static final String COLUMN_SEPARATOR_CHAR = ";";
	private static final String NEW_LINE_CHAR = "\n";
	
	private static final String HEADER = 
		"mixedRevisionID baseRevisionID otherRevisionID file revertType " + 
		"compilable test mixedTestResult baseTestResult otherTestResult" + NEW_LINE_CHAR;
	
	private static final String ADDED_CHAR = "+";
	private static final String DELETED_CHAR = "-";
	private static final String MODIFIED_CHAR = "~";
	private static final String TRUE = "1";
	private static final String FALSE = "0";
	private static final String NONE = "n";

	private final List<MixedRevision> mixedRevisions;
	
	public MixedRevisionAnalysis(List<MixedRevision> mixedRevisions) {
		this.mixedRevisions = mixedRevisions;
	}
	
	/**
	 * For all MixedRevisions in mixedRevisions, creates actual mixed revisions 
	 * on the file system, runs tests on them and records the results to 
	 * an output file.
	 * 
	 * @throws Exception
	 */
	public void runTestOnMixedRevisions(File outputFile) throws Exception {
		runTestOnMixedRevisions(0, mixedRevisions.size(), outputFile);
	}
	
	/**
	 * For a specified range in mixedRevisions, creates actual mixed revisions 
	 * on the file system, runs tests on them and records the results to 
	 * an output file.
	 * 
	 * @throws Exception
	 */
	public void runTestOnMixedRevisions(int startIndex, int numElements, 
			File outputFile) throws Exception {
		FileWriter fstream = new FileWriter(outputFile);
		BufferedWriter out = new BufferedWriter(fstream);
		out.write(HEADER);
		out.flush();
		
		for (int i = startIndex; i < startIndex + numElements; i++) {
			// mixedRevision already has its revertedFiles set
			MixedRevision mixedRevision = mixedRevisions.get(i);
			
			mixedRevision.checkoutBaseRevision();
			mixedRevision.revertFiles();
			mixedRevision.runTest();
			
			String lines = analyzeMixedRevision(mixedRevision, i);
			out.write(lines);
			out.flush();
			
			mixedRevision.restoreBaseRevision();
		}
		
		out.close();
	}
	
	/**
	 * Records data of mixedRevision.
	 * 
	 * @return a String representation of data of mixedRevision.
	 */
	public String analyzeMixedRevision(MixedRevision mixedRevision, 
			int mixedRevisionID) {
		String lines = "";
		
		Revision baseRevision = mixedRevision.getBaseRevision();
		TestResult baseTestResult = baseRevision.getTestResult();
		
		Map<Set<DiffFile>, Revision> revertedFileRecords = 
			mixedRevision.getRevertedFileRecords();
		
		for (Map.Entry<Set<DiffFile>, Revision> entry : revertedFileRecords.entrySet()) {
			Set<DiffFile> revertedFiles = entry.getKey();
			
			Revision otherRevision = entry.getValue();
			TestResult otherTestResult = otherRevision.getTestResult();
			
			// mixedRevisionID baseRevisionID otherRevisionID
			String lineHeader = mixedRevisionID + COLUMN_SEPARATOR_CHAR + 
				baseRevision.getCommitID() + COLUMN_SEPARATOR_CHAR + 
				otherRevision.getCommitID();
			
			for (DiffFile revertedFile : revertedFiles) {
				// file revertType
				String lineRevertedFile = getLineRevertedFile(revertedFile);
			
				lines += getFullLine(mixedRevision, baseTestResult, otherTestResult, 
						lineHeader, lineRevertedFile) + NEW_LINE_CHAR;
			}
		}
		
		return lines;
	}
	
	/**
	 * Line format: 
	 * mixedRevisionID baseRevisionID otherRevisionID 
	 * file revertType 
	 * compilable test mixedTestResult baseTestResult otherTestResult
	 */
	private String getFullLine(MixedRevision mixedRevision, 
			TestResult baseTestResult, TestResult otherTestResult, 
			String lineHeader, String lineRevertedFile) {
		String line = "";
		
		if (mixedRevision.isCompilable() == Compilable.YES) {
			TestResult mixedTestResult = mixedRevision.getTestResult();
			
			for (String test : baseTestResult.getAllTests()) {
				line += lineHeader + COLUMN_SEPARATOR_CHAR;
				line += lineRevertedFile + COLUMN_SEPARATOR_CHAR;
				
				// compilable
				line += TRUE + COLUMN_SEPARATOR_CHAR;
				
				// test mixedTestResult baseTestResult otherTestResult
				line += getLineTestResults(test, mixedTestResult, baseTestResult, 
						otherTestResult) + NEW_LINE_CHAR;
			}
		} else {
			line += lineHeader + COLUMN_SEPARATOR_CHAR;
			line += lineRevertedFile + COLUMN_SEPARATOR_CHAR;
			
			// compilable
			line += FALSE + COLUMN_SEPARATOR_CHAR;
			
			line += NONE + COLUMN_SEPARATOR_CHAR +	// test
					NONE + COLUMN_SEPARATOR_CHAR +	// baseTestResult
					NONE + COLUMN_SEPARATOR_CHAR +	// otherTestResult
					NONE + NEW_LINE_CHAR;			// mixedTestResult
		}
		
		return line;
	}
	
	/**
	 * Format: file revertType
	 */
	private String getLineRevertedFile(DiffFile revertedFile) {
		String res = "";
		
		String fileName = revertedFile.getFileName();
		DiffType diffType = revertedFile.getDiffType();
		
		res += fileName + COLUMN_SEPARATOR_CHAR;
		
		if (diffType == DiffType.ADDED) {
			res += DELETED_CHAR;
		} else if (diffType == DiffType.DELETED) {
			res += ADDED_CHAR;
		} else {
			res += MODIFIED_CHAR;
		}
		
		return res;
	}
	
	/**
	 * Format: test mixedTestResult baseTestResult otherTestResult
	 */
	private String getLineTestResults(String test, TestResult mixedTestResult, 
			TestResult baseTestResult, TestResult otherTestResult) {
		String testResults = test + COLUMN_SEPARATOR_CHAR + 
					mixedTestResult.encodeAsString(test) + COLUMN_SEPARATOR_CHAR + 
					baseTestResult.encodeAsString(test) + COLUMN_SEPARATOR_CHAR + 
					otherTestResult.encodeAsString(test);
		
		return testResults;
	}
}
