package com.nono.xml;

import android.sax.Element;

/**
 * Standard interface for parsing an XML document using SAX
 */
public interface SAXParseInitializer {
	
	/**
	 * Set the current data instance for the parser to populate.
	 * @param currentInstance The data instance for the parser to populate. 
	 */
	public void setCurrentInstance(SAXSerializable currentInstance);
	
	/**
	 * Notify the instance that the parser is finished populating the data object.
	 */
	public void end();

	/**
	 * Parse the XML
	 * @param node The node to be parsed.
	 */
	public void prepareNodes(final Element node);
}
