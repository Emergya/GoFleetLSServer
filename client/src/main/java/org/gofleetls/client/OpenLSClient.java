package org.gofleetls.client;

/**
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.opengis.gml.v_3_1_1.CoordType;
import net.opengis.gml.v_3_1_1.DirectPositionType;
import net.opengis.gml.v_3_1_1.PointType;
import net.opengis.xls.v_1_2_0.AbstractBodyType;
import net.opengis.xls.v_1_2_0.AbstractLocationType;
import net.opengis.xls.v_1_2_0.AbstractResponseParametersType;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.DistanceUnitType;
import net.opengis.xls.v_1_2_0.PositionType;
import net.opengis.xls.v_1_2_0.RequestHeaderType;
import net.opengis.xls.v_1_2_0.RequestType;
import net.opengis.xls.v_1_2_0.ResponseType;
import net.opengis.xls.v_1_2_0.RouteGeometryRequestType;
import net.opengis.xls.v_1_2_0.RouteHandleType;
import net.opengis.xls.v_1_2_0.RouteInstructionsRequestType;
import net.opengis.xls.v_1_2_0.RouteMapRequestType;
import net.opengis.xls.v_1_2_0.RoutePlanType;
import net.opengis.xls.v_1_2_0.RoutePreferenceType;
import net.opengis.xls.v_1_2_0.WayPointListType;
import net.opengis.xls.v_1_2_0.WayPointType;
import net.opengis.xls.v_1_2_0.XLSType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.vividsolutions.jts.geom.Point;

public class OpenLSClient {

	/**
	 * 
	 */
	private static final BigInteger BG_2 = BigInteger.valueOf(2l);
	private static Unmarshaller UNMARSHALLER = null;

	/**
	 * Unless you want to use batch petitions, your should use some of the
	 * customized functions of this same class
	 * 
	 * @param parameter
	 * @param url
	 * @return
	 * @throws JAXBException
	 */
	@SuppressWarnings({ "restriction", "unchecked" })
	public static XLSType post(XLSType parameter, String url) {

		Client client = Client.create();

		RequestHeaderType header = new RequestHeaderType();

		parameter.setHeader(new JAXBElement<RequestHeaderType>(new QName(
				"http://www.opengis.net/xls", "RequestHeader"),
				RequestHeaderType.class, header));

		JAXBElement<XLSType> param = new JAXBElement<XLSType>(new QName(
				"http://www.opengis.net/xls", "xls"), XLSType.class, parameter);

		WebResource resource = client.resource(url);

		GenericType<JAXBElement<XLSType>> xlsType = new GenericType<JAXBElement<XLSType>>() {
		};

		JAXBElement<XLSType> response = resource
				.type(MediaType.APPLICATION_XML).post(xlsType, param);

		return response.getValue();
	}

	/**
	 * 
	 * Simplest route request. Just one maximum response.
	 * 
	 * @param parameter
	 * @param url
	 * @return
	 */
	public static DetermineRouteResponseType determineRoute(
			DetermineRouteRequestType parameter, String url) {
		return determineRoute(parameter, url, new BigInteger("1"));
	}

	/**
	 * Generic Route Request
	 * 
	 * @param parameter
	 * @param url
	 * @param maximumResponses
	 * @return
	 */
	@SuppressWarnings("restriction")
	public static DetermineRouteResponseType determineRoute(
			DetermineRouteRequestType parameter, String url,
			BigInteger maximumResponses) {

		JAXBElement<DetermineRouteRequestType> parameters = new JAXBElement<DetermineRouteRequestType>(
				new QName("http://www.opengis.net/xls",
						"DetermineRouteRequest", ""),
				DetermineRouteRequestType.class, parameter);

		RequestType request = new RequestType();
		request.setMaximumResponses(maximumResponses);
		request.setMethodName("routeRequest");
		request.setRequestParameters(parameters);

		JAXBElement<RequestType> requestBody = new JAXBElement<RequestType>(
				new QName("http://www.opengis.net/xls", "Request", ""),
				RequestType.class, request);
		XLSType xls = new XLSType();
		xls.getBody().add(requestBody);

		XLSType response = post(xls, url);
		List<JAXBElement<? extends AbstractBodyType>> body = response.getBody();
		for (JAXBElement<? extends AbstractBodyType> element : body) {
			AbstractBodyType bodyType = element.getValue();
			if (bodyType instanceof ResponseType) {
				ResponseType responseType = (ResponseType) bodyType;
				AbstractResponseParametersType responseParameters = responseType
						.getResponseParameters().getValue();
				if (responseParameters instanceof DetermineRouteResponseType)
					return (DetermineRouteResponseType) responseParameters;
			}
		}
		return null;
	}

	/**
	 * Route request with all parameters
	 * 
	 * @param url
	 * @param maximumResponses
	 * @param distanceUnit
	 * @param routeGeometryRequest
	 * @param routeInstructionsRequest
	 * @param routeHandle
	 * @param routePlan
	 * @param routeMapRequest
	 * @return
	 */
	public static DetermineRouteResponseType determineRoute(String url,
			BigInteger maximumResponses, DistanceUnitType distanceUnit,
			RouteGeometryRequestType routeGeometryRequest,
			RouteInstructionsRequestType routeInstructionsRequest,
			RouteHandleType routeHandle, RoutePlanType routePlan,
			RouteMapRequestType routeMapRequest) {

		DetermineRouteRequestType determineRouteRequest = new DetermineRouteRequestType();
		determineRouteRequest.setDistanceUnit(distanceUnit);
		determineRouteRequest.setProvideRouteHandle(routeHandle != null);
		determineRouteRequest.setRouteGeometryRequest(routeGeometryRequest);
		determineRouteRequest.setRouteHandle(routeHandle);
		determineRouteRequest
				.setRouteInstructionsRequest(routeInstructionsRequest);
		determineRouteRequest.setRouteMapRequest(routeMapRequest);
		determineRouteRequest.setRoutePlan(routePlan);

		return determineRoute(determineRouteRequest, url, maximumResponses);
	}

	public static DetermineRouteResponseType determineRoute(String url,
			Point origin, List<Point> stops) {

		DistanceUnitType distanceUnit = DistanceUnitType.M;
		RouteGeometryRequestType routeGeometryRequest = new RouteGeometryRequestType();
		RoutePlanType routePlan = new RoutePlanType();
		routePlan.setRoutePreference(RoutePreferenceType.FASTEST);
		WayPointListType waypointlist = routePlan.getWayPointList();

		if (waypointlist == null) {
			waypointlist = new WayPointListType();
			routePlan.setWayPointList(waypointlist);
		}

		WayPointType origin_wp = new WayPointType();
		origin_wp.setStop(false);
		origin_wp.setLocation(getPoint(origin));
		WayPointType end_wp = new WayPointType();
		end_wp.setStop(true);
		end_wp.setLocation(getPoint(stops.get(stops.size() - 1)));

		waypointlist.setEndPoint(end_wp);
		waypointlist.setStartPoint(origin_wp);

		for (int i = 0; i < stops.size() - 1; i++) {
			WayPointType stop_wp = new WayPointType();
			stop_wp.setStop(true);
			stop_wp.setLocation(getPoint(stops.get(i)));
			waypointlist.getViaPoint().add(stop_wp);
		}

		RouteMapRequestType routeMapRequest = new RouteMapRequestType();
		RouteInstructionsRequestType routeInstructionsRequest = new RouteInstructionsRequestType();
		RouteHandleType routeHandle = new RouteHandleType();
		return determineRoute(url, BigInteger.ONE, distanceUnit,
				routeGeometryRequest, routeInstructionsRequest, routeHandle,
				routePlan, routeMapRequest);
	}

	/**
	 * @param point
	 * @return
	 */
	@SuppressWarnings("restriction")
	private static JAXBElement<? extends AbstractLocationType> getPoint(
			Point point) {
		PositionType position = new PositionType();

		PointType p = new PointType();

		p.setSrsDimension(BG_2);
		p.setSrsName("EPSG:" + point.getSRID());
		CoordType coordType = new CoordType();
		coordType.setX(BigDecimal.valueOf(point.getX()));
		coordType.setY(BigDecimal.valueOf(point.getY()));
		p.setCoord(coordType);
		DirectPositionType directPositionType = new DirectPositionType();
		directPositionType.setSrsDimension(BG_2);
		List<Double> doubles = new LinkedList<Double>();
		doubles.add(point.getX());
		doubles.add(point.getY());
		directPositionType.setValue(doubles);
		p.setPos(directPositionType);

		position.setPoint(p);

		JAXBElement<PositionType> jaxb = new JAXBElement<PositionType>(
				new QName("http://www.opengis.net/xls", "Position", ""),
				PositionType.class, position);
		return jaxb;
	}
}
