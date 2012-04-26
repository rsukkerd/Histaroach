package common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import util.CombinationGenerator;
import util.Util;

/**
 * MixedRevisionTemplatesGenerator gets all Flips occurred in 
 * a particular HistoryGraph, filters out Flips that are too 
 * large in the number of DiffFiles, and sorts the remaining Flips. 
 * 
 * For each Flip in the filtered, sorted list of Flips, 
 * MixedRevisionTemplatesGenerator generates a list of 
 * MixedRevisionTemplates from it.
 */
public class MixedRevisionTemplatesGenerator {
	
	public static final String MIXED_REVISION_TEMPLATES_FILE = "output/mixed_revision_templates.ser";
	
	private static final int MAX_NUM_DIFF_FILES = 7;
	
	private final List<Flip> sortedFlips;
	private final List<MixedRevisionTemplate> mixedRevisionTemplates;
	
	public MixedRevisionTemplatesGenerator(HistoryGraph historyGraph) {
		Set<Flip> flips = historyGraph.getAllFlips();
		sortedFlips = new ArrayList<Flip>();
		
		for (Flip flip : flips) {
			if (flip.getDiffFiles().size() <= MAX_NUM_DIFF_FILES) {
				sortedFlips.add(flip);
			}
		}
		
		Collections.sort(sortedFlips);
		
		mixedRevisionTemplates = new ArrayList<MixedRevisionTemplate>();
	}
	
	public void generateMixedRevisionTemplates() {
		for (Flip flip : sortedFlips) {			
			List<MixedRevisionTemplate> templates = getMixedRevisionTemplates(flip);
			mixedRevisionTemplates.addAll(templates);
		}
	}
	
	public void writeOutMixedRevisionTemplates() {
		Util.writeToSerializedFile(MIXED_REVISION_TEMPLATES_FILE, mixedRevisionTemplates);
	}
	
	private List<MixedRevisionTemplate> getMixedRevisionTemplates(Flip flip) {		
		List<MixedRevisionTemplate> templates = new ArrayList<MixedRevisionTemplate>();
		
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
				
				MixedRevisionTemplate template = new MixedRevisionTemplate(flip.getChildRevision());
				template.revertFiles(combination, flip.getParentRevision());
				templates.add(template);
			}
		}
		
		return templates;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		MixedRevisionTemplatesGenerator holder = (MixedRevisionTemplatesGenerator) other;

		return mixedRevisionTemplates.equals(holder.mixedRevisionTemplates);
	}

	@Override
	public int hashCode() {
		return mixedRevisionTemplates.hashCode();
	}
	
	@Override
    public String toString() {
        String str = "";
        
        for (MixedRevisionTemplate template : mixedRevisionTemplates) {
        	str += template + "\n";
        }
        
        return str;
    }
}