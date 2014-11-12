package net.simplyadvanced.unfpathfinder.Search;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.simplyadvanced.unfpathfinder.R;

import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class SearchManager {

    //Decs
    private static SearchManager mSearchManager;
    private static GoogleMap mMap;
    private static ArrayList<Node> storage = new ArrayList<Node>();
    private Polyline line;
    private static Context mContext;
    private Marker marker;

    private SearchManager(){

    }

    public static SearchManager getInstance(){
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
        }
        return mSearchManager;
    }

    public static SearchManager getInstance(GoogleMap map, Context context){
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
            mMap = map;
            mContext = context;

        }
        return mSearchManager;
    }

    public void loadNodes(){

    }

    public void drawPath(Path path){

        String message = "Walking time: " + path.getWalkingTime() + " minutes\n";
        message += "Distance: " + path.getPathDistance() + " miles";

        //Draws path on map
        for(int i = 0; i < path.size()-1; i++){
            line = mMap.addPolyline(new PolylineOptions()
                    .add(path.getNode(i).getLatLog(), path.getNode(i+1).getLatLog())
                    .width(5).color(Color.BLUE)
                    .geodesic(true));
        }

        //Display ending point with walking time and distances
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        Log.d("DrawPath",message);

        //TODO: Have title pull form the destination list
        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle())
                .snippet(message));

    }

    private BitmapDescriptor getIconForMapMarker() {
        // Eventually, more conditions will be added here maybe.
        return BitmapDescriptorFactory.fromResource(R.drawable.abc_ab_share_pack_holo_dark);
    }


}
