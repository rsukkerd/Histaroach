package histaroach.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import histaroach.buildstrategy.AntBuildStrategy;
import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.DiffFile;
import histaroach.model.GitRepository;
import histaroach.model.HistoryGraph;
import histaroach.model.IRepository;
import histaroach.model.Revision;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class RepositoryBuildHistoryGraphTest {
	
	private static final String DEST_PATH = "test-data/";
	private static final String ANT_COMMAND = "ant";
	
	/****************************/
	/** in sample_repositories **/
	/****************************/
	private static final String SAMPLE_REPOSITORIES = DEST_PATH + "sample_repositories";
	private static final String SAMPLE_REPOSITORIES_TAR = DEST_PATH + "sample_repositories.tar";
	
	private static final String[] REPO_PATHS = { DEST_PATH + "sample_repositories/repo1", 
		DEST_PATH + "sample_repositories/repo2", 
		DEST_PATH + "sample_repositories/repo3", 
		DEST_PATH + "sample_repositories/repo4", 
		DEST_PATH + "sample_repositories/repo5"};
	
	private static final File[] REPO_DIRS = new File[REPO_PATHS.length];
	static {
		for (int i = 0; i < REPO_PATHS.length; i++) {
			File dir = new File(REPO_PATHS[i]);
			REPO_DIRS[i] = dir;
		}
	}
	
	private static final IBuildStrategy[] BUILD_STRATEGIES = 
		new AntBuildStrategy[REPO_PATHS.length];
	static {
		for (int i = 0; i < REPO_DIRS.length; i++) {
			IBuildStrategy buildStrategy = new VoldemortBuildStrategy(REPO_DIRS[i], 
					ANT_COMMAND);
			BUILD_STRATEGIES[i] = buildStrategy;
		}
	}
	
	private static final File[] REPO_DIRS_PARTIAL = 
		{ REPO_DIRS[2], REPO_DIRS[3], REPO_DIRS[4] };
	
	private static final IBuildStrategy[] BUILD_STRATEGIES_PARTIAL = 
		{ BUILD_STRATEGIES[2], BUILD_STRATEGIES[3], BUILD_STRATEGIES[4] };
    
	private static final List<DiffFile> DIFF_FILES = new ArrayList<DiffFile>();
	static {
		DiffFile diffFile = new DiffFile(DiffType.MODIFIED, "tmpfile");
		DIFF_FILES.add(diffFile);
	}
	
	private static final String COMMIT_1_1 = "8fa00ca";
	
	private static final String COMMIT_1_2 = "3e3a0ef";
	private static final String COMMIT_2_2 = "a667caa";
	
	private static final String COMMIT_1_3 = "e082fcd";
	private static final String COMMIT_2_3 = "43c4a2e";
	private static final String COMMIT_3_3 = "2512c4a";
	
	private static final String COMMIT_1_4 = "ce7fccb";
	private static final String COMMIT_2_4 = "50bb766";
	private static final String COMMIT_3_4 = "8d4a966";
	private static final String COMMIT_4_4 = "11f34d6";
	
	private static final String COMMIT_1_5 = "eab94e5";
	private static final String COMMIT_2_5 = "d863c56";
	private static final String COMMIT_3_5 = "7d72bc0";
	private static final String COMMIT_4_5 = "e14c9c6";
	private static final String COMMIT_5_5 = "35c25ec";
	private static final String COMMIT_6_5 = "e654cdb";
	
	/*
	 * revision in hGraph 1
	 * (1)
	 */
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_1 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_1 = new Revision(COMMIT_1_1, PARENT_TO_DIFF_FILES_1_1, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in hGraph 2
	 * (2)
	 *  |
	 * (1)
	 */
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_1_2 = new HashMap<Revision, List<DiffFile>>();
	private static final Revision REVISION_1_2 = new Revision(COMMIT_1_2, PARENT_TO_DIFF_FILES_1_2, 
			Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_2_2 = new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_2.put(REVISION_1_2, DIFF_FILES);
	}
	private static final Revision REVISION_2_2 = new Revision(COMMIT_2_2, PARENT_TO_DIFF_FILES_2_2, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in hGraph 3
	 * (3)
	 *  |
	 * (2)
	 *  |
	 * (1)
	 */
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
	
	
	/*
	 * revisions in hGraph 4
	 * (4)
	 *  | \
	 *  | (2)
	 * (3) |
	 *  | /
	 * (1)
	 */
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
	
	
	/*
	 * revisions in hGraph 5
	 * (6)
	 *  | \
	 *  | (4)
	 *  |  | \
	 *  |  | (3)
	 * (5)(2) |
	 *  \  | /
	 *    (1)
	 */
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
	
	
	/*
	 * revisions in partial hGraph 3
	 * (3)
	 *  |
	 * (2)
	 */	
	private static final Revision REVISION_2_3_PARTIAL = new Revision(COMMIT_2_3, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 4_1
	 * (4)
	 *  | \
	 *  | (2)
	 * (3)
	 */	
	private static final Revision REVISION_2_4_PARTIAL_1 = new Revision(COMMIT_2_4, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	private static final Revision REVISION_3_4_PARTIAL_1 = new Revision(COMMIT_3_4, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 5_1
	 * (6)
	 *  | \
	 *  | (4)
	 *  |    \
	 *  |    (3)
	 * (5)
	 */
	private static final Revision REVISION_3_5_PARTIAL_1 = new Revision(COMMIT_3_5, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	private static final Revision REVISION_5_5_PARTIAL_1 = new Revision(COMMIT_5_5, 
			new HashMap<Revision, List<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	private static final Map<Revision, List<DiffFile>> PARENT_TO_DIFF_FILES_4_5_PARTIAL_1 
			= new HashMap<Revision, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_5_PARTIAL_1.put(REVISION_3_5_PARTIAL_1, DIFF_FILES);
	}
	private static final Revision REVISION_4_5_PARTIAL_1 = new Revision(COMMIT_4_5, 
			PARENT_TO_DIFF_FILES_4_5_PARTIAL_1, Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * ordered revisions of hGraph 1
	 */
	private static final List<Revision> REVISIONS_1 = new ArrayList<Revision>();
	static {
		REVISIONS_1.add(REVISION_1_1);
	}
	
	/*
	 * ordered revisions of hGraph 2
	 */
	private static final List<Revision> REVISIONS_2 = new ArrayList<Revision>();
	static {
		REVISIONS_2.add(REVISION_1_2);
		REVISIONS_2.add(REVISION_2_2);
	}
	
	/*
	 * ordered revisions of hGraph 3
	 */
	private static final List<Revision> REVISIONS_3 = new ArrayList<Revision>();
	static {
		REVISIONS_3.add(REVISION_1_3);
		REVISIONS_3.add(REVISION_2_3);
		REVISIONS_3.add(REVISION_3_3);
	}
	
	/*
	 * ordered revisions of partial hGraph 3
	 */
	private static final List<Revision> REVISIONS_3_PARTIAL = new ArrayList<Revision>();
	static {
		REVISIONS_3_PARTIAL.add(REVISION_2_3_PARTIAL);
		REVISIONS_3_PARTIAL.add(REVISION_3_3);
	}
	
	/*
	 * ordered revisions of hGraph 4
	 */
	private static final List<Revision> REVISIONS_4 = new ArrayList<Revision>();
	static {
		REVISIONS_4.add(REVISION_1_4);
		REVISIONS_4.add(REVISION_2_4);
		REVISIONS_4.add(REVISION_3_4);
		REVISIONS_4.add(REVISION_4_4);
	}
	
	/*
	 * ordered revisions of partial hGraph 4
	 */
	private static final List<Revision> REVISIONS_4_PARTIAL = new ArrayList<Revision>();
	static {
		REVISIONS_4_PARTIAL.add(REVISION_2_4_PARTIAL_1);
		REVISIONS_4_PARTIAL.add(REVISION_3_4_PARTIAL_1);
		REVISIONS_4_PARTIAL.add(REVISION_4_4);
	}
	
	/*
	 * ordered revisions of hGraph 5
	 */
	private static final List<Revision> REVISIONS_5 = new ArrayList<Revision>();
	static {
		REVISIONS_5.add(REVISION_1_5);
		REVISIONS_5.add(REVISION_2_5);
		REVISIONS_5.add(REVISION_3_5);
		REVISIONS_5.add(REVISION_4_5);
		REVISIONS_5.add(REVISION_5_5);
		REVISIONS_5.add(REVISION_6_5);
	}
	
	/*
	 * ordered revisions of partial hGraph 5
	 */
	private static final List<Revision> REVISIONS_5_PARTIAL = new ArrayList<Revision>();
	static {
		REVISIONS_5_PARTIAL.add(REVISION_3_5_PARTIAL_1);
		REVISIONS_5_PARTIAL.add(REVISION_4_5_PARTIAL_1);
		REVISIONS_5_PARTIAL.add(REVISION_5_5_PARTIAL_1);
		REVISIONS_5_PARTIAL.add(REVISION_6_5);
	}
	
	
	private static final String[] START_COMMIT_IDS = 
		{ COMMIT_1_1, COMMIT_2_2, COMMIT_3_3, COMMIT_4_4, COMMIT_6_5 };
	private static final String[] END_COMMIT_IDS = 
		{ COMMIT_1_1, COMMIT_1_2, COMMIT_1_3, COMMIT_1_4, COMMIT_1_5 };
	
	private static final String[] START_COMMIT_IDS_PARTIAL = 
		{ COMMIT_3_3, COMMIT_4_4, COMMIT_6_5 };
	private static final String[] END_COMMIT_IDS_PARTIAL = 
		{ COMMIT_2_3, COMMIT_2_4, COMMIT_3_5 };
	
	private static final HistoryGraph HGRAPH_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_3 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5 = new HistoryGraph();
	
	private static final HistoryGraph HGRAPH_3_PARTIAL = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4_PARTIAL = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5_PARTIAL = new HistoryGraph();
	
	static {
		buildHistoryGraph(HGRAPH_1, REVISIONS_1);
		buildHistoryGraph(HGRAPH_2, REVISIONS_2);
		buildHistoryGraph(HGRAPH_3, REVISIONS_3);
		buildHistoryGraph(HGRAPH_4, REVISIONS_4);
		buildHistoryGraph(HGRAPH_5, REVISIONS_5);
		
		buildHistoryGraph(HGRAPH_3_PARTIAL, REVISIONS_3_PARTIAL);
		buildHistoryGraph(HGRAPH_4_PARTIAL, REVISIONS_4_PARTIAL);
		buildHistoryGraph(HGRAPH_5_PARTIAL, REVISIONS_5_PARTIAL);
	}
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS = 
		{ HGRAPH_1, HGRAPH_2, HGRAPH_3, HGRAPH_4, HGRAPH_5 };
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS_PARTIAL = 
		{ HGRAPH_3_PARTIAL, HGRAPH_4_PARTIAL, HGRAPH_5_PARTIAL };
	
	@Test
	public void testBuildFullHistoryGraph() throws Exception {
		Util.untar(SAMPLE_REPOSITORIES_TAR, DEST_PATH);
		
		for (int i = 0; i < REPO_DIRS.length; i++) {
			IRepository repo = new GitRepository(REPO_DIRS[i], BUILD_STRATEGIES[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i], END_COMMIT_IDS[i]);
			
			assertNotNull("constructor returns null on " + REPO_DIRS[i], actualHGraph);
			assertEquals("result mismatched on " + REPO_DIRS[i], EXPECTED_HGRAPHS[i], actualHGraph);
		}
		
		FileUtils.deleteDirectory(new File(SAMPLE_REPOSITORIES));
	}
	
	@Test
	public void testBuildPartialHistoryGraph() throws Exception {
		Util.untar(SAMPLE_REPOSITORIES_TAR, DEST_PATH);
		
		for (int i = 2; i < REPO_DIRS_PARTIAL.length; i++) {
			IRepository repo = new GitRepository(REPO_DIRS_PARTIAL[i], BUILD_STRATEGIES_PARTIAL[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS_PARTIAL[i], END_COMMIT_IDS_PARTIAL[i]);
			
			assertNotNull("constructor returns null on " + REPO_DIRS_PARTIAL[i], actualHGraph);
			assertEquals("result mismatched on " + REPO_DIRS_PARTIAL[i], 
					EXPECTED_HGRAPHS_PARTIAL[i], actualHGraph);
		}
		
		FileUtils.deleteDirectory(new File(SAMPLE_REPOSITORIES));
	}
	
	private static void buildHistoryGraph(HistoryGraph hGraph, List<Revision> orderedRevisions) {
		for (Revision revision : orderedRevisions) {
			hGraph.addRevision(revision);
		}
	}
}