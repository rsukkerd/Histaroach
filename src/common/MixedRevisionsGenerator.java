package common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.DiffFile.DiffType;
import common.Revision.Compilable;

public class MixedRevisionsGenerator {
	
	private static final String MIXED_REVISION_ANALYSIS = "output/mixed_revision_analysis.txt";
	private static final String HEADER = "Child Parent Mixed Test Compilable ParentResult ChildResult MixedResult\n";
	private static final char COLUMN_SEPARATOR_CHAR = ' ';
	private static final char FILE_SEPARATOR_CHAR = ',';
	private static final char ADDED_CHAR = '+';
	private static final char DELETED_CHAR = '-';
	private static final char MODIFIED_CHAR = '~';
	private static final char TRUE = '1';
	private static final char FALSE = '0';
	private static final char NONE = 'n';
	
	private List<MixedRevisionTemplate> mixedRevisionTemplates;
	
	private Repository repository;
	private Repository clonedRepository;
	
	private BufferedWriter out;
	
	public MixedRevisionsGenerator(List<MixedRevisionTemplate> mixedRevisionTemplates, 
			Repository repository, Repository clonedRepository) throws IOException{
		this.mixedRevisionTemplates = mixedRevisionTemplates;
		this.repository = repository;
		this.clonedRepository = clonedRepository;
	}
	
	public void constructSimpleMixedRevisions(int startIndex, int numElements) throws Exception {
		FileWriter fstream = new FileWriter(MIXED_REVISION_ANALYSIS);
		out = new BufferedWriter(fstream);
		
		for (int i = startIndex; i < startIndex + numElements; i++) {
			MixedRevisionTemplate template = mixedRevisionTemplates.get(i);
			
			Revision baseRevision = template.getBaseRevision();
			Map<Revision, Set<DiffFile>> revertedFiles = template.getRevertedFiles();
			
			MixedRevision mixedRevision = new MixedRevision(baseRevision, repository, clonedRepository);
			
			assert revertedFiles.keySet().size() == 1; // simple MixedRevision
			
			for (Revision otherRevision : revertedFiles.keySet()) {
				Set<DiffFile> diffFiles = revertedFiles.get(otherRevision);
				
				mixedRevision.revertFiles(diffFiles, otherRevision);
				mixedRevision.runTest();
				
				MixedRevision exportedMixedRevision = mixedRevision.export();
				analyzeSimpleMixedRevision(exportedMixedRevision, otherRevision);
				
				mixedRevision.restoreBaseRevision();
			}
		}
		
		out.close();
	}
	
	public void analyzeSimpleMixedRevision(MixedRevision mixedRevision, Revision otherRevision) 
			throws IOException {
		out.write(HEADER);
		
		Revision baseRevision = mixedRevision.getBaseRevision();
		Map<DiffFile, Revision> revertedFiles = mixedRevision.getRevertedFiles();
		
		out.write(baseRevision.getCommitID() + COLUMN_SEPARATOR_CHAR + 
				otherRevision.getCommitID() + COLUMN_SEPARATOR_CHAR);
		
		Iterator<DiffFile> iter = revertedFiles.keySet().iterator();
						
		while (iter.hasNext()) {
			DiffFile diffFile = iter.next();
			
			String fileName = diffFile.getFileName();
			DiffType type = diffFile.getDiffType();
			
			if (type == DiffType.ADDED)
				out.write(ADDED_CHAR);
			else if (type == DiffType.DELETED)
				out.write(DELETED_CHAR);
			else
				out.write(MODIFIED_CHAR);
			
			out.write(fileName);
			
			if (iter.hasNext())
				out.write(FILE_SEPARATOR_CHAR);
			else
				out.write(COLUMN_SEPARATOR_CHAR);
		}
		
		TestResult baseTestResult = baseRevision.getTestResult();
		TestResult otherTestResult = otherRevision.getTestResult();
		
		assert baseTestResult != null && otherTestResult != null;
		
		Set<String> baseTests = baseTestResult.getAllTests();
		
		if (mixedRevision.isCompilable() == Compilable.YES) {
			out.write(TRUE + COLUMN_SEPARATOR_CHAR); // compilable
			
			TestResult mixedTestResult = mixedRevision.getTestResult();
			
			for (String test : baseTests) {
				if (otherTestResult.pass(test) && 
						baseTestResult.pass(test) && 
						mixedTestResult.pass(test)) {
					writeTestResults(test, TRUE, TRUE, TRUE);
				} else if (otherTestResult.pass(test) && 
						baseTestResult.pass(test) && 
						mixedTestResult.fail(test)) {
					writeTestResults(test, TRUE, TRUE, FALSE);
				} else if (otherTestResult.pass(test) && 
						baseTestResult.fail(test) && 
						mixedTestResult.pass(test)) {
					writeTestResults(test, TRUE, FALSE, TRUE);
				} else if (otherTestResult.pass(test) && 
						baseTestResult.fail(test) && 
						mixedTestResult.fail(test)) {
					writeTestResults(test, TRUE, FALSE, FALSE);
				} else if (otherTestResult.fail(test) && 
						baseTestResult.pass(test) && 
						mixedTestResult.pass(test)) {
					writeTestResults(test, FALSE, TRUE, TRUE);
				} else if (otherTestResult.fail(test) && 
						baseTestResult.pass(test) && 
						mixedTestResult.fail(test)) {
					writeTestResults(test, FALSE, TRUE, FALSE);
				} else if (otherTestResult.fail(test) && 
						baseTestResult.fail(test) && 
						mixedTestResult.pass(test)) {
					writeTestResults(test, FALSE, FALSE, TRUE);
				} else if (otherTestResult.fail(test) && 
						baseTestResult.fail(test) && 
						mixedTestResult.fail(test)) {
					writeTestResults(test, FALSE, FALSE, FALSE);
				}
				// else: test is absent in otherRevision or mixedRevision. 
			}
		} else {
			out.write(FALSE + COLUMN_SEPARATOR_CHAR);		// non-compilable
			out.write(NONE + COLUMN_SEPARATOR_CHAR + 	// test
					NONE + COLUMN_SEPARATOR_CHAR +		// ParentResult
					NONE + COLUMN_SEPARATOR_CHAR +		// ChildResult
					NONE + '\n');						// MixedResult
		}
	}
	
	public void writeTestResults(String test, char parentResult, char childResult, char mixedResult) 
			throws IOException {
		out.write(test + COLUMN_SEPARATOR_CHAR + 
				parentResult + COLUMN_SEPARATOR_CHAR + 
				childResult + COLUMN_SEPARATOR_CHAR + 
				mixedResult + '\n');
	}
}
