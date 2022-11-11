package com.nono.xml;

import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.nono.util.StringUtilities;

public class XMLUtils {

	private static final ThreadLocal<SimpleDateFormat> m_dateLocal =
		new ThreadLocal<SimpleDateFormat>() {
			protected SimpleDateFormat initialValue() {
				return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			}
	};

	/**
	 * @return The node value as a short integer.
	 */
	public static String getValue(String value) {
		return value == null || value.length() == 0 ? null : value;
	}

	/**
	 * @return The node value as a boolean value
	 */
	public static boolean getValueBoolean(String value) {
		return StringUtilities.parseBoolean(value);
	}

	/**
	 * @return The node value as a Date.
	 */
	public static Date getValueDate(String value) {
        Date retVal;
        try {
            retVal = parseDate(value);
        } catch (Throwable e) {
            retVal = null;
        }
		return retVal;
	}

	/**
	 * Input a Date in ISO 8601 format which is the XML standard for dates.
	 * @return The node value as a long, representing milliseconds.
	 */
	public static long getValueDateMillis(String value) {
        Date retVal;
        long millis = -1; 
        try {
            retVal = parseDate(value);
            millis = retVal.getTime();
        } catch (Throwable e) {
            retVal = null;
        }
		return millis;
	}

	/**
	 * @return The node value as a floating point value.
	 */
	public static float getValueFloat(String value) {
		Float temp = new Float(value);
		return temp.floatValue();
	}

	/**
	 * @return The node value as a short integer.
	 */
	public static short getValueShort(String value) {
		return Short.parseShort(value);
	}

	/**
	 * @return The node value as an integer
	 */
	public static int getValueInt(String value)  {
        int     intVal = -1;

	    try {
			intVal = Integer.parseInt(value);
		} catch (NumberFormatException e) {}
        return intVal;
	}

	/** @return The node value as a long integer.
	 */
	public static long getValueLong(String value) {
		return Long.parseLong(value);
	}

	/** @return The node value as a BigDecimal object.
	 */
	public static BigDecimal getValueBigDecimal(String value) {
		if (value == null) {
			return null;
		}
		return new BigDecimal(value);
	}

	/**
	 * This routine will format a date into ISO 8601 format which is the XML
	 * standard for dates. February, 12, 2000 1:42:18 PM would be formated as
	 * 2000-02-12T13:42:18 with a T separating the date and time.
     * @param dtValueMillis The Date to format, in milliseconds. The value is the number of milliseconds since Jan. 1, 1970 GMT.
     * @return The formatted date as a string.
	 */
	public static String formatDate(long dtValueMillis) {
		if (dtValueMillis > 0) {
			Date dtValue = new Date(dtValueMillis);
			
			SimpleDateFormat formatter = m_dateLocal.get();
			return formatter.format(dtValue);
		}
		
		return "";
	}

	/**
	 * This routine will format a date into ISO 8601 format which is the XML
	 * standard for dates. February, 12, 2000 1:42:18 PM would be formated as
	 * 2000-02-12T13:42:18 with a T separating the date and time.
     * @param dtValue The Date to format.
     * @return The formatted date as a string.
	 */
	public static String formatDate(Date dtValue) {
		if (dtValue == null)
			return null;
		
		SimpleDateFormat formatter = m_dateLocal.get();
		return formatter.format(dtValue);
	}


	/**
	 * This routine will parse a string containing a date in ISO 8601 format
	 * and return the Date value.
     * @param value The Date to parse.
     * @return The parsed date as a Date.
	 */
	public static Date parseDate(String value) {
		if (value == null)
			return null;
		
		ParsePosition parsePos = new ParsePosition(0);
		SimpleDateFormat formatter = m_dateLocal.get();
		return formatter.parse(value, parsePos);
	}
}