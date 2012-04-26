package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import git.GitRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import util.Util;

import ant.AntBuildStrategy;
import ant.VoldemortBuildStrategy;

import common.BuildStrategy;
import common.DiffFile;
import common.DiffFile.DiffType;
import common.HistoryGraph;
import common.Repository;
import common.Revision;
import common.Revision.Compilable;
import common.TestResult;

public class RepositoryTest {
	private static final String ANT_COMMAND = "ant";
	private static final String DEST_PATH = "test";
	
	/****************************/
	/** in sample_repositories **/
	/****************************/
	private static final String SAMPLE_REPOSITORIES = "test/sample_repositories";
	private static final String SAMPLE_REPOSITORIES_TAR_FILE = "test/sample_repositories.tar";
	
	private static final String[] PATHS = { "test/sample_repositories/repo1", 
		"test/sample_repositories/repo2", 
		"test/sample_repositories/repo3", 
		"test/sample_repositories/repo4", 
		"test/sample_repositories/repo5"};
	
	private static final File[] DIRECTORIES = new File[5];
	static {
		for (int i = 0; i < PATHS.length; i++) {
			File dir = new File(PATHS[i]);
			DIRECTORIES[i] = dir;
		}
	}
	
	private static final BuildStrategy[] BUILD_STRATEGIES = new AntBuildStrategy[5];
	static {
		for (int i = 0; i < DIRECTORIES.length; i++) {
			BuildStrategy buildStrategy = new VoldemortBuildStrategy(DIRECTORIES[i], 
					ANT_COMMAND);
			BUILD_STRATEGIES[i] = buildStrategy;
		}
	}
    
	private static final List<DiffFile> DIFF_FILES = new ArrayList<DiffFile>();
	static {
		DiffFile diffFile = new DiffFile(DiffType.MODIFIED, "tmpfile");
		DIFF_FILES.add(diffFile);
	}

	/** commit ids in hGraph 1 **/
	private static final String COMMIT_1_1 = "8fa00ca";
	
	/** commit ids in hGraph 2 **/
	private static final String COMMIT_1_2 = "3e3a0ef";
	private static final String COMMIT_2_2 = "a667caa";
	
	/** commit ids in hGraph 3 **/
	private static final String COMMIT_1_3 = "e082fcd";
	private static final String COMMIT_2_3 = "43c4a2e";
	private static final String COMMIT_3_3 = "2512c4a";
	
	/** commit ids in hGraph 4 **/
	private static final String COMMIT_1_4 = "ce7fccb";
	private static final String COMMIT_2_4 = "50bb766";
	private static final String COMMIT_3_4 = "8d4a966";
	private static final String COMMIT_4_4 = "11f34d6";
	
	/** commit ids in hGraph 5 **/
	private static final String COMMIT_1_5 = "eab94e5";
	private static final String COMMIT_2_5 = "d863c56";
	private static final String COMMIT_3_5 = "7d72bc0";
	private static final String COMMIT_4_5 = "e14c9c6";
	private static final String COMMIT_5_5 = "35c25ec";
	private static final String COMMIT_6_5 = "e654cdb";
	
	/** revisions in full hGraphs **/
	/** revisions in hGraph 1 **/
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_1 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_1 = new Revision(COMMIT_1_1, PARENT_TO_DIFF_FILES_1_1, 
			Compilable.NO_BUILD_FILE, null);
	
	/** revisions in hGraph 2 **/
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_2 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_2 = new Revision(COMMIT_1_2, PARENT_TO_DIFF_FILES_1_2, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_2 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_2.put(REVISION_1_2, DIFF_FILES);
	}
	private static final Revision REVISION_2_2 = new Revision(COMMIT_2_2, PARENT_TO_DIFF_FILES_2_2, 
			Compilable.NO_BUILD_FILE, null);
	
	/** revisions in hGraph 3 **/
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_3 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_3 = new Revision(COMMIT_1_3, PARENT_TO_DIFF_FILES_1_3, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_3 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_3.put(REVISION_1_3, DIFF_FILES);
	}
	private static final Revision REVISION_2_3 = new Revision(COMMIT_2_3, PARENT_TO_DIFF_FILES_2_3, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_3_3 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_3.put(REVISION_2_3, DIFF_FILES);
	}
	private static final Revision REVISION_3_3 = new Revision(COMMIT_3_3, PARENT_TO_DIFF_FILES_3_3, 
			Compilable.NO_BUILD_FILE, null);
	
	/** revisions in hGraph 4 **/
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_4 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_4 = new Revision(COMMIT_1_4, PARENT_TO_DIFF_FILES_1_4, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_4 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_4.put(REVISION_1_4, DIFF_FILES);
	}
	private static final Revision REVISION_2_4 = new Revision(COMMIT_2_4, PARENT_TO_DIFF_FILES_2_4, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_3_4 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_4.put(REVISION_1_4, DIFF_FILES);
	}
	private static final Revision REVISION_3_4 = new Revision(COMMIT_3_4, PARENT_TO_DIFF_FILES_3_4, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_4_4 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_4.put(REVISION_2_4, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_4.put(REVISION_3_4, DIFF_FILES);
	}
	private static final Revision REVISION_4_4 = new Revision(COMMIT_4_4, PARENT_TO_DIFF_FILES_4_4, 
			Compilable.NO_BUILD_FILE, null);
	
	/** revisions in hGraph 5 **/
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_5 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_5 = new Revision(COMMIT_1_5, PARENT_TO_DIFF_FILES_1_5, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_5 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_5.put(REVISION_1_5, DIFF_FILES);
	}
	private static final Revision REVISION_2_5 = new Revision(COMMIT_2_5, PARENT_TO_DIFF_FILES_2_5, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_3_5 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_5.put(REVISION_1_5, DIFF_FILES);
	}
	private static final Revision REVISION_3_5 = new Revision(COMMIT_3_5, PARENT_TO_DIFF_FILES_3_5, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_4_5 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_5.put(REVISION_2_5, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_5.put(REVISION_3_5, DIFF_FILES);
	}
	private static final Revision REVISION_4_5 = new Revision(COMMIT_4_5, PARENT_TO_DIFF_FILES_4_5, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_5_5 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5.put(REVISION_1_5, DIFF_FILES);
	}
	private static final Revision REVISION_5_5 = new Revision(COMMIT_5_5, PARENT_TO_DIFF_FILES_5_5, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_6_5 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_6_5.put(REVISION_4_5, DIFF_FILES);
		PARENT_TO_DIFF_FILES_6_5.put(REVISION_5_5, DIFF_FILES);
	}
	private static final Revision REVISION_6_5 = new Revision(COMMIT_6_5, PARENT_TO_DIFF_FILES_6_5, 
			Compilable.NO_BUILD_FILE, null);
	/*******************************/
	
	/** revisions in partial hGraph 3 **/
	private static final Revision DUMMY_REVISION_1_3 = new Revision(COMMIT_1_3, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.UNKNOWN, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_3_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_3_PARTIAL.put(DUMMY_REVISION_1_3, DIFF_FILES);
	}
	private static final Revision REVISION_2_3_PARTIAL = new Revision(COMMIT_2_3, 
			PARENT_TO_DIFF_FILES_2_3_PARTIAL, Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_3_3_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_3_PARTIAL.put(REVISION_2_3_PARTIAL, DIFF_FILES);
	}
	private static final Revision REVISION_3_3_PARTIAL = new Revision(COMMIT_3_3, 
			PARENT_TO_DIFF_FILES_3_3_PARTIAL, Compilable.NO_BUILD_FILE, null);
	/***********************************/
	
	/** revisions in partial hGraph 4 **/
	private static final Revision DUMMY_REVISION_1_4 = new Revision(COMMIT_1_4, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.UNKNOWN, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_4_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_4_PARTIAL.put(DUMMY_REVISION_1_4, DIFF_FILES);
	}
	private static final Revision REVISION_2_4_PARTIAL = new Revision(COMMIT_2_4, 
			PARENT_TO_DIFF_FILES_2_4_PARTIAL, Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_3_4_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_4_PARTIAL.put(DUMMY_REVISION_1_4, DIFF_FILES);
	}
	private static final Revision REVISION_3_4_PARTIAL = new Revision(COMMIT_3_4, 
			PARENT_TO_DIFF_FILES_3_4_PARTIAL, Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_4_4_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_4_PARTIAL.put(REVISION_2_4_PARTIAL, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_4_PARTIAL.put(REVISION_3_4_PARTIAL, DIFF_FILES);
	}
	private static final Revision REVISION_4_4_PARTIAL = new Revision(COMMIT_4_4, 
			PARENT_TO_DIFF_FILES_4_4_PARTIAL, Compilable.NO_BUILD_FILE, null);
	/***********************************/
	
	/** revisions in partial hGraph 5 **/
	private static final Revision DUMMY_REVISION_1_5 = 
		new Revision(COMMIT_1_5, new HashMap<Revision, List<DiffFile>>(), Compilable.UNKNOWN, null);
	private static final Revision DUMMY_REVISION_2_5 = 
		new Revision(COMMIT_2_5, new HashMap<Revision, List<DiffFile>>(), Compilable.UNKNOWN, null);
	private static final Revision DUMMY_REVISION_3_5 = 
		new Revision(COMMIT_3_5, new HashMap<Revision, List<DiffFile>>(), Compilable.UNKNOWN, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_4_5_PARTIAL = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_5_PARTIAL.put(DUMMY_REVISION_2_5, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_5_PARTIAL.put(DUMMY_REVISION_3_5, DIFF_FILES);
	}
	private static final Revision REVISION_4_5_PARTIAL = new Revision(COMMIT_4_5, 
			PARENT_TO_DIFF_FILES_4_5_PARTIAL, Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_5_5_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5_PARTIAL.put(DUMMY_REVISION_1_5, DIFF_FILES);
	}
	private static final Revision REVISION_5_5_PARTIAL = new Revision(COMMIT_5_5, 
			PARENT_TO_DIFF_FILES_5_5_PARTIAL, Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_6_5_PARTIAL 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_6_5_PARTIAL.put(REVISION_4_5_PARTIAL, DIFF_FILES);
		PARENT_TO_DIFF_FILES_6_5_PARTIAL.put(REVISION_5_5_PARTIAL, DIFF_FILES);
	}
	private static final Revision REVISION_6_5_PARTIAL = new Revision(COMMIT_6_5, 
			PARENT_TO_DIFF_FILES_6_5_PARTIAL, Compilable.NO_BUILD_FILE, null);
	/***********************************/
		
	/** a set of revisions in a particular hGraph **/
	private static final Set<Revision> REVISIONS_1 = new HashSet<Revision>();
	static {
		REVISIONS_1.add(REVISION_1_1);
	}
	
	private static final Set<Revision> REVISIONS_2 = new HashSet<Revision>();
	static {
		REVISIONS_2.add(REVISION_2_2);
		REVISIONS_2.add(REVISION_1_2);
	}
	
	private static final Set<Revision> REVISIONS_3 = new HashSet<Revision>();
	static {
		REVISIONS_3.add(REVISION_3_3);
		REVISIONS_3.add(REVISION_2_3);
		REVISIONS_3.add(REVISION_1_3);
	}
	
	private static final Set<Revision> REVISIONS_3_PARTIAL = new HashSet<Revision>();
	static {
		REVISIONS_3_PARTIAL.add(REVISION_3_3_PARTIAL);
		REVISIONS_3_PARTIAL.add(REVISION_2_3_PARTIAL);
	}
	
	private static final Set<Revision> REVISIONS_4 = new HashSet<Revision>();
	static {
		REVISIONS_4.add(REVISION_4_4);
		REVISIONS_4.add(REVISION_3_4);
		REVISIONS_4.add(REVISION_2_4);
		REVISIONS_4.add(REVISION_1_4);
	}
	
	private static final Set<Revision> REVISIONS_4_PARTIAL = new HashSet<Revision>();
	static {
		REVISIONS_4_PARTIAL.add(REVISION_4_4_PARTIAL);
		REVISIONS_4_PARTIAL.add(REVISION_3_4_PARTIAL);
		REVISIONS_4_PARTIAL.add(REVISION_2_4_PARTIAL);
	}
	
	private static final Set<Revision> REVISIONS_5 = new HashSet<Revision>();
	static {
		REVISIONS_5.add(REVISION_6_5);
		REVISIONS_5.add(REVISION_5_5);
		REVISIONS_5.add(REVISION_4_5);
		REVISIONS_5.add(REVISION_3_5);
		REVISIONS_5.add(REVISION_2_5);
		REVISIONS_5.add(REVISION_1_5);
	}
	
	private static final Set<Revision> REVISIONS_5_PARTIAL = new HashSet<Revision>();
	static {
		REVISIONS_5_PARTIAL.add(REVISION_6_5_PARTIAL);
		REVISIONS_5_PARTIAL.add(REVISION_5_5_PARTIAL);
		REVISIONS_5_PARTIAL.add(REVISION_4_5_PARTIAL);
	}
	/***********************************************/
	
	private static final String[] START_COMMIT_IDS = {COMMIT_1_1, COMMIT_2_2, COMMIT_3_3, COMMIT_4_4, COMMIT_6_5};
	private static final String[] END_COMMIT_IDS = {COMMIT_1_1, COMMIT_1_2, COMMIT_1_3, COMMIT_1_4, COMMIT_1_5};
	private static final String[] END_COMMIT_IDS_PARTIAL = {COMMIT_1_1, COMMIT_1_2, COMMIT_2_3, COMMIT_2_4, COMMIT_4_5};
	
	private static final HistoryGraph HGRAPH_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_3 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_3_PARTIAL = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4_PARTIAL = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5_PARTIAL = new HistoryGraph();
	static {
		buildHistoryGraph(HGRAPH_1, REVISIONS_1);
		buildHistoryGraph(HGRAPH_2, REVISIONS_2);
		buildHistoryGraph(HGRAPH_3, REVISIONS_3);
		buildHistoryGraph(HGRAPH_3_PARTIAL, REVISIONS_3_PARTIAL);
		buildHistoryGraph(HGRAPH_4, REVISIONS_4);
		buildHistoryGraph(HGRAPH_4_PARTIAL, REVISIONS_4_PARTIAL);
		buildHistoryGraph(HGRAPH_5, REVISIONS_5);
		buildHistoryGraph(HGRAPH_5_PARTIAL, REVISIONS_5_PARTIAL);
	}
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS = {HGRAPH_1, HGRAPH_2, HGRAPH_3, HGRAPH_4, HGRAPH_5};
	private static final HistoryGraph[] EXPECTED_HGRAPHS_PARTIAL = {HGRAPH_1, HGRAPH_2, HGRAPH_3_PARTIAL, HGRAPH_4_PARTIAL, HGRAPH_5_PARTIAL};
	
	@Test
	public void testBuildFullHistoryGraph() throws Exception {
		Util.untar(SAMPLE_REPOSITORIES_TAR_FILE, DEST_PATH);
		
		for (int i = 0; i < DIRECTORIES.length; i++) {
			Repository repo = new GitRepository(DIRECTORIES[i], BUILD_STRATEGIES[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i], END_COMMIT_IDS[i]);
			
			assertNotNull("constructor returns null on " + DIRECTORIES[i], actualHGraph);
			assertEquals("result mismatched on " + DIRECTORIES[i], EXPECTED_HGRAPHS[i], actualHGraph);
		}
		
		FileUtils.deleteDirectory(new File(SAMPLE_REPOSITORIES));
	}
	
	@Test
	public void testBuildPartialHistoryGraph() throws Exception {
		Util.untar(SAMPLE_REPOSITORIES_TAR_FILE, DEST_PATH);
		
		for (int i = 2; i < DIRECTORIES.length; i++) {
			Repository repo = new GitRepository(DIRECTORIES[i], BUILD_STRATEGIES[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i], END_COMMIT_IDS_PARTIAL[i]);
			
			assertNotNull("constructor returns null on " + DIRECTORIES[i], actualHGraph);
			assertEquals("result mismatched on " + DIRECTORIES[i], EXPECTED_HGRAPHS_PARTIAL[i], actualHGraph);
		}
		
		FileUtils.deleteDirectory(new File(SAMPLE_REPOSITORIES));
	}
	
	/****************/
	/** in project **/
	/****************/
	private static final String PROJ = "test/project";
	private static final String PROJ_TAR_FILE = "test/project.tar";
	
	private static final File PROJ_DIR = new File(PROJ);
	
	private static final BuildStrategy PROJ_BUILD_STARTEGY = new VoldemortBuildStrategy(PROJ_DIR, 
			ANT_COMMAND);
	
	private static final Repository REPOSITORY_PROJ = new GitRepository(PROJ_DIR, PROJ_BUILD_STARTEGY);
	
	private static final String COMMIT_1 = "8a75644";
	private static final String COMMIT_2 = "57451b4";
	private static final String COMMIT_3 = "5ee21b8";
	private static final String COMMIT_4 = "bbd79d7";
	
	private static final DiffFile DIFF_FILE_BUILD = new DiffFile(DiffType.ADDED, "build.xml");
	private static final DiffFile DIFF_FILE_MAIN = new DiffFile(DiffType.MODIFIED, "src/proj/Main.java");
	private static final DiffFile DIFF_FILE_TEST = new DiffFile(DiffType.MODIFIED, "src/proj/tests/Main2Test.java");
	
	private static final List<DiffFile> DIFF_FILES_2 = new ArrayList<DiffFile>();
	static {
		DIFF_FILES_2.add(DIFF_FILE_BUILD);
		DIFF_FILES_2.add(DIFF_FILE_MAIN);
	}
	private static final List<DiffFile> DIFF_FILES_3 = new ArrayList<DiffFile>();
	static {
		DIFF_FILES_3.add(DIFF_FILE_MAIN);
	}
	private static final List<DiffFile> DIFF_FILES_4 = new ArrayList<DiffFile>();
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
	
	/** revision 1 in proj **/
	private static final Map<Revision, List<DiffFile>> PARENT_DIFF_FILES_1 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1 = new Revision(COMMIT_1, PARENT_DIFF_FILES_1, 
			Compilable.NO_BUILD_FILE, null);
	
	/** revision 2 in proj **/
	private static final Map<Revision, List<DiffFile>> PARENT_DIFF_FILES_2 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_DIFF_FILES_2.put(REVISION_1, DIFF_FILES_2);
	}
	private static final Revision REVISION_2 = new Revision(COMMIT_2, PARENT_DIFF_FILES_2, 
			Compilable.NO, null);
	
	/** revision 3 in proj **/
	private static final Map<Revision, List<DiffFile>> PARENT_DIFF_FILES_3 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_DIFF_FILES_3.put(REVISION_2, DIFF_FILES_3);
	}
	private static final Revision REVISION_3 = new Revision(COMMIT_3, PARENT_DIFF_FILES_3, 
			Compilable.YES, TEST_RESULT_3);
	
	/** revision 4 in proj **/
	private static final Map<Revision, List<DiffFile>> PARENT_DIFF_FILES_4 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_DIFF_FILES_4.put(REVISION_3, DIFF_FILES_4);
	}
	private static final Revision REVISION_4 = new Revision(COMMIT_4, PARENT_DIFF_FILES_4, 
			Compilable.YES, TEST_RESULT_4);
	
	/** hGraph of proj **/
	private static final HistoryGraph EXPECTED_HGRAPH_PROJ = new HistoryGraph();
	static {
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_1);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_2);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_3);
		EXPECTED_HGRAPH_PROJ.addRevision(REVISION_4);
	}
	
	private static final Set<Revision> PROJ_REVISIONS = new HashSet<Revision>();
	static {
		PROJ_REVISIONS.add(REVISION_1);
		PROJ_REVISIONS.add(REVISION_2);
		PROJ_REVISIONS.add(REVISION_3);
		PROJ_REVISIONS.add(REVISION_4);
	}
	
	@Test
	public void testRun() throws Exception {
		Util.untar(PROJ_TAR_FILE, DEST_PATH);
		
		HistoryGraph actualHGraph = null;
		actualHGraph = REPOSITORY_PROJ.buildHistoryGraph(COMMIT_4, COMMIT_1);
		
		assertNotNull("constructor returns null on " + PROJ, actualHGraph);
		assertEquals("result mismatched on " + PROJ, EXPECTED_HGRAPH_PROJ, actualHGraph);
		
		FileUtils.deleteDirectory(PROJ_DIR);
	}
	
	private static void buildHistoryGraph(HistoryGraph hGraph, Set<Revision> revisions) {
		for (Revision revision : revisions) {
			hGraph.addRevision(revision);
		}
	}
}
