package com.nono.wherewerewe;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Draw an overlay on the map
 */
public class MarkerOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext = null;

    public MarkerOverlay(Drawable defaultMarker, Context context) {
    	super(boundCenterBottom(defaultMarker));
    	mContext = context;
    }
    
    public void addOverlay(OverlayItem overlay) {
        mOverlays.add(overlay);
        populate();
    }
    /**
     * @see com.google.android.maps.ItemizedOverlay#size()
     */
    @Override
    public int size() {
    	return mOverlays.size();
    }

    @Override
    protected OverlayItem createItem(int i) {
      return mOverlays.get(i);
    }

    /**
     * React to tap events on Map by showing an appropriate detail activity
     *
     * @see com.google.android.maps.ItemizedOverlay#onTap(com.google.android.maps.GeoPoint, com.google.android.maps.MapView)
     */
    @Override
    protected boolean onTap(int index) {
      OverlayItem item = mOverlays.get(index);
      AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
      dialog.setTitle(item.getTitle());
      dialog.setMessage(item.getSnippet());
      dialog.show();
//		Toast.makeText(mContext, item.getTitle(), Toast.LENGTH_LONG).show();
      return true;
    }
}

///**
// * Draw an overlay on the map
// */
//private class MarkerOverlay extends ItemizedOverlay<OverlayItem> {
//
//	private GeoPoint markerPoint = null;
//
//    /**
//     * @param marker the push-pin
//     */
//    public MarkerOverlay(Drawable marker, double latitude, double longitude) {
//        super(marker);
////        Double lat = 42.300092 * 1E6;
////        Double lon = -95.035716 * 1E6;
//        markerPoint = getNewGeoPoint(latitude, longitude);
//        populate();
//    }
//
//    /**
//     * @see com.google.android.maps.ItemizedOverlay#size()
//     */
//    @Override
//    public int size() {
//    	int size = 1;
//    	return size;
//    }
//
//    /**
//     * @see com.google.android.maps.ItemizedOverlay#createItem(int)
//     */
//    @Override
//    protected OverlayItem createItem(int i) {
//
//    	return new OverlayItem(markerPoint, "URHere", "Here is where you are.");
//    }
//
//    /**
//     * React to tap events on Map by showing an appropriate detail activity
//     *
//     * @see com.google.android.maps.ItemizedOverlay#onTap(com.google.android.maps.GeoPoint, com.google.android.maps.MapView)
//     */
//    @Override
//    public boolean onTap(GeoPoint p, MapView mvMap1) {
//
//    	boolean nearby = false;
//        long lat = p.getLatitudeE6();
//        long lon = p.getLongitudeE6();
//        long markerLat = markerPoint.getLatitudeE6();
//        long markerLon = markerPoint.getLongitudeE6();
//
//    	if (Math.abs(markerLat-lat)<1000 && Math.abs(markerLon-lon)<1000){
//    		nearby = true;
//    	}
//
//        if (!nearby) {
//        	return false;
//        }
//
//		Toast.makeText(MapDetail.this, "Tapped Me!", Toast.LENGTH_LONG).show();
//
//        return true;
//    }
//}
