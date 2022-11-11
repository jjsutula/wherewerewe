package com.nono.xml;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import android.sax.RootElement;
import android.util.Xml;

public class MsgSaxParser {
	
	private String msg;
	
	public MsgSaxParser(String msg) {
		this.msg = msg;
	}
	
	public InputStream getInputStream() {
		if (msg == null) {
			msg = "";
		}

		ByteArrayInputStream bais = new ByteArrayInputStream(msg.getBytes());
		return bais;
	}
	
	public void parse(RootElement root) {
        try {
        	InputStream is = getInputStream();
            Xml.parse(is, Xml.Encoding.UTF_8, root.getContentHandler());
            is.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}
}

