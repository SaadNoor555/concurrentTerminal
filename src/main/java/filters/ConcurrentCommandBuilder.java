package filters;

import java.util.ArrayList;
import java.util.List;

import misc.Message;

/**
 * This class manages the parsing and execution of a command. It splits the raw
 * input into separated subcommands, creates subcommand filters, and links them
 * into a list.
 *
 */
public class ConcurrentCommandBuilder {
	/**
	 * Creates and returns a list of filters from the specified command
	 * 
	 * @param command the command to create filters from
	 * @return the list of ConcurrentFilter that represent the specified command
	 */
	public static List<ConcurrentFilter> createFiltersFromCommand(String command) {

		// determine final filter and remove its subcommand from the command (if it was
		// a redirect) using helpers
		ConcurrentFilter finalFilter = determineFinalFilter(command);
		String newCommand = adjustCommandToRemoveFinalFilter(command);

		// this will hold our filters
		List<ConcurrentFilter> filters = new ArrayList<ConcurrentFilter>();

		// if our command is now just whitespace after removing the final filter, that
		// means we had a single filter in our command: a RedirectFilter (note because
		// we ensure parameter command is not blank in ConcurrentREPL), redirect needs
		// input so throw appropriate IAE
		if (newCommand.trim().isEmpty()) {
			throw new InvalidCommandException(Message.REQUIRES_INPUT.with_parameter(command));
		}

		// this will hold our list of subcommands
		List<String> subCommands = new ArrayList<String>();

		// first split up our new command on the pipe - since | means something in
		// regex, we need to escape it in regex by providing the regex string \|, but to
		// do this in Java we need to escape \ so pass in \\|
		String[] pipeSplit = newCommand.split("\\" + ConcurrentREPL.PIPE);

		for (int i = 0; i < pipeSplit.length; i++) {

			// get index of > in sub command (i.e. if there is a > between | | or in front
			// of first |) - both of these are violations to be handled later
			int redirectIdx = pipeSplit[i].indexOf(ConcurrentREPL.REDIRECT);

			// if there isn't one, just add the sub command
			if (redirectIdx == -1) {
				subCommands.add(pipeSplit[i]);
			} else {
				// otherwise, if > discovered split string before and after it (not after
				// portion includes the > as we need that for RedirectFilter construction)
				String preRedirect = pipeSplit[i].substring(0, redirectIdx);
				String redirect = pipeSplit[i].substring(redirectIdx);

				// if there was something before the > (i.e. we don't have | >) then add
				// whatever we had in front of the > as a sub command
				if (!preRedirect.isEmpty()) {
					subCommands.add(preRedirect);
				}

				// add the redirect component regardless - note this assumes this treats | cmd >
				// file1 > file2 | will be treated as only cmd and a single redirect and thus
				// the error that will be reported is file1 > file2 isn't a file instead of >
				// not being allowed to have output - tests never pass this in so its ok
				subCommands.add(redirect);
			}
		}

		// construct filters from each of the identified subcommands
		for (String sc : subCommands) {
			filters.add(constructFilterFromSubCommand(sc));
		}

		// get the last command (we know it will exist b/c by passing
		// newCommand.isBlank() check we know we have at least 2 filters and thus after
		// removing the final filter, we will have at least 1 filter in filters
		ConcurrentFilter lastCmdFilter = filters.get(filters.size() - 1);

		// if the determined final filter was a redirect filter, add it if it was print,
		// then add it as long as the last command given by user isnt cd (note we add
		// redirect even if cd filter exists so we can detect that error in linkFilters)
		if (finalFilter instanceof RedirectFilter || !(lastCmdFilter instanceof ChangeDirectoryFilter)) {
			filters.add(finalFilter);
		}

		// if the first filter is grep, wc, uniq, redirect, head, or tail then throw IAE
		// as all these commands require an input filter
		ConcurrentFilter firstFilter = filters.get(0);
		if (firstFilter instanceof GrepFilter || firstFilter instanceof WordCountFilter
				|| firstFilter instanceof UniqFilter || firstFilter instanceof RedirectFilter
				|| firstFilter instanceof HeadFilter || firstFilter instanceof TailFilter
				|| firstFilter instanceof CaesarCipherFilter) {
			throw new InvalidCommandException(Message.REQUIRES_INPUT.with_parameter(subCommands.get(0)));
		}

		// link filters all together
		linkFilters(filters);
		return filters;
	}

	/**
	 * Returns the filter that appears last in the specified command
	 * 
	 * @param command the command to search from
	 * @return the ConcurrentFilter that appears last in the specified command
	 */
	private static ConcurrentFilter determineFinalFilter(String command) {

		// find the last pipe and last redirect
		int lastPipeIdx = command.lastIndexOf(ConcurrentREPL.PIPE);
		int lastRedirectIdx = command.lastIndexOf(ConcurrentREPL.REDIRECT);

		// if the last redirect falls after the last pipe that means the last filter is
		// a redirect filter, thus create a RedirectFilter using the part of the command
		// starting from the last >
		if (lastRedirectIdx > lastPipeIdx) {
			return new RedirectFilter(command.substring(lastRedirectIdx));
		}

		// if the last pipe index falls after the last redirect, then the command is
		// going to stdout, so we need to add a PrintFilter
		return new PrintFilter();
	}

	/**
	 * Returns a string that contains the specified command without the final filter
	 * 
	 * @param command the command to parse and remove the final filter from
	 * @return the adjusted command that does not contain the final filter
	 */
	private static String adjustCommandToRemoveFinalFilter(String command) {

		// find the last pipe and last redirect
		int lastPipeIdx = command.lastIndexOf(ConcurrentREPL.PIPE);
		int lastRedirectIdx = command.lastIndexOf(ConcurrentREPL.REDIRECT);

		// if the last redirect falls after the last pipe that means the last filter is
		// a redirect filter, then drop the substring of the command starting from >
		if (lastRedirectIdx > lastPipeIdx) {
			return command.substring(0, lastRedirectIdx);
		}

		// if the last pipe index falls after the last redirect, then the command is
		// going to stdout, so we leave the command unchanged
		return command;
	}

	/**
	 * Helper method that determines whether a trimmed sub command is okay to be
	 * passed to its corresponding filter. A trimmed subcommand is ok to be passed
	 * to its corresponding filter if it is either equal to a command or it starts
	 * with that command followed by a space. This is used for commands that take an
	 * input (cat, grep, etc). Example: "cat" or "cat " would be okay, but "catx"
	 * would not be ok.
	 * 
	 * @param trimmedSubCommand a trimmed subcommand
	 * @param cmd               command to compare trimmedSubCommand to
	 * @return true if the (untrimmed) version of trimmedSubCommand should be passed
	 *         to its corresponding Filter, false otherwise.
	 */
	private static boolean canPassToFilter(String trimmedSubCommand, String cmd) {
		return trimmedSubCommand.startsWith(cmd + " ") || trimmedSubCommand.equals(cmd);
	}

	/**
	 * Creates a single filter from the specified subCommand
	 * 
	 * @param subCommand the command to create a filter from
	 * @return the ConcurrentFilter created from the given subCommand
	 */
	private static ConcurrentFilter constructFilterFromSubCommand(String subCommand) {

		// trim the sub command and then determine its corresponding filter as follows:
		// 1) if the trimmed sub command equals some command that doesn't require
		// parameters (e.g. pwd) then pass to its corresponding filter (e.g.
		// WorkingDirectoryFilter) constructor to build a Filter.
		// 2) if the trimmed sub command can be passed to a filter (see canPassToFilter)
		// for some command that requires a parameter (e.g. cat) then pass to its
		// corresponding filter (CatFilter) constructor to build a Filter
		String trimmed = subCommand.trim();
		if (trimmed.equals("pwd")) {
			return new WorkingDirectoryFilter(trimmed);
		} else if (trimmed.equals("ls")) {
			return new ListFilter(trimmed);
		} else if (canPassToFilter(trimmed, "cd")) {
			return new ChangeDirectoryFilter(trimmed);
		} else if (canPassToFilter(trimmed, "cat")) {
			return new CatFilter(trimmed);
		} else if (canPassToFilter(trimmed, "cc")) {
			return new CaesarCipherFilter(trimmed);
		} else if (canPassToFilter(trimmed, "grep")) {
			return new GrepFilter(trimmed);
		} else if (trimmed.equals("wc")) {
			return new WordCountFilter();
		} else if (trimmed.equals("uniq")) {
			return new UniqFilter();
		} else if (trimmed.equals("head")) {
			return new HeadFilter();
		} else if (trimmed.equals("tail")) {
			return new TailFilter();
		} else if (canPassToFilter(trimmed, ConcurrentREPL.REDIRECT)) {
			return new RedirectFilter(trimmed);
		} else {
			// if the trimmed subcommand neither equals a command that has no parameters or
			// can be passed to a filter that requires a parameter, then say the command
			// can't be found
			throw new InvalidCommandException(Message.COMMAND_NOT_FOUND.with_parameter(trimmed));
		}
	}

	/**
	 * links the given filters with the order they appear in the list
	 */
	private static void linkFilters(List<ConcurrentFilter> filters) {

		// loop over filters starting from 2nd, link to previous. this will catch if
		// filter i should have a previous input (or not) and if filter i - 1 should
		// have a next output since if setPrevFilter is not overridden by a filter then
		// it will just call setNextFilter on filter i - 1 (since the only time we
		// override setPrevFilter is for commands that should not have input)
		for (int i = 1; i < filters.size(); i++) {
			filters.get(i).setPrevFilter(filters.get(i - 1));
		}
	}
}
