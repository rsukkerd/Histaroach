package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import common.Revision.COMPILABLE;

public class MixingTool {
	private final String clonedRepoPath;
	private final List<Flip> sortedToFailFlips;

	public MixingTool(HistoryGraph historyGraph, String clonedRepoPath) {
		Set<Flip> flips = historyGraph.getAllFlips();
		sortedToFailFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			if (flip.getToPassTests().isEmpty()) {
				sortedToFailFlips.add(flip);
			}
		}
		
		Collections.sort(sortedToFailFlips);
		this.clonedRepoPath = clonedRepoPath;			
	}
	
	public void run() throws IOException {
		for (Flip flip : sortedToFailFlips) {
			List<MixedRevision> compilableMixedRevisions = revertChildToParent(flip);
			
			evaluateCompilableMixedRevisions(flip, compilableMixedRevisions);
		}
	}
	
	/**
	 * 
	 * @requires flip has only TO_FAIL tests
	 * @return a list of compilable MixedRevision's sorted by 
	 * the number of reverted files
	 * @throws IOException
	 */
	public List<MixedRevision> revertChildToParent(Flip flip) throws IOException {
		List<MixedRevision> compilableMixedRevisions = new ArrayList<MixedRevision>();
		
		Revision child = flip.getChildRevision();
		Revision parent = flip.getParentRevision();
		List<DiffFile> diffFiles = flip.getDiffFiles();
		
		MixedRevision mixedRevision = new MixedRevision(child, clonedRepoPath);
		
		for (int r = 1; r < diffFiles.size(); r++) {
			CombinationGenerator generator = new CombinationGenerator(diffFiles.size(), r);
			
			while (generator.hasMore()) {
				int[] indices = generator.getNext();
				List<DiffFile> combination = new ArrayList<DiffFile>();
				
				for (int index : indices) {
					DiffFile diffFile = diffFiles.get(index);
					combination.add(diffFile);
				}
				
				mixedRevision.revertFiles(combination, parent);
				mixedRevision.compileAndRunAllTests();
				
				if (mixedRevision.isCompilable() == COMPILABLE.YES) {
					MixedRevision mr = mixedRevision.export();
					compilableMixedRevisions.add(mr);
				}
				
				mixedRevision.restoreBaseRevision();
			}
		}
				
		return compilableMixedRevisions;
	}
	
	public void evaluateCompilableMixedRevisions(Flip flip, List<MixedRevision> compilableMixedRevisions) {
		System.out.println(flip);
		
		Revision child = flip.getChildRevision();
		Set<String> childFailedTests = child.getTestResult().getFailedTests();
		Set<String> childPassedTests = new HashSet<String>();
		
		for (String test : child.getTestResult().getAllTests()) {
			if (!childFailedTests.contains(test)) {
				childPassedTests.add(test);
			}
		}
		
		Set<String> toFailTests = flip.getToFailTests();
		
		for (MixedRevision mixedRevision : compilableMixedRevisions) {
			int numRecoverTest = 0;
			int numBreakTest = 0;
			int numUnexpectedRecoverTest = 0;
			
			TestResult testResult = mixedRevision.getTestResult();
			
			for (String test : toFailTests) {
				if (testResult.pass(test)) {
					numRecoverTest++;
				}
			}
			
			for (String test : childPassedTests) {
				if (testResult.fail(test)) {
					numBreakTest++;
				}
			}
			
			for (String test : childFailedTests) {
				if (testResult.pass(test) && !toFailTests.contains(test)) {
					numUnexpectedRecoverTest++;
				}
			}
			
			System.out.println(mixedRevision);
			System.out.println("number of recovered tests: " + numRecoverTest);
			System.out.println("number of broken tests: " + numBreakTest);
			System.out.println("number of unexpected recovered tests: " + numUnexpectedRecoverTest);
		}		
	}
}
