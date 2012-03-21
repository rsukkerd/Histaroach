package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import common.Revision.COMPILABLE;

public class MixingTool {
	
	private static final Logger LOGGER = Logger.getLogger(MixingTool.class.getName());
	
	private final String repoPath;
	private final String clonedRepoPath;
	private final List<Flip> sortedToFailFlips;

	public MixingTool(HistoryGraph historyGraph, String repoPath, String clonedRepoPath) {
		Set<Flip> flips = historyGraph.getAllFlips();
		sortedToFailFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			if (flip.getToPassTests().isEmpty()) {
				sortedToFailFlips.add(flip);
			}
		}
		
		Collections.sort(sortedToFailFlips);
		
		this.repoPath = repoPath;
		this.clonedRepoPath = clonedRepoPath;			
	}
	
	public void run() throws Exception {
		
		for (Flip flip : sortedToFailFlips) {
			LOGGER.info("Flip:\n" + flip);
			
			List<MixedRevision> compilableMixedRevisions = revertChildToParent(flip);
			
			for (MixedRevision compilableMixedRevision : compilableMixedRevisions) {
				evaluateCompilableMixedRevision(flip, compilableMixedRevision);
			}
		}
	}
	
	/**
	 * 
	 * @requires flip has only TO_FAIL tests
	 * @return a list of compilable MixedRevision's sorted by 
	 * the number of reverted files
	 * @throws Exception 
	 */
	public List<MixedRevision> revertChildToParent(Flip flip) throws Exception {
		List<MixedRevision> compilableMixedRevisions = new ArrayList<MixedRevision>();
		
		Revision child = flip.getChildRevision();
		Revision parent = flip.getParentRevision();
		List<DiffFile> diffFiles = flip.getDiffFiles();
		
		MixedRevision mixedRevision = new MixedRevision(child, repoPath, clonedRepoPath);
		
		for (int r = 1; r < diffFiles.size(); r++) {
			CombinationGenerator generator = new CombinationGenerator(diffFiles.size(), r);
			
			while (generator.hasMore()) {
				int[] indices = generator.getNext();
				Set<DiffFile> combination = new HashSet<DiffFile>();
				
				for (int index : indices) {
					DiffFile diffFile = diffFiles.get(index);
					combination.add(diffFile);
				}
				
				mixedRevision.revertFiles(combination, parent);
				mixedRevision.compileAndRunAllTests();
				
				if (mixedRevision.isCompilable() == COMPILABLE.YES) {
					LOGGER.info("compilable MixedRevision:\n" + mixedRevision);
					
					MixedRevision mr = mixedRevision.export();
					compilableMixedRevisions.add(mr);
				} else {
					LOGGER.info("non-compilable MixedRevision:\n" + mixedRevision);
				}
				
				mixedRevision.restoreBaseRevision();
			}
		}
				
		return compilableMixedRevisions;
	}
	
	public void evaluateCompilableMixedRevision(Flip flip, MixedRevision compilableMixedRevision) {
		LOGGER.info("EVALUATION:\n" + "Flip:\n" + flip + "MixedRevision:\n" + compilableMixedRevision);
		
		Revision child = flip.getChildRevision();
		Set<String> childFailedTests = child.getTestResult().getFailedTests();
		Set<String> childPassedTests = new HashSet<String>();
		
		for (String test : child.getTestResult().getAllTests()) {
			if (!childFailedTests.contains(test)) {
				childPassedTests.add(test);
			}
		}
		
		Set<String> toFailTests = flip.getToFailTests();
		
		// tests pass in parent, fail in child, and pass in MixedRevision
		List<String> pass_fail_pass = new ArrayList<String>();
		// tests pass in parent, pass in child, and fail in MixedRevision
		List<String> pass_pass_fail = new ArrayList<String>();
		// tests fail in parent, fail in child, and pass in MixedRevision
		List<String> fail_fail_pass = new ArrayList<String>();
		
		TestResult mixedRevisionTestResult = compilableMixedRevision.getTestResult();
		
		for (String test : toFailTests) {
			if (mixedRevisionTestResult.pass(test)) {
				pass_fail_pass.add(test);
			}
		}
		
		for (String test : childPassedTests) {
			if (mixedRevisionTestResult.fail(test)) {
				pass_pass_fail.add(test);
			}
		}
		
		for (String test : childFailedTests) {
			if (mixedRevisionTestResult.pass(test) && !toFailTests.contains(test)) {
				fail_fail_pass.add(test);
			}
		}
		
		LOGGER.info("Tests that pass in parent, fail in child (flipped tests), and pass in MixedRevision:\n"
				+ pass_fail_pass);
		LOGGER.info("Tests that pass in parent, pass in child, and fail in MixedRevision:\n"
				+ pass_pass_fail);
		LOGGER.info("Tests that fail in parent, fail in child, and pass in MixedRevision:\n"
				+ fail_fail_pass);
	}
}
