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
package org.gofleetls.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import net.opengis.xls.v_1_2_0.DetermineRouteResponseType;
import net.opengis.xls.v_1_2_0.XLSType;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * @author marias
 * 
 */
public class OpenLSClientTest {

	private String url = "http://gofre:8081/openLS/";
	private PrecisionModel pm = new PrecisionModel(PrecisionModel.FIXED);
	private GeometryFactory gf = new GeometryFactory(pm, 4326);

	private double x = -3.7055;
	private double y = 40.4202;
	private Random r = new Random();

	/**
	 * Test method for
	 * {@link org.gofleetls.client.OpenLSClient#post(net.opengis.xls.v_1_2_0.XLSType, java.lang.String)}
	 * .
	 * 
	 * @throws JAXBException
	 */
	@Test
	@SuppressWarnings({ "restriction", "unchecked" })
	public void testPostXML() throws JAXBException {
		Unmarshaller m = JAXBContext.newInstance(XLSType.class)
				.createUnmarshaller();
		JAXBElement<XLSType> object = (JAXBElement<XLSType>) m
				.unmarshal(OpenLSClientTest.class
						.getResourceAsStream("/determineRouteRequest.xml"));

		assertNotNull(OpenLSClient.post(object.getValue(), url));
	}

	/**
	 * Test method for {@link OpenLSClient#determineRoute(String, Point, List)}
	 * 
	 */
	@Test
	public void testSimplestRouting() {
		Point origin = new Point(new CoordinateArraySequence(
				new Coordinate[] { new Coordinate(x, y) }), gf);
		List<Point> stops = new LinkedList<Point>();

		for (int i = 0; i < 5; i++) {
			double d = r.nextDouble() / 8;
			if (r.nextBoolean())
				d = -d;

			double d2 = r.nextDouble() / 8;
			if (r.nextBoolean())
				d2 = -d2;

			Point p = new Point(new CoordinateArraySequence(
					new Coordinate[] { new Coordinate(x + d, y + d2) }), gf);
			stops.add(p);
		}

		DetermineRouteResponseType determineRoute = OpenLSClient
				.determineRoute(url, origin, stops);
		assertNotNull(determineRoute);

		assertNotNull(determineRoute.getRouteGeometry().getLineString());

		assertTrue(determineRoute.getRouteGeometry().getLineString()
				.getPosList().getValue().size() > 0);
	}
}
