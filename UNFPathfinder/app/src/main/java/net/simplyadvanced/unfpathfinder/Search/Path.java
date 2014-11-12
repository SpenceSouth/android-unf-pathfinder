package net.simplyadvanced.unfpathfinder.Search;

import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;

/**
 * Created by Spence on 11/12/2014.
 */
public class Path {

    //Decs
    ArrayList<Node> nodes = new ArrayList<Node>();

    public Path(){

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

}
