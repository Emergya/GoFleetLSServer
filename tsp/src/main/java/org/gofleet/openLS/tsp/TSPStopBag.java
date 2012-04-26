

/**
 * Copyright (C) 2012, Emergya (http://www.emergya.es)
 * 
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
 * 
 *         This file is part of GoFleet
 * 
 *         This software is free software; you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation; either version 2 of the License, or (at
 *         your option) any later version.
 * 
 *         This software is distributed in the hope that it will be useful, but
 *         WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *         General Public License for more details.
 * 
 *         You should have received a copy of the GNU General Public License
 *         along with this library; if not, write to the Free Software
 *         Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 *         02110-1301 USA
 * 
 *         As a special exception, if you link this library with other files to
 *         produce an executable, this library does not by itself cause the
 *         resulting executable to be covered by the GNU General Public License.
 *         This exception does not however invalidate any other reasons why the
 *         executable file might be covered by the GNU General Public License.
 */
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
