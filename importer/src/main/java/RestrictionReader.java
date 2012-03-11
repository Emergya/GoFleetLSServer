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
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.GeometryFactory;


public class RestrictionReader {

	public static void  processRestrictions(String pathManeuvers, String pathManeuversPath, String pathRestrictions, File file) {
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		GeometryFactory gf = new GeometryFactory();
		Row rdb = null;
		Map<String, String> attributes = null;
		FileWriter fw = null;
		try {
			// Open file
			fw = new FileWriter(file);
			shpFile = new ShpFiles(pathManeuvers);
			DbaseFileReader dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped, Charset.defaultCharset());
			//ShapefileReader shapefilereader = new ShapefileReader(shpFile, true,useMemoryMapped, gf);
			int num = 0;
			while (dbfilereader.hasNext()){
				rdb = dbfilereader.readRow();
				attributes = getAttributesManeuvers(rdb);
				// Attributes restrictions
				if(attributes.get("FEATTYP").equals("2103") && attributes.get("PROMANTYP").equals("0")){
					long id = Long.valueOf(attributes.get("ID"));
					long junctID = Long.valueOf(attributes.get("JNCTID"));
					// Get from way and to way from maneuvers path index table
					List<String> sequence = getSequence(id, pathManeuversPath);
					String restrictions = getRestriction(id, pathRestrictions);
					if(sequence.size() == 2){
						System.out.println("Restriction nº: " + num);
						num++;
						long from = Long.valueOf(sequence.get(0));
						long to = Long.valueOf(sequence.get(1));
						String rest = writeRestriction(id, junctID, from, to, restrictions);
						fw.write(rest);
						fw.flush();
					} //TODO length > 2
				}
			}
			// Close files
			fw.close();
			dbfilereader.close();
			//shapefilereader.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// Method to get Maneuvers attributes from a Maneuvers shape
	private static Map<String, String> getAttributesManeuvers(Row rdb){
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
	
	private static Map<String, String> getAttributesManeuversPath(Row rdb){
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
	
	private static Map<String, String> getAttributesRestrictions(Row rdb){
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
	
	// Method to write a restriction
	private static String writeRestriction(long idRestriction, long junctID, long from, long to, String tags){
		String relationHeader = "<relation changeset=\"1\" " +
				"uid=\"1\" " +
				"timestamp=\"2012-02-19T19:07:25Z\" " +
				"version=\"1\" " +
				"user=\"TeleAtlas2OSM\" " +
				"id=\"" + idRestriction + "\">\n";
		
		String memberWayFrom = "<member type=\"way\" ref=\"" + from + "\" role=\"from\"/>\n";
		
		String memberWayTo = "<member type=\"way\" ref=\"" + to + "\" role=\"to\"/>\n";
		
		String memberNode = "<member type=\"node\" " +
				"ref=\"" + junctID + "\" " +
				"role=\"via\"/>\n";
		
		String relationTail = "</relation>\n";
		String tagType = "<tag k=\"type\" v=\"restriction\"/>\n";
		return relationHeader + memberWayFrom + memberWayTo + memberNode + tagType + tags + relationTail;
	}
	
	private static String writeTag(String rt, String rv, String vt){
		String value[] = new String[2];
		String tagRestriction = "";
		String tagExcept = "";
		if(rt.equals("6Z") || rt.equals("TR") || rt.equals("4B")){
			
		}else{
			// Check all posibles cases
			if(rt.equals("")){
				value = checkBlank(rv, vt);
			}else if(rt.equals("BP")){
				value = checkBP(rv, vt);
			}else if(rt.equals("RB")){
				value = checkRB(rv, vt);
			}else if(rt.equals("SR")){
				value = checkSR(rv, vt);
			}else if(rt.equals("8I")){
				value = check8I(rv, vt);
			}else if(rt.equals("DF") || rt.equals("6Q")){
				value = checkDF6Q(rv, vt);
			}
			if (value[0] != "") {
				tagRestriction = "<tag k=\"restriction\" v =\"" + value[0] + "\"/>\n";
			}
			if (value[1] != "") {
				tagExcept = "<tag k=\"except\" v=\"" + value[1] + "\"/>\n";
			}
			
		}
		return tagRestriction + tagExcept;
	}
	
	private static String checkVT(String valueIN, String vt){
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
	
	private static String[] checkBlank(String rv, String vt) {
		String value[] = new String[2];
		value[0] = "no_straight_on";
		value[1] = "";
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}

	private static String[] checkDF6Q(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if(rv.equals("2")){
			value[0] = "no_entry";
			value[1] = "";
		}else if(rv.equals("3")){
			value[0] = "no_exit";
			value[1] = "";
		}else if(rv.equals("4")){
			// TODO Two restrictions
			value[0] = "";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}
	private static String[] check8I(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if(rv.equals("0")){
			value[0] = "no_straight_on ";
			value[1] = "";
		}else if(rv.equals("1")){
			value[0] = "only_straight_on";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}
	private static String[] checkSR(String rv, String vt) {
		String value[] = new String[2];
		value[0] = "no_entry";
		value[1] = "psv";
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}
	private static String[] checkRB(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if(rv.equals("1") || rv.equals("3")){
			value[0] = "no_entry";
			value[1] = "psv";
		}else if(rv.equals("2")){
			value[0] = "no_entry";
			value[1] = "";
		}
		// Check VT value
		value[1] = checkVT(value[1], vt);
		return value;
	}
	private static String[] checkBP(String rv, String vt) {
		String value[] = new String[2];
		// Check RESTRVAL value
		if(rv.equals("11") || rv.equals("13") || rv.equals("23")){
			value[0] = "no_entry";
			value[1] = "psv";
		}else if(rv.equals("1") || rv.equals("2") || rv.equals("12") || rv.equals("22")){
			value[0] = "no_entry";
			value[1] = "";
		}
		value[1] = checkVT(value[1], vt);
		
		return value;
	}
	private static List<String> getSequence(long id, String path){
		List<String> trpelid = new LinkedList<String>();
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		GeometryFactory gf = new GeometryFactory();
		Row rdb = null;
		Map<String, String> attributes = null;
		try {
			shpFile = new ShpFiles(path);
			DbaseFileReader dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped, Charset.defaultCharset());
			//ShapefileReader shapefilereader = new ShapefileReader(shpFile, true,useMemoryMapped, gf);
			while (dbfilereader.hasNext()){
				rdb = dbfilereader.readRow();
				attributes = getAttributesManeuversPath(rdb);
				long idMP = Long.valueOf(attributes.get("ID"));
				if(id == idMP){
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
	
	private static String getRestriction(long id, String path){
		String rest = "";
		ShpFiles shpFile = null;
		boolean useMemoryMapped = false;
		GeometryFactory gf = new GeometryFactory();
		Row rdb = null;
		Map<String, String> attributes = null;
		try {
			shpFile = new ShpFiles(path);
			DbaseFileReader dbfilereader = new DbaseFileReader(shpFile, useMemoryMapped, Charset.defaultCharset());
			//ShapefileReader shapefilereader = new ShapefileReader(shpFile, true,useMemoryMapped, gf);
			while (dbfilereader.hasNext()){
				rdb = dbfilereader.readRow();
				attributes = getAttributesRestrictions(rdb);
				long idMP = Long.valueOf(attributes.get("ID"));
				String feattyp = attributes.get("FEATTYP");
				if(id == idMP){
					// Restriction to process
					if(feattyp.equals("2101") || feattyp.equals("2102") || feattyp.equals("2103")){
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
