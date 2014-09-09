package org.gofleet.openLS.util;

import java.util.LinkedList;
import java.util.List;

import net.opengis.gml.v_3_1_1.AbstractRingPropertyType;
import net.opengis.gml.v_3_1_1.CoordType;
import net.opengis.gml.v_3_1_1.DirectPositionType;
import net.opengis.gml.v_3_1_1.LinearRingType;
import net.opengis.gml.v_3_1_1.PointType;
import net.opengis.gml.v_3_1_1.PolygonType;
import net.opengis.xls.v_1_2_0.AddressType;
import net.opengis.xls.v_1_2_0.NamedPlaceType;
import net.opengis.xls.v_1_2_0.PositionType;
import net.opengis.xls.v_1_2_0.StreetNameType;
import net.opengis.xls.v_1_2_0.WayPointType;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/*
 * Copyright (C) 2011, Emergya (http://www.emergya.es)
 *
 * @author <a href="mailto:marias@emergya.es">María Arias</a>
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
public class GeoUtil {
	static Log LOG = LogFactory.getLog(GeoUtil.class);
	static GeometryFactory geomFact = new GeometryFactory();

	public static String extractStreet(AddressType address) {
		String res = null;

		try {
			if (address.getStreetAddress() != null) {
				List<StreetNameType> street = address.getStreetAddress()
						.getStreet();
				for (StreetNameType snt : street)
					if (snt != null && snt.getValue() != null)
						res = snt.getValue();
			}
		} catch (Throwable t) {
			LOG.error("Error extracting Street from parameters. Failing back to null");
			res = null;
		}

		return res;
	}

	public static String extractMun(AddressType address) {
		String res = null;

		try {
			for (NamedPlaceType place : address.getPlace()) {
				try {
					if (StringUtils.equalsIgnoreCase(place.getType().name(),
							("Municipality"))
							|| StringUtils.equalsIgnoreCase(place.getType()
									.toString(), ("Municipality")))
						res = place.getValue();
					break;
				} catch (Throwable t) {
					LOG.error(t);
				}
			}
		} catch (Throwable t) {
			LOG.error("Error extracting Mun from parameters", t);
			res = null;
		}

		return res;
	}

	public static String extractMunSub(AddressType address) {
		String res = null;

		try {
			for (NamedPlaceType place : address.getPlace()) {
				try {
					if (StringUtils.equalsIgnoreCase(place.getType().name(),
							("MunicipalitySubdivision"))
							|| StringUtils.equalsIgnoreCase(place.getType()
									.toString(), ("MunicipalitySubdivision")))
						res = place.getValue();
					break;
				} catch (Throwable t) {
					LOG.error(t);
				}
			}
		} catch (Throwable t) {
			LOG.error("Error extracting MunSub from parameters", t);
		}

		return res;
	}

	public static String extractCountry(AddressType address) {
		String res = address.getCountryCode();

		try {
			res = address.getCountryCode();
		} catch (Throwable t) {
			LOG.error("Error extracting Country from parameters", t);
		}

		return res;
	}

	public static String extractSubCountry(AddressType address) {
		String res = null;

		try {
			for (NamedPlaceType place : address.getPlace()) {
				try {
					if (StringUtils.equalsIgnoreCase(place.getType().name(),
							("CountrySubdivision"))
							|| StringUtils.equalsIgnoreCase(place.getType()
									.toString(), ("CountrySubdivision")))
						res = place.getValue();
					break;
				} catch (Throwable t) {
					LOG.error(t);
				}
			}
		} catch (Throwable t) {
			LOG.error("Error extracting SubCountry from parameters", t);
		}
		return res;
	}

	public static com.vividsolutions.jts.geom.Point getPoint(
			WayPointType startPoint, CoordinateReferenceSystem targetCRS) {

		// TODO what if we don't receive coordinates?
		PositionType ptype = (PositionType) startPoint.getLocation().getValue();
		PointType pointType = ptype.getPoint();
		DirectPositionType ctype = pointType.getPos();

		CoordinateReferenceSystem sourceCRS = getSRS(startPoint);

		LOG.trace(sourceCRS.toWKT());
		LOG.trace("(" + ctype.getValue().get(0) + ", "
				+ ctype.getValue().get(1) + ")");

		com.vividsolutions.jts.geom.Point p = geomFact
				.createPoint(new Coordinate(ctype.getValue().get(0), ctype
						.getValue().get(1)));

		LOG.info(p);
		if (targetCRS != null && !sourceCRS.equals(targetCRS)) {
			try {
				MathTransform transform = CRS.findMathTransform(sourceCRS,
						targetCRS);
				p = JTS.transform(p, transform).getCentroid();

				p = geomFact.createPoint(new Coordinate(p.getY(), p.getX()));

				LOG.info(p);
			} catch (Throwable t) {
				LOG.error("Error converting coordinates", t);
			}
		}

		return p;
	}

	public static CoordinateReferenceSystem getSRS(WayPointType point) {
		// TODO what if we don't receive coordinates?
		PositionType ptype = (PositionType) point.getLocation().getValue();
		PointType pointType = ptype.getPoint();
		try {
			return CRS.decode(pointType.getSrsName());
		} catch (Throwable e) {
			LOG.trace(e, e);
			try {
				return CRS.decode("EPSG:4326");
			} catch (Throwable t) {
				LOG.trace(t);
				return null;
			}
		}
	}

	public static com.vividsolutions.jts.geom.Geometry getGeometry(
			PositionType position) {

		Geometry g = null;

		if (position.getPoint() != null) {
			if (position.getPoint().getCoord() != null
					&& position.getPoint().getCoord().getX() != null) {
				g = geomFact.createPoint(new Coordinate(position.getPoint()
						.getCoord().getX().doubleValue(), position.getPoint()
						.getCoord().getY().doubleValue()));
			} else if (position.getPoint().getPos() != null
					&& position.getPoint().getPos().getValue() != null
					&& position.getPoint().getPos().getValue().size() == 2) {
				g = geomFact.createPoint(new Coordinate(position.getPoint()
						.getPos().getValue().get(0), position.getPoint()
						.getPos().getValue().get(1)));
			}
		} else if (position.getPolygon() != null) {
			PolygonType polygon = position.getPolygon();

			List<LinearRing> interiorRings = new LinkedList<LinearRing>();
			polygon.getInterior();
			// TODO
			LinearRing[] holes = interiorRings.toArray(new LinearRing[] {});

			List<Coordinate> coordinateList = new LinkedList<Coordinate>();
			AbstractRingPropertyType exterior = polygon.getExterior()
					.getValue();
			LinearRingType ring = (LinearRingType) exterior.getRing()
					.getValue();
			for (CoordType coord : ring.getCoord()) {
				coordinateList.add(new Coordinate(coord.getX().doubleValue(),
						coord.getY().doubleValue()));
			}
			Coordinate[] coordinates = coordinateList
					.toArray(new Coordinate[] {});
			LinearRing shell = geomFact.createLinearRing(coordinates);
			g = geomFact.createPolygon(shell, holes);
		}
		return g;
	}
}
