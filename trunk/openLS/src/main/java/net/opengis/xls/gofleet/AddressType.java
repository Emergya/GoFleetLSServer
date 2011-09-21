/**
 * 
 */
package net.opengis.xls.gofleet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import net.opengis.xls.CountryCodeType;
import net.opengis.xls.NamedPlaceType;
import net.opengis.xls.PostalCodeType;
import net.opengis.xls.StreetAddressType;

import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlDocumentProperties;
import org.apache.xmlbeans.XmlLanguage;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author usuario
 *
 */
public class AddressType implements net.opengis.xls.AddressType {
	
	public AddressType(){
		
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#getAddressee()
	 */
	public String getAddressee() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xgetAddressee()
	 */
	public XmlString xgetAddressee() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#isSetAddressee()
	 */
	public boolean isSetAddressee() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#setAddressee(java.lang.String)
	 */
	public void setAddressee(String addressee) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xsetAddressee(org.apache.xmlbeans.XmlString)
	 */
	public void xsetAddressee(XmlString addressee) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#unsetAddressee()
	 */
	public void unsetAddressee() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#getCountryCode()
	 */
	public String getCountryCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xgetCountryCode()
	 */
	public CountryCodeType xgetCountryCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#setCountryCode(java.lang.String)
	 */
	public void setCountryCode(String countryCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xsetCountryCode(net.opengis.xls.CountryCodeType)
	 */
	public void xsetCountryCode(CountryCodeType countryCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#getLanguage()
	 */
	public String getLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xgetLanguage()
	 */
	public XmlLanguage xgetLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#isSetLanguage()
	 */
	public boolean isSetLanguage() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#setLanguage(java.lang.String)
	 */
	public void setLanguage(String language) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#xsetLanguage(org.apache.xmlbeans.XmlLanguage)
	 */
	public void xsetLanguage(XmlLanguage language) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AbstractAddressType#unsetLanguage()
	 */
	public void unsetLanguage() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#changeType(org.apache.xmlbeans.SchemaType)
	 */
	public XmlObject changeType(SchemaType arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#compareTo(java.lang.Object)
	 */
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#compareValue(org.apache.xmlbeans.XmlObject)
	 */
	public int compareValue(XmlObject arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#copy()
	 */
	public XmlObject copy() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#execQuery(java.lang.String)
	 */
	public XmlObject[] execQuery(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#execQuery(java.lang.String, org.apache.xmlbeans.XmlOptions)
	 */
	public XmlObject[] execQuery(String arg0, XmlOptions arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#isImmutable()
	 */
	public boolean isImmutable() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#isNil()
	 */
	public boolean isNil() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#schemaType()
	 */
	public SchemaType schemaType() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectAttribute(javax.xml.namespace.QName)
	 */
	public XmlObject selectAttribute(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectAttribute(java.lang.String, java.lang.String)
	 */
	public XmlObject selectAttribute(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectAttributes(org.apache.xmlbeans.QNameSet)
	 */
	public XmlObject[] selectAttributes(QNameSet arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectChildren(javax.xml.namespace.QName)
	 */
	public XmlObject[] selectChildren(QName arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectChildren(org.apache.xmlbeans.QNameSet)
	 */
	public XmlObject[] selectChildren(QNameSet arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectChildren(java.lang.String, java.lang.String)
	 */
	public XmlObject[] selectChildren(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectPath(java.lang.String)
	 */
	public XmlObject[] selectPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#selectPath(java.lang.String, org.apache.xmlbeans.XmlOptions)
	 */
	public XmlObject[] selectPath(String arg0, XmlOptions arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#set(org.apache.xmlbeans.XmlObject)
	 */
	public XmlObject set(XmlObject arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#setNil()
	 */
	public void setNil() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#substitute(javax.xml.namespace.QName, org.apache.xmlbeans.SchemaType)
	 */
	public XmlObject substitute(QName arg0, SchemaType arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#validate()
	 */
	public boolean validate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#validate(org.apache.xmlbeans.XmlOptions)
	 */
	public boolean validate(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#valueEquals(org.apache.xmlbeans.XmlObject)
	 */
	public boolean valueEquals(XmlObject arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlObject#valueHashCode()
	 */
	public int valueHashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#documentProperties()
	 */
	public XmlDocumentProperties documentProperties() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#dump()
	 */
	public void dump() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#getDomNode()
	 */
	public Node getDomNode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#monitor()
	 */
	public Object monitor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newCursor()
	 */
	public XmlCursor newCursor() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newDomNode()
	 */
	public Node newDomNode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newDomNode(org.apache.xmlbeans.XmlOptions)
	 */
	public Node newDomNode(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newInputStream()
	 */
	public InputStream newInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newInputStream(org.apache.xmlbeans.XmlOptions)
	 */
	public InputStream newInputStream(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newReader()
	 */
	public Reader newReader() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newReader(org.apache.xmlbeans.XmlOptions)
	 */
	public Reader newReader(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newXMLInputStream()
	 */
	public XMLInputStream newXMLInputStream() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newXMLInputStream(org.apache.xmlbeans.XmlOptions)
	 */
	public XMLInputStream newXMLInputStream(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newXMLStreamReader()
	 */
	public XMLStreamReader newXMLStreamReader() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#newXMLStreamReader(org.apache.xmlbeans.XmlOptions)
	 */
	public XMLStreamReader newXMLStreamReader(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.File)
	 */
	public void save(File arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.OutputStream)
	 */
	public void save(OutputStream arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.Writer)
	 */
	public void save(Writer arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler)
	 */
	public void save(ContentHandler arg0, LexicalHandler arg1)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.File, org.apache.xmlbeans.XmlOptions)
	 */
	public void save(File arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.OutputStream, org.apache.xmlbeans.XmlOptions)
	 */
	public void save(OutputStream arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(java.io.Writer, org.apache.xmlbeans.XmlOptions)
	 */
	public void save(Writer arg0, XmlOptions arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#save(org.xml.sax.ContentHandler, org.xml.sax.ext.LexicalHandler, org.apache.xmlbeans.XmlOptions)
	 */
	public void save(ContentHandler arg0, LexicalHandler arg1, XmlOptions arg2)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#xmlText()
	 */
	public String xmlText() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.xmlbeans.XmlTokenSource#xmlText(org.apache.xmlbeans.XmlOptions)
	 */
	public String xmlText(XmlOptions arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#getFreeFormAddress()
	 */
	public String getFreeFormAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#xgetFreeFormAddress()
	 */
	public XmlString xgetFreeFormAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#isSetFreeFormAddress()
	 */
	public boolean isSetFreeFormAddress() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#setFreeFormAddress(java.lang.String)
	 */
	public void setFreeFormAddress(String freeFormAddress) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#xsetFreeFormAddress(org.apache.xmlbeans.XmlString)
	 */
	public void xsetFreeFormAddress(XmlString freeFormAddress) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#unsetFreeFormAddress()
	 */
	public void unsetFreeFormAddress() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#getStreetAddress()
	 */
	public StreetAddressType getStreetAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#isSetStreetAddress()
	 */
	public boolean isSetStreetAddress() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#setStreetAddress(net.opengis.xls.StreetAddressType)
	 */
	public void setStreetAddress(StreetAddressType streetAddress) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#addNewStreetAddress()
	 */
	public StreetAddressType addNewStreetAddress() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#unsetStreetAddress()
	 */
	public void unsetStreetAddress() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#getPlaceArray()
	 */
	public NamedPlaceType[] getPlaceArray() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#getPlaceArray(int)
	 */
	public NamedPlaceType getPlaceArray(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#sizeOfPlaceArray()
	 */
	public int sizeOfPlaceArray() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#setPlaceArray(net.opengis.xls.NamedPlaceType[])
	 */
	public void setPlaceArray(NamedPlaceType[] placeArray) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#setPlaceArray(int, net.opengis.xls.NamedPlaceType)
	 */
	public void setPlaceArray(int i, NamedPlaceType place) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#insertNewPlace(int)
	 */
	public NamedPlaceType insertNewPlace(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#addNewPlace()
	 */
	public NamedPlaceType addNewPlace() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#removePlace(int)
	 */
	public void removePlace(int i) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#getPostalCode()
	 */
	public String getPostalCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#xgetPostalCode()
	 */
	public PostalCodeType xgetPostalCode() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#isSetPostalCode()
	 */
	public boolean isSetPostalCode() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#setPostalCode(java.lang.String)
	 */
	public void setPostalCode(String postalCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#xsetPostalCode(net.opengis.xls.PostalCodeType)
	 */
	public void xsetPostalCode(PostalCodeType postalCode) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see net.opengis.xls.AddressType#unsetPostalCode()
	 */
	public void unsetPostalCode() {
		// TODO Auto-generated method stub

	}

}
