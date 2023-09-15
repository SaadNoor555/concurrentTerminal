package filters;

import misc.CurrentWorkingDirectory;
import misc.Filter;
import misc.Message;

/**
 * Implements pwd command - overrides necessary behavior of ConcurrentFilter
 *
 */
public class WorkingDirectoryFilter extends ConcurrentFilter {

	/**
	 * command that was used to construct this filter
	 */
	private String command;

	/**
	 * Constructs a pwd filter.
	 * @param cmd cmd is guaranteed to either be "pwd" or "pwd" surrounded by whitespace
	 */
	public WorkingDirectoryFilter(String cmd) {
		super();
		command = cmd;
//		output = new ConcurrentPipe();
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides {@link ConcurrentFilter#process()} by adding
	 * {@link SequentialREPL#currentWorkingDirectory} to the output queue
	 */
	@Override
	public void process() {
		this.output.write(CurrentWorkingDirectory.get());
	}

	/**
	 * Overrides ConcurrentFilter.processLine() - doesn't do anything.
	 */
	@Override
	protected String processLine(String line) {
		return null;
	}

	/**
	 * Overrides equentialFilter.setPrevFilter() to not allow a {@link Filter} to be
	 * placed before {@link WorkingDirectoryFilter} objects.
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
		 * waiting until output pipe for the filter is available,
		 * otherwise we cannot write into it
		 */
		while(output==null);
		
		/*
		 * invoking process method, which has been overwritten to write current directory into output pipe
		 */
		process();
		
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
