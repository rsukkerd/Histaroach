package ant;

import java.io.File;
import java.io.IOException;
import java.util.List;

import util.Pair;
import util.Util;

import git.GitRepository;

import common.Repository;
import common.TestResult;
import common.Revision.Compilable;

/**
 * JodatimeBuildStrateygy is a concrete subclass of AntBuildStrategy. 
 * 
 * For joda-time project.
 */
public class JodatimeBuildStrateygy extends AntBuildStrategy {

	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -5151296358552395902L;
	
	private static final String[] ENSURE_NO_HALT_ON_FAILURE = { "sed", "-i", "-e", 
		"s/haltonfailure=\"yes\"/haltonfailure=\"no\"/", "build.xml" };
	
	private static final String ENSURE_EXCEPTION_MESSAGE = "ensure haltonfailure=\"no\" unsuccessful";
	private static final String RESTORE_EXCEPTION_MESSAGE = "restore build.xml unsuccessful";
	private static final String TEST_COMMAND = "test";
	
	private final File directory;
	private final Repository repository;
	
	/**
	 * Creates a JodatimeBuildStrategy.
	 */
	public JodatimeBuildStrateygy(File directory, String antCommand) {
		super(directory, antCommand, TEST_COMMAND);
		this.directory = directory;
		repository = new GitRepository(directory, this);
	}
	
	@Override
	public Pair<Compilable, TestResult> runTest() throws Exception {
		if (!ensureNoHaltOnFailure()) {
			throw new Exception(ENSURE_EXCEPTION_MESSAGE);
		}
		
		Pair<Compilable, TestResult> result = super.runTest();
		
		if (!restoreBuildFile()) {
			throw new Exception(RESTORE_EXCEPTION_MESSAGE);
		}
		
		return result;
	}
	
	@Override
	public Pair<Compilable, TestResult> runTestViaShellScript() throws Exception {
		if (!ensureNoHaltOnFailure()) {
			throw new Exception(ENSURE_EXCEPTION_MESSAGE);
		}
		
		Pair<Compilable, TestResult> result = super.runTestViaShellScript();
		
		if (!restoreBuildFile()) {
			throw new Exception(RESTORE_EXCEPTION_MESSAGE);
		}
		
		return result;
	}
	
	@Override
	protected TestResult getTestResult(List<String> outputStreamContent, 
			List<String> errorStreamContent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Ensures haltonfailure="no" in build.xml.
	 * 
	 * @return true if the method successfully ensured 
	 *         haltonfailure="no" in build.xml.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean ensureNoHaltOnFailure() throws IOException, InterruptedException {
		File builFile = new File(directory.getPath() + File.separatorChar + BUILD_XML);
	
		Process sedProcess = Util.runProcess(ENSURE_NO_HALT_ON_FAILURE, directory);
		return !builFile.exists() || sedProcess.exitValue() == 0;
	}
	
	/**
	 * Restores build.xml after ensureNoHaltOnFailure().
	 * 
	 * @return true if the method successfully restored 
	 *         build.xml.
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private boolean restoreBuildFile() throws IOException, InterruptedException {
		return repository.discardFileChange(BUILD_XML);
	}

}
