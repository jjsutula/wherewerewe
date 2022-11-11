package com.nono.util;

import java.util.ArrayList;
import java.util.List;

import com.google.android.maps.GeoPoint;

public class Navigator {

	  
//	public String getUrl(GeoPoint src, GeoPoint dest){  
//	  
//		StringBuilder sb = new StringBuilder();  
//		  
//		sb.append("http://maps.google.com/maps?f=d&hl=en");  
//		sb.append("&saddr=");  
//		sb.append(Double.toString((double) src.getLatitudeE6() / 1.0E6));  
//		sb.append(",");  
//		sb.append(Double.toString((double) src.getLongitudeE6() / 1.0E6));  
//		sb.append("&daddr=");// to  
//		sb.append(Double.toString((double) dest.getLatitudeE6() / 1.0E6));  
//		sb.append(",");  
//		sb.append(Double.toString((double) dest.getLongitudeE6() / 1.0E6));  
//		sb.append("&ie=UTF8&0&om=0&output=dragdir");  // returns JSON, to get a KML string use output=kml
//		
//		return sb.toString();  
//	}
//	
//	public List<GeoPoint> getGeopoints(String encoded) {
//		// get only the encoded geopoints  
//		encoded = encoded.split("points:"")[1].split("",")[0];  
//		// replace two backslashes by one (some error from the transmission)  
//		encoded = encoded.replace("\\\\", "\\");  
//		  
//		//decoding  
//		List<geopoint> poly = new ArrayList<geopoint>();  
//		        int index = 0, len = encoded.length();  
//		        int lat = 0, lng = 0;  
//		  
//		        while (index < len) {  
//		            int b, shift = 0, result = 0;  
//		            do {  
//		                b = encoded.charAt(index++) - 63;  
//		                result |= (b & 0x1f) << shift;  
//		                shift += 5;  
//		            } while (b >= 0x20);  
//		            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));  
//		            lat += dlat;  
//		  
//		            shift = 0;  
//		            result = 0;  
//		            do {  
//		  
//		                b = encoded.charAt(index++) - 63;  
//		                result |= (b & 0x1f) << shift;  
//		                shift += 5;  
//		            } while (b >= 0x20);  
//		            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));  
//		            lng += dlng;  
//		  
//		            GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));  
//		            poly.add(p);  
//		        } 
//	}
}
