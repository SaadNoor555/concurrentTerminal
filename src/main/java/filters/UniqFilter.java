package filters;

/**
 * Implements uniq command - overrides necessary behavior of ConcurrentFilter
 *
 */
public class UniqFilter extends ConcurrentFilter {

	/**
	 * Stores previous line
	 */
	private String prevLine;

	/**
	 * Constructs a uniq filter.
	 */
	public UniqFilter() {
		super();
		prevLine = null;
		
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides ConcurrentFilter.processLine() - only returns lines to
	 * {@link ConcurrentFilter#process()} that are not repetitions of the previous
	 * line.
	 */
	@Override
	protected String processLine(String line) {
		String output = null;
		if (prevLine == null || !prevLine.equals(line)) {
			output = line;
		}

		prevLine = line;
		return output;
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while(input==null);
		
		/*
		 * reading each line from input pipe as it becomes available
		 * and processing the line using processLine method which 
		 * has been overwritten to match specifications.
		 * if the read line from input pipe is null, then all input has been read
		 * and we can break the loop
		 */
		while(true) {
			try {
				String temp = input.readAndWait();
				if(temp==null) break;
				temp = processLine(temp);
				/*
				 * if processLine returns null, that means the string was same as the previous one
				 * so we cannot write it into the output pipe
				 */
				if(temp!=null) {
					output.write(temp);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		/*
		 * when all the work is done, the filter can write poison pill, 
		 * indicating the next filter that it is done producing outputs
		 */
		try {
			this.output.writePoisonPill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
