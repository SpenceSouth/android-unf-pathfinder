package net.simplyadvanced.unfpathfinder.Search;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class Node implements Comparable{

    //Decs
    private LatLng mLatlog;
    private String title;
    private ArrayList<String> aliases = new ArrayList<String>();
    private boolean isDestinationNode = false;
    private boolean isCovered = false;
    private ArrayList<Node> adjacency = new ArrayList<Node>();
    private double g_score;
    private double f_score;
    private Node cameFrom;
    private double distance;
    private String[] rawAdjacency;
    private int number;

    public Node(){

    }
    public void setRawAdjacency (String[] input) {rawAdjacency=input;}
    public String[] getRawAdjacency () {return rawAdjacency;}
    public void setNumber (int input) {number=input;}
    public int getNumber() {return number;}
    public double getG_score(){ return g_score;}
    public void setG_score(double input) {g_score=input;}
    public double getF_score(){ return f_score;}
    public void setF_score(double input) {f_score=input;}
    public double getDistance(){ return distance;}
    public void setDistance(double input) {distance=input;}
    public Node getCameFrom(){return cameFrom;}
    public void setCameFrom(Node input){cameFrom=input;}

    public double getDistanceTo(Node input)
    {
        Float[] distances;
        return (Math.sqrt ((this.getLatLog().latitude-input.getLatLog().latitude)*(this.getLatLog().latitude-input.getLatLog().latitude)+(this.getLatLog().longitude-input.getLatLog().longitude)*(this.getLatLog().longitude-input.getLatLog().longitude)));

    }

    public Node(LatLng latLng){
        Log.d("Node",this.toString());
        mLatlog = latLng;
    }

    public Node(double lat, double lng){
        mLatlog = new LatLng(lat, lng);
        Log.d("Node",this.toString());
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

    public ArrayList<Node> getAdjacency() {return adjacency;}
    public void setAdjacent(Node otherNode)
    {
        if (!adjacency.contains(otherNode))
        {
            adjacency.add(otherNode);
            Log.d("adjacency","Added Adjacency");
            if (!otherNode.isAdjacent(this)) {otherNode.setAdjacent(this);}
            Log.d("adjacency","Added Adjacency");
        }
    }
    public int compareTo(Object other)
    {

        if (this.f_score == ((Node) other).f_score)
            return 0;
        else if (this.f_score > ((Node) other).f_score)
            return 1;
        else
            return -1;

    }

    public boolean isDestinationNode(){
        return isDestinationNode;
    }
    public boolean isAdjacent(Node otherNode)    { return (adjacency.contains(otherNode)); }

    public boolean isCovered(){
        return isCovered;
    }

    public void setIsCovered(boolean b){
        isCovered = b;
    }

    @Override
    public String toString(){
        String next="";
        for (Node myNeigbor:adjacency)
        {
            next+=("Neighbor: "+myNeigbor.getLatLog().toString()+" \n");
        }
        return getTitle() + " " + mLatlog.toString()+"\n"+next;
    }

}
