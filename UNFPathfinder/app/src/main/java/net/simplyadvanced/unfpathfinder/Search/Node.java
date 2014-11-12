package net.simplyadvanced.unfpathfinder.Search;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class Node {

    //Decs
    private LatLng mLatlog;
    private ArrayList<LatLng> adjacency = new ArrayList<LatLng>();

    public Node(){

    }

    public Node(LatLng latLng){
        mLatlog = latLng;
    }

    public LatLng getLatLog(){
        return mLatlog;
    }

    @Override
    public String toString(){
        return "";
    }

}
