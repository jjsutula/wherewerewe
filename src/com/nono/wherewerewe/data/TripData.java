package com.nono.wherewerewe.data;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xmlpull.v1.XmlSerializer;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import com.nono.wherewerewe.data.table.DataStoreTable;
import com.nono.wherewerewe.data.table.TripTable;
import com.nono.wherewerewe.data.table.XMLTagConstants;
import com.nono.xml.MsgSaxParser;
import com.nono.xml.SAXParseInitializer;
import com.nono.xml.SAXSerializable;

public class TripData  implements Cloneable, Serializable, SAXSerializable {

	private static final long serialVersionUID = 1L;

	private TripTable tripTable;
	private ArrayList<DataStoreTable> dataStoreList = new ArrayList<DataStoreTable>();
	private ArrayList<WaypointData> waypointList = new ArrayList<WaypointData>();

	/**
	 * @param tripTable the tripTable to set
	 */
	public TripData() {
	}

	/**
	 * @param tripTable the tripTable to set
	 */
	public TripData(TripTable tripTable) {
		this.tripTable = tripTable;
	}

	/**
	 * @return the tripTable
	 */
	public TripTable getTripTable() {
		return tripTable;
	}

	/**
	 * @param tripTable the tripTable to set
	 */
	public void setTripTable(TripTable tripTable) {
		this.tripTable = tripTable;
	}

	/**
	 * Add the waypoint.
	 * @param waypoint The waypoint to add.
	 */
	public void addWaypoint(WaypointData waypoint) {
		if (waypoint == null) {
			return;
		}

		waypointList.add(waypoint);
	}

	/**
	 * Remove the waypoint.
	 * @param waypoint The waypoint to remove.
	 */
	public void removeWaypoint(WaypointData waypoint) {
		if (waypoint == null) {
			return;
		}

		waypointList.remove(waypoint);
	}

	/**
	 * Get the list of locations.
	 * @return The list of locations
	 */
	public WaypointData[] getWaypointList() {
		WaypointData[] list = new WaypointData[waypointList.size()];
		list = waypointList.toArray(list);
		return list;
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

	public TripData[] parseTripListXML(String msg) {
		ArrayList<TripData> tripList = new ArrayList<TripData>();

        final RootElement root = new RootElement(XMLTagConstants.XML_TRIP_LIST);
        final XMLSaxInitializer saxInitializer = new XMLSaxInitializer(tripList);
        saxInitializer.prepareNodes(root);

        MsgSaxParser saxParser = new MsgSaxParser(msg);
        saxParser.parse(root);
        
        TripData[] trips = new TripData[tripList.size()];
        trips = tripList.toArray(trips);
        
        return trips;
	}

	public SAXParseInitializer getSAXInitializer() {
		return new XMLSaxInitializer(null);
	}

	public class XMLSaxInitializer implements SAXParseInitializer {
		
		ArrayList<TripData> tripList = null;
		TripData localInstance = TripData.this;

	    private SAXParseInitializer m_tripSaxInitializer;
	    private SAXParseInitializer m_waypointSaxInitializer;
	    private SAXParseInitializer m_dataStoreSaxInitializer;
		
		public XMLSaxInitializer(ArrayList<TripData> tripList) {
			this.tripList = tripList;
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (TripData)currentInstance;
			if (localInstance.tripTable == null) {
				localInstance.tripTable = new TripTable();
			}
			m_tripSaxInitializer.setCurrentInstance(localInstance.tripTable);
		}

		public void end() {
			if (localInstance != null) {
				if (localInstance.tripTable != null) {
					localInstance.tripTable.resetContentsChanged();
				}
			}
		}

		public void prepareNodes(final Element node) {
			localInstance.tripTable = new TripTable();
			WaypointData waypointData = new WaypointData();
			
			Element tripElement = node.getChild(XMLTagConstants.XML_TRIP);
			m_tripSaxInitializer = localInstance.tripTable.getSAXInitializer();
			m_tripSaxInitializer.prepareNodes(tripElement);

			Element waypointListElement = tripElement.getChild(XMLTagConstants.XML_LOCATION_LIST);
			Element waypointElement = waypointListElement.getChild(XMLTagConstants.XML_LOCATION);
			m_waypointSaxInitializer = waypointData.getSAXInitializer();
			m_waypointSaxInitializer.prepareNodes(waypointListElement);

			Element dataStoreListElement = tripElement.getChild(XMLTagConstants.XML_DATA_STORE_LIST);
			Element dataStoreElement = dataStoreListElement.getChild(XMLTagConstants.XML_DATA_STORE);
			m_dataStoreSaxInitializer = new DataStoreTable().getSAXInitializer();
			m_dataStoreSaxInitializer.prepareNodes(dataStoreListElement);

			tripElement.setStartElementListener(new StartElementListener() {
	            public void start(Attributes attributes) {
	            	TripData tripData = new TripData();
	            	tripList.add(tripData);
	            	setCurrentInstance(tripData);
	            }
			});

			waypointElement.setStartElementListener(new StartElementListener() {
	            public void start(Attributes attributes) {
	            	WaypointData waypointData = new WaypointData();
	            	localInstance.addWaypoint(waypointData);
	            	m_waypointSaxInitializer.setCurrentInstance(waypointData);
	            }
			});

			dataStoreElement.setStartElementListener(new StartElementListener() {
	            public void start(Attributes attributes) {
	            	DataStoreTable dataStoreTable = new DataStoreTable();
	            	localInstance.addDataStore(dataStoreTable);
	            	m_dataStoreSaxInitializer.setCurrentInstance(dataStoreTable);
	            }
			});
		}
	}	

	public String createXML() {
	    XmlSerializer serializer = Xml.newSerializer();
	    StringWriter writer = new StringWriter();
        try {
			serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        serializer.startTag("", XMLTagConstants.XML_TRIP);
			serializeXML(serializer);
            serializer.endTag("", XMLTagConstants.XML_TRIP);
	        serializer.endDocument();
	        return writer.toString();
		} catch (RuntimeException re) {
	        throw re;
		} catch (Exception e) {
	        throw new RuntimeException(e);
		}
		
	}

	public void serializeXML(XmlSerializer serializer) {
	    try {
	    	tripTable.serializeXML(serializer);

	    	if (dataStoreList.size() > 0) {
		        serializer.startTag("", XMLTagConstants.XML_DATA_STORE_LIST);
		        for (DataStoreTable dataStoreTable : dataStoreList) {
			        serializer.startTag("", XMLTagConstants.XML_DATA_STORE);
			        dataStoreTable.serializeXML(serializer);
		            serializer.endTag("", XMLTagConstants.XML_DATA_STORE);
				}
	            serializer.endTag("", XMLTagConstants.XML_DATA_STORE_LIST);
	    	}

            serializer.startTag("", XMLTagConstants.XML_LOCATION_LIST);
	        for (WaypointData waypointData : waypointList) {
		        serializer.startTag("", XMLTagConstants.XML_LOCATION);
		        waypointData.serializeXML(serializer);
	            serializer.endTag("", XMLTagConstants.XML_LOCATION);
			}
            serializer.endTag("", XMLTagConstants.XML_LOCATION_LIST);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

	/**
	 * Convert a list of trip tables to trip data.
	 * @param trips The incoming tables to convert.
	 * @return A list of trip data.
	 */
	public static TripData[] convert(TripTable[] trips) {
		if (trips == null) {
			return new TripData[0];
		}

		int ndx = 0;
		TripData[] list = new TripData[trips.length];
		for (TripTable tripTable : trips) {
			list[ndx++] = new TripData(tripTable);
		}

		return list;
	}

	
	/**
	 * Serialize a list of trips to an xml string.
	 * @param trips The list to serialize.
	 * @return An xml string.
	 */
	public static String serializeTripsToXml(TripData[] trips) {
	    StringWriter writer = new StringWriter();
	    serializeTripsToXmlWriter(writer, trips, true);
	    String xml = writer.toString();
	    try {
			writer.close();
		} catch (IOException e) {}

        return xml;
	}

	/**
	 * Serialize a list of trips to an xml string.
	 * @param trips The list to serialize.
	 * @return An xml string.
	 */
	public static void serializeTripsToXmlWriter(Writer writer, TripData[] trips, boolean indent) {
	    XmlSerializer serializer = Xml.newSerializer();
        try {
			serializer.setOutput(writer);
	        serializer.startDocument("UTF-8", true);
	        if (indent) {
	        	serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
	        }
	        serializer.startTag("", XMLTagConstants.XML_TRIP_LIST);
	        for (TripData tripData : trips) {
		        serializer.startTag("", XMLTagConstants.XML_TRIP);
		        tripData.serializeXML(serializer);
	            serializer.endTag("", XMLTagConstants.XML_TRIP);				
			}
	        serializer.endDocument();
		} catch (RuntimeException re) {
	        throw re;
		} catch (Exception e) {
	        throw new RuntimeException(e);
		}
	}
	
	/**
	 * Deserialize a list of trips from an xml string.
	 * @param xml The xml string to deserialize.
	 * @return A list of Tripdata objects.
	 */
	public static TripData[] deserializeXmlTrips(String xml) {
		
		TripData tripData = new TripData();
		TripData[] trips = tripData.parseTripListXML(xml);
		
		return trips;
	}
}
