package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;

import misc.CurrentWorkingDirectory;

/**
 * Superclass for all test types besides REPL_Jobs_Tests.java. Provides standard
 * boilerplate for REPL interaction as well as a timeout rule.
 */
public abstract class GenericConcurrentTests {

	// adjust if TA computer is slow (seconds)
	private static final int TIMEOUT = 5;

	/**
	 * Specifies timeout rule for all tests in classes that extend
	 * GenericConcurrentTests. Tests that run beyond the above TIMEOUT (seconds)
	 * will fail.
	 */
	@Rule
	public Timeout timeout = Timeout.seconds(TIMEOUT);

	/**
	 * Resets the current working directory for testing cd command. Added for Spring
	 */
	@After
	public void resetCurrentWorkingDirectory() {
		CurrentWorkingDirectory.reset();
	}

	protected ByteArrayInputStream inContent;

	protected final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	protected final ByteArrayOutputStream errContent = new ByteArrayOutputStream();

	public void testInput(String s) {
		inContent = new ByteArrayInputStream(s.getBytes());
		System.setIn(inContent);
	}

	public void assertOutput(String expected) {
		AllConcurrentTests.assertOutput(expected, outContent);
	}

	@Before
	public void setUpStreams() {
		System.setOut(new PrintStream(outContent));
		System.setErr(new PrintStream(errContent));
	}

	@After
	public void cleanUpStreams() {
		System.setIn(null);
		System.setOut(null);
		System.setErr(null);
	}
}
