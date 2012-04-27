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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import net.opengis.gml.v_3_1_1.DirectPositionType;
import net.opengis.gml.v_3_1_1.PointType;
import net.opengis.xls.v_1_2_0.AbstractLocationType;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.PositionType;
import net.opengis.xls.v_1_2_0.RoutePlanType;
import net.opengis.xls.v_1_2_0.WayPointListType;
import net.opengis.xls.v_1_2_0.WayPointType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.emergya.osrm.OSRM;
import org.gofleet.openLS.tsp.TSPStop;

import com.vividsolutions.jts.geom.Point;

public class DistanceMatrix {

	private Map<Key, Double> distances = new HashMap<Key, Double>();
	private static Log LOG = LogFactory.getLog(DistanceMatrix.class);

	private OSRM osrm;
	private org.gofleet.configuration.Configuration configuration;

	public DistanceMatrix() {
		this.osrm = new OSRM();
	}

	public DistanceMatrix(OSRM osrm,
			org.gofleet.configuration.Configuration configuration) {
		super();
		this.osrm = osrm;
		this.configuration = configuration;
	}

	public Double distance(TSPStop from, TSPStop to) {
		Key k = new Key(from.getId(), to.getId());
		if (distances.containsKey(k)) {
			return distances.get(k);
		} else {
			if (LOG.isDebugEnabled())
				LOG.debug("DistanceMatrix.distance(" + k + ")");
			return calculateDistance(from, to, k);
		}
	}

	private Double calculateDistance(TSPStop from, TSPStop to, Key k) {
		String host_port = "localhost:5000";
		String http = "http";
		if (configuration != null) {
			if (configuration.get("OSRM_SSL", "off").equals("on"))
				http = "https";
			host_port = configuration.get("OSRM_HOST", "localhost:5000");
		}
		DetermineRouteRequestType param = new DetermineRouteRequestType();

		if (param.getRoutePlan() == null)
			param.setRoutePlan(new RoutePlanType());

		if (param.getRoutePlan().getWayPointList() == null)
			param.getRoutePlan().setWayPointList(new WayPointListType());

		WayPointListType waypointList = param.getRoutePlan().getWayPointList();

		WayPointType start = getWayPoint(from.getPosition());
		WayPointType end = getWayPoint(to.getPosition());

		waypointList.setStartPoint(start);
		waypointList.setEndPoint(end);

		try {
			DetermineRouteResponseType res = (DetermineRouteResponseType) osrm
					.routePlan(param, host_port, http, Locale.ROOT);
			double cost = res.getRouteSummary().getTotalDistance().getValue()
					.doubleValue();
			distances.put(k, cost);
			return cost;
		} catch (Throwable e) {
			LOG.error("Error extracting distance from " + from + " to " + to, e);
			return Double.MAX_VALUE;
		}
	}

	@SuppressWarnings("restriction")
	private WayPointType getWayPoint(Point position) {
		WayPointType point = new WayPointType();
		point.setStop(Boolean.TRUE);

		PositionType postype = new PositionType();
		PointType pointtype = new PointType();
		DirectPositionType directPosition = new DirectPositionType();

		directPosition.setSrsName("EPSG:" + position.getSRID());
		directPosition.getValue().add(position.getX());
		directPosition.getValue().add(position.getY());

		pointtype.setPos(directPosition);
		postype.setPoint(pointtype);
		JAXBElement<? extends AbstractLocationType> value = new JAXBElement<PositionType>(
				new QName("http://www.opengis.net/gml", "pos", "gml"),
				PositionType.class, postype);
		point.setLocation(value);

		return point;
	}

}

class Key {

	protected Key(Integer from, Integer to) {
		super();
		this.from = from;
		this.to = to;
	}

	private Integer from;
	private Integer to;

	public Integer getFrom() {
		return from;
	}

	public void setFrom(Integer from) {
		this.from = from;
	}

	public Integer getTo() {
		return to;
	}

	public void setTo(Integer to) {
		this.to = to;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Key other = (Key) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "{ K<" + this.getFrom() + "->" + this.getTo() + ">}";
	}

}