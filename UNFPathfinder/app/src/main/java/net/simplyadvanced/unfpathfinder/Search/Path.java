package net.simplyadvanced.unfpathfinder.Search;

import com.google.android.gms.maps.model.LatLng;

import net.simplyadvanced.unfpathfinder.Utils.LocationUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class Path {

    //Decs
    ArrayList<Node> nodes = new ArrayList<Node>();

    public Path(){

    }

    public void add(Node node){
        nodes.add(node);
    }

    public void add(LatLng latLng){
        nodes.add(new Node(latLng));
    }

    public ArrayList<Node> getAllNodes(){
        return nodes;
    }

    public Node getNode(int index){
        return nodes.get(index);
    }

    public int size(){
        return nodes.size();
    }

    public Node getStartingNode(){
        return nodes.get(0);
    }

    public Node getEndingNode(){
        return nodes.get(nodes.size()-1);
    }

    /**Returns the distance between all nodes of the path */
    public double getPathDistance(){
        DecimalFormat df = new DecimalFormat("#.##");
        double distance = 0;
        for(int i = 0; i < nodes.size()-1; i++){
            distance += LocationUtils.calculateDistance(nodes.get(i).getLatLog().latitude,
                    nodes.get(i).getLatLog().longitude,
                    nodes.get(i+1).getLatLog().latitude,
                    nodes.get(i+1).getLatLog().longitude);
        }
        return Double.parseDouble(df.format(distance));
    }

    /**Returns walking time in minutes*/
    public double getWalkingTime(){
        DecimalFormat df = new DecimalFormat("#.##");
        double time = (60*(getPathDistance()/3.1));

        return Double.parseDouble(df.format(time));
    }
}
