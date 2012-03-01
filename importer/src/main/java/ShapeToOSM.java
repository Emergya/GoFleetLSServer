import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class ShapeToOSM {

	private static long ref = 1;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		// Shapefile path
		String fileName = args[0];
		// make a shapefile object
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		// To read Network attributes
		DbaseFileReader dbfilereader = null;
		// To read shape geometries
		ShapefileReader shapefilereader = null;
		GeometryFactory gf = new GeometryFactory();
		Row rdb = null;
		Record recordShapefile = null;
		// Define a map object with the shape file attributes from way
		Map<String, String> attributes = new HashMap<String, String>();
		String way = "";
		try {
			// Make a temporal file to store the ways
			File tempWays = File.createTempFile("tempWays", null);
			File tempNodes = File.createTempFile("tempNodes", null);
			FileWriter frWays = new FileWriter(tempWays);
			FileWriter frNodes = new FileWriter(tempNodes);
			getNodesFromJunction(args[1], frNodes);
			shpFile = new ShpFiles(fileName);
			dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped,
					Charset.defaultCharset());
			shapefilereader = new ShapefileReader(shpFile, true,
					useMemoryMapped, gf);
			String lon = "";
			String lat = "";
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
							String node = "	<nd ref=\"" + ref + "\"/>\n";
							nodes.add(node);
							// Add to tempNodes file
							if (tempNodes.exists()) {
								writeNodes(frNodes, ref, lat, lon);
							}
							ref++;
						}
					}
				}
				// check way restrictions
				if (!attributes.get("FRC").equals("-1")
						&& !attributes.get("ONEWAY").equals("N")) {
					way = writeWay(attributes, nodes);
					if (tempWays.exists()) {
						frWays.write(way);
					}
				}

			}
			frWays.close();
			frNodes.close();
			
			// Read temporal files to make osm file
			File f = new File(args[2]);
			writeOSMFile(tempWays, tempNodes, f);
	
			// Delete temporal file
			tempWays.delete();
			tempNodes.delete();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ShapefileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void writeOSMFile(File ways, File nodes, File fosm){
		FileReader frTempWays;
		FileReader frTempNodes;
		FileWriter fwOSM;
		String osmHeader = "<osm version='0.6' generator='TeleAtlas2OSM'>\n";
		String osmTile = "</osm>";
		try {
			// Open files
			frTempWays = new FileReader(ways);
			frTempNodes = new FileReader(nodes);
			fwOSM = new FileWriter(fosm);
			
			// Write osm header
			fwOSM.write(osmHeader);
			
			// Write osm nodes
			readToWrite(frTempNodes, fwOSM);
			
			// Write osm ways
			readToWrite(frTempWays, fwOSM);
			
			// Write osm tile 
			fwOSM.write(osmTile);
			
			// Close files
			frTempWays.close();
			frTempNodes.close();
			fwOSM.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readToWrite(FileReader fr, FileWriter fw){
		BufferedReader br = null;
		String linea;
        try {
        	br = new BufferedReader(fr);
			while((linea=br.readLine())!=null)
			   fw.write(linea);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to read a file and get the middle nodes
	public static void getNodesFromJunction(String path, FileWriter fw) {
		Coordinate[] coordinates = null;
		String lon = "";
		String lat = "";
		long id = 0;
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		ShapefileReader shapefilereader = null;
		DbaseFileReader dbfilereader = null;
		GeometryFactory gf = new GeometryFactory();
		try {
			shpFile = new ShpFiles(path);
			shapefilereader = new ShapefileReader(shpFile, true,
					useMemoryMapped, gf);
			dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped,
					Charset.defaultCharset());
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
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ShapefileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}finally{
			try {
				shapefilereader.close();
				dbfilereader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Method to get Network attributes from a Network shape
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

	public static String writeWay(Map<String, String> atributos,
			List<String> nodes) {
		String way = "<way";
		way += " id=\"" + atributos.get("ID") + "\"";
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
			way += " changeset=\"1\"";
		}
		way += ">\n";
		way += "	<nd ref=\"" + atributos.get("F_JNCTID") + "\"/>\n";
		// Add the nodes from geometry
		if (!nodes.isEmpty()) {
			for (String s : nodes) {
				way += s;
			}
		}
		way += "	<nd ref=\"" + atributos.get("T_JNCTID") + "\"/>\n";
		String frc = getFRC(atributos.get("FRC"));
		way += "	<tag k=\"highway\" v=\"" + frc + "\"/>\n";
		way += "	<tag k=\"name\" v=\"" + atributos.get("NAME") + "\"/>\n";
		String oneway = getOneWay(atributos.get("ONEWAY"));
		way += "	<tag k=\"oneway\" v=\"" + oneway + "\"/>\n";
		way += "</way>\n";
		return way;
	}

	public static void writeNodes(FileWriter fw, long ref, String lat, String lon) {
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
		} catch (IOException e) {
			e.printStackTrace();
		}
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
