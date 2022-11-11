package com.nono.xml;

/**
 * Standard interface for parsing an XML document using SAX
 */
public interface SAXSerializable {
	
	/**
	 * Invoked by a parent parser to get a handle on this
	 * objects serializer in order to register the elements to deserialize. 
	 */
	public SAXParseInitializer getSAXInitializer();
}
