package com.nono.wherewerewe.data.table;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.nono.data.DbColumnInteger;
import com.nono.data.DbColumnLong;
import com.nono.data.DbColumnText;
import com.nono.data.DbTable;
import com.nono.wherewerewe.data.DbConst;
import com.nono.xml.MsgSaxParser;
import com.nono.xml.SAXParseInitializer;
import com.nono.xml.SAXSerializable;
import com.nono.xml.XMLUtils;

public class DataStoreTable extends DbTable {

	private static final long serialVersionUID = 1L;

	private DbColumnLong m_id = new DbColumnLong(DbConst.KEY_ID);
	private DbColumnLong m_tripId = new DbColumnLong(DbConst.TRIP_ID);
	private DbColumnInteger m_type = new DbColumnInteger(DbConst.TYPE);
	private DbColumnLong m_waypointId = new DbColumnLong(DbConst.WAYPOINT_ID);
	private DbColumnInteger m_dataStoreType = new DbColumnInteger(DbConst.DATASTORE_TYPE);
	private DbColumnText m_path = new DbColumnText(DbConst.PATH);
	
	/**
	 * Default constructor.
	 */
	public DataStoreTable() {
		super();
	}

	/**
	 * Clear all elements in the table.
	 */
	public void clear() {
		m_id.clear();
		m_type.clear();
		m_tripId.clear();
		m_waypointId.clear();
		m_dataStoreType.clear();
		m_path.clear();
		
		markNoAction();
	}
	
	/**
	 * Indicate that all fields match the database values.
	 */
	public void resetContentsChanged() {
		m_id.resetChanged();
		m_type.resetChanged();
		m_tripId.resetChanged();
		m_waypointId.resetChanged();
		m_dataStoreType.resetChanged();
		m_path.resetChanged();
		
		markNoAction();
	}

	/**
	 * Retrieve the content values of the table for update.
	 * @return the content values of the table for update.
	 */
	public ContentValues getContentValues() {
		if (isClean()) {
			return null;
		}
	
		ContentValues contentValues = new ContentValues();
		if (m_type.isChanged()) {
			contentValues.put(DbConst.TYPE, m_type.getData());
		}
		if (m_tripId.isChanged()) {
			contentValues.put(DbConst.TRIP_ID, m_tripId.getData());
		}
		if (m_waypointId.isChanged()) {
			contentValues.put(DbConst.WAYPOINT_ID, m_waypointId.getData());
		}
		if (m_dataStoreType.isChanged()) {
			contentValues.put(DbConst.DATASTORE_TYPE, m_dataStoreType.getData());
		}
		if (m_path.isChanged()) {
			contentValues.put(DbConst.PATH, m_path.getData());
		}

		return contentValues;
	}

	/**
	 * @return the tripId
	 */
	public long getId() {
		return m_id.getData();
	}
	/**
	 * @param data the tripId to set
	 */
	public void setId(long data) {
		m_id.setData(data);
		markForUpdate();
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return m_type.getData();
	}
	/**
	 * @param data the type to set
	 */
	public void setType(int data) {
		m_type.setData(data);
		markForUpdate();
	}
	/**
	 * @return the path
	 */
	public String getPath() {
		return m_path.getData();
	}
	/**
	 * @param data the path to set
	 */
	public void setPath(String data) {
		m_path.setData(data);
		markForUpdate();
	}
	/**
	 * @return the tripId
	 */
	public long getTripId() {
		return m_tripId.getData();
	}
	/**
	 * @param data the tripId to set
	 */
	public void setTripId(long data) {
		m_tripId.setData(data);
		markForUpdate();
	}
	/**
	 * @return the waypointId
	 */
	public long getWaypointId() {
		return m_waypointId.getData();
	}
	/**
	 * @param data the waypointId to set
	 */
	public void setWaypointId(long data) {
		m_waypointId.setData(data);
		markForUpdate();
	}
	/**
	 * @return the dataStoreType
	 */
	public int getDataStoreType() {
		return m_dataStoreType.getData();
	}
	/**
	 * @param data the dataStoreType to set
	 */
	public void setDataStoreType(int data) {
		m_dataStoreType.setData(data);
		markForUpdate();
	}

	public void parseXML(String msg) {
        final RootElement root = new RootElement(XMLTagConstants.XML_DATA_STORE);
        final SAXParseInitializer saxInitializer = getSAXInitializer();
        saxInitializer.setCurrentInstance(this);
        saxInitializer.prepareNodes(root);

        final MsgSaxParser saxParser = new MsgSaxParser(msg);
        saxParser.parse(root);
	}

	public SAXParseInitializer getSAXInitializer() {
		return new XMLSaxInitializer();
	}

	public class XMLSaxInitializer implements SAXParseInitializer {
		
		DataStoreTable localInstance = null;
		
		public XMLSaxInitializer() {
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (DataStoreTable)currentInstance;
		}

		public void end() {
		}

		public void prepareNodes(final Element node) {
			node.getChild(XMLTagConstants.XML_DATA_STORE_ID).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_id.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_TYPE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_type.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_DATA_STORE_TYPE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_dataStoreType.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_TRIP).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_dataStoreType.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_LOCATION).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_dataStoreType.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_PATH).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_path.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
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
	        serializer.startTag("", XMLTagConstants.XML_DATA_STORE);
			serializeXML(serializer);
            serializer.endTag("", XMLTagConstants.XML_DATA_STORE);
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
	        serializer.startTag("", XMLTagConstants.XML_DATA_STORE_ID);
            serializer.text(m_id.toString());
            serializer.endTag("", XMLTagConstants.XML_DATA_STORE_ID);
	        serializer.startTag("", XMLTagConstants.XML_TYPE);
            serializer.text(m_type.toString());
            serializer.endTag("", XMLTagConstants.XML_TYPE);
	        serializer.startTag("", XMLTagConstants.XML_TRIP_ID);
            serializer.text(m_tripId.toString());
            serializer.endTag("", XMLTagConstants.XML_TRIP_ID);
	        serializer.startTag("", XMLTagConstants.XML_LOCATION_ID);
            serializer.text(m_waypointId.toString());
            serializer.endTag("", XMLTagConstants.XML_LOCATION_ID);
	        serializer.startTag("", XMLTagConstants.XML_DATA_STORE_TYPE);
            serializer.text(m_dataStoreType.toString());
            serializer.endTag("", XMLTagConstants.XML_DATA_STORE_TYPE);
            String path = m_path.toString();
            if (path != null) {
		        serializer.startTag("", XMLTagConstants.XML_PATH);
	            serializer.cdsect(path);
	            serializer.endTag("", XMLTagConstants.XML_PATH);
            }
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public static void serializeXMLList(XmlSerializer serializer, ArrayList<DataStoreTable> list)
				throws IllegalArgumentException, IllegalStateException, IOException {

        if (list != null && list.size() > 0) {
            serializer.startTag("", XMLTagConstants.XML_DATA_STORE_LIST);
            for (DataStoreTable table : list) {
                table.serializeXML(serializer);
    		}
            serializer.endTag("", XMLTagConstants.XML_DATA_STORE_LIST);
        }
	}
}
