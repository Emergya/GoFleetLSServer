/*
 * Copyright (C) 2012, Emergya (http://www.emergya.com)
 *
 * @author <a href="mailto:marias@emergya.com">María Arias de Reyna</a>
 *
 * This file is part of GoFleetLS
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
package org.gofleet.openLS.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import net.opengis.gml.v_3_1_1.DirectPositionListType;
import net.opengis.gml.v_3_1_1.LineStringType;
import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.DistanceType;
import net.opengis.xls.v_1_2_0.DistanceUnitType;
import net.opengis.xls.v_1_2_0.RouteGeometryType;
import net.opengis.xls.v_1_2_0.RouteHandleType;
import net.opengis.xls.v_1_2_0.RouteInstructionType;
import net.opengis.xls.v_1_2_0.RouteInstructionsListType;
import net.opengis.xls.v_1_2_0.RouteSummaryType;
import net.opengis.xls.v_1_2_0.WayPointListType;
import net.opengis.xls.v_1_2_0.WayPointType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.gofleet.internacionalization.I18n;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;

/**
 * @author marias
 * 
 */
@Repository
public class OSRMConnector {

	protected static final String EPSG_4326 = "EPSG:4326";
	private static Log LOG = LogFactory.getLog(OSRMConnector.class);
	private GeometryFactory gf = new GeometryFactory();
	private DatatypeFactory dataTypeFactory = new com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl();

	@Autowired
	private I18n i18n;

	/**
	 * @param i18n the i18n to set
	 */
	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}

	public OSRMConnector() {
	}

	/**
	 * Route plan using osrm server.
	 * 
	 * @param param
	 * @param host_port
	 * @param http
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws JAXBException
	 * @throws InterruptedException
	 */
	public AbstractResponseParametersType routePlan(
			DetermineRouteRequestType param, String host_port, String http,
			Locale locale) throws IOException, JAXBException, ParseException,
			InterruptedException {

		DetermineRouteResponseType res = new DetermineRouteResponseType();

		// MANDATORY:
		// Describes the overall characteristics of the route
		RouteSummaryType routeSummary = new RouteSummaryType();

		// OPTIONAL:
		// Contains a reference to the route stored at the Route
		// Determination Service server.
		// Can be used in subsequent request to the Route Service to request
		// additional information about the route, or to request an
		// alternate route.
		RouteHandleType routeHandle = new RouteHandleType();

		// Constains a list of turn-by-turn route instructions and advisories,
		// formatted for presentation.
		// May contain the geometry and bounding box if specified in the
		// request.
		// May contain description. FOr example this can be used to connect the
		// instruction with a map.
		RouteInstructionsListType routeInstructionsList = new RouteInstructionsListType();

		// RouteMapType: Contains a list of route maps.
		// Can be used to specify which type of map to be generated whether an
		// overview or maneuver.
		// May contain description. For example this can be used to connect the
		// instruction with a map.

		try {

			RouteGeometryType routeGeometry = new RouteGeometryType();
			WayPointListType wayPointList = param.getRoutePlan()
					.getWayPointList();

			String url = http + "://" + host_port + "/viaroute";

			CoordinateReferenceSystem sourceCRS = CRS.decode(EPSG_4326);
			CoordinateReferenceSystem targetCRS = GeoUtil.getSRS(wayPointList
					.getStartPoint());

			Point point = GeoUtil.getPoint(wayPointList.getStartPoint(),
					sourceCRS);
			url += "&start=" + point.getY() + "," + point.getX();
			point = GeoUtil.getPoint(wayPointList.getEndPoint(), sourceCRS);
			url += "&dest=" + point.getY() + "," + point.getX();

			for (WayPointType wayPoint : wayPointList.getViaPoint()) {
				point = GeoUtil.getPoint(wayPoint, sourceCRS);
				url += "&via=" + point.getY() + "," + point.getX();
			}

			url += "&z=15&output=json&geomformat=cmp&instructions=true";

			LOG.info(url);

			LineStringType lst = new LineStringType();

			lst.setSrsName(targetCRS.getName().getCode());

			JsonFactory f = new JsonFactory();
			JsonParser jp = f.createJsonParser(new URL(url));
			jp.nextToken();
			while (jp.nextToken() != JsonToken.END_OBJECT
					&& jp.getCurrentToken() != null) {
				String fieldname = jp.getCurrentName();
				if (fieldname == null)
					;
				else if (fieldname.equals("route_summary")) {
					if (jp.nextToken() == JsonToken.START_OBJECT
							&& jp.getCurrentToken() != null) {
						while (jp.nextToken() != JsonToken.END_OBJECT
								&& jp.getCurrentToken() != null) {
							if (jp.getCurrentName().equals("total_time")) {
								jp.nextToken();
								Duration duration = dataTypeFactory
										.newDuration(true, 0, 0, 0, 0,
												jp.getIntValue(), 0);
								routeSummary.setTotalTime(duration);
							} else if (jp.getCurrentName().equals(
									"total_distance")) {
								jp.nextToken();
								DistanceType duration = new DistanceType();
								duration.setUom(DistanceUnitType.M);
								duration.setValue(new BigDecimal(jp.getText()));
								routeSummary.setTotalDistance(duration);
							}
						}
					}
				} else if (jp.getCurrentName().equals("route_geometry")) {
					String geometry = jp.getText();
					decodeRouteGeometry(geometry,
							lst.getPosOrPointPropertyOrPointRep(), targetCRS,
							sourceCRS);
				} else if (jp.getCurrentName().equals("route_instructions")) {
					while (jp.nextToken() == JsonToken.START_ARRAY
							&& jp.getCurrentToken() != null) {
						RouteInstructionType e = new RouteInstructionType();
						jp.nextToken();
						String instruction = i18n.getString(locale,
								jp.getText());
						jp.nextToken();
						if (jp.getText().length() > 0)
							instruction += " " + i18n.getString(locale, "on")
									+ " " + jp.getText();
						e.setInstruction(instruction);
						jp.nextToken();

						DistanceType distance = new DistanceType();
						distance.setUom(DistanceUnitType.M);
						distance.setValue(new BigDecimal(jp.getText()));
						e.setDistance(distance);

						jp.nextToken();

						Duration duration = dataTypeFactory.newDuration(true,
								0, 0, 0, 0, jp.getIntValue(), 0);
						routeSummary.setTotalTime(duration);
						e.setDuration(duration);

						while (jp.nextToken() != JsonToken.END_ARRAY
								&& jp.getCurrentToken() != null)
							;
						routeInstructionsList.getRouteInstruction().add(e);
					}
				}
				jp.nextToken();
			}
			jp.close(); // ensure resources get cleaned up timely and properly

			routeGeometry.setLineString(lst);
			res.setRouteGeometry(routeGeometry);
			res.setRouteHandle(routeHandle);

			if (param.getRouteInstructionsRequest() != null)
				res.setRouteInstructionsList(routeInstructionsList);
			res.getRouteInstructionsList().setFormat(MediaType.TEXT_PLAIN);
			res.getRouteInstructionsList().setLang(locale.getLanguage());
			res.setRouteSummary(routeSummary);
		} catch (Throwable t) {
			LOG.error("Error generating route response: " + t, t);
			t.printStackTrace();
		}
		return res;
	}

	private List<JAXBElement<?>> decodeRouteGeometry(String encoded,
			List<JAXBElement<?>> list, CoordinateReferenceSystem targetCRS,
			CoordinateReferenceSystem sourceCRS)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException {
		MathTransform transform = null;

		LOG.info(targetCRS.toWKT());
		LOG.info(sourceCRS.toWKT());
		
		double precision = 5;
		precision = Math.pow(10, -precision);
		int len = encoded.length(), index = 0, lat = 0, lng = 0;
		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
			lat += dlat;
			shift = 0;
			result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = (((result & 1) != 0) ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			Coordinate coord = new Coordinate(lng * precision, lat * precision);
			Point sourceGeometry = gf.createPoint(coord);
			if (sourceCRS != targetCRS) {
				if (transform == null)
					transform = CRS.findMathTransform(sourceCRS, targetCRS);
				sourceGeometry = JTS.transform(sourceGeometry, transform)
						.getCentroid();
			}
			DirectPositionListType e = new DirectPositionListType();
			e.getValue().add(coord.x);
			e.getValue().add(coord.y);

			JAXBElement<DirectPositionListType> elem = new JAXBElement<DirectPositionListType>(
					new QName("http://www.opengis.net/gml", "pos", "gml"),
					DirectPositionListType.class, e);

			list.add(elem);
		}

		return list;
	}
}
