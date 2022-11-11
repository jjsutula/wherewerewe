package com.nono.wherewerewe.data.table;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.nono.data.DbColumnCoordinate;
import com.nono.data.DbColumnDouble;
import com.nono.data.DbColumnInteger;
import com.nono.data.DbColumnLong;
import com.nono.data.DbColumnText;
import com.nono.data.DbTable;
import com.nono.wherewerewe.data.DbConst;
import com.nono.xml.MsgSaxParser;
import com.nono.xml.SAXParseInitializer;
import com.nono.xml.SAXSerializable;
import com.nono.xml.XMLUtils;

public class WaypointTable extends DbTable {

	private static final long serialVersionUID = 1L;

	private DbColumnLong m_id = new DbColumnLong(DbConst.KEY_ID);
	private DbColumnLong m_parentTripId = new DbColumnLong(DbConst.TRIP_ID);
	private DbColumnText m_name = new DbColumnText(DbConst.NAME);
	private DbColumnLong m_when = new DbColumnLong(DbConst.WHEN);
	private DbColumnText m_address = new DbColumnText(DbConst.ADDRESS);
	private DbColumnDouble m_altitude = new DbColumnDouble(DbConst.ALTITUDE);
	private DbColumnCoordinate m_latitude = new DbColumnCoordinate(DbConst.LATITUDE);
	private DbColumnCoordinate m_longitude = new DbColumnCoordinate(DbConst.LONGITUDE);
	private DbColumnText m_comment = new DbColumnText(DbConst.COMMENT);
	private DbColumnLong m_trail = new DbColumnLong(DbConst.TRAIL);
	private DbColumnInteger m_lastCrumb = new DbColumnInteger(DbConst.LAST_CRUMB);
	private DbColumnInteger m_crumbNum = new DbColumnInteger(DbConst.CRUMB_NUM);
	
	/**
	 * Default constructor.
	 */
	public WaypointTable() {
		super();
	}

	/**
	 * Clear all elements in the table.
	 */
	public void clear() {
		m_id.clear();
		m_parentTripId.clear();
		m_name.clear();
		m_when.clear();
		m_address.clear();
		m_altitude.clear();
		m_latitude.clear();
		m_longitude.clear();
		m_comment.clear();
		m_trail.clear();
		m_lastCrumb.clear();
		m_crumbNum.clear();
		
		markNoAction();
	}

	/**
	 * Standard clone method, will perform a deep copy of this object.
	 */
	public Object clone() {
		WaypointTable clone = (WaypointTable) super.clone();

		// Perform a deep copy of these objects
		clone.m_id = (DbColumnLong) clone.m_id.clone();
		clone.m_parentTripId = (DbColumnLong) clone.m_parentTripId.clone();
		clone.m_name = (DbColumnText) clone.m_name.clone();
		clone.m_when = (DbColumnLong) clone.m_when.clone();
		clone.m_address = (DbColumnText) clone.m_address.clone();
		clone.m_altitude = (DbColumnDouble) clone.m_altitude.clone();
		clone.m_latitude = (DbColumnCoordinate) clone.m_latitude.clone();
		clone.m_longitude = (DbColumnCoordinate) clone.m_longitude.clone();
		clone.m_comment = (DbColumnText) clone.m_comment.clone();
		clone.m_trail = (DbColumnLong) clone.m_trail.clone();
		clone.m_lastCrumb = (DbColumnInteger) clone.m_lastCrumb.clone();
		clone.m_crumbNum = (DbColumnInteger) clone.m_crumbNum.clone();
		return clone;
	}
	
	/**
	 * Indicate that all fields match the database values.
	 */
	public void resetContentsChanged() {
		m_id.resetChanged();
		m_parentTripId.resetChanged();
		m_name.resetChanged();
		m_when.resetChanged();
		m_address.resetChanged();
		m_altitude.resetChanged();
		m_latitude.resetChanged();
		m_longitude.resetChanged();
		m_comment.resetChanged();
		m_trail.resetChanged();
		m_lastCrumb.resetChanged();
		m_crumbNum.resetChanged();
		
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
		if (m_parentTripId.isChanged()) {
			contentValues.put(DbConst.TRIP_ID, m_parentTripId.getData());
		}
		if (m_name.isChanged()) {
			contentValues.put(DbConst.NAME, m_name.getData());
		}
		if (m_when.isChanged()) {
			contentValues.put(DbConst.WHEN, m_when.getData());
		}
		if (m_address.isChanged()) {
			contentValues.put(DbConst.ADDRESS, m_address.getData());
		}
		if (m_altitude.isChanged()) {
			contentValues.put(DbConst.ALTITUDE, m_altitude.getData());
		}
		if (m_latitude.isChanged()) {
			contentValues.put(DbConst.LATITUDE, m_latitude.getDataStr());
		}
		if (m_longitude.isChanged()) {
			contentValues.put(DbConst.LONGITUDE, m_longitude.getDataStr());
		}
		if (m_comment.isChanged()) {
			contentValues.put(DbConst.COMMENT, m_comment.getData());
		}
		if (m_trail.isChanged()) {
			contentValues.put(DbConst.TRAIL, m_trail.getData());
		}
		if (m_lastCrumb.isChanged()) {
			contentValues.put(DbConst.LAST_CRUMB, m_lastCrumb.getData());
		}
		if (m_crumbNum.isChanged()) {
			contentValues.put(DbConst.CRUMB_NUM, m_crumbNum.getData());
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
	 * @return the parentTripId
	 */
	public long getParentTripId() {
		return m_parentTripId.getData();
	}
	/**
	 * @param data the parentTripId to set
	 */
	public void setParentTripId(long data) {
		m_parentTripId.setData(data);
		markForUpdate();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return m_name.getData();
	}
	/**
	 * @param data the name to set
	 */
	public void setName(String data) {
		m_name.setData(data);
		markForUpdate();
	}
	/**
	 * @return the when
	 */
	public long getWhen() {
		return m_when.getData();
	}
	/**
	 * @param data the when to set
	 */
	public void setWhen(long data) {
		m_when.setData(data);
		markForUpdate();
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return m_address.getData();
	}

	/**
	 * @param data the address to set
	 */
	public void setAddress(String data) {
		m_address.setData(data);
		markForUpdate();
	}

	/**
	 * @return the altitude
	 */
	public double getAltitude() {
		return m_altitude.getData();
	}

	/**
	 * @param data the altitude to set
	 */
	public void setAltitude(double data) {
		m_altitude.setData(data);
		markForUpdate();
	}

	/**
	 * @return the latitude
	 */
	public double getLatitude() {
		return m_latitude.getData();
	}
	/**
	 * @return the latitude as a String
	 */
	public String getLatitudeStr() {
		return m_latitude.getDataStr();
	}
	/**
	 * @param data the latitude to set
	 */
	public void setLatitude(String data) {
		m_latitude.setData(data);
		markForUpdate();
	}
	/**
	 * @param data the latitude to set
	 */
	public void setLatitude(double data) {
		m_latitude.setData(data);
		markForUpdate();
	}
	/**
	 * @return the longitude
	 */
	public double getLongitude() {
		return m_longitude.getData();
	}
	/**
	 * @return the longitude as a String
	 */
	public String getLongitudeStr() {
		return m_longitude.getDataStr();
	}
	/**
	 * @param data the longitude to set
	 */
	public void setLongitude(String data) {
		m_longitude.setData(data);
		markForUpdate();
	}
	/**
	 * @param data the longitude to set
	 */
	public void setLongitude(double data) {
		m_longitude.setData(data);
		markForUpdate();
	}
	/**
	 * @return the comment
	 */
	public String getComment() {
		return m_comment.getData();
	}
	/**
	 * @param data the comment to set
	 */
	public void setComment(String data) {
		m_comment.setData(data);
		markForUpdate();
	}

	/**
	 * @return the trail
	 */
	public long getTrail() {
		return m_trail.getData();
	}
	/**
	 * @param data the trail to set
	 */
	public void setTrail(long data) {
		m_trail.setData(data);
		markForUpdate();
	}

	/**
	 * @return the last crumb sequence number created for this location
	 */
	public int getLastCrumb() {
		return m_lastCrumb.getData();
	}
	/**
	 * @param data the last crumb sequence number created for this location
	 */
	public void setLastCrumb(int data) {
		m_lastCrumb.setData(data);
		markForUpdate();
	}

	/**
	 * @return the crumb sequence number
	 */
	public int getCrumbNum() {
		return m_crumbNum.getData();
	}
	/**
	 * @param data the crumb sequence number to set
	 */
	public void setCrumbNum(int data) {
		m_crumbNum.setData(data);
		markForUpdate();
	}

	public void parseXML(String msg) {
        final RootElement root = new RootElement(XMLTagConstants.XML_LOCATION);
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
		
		WaypointTable localInstance = null;
		
		public XMLSaxInitializer() {
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (WaypointTable)currentInstance;
		}

		public void end() {
		}

		public void prepareNodes(final Element node) {
			node.getChild(XMLTagConstants.XML_LOCATION_ID).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_id.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_TRIP_ID).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_parentTripId.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_NAME).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_name.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_WHEN).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_when.setData(XMLUtils.getValueDateMillis(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_ADDRESS).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_address.setData(XMLUtils.getValue(body));
	             }
	        });
			node.getChild(XMLTagConstants.XML_ALTITUDE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_altitude.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_LATITUDE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_latitude.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_LONGITUDE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_longitude.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_COMMENT).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_comment.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_TRAIL).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_trail.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_LAST_CRUMB).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_lastCrumb.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_CRUMB_NUM).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_crumbNum.setStringData(body);
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
	        serializer.startTag("", XMLTagConstants.XML_LOCATION);
			serializeXML(serializer);
            serializer.endTag("", XMLTagConstants.XML_LOCATION);
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
	        serializer.startTag("", XMLTagConstants.XML_LOCATION_ID);
            serializer.text(m_id.toString());
            serializer.endTag("", XMLTagConstants.XML_LOCATION_ID);
	        serializer.startTag("", XMLTagConstants.XML_TRIP_ID);
            serializer.text(m_parentTripId.toString());
            serializer.endTag("", XMLTagConstants.XML_TRIP_ID);
	        serializer.startTag("", XMLTagConstants.XML_NAME);
            serializer.text(m_name.toString());
            serializer.endTag("", XMLTagConstants.XML_NAME);
	        serializer.startTag("", XMLTagConstants.XML_WHEN);
            serializer.text(XMLUtils.formatDate(m_when.getData()));
            serializer.endTag("", XMLTagConstants.XML_WHEN);
            String address = m_address.toString();
            if (address != null) {
		        serializer.startTag("", XMLTagConstants.XML_ADDRESS);
	            serializer.cdsect(address);
	            serializer.endTag("", XMLTagConstants.XML_ADDRESS);
            }
	        serializer.startTag("", XMLTagConstants.XML_ALTITUDE);
            serializer.text(m_altitude.toString());
            serializer.endTag("", XMLTagConstants.XML_ALTITUDE);
	        serializer.startTag("", XMLTagConstants.XML_LATITUDE);
            serializer.text(m_latitude.toString());
            serializer.endTag("", XMLTagConstants.XML_LATITUDE);
	        serializer.startTag("", XMLTagConstants.XML_LONGITUDE);
            serializer.text(m_longitude.toString());
            serializer.endTag("", XMLTagConstants.XML_LONGITUDE);
            String comment = m_comment.toString();
            if (comment != null) {
		        serializer.startTag("", XMLTagConstants.XML_COMMENT);
	            serializer.cdsect(comment);
	            serializer.endTag("", XMLTagConstants.XML_COMMENT);
            }
	        serializer.startTag("", XMLTagConstants.XML_TRAIL);
            serializer.text(m_trail.toString());
            serializer.endTag("", XMLTagConstants.XML_TRAIL);
	        serializer.startTag("", XMLTagConstants.XML_LAST_CRUMB);
            serializer.text(m_lastCrumb.toString());
            serializer.endTag("", XMLTagConstants.XML_LAST_CRUMB);
	        serializer.startTag("", XMLTagConstants.XML_CRUMB_NUM);
            serializer.text(m_crumbNum.toString());
            serializer.endTag("", XMLTagConstants.XML_CRUMB_NUM);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
