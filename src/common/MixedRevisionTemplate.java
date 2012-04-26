package common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * MixedRevisionTemplate contains a baseRevision and pairs of 
 * (a set of reverted files, a Revision those files are reverted to). 
 * 
 * For a simple MixedRevision, there is only one such pair.
 */
public class MixedRevisionTemplate implements Serializable {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -3839917338367723327L;
	
	private Revision baseRevision;
	private Map<Revision, Set<DiffFile>> revertedFiles;
	
	public MixedRevisionTemplate(Revision baseRevision) {
		this.baseRevision = baseRevision;
		revertedFiles = new HashMap<Revision, Set<DiffFile>>();
	}
	
	public void revertFiles(Set<DiffFile> diffFiles, Revision otherRevision) {
		revertedFiles.put(otherRevision, diffFiles);
	}
	
	public Revision getBaseRevision() {
		return baseRevision;
	}
	
	public Map<Revision, Set<DiffFile>> getRevertedFiles() {
    	return revertedFiles;
    }
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}

		MixedRevisionTemplate template = (MixedRevisionTemplate) other;

		return baseRevision.equals(template.baseRevision) &&
				revertedFiles.equals(template.revertedFiles);
	}

	@Override
	public int hashCode() {
		return 13 * baseRevision.hashCode() + 17 * revertedFiles.hashCode();
	}
	
	@Override
    public String toString() {
        String str = "Base: " + baseRevision.getCommitID() + "\n";
        
        for (Revision otherRevision : revertedFiles.keySet()) {
        	str += "Reverted Files: \n";
        	Set<DiffFile> diffFiles = revertedFiles.get(otherRevision);
        	
        	for (DiffFile diffFile : diffFiles) {
        		str += diffFile + "\n";
        	}
        	
        	str += "To: " + otherRevision.getCommitID() + "\n";
        }
        
        return str;
    }
}
