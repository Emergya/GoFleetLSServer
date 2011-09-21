package net.opengis.xls.gofleet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import net.opengis.gml.CircleByCenterPointType;
import net.opengis.gml.MultiPolygonType;
import net.opengis.gml.PointType;
import net.opengis.gml.PolygonType;
import net.opengis.xls.AngleType;
import net.opengis.xls.CircularArcType;
import net.opengis.xls.EllipseType;
import net.opengis.xls.QualityOfPositionType;
import net.opengis.xls.SpeedType;
import net.opengis.xls.TimeType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class PositionType implements net.opengis.xls.PositionType {

	public PositionType() {
		// TODO Auto-generated constructor stub
	}

	public XmlObject changeType(SchemaType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int compareValue(XmlObject arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public XmlObject copy() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] execQuery(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] execQuery(String arg0, XmlOptions arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isImmutable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNil() {
		// TODO Auto-generated method stub
		return false;
	}

	public SchemaType schemaType() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject selectAttribute(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject selectAttribute(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectAttributes(QNameSet arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectChildren(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectChildren(QNameSet arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectChildren(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject[] selectPath(String arg0, XmlOptions arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlObject set(XmlObject arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNil() {
		// TODO Auto-generated method stub

	}

	public XmlObject substitute(QName arg0, SchemaType arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean validate() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean validate(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean valueEquals(XmlObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int valueHashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	public XmlDocumentProperties documentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	public void dump() {
		// TODO Auto-generated method stub

	}

	public Node getDomNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object monitor() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlCursor newCursor() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node newDomNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node newDomNode(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream newInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public InputStream newInputStream(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader newReader() {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader newReader(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XMLInputStream newXMLInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	public XMLInputStream newXMLInputStream(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public XMLStreamReader newXMLStreamReader() {
		// TODO Auto-generated method stub
		return null;
	}

	public XMLStreamReader newXMLStreamReader(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void save(File arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(OutputStream arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(Writer arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(ContentHandler arg0, LexicalHandler arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void save(File arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(OutputStream arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(Writer arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	public void save(ContentHandler arg0, LexicalHandler arg1, XmlOptions arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public String xmlText() {
		// TODO Auto-generated method stub
		return null;
	}

	public String xmlText(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public PointType getPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPoint(PointType point) {
		// TODO Auto-generated method stub

	}

	public PointType addNewPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	public EllipseType getEllipse() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetEllipse() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setEllipse(EllipseType ellipse) {
		// TODO Auto-generated method stub

	}

	public EllipseType addNewEllipse() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetEllipse() {
		// TODO Auto-generated method stub

	}

	public CircleByCenterPointType getCircleByCenterPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetCircleByCenterPoint() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCircleByCenterPoint(
			CircleByCenterPointType circleByCenterPoint) {
		// TODO Auto-generated method stub

	}

	public CircleByCenterPointType addNewCircleByCenterPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetCircleByCenterPoint() {
		// TODO Auto-generated method stub

	}

	public CircularArcType getCircularArc() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetCircularArc() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setCircularArc(CircularArcType circularArc) {
		// TODO Auto-generated method stub

	}

	public CircularArcType addNewCircularArc() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetCircularArc() {
		// TODO Auto-generated method stub

	}

	public PolygonType getPolygon() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetPolygon() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setPolygon(PolygonType polygon) {
		// TODO Auto-generated method stub

	}

	public PolygonType addNewPolygon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetPolygon() {
		// TODO Auto-generated method stub

	}

	public MultiPolygonType getMultiPolygon() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetMultiPolygon() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMultiPolygon(MultiPolygonType multiPolygon) {
		// TODO Auto-generated method stub

	}

	public MultiPolygonType addNewMultiPolygon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetMultiPolygon() {
		// TODO Auto-generated method stub

	}

	public QualityOfPositionType getQoP() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetQoP() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setQoP(QualityOfPositionType qoP) {
		// TODO Auto-generated method stub

	}

	public QualityOfPositionType addNewQoP() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetQoP() {
		// TODO Auto-generated method stub

	}

	public TimeType getTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetTime() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setTime(TimeType time) {
		// TODO Auto-generated method stub

	}

	public TimeType addNewTime() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetTime() {
		// TODO Auto-generated method stub

	}

	public SpeedType getSpeed() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetSpeed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setSpeed(SpeedType speed) {
		// TODO Auto-generated method stub

	}

	public SpeedType addNewSpeed() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetSpeed() {
		// TODO Auto-generated method stub

	}

	public AngleType getDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetDirection() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setDirection(AngleType direction) {
		// TODO Auto-generated method stub

	}

	public AngleType addNewDirection() {
		// TODO Auto-generated method stub
		return null;
	}

	public void unsetDirection() {
		// TODO Auto-generated method stub

	}

	public String getLevelOfConf() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlString xgetLevelOfConf() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetLevelOfConf() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setLevelOfConf(String levelOfConf) {
		// TODO Auto-generated method stub

	}

	public void xsetLevelOfConf(XmlString levelOfConf) {
		// TODO Auto-generated method stub

	}

	public void unsetLevelOfConf() {
		// TODO Auto-generated method stub

	}

}
