package org.gofleet.openLS.tsp;

import java.util.List;

public interface TSPAlgorithm {

	/**
	 * Returns the optimum TSP of the bag.
	 * 
	 * The list contains all the stops of the bag.
	 * 
	 * If {@link TSPStopBag#hasLast()}, {@link TSPStopBag#getLast()} is the last
	 * {@link TSPStop} of the list.
	 * 
	 * If {@link TSPStopBag#hasFirst()}, {@link TSPStopBag#getFirst()} is the
	 * first {@link TSPStop} of the list.
	 * 
	 * @param bag
	 * @return
	 */
	List<TSPStop> order(TSPStopBag bag);

}
