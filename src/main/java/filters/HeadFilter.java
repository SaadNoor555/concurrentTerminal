package filters;


/**
 * Implements head command - overrides necessary behavior of ConcurrentFilter

 */
public class HeadFilter extends ConcurrentFilter {

	/**
	 * number of lines read so far
	 */
	private int numRead;

	/**
	 * number of lines passed to output via head
	 */
	private static int LIMIT = 10;

	/**
	 * Constructs a head filter.
	 */
	public HeadFilter() {
		super();
		numRead = 0;
		
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		this.t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides ConcurrentFilter.processLine() - doesn't do anything.
	 */
	@Override
	protected String processLine(String line) {
		return null;
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while(this.input==null);
		
		/*
		 * write first 10 lines from input pipe to output pipe
		 */
		while (numRead < LIMIT) {
			String line;
			try {
				line = input.readAndWait();
				/*
				 * if  line==null, it  means the previous filter does not have any 
				 * more strings to write into output pipe so there is no more input
				 * string remaining
				 */
				if(line==null) {
					break;
				}
				output.write(line);
				numRead++;
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
