package filters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import misc.CurrentWorkingDirectory;
import misc.Filter;
import misc.Message;

/**
 * Implements redirection as a {@link ConcurrentFilter} - overrides necessary
 * behavior of ConcurrentFilter
 *
 */
public class RedirectFilter extends ConcurrentFilter {

	/**
	 * destination of redirection
	 */
	private String dest;

	/**
	 * command that was used to construct this filter
	 */
	private String command;

	/**
	 * writer for writing - set in process(), leave as null till then
	 */
	private PrintWriter printWriter;

	/**
	 * Constructs a RedirectFilter given a >.
	 * 
	 * @param cmd cmd is guaranteed to either be ">" or ">" followed by a space.
	 * @throws InvalidCommandException if a file parameter was not provided
	 */
	public RedirectFilter(String cmd) {
		super();

		// save command as a field, we need it when we throw an exception in
		// setNextFilter
		command = cmd;
		output = new ConcurrentPipe();
		// find index of space, if there isn't a space that means we got just ">" =>
		// > needs a parameter so throw IAE with the appropriate message
		int spaceIdx = cmd.indexOf(" ");
		if (spaceIdx == -1) {
			throw new InvalidCommandException(Message.REQUIRES_PARAMETER.with_parameter(cmd));
		}

		// we have a space, filename will be trimmed string after space
		String relativeDest = cmd.substring(spaceIdx + 1).trim();
		/*
		 * if last 2 char of  the file name is " &", they mean this command will run in background and these should not
		 * be in the file name so remove them from the name
		 */
		if(relativeDest.substring(relativeDest.length()-2, relativeDest.length()).equals(" &"))
			relativeDest = relativeDest.substring(0, relativeDest.length()-2);

		// set redirection destination as cwd joined with relative destination file
		dest = CurrentWorkingDirectory.get() + CurrentWorkingDirectory.getPathSeparator() + relativeDest;

		// check if the destination file exists, if so delete it b/c > overwrites the
		// destination file if one exists
		File destFile = new File(dest);
		if (destFile.isFile()) {
			destFile.delete();
		}
		
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides ConcurrentFilter.processLine() to just write the line to the
	 * destination file. Returns null so {@link ConcurrentFilter#process()} doesn't
	 * add anything to the output.
	 */
	@Override
	protected String processLine(String line) {
		printWriter.println(line);
		return null;
	}

	/**
	 * Overrides ConcurrentFilter.setPrevFilter() to not allow a {@link Filter} to
	 * be placed after {@link RedirectFilter} objects.
	 * 
	 * @throws InvalidCommandException - always
	 */
	@Override
	public void setNextFilter(Filter nextFilter) {
		throw new InvalidCommandException(Message.CANNOT_HAVE_OUTPUT.with_parameter(command));
	}

	@Override
	public void run() {
		/*
		 * opening the file given in parameter
		 */
		try {
			printWriter = new PrintWriter(new FileWriter(new File(dest)));
		} catch (IOException e) {
			// do nothing we know file exists
		}
		
		/*
		 * waiting until the input pipe is available
		 */
		while(this.input==null);
		
		/* 
		 * waiting and reading lines from input pipe and writing them into 
		 * the opened file. if line==null, all the input lines have been written.
		 */
		while(true) {
			try {
				String tmp = this.input.readAndWait();
				if(tmp==null)break;
				printWriter.println(tmp);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// closing the file
		printWriter.close();
		
		/*
		 * when everything is done, poison pill is written into the output pipe to 
		 * let concurrentREPL know it's finished its work.
		 */
		try {
			output.writePoisonPill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
