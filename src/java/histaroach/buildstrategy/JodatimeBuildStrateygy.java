package histaroach.buildstrategy;

import histaroach.model.DiffFile;
import histaroach.model.TestResult;
import histaroach.util.Util;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * JodatimeBuildStrateygy is for joda-time project.
 */
public class JodatimeBuildStrateygy extends AntBuildStrategy {

	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = -5151296358552395902L;
	
	private static final String[] ENSURE_NO_HALT_ON_FAILURE = { "sed", "-i", "-e", 
		"s/haltonfailure=\"yes\"/haltonfailure=\"no\"/", "build.xml" };
	
	private static final String BUILD_TARGET_NAME = "compile.tests test.time";
	private static final String TEST_TARGET_NAME = "test";
	
	private final File directory;
	
	/**
	 * Creates a JodatimeBuildStrategy.
	 */
	public JodatimeBuildStrateygy(File directory, String antCommand) {
		super(directory, antCommand, BUILD_TARGET_NAME, TEST_TARGET_NAME);
		
		this.directory = directory;
	}
	
	@Override
	public TestResult runTest() throws IOException, InterruptedException {
		ensureNoHaltOnFailure();
		TestResult testResult = super.runTest();
		return testResult;
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
		File buildFile = new File(directory.getPath() + File.separatorChar + BUILD_XML);
		Process sedProcess = Util.runProcess(ENSURE_NO_HALT_ON_FAILURE, directory);
		return !buildFile.exists() || sedProcess.exitValue() == 0;
	}

	@Override
	public boolean isTestFile(DiffFile diffFile) {
		// TODO Auto-generated method stub
		return false;
	}

}
