package com.nono.util;
import java.io.IOException;
import java.util.Locale;

import org.xml.sax.DTDHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
// See http://code.google.com/p/bikeroute/
/**
 * Parse a google directions json object to a route.
 * 
 */
public class GoogleParser  {
    public static final double CNV = 1E6;

//public class GoogleParser extends XMLParser implements Parser {
//    public static final double CNV = 1E6;
//
//    public GoogleParser(String feedUrl) {
//            super(feedUrl);
//    }
//    
//    public Route parse() {
//        String result = convertStreamToString(this.getInputStream());
//        Route route = new Route();
//        Segment segment = new Segment();
//        try {
//                JSONObject json = new JSONObject(result);
//                JSONObject r = json.getJSONArray("routes").getJSONObject(0);
//                JSONObject leg = r.getJSONArray("legs").getJSONObject(0);
//                JSONArray steps = leg.getJSONArray("steps");
//                int numSteps = steps.length();
//                route.setName(leg.getString("start_address") + " to " + leg.getString("end_address"));
//                route.setCopyright(r.getString("copyrights"));
//                if (!r.getJSONArray("warnings").isNull(0)) {
//                        route.setWarning(r.getJSONArray("warnings").getString(0));
//                }
//                
//                for (int i = 0; i < numSteps; i++) {
//                        JSONObject j = steps.getJSONObject(i);
//                        JSONObject start = j.getJSONObject("start_location");
//                        GeoPoint p = new GeoPoint(convertToMicroDegrees(start.getDouble("lat")), 
//                                        convertToMicroDegrees(start.getDouble("lng")));
//                        segment.setPoint(p);
//                        segment.setLength(j.getJSONObject("distance").getInt("value"));
//                        segment.setTurn(j.getString("html_instructions").replaceAll("<(.*?)*>", ""));
//                        JSONObject poly = j.getJSONObject("polyline");
//                        route.addPoints(decodePolyLine(poly.getString("points")));
//                        route.addSegment(segment.copy());
//                }
//        } catch (JSONException e) {
//                e.printStackTrace();
//        }
//        return route;
//    }
//
//    /**
//     * Convert an inputstream to a string.
//     * @param input inputstream to convert.
//     * @return a String of the inputstream.
//     */  
//    private static String convertStreamToString(InputStream input) {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
//        StringBuilder sBuf = new StringBuilder();
// 
//        String line = null;
//        try {
//            while ((line = reader.readLine()) != null) {
//                sBuf.append(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                input.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return sBuf.toString();
//    }
//        
//    /**
//     * Decode a polyline string into a list of GeoPoints.
//     * @param poly polyline encoded string to decode.
//     * @return the list of GeoPoints represented by this polystring.
//     */
//    private List<GeoPoint> decodePolyLine(final String poly) {
//        int len = poly.length();
//        int index = 0;
//        List<GeoPoint> decoded = new ArrayList<GeoPoint>();
//        int lat = 0;
//        int lng = 0;
//
//        while (index < len) {
//        int b;
//        int shift = 0;
//        int result = 0;
//        do {
//                b = poly.charAt(index++) - 63;
//                result |= (b & 0x1f) << shift;
//                shift += 5;
//        } while (b >= 0x20);
//        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//        lat += dlat;
//
//        shift = 0;
//        result = 0;
//        do {
//                b = poly.charAt(index++) - 63;
//                result |= (b & 0x1f) << shift;
//                shift += 5;
//        } while (b >= 0x20);
//                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
//                lng += dlng;
//
//        decoded.add(new GeoPoint(
//                        convertToMicroDegrees(lat / 1E5), convertToMicroDegrees(lng / 1E5)));
//        }
//
//        return decoded;
//    }
//    
//    /**
//     * Convert degrees to microdegrees.
//     * @param degrees
//     * @return integer microdegrees.
//     */
//    
//    public int convertToMicroDegrees(final double degrees) {
//            return (int) (degrees * CNV);
//    }
//    
//    /**
//     * Convert microdegrees to degrees.
//     * @param mDegrees
//     * @return double type degrees.
//     */
//    
//    public double convertToDegrees(final int mDegrees) {
//            return mDegrees / CNV;
//    }
//
//	@Override
//	public void parse(InputSource arg0) throws SAXException, IOException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void parse(String systemId) throws SAXException, IOException {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setDTDHandler(DTDHandler handler) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setDocumentHandler(DocumentHandler handler) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setEntityResolver(EntityResolver resolver) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setErrorHandler(ErrorHandler handler) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setLocale(Locale locale) throws SAXException {
//		// TODO Auto-generated method stub
//		
//	}
}


