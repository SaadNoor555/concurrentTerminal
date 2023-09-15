package filters;

/**
 * Implements printing as a {@link ConcurrentFilter} - overrides necessary
 * behavior of ConcurrentFilter
 *
 */
public class PrintFilter extends ConcurrentFilter {
	public PrintFilter() {
		this.output = new ConcurrentPipe();
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		this.t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides ConcurrentFilter.processLine() to just print the line to stdout.
	 */
	@Override
	protected String processLine(String line) {

		System.out.print(line);
		
		return null;
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while (this.input == null);
		/*
		 * reading each line from input pipe as it becomes available
		 * when read line equals null, all inputs have been read, as
		 * the previous filter has finished producing more outputs
		 * storing each line in a String s
		 */
		String s = "";
		do {
			String tmp;
			try {
				tmp = input.readAndWait();
				if(tmp==null) {
					break;
				}
				s+= tmp;
				s+="\n";
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		} while(true);
		// printing the string s using processLine method, which is overwridden to print the sting in parameter.
		processLine(s);
		
		/*
		 * once printing is done, printfilter has done it's job.
		 * so it will write poison pill to let ConcurrentREPL that it
		 * is done.
		 */
		try {
			output.writePoisonPill();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
