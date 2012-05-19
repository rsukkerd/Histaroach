package histaroach.model;

import static org.junit.Assert.assertEquals;

import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.MyBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class RevisionTest {

	private static final String DEST_PATH = "test-data/";
	private static final String ANT_COMMAND = "ant";
	
	/****************/
	/** in project **/
	/****************/
	private static final String PROJ = DEST_PATH + "project";
	private static final String PROJ_TAR_FILE = DEST_PATH + "project.tar";
	
	private static final File PROJ_DIR = new File(PROJ);
	
	private static final IBuildStrategy PROJ_BUILD_STARTEGY = new MyBuildStrategy(PROJ_DIR, 
			ANT_COMMAND);
	
	private static final IRepository REPOSITORY_PROJ = new GitRepository(PROJ_DIR, PROJ_BUILD_STARTEGY);
	
	private static final String COMMIT_1 = "8a75644";
	private static final String COMMIT_2 = "57451b4";
	private static final String COMMIT_3 = "5ee21b8";
	private static final String COMMIT_4 = "bbd79d7";
	
	private static final DiffFile DIFF_FILE_BUILD = new DiffFile(DiffType.ADDED, 
			"build.xml");
	private static final DiffFile DIFF_FILE_MAIN = new DiffFile(DiffType.MODIFIED, 
			"src/proj/Main.java");
	private static final DiffFile DIFF_FILE_TEST = new DiffFile(DiffType.MODIFIED, 
			"src/proj/tests/Main2Test.java");
	
	private static final Set<DiffFile> DIFF_FILES_2 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_2.add(DIFF_FILE_BUILD);
		DIFF_FILES_2.add(DIFF_FILE_MAIN);
	}
	private static final Set<DiffFile> DIFF_FILES_3 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_3.add(DIFF_FILE_MAIN);
	}
	private static final Set<DiffFile> DIFF_FILES_4 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_4.add(DIFF_FILE_TEST);
	}
	
	private static final Set<String> ALL_TESTS = new HashSet<String>();
	static {
		ALL_TESTS.add("proj.tests.Main1Test");
		ALL_TESTS.add("proj.tests.Main2Test");
		ALL_TESTS.add("proj.tests.Main3Test");
	}
	
	private static final Set<String> FAILED_TESTS_3 = new HashSet<String>();
	static {
		FAILED_TESTS_3.add("proj.tests.Main3Test");
	}
	
	private static final Set<String> FAILED_TESTS_4 = new HashSet<String>();
	static {
		FAILED_TESTS_4.add("proj.tests.Main2Test");
		FAILED_TESTS_4.add("proj.tests.Main3Test");
	}
	
	private static final TestResult TEST_RESULT_3 = new TestResult(ALL_TESTS, FAILED_TESTS_3);
	private static final TestResult TEST_RESULT_4 = new TestResult(ALL_TESTS, FAILED_TESTS_4);
	
	/*
	 * revision 1 in proj
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_DIFF_FILES_1 = 
		new HashMap<Revision, Set<DiffFile>>();
	private static final Revision REVISION_1 = new Revision(COMMIT_1, PARENT_DIFF_FILES_1, 
			Compilable.NO_BUILD_FILE, false, null);
	
	/*
	 * revision 2 in proj
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_DIFF_FILES_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_DIFF_FILES_2.put(REVISION_1, DIFF_FILES_2);
	}
	private static final Revision REVISION_2 = new Revision(COMMIT_2, PARENT_DIFF_FILES_2, 
			Compilable.NO, false, null);
	
	/*
	 * revision 3 in proj
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_DIFF_FILES_3 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_DIFF_FILES_3.put(REVISION_2, DIFF_FILES_3);
	}
	private static final Revision REVISION_3 = new Revision(COMMIT_3, PARENT_DIFF_FILES_3, 
			Compilable.YES, false, TEST_RESULT_3);
	
	/*
	 * revision 4 in proj
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_DIFF_FILES_4 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_DIFF_FILES_4.put(REVISION_3, DIFF_FILES_4);
	}
	private static final Revision REVISION_4 = new Revision(COMMIT_4, PARENT_DIFF_FILES_4, 
			Compilable.YES, false, TEST_RESULT_4);
	
	/*
	 * revision 5 in proj
	 */
	private static final HistoryGraph EXPECTED_HGRAPH_PROJ = new HistoryGraph();
	static {
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_1);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_2);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_3);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_4);
	}
	
	@Before
	public void setUp() throws FileNotFoundException, IOException {
		Util.untar(PROJ_TAR_FILE, DEST_PATH);
	}
	
	@After
	public void tearDown() throws IOException {
		FileUtils.deleteDirectory(PROJ_DIR);
	}
	
	@Test
	public void testCreateRevision1() throws Exception {
		checkRevision(REVISION_1, COMMIT_1, PARENT_DIFF_FILES_1);
	}
	
	@Test
	public void testCreateRevision2() throws Exception {
		checkRevision(REVISION_2, COMMIT_2, PARENT_DIFF_FILES_2);
	}
	
	@Test
	public void testCreateRevision3() throws Exception {
		checkRevision(REVISION_3, COMMIT_3, PARENT_DIFF_FILES_3);
	}
	
	@Test
	public void testCreateRevision4() throws Exception {
		checkRevision(REVISION_4, COMMIT_4, PARENT_DIFF_FILES_4);
	}
	
	private void checkRevision(Revision expectedRevision, String commitID, 
			Map<Revision, Set<DiffFile>> parentToDiffFiles) throws Exception {
		Revision actualRevision = new Revision(REPOSITORY_PROJ, commitID, 
				parentToDiffFiles);
		
		assertEquals(expectedRevision, actualRevision);
	}
}
