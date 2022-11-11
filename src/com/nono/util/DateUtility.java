package com.nono.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * This class contains some useful functions for converting date/time values
 * between different data types and performing date calcuations using floating
 * point values to represent fractions of a day.
 */
public class DateUtility extends Object {

    private final static int            SECONDS_IN_A_DAY = 86400;

    public static final int     MMDDYY = 0;
    public static final int     DDMMYY = 1;
    public static final int     YYMMDD = 2;

	/**
	 * This function will add the specified amount of time to the date.
	 * The amount of time added is expressed as a floating point value in
	 * terms of days (i.e. 1.0 would equal one day).
	 */
	public synchronized static Date addTime(Date dtDate, float fTime) {
		// Add the specified amount of time in milliseconds
		long nMills = (long) (fTime * (24 * 60 * 60 * 1000));
		return new Date(dtDate.getTime() + nMills);
	}

	/**
	 * This utility method will add the number of seconds specified by
     * the second parameter to the specified date.
	 *
	 * @param   date  The date to use as a base line.
	 * @param   second  The number of seconds to add
	 * @return     The resulting date/time.
	 */
    public synchronized static Date addSeconds(Date date, long second) {

        if (date == null)
            return null;

        long millis = 1000 * second;
        return new Date(date.getTime() + millis);
    }

	/**
	 * This utility method will subtract the number of seconds specified by
     * the second parameter from the specified date.
	 *
	 * @param   date  The date to use as a base line.
	 * @param   second  The number of seconds to subtract.
	 * @return     The resulting date/time.
	 */
    public synchronized static Date subtractSeconds(Date date, long second) {

        if (date == null)
            return null;

        long millis = 1000 * second;
        return new Date(date.getTime() - millis);
    }

	/**
	 * Subtract Date2 from Date1 and return the difference between the two
	 * in seconds.
	 */
	public synchronized static int subtractTime(Date Date1, Date Date2) {
		long n1 = Date1.getTime();
		long n2 = Date2.getTime();
		return (int) ((n1 - n2) / 1000);
	}

	/**
	 * Returns the number of milliseconds between two Dates.
	 */
	public synchronized static long computeDateDiff(Date date1, Date date2) {
		long n1 = date1.getTime();
		long n2 = date2.getTime();
		return n1 - n2;
	}

	/**
	 * Returns the number of seconds between two Dates.
	 */
	public synchronized static long computeDateDiffSeconds(Date date1, Date date2) {
		long n1 = date1.getTime();
		long n2 = date2.getTime();
		return (n1 - n2) / 1000;
	}

	private static final TimeZone tz = TimeZone.getDefault();

    /**
	 * Convert a GMT Date to a local Date using the timezone offset from GMT.
	 */
	public synchronized static Date gmtToLocal(Date gmtDate) {

        return (new Date(gmtDate.getTime() + tz.getRawOffset()));
    }

    /**
	 * Convert a local Date to a GMT Date using the timezone offset from GMT.
	 */
	public synchronized static Date localToGMT(Date localDate) {

        return (new Date(localDate.getTime() - tz.getRawOffset()));
    }

    /**
     * Convert a floating point value representing the number of days
     * to an integer representing the corresponding total number of
     * seconds.
     *
     * @param numDays The floating point value for the number of days in the duration.
     * Rounding errors could conceivably occur as the number is
     * first converted to seconds and then the screen values are set.
     * @return The total number of seconds in the duration.
     */
    public synchronized static int convertDayFloatToSeconds( float numDays )
    {
        float   secondsF;
        int     totalSeconds;

        secondsF = numDays * SECONDS_IN_A_DAY;
        totalSeconds = Math.round(secondsF);

        return( totalSeconds );
    }

    /**
     * Convert an integer representing the total number of seconds to
     * a corresponding floating point value representing the number of
     * days, minutes and seconds.
     * @param seconds The number of seconds in the duration.
     * @return The number of days in the duration.
     */
    public synchronized static float convertSecondsToDayFloat( int seconds )
    {
        float secondsF = (float)((float)seconds / (float)SECONDS_IN_A_DAY);
        return( secondsF );
    }

    /**
     * Validates and converts a time string into an integer time of the
     * format [H]HMM. If the input time is empty, -1 is returned. If the
     * input time is invalid, -2 is returned.
     */
    public synchronized static int getIntegerTime(String timeStrIn)
    {
        int         hour;
        int         minute;
        String      formattedStr;

        if ( timeStrIn.length() == 0 )
            return -1;

        formattedStr = reformatTime(timeStrIn);
        if ( formattedStr == null || formattedStr.length() != 5 )
            return -2;

        hour = Integer.parseInt(formattedStr.substring(0,2));
        minute = Integer.parseInt(formattedStr.substring(3));

        return (hour * 100 ) + minute;
    }

    /**
     * Convert a string of the format yyyy-mm-ddThh:mm:ss to a Date object.
     * This is the format used when passing a date to the MTS Server.
     */
    public synchronized static Date convertStringToDate( String dateStr )
    {
        int     year;
        int     month;
        int     day;
        int     hour;
        int     minute;
        int     second;

        if ( dateStr == null || dateStr == "" )
            return null;

        StringTokenizer toks = new StringTokenizer(dateStr, "-T:", false);
        if( toks.countTokens() != 6 )
            return null;

        try
        {
            year  = Integer.parseInt(toks.nextToken());
            month  = Integer.parseInt(toks.nextToken());
            day    = Integer.parseInt(toks.nextToken());
            hour   = Integer.parseInt(toks.nextToken());
            minute = Integer.parseInt(toks.nextToken());
            second = Integer.parseInt(toks.nextToken());
        }
        catch (NumberFormatException nfe)
        {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day, hour, minute, second);

        return calendar.getTime();
    }

    /**
     * Convert a Date object to a string formated yyyy-mm-ddThh:mm:ss.
     * This is the format used when passing a date to the MTS Server.
     */
    public synchronized static String convertDateToString( Date date)
    {
        int     year;
        int     month;
        int     day;
        int     hour;
        int     minute;
        int     second;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        year   = calendar.get(Calendar.YEAR);
        month  = calendar.get(Calendar.MONTH) + 1;
        day    = calendar.get(Calendar.DAY_OF_MONTH);
        hour   = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);

        return( StringUtilities.toStringZeroFill(year, 4)   + "-" +
        		StringUtilities.toStringZeroFill(month, 2)  + "-" +
        		StringUtilities.toStringZeroFill(day, 2)    + "T" +
        		StringUtilities.toStringZeroFill(hour, 2)   + ":" +
        		StringUtilities.toStringZeroFill(minute, 2) + ":" +
        		StringUtilities.toStringZeroFill(second, 2) );
    }

    /**
     * Convert a Date object to a string formated HHMMSS.
     */
    public synchronized static String dateToHhmmss( Date date)
    {
        int     hour;
        int     minute;
        int     second;
        String  hourText;
        String  minuteText;
        String  secondText;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        hour   = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
        second = calendar.get(Calendar.SECOND);

        if (hour < 10)
            hourText = "0" + String.valueOf(hour);
        else
            hourText = String.valueOf(hour);

        if (minute < 10)
            minuteText = "0" + String.valueOf(minute);
        else
            minuteText = String.valueOf(minute);

        if (second < 10)
            secondText = "0" + String.valueOf(second);
        else
            secondText = String.valueOf(second);

        return(hourText + minuteText + secondText);
    }

    /**
     * Converts a Date field into a standard string
     * date format of M[M]/D[D]/YYYY. If the input date is invalid, null is
     * returned. If the input date is an empty string, an empty string
     * is returned.
     */
    public synchronized static String reformatDate(Date date)
    {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
        return df.format(date);
    }

    /**
     * Validates and converts a date string into a standard
     * date format of M[M]/D[D]/YYYY. If the input date is invalid, null is
     * returned. If the input date is an empty string, an empty string
     * is returned.
     */
    public synchronized static String reformatDate(String str)
    {
        Date tempDate;

        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);

        try
        {
            tempDate = df.parse(str);
        }
        catch (ParseException e)
        {
            return null;
        }

        return df.format(tempDate);
    }

    /**
     * Validates and converts an integer time of the format H[H}MM into
     * a standard 5 character time format of HH:MM. If the input time
     * is invalid, null is returned. If the input time is an empty string,
     * an empty string is returned.
     */
    public synchronized static String reformatTime(int intTime)
    {
        return( reformatTime(Integer.toString(intTime)) );
    }

    /**
     * Formats HHMM, HMM, HH, H into HH:MM.
     * Validates and converts a time string into a standard 5 character
     * time format of HH:MM. If the input time is invalid, null is
     * returned. If the input time is an empty string, an empty string
     * is returned.
     */
    public synchronized static String reformatTime(String str)
    {
        String              matchString;
        StringTokenizer     toks;
        String              token = "";
        String              hourText;
        String              minuteText;
        char                delimiter = ':';
        int                 hour = 0;
        int                 minute = 0;


        if ( str.length() == 0 )
            return ( "" );

        //Check for invalid characters
        matchString = "0123456789" + delimiter;
        toks = new StringTokenizer(str, matchString, false);
        if( toks.countTokens() > 0 )
            return null;

        // Check for more than 1 delimiter
        toks = new StringTokenizer(str, token + delimiter, false);
        if ( toks.countTokens() > 2 )
            return null;

        if ( toks.countTokens() == 2 )
        {
            // Got a delimiter
            hour = Integer.parseInt(toks.nextToken());
            minute = Integer.parseInt(toks.nextToken());
        }
        else
        {
            // No delimiter found
            if ( str.length() == 4 )
            {
                // Full HHMM entry
                hour = Integer.parseInt(str.substring(0,2));
                minute = Integer.parseInt(str.substring(2,4));
            }
            else if ( str.length() == 3 )
            {
                // Assume HMM entry
                hour = Integer.parseInt(str.substring(0,1));
                minute = Integer.parseInt(str.substring(1,3));
            }
            else if ( (str.length() == 2) || (str.length() == 1) )
            {
                // Assume HH[00] or H[00] entry
                hour = Integer.parseInt(str);
                minute = 0;
            }
            else
                return null;
        }

        // Verfy range
        if ( (hour < 0) || (hour > 23) || (minute < 0) || (minute > 59) )
            return null;

        // Fromat the time string to be returned
        if (hour < 10)
            hourText = "0" + String.valueOf(hour);
        else
            hourText = String.valueOf(hour);

        if (minute < 10)
            minuteText = "0" + String.valueOf(minute);
        else
            minuteText = String.valueOf(minute);

        return(hourText + delimiter + minuteText);
    }

    public synchronized static boolean parseDateString (String inDate, StringBuffer outDate,
                                           Date date, int format, char delimiter)
    {
        String[]            parseStrings;
        String              dateString;
		SimpleDateFormat    sdf = new SimpleDateFormat();
		ParsePosition       pp = new ParsePosition(0);
		Date                parseDate;

        if (inDate.length() == 0)
            return(false);

        parseStrings = buildDateParseStrings(format, delimiter);
        sdf.setTimeZone(TimeZone.getDefault());
        sdf.setLenient(false);

        for (int n = 0; n < parseStrings.length; n++)
        {
            pp.setIndex(0);
            sdf.applyPattern(parseStrings[n]);
            if ((parseDate = sdf.parse(inDate, pp)) != null)
            {
                sdf.applyPattern(parseStrings[0]);
                dateString = new String(sdf.format(parseDate));
                outDate.setLength(0);
                outDate.append(dateString);
                date.setTime(parseDate.getTime());
                return(true);
            }
        }

        return(false);
    }

    public synchronized static String[] buildDateParseStrings(int format, char delimiter)
    {
        String[]            parseStrings = new String[2];
        StringBuffer        parse;

        // Build with delimiters
        parse = new StringBuffer();
        switch (format)
        {
            case (DateUtility.MMDDYY):
                parse.append("MM").append(delimiter);
                parse.append("dd").append(delimiter);
                parse.append("yy");
                break;

            case (DateUtility.DDMMYY):
                parse.append("dd").append(delimiter);
                parse.append("MM").append(delimiter);
                parse.append("yy");
                break;

            case (DateUtility.YYMMDD):
                parse.append("yy").append(delimiter);
                parse.append("MM").append(delimiter);
                parse.append("dd");
                break;
        }
        parseStrings[0] = parse.toString();

        // Build without delimiters
        parse = new StringBuffer();
        switch (format)
        {
            case (DateUtility.MMDDYY):
                parse.append("MM");
                parse.append("dd");
                parse.append("yy");
                break;

            case (DateUtility.DDMMYY):
                parse.append("dd");
                parse.append("MM");
                parse.append("yy");
                break;

            case (DateUtility.YYMMDD):
                parse.append("yy");
                parse.append("MM");
                parse.append("dd");
                break;
        }
        parseStrings[1] = parse.toString();

        return(parseStrings);
    }

	/**
	 * Testing function.
	 */
	public static void main(String args[]) {
		Date dtCurrent = new Date();
		Date dtLocal = new Date();
		Date dtGMT;

		System.out.println("Current time is " + dtCurrent);

		Date dtNew = addTime(dtCurrent, 1);
		System.out.println(dtCurrent + " plus 1 is " + dtNew);
		System.out.println(dtNew + " minus " + dtCurrent + " equals " + subtractTime(dtNew, dtCurrent));

		dtNew = addTime(dtCurrent, 2);
		System.out.println(dtCurrent + " plus 2 is " + dtNew);
		System.out.println(dtNew + " minus " + dtCurrent + " equals " + subtractTime(dtNew, dtCurrent));

		dtNew = addTime(dtCurrent, 5);
		System.out.println(dtCurrent + " plus 5 is " + dtNew);
		System.out.println(dtNew + " minus " + dtCurrent + " equals " + subtractTime(dtNew, dtCurrent));

		dtNew = addTime(dtCurrent, 172);
		System.out.println(dtCurrent + " plus 172 is " + dtNew);
		System.out.println(dtNew + " minus " + dtCurrent + " equals " + subtractTime(dtNew, dtCurrent));

		dtNew = addTime(dtCurrent, 9122);
		System.out.println(dtCurrent + " plus 9122 is " + dtNew);
		System.out.println(dtNew + " minus " + dtCurrent + " equals " + subtractTime(dtNew, dtCurrent));

		System.out.println("Current Date = " + dtCurrent);
		System.out.println();
		System.out.println("GMT tests:");

		System.out.println("Local date is " + dtLocal);
		dtGMT = localToGMT(dtLocal);
		System.out.println("GMT date after 'localToGMT(dtLocal)' is " + dtGMT);
		dtLocal = gmtToLocal(dtGMT);
		System.out.println("Local date after 'gmtToLocal(dtGMT)' is " + dtLocal);
	}
}