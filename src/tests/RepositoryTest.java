package tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import common.DiffFile;
import common.DiffFile.DiffType;
import common.HistoryGraph;
import common.Repository;
import common.Revision;

public class RepositoryTest {
    private static final String[] DIRECTORIES = {"test/sample_repositories/repo1", 
    											"test/sample_repositories/repo2",
    											"test/sample_repositories/repo3",
    											"test/sample_repositories/repo4",
    											"test/sample_repositories/repo5"};
        
    private static final Repository REPOSITORY_1 = new Repository(DIRECTORIES[0]);
    private static final Repository REPOSITORY_2 = new Repository(DIRECTORIES[1]);
    private static final Repository REPOSITORY_3 = new Repository(DIRECTORIES[2]);
    private static final Repository REPOSITORY_4 = new Repository(DIRECTORIES[3]);
    private static final Repository REPOSITORY_5 = new Repository(DIRECTORIES[4]);
    
	private static final List<DiffFile> DIFF_FILES = new ArrayList<DiffFile>();
	static {
		DiffFile diffFile = new DiffFile(DiffType.MODIFIED, "tmpfile");
		DIFF_FILES.add(diffFile);
	}

	/** revision -> parent -> diff files in hGraph 1 **/
	private static final String COMMIT_1_1 = "8fa00ca";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_1_1 = new HashMap<String, List<DiffFile>>();
	
	/** revision -> parent -> diff files in hGraph 2 **/
	private static final String COMMIT_1_2 = "3e3a0ef";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_1_2 = new HashMap<String, List<DiffFile>>();

	private static final String COMMIT_2_2 = "a667caa";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_2_2 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_2.put(COMMIT_1_2, DIFF_FILES);
	}
	
	/** revision -> parent -> diff files in hGraph 3 **/
	private static final String COMMIT_1_3 = "e082fcd";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_1_3 = new HashMap<String, List<DiffFile>>();
	
	private static final String COMMIT_2_3 = "43c4a2e";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_2_3 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_3.put(COMMIT_1_3, DIFF_FILES);
	}
	private static final String COMMIT_3_3 = "2512c4a";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_3_3 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_3.put(COMMIT_2_3, DIFF_FILES);
	}
	
	/** revision -> parent -> diff files in hGraph 4 **/
	private static final String COMMIT_1_4 = "ce7fccb";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_1_4 = new HashMap<String, List<DiffFile>>();
	
	private static final String COMMIT_2_4 = "50bb766";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_2_4 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_4.put(COMMIT_1_4, DIFF_FILES);
	}
	private static final String COMMIT_3_4 = "8d4a966";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_3_4 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_4.put(COMMIT_1_4, DIFF_FILES);
	}
	private static final String COMMIT_4_4 = "11f34d6";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_4_4 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_4.put(COMMIT_2_4, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_4.put(COMMIT_3_4, DIFF_FILES);
	}
	
	/** revision -> parent -> diff files in hGraph 5 **/
	private static final String COMMIT_1_5 = "eab94e5";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_1_5 = new HashMap<String, List<DiffFile>>();
	
	private static final String COMMIT_2_5 = "d863c56";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_2_5 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_5.put(COMMIT_1_5, DIFF_FILES);
	}
	private static final String COMMIT_3_5 = "7d72bc0";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_3_5 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_5.put(COMMIT_1_5, DIFF_FILES);
	}
	private static final String COMMIT_4_5 = "e14c9c6";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_4_5 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_5.put(COMMIT_2_5, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_5.put(COMMIT_3_5, DIFF_FILES);
	}
	private static final String COMMIT_5_5 = "35c25ec";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_5_5 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5.put(COMMIT_1_5, DIFF_FILES);
	}
	private static final String COMMIT_6_5 = "e654cdb";
	private static final Map<String, List<DiffFile>> PARENT_TO_DIFF_FILES_6_5 = new HashMap<String, List<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_6_5.put(COMMIT_4_5, DIFF_FILES);
		PARENT_TO_DIFF_FILES_6_5.put(COMMIT_5_5, DIFF_FILES);
	}
	
	/** a revision in a particular hGraph **/
	private static final Revision REVISION_1_1 = new Revision(REPOSITORY_1, COMMIT_1_1, PARENT_TO_DIFF_FILES_1_1);
	
	private static final Revision REVISION_1_2 = new Revision(REPOSITORY_2, COMMIT_1_2, PARENT_TO_DIFF_FILES_1_2);
	private static final Revision REVISION_2_2 = new Revision(REPOSITORY_2, COMMIT_2_2, PARENT_TO_DIFF_FILES_2_2);
	
	private static final Revision REVISION_1_3 = new Revision(REPOSITORY_3, COMMIT_1_3, PARENT_TO_DIFF_FILES_1_3);
	private static final Revision REVISION_2_3 = new Revision(REPOSITORY_3, COMMIT_2_3, PARENT_TO_DIFF_FILES_2_3);
	private static final Revision REVISION_3_3 = new Revision(REPOSITORY_3, COMMIT_3_3, PARENT_TO_DIFF_FILES_3_3);
	
	private static final Revision REVISION_1_4 = new Revision(REPOSITORY_4, COMMIT_1_4, PARENT_TO_DIFF_FILES_1_4);
	private static final Revision REVISION_2_4 = new Revision(REPOSITORY_4, COMMIT_2_4, PARENT_TO_DIFF_FILES_2_4);
	private static final Revision REVISION_3_4 = new Revision(REPOSITORY_4, COMMIT_3_4, PARENT_TO_DIFF_FILES_3_4);
	private static final Revision REVISION_4_4 = new Revision(REPOSITORY_4, COMMIT_4_4, PARENT_TO_DIFF_FILES_4_4);
	
	private static final Revision REVISION_1_5 = new Revision(REPOSITORY_5, COMMIT_1_5, PARENT_TO_DIFF_FILES_1_5);
	private static final Revision REVISION_2_5 = new Revision(REPOSITORY_5, COMMIT_2_5, PARENT_TO_DIFF_FILES_2_5);
	private static final Revision REVISION_3_5 = new Revision(REPOSITORY_5, COMMIT_3_5, PARENT_TO_DIFF_FILES_3_5);
	private static final Revision REVISION_4_5 = new Revision(REPOSITORY_5, COMMIT_4_5, PARENT_TO_DIFF_FILES_4_5);
	private static final Revision REVISION_5_5 = new Revision(REPOSITORY_5, COMMIT_5_5, PARENT_TO_DIFF_FILES_5_5);
	private static final Revision REVISION_6_5 = new Revision(REPOSITORY_5, COMMIT_6_5, PARENT_TO_DIFF_FILES_6_5);
	/***************************************/
		
	/** a set of revisions in a particular hGraph **/
	private static final List<Revision> REVISIONS_1 = new ArrayList<Revision>();
	static {
		REVISIONS_1.add(REVISION_1_1);
	}
	
	private static final List<Revision> REVISIONS_2 = new ArrayList<Revision>();
	static {
		REVISIONS_2.add(REVISION_1_2);
		REVISIONS_2.add(REVISION_2_2);
	}
	
	private static final List<Revision> REVISIONS_3 = new ArrayList<Revision>();
	static {
		REVISIONS_3.add(REVISION_1_3);
		REVISIONS_3.add(REVISION_2_3);
		REVISIONS_3.add(REVISION_3_3);
	}
	
	private static final List<Revision> REVISIONS_4 = new ArrayList<Revision>();
	static {
		REVISIONS_4.add(REVISION_1_4);
		REVISIONS_4.add(REVISION_2_4);
		REVISIONS_4.add(REVISION_3_4);
		REVISIONS_4.add(REVISION_4_4);
	}
	
	private static final List<Revision> REVISIONS_5 = new ArrayList<Revision>();
	static {
		REVISIONS_5.add(REVISION_1_5);
		REVISIONS_5.add(REVISION_2_5);
		REVISIONS_5.add(REVISION_3_5);
		REVISIONS_5.add(REVISION_4_5);
		REVISIONS_5.add(REVISION_5_5);
		REVISIONS_5.add(REVISION_6_5);
	}
	/***********************************************/
	
	private static final String[] START_COMMIT_IDS = {COMMIT_1_1, COMMIT_2_2, COMMIT_3_3, COMMIT_4_4, COMMIT_6_5};
	private static final String[] END_COMMIT_IDS = {COMMIT_1_1, COMMIT_1_2, COMMIT_1_3, COMMIT_1_4, COMMIT_1_5};
	
	private static final HistoryGraph HGRAPH_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_3 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5 = new HistoryGraph();
	static {
		buildHistoryGraph(HGRAPH_1, REVISIONS_1);
		buildHistoryGraph(HGRAPH_2, REVISIONS_2);
		buildHistoryGraph(HGRAPH_3, REVISIONS_3);
		buildHistoryGraph(HGRAPH_4, REVISIONS_4);
		buildHistoryGraph(HGRAPH_5, REVISIONS_5);
	}
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS = {HGRAPH_1, HGRAPH_2, HGRAPH_3, HGRAPH_4, HGRAPH_5};
	
	@Test
	public void testBuildHistoryGraphOnSampleRepositories() {
		for (int i = 0; i < DIRECTORIES.length; i++) {
			Repository repo = new Repository(DIRECTORIES[i]);
	
			HistoryGraph actualHGraph = null;
			try {
				actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i], END_COMMIT_IDS[i]);
				assertNotNull("constructor returns null on " + DIRECTORIES[i], actualHGraph);
			} catch (IOException e) {
				fail("IOException thrown on " + DIRECTORIES[i]);
			}
			
			assertEquals("result mismatched on " + DIRECTORIES[i], EXPECTED_HGRAPHS[i], actualHGraph);
		}
	}
	
	@Test
	public void testBuildHistoryGraphOnVoldemortRepository() {
		// TODO : implement test
	}

	private static void buildHistoryGraph(HistoryGraph hGraph, List<Revision> revisions) {
		for (Revision revision : revisions) {
			hGraph.addRevision(revision);
		}
	}

}
