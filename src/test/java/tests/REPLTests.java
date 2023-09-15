package tests;

import org.junit.Test;

import filters.ConcurrentREPL;
import misc.Message;

public class REPLTests extends GenericConcurrentTests {

	@Test
	public void testNotACommand1() {
		testInput("not-a-command\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "The command [not-a-command] was not recognized.\n");
	}

	@Test
	public void testNotACommand2() {
		testInput("ls | gripe HELLO\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "The command [gripe HELLO] was not recognized.\n");
	}

	@Test
	public void testNotACommand3() {
		testInput("cathello.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "The command [cathello.txt] was not recognized.\n");
	}

	@Test
	public void testNotACommand4() {
		testInput("cdsrc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "The command [cdsrc] was not recognized.\n");
	}

	@Test
	public void testNotACommand5() {
		testInput("pwd | grepunixish\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "The command [grepunixish] was not recognized.\n");
	}

	@Test
	public void testCanContinueAfterError1() {
		testInput("cd dir1\n ls | gripe HELLO\nls | grep f1\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND.toString() + Message.NEWCOMMAND
				+ "The command [gripe HELLO] was not recognized.\n> f1.txt\n");
	}

	@Test
	public void testCanContinueAfterError2() {
		testInput("cat fizz-buzz-100000.txt | grep 1 | wc\ncat fizz-buzz-10000.txt | grep 1 | wc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND
				+ "At least one of the files in the command [cat fizz-buzz-100000.txt] was not found.\n> 1931 1931 7555\n");
	}

	@Test
	public void testFileNotFound() {
		testInput("cat doesnt-exist.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND
				+ "At least one of the files in the command [cat doesnt-exist.txt] was not found.\n");
	}

	@Test
	public void testDirectoryNotFound() {
		testInput("cd mystery-dir\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.DIRECTORY_NOT_FOUND.with_parameter("cd mystery-dir"));
	}

	// ********** Input/Output Tests **********

	@Test
	public void testPwdCannotHaveInput() {
		testInput("cat hello-world.txt | pwd\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.CANNOT_HAVE_INPUT.with_parameter("pwd"));
	}

	@Test
	public void testLsCannotHaveInput() {
		testInput("cat hello-world.txt | ls\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.CANNOT_HAVE_INPUT.with_parameter("ls"));
	}

	@Test
	public void testCdCannotHaveInput() {
		testInput("cat hello-world.txt | cd dir1\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.CANNOT_HAVE_INPUT.with_parameter("cd dir1"));
	}

	@Test
	public void testCdCannotHaveOutput1() {
		testInput("cd dir1\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND.toString());
	}

	@Test
	public void testCdCannotHaveOutput2() {
		testInput("cd dir1 | wc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.CANNOT_HAVE_OUTPUT.with_parameter("cd dir1"));
	}

	@Test
	public void testCdRequiresParameter() {
		testInput("cd\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_PARAMETER.with_parameter("cd"));
	}

	@Test
	public void testCatCannotHaveInput() {
		testInput("pwd | cat hello-world.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.CANNOT_HAVE_INPUT.with_parameter("cat hello-world.txt"));
	}

	@Test
	public void testCatRequiresParameter1() {
		testInput("cat\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_PARAMETER.with_parameter("cat"));
	}

	@Test
	public void testCatFileNotFound() {
		testInput("cat iloveos-hello-world.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.FILE_NOT_FOUND.with_parameter("cat iloveos-hello-world.txt"));
	}

	@Test
	public void testGrepRequiresInput() {
		testInput("grep hahaha\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("grep hahaha"));
	}

	@Test
	public void testGrepRequiresParameter() {
		testInput("pwd | grep\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_PARAMETER.with_parameter("grep"));
	}

	@Test
	public void testWcRequiresInput() {
		testInput("wc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("wc"));
	}

	@Test
	public void testRedirectionRequiresInput() {
		testInput("> new-hello-world.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("> new-hello-world.txt"));
	}

	@Test
	public void testRedirectionRequiresParameter() {
		testInput("ls >\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_PARAMETER.with_parameter(">"));
	}

	@Test
	public void testRedirectionNoOutput1() {
		testInput("cat hello-world.txt > new-hello-world.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND.toString());
		AllConcurrentTests.destroyFile("new-hello-world.txt");
	}

	@Test
	public void testRedirectionNoOutput2() {
		testInput("cat hello-world.txt > new-hello-world.txt|wc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(
				Message.NEWCOMMAND.toString() + Message.CANNOT_HAVE_OUTPUT.with_parameter("> new-hello-world.txt"));
		AllConcurrentTests.destroyFile("new-hello-world.txt");
	}

	// New tests Fall 2021

	/**
	 * Tests that cat works properly when users has cd into a directory. For
	 * example, if user cd's into dir1 then cat should handle filepaths relative to
	 * dir1. That is, if there's a file f1.txt in dir1, then cat f1.txt should
	 * produce the output of f1.txt. This is like
	 * {@link RedirectionTests#testDirectoryShiftedRedirection()}.
	 */
	@Test
	public void testCatInDirectory() {
		testInput("cd dir1\ncat f1.txt\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + "" + Message.NEWCOMMAND
				+ "FILE 1\nTHIS IS THE FIRST FILE.\nI HOPE YOU LIKE IT\n\n\nYOU DO????\n");
	}

	/**
	 * Tests that user reports the appropriate error when you try to call head
	 * without any input
	 * 
	 * @author Chami Lamelas
	 */
	@Test
	public void testHeadRequiresInput() {
		testInput("head\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("head"));
	}

	/**
	 * Tests that user reports the appropriate error when you try to call tail
	 * without any input
	 */
	@Test
	public void testTailRequiresInput() {
		testInput("tail\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("tail"));
	}

	/**
	 * Tests that user reports the appropriate error when you try to call uniq
	 * without any input
	 */
	@Test
	public void testUniqRequiresInput() {
		testInput("uniq\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("uniq"));
	}

	/**
	 * Tests that user reports the appropriate error when you try to call cc without
	 * any input.
	 */
	@Test
	public void testCCRequiresInput() {
		testInput("cc 3\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_INPUT.with_parameter("cc 3"));
	}

	/**
	 * Tests that user reports the appropriate error when you try to call cc without
	 * a parameter.
	 */
	@Test
	public void testCCRequiresParameter() {
		testInput("cat hello-world.txt | cc\nexit");
		ConcurrentREPL.main(null);
		assertOutput(Message.NEWCOMMAND + Message.REQUIRES_PARAMETER.with_parameter("cc"));
	}

}
