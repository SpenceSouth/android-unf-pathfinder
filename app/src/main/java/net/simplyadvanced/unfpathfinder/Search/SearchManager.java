package net.simplyadvanced.unfpathfinder.Search;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.BaseKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
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
    private static Activity mActivity;

    private SearchManager(){
        //loadNodes();
        new LoadNodesTask().execute();
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

    public static SearchManager getInstance(GoogleMap map, Context context, Activity activity){
        Log.d("SearchManager","creating");
        if(mSearchManager == null){
            mMap = map;
            mMap.clear();
            mContext = context;
            mActivity = activity;
            mSearchManager = new SearchManager();

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
            //Log.d("debug", "The file was found");
        }
        catch (Exception e)
        {
            // Catch exception if any
            Toast.makeText(mContext,"The file was not found", Toast.LENGTH_SHORT).show();
            //System.err.println("Error: " + e.getMessage()+"\n");
           // Log.d("debug", "The file was not found");

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
           /* Log.d("debug", "IOException");
            Log.d("debug", s.getMessage());
            Log.d("debug", s.getStackTrace().toString());*/
        }


        //Log.d("debug", "processing inputs");
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
            //Log.d("debug", "Lines tokenized by semicolons Line is: "+test);

        }

        //int pants=0;

        for (int i=0; i<inputFile.length; i++) {
            //ignore commented lines
            if(!inputFile[i][0].equals(""));
            {
                if (inputFile[i][0].substring(0, 1).equals("//"))
                {
                    //Log.d("debug", "InputFile["+i+"][0] is empty");
                    continue;
                }
                else
                {
                    //Log.d("debug", "Got a line with a node");

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

                        try
                        {
                            coordinates=nodeCoords(inputFile[i][j]);
                            for (Node otherNode: storage )
                            {
                                if (otherNode.getLatLog().toString().equals(coordinates.toString()))
                                {
                                    //Log.d("debug", "Ajacency:"+myNode.toString()+" "+otherNode.toString());
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
                    //Log.d("debug", ("Added node: "+myNode.toString()));
                }
            }
            //Log.d("debug", "out of the if");



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

    //pased from aaron's A* project
    public static Path aStar(Node start, Node finish)
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
        Path output=new Path();
        for (Node everyNode: storage)
        {

            everyNode.setDistance(everyNode.getDistanceTo(finish));
            //reinitalize rawnodes
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
            Log.d("debug", ("Current is: "+current.getLatLog().toString()));

            if (current==finish)
            {
                //Path output;
                output=reconstruct_path(start,  finish);
                /*
                Path backwardsoutput = new Path();
                backwardsoutput.add(finish);
                current=finish.getCameFrom();
                while (!current.getLatLog().equals(start.getLatLog()))
                {
                    backwardsoutput.add(current);
                    current=current.getCameFrom();
                }
                backwardsoutput.add(current);
                //output= new Path();
                while (!backwardsoutput.nodes.isEmpty())
                {
                    output.add(backwardsoutput.nodes.get(backwardsoutput.size()-1));
                    backwardsoutput.nodes.remove(backwardsoutput.size()-1);
                }
                */

                Log.d("debug", "Exiting A* from the right spot");
                return output;
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
                        Log.d("debug", "Added"+neighbor.getLatLog().toString()+"to open Set");
                        //System.out.println("Mr Bond went from "+current.toString()+" to "+neighbor.toString()+"\n Tenative score is:"+tenGscore);

                    }
                }

            }

        }


        Log.d("debug", "Exiting A* from the wrong spot look at brackets"+current.getLatLog().toString());

        output=reconstruct_path(start, current);
        return output;
    }

    public static Path reconstruct_path(Node start, Node finish)
    {
        Node current;
        Path output=new Path();
        Path backwardsoutput = new Path();
        backwardsoutput.add(finish);
        current=finish.getCameFrom();
        //what is the tolerence on LatLng.equals
        while (!(current==start))
        {
            backwardsoutput.add(current);
            current=current.getCameFrom();
        }
        backwardsoutput.add(current);
        //output= new Path();
        while (!backwardsoutput.nodes.isEmpty())
        {
            output.add(backwardsoutput.nodes.get(backwardsoutput.size()-1));
            backwardsoutput.nodes.remove(backwardsoutput.size()-1);
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

        marker = mMap.addMarker(new MarkerOptions()
                .position(path.getEndingNode().getLatLog())
                .title("Destination: " + path.getEndingNode().getTitle().toUpperCase())
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

    public void openSearchMenu() {

        final View inflatedView = mActivity.getLayoutInflater().inflate(R.layout.search_menu, null);

        //Get values from EditText fields
        final EditText destinationInput = (EditText) inflatedView.findViewById(R.id.destination_input);
        final EditText originInput = (EditText) inflatedView.findViewById(R.id.origin_input);
        final Button gpsButton = (Button) inflatedView.findViewById(R.id.calculate_origin_button);

        originInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!checkEntry(originInput.getText().toString().toLowerCase())){
                    originInput.setTextColor(Color.RED);
                }
                else{
                    originInput.setTextColor(Color.rgb(102, 153, 0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        destinationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!checkEntry(destinationInput.getText().toString().toLowerCase())){
                    destinationInput.setTextColor(Color.RED);
                }
                else{
                    destinationInput.setTextColor(Color.rgb(102, 153, 0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        final AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity)
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

    private void generatePath(Node start, Node end) {

        //Clear the map before drawing over it again.
        mMap.clear();

        //Easter Egg
        if(start.getAliases().contains("the shire") && end.getAliases().contains("mordor")){
            Toast.makeText(mContext, "One does not simply walk into Mordor", Toast.LENGTH_SHORT).show();
        }

        final Path path = aStar(start, end);

        drawPath(path);

        //Center path if necessary
        LatLng midpoint = LocationUtils.getMidpoint(start.getLatLog(), end.getLatLog());

        Log.d("Zoom","h(n): " + LocationUtils.calculateDistance(start, end));

        int zoomLevel = 18;
        double distance = LocationUtils.calculateDistance(start, end);

        if(distance < .20){
            zoomLevel = 18;
        }
        else if(distance > .65 && distance < .899){
            zoomLevel = 16;
        }
        else if(distance > .9 && distance < 1.499){
            zoomLevel = 15;
        }
        else if(distance > 1.5){
            zoomLevel = 14;
        }
        else {
            zoomLevel = 17;
        }


        MapCenteringUtils.mapMoveAndZoomTo(mMap, midpoint, zoomLevel);
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

    private boolean checkEntry(String input){

        if(input.contains("#")){
            return true;
        }

        for (Node node : storage) {
            //Log.d("findSearchTerm","Checking " + node.getAliases().toString() + " for match to " + term);
            if (node.getAliases().contains(input)) {
                Log.d("CheckEntry","Match found for " + input);
                return true;
            }
        }
        Log.d("CheckEntry","Match not found for " + input);

        return false;

    }

    /*public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_slow:
                if (checked)
                    Toast.makeText(mContext, "Slow", Toast.LENGTH_SHORT).show();
                break;
            case R.id.radio_medium:
                if (checked)
                    Toast.makeText(mContext, "Medium", Toast.LENGTH_SHORT).show();
                break;
            case R.id.radio_fast:
                if (checked)
                    Toast.makeText(mContext, "Fast", Toast.LENGTH_SHORT).show();
                break;
        }
    }*/

    /**Loads nodes in a background thread */
    class LoadNodesTask extends AsyncTask<Void, Integer, Void> {

        ProgressDialog progress = new ProgressDialog(mContext);


        @Override
        protected void onPreExecute() {

            // prepare for a progress bar dialog
            progress = new ProgressDialog(mActivity);
            progress.setMessage("Loading");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            progress.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Void doInBackground(Void... params) {

            loadNodes();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progress.dismiss();
        }
    }
}
