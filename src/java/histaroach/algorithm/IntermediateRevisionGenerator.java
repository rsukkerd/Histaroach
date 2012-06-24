package histaroach.algorithm;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile;
import histaroach.model.Flip;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.IntermediateRevision;
import histaroach.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * IntermediateRevisionGenerator contains algorithms to generate IntermediateRevision. 
 * 
 * A newly generated IntermediateRevision does not know its Compilable state 
 * nor its TestResult (ie. Compilable = UNKNOWN and TestResult = null). 
 * It only knows its base-successor Revisions and delta.
 */
public class IntermediateRevisionGenerator {
		
	private static final int MAX_NUM_DIFF_FILES = 7;
	
	private final HistoryGraph historyGraph;
	private final IRepository repository;
	private final IRepository clonedRepository;
	
	public IntermediateRevisionGenerator(HistoryGraph historyGraph, 
			IRepository repository, IRepository clonedRepository) {
		this.historyGraph = historyGraph;
		this.repository = repository;
		this.clonedRepository = clonedRepository;
	}
	
	/**
	 * @return a list of all possible IntermediateRevisions from all target Flips 
	 *         in historyGraph.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public List<IntermediateRevision> generateIntermediateRevisions() 
			throws IOException, InterruptedException {
		Set<Flip> flips = historyGraph.getToFailFlips();
		List<Flip> targetFlips = filterAndSort(flips);
		
		return generateIntermediateRevisions(targetFlips);
	}
	
	/**
	 * @return a list of all possible IntermediateRevisions from a list of Flips. 
	 *         (Note: use the latest tests) 
	 *         These IntermediateRevisions do not know their Compilable state 
	 *         nor their TestResult.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public List<IntermediateRevision> generateIntermediateRevisions(List<Flip> flips) 
			throws IOException, InterruptedException {
		List<IntermediateRevision> intermediateRevisions = new ArrayList<IntermediateRevision>();
		
		for (Flip flip : flips) {
			List<IntermediateRevision> intermediateRevisionsOfFlip = 
				generateIntermediateRevisions(flip);
			intermediateRevisions.addAll(intermediateRevisionsOfFlip);
		}
		
		return intermediateRevisions;
	}
	
	/**
	 * @return a list of all possible IntermediateRevisions from a single Flip. 
	 *         (Note: use the latest tests) 
	 *         These IntermediateRevisions do not know their Compilable state 
	 *         nor their TestResult.
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public List<IntermediateRevision> generateIntermediateRevisions(Flip flip) 
			throws IOException, InterruptedException {
		List<IntermediateRevision> intermediateRevisions = 
			new ArrayList<IntermediateRevision>();
		
		Pair<Set<DiffFile>, Set<DiffFile>> res = separateTestFromNonTest(flip.getDiffFiles());
		Set<DiffFile> nonTestDelta = res.getFirst();
		Set<DiffFile> testDelta = res.getSecond();
				
		for (int r = 1; r < nonTestDelta.size(); r++) {			
			CombinationGenerator<DiffFile> combinations = 
				new CombinationGenerator<DiffFile>(nonTestDelta, r);
						
			for (Set<DiffFile> combination : combinations) {
				combination.addAll(testDelta); // use the latest tests
				IntermediateRevision intermediateRevision = new IntermediateRevision(
						flip.getParentRevision(), flip.getChildRevision(), 
						repository, clonedRepository);
				intermediateRevision.setDelta(combination);
				
				intermediateRevisions.add(intermediateRevision);
			}
		}
		
		return intermediateRevisions;
	}
	
	/**
	 * Filters out Flips that have non-test delta of size <= 1 or > MAX_NUM_DIFF_FILES, 
	 * and sorts the remaining Flips into ascending order.
	 * 
	 * @return a list of target Flips.
	 */
	private List<Flip> filterAndSort(Set<Flip> flips) {
		List<Flip> targetFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			Set<DiffFile> nonTestDelta = separateTestFromNonTest(
					flip.getDiffFiles()).getFirst();
			
			if (nonTestDelta.size() > 1 
					&& nonTestDelta.size() <= MAX_NUM_DIFF_FILES) {
				targetFlips.add(flip);
			}
		}
		
		Collections.sort(targetFlips);
		
		return targetFlips;
	}

	/**
	 * Divides totalDelta into non-test delta and test delta.
	 * 
	 * @return (non-test delta, test delta)
	 */
	private Pair<Set<DiffFile>, Set<DiffFile>> separateTestFromNonTest(Set<DiffFile> totalDelta) {
		Set<DiffFile> nonTestDelta = new HashSet<DiffFile>();
		Set<DiffFile> testDelta = new HashSet<DiffFile>();
		IBuildStrategy buildStrategy = repository.getBuildStrategy();
		
		for (DiffFile diffFile : totalDelta) {
			
			if (buildStrategy.isTestFile(diffFile)) {
				testDelta.add(diffFile);
			} else {
				nonTestDelta.add(diffFile);
			}
		}
		
		return new Pair<Set<DiffFile>, Set<DiffFile>>(nonTestDelta, testDelta);
	}
}
