package net.simplyadvanced.unfpathfinder.Search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import net.simplyadvanced.unfpathfinder.MapCenteringUtils;
import net.simplyadvanced.unfpathfinder.R;
import net.simplyadvanced.unfpathfinder.Utils.LocationUtils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

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
    boolean flag = false;
    private Lock lock = new ReentrantLock();
    private Semaphore semaphore = new Semaphore(1);

    private SearchManager(){
        loadNodes();
    }

    public Context getContext(){
        return mContext;
    }

    public static SearchManager getInstance(){
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
        }
        return mSearchManager;
    }

    public static SearchManager getInstance(GoogleMap map, Context context){
        Log.d("SearchManager","creating");
        if(mSearchManager == null){
            mSearchManager = new SearchManager();
            mMap = map;
            mMap.clear();
            mContext = context;

            //TODO: Delete this when done with debugging
            //addDebuggingNodes();

        }
        return mSearchManager;
    }

    public void loadNodes(){
        //todo designate targetfile
        String targetFile="nodes.txt";
        String[] inputArray;
        String[][] inputFile=new String[1][1];
        ArrayList<String> inputList = new ArrayList<String>();
        String[]   latlongStrings=new String[1];
        double lat;
        double log;
        Node myNode;
        LatLng coordinates;

        try
        {

            FileInputStream myFileInputStream = getContext().openFileInput("nodes.txt");
            DataInputStream dataInput = new DataInputStream(myFileInputStream);
            BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(dataInput));
            String inputString;



            inputArray = (String[]) inputList.toArray(new String[inputList.size()]);
            inputFile = new String[inputArray.length][];
            for (int i=0; i<inputArray.length; i++)
            {
                inputFile[i]=inputArray[i].split(";");
            }

            //int pants=0;
        }
        catch (Exception e)
        {
            // Catch exception if any
            Toast.makeText(mContext,"The file was not found", Toast.LENGTH_SHORT).show();
            //System.err.println("Error: " + e.getMessage()+"\n");
            return;
        }
        for (int i=0; i<inputFile.length; i++) {
            //ignore commented lines
            //TODO: Breaks here with NullPointerException
            if(!inputFile[i][0].isEmpty())
            {
                if (inputFile[i][0].substring(0, 1).equals("//"))
                {
                    continue;
                }
                else {
                    latlongStrings = inputFile[i][0].split("\\s+", 2);
                }
            }
            lat=Double.parseDouble(latlongStrings[0].substring(0,latlongStrings[0].length()-2));//removes comma after lattitude
            if (latlongStrings[1].contains(";"))
            {
                latlongStrings[1]=latlongStrings[1].substring(0,latlongStrings[1].length()-2); //removes semicolen
            }
            log=Double.parseDouble(latlongStrings[1]);
            myNode=new Node (lat, log);

            for (int j=1; j<inputFile[i].length; j++)
            {
                //todo need a cleaner way to check if numeric or a name
                try
                {
                  coordinates=nodeCoords(inputFile[i][j]);
                  for (Node otherNode: storage )
                    {
                        if (otherNode.getLatLog().equals(coordinates)) {myNode.setAdjacent(otherNode);}
                    }
                }
                catch (NumberFormatException n)
                {
                    if (latlongStrings[1].contains(";"))
                    {
                        latlongStrings[1]=latlongStrings[1].substring(0,latlongStrings[1].length()-2); //removes semicolen
                    }
                    myNode.addAlias(inputFile[i][j]);
                }
            }
            storage.add(myNode);
        }
    }

    private LatLng nodeCoords(String input) throws NumberFormatException
    {
        double lat;
        double log;
        LatLng out;
        String[] latlongStrings;
        {
            latlongStrings=input.split("\\s+", 2);
        }
        lat=Double.parseDouble(latlongStrings[0].substring(0,latlongStrings[0].length()-2));//removes comma after lattitude
        if (latlongStrings[1].contains(";"))
        {
            latlongStrings[1]=latlongStrings[1].substring(0,latlongStrings[1].length()-2); //removes semicolen
        }
        log=Double.parseDouble(latlongStrings[1]);
        out=new LatLng(lat,log);
        return out;

    }

    //pased from aaron's A* project
    public static void aStar(Node start, Node finish)
    {

        Node current;
        ArrayList<Node> closedset = new ArrayList<Node>();
        List<Node> openset = new ArrayList<Node>();
        ArrayList<Node> camefrom;
        start.setG_score(0);
        //Double fscore=start.g_score+start.distance;
        Double gscore=0.0;
        Double tenGscore;
        float[] distances;
        for (Node everyNode: storage)
        {

            everyNode.setDistance(everyNode.getDistanceTo(finish));
            //reinitalize nodes
            everyNode.setF_score(0);
            everyNode.setG_score(0);
            everyNode.setCameFrom(null);
        }
        current=start;
        openset.add(start);
        while (!openset.isEmpty()) {
            //need to find collections and Comparator
            Collections.sort(openset, new Comparator<Node>(){
                @Override
                public int compare(Node node1, Node node2)
                {
                    return node1.compareTo(node2);

                }
            });

            current=openset.get(0);
            //System.out.println(" Mt Bond is considering "+current.toString()+"It's score is: "+current.f_score);

            if (current==finish)
            {
                //reconstruct_path();
                return;
            }


            openset.remove(current);
            closedset.add(current);
            for (Node neighbor: current.getAdjacency())
            {
                if (closedset.contains(neighbor))continue;
                tenGscore=current.getG_score()+current.getDistanceTo(neighbor);

                if (!openset.contains(neighbor)||tenGscore<current.getG_score())
                {

                    neighbor.setCameFrom(current);
                    neighbor.setG_score(tenGscore);
                    neighbor.setF_score(neighbor.getG_score()+neighbor.getDistance());
                    if (!openset.contains(neighbor))
                    {
                        openset.add(neighbor);
                        //System.out.println("Mr Bond went from "+current.toString()+" to "+neighbor.toString()+"\n Tenative score is:"+tenGscore);

                    }
                }

            }
        }

        // todo create path construction method  from A* reconstruct_path();
        /*
            public void reconstruct_path()
            {
                Node current=myNodes.get(1);
                Node start=myNodes.get(0);
                ArrayList<Node> reverse_path= new ArrayList<Node>();
                ArrayList<Node> forward_path= new ArrayList<Node>();
                while (current.x!=start.x&&current.y!=start.y)
                {
                    reverse_path.add(current);
                    current=current.cameFrom;
                }
                reverse_path.add(current);

                for (int i=reverse_path.size()-1; i>=0; i--)
                {
                    forward_path.add(reverse_path.get(i));
                }
                System.out.println("the path is:");
                for (Node step:forward_path)
                {
                    System.out.print(step.toString());
                }

            }

         */
    }

    public void drawPath(Path path){

        String message = "Walking time: " + path.getWalkingTime() + " minutes\n";
        message += "Distance: " + path.getPathDistance() + " miles";

        //Draws path on map
        for(int i = 0; i < path.size()-1; i++){
            line = mMap.addPolyline(new PolylineOptions()
                    .add(path.getNode(i).getLatLog(), path.getNode(i+1).getLatLog())
                    .width(8).color(Color.BLUE)
                    .geodesic(true));
        }

        //Display ending point with walking time and distances
        Log.d("DrawPath",message);

        //TODO: Have title pull form the destination list
        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle())
                .snippet(message));

    }

    public void drawPathSlowly(Path path){

        String message = "Walking time: " + path.getWalkingTime() + " minutes\n";
        message += "Distance: " + path.getPathDistance() + " miles";

        //Draws path on map
        for(int i = 0; i < path.size()-1; i++){

            draw(path.getNode(i).getLatLog(), path.getNode(i+1).getLatLog());

            Log.d("Map","Drew first line" + i);
        }

        //Display ending point with walking time and distances
        Log.d("DrawPath",message);

        Log.d("Map","About to enter marker");

        //TODO: Have title pull form the destination list
        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle())
                .snippet(message));

        Log.d("Map","Exited marker");

    }

    private void draw(LatLng a, LatLng b){

        mMap.addPolyline(new PolylineOptions()
                .add(a, b)
                .width(8).color(Color.BLUE)
                .geodesic(true));
        try {
            Thread.sleep(200);
        }
        catch (Exception ex){
            Toast.makeText(mContext, "No sleep for the weary", Toast.LENGTH_SHORT).show();
        }

    }

    public void clearMap(){
        mMap.clear();
    }

    public void openSearchMenu(final Activity activity){

        final View inflatedView = activity.getLayoutInflater().inflate(R.layout.search_menu, null);

        //Get values from EditText fields
        final EditText destinationInput = (EditText) inflatedView.findViewById(R.id.destination_input);
        final EditText originInput = (EditText) inflatedView.findViewById(R.id.origin_input);
        final Button gpsButton = (Button) inflatedView.findViewById(R.id.calculate_origin_button);


        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
            .setTitle("Destination Search")
            .setMessage("Enter in your starting origin and your destination to calculate the route")
            .setView(inflatedView)
            .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            })
            .setPositiveButton("Let's go!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    //Starts the search with the input values
                    Log.d("TERM","" + storage.size());
                    dialog.dismiss();
                    startSearch(findSearchTerm(originInput.getText().toString().trim()), findSearchTerm(destinationInput.getText().toString().trim()));
                }
            });

        final AlertDialog alert = dialog.create();

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.cancel();
                Toast.makeText(mContext, "Finding closest data point", Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();

    }

    private Node findSearchTerm(String term){
        for(Node node : storage){
            if(node.getAliases().contains(term)){
                return node;
            }
        }
        return null;
    }

    /**After path is created by  Astar */
    private void startSearch(Node start, Node end){
        if(start == null || end == null){
            Toast.makeText(mContext, "Search failed", Toast.LENGTH_SHORT).show();
        }
        else{
            generatePath(start, end);
        }
    }

    //TODO: For debugging purposes only
    private static void addDebuggingNodes(){
        Node starting = new Node(30.271483, -81.509146);
        starting.addAlias("Student Union");
        starting.addAlias("SU");
        storage.add(starting);
        storage.add(new Node(30.271483, -81.509146));
        storage.add(new Node(30.271441, -81.509206));
        storage.add(new Node(30.271413, -81.509300));
        storage.add(new Node(30.271343, -81.509472));
        storage.add(new Node(30.271298, -81.509602));
        storage.add(new Node(30.271284, -81.509614));
        storage.add(new Node(30.271270, -81.509663));
        storage.add(new Node(30.271224, -81.509747));
        storage.add(new Node(30.271165, -81.509745));
        storage.add(new Node(30.271113, -81.509756));
        storage.add(new Node(30.271075, -81.509766));
        storage.add(new Node(30.271002, -81.509761));
        storage.add(new Node(30.270968, -81.509761));
        storage.add(new Node(30.270902, -81.509747));
        storage.add(new Node(30.270857, -81.509715));
        storage.add(new Node(30.270811, -81.509680));
        storage.add(new Node(30.270751, -81.509633));
        storage.add(new Node(30.270699, -81.509568));
        storage.add(new Node(30.270662, -81.509523));
        storage.add(new Node(30.270677, -81.509533));
        storage.add(new Node(30.270683, -81.509540));
        storage.add(new Node(30.270648, -81.509523));
        storage.add(new Node(30.270614, -81.509463));
        storage.add(new Node(30.270610, -81.509442));
        storage.add(new Node(30.270534, -81.509370));
        storage.add(new Node(30.270435, -81.509304));
        storage.add(new Node(30.270349, -81.509274));
        storage.add(new Node(30.270252, -81.509244));
        storage.add(new Node(30.270186, -81.509260));
        storage.add(new Node(30.270007, -81.509269));
        storage.add(new Node(30.269871, -81.509288));
        storage.add(new Node(30.269700, -81.509272));
        storage.add(new Node(30.269694, -81.509272));
        storage.add(new Node(30.269688, -81.509279));
        storage.add(new Node(30.269549, -81.509244));
        storage.add(new Node(30.269505, -81.509211));
        storage.add(new Node(30.269459, -81.509104));
        storage.add(new Node(0.269473, -81.509017));
        Node node = new Node(30.269475, -81.508936);
        node.addAlias("Thomas G Carpenter Library");
        node.addAlias("Lib");
        node.addAlias("Library");
        storage.add(node);
    }

    //TODO: For debugging purposes only
    private Path createFakePath(){

        Path path = new Path();
        path.add(new LatLng(30.271483, -81.509146));
        path.add(new LatLng(30.271441, -81.509206));
        path.add(new LatLng(30.271413, -81.509300));
        path.add(new LatLng(30.271343, -81.509472));
        path.add(new LatLng(30.271298, -81.509602));
        path.add(new LatLng(30.271284, -81.509614));
        path.add(new LatLng(30.271270, -81.509663));
        path.add(new LatLng(30.271224, -81.509747));
        path.add(new LatLng(30.271165, -81.509745));
        path.add(new LatLng(30.271113, -81.509756));
        path.add(new LatLng(30.271075, -81.509766));
        path.add(new LatLng(30.271002, -81.509761));
        path.add(new LatLng(30.270968, -81.509761));
        path.add(new LatLng(30.270902, -81.509747));
        path.add(new LatLng(30.270857, -81.509715));
        path.add(new LatLng(30.270811, -81.509680));
        path.add(new LatLng(30.270751, -81.509633));
        path.add(new LatLng(30.270699, -81.509568));
        path.add(new LatLng(30.270662, -81.509523));
        path.add(new LatLng(30.270677, -81.509533));
        path.add(new LatLng(30.270683, -81.509540));
        path.add(new LatLng(30.270648, -81.509523));
        path.add(new LatLng(30.270614, -81.509463));
        path.add(new LatLng(30.270610, -81.509442));
        path.add(new LatLng(30.270534, -81.509370));
        path.add(new LatLng(30.270435, -81.509304));
        path.add(new LatLng(30.270349, -81.509274));
        path.add(new LatLng(30.270252, -81.509244));
        path.add(new LatLng(30.270186, -81.509260));
        path.add(new LatLng(30.270007, -81.509269));
        path.add(new LatLng(30.269871, -81.509288));
        path.add(new LatLng(30.269700, -81.509272));
        path.add(new LatLng(30.269694, -81.509272));
        path.add(new LatLng(30.269688, -81.509279));
        path.add(new LatLng(30.269549, -81.509244));
        path.add(new LatLng(30.269505, -81.509211));
        path.add(new LatLng(30.269459, -81.509104));
        path.add(new LatLng(30.269473, -81.509017));
        Node node = new Node(30.269475, -81.508936);
        node.addAlias("Thomas G Carpenter Library");
        path.add(node);
        return path;
    }


    private void generatePath(Node start, Node end){

        //Clear the map before drawing over it again.
        mMap.clear();

        final Path path = createFakePath();             //TODO: Replace createFakePath with the call with the path from A*(start, end)
        new Thread(){
            @Override
            public void run(){
                drawPath(path);
            }
        }.run();

        //Center path if necessary
        LatLng midpoint = LocationUtils.getMidpoint(start.getLatLog(), end.getLatLog());
        MapCenteringUtils.mapMoveAndZoomTo(mMap, midpoint, 18);
    }

}
