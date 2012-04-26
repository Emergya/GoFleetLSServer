package org.gofleet.openLS.tsp;

import com.vividsolutions.jts.geom.Point;

public interface TSPStop {
	
	/**
	 * Returns the unique id (on this bag) for this stop.
	 * @return
	 */
	Integer getId();
	
	/**
	 * Returns the position of this stop.
	 * @return
	 */
	Point getPosition();
}
