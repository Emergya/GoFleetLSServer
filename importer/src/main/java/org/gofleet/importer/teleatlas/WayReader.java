package org.gofleet.importer.teleatlas;

/**
 * Copyright (C) 2012, Emergya (http://www.emergya.com)
 *
 * @author <a href="mailto:marcos@emergya.com">Moisés Arcos Santiago</a>
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * As a special exception, if you link this library with other files to
 * produce an executable, this library does not by itself cause the
 * resulting executable to be covered by the GNU General Public License.
 * This exception does not however invalidate any other reasons why the
 * executable file might be covered by the GNU General Public License.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;

public class WayReader {

	private static long value = 57240000000000l;
	private static Map<Long, Integer> idMap;

	/**
	 * Method processWays: write in a temporal file the ways in a path
	 * 
	 * @param pathWays
	 *            : String with the shapefile Networks path
	 * @param tempWays
	 *            : String with the file path to write the ways XML text
	 * @param tempNodes
	 *            : String with the file path to write the nodes XML text
	 * 
	 */
	public static void processWays(String pathWays, File tempWays,
			File tempNodes) {
		idMap = NodeReader.getIDMap();
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		GeometryFactory gf = new GeometryFactory();
		Row rdb = null;
		Record recordShapefile = null;
		Map<String, String> attributes = null;
		DbaseFileReader dbfilereader = null;
		ShapefileReader shapefilereader = null;
		FileWriter frWays = null;
		FileWriter frNodes = null;
		try {
			// Open file
			frWays = new FileWriter(tempWays);
			frNodes = new FileWriter(tempNodes);
			shpFile = new ShpFiles(pathWays);
			dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped,
					Charset.defaultCharset());
			shapefilereader = new ShapefileReader(shpFile, true,
					useMemoryMapped, gf);
			String lon = "";
			String lat = "";
			int idRef = idMap.size() + 1000;
			while (dbfilereader.hasNext() || shapefilereader.hasNext()) {
				// Define a list object with the lonlat nodes
				List<String> nodes = new LinkedList<String>();
				// read a row
				rdb = dbfilereader.readRow();
				// get the attributes from this row
				attributes = getAttributesNetwork(rdb);
				recordShapefile = shapefilereader.nextRecord();
				Object shape = recordShapefile.shape();
				if (shape instanceof MultiLineString) {
					Coordinate[] geom = ((MultiLineString) shape)
							.getCoordinates();
					if (geom.length > 2) {
						// The geometry have more than 2 elements
						// (source/target)
						for (int i = 1; i < geom.length - 1; i++) {
							lon = Double.toString(geom[i].x);
							lat = Double.toString(geom[i].y);
							// Add to tempWays file
							String node = "	<nd ref=\"" + idRef + "\"/>\n";
							nodes.add(node);
							// Add to tempNodes file
							if (tempNodes.exists()) {
								NodeReader.writeNodes(frNodes, idRef, lat, lon, 1);
							}
							idRef++;
						}
					}
				}
				// check way restrictions
				if (!attributes.get("FRC").equals("-1")
						&& !attributes.get("ONEWAY").equals("N")) {
					String way = writeWay(attributes, nodes);
					if (tempWays.exists()) {
						frWays.write(way);
					}
				}
			}
			frNodes.flush();
			frWays.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				frWays.close();
				frNodes.close();
				shapefilereader.close();
				dbfilereader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method getAttributesManeuvers: Method to get Network attributes from a
	 * Network shape
	 * 
	 * @param rdb
	 *            : Row with the content of a shapefile Network line
	 * @return Map<String, String>: Map with attributes from the row and its
	 *         values
	 */
	public static Map<String, String> getAttributesNetwork(Row rdb) {
		Map<String, String> attributes = new HashMap<String, String>();
		try {
			attributes.put("ID", rdb.read(0).toString());
			attributes.put("FEATTYP", rdb.read(1).toString());
			attributes.put("FT", rdb.read(2).toString());
			attributes.put("F_JNCTID", rdb.read(3).toString());
			attributes.put("F_JNCTTYP", rdb.read(4).toString());
			attributes.put("T_JNCTID", rdb.read(5).toString());
			attributes.put("T_JNCTTYP", rdb.read(6).toString());
			attributes.put("PJ", rdb.read(7).toString());
			attributes.put("METERS", rdb.read(8).toString());
			attributes.put("FRC", rdb.read(9).toString());
			attributes.put("NETCLASS", rdb.read(10).toString());
			attributes.put("NETBCLASS", rdb.read(11).toString());
			attributes.put("NET2CLASS", rdb.read(12).toString());
			attributes.put("NAME", rdb.read(13).toString());
			attributes.put("NAMELC", rdb.read(14).toString());
			attributes.put("SOL", rdb.read(15).toString());
			attributes.put("NAMETYP", rdb.read(16).toString());
			attributes.put("CHARGE", rdb.read(17).toString());
			attributes.put("SHIELDNUM", rdb.read(18).toString());
			attributes.put("RTETYP", rdb.read(19).toString());
			attributes.put("RTEDIR", rdb.read(20).toString());
			attributes.put("RTEDIRVD", rdb.read(21).toString());
			attributes.put("PROCSTAT", rdb.read(22).toString());
			attributes.put("FOW", rdb.read(23).toString());
			attributes.put("SLIPRD", rdb.read(24).toString());
			attributes.put("FREEWAY", rdb.read(25).toString());
			attributes.put("BACKRD", rdb.read(26).toString());
			attributes.put("TOLLRD", rdb.read(27).toString());
			attributes.put("RDCOND", rdb.read(28).toString());
			attributes.put("STUBBLE", rdb.read(29).toString());
			attributes.put("PRIVATERD", rdb.read(30).toString());
			attributes.put("CONSTATUS", rdb.read(31).toString());
			attributes.put("ONEWAY", rdb.read(32).toString());
			attributes.put("F_BP", rdb.read(33).toString());
			attributes.put("T_BP", rdb.read(34).toString());
			attributes.put("F_ELEV", rdb.read(35).toString());
			attributes.put("T_ELEV", rdb.read(36).toString());
			attributes.put("KPH", rdb.read(37).toString());
			attributes.put("MINUTES", rdb.read(38).toString());
			attributes.put("POSACCUR", rdb.read(39).toString());
			attributes.put("CARRIAGE", rdb.read(40).toString());
			attributes.put("LANES", rdb.read(41).toString());
			attributes.put("RAMP", rdb.read(42).toString());
			attributes.put("ADAS", rdb.read(43).toString());
			attributes.put("TRANS", rdb.read(44).toString());
			attributes.put("DYNSPEED", rdb.read(45).toString());
			attributes.put("SPEEDCAT", rdb.read(46).toString());
			attributes.put("NTHRUTRAF", rdb.read(47).toString());
			attributes.put("ROUGHRD", rdb.read(48).toString());
			attributes.put("PARTSTRUC", rdb.read(49).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attributes;
	}

	/**
	 * Method writeWay: return a string with the XML text to write a way in a
	 * OSM format
	 * 
	 * @param atributos
	 *            : Map with the attributes value read from the Network
	 *            shapefile
	 * @param nodes
	 *            : List with the nodes to add to the XML
	 * @return String: String with the XML text
	 */
	public static String writeWay(Map<String, String> atributos,
			List<String> nodes) {
		String way = "<way";
		long idWay = Long.valueOf(atributos.get("ID"));
		idWay = idWay - value;
		way += " id=\"" + idWay + "\"";
		if (!atributos.containsKey("user")) {
			way += " user=\"teleAtlas2osm\"";
		}
		if (!atributos.containsKey("uid")) {
			way += " uid=\"1\"";
		}
		if (!atributos.containsKey("timestamp")) {
			way += " timestamp=\"2012-01-19T19:07:25Z\"";
		}
		if (!atributos.containsKey("visible")) {
			way += " visible=\"true\"";
		}
		if (!atributos.containsKey("version")) {
			way += " version=\"1\"";
		}
		if (!atributos.containsKey("changeset")) {
			way += " changeset=\"0\"";
		}
		way += ">\n";
		// Change the type of the id nodes, because it must be an Integer
		String from_junct_id = atributos.get("F_JNCTID");
		String to_junct_id = atributos.get("T_JNCTID");
		Integer idValueFrom = idMap.get(from_junct_id);
		Integer idValueTo = idMap.get(to_junct_id);
		way += "	<nd ref=\"" + idValueFrom + "\"/>\n";
		// Add the nodes from geometry
		if (!nodes.isEmpty()) {
			for (String s : nodes) {
				way += s;
			}
		}
		way += "	<nd ref=\"" + idValueTo + "\"/>\n";
		String frc = getFRC(atributos.get("FRC"));
		way += "	<tag k=\"highway\" v=\"" + frc + "\"/>\n";
		way += "	<tag k=\"name\" v=\"" + atributos.get("NAME") + "\"/>\n";
		String oneway = getOneWay(atributos.get("ONEWAY"));
		way += "	<tag k=\"oneway\" v=\"" + oneway + "\"/>\n";
		way += "</way>\n";
		return way;
	}

	/**
	 * Method getFRC: Get a osm format string with the FRC value
	 * 
	 * @param parameter
	 *            : String with the number value from the Functional Road Class
	 * 
	 * @return String: String with the value in osm format
	 */
	private static String getFRC(String parameter) {
		String frc = "";
		if (parameter.equals("0")) {
			frc = "motorway";
		} else if (parameter.equals("1")) {
			frc = "trunk";
		} else if (parameter.equals("2")) {
			frc = "trunk_link";
		} else if (parameter.equals("3")) {
			frc = "secondary";
		} else if (parameter.equals("4")) {
			frc = "secondary_link";
		} else if (parameter.equals("5")) {
			frc = "tertiary";
		} else if (parameter.equals("6")) {
			frc = "tertiary_link";
		} else if (parameter.equals("7")) {
			frc = "service";
		} else if (parameter.equals("8")) {
			frc = "residential";
		}
		return frc;
	}

	/**
	 * Method getOneWay: Get a osm format string with the ONEWAY value
	 * 
	 * @param parameter
	 *            : String with the number value from the ONEWAY parameter
	 * 
	 * @return String: String with the value in osm format
	 */
	private static String getOneWay(String parameter) {
		String oneway = "";
		if (parameter.equals("")) {
			oneway = "no";
		} else if (parameter.equals("FT")) {
			oneway = "yes";
		} else if (parameter.equals("TF")) {
			oneway = "-1";
		}
		return oneway;
	}

}
