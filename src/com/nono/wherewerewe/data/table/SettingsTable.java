package com.nono.wherewerewe.data.table;

import java.io.StringWriter;

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

public class SettingsTable extends DbTable {

	private static final long serialVersionUID = 1L;

	private DbColumnLong m_id = new DbColumnLong(DbConst.KEY_ID);
	private DbColumnInteger m_type = new DbColumnInteger(DbConst.TYPE);
	private DbColumnText m_setting = new DbColumnText(DbConst.SETTING);
	
	/**
	 * Default constructor.
	 */
	public SettingsTable() {
		super();
	}

	/**
	 * Clear all elements in the table.
	 */
	public void clear() {
		m_id.clear();
		m_type.clear();
		m_setting.clear();
		
		markNoAction();
	}
	
	/**
	 * Indicate that all fields match the database values.
	 */
	public void resetContentsChanged() {
		m_id.resetChanged();
		m_type.resetChanged();
		m_setting.resetChanged();
		
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
		if (m_setting.isChanged()) {
			contentValues.put(DbConst.SETTING, m_setting.getData());
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
	 * @return the setting
	 */
	public String getSetting() {
		return m_setting.getData();
	}
	/**
	 * @param data the setting to set
	 */
	public void setSetting(String data) {
		m_setting.setData(data);
		markForUpdate();
	}

	/**
	 * @param data the setting to set
	 */
	public void setSetting(long data) {
		m_setting.setData(Long.toString(data));
		markForUpdate();
	}
	/**
	 * @return the setting
	 */
	public long getSettingLong() {
    	long retval = -1;
    	String str = m_setting.getData();
    	if (str != null) {
    		try {
    			retval = Long.parseLong(str);
			} catch (Exception e) {}
    	}

    	return retval;
	}

	public void parseXML(String msg) {
        final RootElement root = new RootElement(XMLTagConstants.XML_SETTING);
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
		
		SettingsTable localInstance = null;
		
		public XMLSaxInitializer() {
		}

		public void setCurrentInstance(SAXSerializable currentInstance) {
			localInstance = (SettingsTable)currentInstance;
		}

		public void end() {
		}

		public void prepareNodes(final Element node) {
			node.getChild(XMLTagConstants.XML_SETTINGS_ID).setEndTextElementListener(new EndTextElementListener(){
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
			node.getChild(XMLTagConstants.XML_DATA).setEndTextElementListener(new EndTextElementListener(){
	             public void end(String body) {
	            	 localInstance.m_setting.setData(XMLUtils.getValue(body));
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
	        serializer.startTag("", XMLTagConstants.XML_SETTING);
			serializeXML(serializer);
            serializer.endTag("", XMLTagConstants.XML_SETTING);
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
	        serializer.startTag("", XMLTagConstants.XML_SETTINGS_ID);
            serializer.text(m_id.toString());
            serializer.endTag("", XMLTagConstants.XML_SETTINGS_ID);
	        serializer.startTag("", XMLTagConstants.XML_TYPE);
            serializer.text(m_type.toString());
            serializer.endTag("", XMLTagConstants.XML_TYPE);
	        serializer.startTag("", XMLTagConstants.XML_DATA);
            serializer.text(m_setting.getData());
            serializer.endTag("", XMLTagConstants.XML_DATA);
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}
}
