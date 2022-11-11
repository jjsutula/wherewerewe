package com.nono.wherewerewe.data.table;

import java.io.StringWriter;

import org.xmlpull.v1.XmlSerializer;

import android.content.ContentValues;
import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import com.nono.data.DbColumnLong;
import com.nono.data.DbColumnText;
import com.nono.data.DbTable;
import com.nono.wherewerewe.data.DbConst;
import com.nono.xml.MsgSaxParser;
import com.nono.xml.SAXParseInitializer;
import com.nono.xml.SAXSerializable;
import com.nono.xml.XMLUtils;

public class TripTable extends DbTable {

	private static final long serialVersionUID = 1L;

	private DbColumnLong m_id = new DbColumnLong(DbConst.KEY_ID);
	private DbColumnText m_name = new DbColumnText(DbConst.NAME);
	private DbColumnLong m_startDate = new DbColumnLong(DbConst.START_DATE);
	private DbColumnLong m_endDate = new DbColumnLong(DbConst.END_DATE);
	private DbColumnLong m_parentTripId = new DbColumnLong(DbConst.TRIP_ID);
	private DbColumnText m_comment = new DbColumnText(DbConst.COMMENT);
	
	/**
	 * Default constructor.
	 */
	public TripTable() {
		super();
	}

	/**
	 * Clear all elements in the table.
	 */
	public void clear() {
		m_id.clear();
		m_name.clear();
		m_startDate.clear();
		m_endDate.clear();
		m_parentTripId.clear();
		m_comment.clear();
		
		markNoAction();
	}
	
	/**
	 * Indicate that all fields match the database values.
	 */
	public void resetContentsChanged() {
		m_id.resetChanged();
		m_name.resetChanged();
		m_startDate.resetChanged();
		m_endDate.resetChanged();
		m_parentTripId.resetChanged();
		m_comment.resetChanged();
		
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
		if (m_name.isChanged()) {
			contentValues.put(DbConst.NAME, m_name.getData());
		}
		if (m_startDate.isChanged()) {
			contentValues.put(DbConst.START_DATE, m_startDate.getData());
		}
		if (m_endDate.isChanged()) {
			contentValues.put(DbConst.END_DATE, m_endDate.getData());
		}
		if (m_parentTripId.isChanged()) {
			contentValues.put(DbConst.TRIP_ID, m_parentTripId.getData());
		}
		if (m_comment.isChanged()) {
			contentValues.put(DbConst.COMMENT, m_comment.getData());
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
	 * @return the startDate
	 */
	public long getStartDate() {
		return m_startDate.getData();
	}
	/**
	 * @param data the startDate to set
	 */
	public void setStartDate(long data) {
		m_startDate.setData(data);
		markForUpdate();
	}
	/**
	 * @return the endDate
	 */
	public long getEndDate() {
		return m_endDate.getData();
	}
	/**
	 * @param data the endDate to set
	 */
	public void setEndDate(long data) {
		m_endDate.setData(data);
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

	public void parseXML(String msg) {
        final RootElement root = new RootElement(XMLTagConstants.XML_TRIP);
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
		
		TripTable localInstance = null;
		
		public XMLSaxInitializer() {
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (TripTable)currentInstance;
		}

		public void end() {
		}

		public void prepareNodes(final Element node) {
			node.getChild(XMLTagConstants.XML_TRIP_ID).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_id.setStringData(body);
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_NAME).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_name.setData(XMLUtils.getValue(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_START_DATE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_startDate.setData(XMLUtils.getValueDateMillis(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_END_DATE).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_endDate.setData(XMLUtils.getValueDateMillis(body));
	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_PARENT_TRIP_ID).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
//	            	 localInstance.m_parentTripId.setStringData(body);
//	            	 localInstance.markForUpdate();
	             }
	        });
			node.getChild(XMLTagConstants.XML_COMMENT).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_comment.setData(XMLUtils.getValue(body));
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
	        serializer.startTag("", XMLTagConstants.XML_TRIP_ID);
            serializer.text(m_id.toString());
            serializer.endTag("", XMLTagConstants.XML_TRIP_ID);
            long parentTripId = m_parentTripId.getData();
            if (parentTripId > 0) {
		        serializer.startTag("", XMLTagConstants.XML_PARENT_TRIP_ID);
	            serializer.text(m_parentTripId.toString());
	            serializer.endTag("", XMLTagConstants.XML_PARENT_TRIP_ID);
            }
	        serializer.startTag("", XMLTagConstants.XML_NAME);
            serializer.text(m_name.toString());
            serializer.endTag("", XMLTagConstants.XML_NAME);
	        serializer.startTag("", XMLTagConstants.XML_START_DATE);
            serializer.text(XMLUtils.formatDate(m_startDate.getData()));
            serializer.endTag("", XMLTagConstants.XML_START_DATE);
	        serializer.startTag("", XMLTagConstants.XML_END_DATE);
            serializer.text(XMLUtils.formatDate(m_endDate.getData()));
            serializer.endTag("", XMLTagConstants.XML_END_DATE);
	        serializer.startTag("", XMLTagConstants.XML_PARENT_TRIP_ID);
            serializer.text(m_id.toString());
            serializer.endTag("", XMLTagConstants.XML_PARENT_TRIP_ID);
            String comment = m_comment.toString();
            if (comment != null) {
		        serializer.startTag("", XMLTagConstants.XML_COMMENT);
	            serializer.cdsect(comment);
	            serializer.endTag("", XMLTagConstants.XML_COMMENT);
            }
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
