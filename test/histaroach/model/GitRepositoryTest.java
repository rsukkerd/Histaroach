package histaroach.model;

import histaroach.buildstrategy.AntBuildStrategy;
import histaroach.buildstrategy.IBuildStrategy;
import histaroach.buildstrategy.VoldemortBuildStrategy;
import histaroach.model.DiffFile.DiffType;
import histaroach.model.Revision.Compilable;
import histaroach.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


public class GitRepositoryTest extends TestCase {
	
	private static final String TEST_DATA_PATH = "test-data/";
	private static final String ANT_COMMAND = "ant";
	
	/****************************/
	/** in sample_repositories **/
	/****************************/
	private static final String SAMPLE_REPOSITORIES = TEST_DATA_PATH + "sample_repositories";
	private static final String SAMPLE_REPOSITORIES_TAR = TEST_DATA_PATH + "sample_repositories.tar";
	
	private static final String[] REPO_PATHS = 
		{ TEST_DATA_PATH + "sample_repositories/repo1", 
		TEST_DATA_PATH + "sample_repositories/repo2", 
		TEST_DATA_PATH + "sample_repositories/repo3", 
		TEST_DATA_PATH + "sample_repositories/repo4", 
		TEST_DATA_PATH + "sample_repositories/repo5", 
		TEST_DATA_PATH + "sample_repositories/repo6", };
	
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
		{ REPO_DIRS[2], 
		REPO_DIRS[3], REPO_DIRS[3], 
		REPO_DIRS[4], REPO_DIRS[4], 
		REPO_DIRS[5], REPO_DIRS[5] };
	
	private static final IBuildStrategy[] BUILD_STRATEGIES_PARTIAL = 
		{ BUILD_STRATEGIES[2], 
		BUILD_STRATEGIES[3], BUILD_STRATEGIES[3], 
		BUILD_STRATEGIES[4], BUILD_STRATEGIES[4], 
		BUILD_STRATEGIES[5], BUILD_STRATEGIES[5] };
    
	private static final Set<DiffFile> DIFF_FILES = new HashSet<DiffFile>();
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
	
	private static final String COMMIT_1_5 = "4398141";
	private static final String COMMIT_2_5 = "21bb079";
	private static final String COMMIT_3_5 = "29a3c6b";
	private static final String COMMIT_4_5 = "76c936c";
	private static final String COMMIT_5_5 = "401a636";
	
	private static final String COMMIT_1_6 = "eab94e5";
	private static final String COMMIT_2_6 = "d863c56";
	private static final String COMMIT_3_6 = "7d72bc0";
	private static final String COMMIT_4_6 = "e14c9c6";
	private static final String COMMIT_5_6 = "35c25ec";
	private static final String COMMIT_6_6 = "e654cdb";
	
	
	/*
	 * hGraph 1
	 * (1)
	 */
	/**
	 *  parents/diff files of revision 1 in hGraph 1
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_1 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 * revision 1 in hGraph1
	 */
	private static final Revision REVISION_1_1 = new Revision(COMMIT_1_1, PARENT_TO_DIFF_FILES_1_1, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * hGraph 2
	 * (2)
	 *  |
	 * (1)
	 */
	/**
	 *  parents/diff files of revision 1 in hGraph 2
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 *  revision 1 in hGraph 2
	 */
	private static final Revision REVISION_1_2 = new Revision(COMMIT_1_2, PARENT_TO_DIFF_FILES_1_2, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 2 in hGraph 2
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_2.put(REVISION_1_2, DIFF_FILES);
	}
	/**
	 *  revision 2 in hGraph 2
	 */
	private static final Revision REVISION_2_2 = new Revision(COMMIT_2_2, PARENT_TO_DIFF_FILES_2_2, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * hGraph 3
	 * (3)
	 *  |
	 * (2)
	 *  |
	 * (1)
	 */
	/**
	 *  parents/diff files of revision 1 in hGraph 3
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_3 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 *  revision 1 in hGraph 3
	 */
	private static final Revision REVISION_1_3 = new Revision(COMMIT_1_3, PARENT_TO_DIFF_FILES_1_3, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 2 in hGraph 3
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2_3 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_3.put(REVISION_1_3, DIFF_FILES);
	}
	/**
	 *  revision 2 in hGraph 3
	 */
	private static final Revision REVISION_2_3 = new Revision(COMMIT_2_3, PARENT_TO_DIFF_FILES_2_3, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 3 in hGraph 3
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_3_3 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_3.put(REVISION_2_3, DIFF_FILES);
	}
	/**
	 *  revision 3 in hGraph 3
	 */
	private static final Revision REVISION_3_3 = new Revision(COMMIT_3_3, PARENT_TO_DIFF_FILES_3_3, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * hGraph 4
	 * (4)
	 *  | \
	 *  | (2)
	 * (3) |
	 *  | /
	 * (1)
	 */
	/**
	 *  parents/diff files of revision 1 in hGraph 4
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_4 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 *  revision 1 in hGraph 4
	 */
	private static final Revision REVISION_1_4 = new Revision(COMMIT_1_4, PARENT_TO_DIFF_FILES_1_4, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 2 in hGraph 4
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2_4 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_4.put(REVISION_1_4, DIFF_FILES);
	}
	/**
	 *  revision 2 in hGraph 4
	 */
	private static final Revision REVISION_2_4 = new Revision(COMMIT_2_4, PARENT_TO_DIFF_FILES_2_4, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 3 in hGraph 4
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_3_4 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_4.put(REVISION_1_4, DIFF_FILES);
	}
	/**
	 *  revision 3 in hGraph 4 in hGraph 4
	 */
	private static final Revision REVISION_3_4 = new Revision(COMMIT_3_4, PARENT_TO_DIFF_FILES_3_4, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 4 in hGraph 4
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_4_4 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_4.put(REVISION_2_4, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_4.put(REVISION_3_4, DIFF_FILES);
	}
	/**
	 *  revision 4 in hGraph 4
	 */
	private static final Revision REVISION_4_4 = new Revision(COMMIT_4_4, PARENT_TO_DIFF_FILES_4_4, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in hGraph 5
	 *   _(5)
	 *  /  | \
	 * (4)(3)(2)
	 *   \ | /
	 *    (1)
	 */
	private static final DiffFile DIFF_FILE_1 = new DiffFile(DiffType.MODIFIED, "tmpfile_1");
	private static final DiffFile DIFF_FILE_2 = new DiffFile(DiffType.ADDED, "tmpfile_2");
	private static final DiffFile DIFF_FILE_3 = new DiffFile(DiffType.ADDED, "tmpfile_3");
	
	private static final Set<DiffFile> DIFF_FILES_FROM_1_TO_2 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_1_TO_2.add(DIFF_FILE_2);
	}
	
	private static final Set<DiffFile> DIFF_FILES_FROM_1_TO_3 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_1_TO_3.add(DIFF_FILE_3);
	}
	
	private static final Set<DiffFile> DIFF_FILES_FROM_1_TO_4 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_1_TO_4.add(DIFF_FILE_1);
	}
	
	private static final Set<DiffFile> DIFF_FILES_FROM_2_TO_5 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_2_TO_5.add(DIFF_FILE_1);
		DIFF_FILES_FROM_2_TO_5.add(DIFF_FILE_3);
	}
	
	private static final Set<DiffFile> DIFF_FILES_FROM_3_TO_5 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_3_TO_5.add(DIFF_FILE_1);
		DIFF_FILES_FROM_3_TO_5.add(DIFF_FILE_2);
	}
	
	private static final Set<DiffFile> DIFF_FILES_FROM_4_TO_5 = new HashSet<DiffFile>();
	static {
		DIFF_FILES_FROM_4_TO_5.add(DIFF_FILE_2);
		DIFF_FILES_FROM_4_TO_5.add(DIFF_FILE_3);
	}
	
	/**
	 *  parents/diff files of revision 1 in hGraph 5
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_5 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 *  revision 1 in hGraph 5
	 */
	private static final Revision REVISION_1_5 = new Revision(COMMIT_1_5, PARENT_TO_DIFF_FILES_1_5, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 2 in hGraph 5
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2_5 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_5.put(REVISION_1_5, DIFF_FILES_FROM_1_TO_2);
	}
	/**
	 *  revision 2 in hGraph 5
	 */
	private static final Revision REVISION_2_5 = new Revision(COMMIT_2_5, PARENT_TO_DIFF_FILES_2_5, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 3 in hGraph 5
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_3_5 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_5.put(REVISION_1_5, DIFF_FILES_FROM_1_TO_3);
	}
	/**
	 *  revision 3 in hGraph 5
	 */
	private static final Revision REVISION_3_5 = new Revision(COMMIT_3_5, PARENT_TO_DIFF_FILES_3_5, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 4 in hGraph 5
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_4_5 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_5.put(REVISION_1_5, DIFF_FILES_FROM_1_TO_4);
	}
	/**
	 *  revision 4 in hGraph 5
	 */
	private static final Revision REVISION_4_5 = new Revision(COMMIT_4_5, PARENT_TO_DIFF_FILES_4_5, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 5 in hGraph 5
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_5_5 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5.put(REVISION_2_5, DIFF_FILES_FROM_2_TO_5);
		PARENT_TO_DIFF_FILES_5_5.put(REVISION_3_5, DIFF_FILES_FROM_3_TO_5);
		PARENT_TO_DIFF_FILES_5_5.put(REVISION_4_5, DIFF_FILES_FROM_4_TO_5);
	}
	/**
	 *  revision 5 in hGraph 5
	 */
	private static final Revision REVISION_5_5 = new Revision(COMMIT_5_5, PARENT_TO_DIFF_FILES_5_5, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in hGraph 6
	 * (6)
	 *  | \
	 *  | (4)
	 *  |  | \
	 *  |  | (3)
	 * (5)(2) |
	 *  \  | /
	 *    (1)
	 */
	/**
	 *  parents/diff files of revision 1 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_1_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	/**
	 *  revision 1 in hGraph 6
	 */
	private static final Revision REVISION_1_6 = new Revision(COMMIT_1_6, PARENT_TO_DIFF_FILES_1_6, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 2 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_2_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_2_6.put(REVISION_1_6, DIFF_FILES);
	}
	/**
	 *  revision 2 in hGraph 6
	 */
	private static final Revision REVISION_2_6 = new Revision(COMMIT_2_6, PARENT_TO_DIFF_FILES_2_6, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 3 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_3_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_3_6.put(REVISION_1_6, DIFF_FILES);
	}
	/**
	 *  revision 3 in hGraph 6
	 */
	private static final Revision REVISION_3_6 = new Revision(COMMIT_3_6, PARENT_TO_DIFF_FILES_3_6, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 4 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_4_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_6.put(REVISION_2_6, DIFF_FILES);
		PARENT_TO_DIFF_FILES_4_6.put(REVISION_3_6, DIFF_FILES);
	}
	/**
	 *  revision 4 in hGraph 6
	 */
	private static final Revision REVISION_4_6 = new Revision(COMMIT_4_6, PARENT_TO_DIFF_FILES_4_6, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 5 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_5_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_6.put(REVISION_1_6, DIFF_FILES);
	}
	/**
	 *  revision 5 in hGraph 6
	 */
	private static final Revision REVISION_5_6 = new Revision(COMMIT_5_6, PARENT_TO_DIFF_FILES_5_6, 
			Compilable.NO_BUILD_FILE, null);
	
	/**
	 *  parents/diff files of revision 6 in hGraph 6
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_6_6 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_6_6.put(REVISION_4_6, DIFF_FILES);
		PARENT_TO_DIFF_FILES_6_6.put(REVISION_5_6, DIFF_FILES);
	}
	/**
	 *  revision 6 in hGraph 6
	 */
	private static final Revision REVISION_6_6 = new Revision(COMMIT_6_6, PARENT_TO_DIFF_FILES_6_6, 
			Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 3
	 * (3)
	 *  |
	 * (2)
	 *  |
	 */	
	/**
	 * revision 2 in partial hGraph 3
	 */
	private static final Revision REVISION_2_3_PARTIAL = new Revision(COMMIT_2_3, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 4_1
	 * (4)
	 *  | \
	 *  | (2)
	 * (3) |
	 *  |
	 */	
	/**
	 * revision 2 in partial hGraph 4_1
	 */
	private static final Revision REVISION_2_4_PARTIAL_1 = new Revision(COMMIT_2_4, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * revision 3 in partial hGraph 4_1
	 */
	private static final Revision REVISION_3_4_PARTIAL_1 = new Revision(COMMIT_3_4, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/*
	 * revisions in partial hGraph 4_2
	 * (4)
	 *  | \
	 *  |
	 * (3)
	 *  |
	 */	
	/**
	 * revision 3 in partial hGraph 4_2
	 */
	private static final Revision REVISION_3_4_PARTIAL_2 = new Revision(COMMIT_3_4, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * parents/diff files of revision 4 in partial hGraph 4_2
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_4_4_PARTIAL_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_4_PARTIAL_2.put(REVISION_3_4_PARTIAL_2, DIFF_FILES);
	}
	/**
	 * revision 4 in partial hGraph 4_2
	 */
	private static final Revision REVISION_4_4_PARTIAL_2 = new Revision(COMMIT_4_4, 
			PARENT_TO_DIFF_FILES_4_4_PARTIAL_2, Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 5_1
	 *   _(5)
	 *  /  | \
	 * (4)(3)
	 *   \ |
	 */
	/**
	 * revision 3 in partial hGraph 5_1
	 */
	private static final Revision REVISION_3_5_PARTIAL_1 = new Revision(COMMIT_3_5, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * revision 4 in partial hGraph 5_1
	 */
	private static final Revision REVISION_4_5_PARTIAL_1 = new Revision(COMMIT_4_5, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * parents/diff files of revision 5 in partial hGraph 5_1
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_5_5_PARTIAL_1 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5_PARTIAL_1.put(REVISION_3_5_PARTIAL_1, DIFF_FILES_FROM_3_TO_5);
		PARENT_TO_DIFF_FILES_5_5_PARTIAL_1.put(REVISION_4_5_PARTIAL_1, DIFF_FILES_FROM_4_TO_5);
	}
	/**
	 * revision 5 in partial hGraph 5_1
	 */
	private static final Revision REVISION_5_5_PARTIAL_1 = new Revision(COMMIT_5_5, 
			PARENT_TO_DIFF_FILES_5_5_PARTIAL_1, Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 5_2
	 *   _(5)
	 *  /  | \
	 * (4)
	 *   \
	 */
	/**
	 * revision 4 in partial hGraph 5_2
	 */
	private static final Revision REVISION_4_5_PARTIAL_2 = new Revision(COMMIT_4_5, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * parents/diff files of revision 5 in partial hGraph 5_2
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_5_5_PARTIAL_2 = 
		new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_5_5_PARTIAL_2.put(REVISION_4_5_PARTIAL_2, DIFF_FILES_FROM_4_TO_5);
	}
	/**
	 * revision 5 in partial hGraph 5_2
	 */
	private static final Revision REVISION_5_5_PARTIAL_2 = new Revision(COMMIT_5_5, 
			PARENT_TO_DIFF_FILES_5_5_PARTIAL_2, Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 6_1
	 * (6)
	 *  | \
	 *  | (4)
	 *  |  | \
	 *  |    (3)
	 * (5)    |
	 *   \
	 */
	/**
	 * revision 3 in partial hGraph 6_1
	 */
	private static final Revision REVISION_3_6_PARTIAL_1 = new Revision(COMMIT_3_6, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * revision 5 in partial hGraph 6_1
	 */
	private static final Revision REVISION_5_6_PARTIAL_1 = new Revision(COMMIT_5_6, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * parents/diff files of revision 4 in partial hGraph 6_1
	 */
	private static final Map<Revision, Set<DiffFile>> PARENT_TO_DIFF_FILES_4_6_PARTIAL_1 
			= new HashMap<Revision, Set<DiffFile>>();
	static {
		PARENT_TO_DIFF_FILES_4_6_PARTIAL_1.put(REVISION_3_6_PARTIAL_1, DIFF_FILES);
	}
	/**
	 * revision 4 in partial hGraph 6_1
	 */
	private static final Revision REVISION_4_6_PARTIAL_1 = new Revision(COMMIT_4_6, 
			PARENT_TO_DIFF_FILES_4_6_PARTIAL_1, Compilable.NO_BUILD_FILE, null);
	
	
	/*
	 * revisions in partial hGraph 6_2
	 * (6)
	 *  | \
	 *  | (4)
	 *  |  | \
	 *  |
	 * (5)
	 *   \
	 */
	/**
	 * revision 4 in partial hGraph 6_2
	 */
	private static final Revision REVISION_4_6_PARTIAL_2 = new Revision(COMMIT_4_6, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	/**
	 * revision 5 in partial hGraph 6_2
	 */
	private static final Revision REVISION_5_6_PARTIAL_2 = new Revision(COMMIT_5_6, 
			new HashMap<Revision, Set<DiffFile>>(), Compilable.NO_BUILD_FILE, null);
	
	
	/**
	 * ordered revisions of hGraph 1
	 */
	private static final List<Revision> REVISIONS_1 = new ArrayList<Revision>();
	static {
		REVISIONS_1.add(REVISION_1_1);
	}
	
	/**
	 * ordered revisions of hGraph 2
	 */
	private static final List<Revision> REVISIONS_2 = new ArrayList<Revision>();
	static {
		REVISIONS_2.add(REVISION_1_2);
		REVISIONS_2.add(REVISION_2_2);
	}
	
	/**
	 * ordered revisions of hGraph 3
	 */
	private static final List<Revision> REVISIONS_3 = new ArrayList<Revision>();
	static {
		REVISIONS_3.add(REVISION_1_3);
		REVISIONS_3.add(REVISION_2_3);
		REVISIONS_3.add(REVISION_3_3);
	}
	
	/**
	 * ordered revisions of partial hGraph 3
	 */
	private static final List<Revision> REVISIONS_3_PARTIAL = new ArrayList<Revision>();
	static {
		REVISIONS_3_PARTIAL.add(REVISION_2_3_PARTIAL);
		REVISIONS_3_PARTIAL.add(REVISION_3_3);
	}
	
	/**
	 * ordered revisions of hGraph 4
	 */
	private static final List<Revision> REVISIONS_4 = new ArrayList<Revision>();
	static {
		REVISIONS_4.add(REVISION_1_4);
		REVISIONS_4.add(REVISION_2_4);
		REVISIONS_4.add(REVISION_3_4);
		REVISIONS_4.add(REVISION_4_4);
	}
	
	/**
	 * ordered revisions of partial hGraph 4_1
	 */
	private static final List<Revision> REVISIONS_4_PARTIAL_1 = new ArrayList<Revision>();
	static {
		REVISIONS_4_PARTIAL_1.add(REVISION_2_4_PARTIAL_1);
		REVISIONS_4_PARTIAL_1.add(REVISION_3_4_PARTIAL_1);
		REVISIONS_4_PARTIAL_1.add(REVISION_4_4);
	}
	
	/**
	 * ordered revisions of partial hGraph 4_2
	 */
	private static final List<Revision> REVISIONS_4_PARTIAL_2 = new ArrayList<Revision>();
	static {
		REVISIONS_4_PARTIAL_2.add(REVISION_3_4_PARTIAL_2);
		REVISIONS_4_PARTIAL_2.add(REVISION_4_4_PARTIAL_2);
	}
	
	/**
	 * ordered revisions of hGraph 5
	 */
	private static final List<Revision> REVISIONS_5 = new ArrayList<Revision>();
	static {
		REVISIONS_5.add(REVISION_1_5);
		REVISIONS_5.add(REVISION_2_5);
		REVISIONS_5.add(REVISION_3_5);
		REVISIONS_5.add(REVISION_4_5);
		REVISIONS_5.add(REVISION_5_5);
	}
	
	/**
	 * ordered revisions of partial hGraph 5_1
	 */
	private static final List<Revision> REVISIONS_5_PARTIAL_1 = new ArrayList<Revision>();
	static {
		REVISIONS_5_PARTIAL_1.add(REVISION_3_5_PARTIAL_1);
		REVISIONS_5_PARTIAL_1.add(REVISION_4_5_PARTIAL_1);
		REVISIONS_5_PARTIAL_1.add(REVISION_5_5_PARTIAL_1);
	}
	
	/**
	 * ordered revisions of partial hGraph 5_2
	 */
	private static final List<Revision> REVISIONS_5_PARTIAL_2 = new ArrayList<Revision>();
	static {
		REVISIONS_5_PARTIAL_2.add(REVISION_4_5_PARTIAL_2);
		REVISIONS_5_PARTIAL_2.add(REVISION_5_5_PARTIAL_2);
	}
	
	/**
	 * ordered revisions of hGraph 6
	 */
	private static final List<Revision> REVISIONS_6 = new ArrayList<Revision>();
	static {
		REVISIONS_6.add(REVISION_1_6);
		REVISIONS_6.add(REVISION_2_6);
		REVISIONS_6.add(REVISION_3_6);
		REVISIONS_6.add(REVISION_4_6);
		REVISIONS_6.add(REVISION_5_6);
		REVISIONS_6.add(REVISION_6_6);
	}
	
	/**
	 * ordered revisions of partial hGraph 6_1
	 */
	private static final List<Revision> REVISIONS_6_PARTIAL_1 = new ArrayList<Revision>();
	static {
		REVISIONS_6_PARTIAL_1.add(REVISION_3_6_PARTIAL_1);
		REVISIONS_6_PARTIAL_1.add(REVISION_4_6_PARTIAL_1);
		REVISIONS_6_PARTIAL_1.add(REVISION_5_6_PARTIAL_1);
		REVISIONS_6_PARTIAL_1.add(REVISION_6_6);
	}
	
	/**
	 * ordered revisions of partial hGraph 6_2
	 */
	private static final List<Revision> REVISIONS_6_PARTIAL_2 = new ArrayList<Revision>();
	static {
		REVISIONS_6_PARTIAL_2.add(REVISION_4_6_PARTIAL_2);
		REVISIONS_6_PARTIAL_2.add(REVISION_5_6_PARTIAL_2);
		REVISIONS_6_PARTIAL_2.add(REVISION_6_6);
	}
	
	
	private static final String[] START_COMMIT_IDS = 
		{ COMMIT_1_1, COMMIT_2_2, COMMIT_3_3, COMMIT_4_4, COMMIT_5_5, COMMIT_6_6 };
	private static final String[] END_COMMIT_IDS = 
		{ COMMIT_1_1, COMMIT_1_2, COMMIT_1_3, COMMIT_1_4, COMMIT_1_5, COMMIT_1_6 };
	
	private static final String[] START_COMMIT_IDS_PARTIAL = 
		{ COMMIT_3_3, 
		COMMIT_4_4, COMMIT_4_4, 
		COMMIT_5_5, COMMIT_5_5, 
		COMMIT_6_6, COMMIT_6_6 };
	private static final String[] END_COMMIT_IDS_PARTIAL = 
		{ COMMIT_2_3, 
		COMMIT_2_4, COMMIT_3_4, 
		COMMIT_3_5, COMMIT_4_5, 
		COMMIT_3_6, COMMIT_4_6 };
	
	private static final HistoryGraph HGRAPH_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_3 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_6 = new HistoryGraph();
	
	private static final HistoryGraph HGRAPH_3_PARTIAL = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4_PARTIAL_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_4_PARTIAL_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5_PARTIAL_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_5_PARTIAL_2 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_6_PARTIAL_1 = new HistoryGraph();
	private static final HistoryGraph HGRAPH_6_PARTIAL_2 = new HistoryGraph();
	
	static {
		buildHistoryGraph(HGRAPH_1, REVISIONS_1);
		buildHistoryGraph(HGRAPH_2, REVISIONS_2);
		buildHistoryGraph(HGRAPH_3, REVISIONS_3);
		buildHistoryGraph(HGRAPH_4, REVISIONS_4);
		buildHistoryGraph(HGRAPH_5, REVISIONS_5);
		buildHistoryGraph(HGRAPH_6, REVISIONS_6);
		
		buildHistoryGraph(HGRAPH_3_PARTIAL, REVISIONS_3_PARTIAL);
		buildHistoryGraph(HGRAPH_4_PARTIAL_1, REVISIONS_4_PARTIAL_1);
		buildHistoryGraph(HGRAPH_4_PARTIAL_2, REVISIONS_4_PARTIAL_2);
		buildHistoryGraph(HGRAPH_5_PARTIAL_1, REVISIONS_5_PARTIAL_1);
		buildHistoryGraph(HGRAPH_5_PARTIAL_2, REVISIONS_5_PARTIAL_2);
		buildHistoryGraph(HGRAPH_6_PARTIAL_1, REVISIONS_6_PARTIAL_1);
		buildHistoryGraph(HGRAPH_6_PARTIAL_2, REVISIONS_6_PARTIAL_2);
	}
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS = 
		{ HGRAPH_1, HGRAPH_2, HGRAPH_3, HGRAPH_4, HGRAPH_5, HGRAPH_6 };
	
	private static final HistoryGraph[] EXPECTED_HGRAPHS_PARTIAL = 
		{ HGRAPH_3_PARTIAL, 
		HGRAPH_4_PARTIAL_1, HGRAPH_4_PARTIAL_2, 
		HGRAPH_5_PARTIAL_1, HGRAPH_5_PARTIAL_2, 
		HGRAPH_6_PARTIAL_1, HGRAPH_6_PARTIAL_2 };
	
	@Override
	protected void setUp() throws FileNotFoundException, IOException {
		Util.untar(SAMPLE_REPOSITORIES_TAR, TEST_DATA_PATH);
	}
	
	@Override
	protected void tearDown() throws IOException {
		FileUtils.deleteDirectory(new File(SAMPLE_REPOSITORIES));
	}
	
	@Test
	public void testBuildFullHistoryGraph() throws Exception {
		
		for (int i = 0; i < REPO_DIRS.length; i++) {
			IRepository repo = new GitRepository(REPO_DIRS[i], BUILD_STRATEGIES[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS[i], END_COMMIT_IDS[i]);
			
			assertNotNull("constructor returns null on " + REPO_DIRS[i], actualHGraph);
			assertEquals("result mismatched on " + REPO_DIRS[i], EXPECTED_HGRAPHS[i], actualHGraph);
		}		
	}
	
	@Test
	public void testBuildPartialHistoryGraph() throws Exception {
		
		for (int i = 2; i < REPO_DIRS_PARTIAL.length; i++) {
			IRepository repo = new GitRepository(REPO_DIRS_PARTIAL[i], BUILD_STRATEGIES_PARTIAL[i]);
	
			HistoryGraph actualHGraph = null;
			actualHGraph = repo.buildHistoryGraph(START_COMMIT_IDS_PARTIAL[i], END_COMMIT_IDS_PARTIAL[i]);
			
			assertNotNull("constructor returns null on " + REPO_DIRS_PARTIAL[i], actualHGraph);
			assertEquals("result mismatched on " + REPO_DIRS_PARTIAL[i], 
					EXPECTED_HGRAPHS_PARTIAL[i], actualHGraph);
		}		
	}
	
	/**
	 * Adds orderedRevisions into hGraph.
	 */
	private static void buildHistoryGraph(HistoryGraph hGraph, List<Revision> orderedRevisions) {
		for (Revision revision : orderedRevisions) {
			hGraph.addRevision(revision);
		}
	}
}
