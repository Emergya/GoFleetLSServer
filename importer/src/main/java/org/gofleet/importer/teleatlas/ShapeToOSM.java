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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.geotools.data.shapefile.shp.ShapefileException;

public class ShapeToOSM {

	private static long ref = 1;

	public static void main(final String[] args) {

		try {
			// Make a temporal file to store the ways, the nodes and the
			// restrictions
			final File tempWays = File.createTempFile("tempWays", null);
			final File tempNodes = File.createTempFile("tempNodes", null);
			final File tempNodesFromWays = File.createTempFile(
					"tempNodesFromWays", null);
			final File tempRestrictions = File.createTempFile(
					"tempRestrictions", null);

			ExecutorService executor = Executors.newFixedThreadPool(10);

			Thread t = new Thread() {
				public void run() {
					RestrictionReader.processRestrictions(args[2], args[3],
							args[4], tempRestrictions);
				};
			};
			
			Thread tway = new Thread() {
				public void run() {
					WayReader.processWays(args[0], tempWays, tempNodesFromWays);
				};
			};

			Thread tnode = new Thread() {
				public void run() {
					NodeReader.processNodes(args[1], tempNodes);
				};
			};
			
			long time = System.currentTimeMillis();
			System.out.println("Start at: " + new Date(System.currentTimeMillis()));
			executor.execute(t);
			executor.execute(tway);
			executor.execute(tnode);
			
			executor.shutdown();
		
			executor.awaitTermination(3, TimeUnit.DAYS);
			
			System.out.println("Finished all threads at: " + new Date(System.currentTimeMillis()));
			System.out.println("Total Time: " + (System.currentTimeMillis() - time) / 1000 + " sec");
			
			// Read temporal files to make osm file
			File f = new File(args[5]);

			writeOSMFile(tempWays, tempNodes, tempNodesFromWays,
					tempRestrictions, f);

			// Delete temporal file
			tempWays.delete();
			tempNodes.delete();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ShapefileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static void writeOSMFile(File ways, File nodes, File nodesFromWays,
			File restrictions, File fosm) {
		FileReader frTempWays;
		FileReader frTempNodes;
		FileReader frTempNodesFromWays;
		FileReader frTempRestrictions;
		FileWriter fwOSM;
		String osmHeader = "<osm version='0.6' generator='TeleAtlas2OSM'>\n";
		String osmTail = "</osm>";
		try {
			// Open files
			frTempWays = new FileReader(ways);
			frTempNodes = new FileReader(nodes);
			frTempNodesFromWays = new FileReader(nodesFromWays);
			frTempRestrictions = new FileReader(restrictions);
			fwOSM = new FileWriter(fosm);

			// Write osm header
			fwOSM.write(osmHeader);

			// Write osm nodes
			readToWrite(frTempNodes, fwOSM);
			readToWrite(frTempNodesFromWays, fwOSM);

			// Write osm ways
			readToWrite(frTempWays, fwOSM);

			// Write osm restrictions
			readToWrite(frTempRestrictions, fwOSM);

			// Write osm tile
			fwOSM.write(osmTail);

			// Close files
			frTempWays.close();
			frTempNodes.close();
			frTempNodesFromWays.close();
			frTempRestrictions.close();
			fwOSM.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void readToWrite(FileReader fr, FileWriter fw) {
		BufferedReader br = null;
		String linea;
		try {
			br = new BufferedReader(fr);
			while ((linea = br.readLine()) != null)
				fw.write(linea);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
