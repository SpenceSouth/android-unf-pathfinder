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
    private ArrayList<String> aliases = new ArrayList<String>();
    private boolean isDestinationNode = false;
    private boolean isCovered = false;
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

    public void addAlias(String title){
        aliases.add(title);
        isDestinationNode = true;
    }

    public ArrayList<String> getAliases(){
        return aliases;
    }

    public String getTitle(){
        if(aliases.size() > 0){
            return aliases.get(0);
        }
        else{
            return "No title";
        }
    }

    public boolean isDestinationNode(){
        return isDestinationNode;
    }

    public boolean isCovered(){
        return isCovered;
    }

    public void setIsCovered(boolean b){
        isCovered = b;
    }

    @Override
    public String toString(){
        return "";
    }

}
