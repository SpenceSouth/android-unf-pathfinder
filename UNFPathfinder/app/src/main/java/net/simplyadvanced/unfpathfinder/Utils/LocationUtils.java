package net.simplyadvanced.unfpathfinder.Utils;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

import net.simplyadvanced.unfpathfinder.MapCenteringUtils;
import net.simplyadvanced.unfpathfinder.Search.Node;
import net.simplyadvanced.unfpathfinder.Search.Path;

/**
 * Created by Spence on 11/12/2014.
 */
public class LocationUtils {

    //Decs
    private static LocationUtils mLocationUtils;

    private LocationUtils(){

    }

    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        int earthRadius = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = earthRadius * c;
        return d;
    }

    public static double calculateDistance(Node aa, Node bb) {
        double lat1 = aa.getLatLog().latitude;
        double lng1 = aa.getLatLog().longitude;
        double lat2 = bb.getLatLog().latitude;
        double lng2 = bb.getLatLog().longitude;
        int earthRadius = 6371; // average radius of the earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = earthRadius * c;
        return d;
    }

    public static LatLng getMidpoint(LatLng a, LatLng b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Don't pass in null.");
        }
        double midLatitude = (a.latitude + b.latitude) / 2;
        double midLongitude = (a.longitude + b.longitude) / 2;
        return new LatLng(midLatitude, midLongitude);
    }

    public static double calculateWalkingTime(Path path){
        return 0;
    }

    public static LatLng getCurrentPosition(Context context){
        return MapCenteringUtils.getCurrentGpsPosition(context);
    }

}
