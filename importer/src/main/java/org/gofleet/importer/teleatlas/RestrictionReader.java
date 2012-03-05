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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.dbf.DbaseFileReader.Row;

public class RestrictionReader {

	/**
	 * Method processRestrictions: write in a temporal file the restrictions in
	 * a path
	 * 
	 * @param pathManeuvers
	 *            : String with the shapefile maneuvers path
	 * @param pathManeuversPath
	 *            : String with the shapefile maneuvers_path_index path
	 * @param pathRestrictions
	 *            : String with the shapefile restrictions path
	 * @param file
	 *            : String with the file path to write the result
	 * 
	 */
	public static void processRestrictions(String pathManeuvers,
			String pathManeuversPath, String pathRestrictions, File file) {
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		Row rdb = null;
		Map<String, String> attributes = null;
		DbaseFileReader dbfilereader = null;
		FileWriter fw = null;
		try {
			// Open file
			fw = new FileWriter(file);
			shpFile = new ShpFiles(pathManeuvers);
			dbfilereader = new DbaseFileReader(shpFile,
					useMemoryMapped, Charset.defaultCharset());
			while (dbfilereader.hasNext()) {
				rdb = dbfilereader.readRow();
				attributes = getAttributesManeuvers(rdb);
				// Attributes restrictions
				if (attributes.get("FEATTYP").equals("2103")
						&& attributes.get("PROMANTYP").equals("0")) {
					long id = Long.valueOf(attributes.get("ID"));
					long junctID = Long.valueOf(attributes.get("JNCTID"));
					// Get from way and to way from maneuvers path index table
					List<String> sequence = getSequence(id, pathManeuversPath);
					String restrictions = getRestriction(id, pathRestrictions);
					if (sequence.size() == 2) {
						long from = Long.valueOf(sequence.get(0));
						long to = Long.valueOf(sequence.get(1));
						String rest = writeRestriction(id, junctID, from, to,
								restrictions);
						fw.write(rest);
						fw.flush();
					} // TODO length > 2
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				fw.close();
				dbfilereader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Method getAttributesManeuvers: Method to get Maneuvers attributes from a
	 * Maneuvers shape
	 * 
	 * @param rdb: Row with the content of a shapefile Maneuvers line
	 * @return Map<String, String>: Map with attributes from the row and its
	 *         values
	 */
	private static Map<String, String> getAttributesManeuvers(Row rdb) {
		Map<String, String> attributes = new HashMap<String, String>();
		try {
			attributes.put("ID", rdb.read(0).toString());
			attributes.put("FEATTYP", rdb.read(1).toString());
			attributes.put("BIFTYP", rdb.read(2).toString());
			attributes.put("PROMANTYP", rdb.read(3).toString());
			attributes.put("JNCTID", rdb.read(4).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attributes;
	}

	/**
	 * Method getAttributesManeuversPath: Method to get Maneuvers_Path_Index
	 * attributes from a Maneuvers_Path_Index shape
	 * 
	 * @param rdb: Row with the content of a shapefile Maneuvers_Path_Index line
	 * @return Map<String, String>: Map with attributes from the row and its values
	 */
	private static Map<String, String> getAttributesManeuversPath(Row rdb) {
		Map<String, String> attributes = new HashMap<String, String>();
		try {
			attributes.put("ID", rdb.read(0).toString());
			attributes.put("SEQNR", rdb.read(1).toString());
			attributes.put("TRPELID", rdb.read(2).toString());
			attributes.put("TRPELTYP", rdb.read(3).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attributes;
	}

	/**
	 * Method getAttributesRestrictions: Method to get Restrictions
	 * attributes from a Restrictions shape
	 * 
	 * @param rdb: Row with the content of a shapefile Restrictions line
	 * @return Map<String, String>: Map with attributes from the row and its values
	 */
	private static Map<String, String> getAttributesRestrictions(Row rdb) {
		Map<String, String> attributes = new HashMap<String, String>();
		try {
			attributes.put("ID", rdb.read(0).toString());
			attributes.put("SEQNR", rdb.read(1).toString());
			attributes.put("FEATTYP", rdb.read(2).toString());
			attributes.put("RESTRTYP", rdb.read(3).toString());
			attributes.put("RESTRVAL", rdb.read(4).toString());
			attributes.put("VT", rdb.read(5).toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return attributes;
	}
	
	/**
	 * Method writeRestriction: return a string with the XML text to write a relation OSM
	 * 
	 * @param idRestriction: long with the id restriction value read from the shapefile
	 * @param junctID: long with the id node value read from the shapefile
	 * @param from: long with the id from node value read from the shapefile
	 * @param to: long with the id to node value read from the shapefile 
	 * @param tags: String with the XML text to write every tag from the shapefile read
	 * @return String: Text with the XML content of a relation OSM
	 */
	private static String writeRestriction(long idRestriction, long junctID,
			long from, long to, String tags) {
		String relationHeader = "<relation changeset=\"1\" " + "uid=\"1\" "
				+ "timestamp=\"2012-02-19T19:07:25Z\" " + "version=\"1\" "
				+ "user=\"TeleAtlas2OSM\" " + "id=\"" + idRestriction + "\">\n";

		String memberWayFrom = "<member type=\"way\" ref=\"" + from
				+ "\" role=\"from\"/>\n";

		String memberWayTo = "<member type=\"way\" ref=\"" + to
				+ "\" role=\"to\"/>\n";

		String memberNode = "<member type=\"node\" " + "ref=\"" + junctID
				+ "\" " + "role=\"via\"/>\n";

		String relationTail = "</relation>\n";
		String tagType = "<tag k=\"type\" v=\"restriction\"/>\n";
		return relationHeader + memberWayFrom + memberWayTo + memberNode
				+ tagType + tags + relationTail;
	}
	
	/**
	 * Method writeTag: return a string with the XML text to write a Tag OSM
	 * 
	 * @param rt: String with the RESTTYP value
	 * @param rv: String with the RESTVAL value
	 * @param vt: String with the VT value
	 * @return String: String qith XML text
	 */
	private static String writeTag(String rt, String rv, String vt) {
		String value[] = new String[2];
		String tagRestriction = "";
		String tagExcept = "";
		if (rt.equals("6Z") || rt.equals("TR") || rt.equals("4B")) {

		} else {
			// Check all posibles cases
			if (rt.equals("")) {
				value = checkBlank(rv, vt);
			} else if (rt.equals("BP")) {
				value = checkBP(rv, vt);
			} else if (rt.equals("RB")) {
				value = checkRB(rv, vt);
			} else if (rt.equals("SR")) {
				value = checkSR(rv, vt);
			} else if (rt.equals("8I")) {
				value = check8I(rv, vt);
			} else if (rt.equals("DF") || rt.equals("6Q")) {
				value = checkDF6Q(rv, vt);
			}
			if (value[0] != "") {
				tagRestriction = "<tag k=\"restriction\" v =\"" + value[0]
						+ "\"/>\n";
			}
			if (value[1] != "") {
				tagExcept = "<tag k=\"except\" v=\"" + value[1] + "\"/>\n";
			}

		}
		return tagRestriction + tagExcept;
	}

	/**
	 * Method checkVT: Method to check the VT value and return the string with the equivalent OSM VT value 
	 * 
	 * @param valueIN: String with the previous VT
	 * @param vt: String with the VT read from the shapefile
	 * @return String: String with the OSM VT value
	 */
	private static String checkVT(String valueIN, String vt) {
		String res = "";
		// check VT value
		if (vt.equals("-1")) {
			res = "";
		} else if (vt.equals("0")) {
			// if it's not empty --> ;
			if (valueIN != "") {
				res = valueIN + ";bicycle";
			} else {
				res = "bicycle";
			}
		} else if (vt.equals("11") || vt.equals("12")) {
			// if it's not psv --> psv;bicycle
			if (valueIN != "") {
				if (valueIN != "psv") {
					res = "psv;bicycle";
				} else {
					res = valueIN + ";bicycle";
				}
			} else {
				res = "psv;bicycle";
			}
		} else if (vt.equals("16") || vt.equals("17")) {
			// If it's not empty --> ;
			if (valueIN != "") {
				res = valueIN + ";motorcar;bicycle";
			} else {
				res = "motorcar;bicycle";
			}
		} else if (vt.equals("24")) {
			if (valueIN != "") {
				if (valueIN != "psv") {
					res = "psv;motorcar";
				} else {
					res = valueIN + ";motorcar";
				}
			} else {
				res = "psv;motorcar";
			}
		}
		return res;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is blank, 
	 * if is blank the OSM RESTVAL value should be 'no_straight_on'.
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] checkBlank(String rv, String vt) {
		String value[] = new String[2];
		value[0] = "no_straight_on";
		value[1] = "";
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is DF or 6Q
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] checkDF6Q(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if (rv.equals("2")) {
			value[0] = "no_entry";
			value[1] = "";
		} else if (rv.equals("3")) {
			value[0] = "no_exit";
			value[1] = "";
		} else if (rv.equals("4")) {
			// TODO Two restrictions
			value[0] = "";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is 8I
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] check8I(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if (rv.equals("0")) {
			value[0] = "no_straight_on ";
			value[1] = "";
		} else if (rv.equals("1")) {
			value[0] = "only_straight_on";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is SR
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] checkSR(String rv, String vt) {
		String value[] = new String[2];
		value[0] = "no_entry";
		value[1] = "psv";
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is RB
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] checkRB(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if (rv.equals("1") || rv.equals("3")) {
			value[0] = "no_entry";
			value[1] = "psv";
		} else if (rv.equals("2")) {
			value[0] = "no_entry";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	/**
	 * Method checkBlank: Method to check if the RESTVALUE is BP
	 * 
	 * @param rv: String with the RESTVALUE value read from the shapefile
	 * @param vt: String with the VT value read from the shapefile
	 * @return String[]: String Array with the restriction tag value and the except tag value
	 */
	private static String[] checkBP(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if (rv.equals("11") || rv.equals("13") || rv.equals("23")) {
			value[0] = "no_entry";
			value[1] = "psv";
		} else if (rv.equals("1") || rv.equals("2") || rv.equals("12")
				|| rv.equals("22")) {
			value[0] = "no_entry";
			value[1] = "";
		}
		value[1] = checkVT(value[1], vt);

		return value;
	}

	/**
	 * Method getSequence: Method to get the sequence of TRPLIDs in order 
	 * o obtain the source and target way
	 * 
	 * @param id: long with the id restriction value to compare with the ManeuversPath id
	 * @param path: String with the Maneuvers_Path_Index path 
	 * @return List<String>: List with the ids from source and target ways 
	 */
	private static List<String> getSequence(long id, String path) {
		List<String> trpelid = new LinkedList<String>();
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		Row rdb = null;
		Map<String, String> attributes = null;
		try {
			shpFile = new ShpFiles(path);
			DbaseFileReader dbfilereader = new DbaseFileReader(shpFile,
					useMemoryMapped, Charset.defaultCharset());
			while (dbfilereader.hasNext()) {
				rdb = dbfilereader.readRow();
				attributes = getAttributesManeuversPath(rdb);
				long idMP = Long.valueOf(attributes.get("ID"));
				if (id == idMP) {
					trpelid.add(attributes.get("TRPELID"));
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return trpelid;
	}

	/**
	 * Method getRestriction: Method to get a XML text with the equivalent OSM tag value 
	 * 
	 * @param id: long with the id restriction value to compare with the ManeuversPath id
	 * @param path: String with the Restrictions path 
	 * @return
	 */
	private static String getRestriction(long id, String path) {
		String rest = "";
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		Row rdb = null;
		Map<String, String> attributes = null;
		try {
			shpFile = new ShpFiles(path);
			DbaseFileReader dbfilereader = new DbaseFileReader(shpFile,
					useMemoryMapped, Charset.defaultCharset());
			while (dbfilereader.hasNext()) {
				rdb = dbfilereader.readRow();
				attributes = getAttributesRestrictions(rdb);
				long idMP = Long.valueOf(attributes.get("ID"));
				String feattyp = attributes.get("FEATTYP");
				if (id == idMP) {
					// Restriction to process
					if (feattyp.equals("2101") || feattyp.equals("2102")
							|| feattyp.equals("2103")) {
						String resttyp = attributes.get("RESTRTYP");
						String restval = attributes.get("RESTRVAL");
						String vt = attributes.get("VT");
						rest = writeTag(resttyp, restval, vt);
					}
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rest;
	}

}
