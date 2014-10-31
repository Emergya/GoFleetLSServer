/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias</a>
 *
 * This file is part of GoFleet
 *
 * This software is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
package org.gofleet.openLS.ddbb.dao.postgis;

import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.gofleet.openLS.ddbb.bean.HBA;
import org.gofleet.openLS.ddbb.dao.HibernateDAOBase;
import org.gofleet.openLS.util.GeoUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernatespatial.GeometryUserType;
import org.jvnet.ogc.DetermineRouteRequestType;
import org.jvnet.ogc.DetermineRouteResponseType;
import org.jvnet.ogc.DistanceType;
import org.jvnet.ogc.LineStringType;
import org.jvnet.ogc.RouteGeometryType;
import org.jvnet.ogc.RouteHandleType;
import org.jvnet.ogc.RouteInstructionsListType;
import org.jvnet.ogc.RouteMapType;
import org.jvnet.ogc.RouteSummaryType;
import org.jvnet.ogc.WayPointListType;
import org.jvnet.ogc.WayPointType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

@Repository
public class PostGisHBRoutingDAO extends HibernateDAOBase {

	public GeometryFactory gf = new GeometryFactory();

	private static Log LOG = LogFactory.getLog(PostGisHBRoutingDAO.class);

	private Unmarshaller unmarshaller;
	private JAXBContext context;
	private Marshaller marshaller;
    
    @Value("${org.gofleet.postgis.routing.tableName}")
    private String TABLE_NAME;
    
    @Value("${org.gofleet.postgis.routing.routingId}")
    private String ROUTING_ID;

	/**
	 * 
	 */
	public PostGisHBRoutingDAO() {
		try {
			unmarshaller = JAXBContext.newInstance(LineStringType.class)
					.createUnmarshaller();
			context = JAXBContext.newInstance("org.jvnet.ogc.gml.v_3_1_1.jts");
			marshaller = context.createMarshaller();
		} catch (JAXBException e) {
			LOG.error(e, e);
		}
	}

	@Transactional(readOnly = true)
	public DetermineRouteResponseType routePlan(
			final DetermineRouteRequestType param) {

		HibernateCallback<DetermineRouteResponseType> action = new HibernateCallback<DetermineRouteResponseType>() {

			@SuppressWarnings("unchecked")
            @Override
			public DetermineRouteResponseType doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query consulta = session.getNamedQuery("tsp");

				WayPointListType wayPointList = param.getRoutePlan()
						.getWayPointList();

				List<Geometry> stops = new LinkedList<Geometry>();
				for (WayPointType wayPoint : wayPointList.getViaPoint()) {
					stops.add(GeoUtil.getPoint(wayPoint, null));
				}
				stops.add(GeoUtil.getPoint(wayPointList.getEndPoint(), null));

				consulta.setString("tablename", TABLE_NAME);
				consulta.setParameterList("stoptable", stops,
						GeometryUserType.TYPE);
				consulta.setString("gid", ROUTING_ID);
				consulta.setParameter("start",
						GeoUtil.getPoint(wayPointList.getStartPoint(), null),
						GeometryUserType.TYPE);

				consulta.setReadOnly(true);
				LOG.debug(consulta);

				return getRouteResponse(consulta.list());

			}

		};
		return hibernateTemplate.executeWithNativeSession(action);
	}

	private RouteGeometryType getRouteGeometry(
			com.vividsolutions.jts.geom.LineString geometry)
			throws JAXBException, ParseException {

		LineString line = (LineString) (new WKTReader()).read(
				geometry.toString()).getGeometryN(0);

		if (LOG.isTraceEnabled())
			marshaller.marshal(line, System.out);

		StringWriter writer = new StringWriter();
		marshaller.marshal(line, writer);
		StringReader reader = new StringReader(writer.toString());

		LineStringType lineString = ((JAXBElement<LineStringType>) unmarshaller
				.unmarshal(reader)).getValue();

		RouteGeometryType routeGeometry = new RouteGeometryType();
		routeGeometry.setLineString(lineString);
		return routeGeometry;
	}

	private RouteSummaryType getRouteSummary(Double cost) {
		RouteSummaryType res = new RouteSummaryType();
		DistanceType coste = new DistanceType();
		if (cost.isInfinite() || cost.isNaN())
			coste.setValue(BigDecimal.valueOf(0d));
		else
			coste.setValue(BigDecimal.valueOf(cost));
		res.setTotalDistance(coste);
		return res;
	}

	private List<RouteMapType> getRouteMap(List<Coordinate> lineStrings) {
		List<RouteMapType> res = new ArrayList<RouteMapType>(0);
		return res;
	}

	private RouteInstructionsListType getInstructionsList(
			List<Coordinate> lineStrings) {
		RouteInstructionsListType res = new RouteInstructionsListType();
		return res;
	}

	private RouteHandleType getRouteHandle(List<Coordinate> lineStrings) {
		RouteHandleType handleType = new RouteHandleType();
		handleType.setRouteID("-1");
		handleType.setServiceID("-1");
		return handleType;
	}

	private DetermineRouteResponseType getRouteResponse(List<HBA> resultado)
			throws SQLException {

		DetermineRouteResponseType res = new DetermineRouteResponseType();
		Long last = -1l;
		List<Coordinate> coords = new LinkedList<Coordinate>();
		double cost = 0;
		for (HBA step : resultado) {
			try {
				Long current = step.getId();
				if (!current.equals(last)) {
					coords.addAll(Arrays.asList(step.getGeometria()
							.getCoordinates()));
					cost += step.getCost();
				} else
					LOG.trace("Repeating step " + current);
				last = current;
			} catch (Exception e) {
				LOG.error("Unknown Step", e);
			}
		}

		RouteGeometryType routeGeometry;
		try {
			CoordinateSequence cs = new CoordinateArraySequence(
					coords.toArray(new Coordinate[] {}));
			LineString line = new LineString(cs, gf);
			routeGeometry = getRouteGeometry(line);
			res.setRouteGeometry(routeGeometry);
		} catch (JAXBException e) {
			LOG.error(e, e);
		} catch (ParseException e) {
			LOG.error(e, e);
		}

		res.setRouteHandle(getRouteHandle(coords));
		res.setRouteInstructionsList(getInstructionsList(coords));
		res.setRouteSummary(getRouteSummary(cost));
		return res;
	}
}
