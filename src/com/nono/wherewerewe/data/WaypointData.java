package com.nono.wherewerewe.data;

import java.io.Serializable;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import android.sax.Element;
import android.sax.StartElementListener;

import com.nono.wherewerewe.data.table.DataStoreTable;
import com.nono.wherewerewe.data.table.WaypointTable;
import com.nono.wherewerewe.data.table.XMLTagConstants;
import com.nono.xml.SAXParseInitializer;
import com.nono.xml.SAXSerializable;

public class WaypointData implements Cloneable, Serializable, SAXSerializable {

	private static final long serialVersionUID = 1L;
	private WaypointTable waypointTable = null;
	private ArrayList<DataStoreTable> dataStoreList = new ArrayList<DataStoreTable>();
	
	public WaypointData() {
	}

	public WaypointData(WaypointTable waypointTable) {
		this.waypointTable = waypointTable;
	}

	/**
	 * @return the waypointTable
	 */
	public WaypointTable getWaypointTable() {
		return waypointTable;
	}

	/**
	 * Add the dataStore.
	 * @param dataStore The dataStore to add.
	 */
	public void addDataStore(DataStoreTable dataStore) {
		if (dataStore == null) {
			return;
		}

		dataStoreList.add(dataStore);
	}

	/**
	 * Remove the dataStore.
	 * @param dataStore The dataStore to remove.
	 */
	public void removeDataStore(DataStoreTable dataStore) {
		if (dataStore == null) {
			return;
		}
		dataStoreList.remove(dataStore);

	}

	/**
	 * Get the list of dataStores.
	 * @return The list of dataStores
	 */
	public DataStoreTable[] getDataStoreList() {
		DataStoreTable[] list = new DataStoreTable[dataStoreList.size()];
		list = dataStoreList.toArray(list);
		return list;
	}

	@Override
	public SAXParseInitializer getSAXInitializer() {
		return new XMLSaxInitializer();
	}
	
	public class XMLSaxInitializer implements SAXParseInitializer {
		
		WaypointData localInstance = WaypointData.this;
	    private SAXParseInitializer m_waypointSaxInitializer;
	    private SAXParseInitializer m_dataStoreSaxInitializer;
		
		public XMLSaxInitializer() {
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (WaypointData)currentInstance;
			if (localInstance.waypointTable == null) {
				localInstance.waypointTable = new WaypointTable();
			}
			m_waypointSaxInitializer.setCurrentInstance(localInstance.waypointTable);
		}

		public void end() {
			if (localInstance != null) {
				if (localInstance.waypointTable != null) {
					localInstance.waypointTable.resetContentsChanged();
				}
			}
		}

		public void prepareNodes(final Element node) {
		
			localInstance.waypointTable = new WaypointTable();
			
			Element waypointElement = node.getChild(XMLTagConstants.XML_LOCATION);
			m_waypointSaxInitializer = localInstance.waypointTable.getSAXInitializer();
			m_waypointSaxInitializer.prepareNodes(waypointElement);

			Element dataStoreListElement = waypointElement.getChild(XMLTagConstants.XML_DATA_STORE_LIST);
			Element dataStoreElement = dataStoreListElement.getChild(XMLTagConstants.XML_DATA_STORE);
			m_dataStoreSaxInitializer = new DataStoreTable().getSAXInitializer();
			m_dataStoreSaxInitializer.prepareNodes(dataStoreListElement);

			dataStoreElement.setStartElementListener(new StartElementListener() {
	            public void start(Attributes attributes) {
	            	DataStoreTable dataStoreTable = new DataStoreTable();
	            	localInstance.addDataStore(dataStoreTable);
	            	m_dataStoreSaxInitializer.setCurrentInstance(dataStoreTable);
	            }
			});
		}
	}	


	public void serializeXML(XmlSerializer serializer) {
		try {
			waypointTable.serializeXML(serializer);

			if (dataStoreList.size() > 0) {
			    serializer.startTag("", XMLTagConstants.XML_DATA_STORE_LIST);
			    for (DataStoreTable dataStoreTable : dataStoreList) {
			        serializer.startTag("", XMLTagConstants.XML_DATA_STORE);
			        dataStoreTable.serializeXML(serializer);
			        serializer.endTag("", XMLTagConstants.XML_DATA_STORE);
				}
			    serializer.endTag("", XMLTagConstants.XML_DATA_STORE_LIST);
			}
		} catch (Exception e) {
	        throw new RuntimeException(e);
		}
	}
}
