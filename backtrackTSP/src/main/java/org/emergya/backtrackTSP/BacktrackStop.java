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

package org.emergya.backtrackTSP;

import org.gofleet.openLS.tsp.TSPStop;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class BacktrackStop implements TSPStop {

	protected Integer id;
	private Geometry position;

	public Integer getId() {
		return this.id;
	}

	public Point getPosition() {
		return this.position.getCentroid();
	}

	public BacktrackStop(Integer id, Geometry position) {
		super();
		this.id = id;
		this.position = position;
	}

	@Override
	public String toString() {
		return "{" + this.getId()
		// + "->" + this.getPosition().toText()
				+ "}";
	}

}
