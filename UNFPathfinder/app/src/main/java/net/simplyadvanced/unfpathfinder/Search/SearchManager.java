package net.simplyadvanced.unfpathfinder.Search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

/**
 * 
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
    private Node closest = null;
    private static ArrayList<Path> paths;

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
            mMap = map;
            mMap.clear();
            mContext = context;
            mSearchManager = new SearchManager();

            //TODO: Delete this when done with debugging
            //addDebuggingNodes();

        }
        return mSearchManager;
    }

    public void loadNodes(){
        //done todo designate targetfile
        // String targetFile="rawNodestxt";
        String[] inputArray;
        String[][] inputFile;
        ArrayList<String> inputList = new ArrayList<String>();
        String[] latlongStrings=new String[1];
        double lat;
        double log;
        Node myNode;
        LatLng coordinates;
        InputStream myInputStream;

        try
        {

            myInputStream = mContext.getResources().openRawResource(R.raw.rawnodes);
            Log.d("debug", "The file was found");
        }
        catch (Exception e)
        {
            // Catch exception if any
            Toast.makeText(mContext,"The file was not found", Toast.LENGTH_SHORT).show();
            //System.err.println("Error: " + e.getMessage()+"\n");
            Log.d("debug", "The file was not found");

            return;
        }


        DataInputStream dataInput = new DataInputStream(myInputStream);
        BufferedReader inputBuffer = new BufferedReader(new InputStreamReader(dataInput));
        String inputString;
        try {
            while ((inputString = inputBuffer.readLine()) != null) {
                //inputString = inputString.trim();
                if ((inputString.length() != 0)) {
                    inputList.add(inputString);
                }
            }
        }
        catch (IOException s)
        {
            Log.d("debug", "IOException");
            Log.d("debug", s.getMessage());
            Log.d("debug", s.getStackTrace().toString());
        }


        Log.d("debug", "processing inputs");
        inputArray = (String[]) inputList.toArray(new String[inputList.size()]);
        inputFile = new String[inputArray.length][];
        for (int i=0; i<inputArray.length; i++)
        {

            String test="";
            inputFile[i]=inputArray[i].split(";");
            for (int j=0; j<inputFile[i].length; j++)
            {
                test+=inputFile[i][j]+"/";

            }
            Log.d("debug", "Lines tokenized by semicolons Line is: "+test);

        }

        //int pants=0;

        for (int i=0; i<inputFile.length; i++) {
            //ignore commented lines
            if(!inputFile[i][0].equals(""));
            {
                if (inputFile[i][0].substring(0, 1).equals("//"))
                {
                    Log.d("debug", "InputFile["+i+"][0] is empty");
                    continue;
                }
                else
                {
                    Log.d("debug", "Got a line with a node");

                    latlongStrings = inputFile[i][0].split("\\s+", 2);
                    lat=Double.parseDouble(latlongStrings[0].substring(0,latlongStrings[0].length()-1));//removes comma after lattitude
                    if (latlongStrings[1].contains(";"))
                    {
                        latlongStrings[1]=latlongStrings[1].substring(0,latlongStrings[1].length()-2); //removes semicolen
                    }
                    log=Double.parseDouble(latlongStrings[1]);
                    myNode=new Node (lat, log);
                    myNode.setRawAdjacency(inputFile[i]);
                    myNode.setNumber(i);//this may be useless after troubleshooting

                    for (int j=1; j<inputFile[i].length; j++)
                    {
                        //todo need a cleaner way to check if numeric or a name
                        try
                        {
                            coordinates=nodeCoords(inputFile[i][j]);
                            for (Node otherNode: storage )
                            {
                                if (otherNode.getLatLog().toString().equals(coordinates.toString()))
                                {
                                    Log.d("debug", "Ajacency:"+myNode.toString()+" "+otherNode.toString());
                                    myNode.setAdjacent(otherNode);
                                }
                            }
                        }
                        catch (NumberFormatException n)
                        {

                            myNode.addAlias(inputFile[i][j].toLowerCase().trim());
                        }
                    }
                    storage.add(myNode);
                    Log.d("debug", ("Added node: "+myNode.toString()));
                }
            }
            //Log.d("debug", "out of the if");
            //TODO:  Never makes it outside of the if statement above



        }

        for (Node oneNode: storage)//Ugly ugly hack
        {
            for (String oneToken: oneNode.getRawAdjacency())
            {
                for (Node anotherNode: storage)
                {
                    if (oneToken.equals(anotherNode.getRawAdjacency()[0]))
                    {
                        oneNode.setAdjacent(anotherNode);
                        anotherNode.setAdjacent(oneNode);
                    }
                }
            }
        }
    }

    private LatLng nodeCoords(String input) throws NumberFormatException
    {
        double lat;
        double log;
        LatLng out;
        String[] latlongStrings;
        {
            latlongStrings=input.split(",");
        }
        lat=Double.parseDouble(latlongStrings[0]);
        if (latlongStrings[1].contains(";"))
        {
            latlongStrings[1]=latlongStrings[1].substring(0,latlongStrings[1].length()-2); //removes semicolen
        }
        log=Double.parseDouble(latlongStrings[1]);
        out=new LatLng(lat,log);
        return out;

    }

    //passed from aaron's A* project
    public static Path aStar(Node start, Node finish) {

        //Reset ArrayList
        paths = new ArrayList<Path>();

        Path path = new Path();
        path.add(start);
        addPath(path, finish);
        Path current = path;
        //ArrayList<Node> ad;
        int count = 0;

        Log.d("Optimal Path","Goal node is " + finish.getNumber());

        while(!current.isFinished(finish)){

            //Make a new path for each adjacency
            for(int i = 0; i < current.getLastNode().getAdjacency().size(); i++){

                if(current.contains(current.getLastNode().getAdjacency().get(i))) continue;

                Path p = new Path();

                //Copy the old path and add the adjacency to a new one to be added to paths
                p.copy(current);
                p.add(current.getLastNode().getAdjacency().get(i));
                //Log.d("Optimal Path", "Added " + current.getLastNode().getAdjacency().get(i) + " to the path " + p.toString());

                addPath(p, finish);
            }

            //Remove the path that was just expanded
            paths.remove(current);

            //Update current to the shortest path
            current = paths.get(0);
            //Log.d("Optimal Path", "Shorest path is " + current.toString());
            Log.d("Optimal Path", paths.toString());

            //if(count++ == 5) break;

        }

        //Returns the optimal path
        return paths.get(0);

    }

    public static Path reconstruct_path(Node start, Node finish) {
        Node current;
        Path output = new Path();
        Path backwardsoutput = new Path();
        backwardsoutput.add(finish);
        current = finish.getCameFrom();
        //what is the tolerence on LatLng.equals
        while (!(current == start)) {
            backwardsoutput.add(current);
            current = current.getCameFrom();
        }
        backwardsoutput.add(current);
        //output= new Path();
        while (!backwardsoutput.nodes.isEmpty()) {
            output.add(backwardsoutput.nodes.get(backwardsoutput.size() - 1));
            backwardsoutput.nodes.remove(backwardsoutput.size() - 1);
        }
        return output;
    }


    public void drawPath(Path path) {

        String message = "Walking time: " + path.getWalkingTime() + " minutes\n";
        message += "Distance: " + path.getPathDistance() + " miles";

        //Draws path on map
        for (int i = 0; i < path.size() - 1; i++) {
            line = mMap.addPolyline(new PolylineOptions()
                    .add(path.getNode(i).getLatLog(), path.getNode(i + 1).getLatLog())
                    .width(8).color(Color.BLUE)
                    .geodesic(true));
        }

        //Display ending point with walking time and distances
        Log.d("DrawPath", message);

        //TODO: Have title pull form the destination list
        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle())
                .snippet(message));

    }

    public void drawPathSlowly(Path path) {

        String message = "Walking time: " + path.getWalkingTime() + " minutes\n";
        message += "Distance: " + path.getPathDistance() + " miles";

        //Draws path on map
        for (int i = 0; i < path.size() - 1; i++) {

            draw(path.getNode(i).getLatLog(), path.getNode(i + 1).getLatLog());

            Log.d("Map", "Drew first line" + i);
        }

        //Display ending point with walking time and distances
        Log.d("DrawPath", message);

        Log.d("Map", "About to enter marker");

        //TODO: Have title pull form the destination list
        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle())
                .snippet(message));

        Log.d("Map", "Exited marker");

    }

    private void draw(LatLng a, LatLng b) {

        mMap.addPolyline(new PolylineOptions()
                .add(a, b)
                .width(8).color(Color.BLUE)
                .geodesic(true));
        try {
            Thread.sleep(200);
        } catch (Exception ex) {
            Toast.makeText(mContext, "No sleep for the weary", Toast.LENGTH_SHORT).show();
        }

    }

    public void clearMap() {
        mMap.clear();
    }

    public void openSearchMenu(final Activity activity) {

        final View inflatedView = activity.getLayoutInflater().inflate(R.layout.search_menu, null);

        //Get values from EditText fields
        final EditText destinationInput = (EditText) inflatedView.findViewById(R.id.destination_input);
        final EditText originInput = (EditText) inflatedView.findViewById(R.id.origin_input);
        final Button gpsButton = (Button) inflatedView.findViewById(R.id.calculate_origin_button);


        final AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
                .setTitle("Destination Search")
                .setMessage("Enter in your starting origin and your destination to calculate the route")
                .setView(inflatedView)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Let's go!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        //Starts the search with the input values
                        Log.d("TERM", "" + storage.size());

                        dialog.dismiss();
                        startSearch(findSearchTerm(originInput.getText().toString().toLowerCase().trim()), findSearchTerm(destinationInput.getText().toString().toLowerCase().trim()));
                    }
                });

        final AlertDialog alert = dialog.create();

        gpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext, "Finding closest data point", Toast.LENGTH_SHORT).show();

                double distance;
                double shortestDistance = 10000;
                LatLng currentPosition = LocationUtils.getCurrentPosition(mContext);

                //Find the closest node
                for (Node node : storage) {
                    distance = LocationUtils.calculateDistance(node.getLatLog().latitude, node.getLatLog().longitude, currentPosition.latitude, currentPosition.longitude);

                    if (distance < shortestDistance) {
                        shortestDistance = distance;
                        closest = node;
                    }
                }

                //Send node to output box
                originInput.setText("#" + currentPosition.toString());


            }
        });

        alert.show();

    }

    private Node findSearchTerm(String term) {

        if (term.contains("#") && closest != null) {
            return closest;
        }

        for (Node node : storage) {
            //Log.d("findSearchTerm","Checking " + node.getAliases().toString() + " for match to " + term);
            if (node.getAliases().contains(term)) {
                //Log.d("findSearchTerm","Match found");
                return node;
            }
        }

        Toast.makeText(mContext, "Search failed on " + term, Toast.LENGTH_SHORT).show();

        return null;
    }

    /**
     * After path is created by  Astar
     */
    private void startSearch(Node start, Node end) {
        if (start == null || end == null) {
            Toast.makeText(mContext, "Search failed", Toast.LENGTH_SHORT).show();
        } else {
            generatePath(start, end);
        }
    }

    //TODO: For debugging purposes only
    private static void addDebuggingNodes() {
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
    private Path createFakePath() {

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


    private void generatePath(Node start, Node end) {

        //Clear the map before drawing over it again.
        mMap.clear();

        final Path path = aStar(start, end);             //TODO: Replace createFakePath with the call with the path from A*(start, end)
        Log.d("generatePath", path.toString());
        Log.d("generatePath", "Size: " + path.size());
        /*new Thread(){
            @Override
            public void run(){*/
        drawPath(path);
            /*}
        }.run();*/

        //Center path if necessary
        LatLng midpoint = LocationUtils.getMidpoint(start.getLatLog(), end.getLatLog());
        MapCenteringUtils.mapMoveAndZoomTo(mMap, midpoint, 18);
    }


    private static void addPath(Path path, Node goal) {

        int size = paths.size();

        if (paths.size() == 0) {
            paths.add(path);
        }
        else {
            for (int i = 0; i < size; i++) {
                if (path.getHeuristicsDistance(goal) < paths.get(i).getHeuristicsDistance(goal)) {
                    if (i == 0) {
                        paths.add(0, path);
                        return;
                    } else {
                        paths.add(i, path);
                        return;
                    }
                } else if (path.getHeuristicsDistance(goal) == paths.get(i).getHeuristicsDistance(goal)) {
                    paths.add(i, path);
                    return;
                }
                //Is the last path to compare against
                else if (i == size - 1) {
                    paths.add(path);
                    return;
                }
            }
        }


    }
}
