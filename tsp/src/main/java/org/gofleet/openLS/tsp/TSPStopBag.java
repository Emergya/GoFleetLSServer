package org.gofleet.openLS.tsp;

import java.util.Collection;

public interface TSPStopBag {
	/**
	 * If {@link #hasFirst()} returns true, returns the stop which has to be
	 * first
	 * 
	 * Otherwise, returns null.
	 * 
	 * @return
	 */
	TSPStop getFirst();

	/**
	 * If {@link #hasLast()} returns true, returns the stop which has to be last
	 * 
	 * Otherwise, returns null.
	 * 
	 * @return
	 */
	TSPStop getLast();

	/**
	 * Returns, unordered, all the stops.
	 * 
	 * @return
	 */
	Collection<TSPStop> getAll();

	/**
	 * Returns if this bag has a stop which must be the first stop
	 * 
	 * @return
	 */
	Boolean hasFirst();

	/**
	 * Returns if this bag has a stop which must be the last stop
	 * 
	 * @return
	 */
	Boolean hasLast();

}
