package tests;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import filters.ConcurrentREPL;
import misc.CurrentWorkingDirectory;
import misc.Message;

public class WorkingDirectoryTests extends GenericConcurrentTests {

	private static final String sep = CurrentWorkingDirectory.getPathSeparator();

	@Test
	public void testBasicPwd() {
		testInput("pwd\nexit");
		ConcurrentREPL.main(null);
		String expectation = Message.NEWCOMMAND + "" + System.getProperty("user.dir") + "\n";
		assertOutput(expectation);
	}

	@Test
	public void testPwdWithChangedDirectory() {
		testInput("pwd\ncd src\npwd\nexit");
		ConcurrentREPL.main(null);
		String expectation = Message.NEWCOMMAND + "" + System.getProperty("user.dir");
		expectation += "\n" + Message.NEWCOMMAND + expectation + sep + "src\n";
		assertOutput(expectation);
	}

	@Test
	public void testCdNonExistentDirectory() {
		testInput("cd not-a-directory\nexit");
		ConcurrentREPL.main(null);
		String expectation = Message.NEWCOMMAND
				+ "The directory specified by the command [cd not-a-directory] was not found.\n";
		assertOutput(expectation);
	}

	@Test
	public void testPwdWithCdDot() {
		testInput("pwd\ncd .\npwd\nexit");
		ConcurrentREPL.main(null);
		String expectation = Message.NEWCOMMAND + "" + System.getProperty("user.dir");
		expectation += "\n> " + expectation + "\n";
		assertOutput(expectation);
	}

	@Test
	public void testPwdWithCdDotDot() {
		testInput("pwd\ncd ..\npwd\nexit");
		ConcurrentREPL.main(null);
		String expectation = Message.NEWCOMMAND + "" + System.getProperty("user.dir");
		expectation += "\n> " + expectation.substring(0, expectation.lastIndexOf(sep)) + "\n";
		assertOutput(expectation);
	}

	/**
	 * Tests ls command. This was primarily rewritten to actually test "ls"
	 * properly. In the past, you could pass if you pass in all the listed file
	 * names as just one line from LsFilter. This also provides better feedback to
	 * students who fail as Set toString()'s are not used.
	 */
	@Test
	public void testLs() {
		testInput("cd dir1\ncd dir2\nls\nexit");
		ConcurrentREPL.main(null);
		String expected1 = Message.NEWCOMMAND + "" + Message.NEWCOMMAND + "" + Message.NEWCOMMAND + "dir3\nf2.txt\n";
		String expected2 = Message.NEWCOMMAND + "" + Message.NEWCOMMAND + "" + Message.NEWCOMMAND + "f2.txt\ndir3\n";
		checkMultipleOutput(expected1, expected2);
	}

	// Helper method used by testLS where two possible student outputs are possible.
	// Made as an improvement (in this case) over assertOutput() in
	// AllConcurrentTests/GenericConcurrentTest.
	private void checkMultipleOutput(String expected1, String expected2) {
		String actual = outContent.toString().replace("\r", "");
		expected1 = String.format("%s%s%s%s", Message.WELCOME, expected1, Message.NEWCOMMAND, Message.GOODBYE);
		expected2 = String.format("%s%s%s%s", Message.WELCOME, expected2, Message.NEWCOMMAND, Message.GOODBYE);
		assertTrue("Output:\n" + actual + "\nis not equal to:\n" + expected1 + "\nor:\n" + expected2,
				actual.equals(expected1) || actual.equals(expected2));
	}

	@Test
	public void testMultiMoveDirectory() {
		testInput("cd dir1" + sep + "dir2" + sep + "dir3" + sep
				+ "dir4\npwd\ncd ..\n pwd \n cd ..\n pwd \n cd ..\n pwd \n cd ..\n pwd\nexit");
		ConcurrentREPL.main(null);
		String originalLocation = System.getProperty("user.dir");

		String expectation = Message.NEWCOMMAND.toString() + Message.NEWCOMMAND.toString() + "" + originalLocation + sep
				+ "dir1" + sep + "dir2" + sep + "dir3" + sep + "dir4\n" + Message.NEWCOMMAND + Message.NEWCOMMAND + ""
				+ originalLocation + sep + "dir1" + sep + "dir2" + sep + "dir3\n" + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + "" + originalLocation + sep + "dir1" + sep + "dir2\n" + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + "" + originalLocation + sep + "dir1\n" + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ "" + originalLocation + "\n";
		assertOutput(expectation);
	}
}
