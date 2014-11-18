package net.simplyadvanced.unfpathfinder.Search;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            mMap.clear();
            mContext = context;

            //TODO: Delete this when done with debugging
            addDebuggingNodes();

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


        AlertDialog.Builder dialog = new AlertDialog.Builder(activity)
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

        dialog.show();

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
