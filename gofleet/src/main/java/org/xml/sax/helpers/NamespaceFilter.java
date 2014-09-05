package org.xml.sax.helpers;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Helper class found on this url:
 * http://stackoverflow.com/questions/277502/jaxb
 * -how-to-ignore-namespace-during-unmarshalling-xml-document/2148541#2148541
 * 
 * @author marias
 * 
 */
public class NamespaceFilter extends XMLFilterImpl {

	private String usedNamespaceUri;
	private boolean addNamespace;

	// State variable
	private boolean addedNamespace = false;

	public NamespaceFilter(String namespaceUri, boolean addNamespace) {
		super();

		if (addNamespace)
			this.usedNamespaceUri = namespaceUri;
		else
			this.usedNamespaceUri = "";
		this.addNamespace = addNamespace;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
		if (addNamespace) {
			startControlledPrefixMapping();
		}
	}

	@Override
	public void startElement(String arg0, String arg1, String arg2,
			Attributes arg3) throws SAXException {
		super.startElement(arg0, arg1, arg1, arg3);
	}

	@Override
	public void endElement(String arg0, String arg1, String arg2)
			throws SAXException {
		super.endElement(this.usedNamespaceUri, arg1, arg2);
	}

	@Override
	public void startPrefixMapping(String prefix, String url)
			throws SAXException {

		if (addNamespace) {
			this.startControlledPrefixMapping();
		} else {
			// Remove the namespace, i.e. don´t call startPrefixMapping for
			// parent!
		}

	}

	private void startControlledPrefixMapping() throws SAXException {

		if (this.addNamespace && !this.addedNamespace) {
			// We should add namespace since it is set and has not yet been
			// done.
			super.startPrefixMapping("", this.usedNamespaceUri);

			// Make sure we dont do it twice
			this.addedNamespace = true;
		}
	}

}