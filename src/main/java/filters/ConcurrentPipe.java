package filters;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class that allows filters to write input to and read output from. This
 * class differs from the Pipe class because it offers methods that block on
 * read and write.
 * 
 */
public class ConcurrentPipe {

	private LinkedBlockingQueue<PipeItem> buffer;

	public ConcurrentPipe() {
		buffer = new LinkedBlockingQueue<PipeItem>();
	}

	/**
	 * Retrieves and removes the next element in the pipe, waiting if necessary
	 * until an element becomes available. If null is returned, you should take that
	 * to mean the poison pill has been returned.
	 * 
	 * @return the next element in the pipe
	 * @throws InterruptedException - if interrupted while waiting
	 */
	public String readAndWait() throws InterruptedException {
		return buffer.take().toString();
	}

	/**
	 * Inserts the specified element into the pipe, waiting if necessary for space
	 * to become available.
	 * 
	 * @param data - the element to add
	 * @throws InterruptedException - if interrupted while waiting
	 */
	public void writeAndWait(String data) throws InterruptedException {
		buffer.put(new PipeItem(data));
	}

	/**
	 * Retrieves and removes the next element in the pipe, or returns null if the
	 * pipe is empty. If null is returned, you should take that to mean the poison
	 * pill has been returned.
	 * 
	 * @return the next element in the pipe, or null if this pipe is empty
	 */
	public String read() {
		return buffer.poll().toString();
	}

	/**
	 * Inserts the specified element into the pipe.
	 * 
	 * @param data - the element to add
	 */
	public void write(String data) {
		buffer.add(new PipeItem(data));
	}

	/**
	 * {@return the number of elements in this collection}
	 */
	public int size() {
		return buffer.size();
	}

	/**
	 * {@return true if this collection contains no elements}
	 */
	public boolean isEmpty() {
		return buffer.isEmpty();
	}

	/**
	 * Removes all of the elements from the pipe. The pipe will be empty after this
	 * method returns.
	 */
	public void clear() {
		buffer.clear();
	}

	/**
	 * Writes a poison pill to the pipe, waiting if necessary for space to become
	 * available.
	 * 
	 * @throws InterruptedException - if interrupted while waiting
	 */
	public void writePoisonPill() throws InterruptedException {
		buffer.put(PipeItem.createPoisonPill());
	}

	/**
	 * Represents the elements of the ConcurrentPipe's buffer. It wraps a String. A
	 * LinkedBlockingQueue cannot store null elements but a PipeItem can wrap null.
	 * This signifies a poison pill.
	 */
	private static class PipeItem {
		private String data;

		public PipeItem(String data) {
			this.data = data;
		}

		public static PipeItem createPoisonPill() {
			return new PipeItem(null);
		}

		public String toString() {
			return this.data;
		}
	}

}