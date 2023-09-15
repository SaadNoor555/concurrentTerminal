package filters;

import misc.Message;

/**
 * Implements grep command - includes parsing grep command by overriding
 * necessary behavior of SequentialFilter.

 */
public class GrepFilter extends ConcurrentFilter {

	/**
	 * holds the grep query
	 */
	private String query;

	/**
	 * constructs GrepFilter given grep command
	 * 
	 * @param cmd cmd is guaranteed to either be "grep" or "grep" followed by a
	 *            space.
	 * @throws InvalidCommandException if query parameter was not provided
	 */
	public GrepFilter(String cmd) {

		// find index of space, if there isn't a space that means we got just "grep" =>
		// grep needs a parameter so throw IAE with the appropriate message
		int spaceIdx = cmd.indexOf(" ");
		if (spaceIdx == -1) {
			throw new InvalidCommandException(Message.REQUIRES_PARAMETER.with_parameter(cmd));
		}

		// we have a space, query will be trimmed string after space
		query = cmd.substring(spaceIdx + 1).trim();
		
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides  ConcurrentFilter.processLine() - only returns lines to
	 * {@link ConcurrentFilter#process()} that contain the query parameter specified
	 * in the command passed to the constructor.
	 */
	@Override
	protected String processLine(String line) {

		// only have SequentialFilter:process() add lines to the output queue that
		// include the query string
//		System.out.println(line+" "+query);
		if (line.contains(query)) {
			return line;
		}

		return null;
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while(input==null);
		/* 
		 * when input pipe is available, start reading its contents
		 * will stop reading from input when the null string in input pipe
		 * is found. This indicates that the previous filter has written
		 * poison pill, indicating there will be no more input string
		 */
		while(true) {
			try {
				String tmp = this.input.readAndWait();
				/* 
				 * if the line read from input pipe is null, this indicates last filter has finished generating  
				 * all the outputs and there is no input remaining for this filter so we break the loop
				 */
				if(tmp==null) break;		
				/*
				 * each line from input goes through processLine() method which matches the it to query
				 */
				tmp = processLine(tmp);
				/* 
				 * if the processLine method returns null, this means query was not found in input string
				 * So we cannot write them into output  pipe
				 */
				if(tmp!=null)
					output.write(tmp);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		/*
		 * when all the work is done, the filter can write poison pill, 
		 * indicating the next filter that it is done producing outputs
		 */
		try {
			output.writePoisonPill();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
