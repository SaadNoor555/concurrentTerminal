package filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import misc.Message;

/**
 * The main implementation of the REPL loop (read-eval-print loop). It reads
 * commands from the user, parses them, executes them and displays the result.
 */
public class ConcurrentREPL {
	private static class Running {
		private int serial;
		private String command;
		private ConcurrentFilter lastFilter;
		public Running(String cmd,  ConcurrentFilter filter, int count) {
			this.command = cmd;
			this.lastFilter = filter;
			this.serial = count;
		}
		/*
		 * return details of a background process for repl_jobs command
		 */
		private String details() {
			return "\t"+this.serial+". "+this.command;
		}
		private int getSerial() {
			return this.serial;
		}
		private ConcurrentFilter getLastFilter() {
			return this.lastFilter;
		}
	}
	
	/**
	 * pipe string
	 */
	static final String PIPE = "|";

	/**
	 * redirect string
	 */
	static final String REDIRECT = ">";

	/**
	 * The main method that will execute the REPL loop
	 * 
	 * @param args not used
	 */
	public static void main(String[] args) {
		int cnt = 1;
		/*
		 * ArrayList of Running objects, which contains all the jobs that 
		 * were set to run in background
		 */
		ArrayList<Running> bg_commands = new ArrayList();
		Scanner consoleReader = new Scanner(System.in);
		System.out.print(Message.WELCOME);

		while (true) {
			System.out.print(Message.NEWCOMMAND);

			// read user command, if its just whitespace, skip to next command
			String cmd = consoleReader.nextLine();
			if (cmd.trim().isEmpty()) {
				continue;
			}

			// exit the REPL if user specifies it
			if (cmd.trim().equals("exit")) {
				break;
			}
			
			if(cmd.trim().equals("repl_jobs")) {
				for(Running tmp : bg_commands) {
					if(tmp.getLastFilter().output.isEmpty())
						System.out.println(tmp.details());
				}
				continue;
			}
			if(cmd.trim().split(" ")[0].equals("kill")) {
				int process_no = Integer.parseInt(cmd.trim().split(" ")[1]);
				for(Running tmp : bg_commands) {
					if(tmp.getSerial()==process_no) {
						try {
							tmp.getLastFilter().output.writePoisonPill();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}
				continue;
			}

			try {
				// parse command into sub commands, then into Filters, add final PrintFilter if
				// necessary, and link them together - this can throw IAE so surround in
				// try-catch so appropriate Message is printed (will be the message of the IAE)
				/*
				 * We are not invoking process method for each filter to get them to start working
				 * When the filters are created, they start doing their job instantly so no need for invoking process method
				 */
				List<ConcurrentFilter> filters = ConcurrentCommandBuilder.createFiltersFromCommand(cmd);
				
				/*
				 * checking whether the command is supposed to run in background or foreground
				 * if it is not supposed to run in background, then wait for the last filter to
				 * write poison pill into its output pipe, indicating it has finished its job 
				 * and then we proceed to taking the next command.
				 */
				if(cmd.trim().charAt(cmd.trim().length()-1)!='&') {
					/*
					 * we might face an exception trying to get the last filter so wrapping it in try catch
					 */
					try {
						/*
						 * we will wait until the last filter's output filter has an element in it.
						 * Since the last filters can only be filters that do not write anything into
						 * the output pipe, only thing they will be writing are poison pills
						 * so when output pipe size is not 0, we can say for sure the poison pill
						 * has been written and we can proceed to taking the next command
						 */
						while(filters.get(filters.size()-1).output.size()==0);
					}
					catch(Exception e) {
						/*
						 * An exception will be thrown if the filters array was empty
						 * In that case, we know it was CDFilter with runs sequentially so
						 * it is already done and we can continue execution
						 */
					}
				}
				else {
					bg_commands.add(new Running(cmd, filters.get(filters.size()-1), cnt++));
				}
			} catch (InvalidCommandException e) {
				System.out.print(e.getMessage());
			}
		}
		System.out.print(Message.GOODBYE);
		consoleReader.close();

	}

}
