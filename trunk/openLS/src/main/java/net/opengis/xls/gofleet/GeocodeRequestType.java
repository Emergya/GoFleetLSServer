package net.opengis.xls.gofleet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import net.opengis.xls.AddressType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public class GeocodeRequestType implements net.opengis.xls.GeocodeRequestType {

	public GeocodeRequestType() {
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

	public AddressType[] getAddressArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public AddressType getAddressArray(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public int sizeOfAddressArray() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAddressArray(AddressType[] addressArray) {
		// TODO Auto-generated method stub

	}

	public void setAddressArray(int i, AddressType address) {
		// TODO Auto-generated method stub

	}

	public AddressType insertNewAddress(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	public AddressType addNewAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAddress(int i) {
		// TODO Auto-generated method stub

	}

	public boolean getReturnFreeForm() {
		// TODO Auto-generated method stub
		return false;
	}

	public XmlBoolean xgetReturnFreeForm() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSetReturnFreeForm() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setReturnFreeForm(boolean returnFreeForm) {
		// TODO Auto-generated method stub

	}

	public void xsetReturnFreeForm(XmlBoolean returnFreeForm) {
		// TODO Auto-generated method stub

	}

	public void unsetReturnFreeForm() {
		// TODO Auto-generated method stub

	}

}
