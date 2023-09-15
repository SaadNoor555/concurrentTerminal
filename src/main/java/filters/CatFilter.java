package filters;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import misc.CurrentWorkingDirectory;
import misc.Filter;
import misc.Message;

/**
 * Implements cat command - includes parsing cat command, detecting if input
 * filter was linked, as well as overriding necessary behavior of
 * SequentialFilter.
 *
 */
public class CatFilter extends ConcurrentFilter {

	/**
	 * file to be read
	 */
	private File file;

	/**
	 * command that was used to construct this filter
	 */
	private String command;

	/**
	 * Constructs a CatFilter given a cat command.
	 * 
	 * @param cmd cmd is guaranteed to either be "cat" or "cat" followed by a space.
	 * @throws InvalidCommandException if the file in the command cannot be found
	 *                                  or if a file parameter was not provided
	 */
	public CatFilter(String cmd) {
		super();

		// save command as a field, we need it when we throw an exception in
		// setPrevFilter
		command = cmd;

		// find index of space, if there isn't a space that means we got just "cat" =>
		// cat needs a parameter so throw IAE with the appropriate message
		int spaceIdx = cmd.indexOf(" ");
		if (spaceIdx == -1) {
			throw new InvalidCommandException(Message.REQUIRES_PARAMETER.with_parameter(cmd));
		}

		// we have a space, filename will be trimmed string after space
		String dest = cmd.substring(spaceIdx + 1).trim();

		// create a File with the path to the file from the current working directory
		// since we interpret dest as a relative path
		file = new File(CurrentWorkingDirectory.get() + CurrentWorkingDirectory.getPathSeparator() + dest);

		// if this is not a valid File, throw an IAE with the appropriate message
		if (!file.isFile()) {
			throw new InvalidCommandException(Message.FILE_NOT_FOUND.with_parameter(cmd));
		}
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides ConcurrentFilter.processLine() - doesn't do anything.
	 */
	@Override
	protected String processLine(String line) {
		return null;
	}

	/**
	 * Overrides Concurrent.setPrevFilter() to not allow a {@link Filter} to
	 * be placed before {@link CatFilter} objects.
	 * 
	 * @throws InvalidCommandException - always
	 */
	@Override
	public void setPrevFilter(Filter prevFilter) {

		// as specified in the PDF throw an IAE with the appropriate message if we try
		// to link a Filter before this one (since cat doesn't take input)
		throw new InvalidCommandException(Message.CANNOT_HAVE_INPUT.with_parameter(command));

	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		/*
		 * wait until the output pipe for this filter has been initialized.
		 * otherwise we cannot write anything into the output pipe
		 */
		while(this.output==null);
		/* 
		 * open a Scanner on the File and read it line by line adding each line to the
		 * output message queue 
		 */
		Scanner s = null;
		try {
			s = new Scanner(file);
			while (s.hasNextLine()) {
				output.write(s.nextLine());
			}
		} catch (FileNotFoundException e) {
		} finally {
			if (s != null) {
				s.close();
			}
		}
		/*
		 * when all the work is done, the filter can write poison pill, 
		 * indicating the next filter that it is done producing outputs
		 */
		try {
			output.writePoisonPill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
