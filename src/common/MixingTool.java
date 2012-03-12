package common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import common.DiffFile.DiffType;
import common.Revision.COMPILABLE;

public class MixingTool {
	private final List<Flip> sortedFlips;
	private final List<MixedRevision> compilableMixedRevisions;
	
	public MixingTool(List<Flip> flips) {
		sortedFlips = flips;
		Collections.sort(sortedFlips);
		
		compilableMixedRevisions = new ArrayList<MixedRevision>();
	}
	
	public void mixParentToChild(Flip flip) throws IOException {
		Revision child = flip.getChildRevision();
		Revision parent = flip.getParentRevision();
		List<DiffFile> diffFiles = flip.getDiffFiles();
		
		MixedRevision mixedRevision = new MixedRevision(child);
		
		for (DiffFile diffFile : diffFiles) {
			
			if (diffFile.getDiffType() == DiffType.MODIFIED || 
					diffFile.getDiffType() == DiffType.DELETED) {
				mixedRevision.mixIn(diffFile, parent);
			} else {
				mixedRevision.mixOut(diffFile);
			}
			
			mixedRevision.compile();
			
			if (mixedRevision.isCompilable() == COMPILABLE.YES) {
				MixedRevision mr = mixedRevision.export();
				compilableMixedRevisions.add(mr);
				
				mixedRevision.restoreBaseRevision(diffFile);
			}
		}
		
		mixedRevision.restoreBaseRevsision();
	}
}
