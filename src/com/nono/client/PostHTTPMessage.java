package com.nono.client;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;

public class PostHTTPMessage
{
	private final static int CONNECT_TIMEOUT = 1000 * 20;
	
    protected String location;
    private HashMap<String, String> requestProperties = new HashMap<String, String>();

    public PostHTTPMessage()
    {
        // Set to default value. Call setRequestProperty() to override.
        setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    }

    public void setURL(String value)
    {
        location = value;
    }

    public void setRequestProperty(String key, String value)
    {
        requestProperties.put(key, value);
    }

    public String sendMessage(String request) throws IOException
    {
        String response = "";
        URLConnection con = null;

        try
        {
            URL postURL = new URL(location);
            con = postURL.openConnection();
            con.setDoInput (true);
            con.setDoOutput (true);
            con.setUseCaches (false);
            con.setAllowUserInteraction(false);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
            PrintWriter out = new PrintWriter(byteStream, false);
            out.print(request);
            out.flush();
            con.setRequestProperty("Content-Length", String.valueOf(byteStream.size()));
            for (Iterator<String> iter = requestProperties.keySet().iterator(); iter.hasNext();)
            {
                String key = iter.next();
                String value = requestProperties.get(key);
                con.setRequestProperty(key, value);
            }
            con.setConnectTimeout(CONNECT_TIMEOUT);
            byteStream.writeTo(con.getOutputStream());
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder inStream = new StringBuilder();
            String line;
            while((line = in.readLine()) != null)
            {
                inStream.append(line);
            }

            response = new String(inStream.toString());
        }
        catch(MalformedURLException e)
        {
            String msg = "MalformedURLException caught in PostHTTPMessage.\nException = " + e.getMessage();
            throw new IOException(msg);
        }
        finally
        {
            // clean up connection
            if (con != null)
            {
                try
                {
                    con.getOutputStream().close();
                    con.getInputStream().close();
                }
                catch (IOException e1)
                {
                    // Don't care because we are cleaning up
                }
            }
        }

        return response;
    }
}
