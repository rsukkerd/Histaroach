package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import voldemort.VoldemortTestParsingStrategy;

import common.DiffFile;
import common.DiffFile.DiffType;
import common.MixedRevision;
import common.Repository;
import common.Revision;
import common.Util;
import common.Revision.COMPILABLE;
import common.TestParsingStrategy;
import common.TestResult;


public class MixedRevisionTest {
	/********************/
	/** in delta_files **/
	/********************/
	private static final String TAR_FILE = "test/delta_files.tar";
	private static final String TAR_FILE_CLONE = "test/delta_files_clone.tar";
	
	private static final String PATHNAME = "test/delta_files";
	private static final String PATHNAME_CLONE = "test/delta_files_clone";
	
	private static final TestParsingStrategy STRATEGY = new VoldemortTestParsingStrategy();
	private static final Repository REPOSITORY = new Repository(PATHNAME, "ant", STRATEGY);
	
	private static final String FILENAME_1 = "f1";
	private static final String FILENAME_2 = "f2";
	private static final String FILENAME_3 = "f3";
	
	private static final File FILE_1 = new File(PATHNAME + File.separatorChar + FILENAME_1);
	private static final File FILE_2 = new File(PATHNAME + File.separatorChar + FILENAME_2);
	private static final File FILE_3 = new File(PATHNAME + File.separatorChar + FILENAME_3);
	
	private static final DiffFile DIFF_FILE_1 = new DiffFile(DiffType.MODIFIED, FILENAME_1);
	private static final DiffFile DIFF_FILE_2 = new DiffFile(DiffType.DELETED, FILENAME_2);
	private static final DiffFile DIFF_FILE_3 = new DiffFile(DiffType.ADDED, FILENAME_3);
	
	private static final List<DiffFile> DIFF_FILES = new ArrayList<DiffFile>();
	static {
		DIFF_FILES.add(DIFF_FILE_1);
		DIFF_FILES.add(DIFF_FILE_2);
		DIFF_FILES.add(DIFF_FILE_3);
	}
	
	private static final String COMMIT_1 = "a3e38d0";
	private static final String COMMIT_2 = "1e7bdee";
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1 
		= new HashMap<Revision, List<DiffFile>>();
	
	private static final Revision REVISION_1 
		= new Revision(REPOSITORY, COMMIT_1, PARENT_TO_DIFF_FILES_1, COMPILABLE.UNKNOWN, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2 
		= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2.put(REVISION_1, DIFF_FILES);
	}
	
	private static final Revision REVISION_2 
		= new Revision(REPOSITORY, COMMIT_2, PARENT_TO_DIFF_FILES_2, COMPILABLE.UNKNOWN, null);
	
	private static final String FILE_1_REVISION_1 = "f1r1";
	private static final String FILE_1_REVISION_2 = "f1r2";
	
	@Test
	public void testRevertRestoreFiles() {
		assertTrue(untar(TAR_FILE));
		assertTrue(untar(TAR_FILE_CLONE));
		
		MixedRevision mr = new MixedRevision(REVISION_2, PATHNAME_CLONE);
		
		try {
			mr.revertFiles(DIFF_FILES, REVISION_1);
		} catch (IOException e) {
			fail("revertFiles throws exception");
			e.printStackTrace();
		}
		
		checkFile(FILE_1, FILE_1_REVISION_1);
		assertTrue(FILENAME_2 + " does not exist", FILE_2.exists());
		assertFalse(FILENAME_3 + " exists", FILE_3.exists());
		
		try {
			mr.restoreBaseRevision();
		} catch (IOException e) {
			fail("restoreBaseRevision throws exception");
			e.printStackTrace();
		}
		
		checkFile(FILE_1, FILE_1_REVISION_2);
		assertTrue(FILENAME_3 + " does not exist", FILE_3.exists());
		assertFalse(FILENAME_2 + " exists", FILE_2.exists());
		
		assertTrue(deleteDirectory(PATHNAME));
		assertTrue(deleteDirectory(PATHNAME_CLONE));
	}

	/**
	 * Helper method 
	 * Check if the file exists and contains the content
	 */
	public void checkFile(File file, String content) {
		assertTrue(file.getName() + " does not exist", file.exists());
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			assertEquals("content of " + file.getName() + " is not as expected", content, line);
		} catch (FileNotFoundException e) {
			fail(file.getName() + " not found");
			e.printStackTrace();
		} catch (IOException e) {
			fail(file.getName() + "'s reader throws IOException");
			e.printStackTrace();
		}
	}
	
	/**********************/
	/** in delta_project **/
	/**********************/
	private static final String DEST_DIR = "test";
	
	private static final String PRJ_TAR_FILE = "test/delta_project.tar";
	private static final String PRJ_TAR_FILE_CLONE = "test/delta_project_clone.tar";
	
	private static final String PRJ_PATHNAME = "test/delta_project";
	private static final String PRJ_PATHNAME_CLONE = "test/delta_project_clone";
	
	private static final Repository PRJ_REPOSITORY = new Repository(PRJ_PATHNAME, "ant", STRATEGY);
	
	private static final DiffFile PRJ_DIFF_FILE_1 = new DiffFile(DiffType.MODIFIED, "src/proj/F1.java");
	private static final DiffFile PRJ_DIFF_FILE_2 = new DiffFile(DiffType.MODIFIED, "src/proj/F2.java");
	private static final DiffFile PRJ_DIFF_FILE_3 = new DiffFile(DiffType.MODIFIED, "src/proj/F3.java");
	
	private static final List<DiffFile> PRJ_DIFF_FILES = new ArrayList<DiffFile>();
	static {
		PRJ_DIFF_FILES.add(PRJ_DIFF_FILE_1);
		PRJ_DIFF_FILES.add(PRJ_DIFF_FILE_2);
		PRJ_DIFF_FILES.add(PRJ_DIFF_FILE_3);
	}
	
	private static final String PRJ_COMMIT_1 = "860c425";
	private static final String PRJ_COMMIT_2 = "fcfde77";
	
	private static final Set<String> TESTS = new HashSet<String>();
	static {
		TESTS.add("proj.tests.FTest");
	}
	
	private static final TestResult PRJ_TEST_RESULT_1 = new TestResult(PRJ_COMMIT_1, 
			TESTS, new HashSet<String>());
	private static final TestResult PRJ_TEST_RESULT_2 = new TestResult(PRJ_COMMIT_2, 
			TESTS, TESTS);
	private static final TestResult COMBINATION_2_TEST_RESULT = new TestResult(PRJ_COMMIT_2, 
			TESTS, TESTS);
	private static final TestResult COMBINATION_3_TEST_RESULT = new TestResult(PRJ_COMMIT_2, 
			TESTS, new HashSet<String>());
	
	private static final Map<Revision, List<DiffFile>> PRJ_PARENT_TO_DIFF_FILES_1 
		= new HashMap<Revision, List<DiffFile>>();

	private static final Revision PRJ_REVISION_1 
		= new Revision(PRJ_REPOSITORY, PRJ_COMMIT_1, PRJ_PARENT_TO_DIFF_FILES_1, 
				COMPILABLE.YES, PRJ_TEST_RESULT_1);
	
	private static final Map<Revision, List<DiffFile>> PRJ_PARENT_TO_DIFF_FILES_2 
		= new HashMap<Revision, List<DiffFile>>();
	static {
		PRJ_PARENT_TO_DIFF_FILES_2.put(PRJ_REVISION_1, PRJ_DIFF_FILES);
	}
	
	private static final Revision PRJ_REVISION_2 
		= new Revision(PRJ_REPOSITORY, PRJ_COMMIT_2, PRJ_PARENT_TO_DIFF_FILES_2, 
				COMPILABLE.YES, PRJ_TEST_RESULT_2);
	
	// not compilable
	private static final List<DiffFile> COMBINATION_1 = new ArrayList<DiffFile>();
	static {
		COMBINATION_1.add(PRJ_DIFF_FILE_1);
	}
	
	// compilable, but still fail test
	private static final List<DiffFile> COMBINATION_2 = new ArrayList<DiffFile>();
	static {
		COMBINATION_2.add(PRJ_DIFF_FILE_1);
		COMBINATION_2.add(PRJ_DIFF_FILE_2);
	}
	
	// pass test
	private static final List<DiffFile> COMBINATION_3 = new ArrayList<DiffFile>();
	static {
		COMBINATION_3.add(PRJ_DIFF_FILE_3);
	}
	
	@Test
	public void testRevertNotCompilable() {
		checkTestResult(COMBINATION_1, COMPILABLE.NO, null);
	}
	
	@Test
	public void testRevertFail() {
		checkTestResult(COMBINATION_2, COMPILABLE.YES, COMBINATION_2_TEST_RESULT);
	}
	
	@Test
	public void testRevertPass() {
		checkTestResult(COMBINATION_3, COMPILABLE.YES, COMBINATION_3_TEST_RESULT);
	}
	
	public void checkTestResult(List<DiffFile> combination, COMPILABLE expectedCompilable, 
			TestResult expectedTestResult) {
		assertTrue(untar(PRJ_TAR_FILE));
		assertTrue(untar(PRJ_TAR_FILE_CLONE));
		
		MixedRevision mr = new MixedRevision(PRJ_REVISION_2, PRJ_PATHNAME_CLONE);
		
		try {
			mr.revertFiles(combination, PRJ_REVISION_1);
			mr.compileAndRunAllTests();
			
			assertTrue(mr.isCompilable() == expectedCompilable);
			assertEquals(expectedTestResult, mr.getTestResult());
			
		} catch (IOException e) {
			fail("revertFiles throws exception");
			e.printStackTrace();
		}
		
		try {
			mr.restoreBaseRevision();
		} catch (IOException e) {
			fail("restoreBaseRevision throws exception");
			e.printStackTrace();
		}
		
		assertTrue(deleteDirectory(PRJ_PATHNAME));
		assertTrue(deleteDirectory(PRJ_PATHNAME_CLONE));
	}
	
	private static boolean untar(String tarFile) {
		try {
			Util.untar(tarFile, DEST_DIR);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean deleteDirectory(String dirpath) {
		File target = new File(dirpath);
		try {
			FileUtils.deleteDirectory(target);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
