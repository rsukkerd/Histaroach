package histaroach.model;

import java.io.Serializable;


/**
 * DiffFile represents a file that is different between 2 Revisions. 
 * 
 * DiffFile is immutable.
 */
public class DiffFile implements Serializable {
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 491368705717578455L;

	public enum DiffType {
		ADDED,
		MODIFIED,
		DELETED
	}
	
	private final DiffType type;
	private final String fileName;
	
	/**
	 * Creates a DiffFile.
	 */
	public DiffFile(DiffType type, String fileName) {
		this.type = type;
		this.fileName = fileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public DiffType getDiffType() {
		return type;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null || !other.getClass().equals(this.getClass())) {
			return false;
		}
		
		DiffFile diffFile = (DiffFile) other;
		
		return type == diffFile.type && fileName.equals(diffFile.fileName);
	}
	
	@Override
	public int hashCode() {
		return 11 * type.hashCode() + 13 * fileName.hashCode();
	}
	
	@Override
	public String toString() {
		return type.toString().charAt(0) + "\t" + fileName;
	}
}
