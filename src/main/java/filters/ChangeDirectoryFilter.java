package filters;

import java.io.File;

import misc.CurrentWorkingDirectory;
import misc.Filter;
import misc.Message;

/**
 * Implements cd command - includes parsing cd command, detecting if input or
 * output filter was linked, as well as overriding necessary behavior of
 * SequentialFilter.
 *
 */
public class ChangeDirectoryFilter extends ConcurrentFilter {

	/**
	 * absolute path to directory cd will cause cwd to be changed to
	 */
	private String dest;

	/**
	 * command that was used to construct this filter
	 */
	private String command;

	/**
	 * Constructs a ChangeDirectoryFilter given a cd command.
	 * 
	 * @param cmd cmd is guaranteed to either be "cd" or "cd" followed by a space.
	 * @throws InvalidCommandException if the directory in the command cannot be
	 *                                  found or if a directory parameter was not
	 *                                  provided
	 */
	public ChangeDirectoryFilter(String cmd) {
		super();

		// save command as a field, we need it when we throw an exception in
		// setPrevFilter and setNextFilter
		command = cmd;

		// find index of space, if there isn't a space that means we got just "cd" =>
		// cd needs a parameter so throw IAE with the appropriate message
		int spaceIdx = cmd.indexOf(" ");
		if (spaceIdx == -1) {
			throw new InvalidCommandException(Message.REQUIRES_PARAMETER.with_parameter(cmd));
		}

		// we have a space, directory will be trimmed string after space
		String relativeDest = cmd.substring(spaceIdx + 1).trim();

		// if we have a non-special destination directory, append it to cwd and set it
		// to dest
		if (!relativeDest.equals(".") && !relativeDest.equals("..")) {
			dest = CurrentWorkingDirectory.get() + CurrentWorkingDirectory.getPathSeparator() + relativeDest;

			// make sure that this is a valid directory, if not throw appropriate IAE
			File destFile = new File(dest);
			if (!destFile.isDirectory()) {
				throw new InvalidCommandException(Message.DIRECTORY_NOT_FOUND.with_parameter(cmd));
			}

			// if specified relative destination is . or .., just set that as dest
		} else {
			dest = relativeDest;
		}
		/*
		 * change the directory using process method.
		 */
		process();

	}

	/**
	 * Overrides ConcurrentFilter.processLine() - doesn't do anything.
	 */
	@Override
	protected String processLine(String line) {
		return null;
	}

	/**
	 * Overrides {@link ConcurrentFilter#process()} to change
	 * {@link ConcurrentREPL#currentWorkingDirectory} based on command passed to
	 * constructor.
	 */
	@Override
	public void process() {

		// if .., then find parent of the cwd
		if (dest.equals("..")) {
			String parent = new File(CurrentWorkingDirectory.get()).getParent();

			// if a parent exists, change cwd to parent, else leave it (this handles case if
			// user keeps doing cd .., cd .., etc this will eventually just stop them in the
			// directory that has no root (C:/) on windows)
			if (parent != null) {
				CurrentWorkingDirectory.setTo(parent);
			}

			// if a relative dest was specified and included in dest in constructor, change
			// cwd, otherwise (user did cd .) leave cwd as is
		} else if (!dest.equals(".")) {
			CurrentWorkingDirectory.setTo(dest);
		}

	}

	/**
	 * Overrides ConcurrentFilter.setPrevFilter() to not allow a {@link Filter} to
	 * be placed before {@link ChangeDirectoryFilter} objects.
	 * 
	 * @throws InvalidCommandException - always
	 */
	@Override
	public void setPrevFilter(Filter prevFilter) {
		// as specified in the PDF throw an IAE with the appropriate message if we try
		// to link a Filter before this one (since cd doesn't take input)
		throw new InvalidCommandException(Message.CANNOT_HAVE_INPUT.with_parameter(command));
	}

	/**
	 * Overrides Concurrent.setNextFilter() to not allow a {@link Filter} to
	 * be placed after {@link ChangeDirectoryFilter} objects.
	 * 
	 * @throws InvalidCommandException - always
	 */
	@Override
	public void setNextFilter(Filter nextFilter) {
		// as specified in the PDF throw an IAE with the appropriate message if we try
		// to link a Filter after this one (since cd doesn't make output)
		throw new InvalidCommandException(Message.CANNOT_HAVE_OUTPUT.with_parameter(command));
	}

	@Override
	public void run() {
		/*
		 * changeDirectoryFiler will never be paired with another filter in a command as it does
		 * not take any input neither produces any output. So it doesn't have any reason to run
		 * concurrently as it won't have any other filter next to it to run concurrently with.
		 * That is why ChangeDirectoryFilter is working sequentially. And run method is empty
		 */
		
	}

}
