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
import java.net.MalformedURLException;
import java.nio.charset.Charset;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class NodeReader {

	/**
	 * Method processNodes: write in a temporal file the nodes in
	 * a path
	 * 
	 * @param pathNodes
	 *            : String with the shapefile Junctions path
	 * @param tempNodes
	 *            : String with the file path to write the nodes XML text
	 * 
	 */
	public static void processNodes(String pathNodes, File tempNodes){
		Coordinate[] coordinates = null;
		FileWriter fw = null;
		String lon = "";
		String lat = "";
		long id = 0;
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		ShapefileReader shapefilereader = null;
		DbaseFileReader dbfilereader = null;
		GeometryFactory gf = new GeometryFactory();
		try {
			shpFile = new ShpFiles(pathNodes);
			shapefilereader = new ShapefileReader(shpFile, true,
					useMemoryMapped, gf);
			dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped,
					Charset.defaultCharset());
			fw = new FileWriter(tempNodes);
			while (shapefilereader.hasNext() || dbfilereader.hasNext()) {
				Row rdb = dbfilereader.readRow();
				String id_s = rdb.read(0).toString();
				id = Long.valueOf(id_s);
				Record recordShapefile = shapefilereader.nextRecord();
				Object shape = recordShapefile.shape();
				if (shape instanceof Point) {
					coordinates = ((Point) shape).getCoordinates();
					if (coordinates.length > 0) {
						// The geometry have more than 2 elements
						// (source/target)
						for (int i = 0; i < coordinates.length; i++) {
							lon = Double.toString(coordinates[i].x);
							lat = Double.toString(coordinates[i].y);
							// Add to tempNodes file
							writeNodes(fw, id, lat, lon);
						}
					}
				}
			}
			fw.flush();
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
				shapefilereader.close();
				dbfilereader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Method writeNodes: Method to write a XML text with the node OSM format
	 * 
	 * @param fw: FileWriter where it's written the XML text
	 * @param ref: long with the node id
	 * @param lat: String with the latitude of the coordinate from a geometry
	 * @param lon: String with the longitude of the coordinate from a geometry
	 */
	public static void writeNodes(FileWriter fw, long ref, String lat,
			String lon) {
		try {
			fw.write("<node id=\""
					+ ref
					+ "\" lat=\""
					+ lat
					+ "\" lon=\""
					+ lon
					+ "\""
					+ " version=\"1\" changeset=\"1\" user=\"teleAtlas2osm\" uid=\"1\" visible=\"true\""
					+ " timestamp=\"2007-01-28T11:40:26Z\"/>\n");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
}
