package misc;

/**
 * The abstract class that represents a filter to perform shell commands
 * 
 * You should not modify this class.
 * 
 * @author cs131a
 *
 */
public abstract class Filter {

	/**
	 * The next filter
	 */
	protected Filter next;
	/**
	 * The previous filter
	 */
	protected Filter prev;

	/**
	 * sets the next filter as the specified filter
	 * 
	 * @param next the next filter to be set
	 */
	public abstract void setNextFilter(Filter next);

	/**
	 * sets the previous filter as the specified filter
	 * 
	 * @param prev the previous filter to be set
	 */
	public abstract void setPrevFilter(Filter prev);

}
