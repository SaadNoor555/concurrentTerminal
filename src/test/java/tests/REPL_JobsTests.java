package tests;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import filters.ConcurrentREPL;
import misc.CurrentWorkingDirectory;
import misc.Message;

/**
 * Tests for repl_jobs/kill. Moved from REPLTests as these tests should not have
 * a short timeout.
 */
public class REPL_JobsTests {

	// adjust if TA computer is slow (seconds)
	private static final int TIMEOUT = 30;

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

	// *** repl_jobs tests ***
	@Test
	public void testREPLJobs() {
		testInput("cat fizz-buzz-10000.txt | grep Fi | wc > replTest1.txt &\nrepl_jobs\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | wc > replTest3.txt\nrepl_jobs\nexit");
		ConcurrentREPL.main(null);
		String result = outContent.toString().replace("\r", "");
		assertEquals(Message.WELCOME.toString() + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ "\t1. cat fizz-buzz-10000.txt | grep Fi | wc > replTest1.txt &\n" + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + Message.NEWCOMMAND + Message.GOODBYE, result);
		assertTrue((new File("replTest1.txt")).exists());
	}

	@Test
	public void testREPLMultipleJobs() {
		testInput("cat fizz-buzz-1500000.txt | grep Fi | wc > replTest1.txt &\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | wc > replTest2.txt &\n" + "repl_jobs\nexit");
		ConcurrentREPL.main(null);
		String result = outContent.toString().replace("\r", "");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		try {
			assertEquals(Message.WELCOME.toString() + Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
					+ "\t1. cat fizz-buzz-1500000.txt | grep Fi | wc > replTest1.txt &\n"
					+ "\t2. cat fizz-buzz-1500000.txt | grep Fi | wc > replTest2.txt &\n" + Message.NEWCOMMAND
					+ Message.GOODBYE, result);
		} catch (AssertionError e) {
			assertEquals(Message.WELCOME.toString() + Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
					+ "\t1. cat fizz-buzz-1500000.txt | grep Fi | wc > replTest2.txt &\n"
					+ "\t2. cat fizz-buzz-1500000.txt | grep Fi | wc > replTest1.txt &\n" + Message.NEWCOMMAND
					+ Message.GOODBYE, result);
		}
		assertTrue((new File("replTest1.txt")).exists());
		assertTrue((new File("replTest2.txt")).exists());
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	@Test
	public void testKill() {
		testInput("cat fizz-buzz-1500000.txt | grep Fi | wc > killTest1.txt &\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | wc > killTest2.txt &\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | wc > killTest3.txt &\n" + "kill 2\n"
				+ "cat fizz-buzz-10000.txt | grep waittt\n" + "repl_jobs\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | wc > killTest4.txt\n" + "exit");
		ConcurrentREPL.main(null);
		String result = outContent.toString().replace("\r", "");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(Message.WELCOME.toString() + Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ "\t1. cat fizz-buzz-1500000.txt | grep Fi | wc > killTest1.txt &\n"
				+ "\t3. cat fizz-buzz-1500000.txt | grep Fi | wc > killTest3.txt &\n" + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + Message.GOODBYE, result);
		assertTrue((new File("killTest1.txt")).exists());
		assertTrue((new File("killTest2.txt")).exists());
		assertTrue((new File("killTest3.txt")).exists());
		assertTrue((new File("killTest4.txt")).exists());
		File f = new File("killTest2.txt");
		if (f.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(f));
				int cnt = 0;
				while (isr.ready()) {
					isr.read();
					cnt++;
				}
				isr.close();
				if (cnt > 0)
					assertTrue(false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				assertTrue(false);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				assertTrue(false);
			}
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
	}

	/**
	 * Tests the behavior of commands involving tail, similar to testKill.
	 */
	@Test
	public void testTail() {
		testInput("cat fizz-buzz-1500000.txt | grep Fi | tail > killTest5.txt &\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | tail > killTest6.txt &\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | tail > killTest7.txt &\n" + "kill 2\n"
				+ "cat fizz-buzz-10000.txt | grep waittt\n" + "repl_jobs\n"
				+ "cat fizz-buzz-1500000.txt | grep Fi | tail > killTest8.txt\n" + "exit");
		ConcurrentREPL.main(null);
		String result = outContent.toString().replace("\r", "");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		assertEquals(Message.WELCOME.toString() + Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + Message.NEWCOMMAND + Message.NEWCOMMAND
				+ "\t1. cat fizz-buzz-1500000.txt | grep Fi | tail > killTest5.txt &\n"
				+ "\t3. cat fizz-buzz-1500000.txt | grep Fi | tail > killTest7.txt &\n" + Message.NEWCOMMAND
				+ Message.NEWCOMMAND + Message.GOODBYE, result);
		assertTrue((new File("killTest5.txt")).exists());
		assertTrue((new File("killTest6.txt")).exists());
		assertTrue((new File("killTest7.txt")).exists());
		assertTrue((new File("killTest8.txt")).exists());
		File f = new File("killTest6.txt");
		if (f.exists()) {
			try {
				InputStreamReader isr = new InputStreamReader(new FileInputStream(f));
				int cnt = 0;
				while (isr.ready()) {
					isr.read();
					cnt++;
				}
				isr.close();
				if (cnt > 0)
					assertTrue(false);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				assertTrue(false);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				assertTrue(false);
			}
		}
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ie) {
			ie.printStackTrace();
		}
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
