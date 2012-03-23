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
	
	private final Repository repository;
	private final Repository clonedRepository;
	private final List<Flip> sortedToFailFlips;
	
	private final String antCommand;

	public MixingTool(HistoryGraph historyGraph, Repository clonedRepository, String antCommand) {
		this.clonedRepository = clonedRepository;
		this.antCommand = antCommand;
		
		Set<Flip> flips = historyGraph.getAllFlips();
		sortedToFailFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			if (flip.getToPassTests().isEmpty()) {
				sortedToFailFlips.add(flip);
			}
		}
		
		Collections.sort(sortedToFailFlips);
		
		repository = historyGraph.getRepository();
	}
	
	public void run() throws Exception {
		
		for (Flip flip : sortedToFailFlips) {			
			List<MixedRevision> mixedRevisions = mixFlip(flip);
		}
	}
	
	/**
	 * 
	 * @return a list of all possible MixedRevisions of flip
	 * @throws Exception
	 */
	public List<MixedRevision> mixFlip(Flip flip) throws Exception {
		LOGGER.info("Flip:\n" + flip);
		
		List<MixedRevision> mixedRevisions = new ArrayList<MixedRevision>();
		
		List<DiffFile> diffFiles = flip.getDiffFiles();
		
		for (int r = 1; r < diffFiles.size(); r++) {
			CombinationGenerator generator = new CombinationGenerator(diffFiles.size(), r);
			
			while (generator.hasMore()) {
				int[] indices = generator.getNext();
				Set<DiffFile> combination = new HashSet<DiffFile>();
				
				for (int index : indices) {
					DiffFile diffFile = diffFiles.get(index);
					combination.add(diffFile);
				}
				
				MixedRevision mixedRevision = revertChildToParent(flip, combination);
				mixedRevisions.add(mixedRevision);
			}
		}
		
		return mixedRevisions;
	}
	
	public void runOneFlipOneCombination() throws Exception {
		Flip targetFlip = null;
		for (Flip flip : sortedToFailFlips) {
			if (flip.getChildRevision().getCommitID().equals("e701860")) {
				targetFlip = flip;
			}
		}
		
		LOGGER.info("Flip:\n" + targetFlip);
		
		Set<DiffFile> combination = new HashSet<DiffFile>();
		combination.add(targetFlip.getDiffFiles().get(0));
		
		revertChildToParent(targetFlip, combination);
	}
	
	/**
	 * 
	 * @requires flip has only TO_FAIL tests
	 * @return a MixedRevision that has flip's child as a base revision, 
	 *         and has files in diffFilesToBeReverted reverted to their 
	 *         states in flip's parent
	 * @throws Exception 
	 */
	public MixedRevision revertChildToParent(Flip flip, Set<DiffFile> diffFilesToBeReverted) 
		throws Exception {		
		Revision child = flip.getChildRevision();
		Revision parent = flip.getParentRevision();
		
		MixedRevision mixedRevision = new MixedRevision(child, repository, clonedRepository, antCommand);
		
		mixedRevision.revertFiles(diffFilesToBeReverted, parent);
		// mixedRevision.compileAndRunAllTests();
		mixedRevision.runAntJunitViaShell();
		
		MixedRevision exportedMixedRevision = mixedRevision.export();
		
		if (exportedMixedRevision.isCompilable() == COMPILABLE.YES) {
			LOGGER.info("compilable MixedRevision:\n" + exportedMixedRevision);
			
			evaluateCompilableMixedRevision(flip, exportedMixedRevision);
		} else {
			LOGGER.info("non-compilable MixedRevision:\n" + exportedMixedRevision);
		}
		
		mixedRevision.restoreBaseRevision();
				
		return exportedMixedRevision;
	}
	
	/**
	 * Log evaluation of compilableMixedRevision
	 */
	public void evaluateCompilableMixedRevision(Flip flip, MixedRevision compilableMixedRevision) {
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
		
		String eval = "Tests that pass in parent, fail in child (flipped tests), and pass in MixedRevision:\n"
			+ pass_fail_pass + "\n";
		eval += "Tests that pass in parent, pass in child, and fail in MixedRevision:\n"
			+ pass_pass_fail + "\n";
		eval += "Tests that fail in parent, fail in child, and pass in MixedRevision:\n"
			+ fail_fail_pass;
		
		LOGGER.info("EVALUATION:\n" + eval);
	}
}
