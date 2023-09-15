package filters;

import misc.Message;

/**
 * Implements cc command - includes parsing cc command by overriding
 * necessary behavior of SequentialFilter.
 *
 */
public class CaesarCipherFilter extends ConcurrentFilter {

	private int key;

	public CaesarCipherFilter(String cmd) {
		super();

		// find index of space, if there isn't a space that means we got just "cc" =>
		// cc needs a parameter so throw IAE with the appropriate message
		int spaceIdx = cmd.indexOf(" ");
		if (spaceIdx == -1) {
			throw new InvalidCommandException(Message.REQUIRES_PARAMETER.with_parameter(cmd));
		}

		// we have a space, key will be trimmed int after space
		key = Integer.parseInt(cmd.substring(spaceIdx + 1).trim());
		
		/*
		 * Initiating a thread for cc filter
		 * and invoking its start method
		 */
		t = new Thread(this);
		t.start();
	}

	@Override
	protected String processLine(String line) {
		char[] output = new char[line.length()];

		for (int i = 0; i < line.length(); i++) {

			// Whether or not the current character is A-Z
			boolean isUpper = false;
			char c = line.charAt(i);

			// If c is upper case, marks that we've seen it and set c to lower case
			// (simplifies logic later)
			if (Character.isUpperCase(c)) {
				isUpper = true;
				c = Character.toLowerCase(c);
			}

			// If lower case version of character is not in a-z (that means c isn't in a-z
			// or A-Z). Thus, we keep unchanged version of character in output.
			if (c < 'a' || c > 'z') {
				output[i] = c;
				continue;
			}

			// At this point, c is in a-z. Map c to 0-25. Perform the shift to get changed c
			// in 0-25. Map changed character back to a-z.
			c = (char) ((((c - 'a') + key) % 26) + 'a');

			// If character was A-Z (and shifted above to something else in A-Z), convert it
			// back from a-z before storing in the output.
			if (isUpper) {
				c = Character.toUpperCase(c);
			}
			output[i] = c;
		}

		return new String(output);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		/*
		 * waiting until input pipe for the filter is available
		 */
		while(this.input==null);
		/* 
		 * when input pipe is available, start reading its contents
		 * will stop reading from input when the null string in input pipe
		 * is found. This indicates that the previous filter has written
		 * poison pill, indicating there will be no more input string
		 */
		while(true) {
			try {
				String tmp = this.input.readAndWait();
				if(tmp==null) break;		// tmp==null means the poison  pill is found and there is no more input left
				this.output.write(this.processLine(tmp));		// when an input string is read, process it and write it into the output pipe
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
