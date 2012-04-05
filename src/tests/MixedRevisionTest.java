package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import git.GitRepository;

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

import ant.VoldemortBuildStrategy;

import common.BuildStrategy;
import common.DiffFile;
import common.DiffFile.DiffType;
import common.MixedRevision;
import common.Repository;
import common.Revision;
import common.Util;
import common.Revision.COMPILABLE;
import common.TestResult;

public class MixedRevisionTest {
	
	private static final String ANT_COMMAND = "ant";
	
	/********************/
	/** in delta_files **/
	/********************/
	private static final String TAR_FILE = "test/delta_files.tar";
	private static final String TAR_FILE_CLONE = "test/delta_files_clone.tar";
	
	private static final String PATHNAME = "test/delta_files";
	private static final String PATHNAME_CLONE = "test/delta_files_clone";
	
	private static final File DIR = new File(PATHNAME);
	private static final File DIR_CLONE = new File(PATHNAME_CLONE);
	
	private static final BuildStrategy BUILD_STRATEGY = new VoldemortBuildStrategy(DIR, ANT_COMMAND);
	private static final BuildStrategy BUILD_STRATEGY_CLONE = new VoldemortBuildStrategy(DIR_CLONE, ANT_COMMAND);
	
	private static final Repository REPOSITORY = new GitRepository(DIR, BUILD_STRATEGY);
	private static final Repository REPOSITORY_CLONE = new GitRepository(DIR_CLONE, BUILD_STRATEGY_CLONE);
	
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
	
	private static final Set<DiffFile> COMBINATION = new HashSet<DiffFile>();
	static {
		COMBINATION.addAll(DIFF_FILES);
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
	public void testRevertRestoreFiles() throws Exception {
		assertTrue(untar(TAR_FILE));
		assertTrue(untar(TAR_FILE_CLONE));
		
		MixedRevision mr = new MixedRevision(REVISION_2, REPOSITORY, REPOSITORY_CLONE);
		mr.revertFiles(COMBINATION, REVISION_1);
		
		checkFile(FILE_1, FILE_1_REVISION_1);
		assertTrue(FILENAME_2 + " does not exist", FILE_2.exists());
		assertFalse(FILENAME_3 + " exists", FILE_3.exists());
		
		mr.restoreBaseRevision();
		
		checkFile(FILE_1, FILE_1_REVISION_2);
		assertTrue(FILENAME_3 + " does not exist", FILE_3.exists());
		assertFalse(FILENAME_2 + " exists", FILE_2.exists());
		
		assertTrue(deleteDirectory(DIR));
		assertTrue(deleteDirectory(DIR_CLONE));
	}

	/**
	 * Helper method 
	 * Check if the file exists and contains the content
	 * @throws IOException 
	 */
	public void checkFile(File file, String content) throws IOException {
		assertTrue(file.getName() + " does not exist", file.exists());
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		
		assertEquals("content of " + file.getName() + " is not as expected", content, line);
	}
	
	/**********************/
	/** in delta_project **/
	/**********************/
	private static final String DEST_PATH = "test";
	
	private static final String PRJ_TAR_FILE = "test/delta_project.tar";
	private static final String PRJ_TAR_FILE_CLONE = "test/delta_project_clone.tar";
	
	private static final String PRJ_PATHNAME = "test/delta_project";
	private static final String PRJ_PATHNAME_CLONE = "test/delta_project_clone";
	
	private static final File PRJ = new File(PRJ_PATHNAME);
	private static final File PRJ_CLONE = new File(PRJ_PATHNAME_CLONE);
	
	private static final BuildStrategy PRJ_BUILD_STRATEGY = new VoldemortBuildStrategy(
			PRJ, ANT_COMMAND);
	private static final BuildStrategy PRJ_BUILD_STRATEGY_CLONE = new VoldemortBuildStrategy(
			PRJ_CLONE, ANT_COMMAND);
	
	private static final Repository PRJ_REPOSITORY = new GitRepository(PRJ, PRJ_BUILD_STRATEGY);
	private static final Repository PRJ_REPOSITORY_CLONE = new GitRepository(PRJ_CLONE, 
			PRJ_BUILD_STRATEGY_CLONE);
	
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
	private static final Set<DiffFile> COMBINATION_1 = new HashSet<DiffFile>();
	static {
		COMBINATION_1.add(PRJ_DIFF_FILE_1);
	}
	
	// compilable, but still fail test
	private static final Set<DiffFile> COMBINATION_2 = new HashSet<DiffFile>();
	static {
		COMBINATION_2.add(PRJ_DIFF_FILE_1);
		COMBINATION_2.add(PRJ_DIFF_FILE_2);
	}
	
	// pass test
	private static final Set<DiffFile> COMBINATION_3 = new HashSet<DiffFile>();
	static {
		COMBINATION_3.add(PRJ_DIFF_FILE_3);
	}
	
	@Test
	public void testRevertNotCompilable() throws Exception {
		checkTestResult(COMBINATION_1, COMPILABLE.NO, null);
	}
	
	@Test
	public void testRevertFail() throws Exception {
		checkTestResult(COMBINATION_2, COMPILABLE.YES, COMBINATION_2_TEST_RESULT);
	}
	
	@Test
	public void testRevertPass() throws Exception {
		checkTestResult(COMBINATION_3, COMPILABLE.YES, COMBINATION_3_TEST_RESULT);
	}
	
	public void checkTestResult(Set<DiffFile> combination, COMPILABLE expectedCompilable, 
			TestResult expectedTestResult) throws Exception {
		assertTrue(untar(PRJ_TAR_FILE));
		assertTrue(untar(PRJ_TAR_FILE_CLONE));
		
		MixedRevision mr = new MixedRevision(PRJ_REVISION_2, PRJ_REPOSITORY, PRJ_REPOSITORY_CLONE);
		
		mr.revertFiles(combination, PRJ_REVISION_1);
		mr.runTest();
			
		assertTrue(mr.isCompilable() == expectedCompilable);
		assertEquals(expectedTestResult, mr.getTestResult());
		
		mr.restoreBaseRevision();
		
		assertTrue(deleteDirectory(PRJ));
		assertTrue(deleteDirectory(PRJ_CLONE));
	}
	
	private static boolean untar(String tarFile) {
		try {
			Util.untar(tarFile, DEST_PATH);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	private static boolean deleteDirectory(File dir) {
		try {
			FileUtils.deleteDirectory(dir);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
