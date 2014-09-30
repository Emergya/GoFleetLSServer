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
package org.gofleet.openLS.monav;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import net.opengis.gml.v_3_1_1.DirectPositionListType;
import net.opengis.gml.v_3_1_1.LineStringType;
import net.opengis.xls.v_1_2_0.DetermineRouteRequestType;
import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.RouteGeometryType;
import net.opengis.xls.v_1_2_0.RouteHandleType;
import net.opengis.xls.v_1_2_0.RouteInstructionsListType;
import net.opengis.xls.v_1_2_0.RouteMapType;
import net.opengis.xls.v_1_2_0.RouteSummaryType;
import net.opengis.xls.v_1_2_0.WayPointListType;
import net.opengis.xls.v_1_2_0.WayPointType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import org.gofleet.openLS.handlers.RoutingHandler;
import org.gofleet.openLS.util.GeoUtil;
import org.springframework.stereotype.Service;

/**
 * @author marias
 * 
 */
@Service()
public class MoNaVConnector implements RoutingHandler{

	private static Log LOG = LogFactory.getLog(MoNaVConnector.class);
	private GeometryFactory gf = new GeometryFactory();
	private Unmarshaller unmarshaller;
	private JAXBContext context;
	private Marshaller marshaller;

	/**
	 * 
	 */
	public MoNaVConnector() {
		try {
			unmarshaller = JAXBContext.newInstance(LineStringType.class)
					.createUnmarshaller();
			context = JAXBContext.newInstance("org.jvnet.ogc.gml.v_3_1_1.jts");
			marshaller = context.createMarshaller();
		} catch (Throwable e) {
			LOG.error("Unable to load MoNaV connector" , e);
		}
	}

	/**
	 * Route plan using monav daemon. Only geometry.
	 * 
	 * @param param
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 * @throws JAXBException
	 * @throws InterruptedException
	 */
    @Override
	public DetermineRouteResponseType routePlan(
			DetermineRouteRequestType param, int maxResponses)  {
		DetermineRouteResponseType res = new DetermineRouteResponseType();
		Double cost = null;
		String cmd = "/opt/monav/bin/daemon-test /var/www/monav/routing_yes/";

		RouteGeometryType routeGeometry = new RouteGeometryType();

		WayPointListType wayPointList = param.getRoutePlan().getWayPointList();

		Point point = GeoUtil.getPoint(wayPointList.getStartPoint(), null);
		cmd += " " + point.getY() + " " + point.getX();

		for (WayPointType wayPoint : wayPointList.getViaPoint()) {
			point = GeoUtil.getPoint(wayPoint, null);
			cmd += " " + point.getY() + " " + point.getX();
		}

		point = GeoUtil.getPoint(wayPointList.getEndPoint(), null);
		cmd += " " + point.getY() + " " + point.getX();

		LOG.debug(cmd);

		Process p;
        try {
            p = Runtime.getRuntime().exec(cmd);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

		BufferedReader br = new BufferedReader(new InputStreamReader(
				p.getErrorStream()));

		LineStringType lst = new LineStringType();

		List<JAXBElement<?>> list = new LinkedList<JAXBElement<?>>();

		String l;
        try {
            l = br.readLine();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
		while (l != null) {
			LOG.info(l);
			try {
				if (cost == null && l.indexOf("distance:") >= 0)
					cost = -1d;// TODO

				Double x = Double.parseDouble(l.substring(0, l.indexOf(" ")));
				Double y = Double.parseDouble(l.substring(1 + l.indexOf(" ")));

				DirectPositionListType e = new DirectPositionListType();
				e.getValue().add(y);
				e.getValue().add(x);

				JAXBElement<DirectPositionListType> elem = new JAXBElement<DirectPositionListType>(
						new QName("http://www.opengis.net/gml", "pos", "gml"),
								DirectPositionListType.class, e);
				
				list.add(elem);

			} catch (Throwable t) {
				// LOG.error(t);
			}
            
            
			try{
                l = br.readLine();
            } catch(IOException ex) {
                throw new RuntimeException(ex);
            }
		}

		try {

			lst.setPosOrPointPropertyOrPointRep(list);
			routeGeometry.setLineString(lst);
			res.setRouteGeometry(routeGeometry);
			res.setRouteHandle(new RouteHandleType());
			res.setRouteInstructionsList(new RouteInstructionsListType());
			res.setRouteMap(new LinkedList<RouteMapType>());
			res.setRouteSummary(new RouteSummaryType());
		} catch (Throwable t) {
			LOG.error("Error generating route response: " + t, t);
		}
		return res;
	}
}
