package filters;

import java.io.File;

import misc.CurrentWorkingDirectory;
import misc.Filter;
import misc.Message;

/**
 * Implements ls command - overrides necessary behavior of ConcurrentFilter
 *
 */
public class ListFilter extends ConcurrentFilter {

	/**
	 * command that was used to construct this filter
	 */
	private String command;

	/**
	 * Constructs an ListFilter from an exit command
	 * 
	 * @param cmd - exit command, will be "ls" or "ls" surrounded by whitespace
	 */
	public ListFilter(String cmd) {
		super();
		command = cmd;
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this, "ListFilter");
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
	 * Overrides ConcurrentFilter.setPrevFilter() to not allow a
	 * {@link Filter} to be placed before {@link ListFilter} objects.
	 * 
	 * @throws InvalidCommandException - always
	 */
	@Override
	public void setPrevFilter(Filter prevFilter) {
		throw new InvalidCommandException(Message.CANNOT_HAVE_INPUT.with_parameter(command));
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while (this.output == null);
		
		/*
		 * getting current directory contents and writing them into output pipe
		 */
		File cwd = new File(CurrentWorkingDirectory.get());
		File[] files = cwd.listFiles();
		for (File f : files) {
			this.output.write(f.getName());
		}
		/*
		 * when all the work is done, the filter can write poison pill, 
		 * indicating the next filter that it is done producing outputs
		 */
		try {
			
			this.output.writePoisonPill();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
