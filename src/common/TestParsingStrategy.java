package common;

import java.io.Serializable;
import java.util.List;

/**
 * TestParsingStrategy is an interface. It contains a method 
 * that parses a test result from standard output and error stream.
 */
public interface TestParsingStrategy extends Serializable {
	
	public TestResult getTestResult(String commitID, List<String> outputStreamContent, List<String> errorStreamContent);
	
}
