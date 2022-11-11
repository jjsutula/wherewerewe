package com.nono.util;

import java.util.ArrayList;
import java.util.Formatter;

public class StringUtilities {

	/**
	 * Replace all of the special characters that might be embedded in the text.
	 * @param input The text that might contain special characters to be replaced.
	 * @return A new string containing replaced tokens.
	 */
	static public String tokenizeSpecialChars(String input) {
	
		// We can't do anything with a null string.
		if (input == null) {
			return null;
		}
		
		String output = input;
		// Replace special characters with tokens
		output = replaceToken(output, "\r\n", "[crlf]");
		output = replaceToken(output, "\f", "[ff]");
		output = replaceToken(output, "\n", "[lf]");
		output = replaceToken(output, "\r", "[cr]");
		output = replaceToken(output, "\t", "[tab]");
		return output;
	}

	/**
	 * Regain all of the special characters that might be embedded in the text.
	 * @param input The text that might contain special tokens to be replaced.
	 * @return     A new string containing replaced tokens.
	 */
	static public String regainSpecialChars(String input) {
	
		// We can't do anything with a null string.
		if (input == null) {
			return null;
		}
		
		String output = input;
		// Replace tokens with special characters
		output = replaceToken(output, "<crlf>", "\n");
		output = replaceToken(output, "<ff>", "\f");
		output = replaceToken(output, "<lf>", "\n");
		output = replaceToken(output, "<cr>", "\r");
		output = replaceToken(output, "<tab>", "\t");
		output = replaceToken(output, "[crlf]", "\n");
		output = replaceToken(output, "[ff]", "\f");
		output = replaceToken(output, "[lf]", "\n");
		output = replaceToken(output, "[cr]", "\r");
		output = replaceToken(output, "[tab]", "\t");
		return output;
	}

	/**
	 * This method will return a string with the specified string replaced
	 * by a token.
	 * 
	 * @param input The string to be searched.
	 * @param find  The string containing the data to look for in the input string.
	 * @param token The string containing the token that should replace those found
	 * in the input string.
	 * @return The input string with the specified token replaced.
	 */
	public static String replaceToken(String input, String find, String token) {

		// We can't do anything with a null string.
		if (input == null) {
			return null;
		}

		int lastIndex = 0;
		int index = input.indexOf(find, lastIndex);
		if (index == -1) {
			// If it didn't find any instances at all, avoid the overhead of any character moving. 
			return input;
		}

		int findLength = find.length();
		StringBuilder buffer = new StringBuilder();
		while (index != -1) {
			buffer.append(input.substring(lastIndex, index));
			buffer.append(token);

			lastIndex = index + findLength;
			index = input.indexOf(find, lastIndex);
		}
		buffer.append(input.substring(lastIndex));
		return buffer.toString();
	}


    // table used to convert a nibble to a hex char.
    private static char[] hexChar = { '0' , '1' , '2' , '3' ,
                              '4' , '5' , '6' , '7' ,
                              '8' , '9' , 'a' , 'b' ,
                              'c' , 'd' , 'e' , 'f' };

	/** This function will parse a string and return a boolean value depending
	 * upon what is in the string. It will return true if the string contains
	 * the word true or a number other than zero. If the string happens to be
	 * empty it will be considered to be false.
	 *
	 * @param parse The string to be parsed for a boolean value
	 * @return The boolean value within the string
	 */
	public static boolean parseBoolean(String parse) {
		// If the string is empty then consider it to be FALSE
		if (parse == null) {
			return false;
		}
		parse = parse.trim();
		if (parse.length() == 0) {
			return false;
		}
		if (parse.equalsIgnoreCase("true")) {
			return true;
		}
		if (parse.equalsIgnoreCase("false")) {
			return false;
		}

		int nTemp;
		try {
    		nTemp = Integer.parseInt(parse);
    	}
    	catch( NumberFormatException nfe ) {
    	    nTemp = 0;
    	}
		if (nTemp != 0) {
			return true;
		}

		// everything failed so it must be false.
		return false;
	}

	/** This function will search for the token within the target string
	 * and return an array of strings that are seperated by that token. Leading
	 * and tariling whitespace will be kept with each string.
	 * 
	 * @param target The target string containing the tokens and data.
	 * @param token The token to search for in the target string
	 * @return The strings found within the target, seperated by tokens.
	 */
	public static String [] tokenize(String target, char token) {
		return tokenize(target, token, false);
	}
	
	/** This function will search for the token within the target string
	 * and return an array of strings that are seperated by that token.
	 *
	 * @param target The target string containing the tokens and data.
	 * @param token The token to search for in the target string
	 * @param ignoreWhitespace If true, the returned string values will have
	 * leading and trailing whitespace removed.
	 * @return The strings found within the target, seperated by tokens.
	 */
	public static String [] tokenize(String target, char token, boolean ignoreWhitespace) {
		// Make sure we have a real string before we try and parse it
		if (target == null || target.length() == 0)
			return null;

		// This linked list will store the tokens we are looking for
		ArrayList<String> list = new ArrayList<String>();

		// We first want to find out how many strings we are going to have.
		int nStart = 0;
		int nIndex = target.indexOf(token, nStart);
		while (nIndex != -1) {
			// Extract the token without the seperator
			String sToken = target.substring(nStart, nIndex);
			list.add(sToken);

			nStart = nIndex + 1;
			nIndex = target.indexOf(token, nStart);
		}
		// Don't for get the last string
		String sToken = target.substring(nStart);
		list.add(sToken);

		int nCount = list.size();
		String [] saReturn = new String [nCount];
		String entry;
		for (int i = 0; i < nCount; i++) {
			entry = (String) list.get(i);
			if (ignoreWhitespace) {
				if (entry == null) {
					saReturn[i] = null;
				}
				else {
					saReturn[i] = entry.trim();
				}
			}
			else {
				saReturn[i] = entry;
			}
		}
		return saReturn;
	}

	/** This function will search for the token within the target string
	 * and return an array of strings that are seperated by that token.
	 *
	 * @param target The target string containing the tokens and data.
	 * @param token The token to search for in the target string. The entire string will be
	 * treated as the delimiter, as opposed to individual characters within the token.
	 * @param ignoreWhitespace If true, the returned string values will have
	 * leading and trailing whitespace removed.
	 * @return The strings found within the target, seperated by tokens.
	 */
	public static String [] tokenize(String target, String token, boolean ignoreWhitespace) {
		// Make sure we have a real string before we try and parse it
		if (target == null || target.length() == 0 || token == null || token.length() == 0)
			return null;

		int tokenLength = token.length();
		// This linked list will store the tokens we are looking for
		ArrayList<String> list = new ArrayList<String>();

		// We first want to find out how many strings we are going to have.
		int nStart = 0;
		int nIndex = target.indexOf(token, nStart);
		while (nIndex != -1) {
			// Extract the token without the seperator
			String sToken = target.substring(nStart, nIndex);
			list.add(sToken);

			nStart = nIndex + tokenLength;
			nIndex = target.indexOf(token, nStart);
		}
		// Don't forget the last string
		String sToken = target.substring(nStart);
		list.add(sToken);

		int nCount = list.size();
		String [] saReturn = new String [nCount];
		String entry;
		for (int i = 0; i < nCount; i++) {
			entry = (String) list.get(i);
			if (ignoreWhitespace) {
				if (entry == null) {
					saReturn[i] = null;
				}
				else {
					saReturn[i] = entry.trim();
				}
			}
			else {
				saReturn[i] = entry;
			}
		}
		return saReturn;
	}

    /**
    * Converts an array of bytes to a string of hex characters.
    * @param   b An array of bytes.
    * @return  A string of hex characters representing the array of bytes.
    */
    public static String byteArrayToHexString( byte[] b )
    {
    	if (b == null)
    		return null;

    	StringBuilder sb = new StringBuilder( b.length * 2 );
        for ( int i = 0; i < b.length; i++ )
        {
            // look up high nibble char
            sb.append( hexChar[( b[i] & 0xf0 ) >>> 4 ] );

            // look up low nibble char
            sb.append( hexChar[b[i] & 0x0f] );
        }
        return sb.toString();
    }
    
    /**
    * Converts a string of hex characters to an array of bytes and checks the length of the string.
    * @param   s A string of hex characters.
    * @param   len The length that s should be.
    * @return  An array of bytes.
    */
    public static byte[] hexStringToByteArray( String s, int len )
    {
		if (s == null)
			return null;

        int stringLength = s.length();

        if ( stringLength != len && stringLength != 0 )
        {
            System.out.println("Unable to convert String: The string must be " + len + " hex characters");
            return null;
        }

        byte[] b = hexStringToByteArray(s);

        return b;
    }

	/**
	* Converts a string of hex characters to an array of bytes.
	* @param   s A string of hex characters.
	* @return  An array of bytes.
	*/
	public static byte[] hexStringToByteArray( String s )
	{
		int stringLength = s.length();

		byte[] b = new byte[ stringLength / 2 ];

		for ( int i = 0, j = 0; i < stringLength; i += 2, j++ )
		{
			int high= charToNibble( s.charAt(i) );
			int low = charToNibble( s.charAt(i+1) );
			b[j] = (byte)( (high << 4) | low );
		}
		return b;
	}

	/**
	* Converts a hex char to to an int value.
	* @param   c A hex char.
	* @return  An int value representing the hex char specified.
	*/
	private static int charToNibble ( char c )
	{
		if ( '0' <= c && c <= '9' )
			return c - '0' ;
		else
		if ( 'a' <= c && c <= 'f' )
			return c - 'a' + 0xa ;
		else
		if ( 'A' <= c && c <= 'F' )
			return c - 'A' + 0xa ;
		else
		{
			System.out.println("Unable to decrypt key: Invalid hex character - " + c);
			return -1;
		}
	}

	/**
	* Within a given string, replace all occuences of 1 string with another.
	* @param   str  The complete string within which all occurences of oldText
	*  will be replaced with newText.
	* @param   oldText  The string of characterd that will be replaced with newText.
	* @param   newText  The string of characterd that will replaced oldText.
	* @return  A string where all occurences of oldText have been replaced with newText.
	*/
	public static String replaceText( String str,  String oldText, String newText )
	{

		if ( str.indexOf(oldText) == -1)
			return str;

		int 			startIndex = 0;
		int 			endIndex = 0;
		StringBuilder	sb = new StringBuilder(str.length());

		while(true)
		{
			endIndex = str.substring(startIndex).indexOf(oldText);
			if (endIndex == -1)
			{
				sb.append(str.substring(startIndex));
				return sb.toString();
			}
			else
			{
				endIndex = startIndex + endIndex;
				sb.append(str.substring(startIndex, endIndex));
				sb.append(newText);
				startIndex = endIndex + newText.length() + 1;
				if ( startIndex >= str.length() )
					return sb.toString();
			}
		}
	}
	
	/**
	 * If the given string contains an apostrophe, replace the 
	 * apostrophe with double apostrophes to form valid SQL.
	 * 
	 * @param strIn The string for which apostrophes will be replaced
	 * @return a valid SQL string with double apostrophes
	 */
	public static String getSqlString(String strIn)
	{
	    String sqlString = strIn;
	    
	    if ( strIn.indexOf("'") != -1)
	        sqlString = strIn.replaceAll("'", "''");
	    
	    return sqlString;
	}

	/** Testing routine to make sure that everything is working correctly.
	 */
	public static void main(String args[]) {
		// tokenize Test #1
		String sInput1 = "A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z";
		System.out.println("===== tokenize Test #1 =====");
		System.out.println("Input : " + sInput1);

		String [] saOutput1 = tokenize(sInput1, ',');
		System.out.print("Output: ");
		for (int nIndex = 0; nIndex < saOutput1.length; nIndex++) {
			System.out.print(saOutput1[nIndex] + "*");
		}
		System.out.print("\n");

		// tokenize Test #2
		String sInput2 = "Four score and seven years ago our fathers " +
						 "brought forth on this continent a new nation, " +
						 "conceived in liberty and dedicated to the " +
						 "proposition that all men are created equal. " +
						 "Now we are engaged in a great civil war, " +
						 "testing whether that nation or any nation so " +
						 "conceived and so dedicated can long endure. " +
						 "We are met on a great battle field of that war. " +
						 "We have come to dedicate a portion of that field, " +
						 "as a final resting-place for those who here gave " +
						 "their lives that that nation might live. It is " +
						 "altogether fitting and proper that we should do this.";

		System.out.println("===== tokenize Test #2 =====");
		System.out.println("Input : " + sInput2);

		String [] saOutput2 = tokenize(sInput2, ' ');
		System.out.print("Output: ");
		for (int nIndex = 0; nIndex < saOutput2.length; nIndex++) {
			System.out.print(saOutput2[nIndex] + "*");
		}
		System.out.print("\n");

		// parseBoolean Test #1
		System.out.println("===== parseBoolean Test #1 =====");
		System.out.println("Input : TRUE, Output: " + parseBoolean("TRUE"));
		System.out.println("Input : 1, Output: " + parseBoolean("1"));
		System.out.println("Input : 0, Output: " + parseBoolean("0"));
		System.out.println("Input : 288281, Output: " + parseBoolean("288281"));

	}
	
	/**
	 * Convert an int to a String and fills zeros (0) of the left if necessary.
	 * 
	 * @param value The value of the number
	 * @param digits The length of the String
	 * @return zero-filled String of the value
	 */
	public static String toStringZeroFill(int value, int digits)
	{
		StringBuilder str = new StringBuilder(String.valueOf(value));
	    
	    for (int n = 0; n < digits - str.length(); n++)
	    {
	        str.insert(0, '0');
	    }
	    
	    return str.toString();
	}
	
	/**
	 * Remove all spaces from a string.
	 * @param inStr
	 * @return The compressed string with no spaces.
	 */
	public static String removeSpaces(String inStr)
	{
		StringBuilder sb = new StringBuilder();

		for (int ndx = 0; ndx < inStr.length(); ndx++) {
			char c = inStr.charAt(ndx);
			switch(c) {
			case ' ':
			case '\t':
				break;
			default:
				sb.append(c);
				break;
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Remove all chracters that don't belong in a filename from a string.
	 * @param inStr
	 * @return The compressed string with no spaces.
	 */
	public static String removeNonFilenameChars(String inStr)
	{
		StringBuilder sb = new StringBuilder();

		for (int ndx = 0; ndx < inStr.length(); ndx++) {
			char c = inStr.charAt(ndx);
			switch(c) {
			case ' ':
			case '\t':
			case '\n':
			case '\r':
			case ',':
			case ';':
			case '\"':
			case '\\':
			case '/':
				break;
			default:
				sb.append(c);
				break;
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Sets the data.
	 * @param data the data to set
	 */
	public static String formatData(double value, int precision) {
		StringBuilder sb = new StringBuilder();
		StringBuilder formatSb = new StringBuilder();
		formatSb.append("%.");
		formatSb.append(precision);
		formatSb.append("f");
		Formatter formatter = new Formatter(sb);
		formatter.format(formatSb.toString(), value);

		return sb.toString();
	}

}
