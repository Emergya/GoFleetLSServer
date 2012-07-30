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
package org.emergya.osrm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
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

/**
 * @author marias
 * 
 */
@SuppressWarnings("restriction")
@Repository
public class OSRM {

	protected static final String EPSG_4326 = "EPSG:4326";
	private static Log LOG = LogFactory.getLog(OSRM.class);
	private GeometryFactory gf = new GeometryFactory();
	private DatatypeFactory dataTypeFactory = new com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl();

	@Autowired
	private I18n i18n;

	/**
	 * @param i18n
	 *            the i18n to set
	 */
	public void setI18n(I18n i18n) {
		this.i18n = i18n;
	}

	public OSRM() {
	}

	/**
	 * To use outside spring context
	 */
	public OSRM(I18n i18n) {
		this();
		setI18n(i18n);
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

			String url = "/viaroute";

			CoordinateReferenceSystem sourceCRS = CRS.decode(EPSG_4326);
			CoordinateReferenceSystem targetCRS = GeoUtil.getSRS(wayPointList
					.getStartPoint());

			com.vividsolutions.jts.geom.Point point = GeoUtil.getPoint(
					wayPointList.getStartPoint(), sourceCRS);
			url += "?loc=" + point.getY() + "," + point.getX();

			for (WayPointType wayPoint : wayPointList.getViaPoint()) {
				point = GeoUtil.getPoint(wayPoint, sourceCRS);
				url += "&loc=" + point.getY() + "," + point.getX();
			}
			point = GeoUtil.getPoint(wayPointList.getEndPoint(), sourceCRS);
			url += "&loc=" + point.getY() + "," + point.getX();

			LOG.debug(url);

			LineStringType lst = new LineStringType();

			lst.setSrsName(targetCRS.getName().getCode());
			routeGeometry.setLineString(lst);
			res.setRouteGeometry(routeGeometry);

			JsonFactory f = new JsonFactory();
			JsonParser jp = f.createJsonParser(getOSRMStream(host_port, url));
			jp.nextToken();
			while (jp.nextToken() != JsonToken.END_OBJECT
					&& jp.getCurrentToken() != null) {
				String fieldname = jp.getCurrentName();
				if (fieldname == null)
					;
				else if (jp.getCurrentName().equals("total_distance")) {
					DistanceType duration = new DistanceType();
					duration.setUom(DistanceUnitType.M);
					duration.setValue(new BigDecimal(jp.getText()));
					routeSummary.setTotalDistance(duration);
				} else if (jp.getCurrentName().equals("total_time")) {
					Duration duration = dataTypeFactory.newDuration(true, 0, 0,
							0, 0, 0, jp.getIntValue());
					routeSummary.setTotalTime(duration);
				} else if (jp.getCurrentName().equals("route_geometry")) {
					decodeRouteGeometry(lst.getPosOrPointPropertyOrPointRep(),
							targetCRS, sourceCRS, jp);
				} else if (jp.getCurrentName().equals("route_instructions")) {
					processInstructions(locale, routeSummary,
							routeInstructionsList, jp);
				}
				jp.nextToken();
			}
			jp.close(); // ensure resources get cleaned up timely and properly

			res.setRouteHandle(routeHandle);

			if (param.getRouteInstructionsRequest() != null) {
				res.setRouteInstructionsList(routeInstructionsList);
				res.getRouteInstructionsList().setFormat("text/plain");
				res.getRouteInstructionsList().setLang(locale.getLanguage());
			}
			res.setRouteSummary(routeSummary);
		} catch (Throwable t) {
			LOG.error("Error generating route response: " + t, t);
			t.printStackTrace();
		}

		return res;
	}

	private BufferedReader getOSRMStream(String host_port, String url)
			throws UnknownHostException, IOException {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://" + host_port + url);

		HttpResponse response = httpclient.execute(httpget);

		BufferedReader in = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		return in;
	}

	/**
	 * @param locale
	 * @param routeSummary
	 * @param routeInstructionsList
	 * @param jp
	 * @throws IOException
	 * @throws JsonParseException
	 */
	public void processInstructions(Locale locale,
			RouteSummaryType routeSummary,
			RouteInstructionsListType routeInstructionsList, JsonParser jp)
			throws IOException, JsonParseException {
		while (jp.nextToken() == JsonToken.START_ARRAY
				&& jp.getCurrentToken() != null) {
			RouteInstructionType e = new RouteInstructionType();
			jp.nextToken();
			String instruction = jp.getText();
			if (i18n != null)
				instruction = i18n.getString(locale, jp.getText());
			jp.nextToken();
			if (jp.getText().length() > 0) {
				if (i18n == null)
					instruction += " on " + jp.getText();
				else
					instruction += " " + i18n.getString(locale, "on") + " "
							+ jp.getText();
			}
			e.setInstruction(instruction);
			jp.nextToken();

			DistanceType distance = new DistanceType();
			distance.setUom(DistanceUnitType.M);
			distance.setValue(new BigDecimal(jp.getText()));
			e.setDistance(distance);

			jp.nextToken();

			Duration duration = dataTypeFactory.newDuration(true, 0, 0, 0, 0,
					0, jp.getIntValue());
			e.setDuration(duration);

			while (jp.nextToken() != JsonToken.END_ARRAY
					&& jp.getCurrentToken() != null);
			routeInstructionsList.getRouteInstruction().add(e);
		}
	}

	private List<JAXBElement<?>> decodeRouteGeometry(List<JAXBElement<?>> list,
			CoordinateReferenceSystem targetCRS,
			CoordinateReferenceSystem sourceCRS, JsonParser jp)
			throws NoSuchAuthorityCodeException, FactoryException,
			MismatchedDimensionException, TransformException,
			JsonParseException, IOException {

		MathTransform transform = null;

		LOG.trace(targetCRS.toWKT());
		LOG.trace(sourceCRS.toWKT());
		jp.nextToken();

		while (jp.nextToken() == JsonToken.START_ARRAY
				&& jp.getCurrentToken() != null) {
			jp.nextToken();
			Double lat = jp.getDoubleValue();
			jp.nextToken();
			Double lon = jp.getDoubleValue();
			jp.nextToken();

			Coordinate coord = new Coordinate(lat, lon);
			Point sourceGeometry = gf.createPoint(coord);
			LOG.trace(sourceGeometry);
			if (sourceCRS != targetCRS) {
				if (transform == null)
					transform = CRS.findMathTransform(sourceCRS, targetCRS);
				sourceGeometry = JTS.transform(sourceGeometry, transform)
						.getCentroid();
				LOG.trace(sourceGeometry);
			}
			DirectPositionListType e = new DirectPositionListType();
			e.getValue().add(sourceGeometry.getY());
			e.getValue().add(sourceGeometry.getX());

			JAXBElement<DirectPositionListType> elem = new JAXBElement<DirectPositionListType>(
					new QName("http://www.opengis.net/gml", "pos", "gml"),
					DirectPositionListType.class, e);

			list.add(elem);

		}

		return list;
	}
}
