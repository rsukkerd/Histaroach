package histaroach.model;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.MyBuildStrategy;
import histaroach.model.DiffFile;
import histaroach.model.GitRepository;
import histaroach.model.IntermediateRevision;
import histaroach.model.IRepository;
import histaroach.model.Revision;
import histaroach.model.TestResult;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class IntermediateRevisionTest {
	
	private static final String DEST_PATH = "test-data/";
	private static final String ANT_COMMAND = "ant";
	
	/********************/
	/** in delta_files **/
	/********************/
	private static final String TAR_FILE = DEST_PATH + "delta_files.tar";
	private static final String TAR_FILE_CLONE = DEST_PATH + "delta_files_clone.tar";
	
	private static final String PATHNAME = DEST_PATH + "delta_files";
	private static final String PATHNAME_CLONE = DEST_PATH + "delta_files_clone";
	
	private static final File DIR = new File(PATHNAME);
	private static final File DIR_CLONE = new File(PATHNAME_CLONE);
	
	private static final IBuildStrategy BUILD_STRATEGY = new MyBuildStrategy(DIR, ANT_COMMAND);
	private static final IBuildStrategy BUILD_STRATEGY_CLONE = new MyBuildStrategy(DIR_CLONE, ANT_COMMAND);
	
	private static final IRepository REPOSITORY = new GitRepository(DIR, BUILD_STRATEGY);
	private static final IRepository REPOSITORY_CLONE = new GitRepository(DIR_CLONE, BUILD_STRATEGY_CLONE);
	
	private static final String FILENAME_1 = "f1";
	private static final String FILENAME_2 = "f2";
	private static final String FILENAME_3 = "f3";
	
	private static final File FILE_1 = new File(PATHNAME + File.separatorChar + FILENAME_1);
	private static final File FILE_2 = new File(PATHNAME + File.separatorChar + FILENAME_2);
	private static final File FILE_3 = new File(PATHNAME + File.separatorChar + FILENAME_3);
	
	private static final DiffFile DIFF_FILE_1 = new DiffFile(DiffType.MODIFIED, FILENAME_1);
	private static final DiffFile DIFF_FILE_2 = new DiffFile(DiffType.DELETED, FILENAME_2);
	private static final DiffFile DIFF_FILE_3 = new DiffFile(DiffType.ADDED, FILENAME_3);
	
	private static final Set<DiffFile> DIFF_FILES = new HashSet<DiffFile>();
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
	
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1 = 
		new HashMap<Revision, Set<DiffFile>>();
	
	private static final Revision REVISION_1 = 
		new Revision(COMMIT_1, PARENT_TO_DIFF_FILES_1, Compilable.UNKNOWN, false, null);
	
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2.put(REVISION_1, DIFF_FILES);
	}
	
	private static final Revision REVISION_2 = 
		new Revision(COMMIT_2, PARENT_TO_DIFF_FILES_2, Compilable.UNKNOWN, false, null);
	
	private static final String FILE_1_CONTENT_VERSION_1 = "f1r1";
	private static final String FILE_1_CONTENT_VERSION_2 = "f1r2";
	
	@Test
	public void testApplyDeltaRestoreBase() throws Exception {
		Util.untar(TAR_FILE, DEST_PATH);
		Util.untar(TAR_FILE_CLONE, DEST_PATH);
		
		IntermediateRevision imr = new IntermediateRevision(REVISION_1, REVISION_2, 
				REPOSITORY, REPOSITORY_CLONE);
		imr.checkoutBaseSuccessorRevisions();
		imr.setDelta(COMBINATION);
		imr.applyDelta();
		
		checkFile(FILE_1, FILE_1_CONTENT_VERSION_2);
		assertFalse(FILENAME_2 + " exists", FILE_2.exists());
		assertTrue(FILENAME_3 + " does not exist", FILE_3.exists());
		
		imr.restoreBaseRevision();
		
		checkFile(FILE_1, FILE_1_CONTENT_VERSION_1);
		assertTrue(FILENAME_2 + " does not exist", FILE_2.exists());
		assertFalse(FILENAME_3 + " exists", FILE_3.exists());
		
		FileUtils.deleteDirectory(DIR);
		FileUtils.deleteDirectory(DIR_CLONE);
	}

	/**
	 * Checks if the file exists and contains the content.
	 * 
	 * @throws IOException 
	 */
	private void checkFile(File file, String content) throws IOException {
		assertTrue(file.getName() + " does not exist", file.exists());
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = reader.readLine();
		
		assertEquals("content of " + file.getName() + " is not as expected", content, line);
	}
	
	/**********************/
	/** in delta_project **/
	/**********************/
	
	private static final String PRJ_TAR_FILE = DEST_PATH + "delta_project.tar";
	private static final String PRJ_TAR_FILE_CLONE = DEST_PATH + "delta_project_clone.tar";
	
	private static final String PRJ_PATHNAME = DEST_PATH + "delta_project";
	private static final String PRJ_PATHNAME_CLONE = DEST_PATH + "delta_project_clone";
	
	private static final File PRJ = new File(PRJ_PATHNAME);
	private static final File PRJ_CLONE = new File(PRJ_PATHNAME_CLONE);
	
	private static final IBuildStrategy PRJ_BUILD_STRATEGY = new MyBuildStrategy(
			PRJ, ANT_COMMAND);
	private static final IBuildStrategy PRJ_BUILD_STRATEGY_CLONE = new MyBuildStrategy(
			PRJ_CLONE, ANT_COMMAND);
	
	private static final IRepository PRJ_REPOSITORY = new GitRepository(PRJ, PRJ_BUILD_STRATEGY);
	private static final IRepository PRJ_REPOSITORY_CLONE = new GitRepository(PRJ_CLONE, 
			PRJ_BUILD_STRATEGY_CLONE);
	
	private static final DiffFile PRJ_DIFF_FILE_1 = new DiffFile(DiffType.MODIFIED, "src/proj/F1.java");
	private static final DiffFile PRJ_DIFF_FILE_2 = new DiffFile(DiffType.MODIFIED, "src/proj/F2.java");
	private static final DiffFile PRJ_DIFF_FILE_3 = new DiffFile(DiffType.MODIFIED, "src/proj/F3.java");
	
	private static final Set<DiffFile> PRJ_DIFF_FILES = new HashSet<DiffFile>();
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
	
	private static final TestResult PRJ_TEST_RESULT_1 = new TestResult(TESTS, new HashSet<String>());
	private static final TestResult PRJ_TEST_RESULT_2 = new TestResult(TESTS, TESTS);
	private static final TestResult COMBINATION_2_TEST_RESULT = new TestResult(TESTS, TESTS);
	private static final TestResult COMBINATION_3_TEST_RESULT = new TestResult(TESTS, new HashSet<String>());
	
	private static final Map<Revision, Set<DiffFile>> PRJ_PARENT_TO_DIFF_FILES_1 = 
		new HashMap<Revision, Set<DiffFile>>();

	private static final Revision PRJ_REVISION_1 = 
		new Revision(PRJ_COMMIT_1, PRJ_PARENT_TO_DIFF_FILES_1, 
				Compilable.YES, false, PRJ_TEST_RESULT_1);
	
	private static final Map<Revision, Set<DiffFile>> PRJ_PARENT_TO_DIFF_FILES_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PRJ_PARENT_TO_DIFF_FILES_2.put(PRJ_REVISION_1, PRJ_DIFF_FILES);
	}
	
	private static final Revision PRJ_REVISION_2 = 
		new Revision(PRJ_COMMIT_2, PRJ_PARENT_TO_DIFF_FILES_2, 
				Compilable.YES, false, PRJ_TEST_RESULT_2);
	
	// not compilable
	private static final Set<DiffFile> COMBINATION_1 = new HashSet<DiffFile>();
	static {
		COMBINATION_1.add(PRJ_DIFF_FILE_2);
		COMBINATION_1.add(PRJ_DIFF_FILE_3);
	}
	
	// compilable, but still fail test
	private static final Set<DiffFile> COMBINATION_2 = new HashSet<DiffFile>();
	static {
		COMBINATION_2.add(PRJ_DIFF_FILE_3);
	}
	
	// pass test
	private static final Set<DiffFile> COMBINATION_3 = new HashSet<DiffFile>();
	static {
		COMBINATION_3.add(PRJ_DIFF_FILE_1);
		COMBINATION_3.add(PRJ_DIFF_FILE_2);
	}
	
	@Before
	public void setUp() throws FileNotFoundException, IOException {
		Util.untar(PRJ_TAR_FILE, DEST_PATH);
		Util.untar(PRJ_TAR_FILE_CLONE, DEST_PATH);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(PRJ);
		FileUtils.deleteDirectory(PRJ_CLONE);
	}
	
	@Test
	public void testApplyDeltaNotCompilable() throws Exception {
		checkTestResult(COMBINATION_1, Compilable.NO, null);
	}
	
	@Test
	public void testApplyDeltaFail() throws Exception {
		checkTestResult(COMBINATION_2, Compilable.YES, COMBINATION_2_TEST_RESULT);
	}
	
	@Test
	public void testApplyDeltaPass() throws Exception {
		checkTestResult(COMBINATION_3, Compilable.YES, COMBINATION_3_TEST_RESULT);
	}
	
	/**
	 * Checks if the test result after applying delta is as expected.
	 * 
	 * @throws Exception
	 */
	private void checkTestResult(Set<DiffFile> combination, Compilable expectedCompilable, 
			TestResult expectedTestResult) throws Exception {
		IntermediateRevision imr = new IntermediateRevision(PRJ_REVISION_1, PRJ_REVISION_2, 
				PRJ_REPOSITORY, PRJ_REPOSITORY_CLONE);
		
		imr.checkoutBaseSuccessorRevisions();
		imr.setDelta(combination);
		imr.applyDelta();
		imr.runTest();
			
		assertTrue(imr.isCompilable() == expectedCompilable);
		assertEquals(expectedTestResult, imr.getTestResult());
		
		imr.restoreBaseRevision();
	}
}
