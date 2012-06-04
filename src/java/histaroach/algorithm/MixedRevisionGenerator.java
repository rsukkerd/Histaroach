package histaroach.algorithm;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.model.DiffFile;
import histaroach.model.Flip;
import histaroach.model.IRepository;
import histaroach.model.MixedRevision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * MixedRevisionGenerator contains algorithms to generate MixedRevision. 
 * 
 * A newly generated MixedRevision does not know its Compilable state 
 * nor its TestResult (ie. Compilable = UNKNOWN and TestResult = null). 
 * It only knows its baseRevision, its reverted files and what Revisions 
 * those files are reverted to.
 */
public class MixedRevisionGenerator {
		
	private static final int MAX_NUM_DIFF_FILES = 7;
	
	private final IRepository repository;
	private final IRepository clonedRepository;
	
	/**
	 * Encodes sorting and filtering policies for flips.
	 * 
	 * @return a list of sorted, filtered flips.
	 */
	public static List<Flip> sortAndFilter(Set<Flip> flips) {
		List<Flip> sortedFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			
			if (flip.getDiffFiles().size() <= MAX_NUM_DIFF_FILES) {
				sortedFlips.add(flip);
			}
		}
		
		Collections.sort(sortedFlips);
		
		return sortedFlips;
	}
	
	public MixedRevisionGenerator(IRepository repository, IRepository clonedRepository) {
		this.repository = repository;
		this.clonedRepository = clonedRepository;
	}
	
	/**
	 * @return a list of all possible MixedRevisions (but not including 
	 *         mixing test files) from flips. 
	 *         These MixedRevisions do not know their Compilable state 
	 *         nor their TestResult.
	 */
	public List<MixedRevision> generateMixedRevisionsFromFlips(Set<Flip> flips) {
		List<Flip> orderedFlips = sortAndFilter(flips);
		List<MixedRevision> mixedRevisions = new ArrayList<MixedRevision>();
		
		for (Flip flip : orderedFlips) {
			List<MixedRevision> mixedRevisionsOfFlip = generateMixedRevisionsFromFlip(flip);
			mixedRevisions.addAll(mixedRevisionsOfFlip);
		}
		
		return mixedRevisions;
	}
	
	/**
	 * @return a list of all possible MixedRevisions (but not including 
	 *         mixing test files) from flip. 
	 *         These MixedRevisions do not know their Compilable state 
	 *         nor their TestResult.
	 */
	public List<MixedRevision> generateMixedRevisionsFromFlip(Flip flip) {
		List<MixedRevision> mixedRevisionsOfFlip = new ArrayList<MixedRevision>();
		Set<DiffFile> nonTestDiffFiles = filterDiffFiles(flip.getDiffFiles());
		
		for (int r = 1; r < nonTestDiffFiles.size(); r++) {			
			CombinationGenerator<DiffFile> combinations = 
				new CombinationGenerator<DiffFile>(nonTestDiffFiles, r);
			
			for (Set<DiffFile> combination : combinations) {
				MixedRevision mixedRevision = new MixedRevision(flip.getChildRevision(), 
						repository, clonedRepository);
				mixedRevision.setRevertedFiles(combination, flip.getParentRevision());
				
				mixedRevisionsOfFlip.add(mixedRevision);
			}
		}
		
		return mixedRevisionsOfFlip;
	}

	/**
	 * Filters out test files from diffFiles.
	 * 
	 * @return a set of all non-test DiffFiles in diffFiles.
	 */
	public Set<DiffFile> filterDiffFiles(Set<DiffFile> diffFiles) {
		IBuildStrategy buildStrategy = repository.getBuildStrategy();
		Set<DiffFile> nonTestDiffFiles = new HashSet<DiffFile>();
		
		for (DiffFile diffFile : diffFiles) {
			
			if (!buildStrategy.isTestFile(diffFile)) {
				nonTestDiffFiles.add(diffFile);
			}
		}
		
		return nonTestDiffFiles;
	}
}
