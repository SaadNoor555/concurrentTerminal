package filters;


/**
 * Implements wc command - overrides necessary behavior of ConcurrentFilter
 *
 */
public class WordCountFilter extends ConcurrentFilter {

	/**
	 * word count in input - words are strings separated by space in the input
	 */
	private int wordCount;

	/**
	 * character count in input - includes ws
	 */
	private int charCount;

	/**
	 * line count in input
	 */
	private int lineCount;

	/**
	 * Constructs a wc filter.
	 */
	public WordCountFilter() {
		super();
		wordCount = 0;
		charCount = 0;
		lineCount = 0;
		
		/*
		 * Initiating a thread for cat filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	/**
	 * Overrides {@link ConcurrentFilter#process()} by computing the word count,
	 * line count, and character count then adding the string with line count + " "
	 * + word count + " " + character count to the output queue
	 */
	@Override
	public void process() {
		while (!input.isEmpty()) {
			String line = input.read();
			processLine(line);
		}
		output.write(lineCount + " " + wordCount + " " + charCount);
	}

	/**
	 * Overrides ConcurrentFilter.processLine() - updates the line, word, and
	 * character counts from the current input line
	 */
	@Override
	protected String processLine(String line) {
		lineCount++;
		wordCount += line.split(" ").length;
		charCount += line.length();
		return null;
	}

	@Override
	public void run() {
		/*
		 * waiting until input pipe for the filter is available
		 */
		while(input==null);
		/*
		 * reading lines from input pipe as it becomes available and passing it
		 * through the process line method, which counts words, chars and lines in the string
		 * when the read line is null, all input has been processed, so we can break the loop
		 */
		while (true) {
			String line;
			try {
				line = input.readAndWait();
				if(line==null) break;
				processLine(line);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		/*
		 * writing the results into the output pipe
		 */
		output.write(lineCount + " " + wordCount + " " + charCount);
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
