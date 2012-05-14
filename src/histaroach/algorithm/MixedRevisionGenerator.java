package histaroach.algorithm;

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
	 * @return a list of all possible MixedRevisions from flips. 
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
	 * @return a list of all possible MixedRevisions from flip. 
	 *         These MixedRevisions do not know their Compilable state 
	 *         nor their TestResult.
	 */
	public List<MixedRevision> generateMixedRevisionsFromFlip(Flip flip) {
		List<MixedRevision> mixedRevisionsOfFlip = new ArrayList<MixedRevision>();
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
								
				MixedRevision mixedRevision = new MixedRevision(flip.getChildRevision(), 
						repository, clonedRepository);
				mixedRevision.setRevertedFiles(combination, flip.getParentRevision());
				
				mixedRevisionsOfFlip.add(mixedRevision);
			}
		}
		
		return mixedRevisionsOfFlip;
	}
}