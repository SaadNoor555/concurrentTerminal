package filters;

import java.util.LinkedList;
import java.util.List;

/**
 * Implements tail command - overrides necessary behavior of ConcurrentFilter
 */
public class TailFilter extends ConcurrentFilter {

	/**
	 * number of lines passed to output via tail
	 */
	private static int LIMIT = 10;

	/**
	 * line buffer
	 */
	private List<String> buf;

	/**
	 * Constructs a tail filter.
	 */
	public TailFilter() {
		super();
		buf = new LinkedList<String>();
		
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
		 * reading each line from input pipe as it becomes available and adding it to the buffer
		 * when buffer size exceeds 10, removing the first string from it
		 * when the read line==null, stopping as there is no input left
		 */
		while(true) {
			try {
				String line = this.input.readAndWait();
				if(line==null) break;
				buf.add(line);
				if(buf.size()>LIMIT) {
					buf.remove(0);
				}
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		// once we're done with the input (and have identified the last 10 lines), add
		// them to the output in the order in which they appeared in the input
		while (!buf.isEmpty()) {
			output.write(buf.remove(0));
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
