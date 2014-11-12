package net.simplyadvanced.unfpathfinder.Search;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class Node {

    //Decs
    private LatLng mLatlog;
    private String title;
    private boolean isDestinationNode = false;
    private ArrayList<LatLng> adjacency = new ArrayList<LatLng>();

    public Node(){

    }

    public Node(LatLng latLng){
        mLatlog = latLng;
    }

    public Node(double lat, double lng){
        mLatlog = new LatLng(lat, lng);
    }

    public LatLng getLatLog(){
        return mLatlog;
    }

    public void setTitle(String title){
        this.title = title;
        isDestinationNode = true;
    }

    public String getTitle(){
        return title;
    }

    public boolean isDestinationNode(){
        return isDestinationNode;
    }

    @Override
    public String toString(){
        return "";
    }

}
