package misc;

import java.util.regex.Pattern;

/**
 * This class acts as abstraction of the shell's current file path.
 * 
 * You should not modify this class.
 *
 */
public final class CurrentWorkingDirectory {

	private static String currentWorkingDirectory = System.getProperty("user.dir");

	private CurrentWorkingDirectory() {
	}

	/**
	 * Sets the shell's new current working directory.
	 * 
	 * @param newDirectory to set the shell's current file path to
	 */
	public static void setTo(String newDirectory) {
		currentWorkingDirectory = newDirectory;
	}

	/**
	 * @return the current working directory
	 */
	public static String get() {
		return currentWorkingDirectory;
	}

	/**
	 * Resets the current working directory to System.getProperty("user.dir")
	 */
	public static void reset() {
		currentWorkingDirectory = System.getProperty("user.dir");
	}

	/**
	 * @return The system path separator as a String.
	 */
	public static String getPathSeparator() {
		return getPathSeparator(false);
	}

	/**
	 * Gets the system path separator as a regular String or as a String
	 * representing a RegEx literal.
	 * 
	 * This method is not necessary to complete this assignment. It is encouraged
	 * you write your solution in such a way that you only need getPathSeparator().
	 * getPathSeparator(boolean) is to be used in cases where you are using the path
	 * separator as part of a regular expression and the system path separator may
	 * include characters that have a special meaning in RegEx (for example the
	 * Windows path separator \ has a special meaning in RegEx).
	 * 
	 * @param forRegex If this is true, the path separator will be returned in a
	 *                 String representing a RegEx literal (e.g. if the file
	 *                 separator is \, "\\Q\\\\E" is returned). If this is false,
	 *                 the path separator will be returned as a String (e.g. if the
	 *                 path separator is \, "\\" is returned).
	 * @return The system path separator either as a regular String or a String
	 *         holding a RegEx literal as described above.
	 */
	public static String getPathSeparator(boolean forRegex) {
		String separator = System.getProperty("file.separator");
		if (forRegex) {
			return Pattern.quote(separator);
		}
		return separator;
	}
}
