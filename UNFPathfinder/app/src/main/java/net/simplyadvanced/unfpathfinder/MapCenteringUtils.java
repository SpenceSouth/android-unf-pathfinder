package net.simplyadvanced.unfpathfinder;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/** Use this class to center the user's position only once.
 * Map may move twice because the first movement will be for the last known location.
 * Must have GoogleMaps in your project to use this class.
 *
 * A lock was put in so that this could be called multiple times without worry. Not tested.
 *
 * To use: Just call `MapCenteringUtil.centerOnce(Context, GoogleMap)`. Also,
 * you will need to add one of the following permissions:
 *     <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
 *     <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
 *
 * Idea: Possibly make a callback to let calling class know when this is done.
 */
public class MapCenteringUtils {

    private static final int DEFAULT_MAP_ZOOM_LEVEL = 12;
    private static final int DEFAULT_MAP_ZOOM_TIME = 2000; // Milliseconds.
    private static GoogleMap mMap;
    private static Context context;

    private static LocationManager mLocationManager;

    private static LocationListener mLocationListener;

    private static volatile boolean lock = false;

    /** No need to instantiate this class. */
    private MapCenteringUtils() {}

    /** Centers the map to the user's current position. If neither the GPS
     * nor network location services are enabled, then nothing will happen. */
    public static void centerOnce(Context context, final GoogleMap map, boolean isUseGps) {
        if (lock) { return; }
        lock = true;

        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        if (isGpsEnabled()) {
            Location gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gpsLocation != null) { // If there is a last known location via GPS.
                mapMoveAndZoomTo(map, new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude()));
            }
        }
        if (isNetworkEnabled()) {
            Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation != null) { // If there is a last known location via network.
                mapMoveAndZoomTo(map, new LatLng(networkLocation.getLatitude(), networkLocation.getLongitude()));
            }
        }

        mLocationListener = new LocationListener() { // Define a listener that responds to location updates
            @Override
            public void onLocationChanged(Location location) { // Called when a new location is found by the network location provider.
                mapMoveAndZoomTo(map, new LatLng(location.getLatitude(), location.getLongitude()));
                lock = false;

                // Unregister listener. It is no longer needed.
                try {
                    if (mLocationListener != null) {
                        mLocationManager.removeUpdates(mLocationListener);
                        mLocationListener = null;
                    }
                } catch (IllegalArgumentException e) { // Intent is null.
                    e.printStackTrace();
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}

        };

        // Register the listener with the Location Manager to receive location updates
        if (isUseGps && isGpsEnabled()) { mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener); }
        if (isNetworkEnabled()) { mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener); }
    }

    /** You probably mean to use PhoneLocation.getLastKnownLocation() instead because that may be
     * more accurate and doesn't use any extra battery. */
    @Deprecated
    public static LatLng getCurrentNetworkPosition(Context context){

        mLocationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        LatLng current;

        if (isNetworkEnabled()) {
            Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (networkLocation != null) { // If there is a last known location via network.
                current = new LatLng(networkLocation.getLatitude(), networkLocation.getLongitude());
                return current;
            }
        }

        return null;
    }

    private static boolean isGpsEnabled() {
        try {
            return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            // Exceptions will be thrown if provider is not permitted.
            return false;
        }
    }

    private static boolean isNetworkEnabled() {
        try {
            return mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            // Exceptions will be thrown if provider is not permitted.
            return false;
        }
    }

    public static final void mapMoveAndZoomTo(GoogleMap map, LatLng location) {
        if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_MAP_ZOOM_LEVEL), DEFAULT_MAP_ZOOM_TIME, null);
        }
    }

    public static final void mapMoveAndZoomTo(GoogleMap map, LatLng location, int zoom) {
        if (map != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom), DEFAULT_MAP_ZOOM_TIME, null);
        }
    }

}