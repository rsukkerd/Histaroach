package common;

import java.io.Serializable;

/**
 * DiffFile represents a file that is different between a child revision and its parent revision.
 * DiffFile contains a file name and a type of difference: Added, Modified, or Deleted.
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
	
	public DiffFile(DiffType type, String fileName) {
		this.type = type;
		this.fileName = fileName;
	}
	
	public DiffType getDiffType() {
		return type;
	}
	
	public String getFileName() {
		return fileName;
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
		String str;
		if (type == DiffType.ADDED) {
			str = "A";
		} else if (type == DiffType.MODIFIED) {
			str = "M";
		} else {
			str = "D";
		}
		
		str += "\t" + fileName;
		
		return str;
	}
}
