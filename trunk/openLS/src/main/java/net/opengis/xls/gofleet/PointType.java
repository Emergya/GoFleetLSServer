package net.opengis.xls.gofleet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import net.opengis.gml.DirectPositionType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnyURI;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlID;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class PointType implements net.opengis.gml.PointType {

	public PointType() {
		// TODO Auto-generated constructor stub
	}

	public String getGid() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlString xgetGid() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetGid() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setGid(String gid) {
		// TODO Auto-generated method stub

	}

	public void xsetGid(XmlString gid) {
		// TODO Auto-generated method stub

	}

	public void unsetGid() {
		// TODO Auto-generated method stub

	}

	public String getSrsName() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlAnyURI xgetSrsName() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetSrsName() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setSrsName(String srsName) {
		// TODO Auto-generated method stub

	}

	public void xsetSrsName(XmlAnyURI srsName) {
		// TODO Auto-generated method stub

	}

	public void unsetSrsName() {
		// TODO Auto-generated method stub

	}

	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	public XmlID xgetId() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetId() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setId(String id) {
		// TODO Auto-generated method stub

	}

	public void xsetId(XmlID id) {
		// TODO Auto-generated method stub

	}

	public void unsetId() {
		// TODO Auto-generated method stub

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

	public DirectPositionType getPos() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setPos(DirectPositionType pos) {
		// TODO Auto-generated method stub

	}

	public DirectPositionType addNewPos() {
		// TODO Auto-generated method stub
		return null;
	}

}
